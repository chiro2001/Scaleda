package top.criwits.scaleda
package idea.runner.configuration

import idea.runner.ScaledaRunProcessHandler
import idea.utils.{ConsoleLogger, MainLogger}
import kernel.project.config.ProjectConfig
import kernel.shell.ScaledaRun

import com.intellij.execution.configurations.{
  LocatableConfigurationBase,
  RunConfiguration,
  RunProfileState
}
import com.intellij.execution.filters.TextConsoleBuilderFactory
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.{ExecutionEnvironment, ProgramRunner}
import com.intellij.execution.ui.ExecutionConsole
import com.intellij.execution.{ExecutionResult, Executor}
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import com.intellij.psi.search.ExecutionSearchScopes

import java.io.File
import scala.collection.mutable

class ScaledaRunConfiguration(
    project: Project,
    factory: ScaledaRunConfigurationFactory,
    name: String
) extends LocatableConfigurationBase[RunProfileState](project, factory, name) {

  var targetName = ""
  var taskName = ""
  val extraEnvs = new mutable.HashMap[String, String]

  override def getConfigurationEditor: SettingsEditor[_ <: RunConfiguration] =
    new ScaledaRunConfigurationEditor(project)

  override def getState(
      executor: Executor,
      environment: ExecutionEnvironment
  ): RunProfileState = {
    MainLogger.warn(s"executing: taskName=$taskName, targetName=$targetName")
    ProjectConfig
      .getConfig()
      .flatMap(c => {
        c.taskByName(taskName)
          .map(f => {
            val (target, task) = f
            val searchScope =
              ExecutionSearchScopes
                .executionScope(project, environment.getRunProfile)
            val myConsoleBuilder =
              TextConsoleBuilderFactory.getInstance
                .createBuilder(project, searchScope)
            val console = myConsoleBuilder.getConsole

            val handler =
              new ScaledaRunProcessHandler(new ConsoleLogger(console))
            val state = new RunProfileState {
              override def execute(
                  executor: Executor,
                  runner: ProgramRunner[_]
              ) = {
                new ExecutionResult {
                  override def getExecutionConsole: ExecutionConsole = console

                  override def getActions: Array[AnAction] = Array()

                  override def getProcessHandler: ProcessHandler = handler
                }
              }
            }
            ScaledaRun.runTaskBackground(
              handler,
              new File(ProjectConfig.projectBase.get),
              target,
              task
            )
            state
          })
      })
      .orNull
  }
}
