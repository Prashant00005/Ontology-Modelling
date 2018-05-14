package kdeProjectQuestions;

import java.io.IOException;
import java.util.Locale;

import javax.swing.JLabel;

import kdeProjectLibs.Constants;
import kdeProjectLibs.OntologyLoading;
import kdeProjectLibs.OntologyQuerying;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.sparql.resultset.ResultSetMem;

import com.vividsolutions.jts.io.ParseException;


/** Main class for execution of program to answer the questions.
 * Run the main from OntologyLoading first to create the cached model file. */

public class AllQuestions {
	/** Main function that loads the cached model and answers questions. 
	 */
	public static void main(String[] args) throws IOException, ParseException  {
		
		
		// Revert locale to a region independent one.
		Locale.setDefault(Locale.ROOT);
		
		// load models using save file
		OntologyLoading.loadAllDatasets(true);
		
		Model unionModel = OntologyLoading.getUnionModel();//save it as a global variable to pass to all functions 
		// answer question 1 & 2


		q1(unionModel);
		q2(unionModel);
		q3(unionModel);
		q4(unionModel);
		q5(unionModel);
		q6(unionModel);
		q7(unionModel);
		q8(unionModel);
		q9(unionModel);
		q10(unionModel);
		
	}
	
	
	//QUESTION 1//
	public static void q1(Model unionModel) {
		
		String queryQ1 = "" +
				"prefix OO: <" + Constants.KDEontology + ">" +  // OO stands for Our Ontology
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
				"prefix skos: <http://www.w3.org/2004/02/skos/core#>" +
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
				"prefix qb: <http://purl.org/linked-data/cube#>" +
				"prefix sdmxdim: <http://purl.org/linked-data/sdmx/2009/dimension#>" +
				"prefix csoProp: <http://data.cso.ie/census-2011/property/>" +
				"" +
			
				// SELECT to get the density for the countries
				"SELECT ?ourCountyLabel ?areaValue (sum(?pop) AS ?popTotal) (?popTotal/?areaValue as ?density)" +
				" WHERE {" +
				// get the age groups 20-24 and 25-29 as ageGroup
				"   ?conceptScheme a skos:ConceptScheme . " +
				"   ?conceptScheme rdfs:label ?conceptSchemeLabel ." +
				"   FILTER (?conceptSchemeLabel = \"age group concept scheme\"@en)" +
				"   ?ageGroup skos:inScheme ?conceptScheme ." +
				"   ?ageGroup rdfs:label ?ageGroupLabel ." +
				"   FILTER (?ageGroupLabel= \"20-24\" || ?ageGroupLabel = \"25-29\")" +
				//"   FILTER (?ageGroupLabel != \"all\")" + // to test for all the population, uncomment

				//get all the observation for all the counties, for the selected ageGroup 
				"  ?obs a qb:Observation ." +
				"  ?obs sdmxdim:refArea ?csoCounty ." +
				"  ?obs csoProp:population ?pop ." +
				"  ?obs csoProp:age-group ?ageGroup ." +
				"  ?obs csoProp:gender <http://data.cso.ie/census-2011/classification/gender/both> ." +
				// TODO add the observation type, population by county with a variable!
				// TODO add the gender from the gender file
				"  ?obs qb:dataSet <http://data.cso.ie/census-2011/dataset/age-group-gender-population/cty> ." +
				
				// get all of our counties and their areas as ourCountyArea + ourCountyLabel
				"{" +
				"SELECT ?csoCounty ?ourCountyLabel ?areaValue" + 
				" WHERE {" +
				"?ourCounty a OO:" + Constants.ourCounty + " ;" +
				"           OO:hasArea ?ourCountyArea ;" +
				"           rdfs:label ?ourCountyLabel ;" +
				"			OO:" + Constants.linkedCsoRegionStr + " ?csoCounty ." +
				"?ourCountyArea OO:valueDouble ?areaValue ." +
						
				"  } " +
				"}" +

				"}" +
				" GROUP BY ?ourCountyLabel ?areaValue ?density" +
				" ORDER BY ASC (?density)" +
				" LIMIT 1" +
				"";
		
		System.out.println("Q1: What is the county with the lowest density of adults from 20 to 29 (both included) in the 2011 census?" +
				"\nANSWER:");

		long startTime = System.nanoTime();
		ResultSetMem results = OntologyQuerying.getQuerryResults(unionModel, queryQ1);
		OntologyQuerying.displayQuery(results);
		
		while (results.hasNext()){
			QuerySolution binding = results.nextSolution();
			String county = (String) binding.get("ourCountyLabel").asLiteral().getValue();
			String area = String.format("%.3f", binding.get("areaValue").asLiteral().getDouble());
			String popTotal = String.format("%d", binding.get("popTotal").asLiteral().getInt());
			String density = String.format("%.3f", binding.get("density").asLiteral().getDouble());
			
			
			System.out.println(county + " has the lowest density of adults from 20 to 29 with " + 
							   density + " young adults per km².\n" +
							   	"Indeed, " + county + " has " + popTotal + " adults aged from 20 to 29 and an area of " + area + "km².\n");
			
			break;
		}
		long endTime = System.nanoTime();
		System.out.println("QueryQ1 done in " + (endTime - startTime) / 1000000000. + "s.\n");
	}

