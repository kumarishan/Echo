package sdslabs.echo.general

import akka.actor.Actor
import Actor._
import java.io.File
import sdslabs.echo.utils._
import java.util.UUID
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap
import com.mongodb._


object EchoMain {
  
  
  
  
  val m = new Mongo("127.0.0.1")
  val dataStore = m.getDB("echo")
  val extracter = new EchoExtraction()
  
  val echo = actorOf(new EchoController())
  echo.start()
  
  def getInfo(id : UUID) : Map[String,String] = {
    
    val query : BasicDBObject = new BasicDBObject()
    query.put("id", id.toString)
    val coll = dataStore.getCollection("echo")
    val cur = coll.find(query)
    var res : Map[String,String] = new HashMap[String,String]()
    
    while(cur.hasNext){
      res = extracter.format(cur.next)
    }
    return res 
    
  }
  
  def main( args: Array[String]){
    
    println("Echo testing starts")
    
    
    //add documents
    println("Echo Indexing starts")
    val dir : File = new File("C:\\Users\\kumarish\\Desktop\\book")
    dir.listFiles foreach ( file => {
            println("    Adding file " + file.getName())
    		echo ! EchoMessage.AddDocument( "C:\\Users\\kumarish\\Desktop\\book\\" + file.getName )
    	}
  	)
    
    println("Echo indexing finished")
    
    println("Start Clustering ?")
    var i = Console.readInt
    
    //do clustering
    echo ! EchoMessage.StartClustering
    
    //search query
    val query : String = Console.readLine
    val result = echo !! EchoMessage.Query(query)
 
    val res : Map[Float, UUID] = result.get.asInstanceOf[EchoMessage.QueryReply].result
    i = 0
    res.keySet foreach ( e => {
    		println(i + " : " + res.get(e).get + "  " + e)
    		i = i + 1
    	}
  	)
  
    //ask for recommendations for the books listed above
    
  }
  
}