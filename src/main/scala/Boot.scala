import java.io.File

import actors.S3
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.regions.Regions
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model._
import org.slf4j.LoggerFactory
import akka.actor.ActorSystem
import akka.http.javadsl.model.StatusCodes
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import spray.json.DefaultJsonProtocol._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import com.amazonaws.AmazonServiceException
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import models.{Body, ErrorInfo}

import scala.concurrent.duration._
import collection.JavaConverters._

case class Response(msg: String, statusCode: StatusCode)

object Boot extends App with JsonSupport {
  var bucketName: String = "bhle-lab"
  var mainPath: String = "src/main/resources/s3/"
  implicit val timeout = Timeout(30.seconds)

  // needed to run the route
  implicit val system = ActorSystem()

  implicit val materializer = ActorMaterializer()
  // needed for the future map/flatmap in the end and future in fetchItem and saveOrder
  implicit val executionContext = system.dispatcher

  val log = LoggerFactory.getLogger("Boot")

  val awsCreds = new BasicAWSCredentials(
    "AKIAXLVB2ODHGBSRCT5R",
    "CPvVvX8tcjuUlzhtmkg5giUigRlX8l9uvVfb6A/U")

  // Frankfurt client
  val s3Client: AmazonS3 = AmazonS3ClientBuilder.standard
    .withCredentials(new AWSStaticCredentialsProvider(awsCreds))
    .withRegion(Regions.EU_CENTRAL_1)
    .build
  // check if bucket exists

  if (s3Client.doesBucketExistV2("bhle-lab")) {
    log.info("Bucket exists")
  } else {
    s3Client.createBucket("bhle-lab")
    log.info("Bucket created")
  }

  val s3interaction = system.actorOf(S3.props(s3Client, mainPath, bucketName), "s3interaction")


  val route = path("s3") {
    get{
      parameters('path.as[String]) { path =>
        complete {
          (s3interaction ? S3.Download(path)).mapTo[ErrorInfo]
        }
      }
    }~
    post{
      entity(as[Body]) { body =>
        complete {
          (s3interaction ? S3.Upload(body.path)).mapTo[ErrorInfo]
        }
      }
    }
  }



  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)
  log.info("Listening on port 8080...")




}
