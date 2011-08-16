package sdslabs.echo.Indexing

import sdslabs.echo.utils.EchoDocument
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.store.FSDirectory
import org.apache.lucene.util.Version
import java.io.File
import org.apache.lucene.analysis.WhitespaceAnalyzer

class EchoIndexing(indexDirectory: String) {

  private val indexDir : FSDirectory = FSDirectory.open(new File(indexDirectory))
  private val writer: IndexWriter = new IndexWriter(indexDir, new StandardAnalyzer(Version.LUCENE_33), true, IndexWriter.MaxFieldLength.UNLIMITED)
  
  def indexDocument( doc: EchoDocument){
	//val writer: IndexWriter = new IndexWriter(indexDir, new WhitespaceAnalyzer(Version.LUCENE_33), true, IndexWriter.MaxFieldLength.UNLIMITED)
    writer.addDocument(doc.getDocument)
    writer.commit()
  }
  
}