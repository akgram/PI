import java.io.IOException;
import java.nio.file.Paths;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BoostQuery;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class Searcher extends BaseConfig implements AutoCloseable {
	private Directory directory;
	private IndexReader indexReader;
	private IndexSearcher indexSearcher;


	public Searcher(String indexDirPath) throws IOException {
		this.directory = FSDirectory.open(Paths.get(indexDirPath));
		this.indexReader = DirectoryReader.open(directory);
		this.indexSearcher = new IndexSearcher(indexReader);

		this.indexSearcher.setSimilarity(similarity);	
	}

	@Override
	public void close() throws Exception {
		this.indexReader.close();
		this.directory.close();
	}
	

	private void searchIndex(Query query) {
		System.out.println();
		System.out.println("Pretraživanje po objektu klase query: '" + query 
				+ "' tipa " + query.getClass().getName());
		this.findAndShowResults(query);
	}

	// 3.
	private void findAndShowResults(Query query) {
	    try {
	        TopDocs hits = indexSearcher.search(query, 10);
	        System.out.println("Broj pogodaka: " + hits.totalHits.value);

	        StoredFields storedFields = indexReader.storedFields();
	        for (int i = 0; i < hits.scoreDocs.length; i++) {
	            ScoreDoc scoreDoc = hits.scoreDocs[i];
	            Document document = storedFields.document(scoreDoc.doc);
	            String path = document.get(POLJE_PUTANJA);

	            System.out.println("=======================================");
	            System.out.println("Pronađeni fajl broj " + (i + 1) + ": " + path);
	            System.out.println("Score: " + scoreDoc.score);


	            Explanation explanation = indexSearcher.explain(query, scoreDoc.doc);
	            System.out.println("Objašnjenje score vrednosti:");
	            System.out.println(explanation.toString());
	        }
	    } catch (IOException ioException) {
	        ioException.printStackTrace();
	    }
	}
	

	public void BooleanQueries1() {

		TermQuery termAmericanNaslov = new TermQuery(new Term(POLJE_NASLOV, "linux"));
		TermQuery termRepublicNaslov = new TermQuery(new Term(POLJE_NASLOV, "stock"));

		
		TermQuery termAmericanSadrzaj = new TermQuery(new Term(POLJE_SADRZAJ, "linux"));
		TermQuery termRepublicSadrzaj = new TermQuery(new Term(POLJE_SADRZAJ, "stock"));
		
		
		BooleanQuery.Builder sadrzajBuilder = new BooleanQuery.Builder();
		sadrzajBuilder.add(termAmericanSadrzaj, Occur.SHOULD);
		sadrzajBuilder.add(termRepublicSadrzaj, Occur.SHOULD);
		BooleanQuery sadrzajQuery = sadrzajBuilder.build();
		
		// Dodavanje boost-a
		Query boostedSadrzajQuery = new BoostQuery(sadrzajQuery, 25.1131988f);
		System.out.println("----- Rezultati za polje SADRZAJ -----");
		searchIndex(boostedSadrzajQuery);
		//searchIndex(sadrzajQuery);
		
		BooleanQuery.Builder naslovBuilder = new BooleanQuery.Builder();
		naslovBuilder.add(termAmericanNaslov, Occur.SHOULD);
		naslovBuilder.add(termRepublicNaslov, Occur.SHOULD);
		BooleanQuery naslovQuery = naslovBuilder.build();
		
		// Dodavanje boost-a
		Query boostedNaslovQuery = new BoostQuery(naslovQuery, 1.374f);
		System.out.println("----- Rezultati za polje NASLOV -----");
		//searchIndex(boostedNaslovQuery);
		searchIndex(naslovQuery);
		
	}
	
	public void BooleanQueries2() {
		
		String queryStr = "Debian GNULinux  Guide to Installation and Usage";
		QueryParser sadrzajParser = new QueryParser(POLJE_SADRZAJ, analyzer);
		QueryParser naslovParser = new QueryParser(POLJE_NASLOV, analyzer);
		try {
		    Query sadrzajQuery1 = sadrzajParser.parse(queryStr);
		    Query naslovQuery1 = naslovParser.parse(queryStr);
		    
	        float boostFactorN = 1.374f;
	        Query naslovQuery2 = new BoostQuery(naslovQuery1, boostFactorN);
	        
	        
	        float boostFactoS = 25.1131988f;
	        Query sadrzajQuery2 = new BoostQuery(sadrzajQuery1, boostFactoS);
		    
		    
		    System.out.println("----- Rezultati za polje SADRZAJ -----");
		    searchIndex(sadrzajQuery1);
		    
		    System.out.println("----- Rezultati za polje NASLOV -----");
		    searchIndex(naslovQuery1);
		    
		    
		} catch (ParseException e) {
		    e.printStackTrace();
		}

	
	}

}
