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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
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


	private QueryParser classicParser;



	public Searcher(String indexDirPath) throws IOException {
		this.directory = FSDirectory.open(Paths.get(indexDirPath));
		this.indexReader = DirectoryReader.open(directory); 
		this.indexSearcher = new IndexSearcher(indexReader);

		this.classicParser = new QueryParser(POLJE_SADRZAJ, this.analyzer); 
																		
	}

	@Override
	public void close() throws Exception {

		this.indexReader.close();
		this.directory.close();
	}
	

	private void parseClassicAndSearch(String stringQuery) {
		System.out.println();
		System.out.println("\"Classic\" parser parsira upit: " + stringQuery);
		try {
			Query query = this.classicParser.parse(stringQuery);
			System.out.println("Pretraživanje po parsiranom objektu: '" + query 
					+ "' tipa " + query.getClass().getName());
			findAndShowResults(query);
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}


	private void searchIndex(Query query) {
		System.out.println();
		System.out.println("Pretraživanje po objektu klase query: '" + query 
				+ "' tipa " + query.getClass().getName());
		this.findAndShowResults(query);
	}

	private void findAndShowResults(Query query) {
		try {
			TopDocs hits = indexSearcher.search(query, 10);
			System.out.println("Broj pogodaka: " + hits.totalHits.value);

			StoredFields storedFields = indexReader.storedFields();
			for (ScoreDoc scoreDoc : hits.scoreDocs) {
				Document document = storedFields.document(scoreDoc.doc);
				String path = document.get(POLJE_PUTANJA);
				System.out.println("Pronađeni fajl je: " + path);
			}
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}


	public void BooleanQueries1() {

		TermQuery termRepublic = new TermQuery(new Term(POLJE_SADRZAJ, "jpeg"));
		TermQuery termaAlgoritmi = new TermQuery(new Term(POLJE_SADRZAJ, "project"));
		TermQuery termAmerican = new TermQuery(new Term(POLJE_SADRZAJ, "privatnost"));

		BooleanQuery.Builder builder = new BooleanQuery.Builder();


		builder.add(termRepublic,Occur.MUST_NOT);
		builder.add(termaAlgoritmi,Occur.MUST);
		builder.add(termAmerican,Occur.SHOULD);

		BooleanQuery query = builder.build();

		searchIndex(query);

	
	}
	public void BooleanQueries2() {
		
		String queryStr = "+sadrzaj:jpeg sadrzaj:project -sadrzaj:privatnost";
		//String queryStr = "jpeg AND project NOT privatnost"; default sadrzaj

		QueryParser parser = new QueryParser(POLJE_SADRZAJ, analyzer);

		try {
		    Query parsedQuery = parser.parse(queryStr);
		    searchIndex(parsedQuery);
		} catch (ParseException e) {
		    e.printStackTrace();
		}
	
	}
	public void PrefixQueries() throws Exception {
	    // prvi nacin – direktno kreiranje objektno
	    PrefixQuery prefixQuery1 = new PrefixQuery(new Term(POLJE_SADRZAJ, "jpeg"));
	    searchIndex(prefixQuery1);

	    // drugi nacin – parsiranje tekstualnog upita
	    // QueryParser automatski tretira "jpeg*" kao PrefixQuery
	    parseClassicAndSearch(POLJE_SADRZAJ + ":jpeg*");
	}

	
	

	
}
