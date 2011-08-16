package sdslabs.echo.dataprocessing

import com.aliasi.classify.PrecisionRecallEvaluation
import com.aliasi.cluster.HierarchicalClusterer
import com.aliasi.cluster.ClusterScore
import com.aliasi.cluster.CompleteLinkClusterer
import com.aliasi.cluster.SingleLinkClusterer
import com.aliasi.cluster.Dendrogram
import com.aliasi.util.Counter
import com.aliasi.util.Distance
import com.aliasi.util.Files
import com.aliasi.util.ObjectToCounterMap
import com.aliasi.util.Strings
import com.aliasi.tokenizer._

import sdslabs.echo.utils.EchoClusteringDocument

import com.mongodb._

import akka.actor.Actor
import sdslabs.echo.utils._
import java.util.HashSet
import java.util.Set
import java.util.UUID
import java.io._
import scala.collection.mutable.Map
import scala.collection.mutable.HashMap


/*
 * The worst clustering I can do for Echo.. 
 * But this should satisfy the current need
 * more like a do it all again... will surely not scale to large number of books
 * So run it at night only... hehehehehe
 */
class EchoClustering(dataStore: DB ) extends Actor{
  
  def getUUID(name : String):UUID = {
    val coll : DBCollection = dataStore.getCollection("echo")
    val query : BasicDBObject = new BasicDBObject()
	query.put("name", name)
	val cur = coll.find(query)
	var id: String = ""
	while(cur.hasNext){
	  id = cur.next().get("uuid").asInstanceOf[String]
	}
	println("      "+id)
    return UUID.fromString(id)
  }
  
  def performClustering( docDir: String): Set[Set[EchoClusteringDocument]] = {
    
    val dir : File = new File(docDir)
	var refPar : Set[Set[EchoClusteringDocument]] = new HashSet[Set[EchoClusteringDocument]]
	var docSet: Set[EchoClusteringDocument] = new HashSet[EchoClusteringDocument]
    
    for( file <- dir.listFiles()){
      val uuid = getUUID(file.getName)
      try {
    	  docSet.add( new EchoClusteringDocument(file, uuid))
      }catch {
        case e => {
          println("Exception thrown skip this document")
        }
      }
    }
    
    val cosine_dist : Distance[EchoClusteringDocument] = new Distance[EchoClusteringDocument](){
      def distance(doc1: EchoClusteringDocument, doc2: EchoClusteringDocument): Double = {
        val ret: Double = 1.0 - doc1.cosine(doc2)
        if( ret > 1.0){
          return 1.0
        }else if ( ret < 0.0) {
          return 0.0
        }else {
          return ret
        }
      }
    }
    println("Heirarchical Clustering")
    val clClusterer: HierarchicalClusterer[EchoClusteringDocument] = 
      new CompleteLinkClusterer[EchoClusteringDocument](cosine_dist)
    val dendrogram: Dendrogram[EchoClusteringDocument] =  
      clClusterer.hierarchicalCluster(docSet)
    return dendrogram.partitionK(3)
    
  }
  
  def storeClusters(cluster: Set[Set[EchoClusteringDocument]]) = {
    
      println("Storing the cluster")
	  val collection : DBCollection = dataStore.getCollection("echo")
	  
	  for( set <- cluster.toArray[Set[EchoClusteringDocument]]( new Array[Set[EchoClusteringDocument]](0))){
	    val clusterId: UUID = UUID.randomUUID
	    for( doc <- set.toArray[EchoClusteringDocument](new Array[EchoClusteringDocument](0))){
	      
	      val id : UUID = doc.id
	      val set: BasicDBObject = new BasicDBObject("$set", new BasicDBObject("cluster", clusterId.toString))
	      val query : BasicDBObject = new BasicDBObject()
	      query.put("uuid", id.toString)
	      collection.update(query, set);
	    }
	  }
   
  }
   
  def receive = {
    case EchoMessage.EchoReClusterAll(docDir) => {
      println("Starting Clustering :o")
      storeClusters(performClustering(docDir))
      println("Clustering done! :)")
    }
  }
  
}