	/** Question 2 */
	public static void q2(Model unionModel) {
		String csoDataset = "<http://data.cso.ie/census-2011/dataset/age-group-gender-population/ed>"; 
	
		String queryQ2 = "" + 
				"prefix OO: <" + Constants.KDEontology + ">" +  // OO stands for Our Ontology
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
				"prefix skos: <http://www.w3.org/2004/02/skos/core#>" +
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
				"prefix qb: <http://purl.org/linked-data/cube#>" +
				"prefix sdmxdim: <http://purl.org/linked-data/sdmx/2009/dimension#>" +
				"prefix csoProp: <http://data.cso.ie/census-2011/property/>" +
				"" +
			
				"SELECT (?labelA as ?ourRegionLabel) (?popA as ?totalPop) ?neighbourLabel (SUM(?pop) as ?totalPopN) " +
				"(?popA - ?totalPopN AS ?diff)" +
				" WHERE {" +
 
				"  ?regionA rdfs:label ?labelA ;" +
				"		    OO:hasNeighbour ?neighbour ." +
				"  ?neighbour rdfs:label ?neighbourLabel ;" +
				"			  OO:" + Constants.linkedCsoRegionStr + "?neiCso ." +
			
				//age group
				"      ?conceptScheme a skos:ConceptScheme ; " +
		        "                     rdfs:label \"age group concept scheme\"@en ." +
				"      ?ageGroup skos:inScheme ?conceptScheme ; " +
		    //  "				 rdfs:label \"all\" . "+				// applying a direct Literal match here makes the query very long... same for replacing ?ageGroup with the uri...
				"                rdfs:label ?ageGroupLabel ." +
				"      FILTER (?ageGroupLabel = \"all\")" +
				
				
				//get obs, joined by csoRegion and ageGroup
				"      ?obs a qb:Observation ;" +
				"  	        sdmxdim:refArea ?neiCso ;" +
				"           csoProp:population ?pop ;" +
				"  	        csoProp:age-group ?ageGroup ;" +
				"           csoProp:gender <http://data.cso.ie/census-2011/classification/gender/both> ;" +
				"           qb:dataSet " + csoDataset + " ." +
				"  { " +
				"    SELECT  ?regionA (sum(?pop) as ?popA) WHERE {" +
				//age group
				"      ?conceptScheme a skos:ConceptScheme ; " +
		        "                     rdfs:label \"age group concept scheme\"@en ." +
				"      ?ageGroup skos:inScheme ?conceptScheme ; " +
		    //  "				 rdfs:label \"all\" . "+				
				"                rdfs:label ?ageGroupLabel ." +
				"      FILTER (?ageGroupLabel = \"all\")" +
				
				"  	   ?regionA a OO:" + Constants.ourElectoralDivision + " ;" +
				"		          OO:" + Constants.linkedCsoRegionStr + " ?csoRegion ." +
				
				//get obs, joined by csoRegion and ageGroup
				"      ?obs a qb:Observation ;" +
				"  	        sdmxdim:refArea ?csoRegion ;" +
				"           csoProp:population ?pop ;" +
				"  	        csoProp:age-group ?ageGroup ;" +
				"           csoProp:gender <http://data.cso.ie/census-2011/classification/gender/both> ;" +
				"           qb:dataSet " + csoDataset + " ." +
				
				"    }GROUP BY ?regionA" +
				"   }" +

				"}" +
				"GROUP BY ?labelA ?popA ?neighbourLabel " +
				" ORDER BY DESC (?diff)" +
				" LIMIT 1" +
				"";
		
		long startTime = System.nanoTime();

		
		System.out.println("Q2: What pair of adjacent Electoral Division have the highest absolute difference of total population in the 2011 census?" +
				"\nANSWER:");
	    ResultSetMem results = OntologyQuerying.getQuerryResults(unionModel, queryQ2);
		OntologyQuerying.displayQuery(results);

		while (results.hasNext()){
			QuerySolution binding = results.nextSolution();
			String region = (String) binding.get("ourRegionLabel").asLiteral().getValue();
			String neighbour = (String) binding.get("neighbourLabel").asLiteral().getValue();

			String totalPop = String.format("%d", binding.get("totalPop").asLiteral().getInt());
			String totalNeighbourPop = String.format("%d", binding.get("totalPopN").asLiteral().getInt());
			String popDiff = String.format("%d", binding.get("diff").asLiteral().getInt());

			
			System.out.println(region + " and " + neighbour + " have the highest absolute difference of total population of " + popDiff + ".\n" +
							   	"Indeed, " + region + " and " + neighbour + " have respective population of " + totalPop + " and " + totalNeighbourPop + ".");
			
			break;
		}
		long endTime = System.nanoTime();
		System.out.println("QueryQ2 done in " + (endTime - startTime) / 1000000000. + "s.");
	}
	
	
	/** Question 3 */
	public static void q3(Model unionModel) {
		
		String queryQ3 = ""
				+ "PREFIX OO: <" + Constants.KDEontology + "> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX xmls: <http://www.w3.org/2001/XMLSchema#> " 
				+ "SELECT ?countyLabel (SUM(xmls:integer(?pop)) AS ?immigrantPopulation) "
				+ "			(xmls:double(?area) AS ?countyArea) (?immigrantPopulation / ?countyArea AS ?immingrantDensity) "
				+ "WHERE { "
				+ "		?id OO:hasCounty ?county . "
				+ "		?county rdfs:label ?countyLabel . "
				+ "		?id OO:hasNationality ?nationality . "
				+ "		?nationality rdfs:label ?nationalityLabel . "
				+ "		?id OO:hasPopulation ?pop . "
				+ "		?county OO:hasArea ?a . "
				+ "		?a OO:valueDouble ?area"
				+ "		FILTER (?nationalityLabel != \"Irish\" && ?nationalityLabel != \"all\" && ?nationalityLabel != \"not stated\") "
				+ "} "
				+ "GROUP BY ?countyLabel ?area "
				+ "ORDER BY DESC(?immingrantDensity) "
				+ "LIMIT 1";
		
		System.out.println("\nQ3: WWhich county has the highest immigration density in the 2011 census?" +
				"\nANSWER:");

		long startTime = System.nanoTime();
		ResultSetMem results = OntologyQuerying.getQuerryResults(unionModel, queryQ3);
		OntologyQuerying.displayQuery(results);
		
		while (results.hasNext()){
			QuerySolution binding = results.nextSolution();
			String county = (String) binding.get("countyLabel").asLiteral().getValue();
			String area = String.format("%.3f", binding.get("countyArea").asLiteral().getDouble());
			String popTotal = String.format("%d", binding.get("immigrantPopulation").asLiteral().getInt());
			String density = String.format("%.3f", binding.get("immingrantDensity").asLiteral().getDouble());
			
			
			System.out.println(county + " has the highest density of immigration (foreign population) with " + 
							   density + " people per km².\n" +
							   	"Indeed, " + county + " has " + popTotal + " people having non-Irish nationality and an area of " + area + "km².\n");
			
			break;
		}
		long endTime = System.nanoTime();
		System.out.println("QueryQ3 done in " + (endTime - startTime) / 1000000000. + "s.\n");
	}
	
	
	/** Question 4 */
	public static void q4(Model unionModel) {
		
		
		String queryQ4 = "" +
				"prefix OO: <" + Constants.KDEontology + ">" +  // OO stands for Our Ontology
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
				"prefix skos: <http://www.w3.org/2004/02/skos/core#>" +
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
				"prefix qb: <http://purl.org/linked-data/cube#>" +
				"prefix sdmxdim: <http://purl.org/linked-data/sdmx/2009/dimension#>" +
				"prefix csoProp: <http://data.cso.ie/census-2011/property/>" +
				"" +
			
				// SELECT to get the density for the countries
				"SELECT ?ourCountyLabel ?areaValue (sum(?pop) AS ?popTotal) (?popTotal/?areaValue as ?density)" +
				" WHERE {" +
				// get the age groups 20-24 and 25-29 as ageGroup
				"   ?conceptScheme a skos:ConceptScheme ; " +
				"                  rdfs:label ?conceptSchemeLabel ." +
				"   FILTER (?conceptSchemeLabel = \"means of travel to work, school or college concept scheme\"@en)" +
				"   ?meanOfTransport skos:inScheme ?conceptScheme ;" +
				"                    rdfs:label ?meanLabel ." +
				"   FILTER (?meanLabel= \"Motorcycle or scooter\")" +

				//get all the observation for all the counties, for the selected ageGroup 
				"  ?obs a qb:Observation ;" +
				"       sdmxdim:refArea ?csoCounty ;" +
				"       csoProp:population-aged-5-years-and-over ?pop ;" +
				"       csoProp:means-of-travel-to-work-school-college ?ageGroup ;" +
				"       qb:dataSet <http://data.cso.ie/census-2011/dataset/population-commuting-means/cty> ." +
				
				// get all of our counties and their areas as ourCountyArea + ourCountyLabel
				"{" +
				"SELECT ?csoCounty ?ourCountyLabel ?areaValue" + 
				" WHERE {" +
				"?ourCounty a OO:" + Constants.ourCounty + " ;" +
				"           OO:hasArea ?ourCountyArea ;" +
				"           rdfs:label ?ourCountyLabel ;" +
				"			OO:" + Constants.linkedCsoRegionStr + " ?csoCounty ." +
				"?ourCountyArea OO:valueDouble ?areaValue ." +
						
				"  } " +
				"}" +

				"}" +
				" GROUP BY ?ourCountyLabel ?areaValue ?density" +
				" ORDER BY DESC (?density)" +
				" LIMIT 1" +
				"";
		
		System.out.println("Q4: Which county has the highest density population aged 5 years and over who travel to work, school or college by Motorcycle or Scooter in the 2011 census?" +
				"\nANSWER:");

		long startTime = System.nanoTime();
		ResultSetMem results = OntologyQuerying.getQuerryResults(unionModel, queryQ4);
		OntologyQuerying.displayQuery(results);
		
		while (results.hasNext()){
			QuerySolution binding = results.nextSolution();
			String county = (String) binding.get("ourCountyLabel").asLiteral().getValue();
			String area = String.format("%.3f", binding.get("areaValue").asLiteral().getDouble());
			String popTotal = String.format("%d", binding.get("popTotal").asLiteral().getInt());
			String density = String.format("%.3f", binding.get("density").asLiteral().getDouble());
			
			
			System.out.println(county + " has the highest density population of " + 
							   density + " \n" +
							   	"" + county + " has " + popTotal + " adults aged 5 years and over who travel to work, school or college by Motorcycle or Scooter and has an area of " + area + "km².\n");
			
			break;
		}
		long endTime = System.nanoTime();
		System.out.println("QueryQ4 done in " + (endTime - startTime) / 1000000000. + "s.\n");
	}

	
	/**Question 5*/
	public static void q5(Model unionModel) {
		
		//Query to find population travelling by motorcycle or scooter per county
		
		String queryQ5 = "" +
				"prefix OO: <" + Constants.KDEontology + ">" +  // OO stands for Our Ontology
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
				"prefix skos: <http://www.w3.org/2004/02/skos/core#>" +
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
				"prefix qb: <http://purl.org/linked-data/cube#>" +
				"prefix sdmxdim: <http://purl.org/linked-data/sdmx/2009/dimension#>" +
				"prefix csoProp: <http://data.cso.ie/census-2011/property/>" +
				"" +
			
				// SELECT to get the density for the countries
				"SELECT ?ourCountyLabel ?areaValue (sum(?pop) AS ?popTotal) (?popTotal/?areaValue as ?density)" +
				" WHERE {" +
				// get the age groups 20-24 and 25-29 as ageGroup
				"   ?conceptScheme a skos:ConceptScheme . " +
				"   ?conceptScheme rdfs:label ?conceptSchemeLabel ." +
				"   FILTER (?conceptSchemeLabel = \"field of study concept scheme\"@en)" +
				"   ?studyGroup skos:inScheme ?conceptScheme ." +
				"   ?studyGroup rdfs:label ?studyGroupLabel ." +
				"   FILTER (?studyGroupLabel= \"Science, Mathematics and Computing\")" +
				//"   FILTER (?ageGroupLabel != \"all\")" + // to test for all the population, uncomment

				//get all the observation for all the counties, for the selected ageGroup 
				"  ?obs a qb:Observation ." +
				"  ?obs sdmxdim:refArea ?csoCounty ." +
				"  ?obs csoProp:population-aged-15-years-and-over ?pop ." +
				"  ?obs csoProp:field-of-study ?studyGroup ." +
				//"  ?obs csoProp:gender <http://data.cso.ie/census-2011/classification/gender/both> ." +
				// TODO add the observation type, population by county with a variable!
				// TODO add the gender from the gender file
				"  ?obs qb:dataSet <http://data.cso.ie/census-2011/dataset/population-field-of-study/cty> ." +
				
				// get all of our counties and their areas as ourCountyArea + ourCountyLabel
				"{" +
				"SELECT ?csoCounty ?ourCountyLabel ?areaValue" + 
				" WHERE {" +
				"?ourCounty a OO:" + Constants.ourCounty + " ;" +
				"           OO:hasArea ?ourCountyArea ;" +
				"           rdfs:label ?ourCountyLabel ;" +
				"			OO:" + Constants.linkedCsoRegionStr + " ?csoCounty ." +
				"?ourCountyArea OO:valueDouble ?areaValue ." +
						
				"  } " +
				"}" +

				"}" +
				" GROUP BY ?ourCountyLabel ?areaValue ?density" +
				" ORDER BY ASC (?density)" +
				" LIMIT 1" +
				"";
		
				// SELECT to get the density for the countries
				

		System.out.println("Q5: What is the county with the lowest population density of 15 years or older studying Science, Mathematics and Computing in the 2011 census?" +
				"\nANSWER:");

		long startTime = System.nanoTime();
		ResultSetMem results = OntologyQuerying.getQuerryResults(unionModel, queryQ5);
		OntologyQuerying.displayQuery(results);
		
		while (results.hasNext()){
			QuerySolution binding = results.nextSolution();
			String county = (String) binding.get("ourCountyLabel").asLiteral().getValue();
			String area = String.format("%.3f", binding.get("areaValue").asLiteral().getDouble());
			String popTotal = String.format("%d", binding.get("popTotal").asLiteral().getInt());
			String density = String.format("%.3f", binding.get("density").asLiteral().getDouble());
			
			
			System.out.println(county + " has the lowest density of population of 15 years or older studying Science, Mathematics and Computer Science with  " + 
							   density + " young adults per km².\n");
			
			break;
		}
		long endTime = System.nanoTime();
		System.out.println("QueryQ5 done in " + (endTime - startTime) / 1000000000. + "s.\n");
	}
	
	
	
