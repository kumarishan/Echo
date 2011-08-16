package sdslabs.echo.general

import akka.actor.Actor
import Actor._
import sdslabs.echo.dataprocessing.EchoClustering
import com.mongodb.Mongo
import com.mongodb.DB
import com.mongodb.DBObject

import sdslabs.echo.utils._
import sdslabs.echo.searching.EchoSearching
import java.io.File
import java.util.UUID
import sdslabs.echo.Indexing._
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap
import sdslabs.echo.recommendation.EchoRecommender
import com.mongodb.DBCollection

class EchoController extends Actor{
  
  val m = new Mongo("127.0.0.1")
  val dataStore = m.getDB("echo")
  
  val clusterer = actorOf(new EchoClustering(dataStore))
  clusterer.start()
  
  val indexing : EchoIndexing = new EchoIndexing("C:\\Users\\kumarish\\Desktop\\index\\")
  
  val extracter: EchoExtraction = new EchoExtraction();
  val recommender : EchoRecommender = new EchoRecommender(dataStore)
  
  def uploadToDataStore(info : Map[String, String] ){
    
    val doc : DBObject = extracter.toDBObject(info)
    val ds = m.getDB("echo")
    val coll : DBCollection = ds.getCollection("echo")
    coll.insert(doc)
   
  }
   
  def receive = {
    
	case EchoMessage.StartClustering => {
	  clusterer ! EchoMessage.EchoReClusterAll( "C:\\Users\\kumarish\\Desktop\\book")
	}
	case EchoMessage.Query(query : String) => {
	  val searcher : EchoSearching = new EchoSearching("C:\\Users\\kumarish\\Desktop\\index\\")
	  self.reply(EchoMessage.QueryReply(searcher.search(query)))
	}
	case EchoMessage.AddDocument(location: String) => {
	  
	  val file : File = new File(location)
	  val info : Map[String,String] = extracter.getInfo(file.getName())
	  
	  val uuid : UUID = UUID.fromString( UUID.randomUUID().toString )
	  val doc: EchoDocument = new EchoDocument(file, uuid)
	  indexing.indexDocument(doc)
	  info.put("uuid", uuid.toString)
	  uploadToDataStore(info)
	  
	}
	case EchoMessage.GetRecommendation(uuid: String) => {
	  self.reply(EchoMessage.RecommenderReply(recommender.getSimilarDocument(uuid)))
	}
	case _ => {
	  println("Error")
	}
  }
	
}