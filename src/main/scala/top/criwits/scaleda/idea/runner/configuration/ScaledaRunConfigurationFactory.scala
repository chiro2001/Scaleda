package top.criwits.scaleda
package idea.runner.configuration

import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.openapi.project.Project

class ScaledaRunConfigurationFactory(private val runConfigurationType: ScaledaRunConfigurationType)
  extends ConfigurationFactory(runConfigurationType) {
  override def createTemplateConfiguration(project: Project) = ???
}
