package top.criwits.scaleda
package kernel.shell

import kernel.project.config.ProjectConfig
import kernel.toolchain.Toolchain
import kernel.utils.{JsonHelper, KernelLogger}

import scopt.OParser
import top.criwits.scaleda.kernel.net.RemoteServer

import java.io.File

object ShellRunMode extends Enumeration {
  val None, Simulation, ListProfiles, ListTasks, Serve = Value
}

case class ShellArgs
(workingDir: File = new File("."),
 runMode: ShellRunMode.Value = ShellRunMode.None,
 serverHost: String = "",
 serverPort: Int = RemoteServer.port,
 runSimulation: String = "iverilog")

object ScaledaShellMain {
  def main(args: Array[String]): Unit = {
    KernelLogger.info(s"Scaleda shell! args: ${args.mkString(" ")}")
    val builder = OParser.builder[ShellArgs]
    val parser = {
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
          .text("Set server port for RPC"),
        cmd("serve")
          .text("Run as server")
          .action((_, c) => c.copy(runMode = ShellRunMode.Serve)),
        cmd("profiles")
          .text("Show loaded profiles")
          .action((_, c) => c.copy(runMode = ShellRunMode.ListProfiles)),
        cmd("tasks")
          .text("Show loaded tasks")
          .action((_, c) => c.copy(runMode = ShellRunMode.ListTasks)),
        cmd("sim")
          .text("Run simulation")
          .action((_, c) => c.copy(runMode = ShellRunMode.Simulation))
          .children(
            opt[String]('s', "simulator")
              .action((x, c) => c.copy(runSimulation = x))
            // .text(s"Run simulation, available: ${Simulator.simulators.keys.mkString(", ")}"),
            ,
            // opt[String]("top")
            //   .action((x, c) => c.copy(simulationConfig = c.simulationConfig.copy(topModule = x)))
            //   .text("Specify top module")
          ),
        cmd("synth"),
        cmd("impl"),
        cmd("debug"),
        cmd("program"),
        help("help").text("Prints this usage text"),
      )
    }
    OParser.parse(parser, args, ShellArgs()) match {
      case Some(shellConfig) => {
        KernelLogger.info(s"shell config: ${shellConfig}")
        val projectConfigFile = new File(shellConfig.workingDir, ProjectConfig.defaultConfigFile)
        if (projectConfigFile.exists() && !projectConfigFile.isDirectory) {
          ProjectConfig.configFile = Some(projectConfigFile.getAbsolutePath)
          val config = ProjectConfig.getConfig()
          KernelLogger.info(s"project config: ${config}")
        } else {
          KernelLogger.info("no project config detected!")
        }
        shellConfig.runMode match {
          case ShellRunMode.ListProfiles => {
            KernelLogger.info("profile list:")
            for (p <- Toolchain.profiles()) {
              KernelLogger.info(s"${JsonHelper(p)}")
            }
          }
          case ShellRunMode.ListTasks => {
            KernelLogger.info("task list:")
            ProjectConfig.getConfig().map(config =>
              for (p <- config.tasks) {
                KernelLogger.info(s"${JsonHelper(p)}")
              }).getOrElse(KernelLogger.info("no task loaded"))
          }
          case ShellRunMode.Serve => {
            // run as server
            RemoteServer.start()
          }
          // case ShellRunMode.Simulation => {
          //   if (!Simulator.simulators.keys.toSeq.contains(shellConfig.runSimulation)) {
          //     KernelLogger.info(s"not supported simulator ${shellConfig.runSimulation}")
          //   } else {
          //     val simulator = Simulator.simulators(shellConfig.runSimulation)(
          //       shellConfig.simulationConfig.copy(
          //         sourceDir = config.getSourcePath(shellConfig.workingDir),
          //         workingDir = new File(shellConfig.workingDir.getAbsolutePath, shellConfig.simulationConfig.workingDir.getPath)
          //       ))
          //     val returnValue = simulator.simulate()
          //   }
          // }
          case _ => {
            KernelLogger.info("not implemented.")
          }
        }
      }
      case _ => {}
    }
  }
}
