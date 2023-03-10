package top.criwits.scaleda
package kernel

import kernel.net.RpcPatch
import kernel.net.fuse._
import kernel.net.fuse.fs.{FuseTransferMessage, PathModeRequest, RemoteFuseTransferGrpc}
import kernel.server.ScaledaServerMainRunTest
import kernel.shell.ScaledaShellMain
import kernel.utils.{KernelFileUtils, KernelLogger, Paths}

import com.google.protobuf.ByteString
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should

import java.io.File
import scala.concurrent.ExecutionContext

class FuseTransferTester extends AnyFlatSpec with should.Matchers {
  behavior of "FuseTransfer"

  it should "do simple transfer test" in {
    val TEST_PORT = 4551
    val thread = new Thread(() => {
      val server = RpcPatch.getStartServer(
        Seq(
          RemoteFuseTransferGrpc.bindService(new FuseTransferServer, ExecutionContext.global)
        ),
        TEST_PORT,
        enableAuthCheck = true
      )
    })
    thread.start()
    Thread.sleep(500)
    val (client, stream, observer, shutdown) =
      FuseTransferClient.asStream(new FuseDataProvider("/tmp"), port = TEST_PORT)
    stream.onNext(FuseTransferMessage.of(0, "login", ByteString.EMPTY))
    Thread.sleep(100)
    FuseTransferServer.requestThread.start()
    // val resp = FuseTransferServer.request(FuseTransferMessage(1, "token", "init", EmptyReq()))
    val resp =
      FuseTransferServer.request(FuseTransferMessageCase(1, "token", "create", PathModeRequest.of("test", 0x1ed)))
    KernelLogger.info("resp: ", resp)
    // Thread.sleep(500)
    stream.onCompleted()
    // Thread.sleep(500)
    FuseTransferServer.requestThread.interrupt()
    shutdown()
  }

  object FuseTransferClientTester {
    def run(): Unit = {
      ScaledaShellMain.main(Array("register", "-h", "localhost", "-u", "chiro2", "-p", "1234"))
      // ScaledaShellMain.main(Array("configurations", "-C", "../scaleda-sample-project"))
      ScaledaShellMain.main(
        Array("run", "-C", "../scaleda-sample-project", "-t", "Run iverilog simulation", "-h", "localhost")
      )
      // ScaledaShellMain.main(Array("run", "-C", "../scaleda-sample-project", "-h", "localhost", "-t", "Vivado Simulation"))
    }
  }

  ignore should "launch client and execute remote profile" in {
    FuseTransferClientTester.run()
  }

  it should "launch server and client to test execute" in {
    val serverThread = new Thread(() => ScaledaServerMainRunTest.main(Array()))
    serverThread.start()
    FuseTransferClientTester.run()
    // serverThread.join()
    serverThread.interrupt()
  }

  it should "test local transfer" in {
    val dataProvider   = new FuseDataProvider(Paths.pwd.getAbsolutePath)
    val observer       = new FuseTransferClientObserver(dataProvider)
    val fs             = new ServerSideFuse(new FuseLocalProxy(observer))
    val mountPoint     = "/tmp/scaledaTest"
    val mountPointFile = new File(mountPoint)
    if (mountPointFile.exists()) KernelFileUtils.deleteDirectory(mountPointFile.toPath)
    FuseUtils.mountFs(fs, mountPoint, blocking = true)
  }
}
