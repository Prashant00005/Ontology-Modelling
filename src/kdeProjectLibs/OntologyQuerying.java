package kdeProjectLibs;


import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.resultset.ResultSetMem;


/** This class provide helper function to run queries on an ontology. */
public class OntologyQuerying {
	
	/** Output query results, special fix to display correctly UTF-8 encoded characters
    The fix create lines shorter that the other and breaks the nice formatting */
	public static void queryAndDisplay(Model model, String queryString)  {	
		// Execute the query and obtain results
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
		ResultSet results = qe.execSelect();
		
		// Output query results, special fix to display correctly UTF-8 encoded characters
		// The fix create lines shorter that the other and breaks the nice formatting
		String tmp;
		tmp = ResultSetFormatter.asText(results);
		try {
			tmp = new String(tmp.getBytes("iso-8859-1"), "utf-8");			
		}catch (Exception problem){
			problem.printStackTrace();
			System.out.println("Error decoding utf-8 query results.");
			System.exit(-1);
		}
		
		System.out.print(tmp);
		// Important - free up resources used when running the query
		qe.close();
	}
	
	/** Execute the query and obtain results as a ResultSetMem */
	public static ResultSetMem getQuerryResults(Model ourModel, String queryString) {	
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, ourModel);
		ResultSet results = qe.execSelect();
		ResultSetMem mem = new ResultSetMem(results);
		// Important - free up resources used when running the query
		qe.close();
		return mem;
	}
	
	/** display the results as a table and rewind the ResultSetMem for further parsing */
	public static void displayQuery(ResultSetMem results){
		
		String tmp;
		tmp = ResultSetFormatter.asText(results);
		try {
			tmp = new String(tmp.getBytes("iso-8859-1"), "utf-8");			
		}catch (Exception problem){
			problem.printStackTrace();
			System.out.println("Error decoding utf-8 query results.");
			System.exit(-1);
		}
		System.out.print(tmp);
		results.rewind();
		
	}
}