	/** Question 6*/
	public static void q6(Model unionModel) {
		
		
		String queryQ6 = "" +
				"prefix OO: <" + Constants.KDEontology + ">" +  // OO stands for Our Ontology
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
				"prefix skos: <http://www.w3.org/2004/02/skos/core#>" +
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
				"prefix qb: <http://purl.org/linked-data/cube#>" +
				"prefix sdmxdim: <http://purl.org/linked-data/sdmx/2009/dimension#>" +
				"prefix csoProp: <http://data.cso.ie/census-2011/property/>" +
				"" +
			
				// SELECT to get the density for the countries
				"SELECT ?ourCountyLabel ?areaValue (sum(?pop) AS ?popTotal) (?popTotal/?areaValue as ?density)" +
				" WHERE {" +
				// get the age groups 20-24 and 25-29 as ageGroup
				"   ?conceptScheme a skos:ConceptScheme . " +
				"   ?conceptScheme rdfs:label ?conceptSchemeLabel ." +
				"   FILTER (?conceptSchemeLabel = \"age group concept scheme\"@en)" +
				"   ?ageGroup skos:inScheme ?conceptScheme ." +
				"   ?ageGroup rdfs:label ?ageGroupLabel ." +
				"   FILTER (?ageGroupLabel= \"Age 25-44 years\")" +
				//"   FILTER (?ageGroupLabel != \"all\")" + // to test for all the population, uncomment

				//get all the observation for all the counties, for the selected ageGroup 
				"  ?obs a qb:Observation ." +
				"  ?obs sdmxdim:refArea ?csoCounty ." +
				"  ?obs csoProp:persons-with-disability ?pop ." +
				"  ?obs csoProp:age-group ?ageGroup ." +
//				"  ?obs csoProp:gender <http://data.cso.ie/census-2011/classification/gender/both> ." +
				// TODO add the observation type, population by county with a variable!
				// TODO add the gender from the gender file
				"  ?obs qb:dataSet <http://data.cso.ie/census-2011/dataset/persons-with-disability/cty> ." +
				
				// get all of our counties and their areas as ourCountyArea + ourCountyLabel
				"{" +
				"SELECT ?csoCounty ?ourCountyLabel ?areaValue" + 
				" WHERE {" +
				"?ourCounty a OO:" + Constants.ourCounty + " ;" +
				"           OO:hasArea ?ourCountyArea ;" +
				"           rdfs:label ?ourCountyLabel ;" +
				"			OO:" + Constants.linkedCsoRegionStr + " ?csoCounty ." +
				"?ourCountyArea OO:valueDouble ?areaValue ." +
						
				"  } " +
				"}" +

				"}" +
				" GROUP BY ?ourCountyLabel ?areaValue ?density" +
				" ORDER BY ASC (?density)" +
				" LIMIT 1" +
				"";
		
		System.out.println("Q6: What is the county with the highest density of adults with disabilities from 25 - 44(both included) in the 2011 census?" +
				"\nANSWER:");

		long startTime = System.nanoTime();
		ResultSetMem results = OntologyQuerying.getQuerryResults(unionModel, queryQ6);
		OntologyQuerying.displayQuery(results);
		
		while (results.hasNext()){
			QuerySolution binding = results.nextSolution();
			String county = (String) binding.get("ourCountyLabel").asLiteral().getValue();
			String area = String.format("%.3f", binding.get("areaValue").asLiteral().getDouble());
			String popTotal = String.format("%d", binding.get("popTotal").asLiteral().getInt());
			String density = String.format("%.3f", binding.get("density").asLiteral().getDouble());
			
			
			System.out.println(county + " has the lowest density of adults from 25 to 44 with " + 
							   density + " disability per km².\n" +
							   	"Indeed, " + county + " has " + popTotal + " adults aged from 25 to 44 with a disability and an area of " + area + "km².\n");
			
			break;
		}
		long endTime = System.nanoTime();
		System.out.println("QueryQ6 done in " + (endTime - startTime) / 1000000000. + "s.\n");
	}


	
	/** Question 7*/
	public static void q7(Model unionModel)  {
		
		
		String queryQ7 = ""+
				"prefix OO: <" + Constants.KDEontology + ">" +  // OO stands for Our Ontology
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
				"prefix skos: <http://www.w3.org/2004/02/skos/core#>" +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>"+
				"prefix qb: <http://purl.org/linked-data/cube#>" +
				"PREFIX sdmxdim: <http://purl.org/linked-data/sdmx/2009/dimension#> "+
				"PREFIX csoProp: <http://data.cso.ie/census-2011/property/> "+

				
				// SELECT to get the density for the countries
				"SELECT ?ourCountyLabel ?areaValue (sum(?pop) AS ?popTotal) (?popTotal/?areaValue as ?density)" +
				" WHERE {" +
				// get the people who can speak irish as  irishSpeaker 
				"   ?conceptScheme a skos:ConceptScheme . " +
				"   ?conceptScheme rdfs:label ?conceptSchemeLabel ." +
				"   FILTER (?conceptSchemeLabel = \"ability to speak Irish concept scheme\"@en)" +
				"   ?irishSpeaker skos:inScheme ?conceptScheme ." +
				"   ?irishSpeaker rdfs:label ?irishSpeakerLabel ." +
				"   FILTER (?irishSpeakerLabel= \"yes\")" +
				//get all the observation for all the counties, for the selected irishSpeaker 
				"  ?obs a qb:Observation ." +
				"  ?obs sdmxdim:refArea ?csoCounty ." +
				"  ?obs csoProp:population-aged-3-or-over ?pop ." +
	        	"  ?obs csoProp:ability-to-speak-irish ?irishSpeaker ." +
				"  ?obs qb:dataSet <http://data.cso.ie/census-2011/dataset/irish-speakers/cty> ." +

				
	        	// get all of our counties and their areas as ourCountyArea + ourCountyLabel
				"{" +
				"SELECT ?csoCounty ?ourCountyLabel ?areaValue" + 
				" WHERE {" +
				"?ourCounty a OO:" + Constants.ourCounty + " ;" +
				"           OO:hasArea ?ourCountyArea ;" +
				"           rdfs:label ?ourCountyLabel ;" +
		        "			OO:" + Constants.linkedCsoRegionStr + " ?csoCounty ." +
				"?ourCountyArea OO:valueDouble ?areaValue ." +
						
				"  } " +
				"}" +

				"}" +
				" GROUP BY ?ourCountyLabel ?areaValue ?density" +
				" ORDER BY DESC (?density)" +
				" LIMIT 1" +
				"";

		System.out.println("Q7:what is  the county with the highest density of people aged 3 or over can speak Irish in the 2011 census?" +
				"\nANSWER:");
				 
		
		long startTime = System.nanoTime();
		ResultSetMem results = OntologyQuerying.getQuerryResults(unionModel, queryQ7);
		OntologyQuerying.displayQuery(results);	
		
		while (results.hasNext()){
			QuerySolution binding = results.nextSolution();
			String county = (String) binding.get("ourCountyLabel").asLiteral().getValue();
			String area = String.format("%.3f", binding.get("areaValue").asLiteral().getDouble());
			String popTotal = String.format("%d", binding.get("popTotal").asLiteral().getInt());
			String density = String.format("%.3f", binding.get("density").asLiteral().getDouble());
			
			
			System.out.println("County " + county + " has the highest density of people aged 3 or over can speak Irish with " + 
							   density + " people per km².\n" +
							   	"Indeed, " + county + " has " + popTotal + " people aged 3 or over can speak Irish and an area of " + area + "km².\n");
			
			break;
			
		}
		
		long endTime = System.nanoTime();
		System.out.println("QueryQ7 done in " + (endTime - startTime) / 1000000000. + "s.\n");

	}
	
	
	/** Question 8 */
	public static void q8(Model unionModel) {
		
		String queryQ8 = "" +
				"prefix OO: <" + Constants.KDEontology + ">" +  // OO stands for Our Ontology
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
				"prefix skos: <http://www.w3.org/2004/02/skos/core#>" +
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
				"prefix qb: <http://purl.org/linked-data/cube#>" +
				"prefix sdmxdim: <http://purl.org/linked-data/sdmx/2009/dimension#>" +
				"prefix csoProp: <http://data.cso.ie/census-2011/property/>" +
				"" +
			
				// SELECT to get the density for the countries
				"SELECT ?townLabel ?neighbourLabel ?CorkCountyLabel (sum(?pop) AS ?popTotal) " +
				" WHERE {" +
				// get the age groups 20-24 and 25-29 as ageGroup
				"   ?conceptScheme a skos:ConceptScheme . " +
				"   ?conceptScheme rdfs:label ?conceptSchemeLabel ." +
				"   FILTER (?conceptSchemeLabel = \"age group concept scheme\"@en)" +
				"   ?ageGroup skos:inScheme ?conceptScheme ." +
				"   ?ageGroup rdfs:label ?ageGroupLabel ." +
				"   FILTER (xsd:integer(?ageGroupLabel) <= 10)" +

				// get all of our counties neighbouring Cork 
				"?ourCounty a OO:" + Constants.ourCounty + " ;" +
				"           OO:hasNeighbour ?neighbour ;" +
				"           rdfs:label ?CorkCountyLabel ." +
				"FILTER(CONTAINS(?CorkCountyLabel, \"Cork\")) " +

				"?neighbour rdfs:label ?neighbourLabel ." +

				//find towns in neighbour
				"{" +
				"?town a OO:" + Constants.ourTown + " ;" +
						"OO:" + Constants.inRegion + "?neighbour ; " + 
				"		 OO:" + Constants.linkedCsoRegionStr + " ?csoTown ;" +
						"rdfs:label ?townLabel." +
				"}" + 
				"UNION "+
				"{" +
				"?town a OO:" + Constants.ourCity + " ;" +
						"OO:" + Constants.inRegion + "?neighbour ; " + 
				"		 OO:" + Constants.linkedCsoRegionStr + " ?csoTown ;" +
						"rdfs:label ?townLabel." +
				"}" +
				
				//get all the observation for all the towns, for the selected ageGroup 
				"  ?obs a qb:Observation ;" +
				"       sdmxdim:refArea ?csoTown ;" +
				"       csoProp:population ?pop ;" +
				"       csoProp:age-group ?ageGroup ;" +
				"       csoProp:gender <http://data.cso.ie/census-2011/classification/gender/both> ;" +
				"       qb:dataSet <http://data.cso.ie/census-2011/dataset/age-group-gender-population/lt> ." +
				
				"}" +
				" GROUP BY ?townLabel ?neighbourLabel ?CorkCountyLabel " +
				" ORDER BY ASC (?popTotal)" +
				" LIMIT 1" +
				"";
		
		System.out.println("Q8: What are the legal towns or cities in a neighbouring county of Cork with \n" +
				"the lowest and highest number of children with age lower or equal to 10 in the 2011 census?" +
				"\nANSWER:");

		ResultSetMem resultsLow = OntologyQuerying.getQuerryResults(unionModel, queryQ8);
		ResultSetMem resultsHigh = OntologyQuerying.getQuerryResults(unionModel, queryQ8.replace("ASC", "DESC"));
		OntologyQuerying.displayQuery(resultsLow);
		OntologyQuerying.displayQuery(resultsHigh);
		
		while (resultsLow.hasNext()){
			QuerySolution binding = resultsLow.nextSolution();
			String town = (String) binding.getLiteral("townLabel").getValue();
			String county = (String) binding.getLiteral("neighbourLabel").getValue();
			String popTotal = String.format("%d", binding.getLiteral("popTotal").getInt());
			
			System.out.println("\"" + town + "\" from \"" + county + "\" has the lowest number of children with age lower than 10 in the 2011 census with " +
								popTotal + ".");
			break;
		}
		while (resultsHigh.hasNext()){
			QuerySolution binding = resultsHigh.nextSolution();
			String town = (String) binding.getLiteral("townLabel").getValue();
			String county = (String) binding.getLiteral("neighbourLabel").getValue();
			String popTotal = String.format("%d", binding.getLiteral("popTotal").getInt());
			
			System.out.println("\"" + town + "\" from \"" + county + "\" has the highest number of children with age lower than 10 in the 2011 census with " +
								popTotal + ".");
			break;
		}
	}
	
	
	/** Question 9 */
	public static void q9(Model unionModel) {
		
		String queryQ9 = ""
				+ "PREFIX OO: <" + Constants.KDEontology + "> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX xmls: <http://www.w3.org/2001/XMLSchema#> " 
				+ "SELECT ?countyLabel (SUM(xmls:integer(?pop)) AS ?population) "
				+ "			(xmls:double(?area) AS ?countyArea) (?countyArea / ?population AS ?landPerCapita) "
				+ "WHERE { "
				+ "		?id OO:hasCounty ?county . "
				+ "		?county rdfs:label ?countyLabel . "
				+ "		?id OO:hasNationality ?nationality . "
				+ "		?nationality rdfs:label ?nationalityLabel . "
				+ "		?id OO:hasPopulation ?pop . "
				+ "		?county OO:hasArea ?a . "
				+ "		?a OO:valueDouble ?area"
				+ "		FILTER (?nationalityLabel = \"all\") "
				+ "} "
				+ "GROUP BY ?countyLabel ?area "
				+ "ORDER BY DESC(?landPerCapita) "
				+ "LIMIT 1";
		
		System.out.println("\nQ9: WWhich county has the largest and smallest land per capita in the 2011 census?" +
				"\nANSWER:");

		long startTime = System.nanoTime();
		ResultSetMem resultsLargest = OntologyQuerying.getQuerryResults(unionModel, queryQ9);
		ResultSetMem resultsSmallest = OntologyQuerying.getQuerryResults(unionModel, queryQ9.replace("DESC", "ASC"));
		OntologyQuerying.displayQuery(resultsLargest);
		OntologyQuerying.displayQuery(resultsSmallest);
		
		while (resultsLargest.hasNext()){
			QuerySolution binding = resultsLargest.nextSolution();
			String county = (String) binding.get("countyLabel").asLiteral().getValue();
			String area = String.format("%.3f", binding.get("countyArea").asLiteral().getDouble());
			String popTotal = String.format("%d", binding.get("population").asLiteral().getInt());
			String landPerCapita = String.format("%.3f", binding.get("landPerCapita").asLiteral().getDouble());
			
			
			System.out.println(county + " has the largest land per capita with " + 
							   landPerCapita + " km² per people.\n" +
							   	"Indeed, " + county + " has " + popTotal + " people and an area of " + area + "km².\n");
			
			break;
		}
		while (resultsSmallest.hasNext()){
			QuerySolution binding = resultsSmallest.nextSolution();
			String county = (String) binding.get("countyLabel").asLiteral().getValue();
			String area = String.format("%.3f", binding.get("countyArea").asLiteral().getDouble());
			String popTotal = String.format("%d", binding.get("population").asLiteral().getInt());
			String landPerCapita = String.format("%.3f", binding.get("landPerCapita").asLiteral().getDouble());
			
			
			System.out.println(county + " has the smallest land per capita with " + 
							   landPerCapita + " km² per people.\n" +
							   	"Indeed, " + county + " has " + popTotal + " people and an area of " + area + "km².\n");
			
			break;
		}
		long endTime = System.nanoTime();
		System.out.println("QueryQ9 done in " + (endTime - startTime) / 1000000000. + "s.\n");
	}
	
public static void q10(Model unionModel) {
		
		String queryQ10 = "" +
				"prefix OO: <" + Constants.KDEontology + ">" +  // OO stands for Our Ontology
				"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" +
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
				"prefix skos: <http://www.w3.org/2004/02/skos/core#>" +
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
				"prefix qb: <http://purl.org/linked-data/cube#>" +
				"prefix sdmxdim: <http://purl.org/linked-data/sdmx/2009/dimension#>" +
				"prefix csoProp: <http://data.cso.ie/census-2011/property/>" +
				"" +
			
				// SELECT to get the density for the countries
				"SELECT ?townLabel ?neighbourLabel ?DublinCountyLabel (sum(?pop) AS ?popTotal) " +
				" WHERE {" +
				// get the age groups 20-24 and 25-29 as ageGroup
				"   ?conceptScheme a skos:ConceptScheme ; " +
				"                  rdfs:label ?conceptSchemeLabel ." +
				"   FILTER (?conceptSchemeLabel = \"field of study concept scheme\"@en)" +
				"   ?studyGroup skos:inScheme ?conceptScheme ;" +
				"               rdfs:label ?studyGroupLabel ." +
				"   FILTER (?studyGroupLabel= \"Health and Welfare\")" + //Edited the 3 lines above

				// get all of our counties neighbouring Cork 
				"?ourCounty a OO:" + Constants.ourCounty + " ;" +
				"           OO:hasNeighbour ?neighbour ;" +
				"           rdfs:label ?DublinCountyLabel ." +
				"FILTER(CONTAINS(?DublinCountyLabel, \"Dublin\")) " +

				"?neighbour rdfs:label ?neighbourLabel ." +

				//find towns in neighbour
				"{" +
				"?town a OO:" + Constants.ourTown + " ;" +
						"OO:" + Constants.inRegion + "?neighbour ; " + 
				"		 OO:" + Constants.linkedCsoRegionStr + " ?csoTown ;" +
						"rdfs:label ?townLabel." +
				"}" + 
				"UNION "+
				"{" +
				"?town a OO:" + Constants.ourCity + " ;" +
						"OO:" + Constants.inRegion + "?neighbour ; " + 
				"		 OO:" + Constants.linkedCsoRegionStr + " ?csoTown ;" +
						"rdfs:label ?townLabel." +
				"}" +
				
				//get all the observation for all the towns, for the selected ageGroup 
				"  ?obs a qb:Observation ;" +
				"       sdmxdim:refArea ?csoTown ;" +
				"       csoProp:population-aged-15-years-and-over ?pop ;" +
				"       csoProp:field-of-study ?studyGroup ;" + // Edited the above 2 lines
				"       qb:dataSet <http://data.cso.ie/census-2011/dataset/population-field-of-study/lt> ." +
				
				"}" +
				" GROUP BY ?townLabel ?neighbourLabel ?DublinCountyLabel " +
				" ORDER BY ASC (?popTotal)" +
				" LIMIT 1" +
				"";
		
		System.out.println("Q10: What are the legal towns or cities in a neighbouring county of Dublin with \n" +
				"the lowest number of people aged over 15 studying Health and Welfare in the 2011 census?" +
				"\nANSWER:");

		ResultSetMem resultsLow = OntologyQuerying.getQuerryResults(unionModel, queryQ10);
		//ResultSetMem resultsHigh = OntologyQuerying.getQuerryResults(unionModel, queryQ10.replace("ASC", "DESC"));
		OntologyQuerying.displayQuery(resultsLow);
		//OntologyQuerying.displayQuery(resultsHigh);
		
		while (resultsLow.hasNext()){
			QuerySolution binding = resultsLow.nextSolution();
			String town = (String) binding.getLiteral("townLabel").getValue();
			String county = (String) binding.getLiteral("neighbourLabel").getValue();
			String popTotal = String.format("%d", binding.getLiteral("popTotal").getInt());
			
			System.out.println("\"" + town + "\" from \"" + county + "\" has the lowest number of people aged over 15 studying Health and Welfare in the 2011 census with " +
								popTotal + ".");
			break;
		}
	}	
	
}
