package top.criwits.scaleda
package kernel.toolchain

import idea.runner.ScaledaRuntime
import kernel.net.remote.RemoteInfo
import kernel.project.ProjectManifest

trait ToolchainPresetProvider {

  /** Execute in `runTask`, from runtime to new runtime
    * @param rt runtime
    * @param remoteInfo remote information
    * @return
    */
  def handlePreset(rtOld: ScaledaRuntime, remoteInfo: Option[RemoteInfo]): Option[ScaledaRuntime]
}
