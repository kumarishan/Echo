package sdslabs.echo.utils

import java.util.UUID
import scala.collection.mutable.Map
import akka.serialization.Serializable.ScalaJSON
import akka.serialization.JsonSerialization._
import akka.serialization.DefaultProtocol._
import akka.serialization._

object EchoMessage {

  case class StartClustering()
  case class EchoReClusterAll( docDir: String)
  case class AddDocument(location : String)
  case class GetRecommendation( uuid : String)
  case class Query(query : String)
  case class QueryReply(result: Map[Float, UUID])
  case class RecommenderReply(res: List[Map[String,String]])
}