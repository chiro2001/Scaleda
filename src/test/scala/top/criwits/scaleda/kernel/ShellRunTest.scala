package top.criwits.scaleda
package kernel

import kernel.shell.ScaledaShellMain
import kernel.utils.EnvironmentUtils

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

class ShellRunTest extends AnyFlatSpec with should.Matchers {
  behavior of "ShellRun"
  it should "test vivado impl generate" in {
    EnvironmentUtils.Backup.env.put("SKIP_EXECUTION", "1")
    ScaledaShellMain.main(Array("run", "-C", "../scaleda-sample-project", "-t", "Vivado Implementation"))
  }

  it should "test vivado impl" in {
    ScaledaShellMain.main(Array("run", "-C", "../scaleda-sample-project", "-t", "Vivado Implementation"))
  }

  it should "test remote iverilog simulation" in {
    ScaledaShellMain.main(Array("run", "-C", "../scaleda-sample-project", "-t", "Run iverilog simulation", "-h", "172.18.189.67"))
  }

  it should "test run server" in {
    ScaledaShellMain.main(Array("serve"))
  }

  it should "test create and run" in {
    ScaledaShellMain.main(Array("create", "-C", "/home/chiro/Vivado/Tests/ip_tests"))
    ScaledaShellMain.main(Array("run", "-C", "/home/chiro/Vivado/Tests/ip_tests", "-t", "Simulation"))
  }
}
