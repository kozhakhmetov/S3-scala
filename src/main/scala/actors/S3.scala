package actors

import java.io.File

import akka.actor.{Actor, Props}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import com.amazonaws.AmazonServiceException

import models.{Body, ErrorInfo}

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{GetObjectRequest, PutObjectResult}

object S3 {

  def props(s3Client: AmazonS3, mainPath: String, bucketName: String) = Props(new S3(s3Client, mainPath, bucketName))

  case class Download(path: String)

  case class Upload(path: String)
}

class S3(s3Client: AmazonS3, mainPath: String, bucketName: String) extends Actor{
  import S3._

  override def preStart() = println("s3 actor created")


  override def receive: Receive = {
    case Download(path) =>
      println(s"Download request with path: $path")
      if (s3Client.doesObjectExist(bucketName, path)) {
        downloadFromS3(path)
        sender() ! ErrorInfo("OK", 200)
      }else {
        sender() ! ErrorInfo("Not Found", 404)
      }
    case Upload(path) =>
      println(s"Upload request with path: $path")
      uploadToS3(path)
      sender() ! ErrorInfo("OK", 200)
  }


  def uploadToS3(path: String): Unit = {
    val file = new File(mainPath + path)
    s3Client.putObject(bucketName, path, file)
    println(s"uploaded to s3 with path: $path")
  }

  def fileIsUploadedToS3(uploadPath: String): Boolean = {
    return s3Client.doesObjectExist(bucketName, uploadPath)
    try {
      s3Client.getObjectMetadata(bucketName, uploadPath)
      true
    } catch {
      case e: AmazonServiceException if e.getStatusCode == 404 =>
        false
    }
  }

  def downloadFromS3(uploadPath: String) {
    val downloadPath: String = mainPath + uploadPath

    if(!fileIsUploadedToS3(uploadPath)) {
      throw new RuntimeException(s"File $uploadPath is not uploaded!")
    }

    var dirPath: String = downloadPath.substring(0, downloadPath.lastIndexOf('/'))
    var newDir = new File(dirPath)
    newDir.mkdir()

    s3Client.getObject(new GetObjectRequest(bucketName, uploadPath),
      new File(downloadPath))
    println(s"downloaded from s3 to path: $uploadPath")
  }

}
