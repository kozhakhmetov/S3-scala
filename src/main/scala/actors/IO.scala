package actors

import java.io.File

import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import com.amazonaws.AmazonServiceException
import models.{Body, ErrorInfo}
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{GetObjectRequest, PutObjectResult}

object IO {

  def props(s3interaction: ActorRef, file: File, path: String) = Props(new IO(s3interaction, file, path))

  case class OUT(replyTo: ActorRef)

  object IN
}

class IO(s3interaction: ActorRef, file: File, path: String) extends Actor{
  import IO._

  override def preStart() = println("IO actor created")


  override def receive: Receive = {
    case OUT =>
      dfs(file, path, s3interaction)
      sender() ! ErrorInfo("OK", 200)
//    case IN =>
//      replyTo ! ErrorInfo("OK", 200)

  }


  def dfs(file: File, path: String, s3actor: ActorRef): Unit = {
    if (file.isFile()) {
      s3actor ! S3.Upload(path)
    }else {
      file.listFiles(!_.isHidden()).foreach(file => dfs(file, path + "/" + file.getName(), s3actor))
    }
  }

}
