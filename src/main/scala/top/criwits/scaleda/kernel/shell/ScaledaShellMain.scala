package top.criwits.scaleda
package kernel.shell

import kernel.net.remote.Empty
import kernel.net.{RemoteClient, RemoteServer}
import kernel.project.config.ProjectConfig
import kernel.template.Template
import kernel.toolchain.Toolchain
import kernel.utils.serialise.JSONHelper
import kernel.utils.{KernelLogger, Paths}

import scopt.OParser

import java.io.File

object ShellRunMode extends Enumeration {
  val None, Run, ListProfiles, ListTasks, Serve = Value
}

case class ShellArgs(
    task: String = "",
    workingDir: File = new File("."),
    runMode: ShellRunMode.Value = ShellRunMode.None,
    serverHost: String = "",
    serverPort: Int = RemoteServer.port
)

object ScaledaShellMain {
  private def loadConfig(projectRootPath: String): Unit = {
    val rootDir = new File(projectRootPath)
    if (rootDir.exists() && rootDir.isDirectory) {
      ProjectConfig.projectBase = Some(rootDir.getAbsolutePath)
    }
    val projectConfigFile =
      new File(projectRootPath, ProjectConfig.defaultConfigFile)
    if (projectConfigFile.exists() && !projectConfigFile.isDirectory) {
      ProjectConfig.configFile = Some(projectConfigFile.getAbsolutePath)
      val config = ProjectConfig.getConfig()
      KernelLogger.info(s"project config: ${config}")
    }
  }

  private def preParseArgs(
      args: Array[String],
      option: Seq[String]
  ): Option[String] = {
    var found = false
    args.foreach(arg => {
      if (option.contains(arg))
        found = true
      else if (found)
        return Some(arg)
    })
    None
  }

  def main(args: Array[String]): Unit = {
    KernelLogger.info(s"Scaleda shell! args: ${args.mkString(" ")}")
    Template.initJinja()

    // preparse workdir
    preParseArgs(args, Seq("-C", "--workdir")).foreach(a => loadConfig(_))
    // preparse server host
    val host = preParseArgs(args, Seq("--host"))
    if (ProjectConfig.configFile.isEmpty) {
      // try loading config in pwd
      loadConfig(Paths.pwd.getAbsolutePath)
    }
    if (ProjectConfig.configFile.isEmpty)
      KernelLogger.info("no project config detected!")
    val shellParser = {
      val builder = OParser.builder[ShellArgs]
      import builder._
      OParser.sequence(
        programName("scaleda"),
        head("scaleda", "0.1"),
        opt[File]('C', "workdir")
          .action((x, c) => c.copy(workingDir = x))
          .text("Working directory"),
        opt[String]("host")
          .action((x, c) => c.copy(serverHost = x))
          .text("Set server host for RPC"),
        opt[Int]("port")
          .action((x, c) => c.copy(serverPort = x))
          .text("Set server port for RPC")
          .hidden(),
        cmd("serve")
          .text("Run as server")
          .action((_, c) => c.copy(runMode = ShellRunMode.Serve)),
        cmd("profiles")
          .text("Show loaded profiles")
          .action((_, c) => c.copy(runMode = ShellRunMode.ListProfiles)),
        cmd("tasks")
          .text("Show loaded tasks")
          .action((_, c) => c.copy(runMode = ShellRunMode.ListTasks)),
        cmd("run")
          .text("Run task")
          .action((_, c) => c.copy(runMode = ShellRunMode.Run))
          .children(
            opt[String]('t', "task")
              .action((x, c) => c.copy(task = x))
              .text(s"Available tasks: ${ProjectConfig
                .getConfig()
                .map(config => config.taskNames.mkString(", "))
                .getOrElse("None")}")
              .validate(name =>
                if (
                  ProjectConfig
                    .getConfig()
                    .exists(c => c.taskNames.contains(name))
                )
                  success
                else failure(s"no task ${name} found!")
              )
          ),
        help("help").text("Prints this usage text")
      )
    }
    OParser
      .parse(shellParser, args, ShellArgs())
      .foreach(shellConfig => {
        val workingDir = shellConfig.workingDir
        val config = ProjectConfig.getConfig()
        KernelLogger.info(s"shell config: ${shellConfig}")
        shellConfig.runMode match {
          case ShellRunMode.ListProfiles => {
            KernelLogger.info("local profile list:")
            for (p <- Toolchain.profiles()) {
              KernelLogger.info(s"${JSONHelper(p)}")
            }
            if (shellConfig.serverHost.nonEmpty) {
              val stub =
                RemoteClient(shellConfig.serverHost, shellConfig.serverPort)
              val profiles = stub.getProfiles(Empty())
              if (profiles.profiles.nonEmpty) {
                KernelLogger.info("remote profile list:")
                for (p <- profiles.profiles)
                  KernelLogger.info(s"${JSONHelper(p)}")
              }
            }
          }
          case ShellRunMode.ListTasks => {
            KernelLogger.info("task list:")
            ProjectConfig
              .getConfig()
              .map(config =>
                for (p <- config.targets.flatMap(_.tasks)) {
                  KernelLogger.info(s"${JSONHelper(p)}")
                }
              )
              .getOrElse(KernelLogger.info("no task loaded"))
          }
          case ShellRunMode.Serve => {
            // run as server
            RemoteServer.start()
          }
          case ShellRunMode.Run => {
            config
              .map(c => {
                ProjectConfig
                  .getConfig()
                  .foreach(
                    _.taskByName(shellConfig.task)
                      .map(f => {
                        val (target, task) = f
                        ScaledaRun.runTask(
                          ScaledaRunKernelHandler,
                          workingDir,
                          target,
                          task
                        )
                      })
                      .getOrElse(KernelLogger.error("no specific task!"))
                  )
              })
              .getOrElse(KernelLogger.error("no config loaded!"))
          }
          case ShellRunMode.None => {
            KernelLogger.warn("no action specified, do nothing")
          }
          case _ => {
            KernelLogger.error("not implemented.")
          }
        }
      })
  }
}
