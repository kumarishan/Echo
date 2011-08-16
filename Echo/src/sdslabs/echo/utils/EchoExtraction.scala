package sdslabs.echo.utils

import scala.collection.mutable._
import com.mongodb._
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import org.apache.tika.exception.TikaException
import org.apache.tika.metadata._
import org.apache.tika.metadata.Metadata._
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.parser.ParseContext
import org.apache.tika.parser.Parser
import org.apache.tika.sax.BodyContentHandler
import org.xml.sax.ContentHandler
import org.xml.sax.SAXException
import java.net.URL
import java.io.DataInputStream
import java.net.URLConnection
import org.w3c.dom._
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.ParserConfigurationException
import scala.collection.mutable._
import java.nio.charset.Charset
import org.json.simple.JSONValue
import org.json.simple.JSONObject
import org.json.simple.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.NullPointerException


class EchoExtraction {


  def getTitle( f : File ): String = {
	
		var title_field : String = DublinCore.TITLE
		
		var stream : InputStream = new FileInputStream(f)            // converting from file to InputStream
		var parser : Parser = new AutoDetectParser()                // to auto detect parser
		var handler : ContentHandler = new BodyContentHandler(-1)      // in argument, maximam limit has to be specified
		var context : ParseContext = new ParseContext()               
		
		context.set(classOf[Parser], parser)
		
		var metadata : Metadata = new Metadata()
     
		try {
			parser.parse(stream, handler, metadata, context)
		} 
		finally {
			stream.close()
		}

		var title : String = new String("")
		metadata.names foreach ( name => {
			if(name.compareTo(title_field) == 0 )    {
				title = metadata.get(name)
			}  
		})
		
		return title.toLowerCase()
  }
  
  //---------end of the function getTitle--------------------
  def searchGoogle(str : String): Map[String,String] = {
	
	var query : String = str.replaceAll(" ", "+").replaceAll(".pdf", "")
			
		try {
			var url : String = "https://www.googleapis.com/books/v1/volumes?q=" + query
			var is : InputStream = new URL(url).openStream()
			var rd : BufferedReader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")))
			var sb : StringBuilder = new StringBuilder()
			var cp : Int = 0
			cp = rd.read()
			while (cp != -1) {
			      sb.append(cp.asInstanceOf[Char])
			      cp = rd.read()
			}
			
			var json : JSONObject = JSONValue.parse(sb.toString()).asInstanceOf[JSONObject]
			var item : String = json.get("items").toString
			println(item)
			var json2 : JSONArray = JSONValue.parse(item).asInstanceOf[JSONArray]
			var book = json2.get(0).toString
			var json3 : JSONObject = JSONValue.parse(book).asInstanceOf[JSONObject]
			var id: String = json3.get("id").toString
			
			var tempMap : Map[String, String] = new HashMap[String, String]()
			var tempURL : String = "http://www.google.com/books/feeds/volumes/" +id
			var newTempMap : Map[String, String] = extractMetadata(tempURL)
			println(newTempMap.toString)
			
			return newTempMap
		}catch {
		  case e : NullPointerException => {
		    return null
		  }
		}
		
	}
	
 
  //ParserConfigurationException, IOException
  def extractMetadata( str :  String ): Map[String,String] = {
	 {

		var list : Map[String,String] = null

		try {
			var docBuilderFactory : DocumentBuilderFactory = DocumentBuilderFactory.newInstance()
			var docBuilder: DocumentBuilder = docBuilderFactory.newDocumentBuilder()
			var url : URL = new URL(str)
			var urlConn : URLConnection = url.openConnection()
			urlConn.setDoInput(true)
			urlConn.setUseCaches(true)

			var dis : DataInputStream = new DataInputStream(urlConn.getInputStream())
			var doc : Document = docBuilder.parse (dis)
			var fields_arr : Array[String] = Array("dc:title","dc:creator","dc:date","dc:description","dc:format","dc:identifier","dc:language","dc:publisher","dc:subject")
			list = new HashMap[String,String]()

			var i : Int = 0
			while( i < 9 ) {
				var ls : String = new String("")
				var listOfNodes : NodeList = doc.getElementsByTagName(fields_arr(i))
				var totalNodes : Int = listOfNodes.getLength()
				var firstNodeElement : Element = null
				var j : Int = 0
				while(j < totalNodes ) {
					firstNodeElement = listOfNodes.item(j).asInstanceOf[Element]
					ls = ls + "  " + firstNodeElement.getTextContent()
					j = j + 1
				}
				list.put(fields_arr(i).substring(3), ls.trim())
				i = i + 1
			}
		}catch  {
		  case e: SAXException => {  
			  var x : Exception = e.getException ()
			
		  }
		}

		return list

	}
		
}

  def getInfo( name: String): Map[String, String] = {
  
    val gRes : Map[String,String] = searchGoogle(name)
    if( gRes == null){
      val ret = new HashMap[String,String]()
      ret += ("title" -> name)
      return ret
    }
    
    gRes += ("name" -> name)
    return gRes
  }
  
  def format( inp : DBObject): Map[String, String] = {
    
    val res : Map[String, String] = new HashMap[String, String]
    
    val set = inp.keySet.asInstanceOf[Set[String]]
    set foreach ( e => {
  			res.put(e, inp.get(e).asInstanceOf[String])
  		}
  	)
    
    if( inp.containsKey("cluster"))
      res += ("cluster" -> inp.get("cluster").asInstanceOf[String])
    return res
  }
  
  def toDBObject( inp: Map[String,String]) : DBObject = {
    
    val obj : DBObject = new BasicDBObject()
  	inp.keySet foreach ( e => {
  			obj.put(e, inp.get(e).get)
  		}
  	)
    return obj
  }
  
  
}