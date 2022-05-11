import cats.effect._
import com.comcast.ip4s._
import config.DI.httpResource
import org.http4s.dsl.io._
import org.http4s.ember.server._
import org.http4s.implicits._
import org.http4s.server.middleware.Logger

object School extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    httpResource.use { httpRes =>
      EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(Logger.httpApp(logHeaders = true, logBody = true)(httpRes))
        .build
        .use(_ => IO.never)
        .as(ExitCode.Success)
    }
  }
}
