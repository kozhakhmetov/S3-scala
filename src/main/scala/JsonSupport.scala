import spray.json.RootJsonFormat
import spray.json.DefaultJsonProtocol._
import models.{Body, ErrorInfo}

trait JsonSupport {
  implicit val bodyFormat: RootJsonFormat[Body] = jsonFormat1(Body)
  implicit val errorInfoFormat = jsonFormat2(ErrorInfo)
}