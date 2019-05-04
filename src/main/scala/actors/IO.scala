package actors

import java.io.File

import akka.actor.{Actor, ActorRef, Props}
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import com.amazonaws.AmazonServiceException
import models.{Body, ErrorInfo}
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.{GetObjectRequest, ListObjectsRequest, ObjectListing, PutObjectResult}

object IO {

  def props(s3Client:AmazonS3, bucketName: String, file: File, mainPath: String) = Props(new IO(s3Client, bucketName, file, mainPath))

  object OUT

  object IN
}

class IO(s3Client:AmazonS3, bucketName: String, file: File, mainPath: String) extends Actor{
  import IO._

  override def preStart() = println("IO actor created")

  override def receive: Receive = {
    case OUT =>
      dfs(file, "")
      sender() ! ErrorInfo("OK", 200)
    case IN =>


      val listObjectsRequest = new ListObjectsRequest().withBucketName(bucketName)
      val list: ObjectListing = s3Client.listObjects(bucketName)
      list.getObjectSummaries().forEach(key => downloadFromS3(key.getKey()))
      sender() ! ErrorInfo("OK", 200)

  }


  def dfs(file: File, path: String): Unit = {
    if (file.isFile()) {
      uploadToS3(path.substring(1))
    }else {
      file.listFiles(!_.isHidden()).foreach(file => dfs(file, path + "/" + file.getName()))
    }
  }

  def uploadToS3(path: String): Boolean = {
    val filePath: String = mainPath + "out/" + path
    //println(s"uploaded from filepath: $filePath to s3 with path: $path")

    val file = new File(filePath)
    if (!file.exists()) {
      return false;
    }
    s3Client.putObject(bucketName, path, file)
    println(s"uploaded from filepath: $filePath to s3 with path: $path")
    return true;
  }

  def fileIsUploadedToS3(uploadPath: String): Boolean = {
    return s3Client.doesObjectExist(bucketName, uploadPath)
  }

  def downloadFromS3(uploadPath: String) {
    if (uploadPath.takeRight(1) == "/") {
      return
    }

    val downloadPath: String = mainPath + "in/" + uploadPath

    if(!fileIsUploadedToS3(uploadPath)) {
      throw new RuntimeException(s"File $uploadPath is not uploaded!")
    }

    var dirPath: String = downloadPath.substring(0, downloadPath.lastIndexOf('/'))
    var newDir = new File(dirPath)
    newDir.mkdir()

    s3Client.getObject(new GetObjectRequest(bucketName, uploadPath),
      new File(downloadPath))
    println(s"downloaded from downloadpath $uploadPath s3 to path: $downloadPath")
  }


}
