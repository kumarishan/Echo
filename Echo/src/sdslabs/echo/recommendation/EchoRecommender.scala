package sdslabs.echo.recommendation

import akka.actor.Actor
import com.mongodb._
import sdslabs.echo.utils.EchoExtraction
import scala.collection.mutable.Map

class EchoRecommender( dataStore: DB) {
  
  val extractor = new EchoExtraction()
  
  def getSimilarDocument(uuid : String ) : List[Map[String,String]] = {
    
    val collection : DBCollection = dataStore.getCollection("echo")
    val query : BasicDBObject = new BasicDBObject()
    query.put("id", uuid)
    val cur : DBCursor = collection.find(query)
    val result: List[Map[String, String]] = List[Map[String, String]]()
    
    while(cur.hasNext){
      val book : DBObject = cur.next
      val clusterId = book.get("cluster").asInstanceOf[String]
      val search :  BasicDBObject = new BasicDBObject()
      search.put("cluster", clusterId)
      val res : DBCursor = collection.find(search)
      while(res.hasNext){
        val info : Map[String, String] = extractor.format(res.next)
        info :: result
      }
    }
    
    return result
  }
   
}