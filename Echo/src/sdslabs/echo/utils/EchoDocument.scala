package sdslabs.echo.utils

import java.io.File
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import java.util.UUID
import java.io.FileReader
import java.io.FileInputStream
import org.apache.pdfbox.pdfparser.PDFParser
import org.apache.pdfbox.cos.COSDocument
import org.apache.pdfbox.util.PDFTextStripper
import org.apache.pdfbox.pdmodel.PDDocument
 


/**
 * COMPLETED
 */
class EchoDocument(myfile : File, id: UUID) {

  val fi: FileInputStream  = new FileInputStream(myfile)  
  val parser: PDFParser  = new PDFParser(fi)  
  parser.parse();  
  val cd: COSDocument = parser.getDocument();  
  val stripper: PDFTextStripper = new PDFTextStripper();  
  val text: String  = stripper.getText(new PDDocument(cd));  
  cd.close
  
  private val doc: Document = new Document()
  println("     " + id.toString + "    length     " + text.length)
  doc.add(new Field("contents", text, Field.Store.NO, Field.Index.ANALYZED))
  doc.add(new Field("id", id.toString, Field.Store.YES, Field.Index.NOT_ANALYZED))
 
  def file = myfile
  def getDocument: Document = doc
  
    	
}