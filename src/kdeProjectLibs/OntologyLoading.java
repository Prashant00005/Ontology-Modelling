package kdeProjectLibs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.text.WordUtils;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.resultset.ResultSetMem;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;

import kdeProjectLibs.Constants;
import kdeProjectLibs.OntologyQuerying;

/**
 * Regroup all the function in charge of loading ontologies and filling new
 * individuals.
 */
public class OntologyLoading {

	/**
	 * Load all the models, add them up and return a model as the sum of all the
	 * models.
	 */
	public static Model loadModelsFromFile(List<String> modelPaths) {

		Model model = ModelFactory.createDefaultModel();

		for (String path : modelPaths) {
			try {
				System.out.println("Loading ontology" + path);
				Model model2 = RDFDataMgr.loadModel(path);
				model.add(model2);
			} catch (Exception problem) {
				problem.printStackTrace();
				System.err.println("Error, unable to load file " + path);
				System.exit(-1);
			}
		}
		return model;
	}

	private static Boolean loaded = false;
	
	private static Model kdeModel = null;
	private static OntModel kdeOntModel = null;
	private static Model unionModel = null;
	
	private static List<Geometry> ourCountiesGeo = null;
	private static List<Individual> ourCountiesInd = null;

	/** return the union of all the models used (both external datasets and our ontology) */
	public static Model getUnionModel() {
		if(!loaded) {
			System.out.println("Error, model not loaded!");
			System.exit(-1);
		}
		return unionModel;
	}
	
	/** return the our kde ontology model as an OntModel (OWL)) */
	public static Model getkdeOntModel() {
		if(true) {
			System.out.println("Error, Ont Model not loaded!");
			System.exit(-1);
		}
		return kdeOntModel;
	}
	
	public static void loadAllDatasets() {
		loadAllDatasets(false);
	}
	
	/** Load both the external datasets and our empty ontology */
	public static void loadAllDatasets(Boolean fromSaved) {
		long startTime = System.nanoTime();

		List<String> model_uris = new ArrayList<String>();
		// external datasets required both for importing and answering questions
		model_uris.add(Constants.cso_legal_town_city_age_group_gender_uri);
		model_uris.add(Constants.cso_age_group_uri);

		if(!fromSaved){ // Construct model
			// Only models required for importing data into our ontology
			model_uris.add(Constants.cso_area_uri);
			model_uris.add(Constants.cso_nationality_uri);
			model_uris.add(Constants.geohive_county_100m);
			model_uris.add(Constants.geohive_electoral_division_100m);
			model_uris.add(Constants.geohive_settlements);
			model_uris.add(Constants.geohive_cities_and_legal_towns_100m);
			model_uris.add(Constants.cso_county_nationality_uri);
			model_uris.add(Constants.cso_county_population_study_uri);//a
			model_uris.add(Constants.cso_town_city_population_study_uri);//a
			model_uris.add(Constants.cso_fiel_of_study_uri);//a
			model_uris.add(Constants.cso_disability_age_group);
			model_uris.add(Constants.cso_disability_age_group_struc);


			System.out.println("Loading all external datasets...");
			Model dataModel = loadModelsFromFile(model_uris);
			
			// our Ontology
			model_uris.clear();
			model_uris.add(Constants.KDEontologyFile);
			System.out.println("Loading our kde Ontology...");
			kdeModel = loadModelsFromFile(model_uris);
	
			// create the Union of the data and the kdeOntology
			unionModel = ModelFactory.createUnion(dataModel, kdeModel);
			
			// create the ontology model from our ontology.
			kdeOntModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM, kdeModel);
	
			// import regions and create links
			loadRegionFromGeohive("<http://ontologies.geohive.ie/osi#County>", "Counties of Ireland", Constants.ourCounty);
			loadRegionFromGeohive("<http://ontologies.geohive.ie/osi#Electoral_Division>", "Electoral Division in Ireland",
					Constants.ourElectoralDivision);
			
			/* Load legal town and cities + Settlements */
			loadRegionFromGeohive("<http://ontologies.geohive.ie/osi#Census_2011_Settlements>", "skip",
					Constants.ourSettlement);
			computeRegionInCounty(Constants.ourSettlement);
			loadLegalTownFromCso("<http://ontologies.geohive.ie/osi#Census_2011_Cities_and_Legal_Town>", "Legal Towns and Cities in Ireland", Constants.ourLegalTownAndCity);
		
			/* Nationalities */
			loadNationality(unionModel, kdeOntModel, Constants.ourCountyNationalityObs, Constants.ourCounty, Constants.ourNationality,
							  Constants.hasCounty, Constants.hasNationality, Constants.hasPopulation);
			
		}else { // load model from saved
			// Only models required for answering questions
			model_uris.add(Constants.cso_county_age_gender_uri);
			model_uris.add(Constants.cso_electoral_division_age_gender_uri);
			model_uris.add(Constants.cso_county_commuting_means_uri);
			model_uris.add(Constants.cso_commuting_means_uri);
			model_uris.add(Constants.cso_county_population_study_uri);//a
			model_uris.add(Constants.cso_town_city_population_study_uri);//a
			model_uris.add(Constants.cso_fiel_of_study_uri);//a
			model_uris.add(Constants.cso_CTY_irish_speakers_uri);
			model_uris.add(Constants.cso_ability_irish_speakers_uri);
			model_uris.add(Constants.cso_electoral_irish_speakers_uri);
			model_uris.add(Constants.cso_area_uri);
			model_uris.add(Constants.cso_disability_age_group);
			model_uris.add(Constants.cso_disability_age_group_struc);

			System.out.println("Loading all external datasets...");
			Model dataModel = loadModelsFromFile(model_uris);
			loadSavedKdeModel();
			// create the Union of the data and the kdeOntology
			unionModel = ModelFactory.createUnion(dataModel, kdeModel);
						
			// create the ontology model from our ontology.
			//kdeOntModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_LITE_MEM, kdeModel);
		}
		
		long endTime = System.nanoTime();
		System.out.println("Loaded in " + (endTime - startTime) / 1000000000. + " s");
		loaded = true;
	}

	private static void computeRegionInCounty(String ourRegion) {
		// get region wkt, label and object in our ontology
		String queryString = "" + "prefix OO: <" + Constants.KDEontology + "> " +
				"prefix skos: <http://www.w3.org/2004/02/skos/core#>"
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + ""
				+ "SELECT (?region AS ?ourRegion) (STR(?regionLabel) AS ?label) ?wkt " + 
				" WHERE { " + "" +
				" ?region a OO:" + ourRegion + "; "+
				"           OO:" + Constants.linkedGeohiveRegionStr + " ?geoRegion ;" +
				"          rdfs:label ?regionLabel ." +
				" ?geoRegion <http://www.opengis.net/ont/geosparql#hasGeometry> ?geom ." +
				" ?geom <http://www.opengis.net/ont/geosparql#asWKT> ?wkt" + 
				"} " +
				"";
		System.out.println("Computing parent counties for " + ourRegion + " and adding links.");

		ResultSetMem results = OntologyQuerying.getQuerryResults(unionModel, queryString);
//		OntologyQuerying.displayQuery(results);
		
		List<Individual> allRegions = new ArrayList<Individual>();
		List<String> allWkt = new ArrayList<String>();
		List<String> allLabels = new ArrayList<String>();

		while (results.hasNext()) {
			QuerySolution binding = results.nextSolution();
			RDFNode our = binding.get("ourRegion");
			RDFNode wkt = binding.get("wkt");
			RDFNode label = binding.get("label");

			Individual region = (Individual)kdeOntModel.getIndividual(our.toString());
			
			allRegions.add(region);
			allWkt.add(wkt.toString());
			allLabels.add(label.toString());
		}
		List<Geometry> ourRegionsGeom = GISUtils.getGeometryFromWKT(allWkt);
		List<Individual> regionInCounty = GISUtils.getParentGeom(ourRegionsGeom, ourCountiesGeo, ourCountiesInd);
		
		assert regionInCounty.size() == ourRegionsGeom.size();
		
		ObjectProperty inRegionProp = kdeOntModel.getObjectProperty(Constants.KDEontology + Constants.inRegion);
		assert inRegionProp != null;
		// set all parent county
		for (int i = 0; i < regionInCounty.size(); i++) {
			Individual currentRegion = allRegions.get(i);
			Individual parentCounty = regionInCounty.get(i);
			if(parentCounty == null){
				System.out.println("Error, unable to find parent for " + allLabels.get(i));
			}else{
				currentRegion.addProperty(inRegionProp, parentCounty);
			}
		}
	}

	/**
	 * Load the labels and compute the areas of a region type from the geohive
	 * datasets and save them as Individuals in our Ontology. Then, add properties
	 * to the Individuals linking them to the Geohive and cso areas objects
	 */
	private static void loadRegionFromGeohive(String geohiveRegionUri, String regionType, String typeInOurOntology) {

		// Query to get the list of the regions and their labels
		String queryString = "" + "prefix skos: <http://www.w3.org/2004/02/skos/core#>"
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + ""
				+ "SELECT (?region AS ?geoUri) (STR(?regionLabel) AS ?label) ?wkt " + " WHERE { " + "?region a "
				+ geohiveRegionUri + " ; " + "        rdfs:label ?regionLabel ."
				+ "  FILTER (langMatches(lang(?regionLabel),\"en\"))"
				+ "?region <http://www.opengis.net/ont/geosparql#hasGeometry> ?geom ."
				+ "?geom <http://www.opengis.net/ont/geosparql#asWKT> ?wkt" + "} " +
				// "LIMIT 200" +
				"";
		
		// Load the Counties and their areas
		System.out.println(
				"Computing Areas and neighbours for " + typeInOurOntology + " and creating individuals from Geohive...");

		ResultSetMem results = OntologyQuerying.getQuerryResults(unionModel, queryString);
		loadAreas(results, kdeOntModel, typeInOurOntology, 0);
		
		if(regionType == "skip")
			return;
		
		// Create the link with the geohive counties
		String querySimilar = "" + "prefix skos: <http://www.w3.org/2004/02/skos/core#>"
				+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "prefix OO: <" + Constants.KDEontology + ">"
				+ "" + "SELECT ?ourRegionLabel ?regionCsoLabel ?regionOur ?regionCso" + " WHERE { " +
				// Cso counties
				(typeInOurOntology == Constants.ourCounty ? 
						"       FILTER ("
						+ "          STRSTARTS(?regionCsoLabelU, ?ourRegionLabelUS) ||"
						+ "          STRENDS(?regionCsoLabelU, ?ourRegionLabelUSB) ||"
						+ "		   ?regionCsoLabelU = ?ourRegionLabelU ||"
						+ "          ((?ourRegionLabelU = \"DUBLIN\") && (?regionCsoLabelU = \"FINGAL\")) ||	"
						+ "          ((?ourRegionLabelU = \"DUBLIN\") && (?regionCsoLabelU = \"D\u00C3\u00BAN LAOGHAIRE-RATHDOWN\")) "
						+ ") ." 
						: "       FILTER (" + "		   ?regionCsoLabelU = ?ourRegionLabelU" + ") .")
				+
				// OurCounties
				// here we have to match for each geographic county all the sub divisions used
				// in the survey and latter sum them.
				// for example: "DUBLIN"@en in Geohive is to map to "Dublin City"@en and "South
				// Dublin"@en and Fingal and "D\u00C3\u00BAN LAOGHAIRE-RATHDOWN"
				// but not "WESTMEATH"@en and "Meath"@en are they are different counties
				" {SELECT ?regionOur (UCASE(STR(?ourRegionLabel)) AS ?ourRegionLabelU) ?ourRegionLabel "
				+ " (CONCAT(?ourRegionLabelU, \" \") as ?ourRegionLabelUS) "
				+ " (CONCAT(\" \", ?ourRegionLabelU) as ?ourRegionLabelUSB) " + " WHERE {" + 
				"   ?regionOur a OO:"	+ typeInOurOntology + "; " +
						"      OO:" + Constants.linkedGeohiveRegionStr + " ?geoRegion . " +
				"    ?geoRegion rdfs:label ?ourRegionLabel ." +
				"    FILTER (langMatches(lang(?ourRegionLabel),\"en\"))"
				+ "}}"
				+ "{SELECT ?regionCso (UCASE(STR(?regionCsoLabel)) as ?regionCsoLabelU) ?regionCsoLabel WHERE {"
				+ "       ?conceptRegion a skos:ConceptScheme ; "
				+ "                      rdfs:label ?conceptCountyLabel ." + "       FILTER (?conceptCountyLabel = \""
				+ regionType + "\"@en)" + "       ?regionCso skos:inScheme ?conceptRegion ;"
				+ "         		   rdfs:label ?regionCsoLabel . " + "}}" + "}" + " ORDER BY ?regionCso " + "";

		System.out.println("Adding links to the Geohive and cso counties...");
//		OntologyQuerying.queryAndDisplay(unionModel, querySimilar);
//		System.exit(0);
		
		ResultSetMem resultsLinked = OntologyQuerying.getQuerryResults(unionModel, querySimilar);
		linkRegion(resultsLinked, 0);
	}

	/**
	 * Load LegalTown and cities, and their geo and cso link, parent Counties and population
	 */
	private static void loadLegalTownFromCso(String geohiveRegionUri, String regionType, String typeInOurOntology) {

		// Load the Counties and their areas
		System.out.println(
				"Computing geo and cso link, parent Counties and population for " + regionType + "...");

		String queryStringCso = "" + 
				"prefix skos: <http://www.w3.org/2004/02/skos/core#>" +
				"prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + 
				"prefix qb: <http://purl.org/linked-data/cube#>" +
				"prefix sdmxdim: <http://purl.org/linked-data/sdmx/2009/dimension#>" +
				"prefix csoProp: <http://data.cso.ie/census-2011/property/>" +

				"prefix OO: <" + Constants.KDEontology + ">" +
				"" +
		        "SELECT (?regionGeo AS ?geoUri) (STR(?regionCsoLabel) AS ?label) (SUM(?pop) AS ?townPop) ?ourSett ?parentCounty ?regionCso " + 
				" WHERE { " +
				"" + 
				//get the Cso LegalTowns from cso areas
				"{SELECT (UCASE(STR(?regionCsoLabel)) AS ?csoLabelU) ?regionCsoLabel  ?regionCso WHERE {" +
				"       ?conceptRegion a skos:ConceptScheme ; " +
				"                      rdfs:label ?conceptRegionLabel ." + 
				"       FILTER (?conceptRegionLabel = \""+ regionType + "\"@en)" + 
				"       ?regionCso skos:inScheme ?conceptRegion ;"+
				"         		   rdfs:label ?regionCsoLabel . " + 
				"}ORDER BY ASC (?csoLabelU)} " +
				// get the geo LegalTowns
				"{SELECT (MIN(?regionsGeo) as ?regionGeo) (UCASE(STR(?geoLabel)) as ?geoLabelU) ?geoLabel WHERE {" +
				"  ?regionsGeo a " + geohiveRegionUri + " ; " + 
				"        rdfs:label ?geoLabel ." +
				"  FILTER (langMatches(lang(?geoLabel),\"en\"))" +
				"}GROUP BY ?geoLabel ORDER BY ASC (?geoLabelU)} " +
				//match cso and Geo and get parent county
				"FILTER(?csoLabelU = ?geoLabelU)" +
				"?regionGeo <http://ontologies.geohive.ie/osi#partOf> ?settlement ." +
				"?ourSett a OO:" + Constants.ourSettlement + " ;" +
				"         OO:" + Constants.linkedGeohiveRegionStr + " ?settlement ;" +
				"		  OO:" + Constants.inRegion + " ?parentCounty ." +
				//age group
				"      ?conceptScheme a skos:ConceptScheme ; " +
		        "                     rdfs:label \"age group concept scheme\"@en ." +
				"      ?ageGroup skos:inScheme ?conceptScheme ; " +
				"                rdfs:label ?ageGroupLabel ." +
				"      FILTER (?ageGroupLabel = \"all\")" +
				// get Population
				"  ?obs a qb:Observation ." +
				"  ?obs sdmxdim:refArea ?regionCso ." +
				"  ?obs csoProp:population ?pop ." +
				"  ?obs csoProp:age-group ?ageGroup ." +
				"  ?obs csoProp:gender <http://data.cso.ie/census-2011/classification/gender/both> ." +
				"  ?obs qb:dataSet <http://data.cso.ie/census-2011/dataset/age-group-gender-population/lt> ." +
				"}" +
				" GROUP BY ?regionGeo ?regionCsoLabel ?parentCounty ?regionCso ?ourSett" +
				"";
		long startTime = System.nanoTime();
		ResultSetMem resultsCso = OntologyQuerying.getQuerryResults(unionModel, queryStringCso);
		//OntologyQuerying.displayQuery(resultsCso);
		long endTime = System.nanoTime();
	
		// actually load area into ourModel
		OntClass cityClass = kdeOntModel.getOntClass(Constants.KDEontology + Constants.ourCity);
		OntClass townClass = kdeOntModel.getOntClass(Constants.KDEontology + Constants.ourTown);
		assert cityClass != null;
		assert townClass != null;

		ObjectProperty linkedGeohiveRegion = kdeOntModel
				.getObjectProperty(Constants.KDEontology + Constants.linkedGeohiveRegionStr);
		assert linkedGeohiveRegion != null;
		ObjectProperty linkedCsoRegion = kdeOntModel
				.getObjectProperty(Constants.KDEontology + Constants.linkedCsoRegionStr);
		assert linkedCsoRegion != null;
		DatatypeProperty hasPopulationProperty = kdeOntModel.getDatatypeProperty(Constants.KDEontology + Constants.hasPopulation);
		assert hasPopulationProperty != null;
		ObjectProperty inRegionProp = kdeOntModel.getObjectProperty(Constants.KDEontology + Constants.inRegion);
		assert inRegionProp != null;
		
		//create individual for Legal Towns and Cities
		while (resultsCso.hasNext()) {
			QuerySolution binding = resultsCso.nextSolution();
			RDFNode geo = binding.get("geoUri");
			int pop = binding.get("townPop").asLiteral().getInt();
			String label = binding.get("label").toString();
			RDFNode cso = binding.get("regionCso");
			RDFNode parent = binding.get("parentCounty");
			RDFNode settlement = binding.get("?ourSett");
			
			OntClass choosenType = pop > Constants.cityThreshold ? cityClass : townClass;  
			Individual town = choosenType.createIndividual(Constants.KDEontology + choosenType.getLocalName() + "_" + label.replaceAll(" ", "_"));
			town.addProperty(linkedCsoRegion, cso);
			town.addProperty(linkedGeohiveRegion, geo);
			town.addProperty(inRegionProp, parent);
			town.addProperty(inRegionProp, settlement);
			town.addLiteral(hasPopulationProperty, pop);
			town.addLabel(kdeOntModel.createLiteral(choosenType.getLocalName() + ": " + label));
		}
		System.out.println("Done in " + (endTime - startTime) / 1000000000. + "s.");
	}
	/**
	 * From a provided query, import regions (polygons) into our ontology, computing
	 * the area from the wkt. For each imported region, add, a property linking to
	 * the original geohive object.
	 */
	public static void loadAreas(ResultSetMem results, OntModel ourModel, String ourClass, int debug) {

		if (debug == 2) {
			while (results.hasNext()) {
				QuerySolution binding = results.nextSolution();
				for (String column : results.getResultVars()) {
					RDFNode n = binding.get(column);
					String txt;
					if (n.isLiteral())
						txt = ((Literal) n).getLexicalForm();
					else
						txt = n.toString();

					if (column.equals("wkt")) {
						List<String> lst = new ArrayList<String>();
						lst.add(txt);
						List<Geometry> lst2 = GISUtils.getGeometryFromWKT(lst);
						System.out.printf("Area: %.3fkm²", GISUtils.computeArea(lst2).get(0));
					} else {
						System.out.printf("%s: %s, ", column, txt);
					}
				}
				System.out.println("");
			}
		} else {
			// actually load area into ourModel
			OntClass regionClass = ourModel.getOntClass(Constants.KDEontology + ourClass);
			assert regionClass != null;
			ObjectProperty linkedGeohiveRegion = ourModel
					.getObjectProperty(Constants.KDEontology + Constants.linkedGeohiveRegionStr);
			assert linkedGeohiveRegion != null;
			ObjectProperty linkedCsoRegion = kdeOntModel
					.getObjectProperty(Constants.KDEontology + Constants.linkedCsoRegionStr);
			assert linkedCsoRegion != null;

			// get properties from the model, assert they exists
			ObjectProperty hasAreaProp = ourModel.getObjectProperty(Constants.KDEontology + Constants.hasArea);
			assert hasAreaProp != null;
			OntClass regionAreaClass = ourModel.getOntClass(Constants.KDEontology + Constants.regionArea);
			assert regionAreaClass != null;
			ObjectProperty hasUnitProp = ourModel.getObjectProperty(Constants.KDEontology + Constants.hasUnit); 
			assert hasUnitProp != null;
			DatatypeProperty valueDouble = ourModel.getDatatypeProperty(Constants.KDEontology + Constants.valueDouble); 
			assert valueDouble != null;
			Individual squareKilometre = ourModel.getIndividual(Constants.KDEontology + Constants.squareKilometre);
			assert squareKilometre != null;

			ObjectProperty hasNeighbourProp = ourModel
					.getObjectProperty(Constants.KDEontology + Constants.hasNeighbour);
			assert hasNeighbourProp != null;

			// OntProperty rdfsLabel = ourModel.getProperty(rdfs + "label");
			List<Individual> allRegions = new ArrayList<Individual>();
			List<String> allWkt = new ArrayList<String>();

			// Parse the query results
			while (results.hasNext()) {
				QuerySolution binding = results.nextSolution();
				RDFNode uri = binding.get("geoUri");

				String label = WordUtils.capitalize(binding.get("label").asLiteral().getString().toLowerCase());
				RDFNode wkt = binding.get("wkt");
				String shortwkt = wkt.toString();
				shortwkt = shortwkt.substring(0, java.lang.Math.min(30, shortwkt.length()));
				if (debug == 1) {
					System.out.println(
							"Loading uri: " + uri.toString() + ", label: " + label.toString() + ", wkt: " + shortwkt);
				}
				Individual region = regionClass.createIndividual(getUniqueUri(Constants.KDEontology + ourClass + "_" + label.replaceAll(" ", "_")));
//						regionClass.createIndividual(Constants.KDEontology + ourClass + "_" + label.replaceAll(" ", "_") + "_" + uri.asResource().getLocalName());
				// region.addLiteral(rdfsLabel, label.asLiteral());
				region.addLabel(ourModel.createLiteral(ourClass + ": " + label));
				region.addProperty(linkedGeohiveRegion, uri);
				if(binding.contains("regionCso")){
					//Add link to region CSO 
					RDFNode csoUri = binding.get("regionCso");
					region.addProperty(linkedCsoRegion, csoUri);
				}
				allRegions.add(region);
				allWkt.add(wkt.toString());
			}

			// parse WKT to geometries
			List<Geometry> geoms = GISUtils.getGeometryFromWKT(allWkt);
			if(ourClass == Constants.ourCounty){
				// save counties for future use
				ourCountiesGeo = new ArrayList<Geometry>(geoms);
				ourCountiesInd = new ArrayList<Individual>(allRegions);
			}
			// compute area in batch
			List<Double> areas = GISUtils.computeArea(geoms);
			assert areas.size() == allRegions.size();

			ArrayList<ArrayList<Integer>> allNeighboursIdx = GISUtils.computeNeighbours(allRegions, geoms);
			assert allNeighboursIdx.size() == allRegions.size();

			// set all the areas and neighbours
			for (int i = 0; i < areas.size(); i++) {
			
				Individual currentRegion = allRegions.get(i);
				Individual regionArea = regionAreaClass.createIndividual(currentRegion.toString() + "_regionArea");
				if (debug == 1) {
					System.out.println(
							"Setting for: " + currentRegion.toString() + ", area: " + areas.get(i));
				}
				regionArea.addProperty(hasUnitProp, squareKilometre);
				regionArea.addLiteral(valueDouble, areas.get(i));
						
			    currentRegion.addProperty(hasAreaProp, regionArea);
				ArrayList<Integer> neighboursIdx = allNeighboursIdx.get(i);
				for (int j = 0; j < neighboursIdx.size(); j++) {
					allRegions.get(i).addProperty(hasNeighbourProp, allRegions.get(neighboursIdx.get(j)));
				}
			}
			System.out.println("Imported " + areas.size() + " areas");

		}
	}

	/** Get a unique URI in case of existing entry, by adding a number at the end
	 * Very useful for Electoral division that share names */
	private static String getUniqueUri(String string) {
		int i = 0;
		String uri;
		do{
			i++;
			uri = string + "_" + i;
			
		}
		while(kdeOntModel.getIndividual(uri) != null);
		return uri;
	}

	/**
	 * From a provided query results, add the properties linking the objects to the
	 * Census ontology region.
	 */
	public static void linkRegion(ResultSetMem results, int debug) {

		// get properties from the model, assert they exists
		ObjectProperty linkedCsoRegion = kdeOntModel
				.getObjectProperty(Constants.KDEontology + Constants.linkedCsoRegionStr);
		assert linkedCsoRegion != null;

		// Parse the query results
		while (results.hasNext()) {
			QuerySolution binding = results.nextSolution();
			RDFNode cso = binding.get("regionCso");
			RDFNode our = binding.get("regionOur");
			if (debug == 1) {
				System.out.println("Linking: " + our.toString() + " with: " + cso.toString());
			}
			Resource region = kdeOntModel.getResource(our.toString());
			if (!kdeOntModel.contains(region, linkedCsoRegion, cso))
				region.addProperty(linkedCsoRegion, cso);
		}
	}
	
	/**
	 * This method loads nationality in each county along with their population into ourOntology.
	 * @param ontModel contains all the ontologies.
	 */
	public static void loadNationality(Model unionModel, OntModel ourModel, String countyNationalityObs, String county, String nationality, String hasCounty, String hasNationality, String hasPopulation) {
		
		// Query to get the nationality in each county along with their population.
		String queryString = ""
				+ "PREFIX csoClassNat: <http://data.cso.ie/census-2011/classification/nationality/> "
				+ "PREFIX csoClassCty: <http://data.cso.ie/census-2011/classification/CTY/> "
				+ "PREFIX csoProp: <http://data.cso.ie/census-2011/property/> "
				+ "PREFIX purlDim: <http://purl.org/linked-data/sdmx/2009/dimension#> "
				+ "PREFIX xmls: <http://www.w3.org/2001/XMLSchema#> "
				+ "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> "
				+ "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> "
				+ "prefix OO: <" + Constants.KDEontology + "> " 
				+ "SELECT ?ourCounty ?countyLabel ?natLabel (SUM(xmls:integer(?pop)) AS ?population) "
				+ "WHERE { "
				+ "		?id csoProp:nationality ?nat ; "
				+ "		    purlDim:refArea ?county ; "
				+ "		    csoProp:usually-resident-population ?pop . "
				+ "		?county rdfs:label ?countyLab . "
				+ "		?nat rdfs:label ?natLabel . "
				+ "     ?ourCounty OO:" + Constants.linkedCsoRegionStr + " ?county ;" +
						"			rdfs:label ?countyLabel ."
				+ "} "
				+ "GROUP BY ?countyLabel ?natLabel ?ourCounty";
		
		ResultSetMem results = OntologyQuerying.getQuerryResults(unionModel, queryString);
//		OntologyQuerying.displayQuery(results);
		OntClass countyNationalityObsClass = ourModel.getOntClass(Constants.KDEontology + countyNationalityObs);
		assert countyNationalityObsClass != null;
		OntClass countyClass = ourModel.getOntClass(Constants.KDEontology + county);
		assert countyClass != null;
		OntClass nationalityClass = ourModel.getOntClass(Constants.KDEontology + nationality);
		assert nationalityClass != null;
		
		ObjectProperty  hasCountyProperty = ourModel.getObjectProperty(Constants.KDEontology + hasCounty);
		assert hasCountyProperty != null;
		ObjectProperty  hasNationalityProperty = ourModel.getObjectProperty(Constants.KDEontology + hasNationality);
		assert hasNationalityProperty != null;
		DatatypeProperty hasPopulationProperty = ourModel.getDatatypeProperty(Constants.KDEontology + hasPopulation);
		assert hasPopulationProperty != null;
		
		// Parse the query results
		while (results.hasNext()){			
			QuerySolution binding = results.nextSolution();
			Individual countyIndividual = ourModel.getIndividual(binding.getResource("ourCounty").toString());
			String countyLabel = binding.getResource("ourCounty").getLocalName();
			String nationalityLabel = binding.get("natLabel").asLiteral().getString();
			int population = binding.get("population").asLiteral().getInt();
			
			Individual countyNationalityIndividual = ourModel.getIndividual(Constants.KDEontology + countyLabel + "_" + nationalityLabel.replaceAll(" ", "_"));
			if (countyNationalityIndividual == null) {
			
				countyNationalityIndividual = countyNationalityObsClass.createIndividual(Constants.KDEontology + countyLabel + "_" + nationalityLabel.replaceAll(" ", "_"));
			}
			

			Individual nationalityIndividual = ourModel.getIndividual(Constants.KDEontology + nationalityLabel.replaceAll(" ", "_"));
			if (nationalityIndividual == null) {

				nationalityIndividual = nationalityClass.createIndividual(Constants.KDEontology + nationalityLabel.replaceAll(" ", "_"));
				nationalityIndividual.addLabel(ourModel.createLiteral(nationalityLabel));
			}			
			
			countyNationalityIndividual.addProperty(hasCountyProperty, countyIndividual);
			countyNationalityIndividual.addProperty(hasNationalityProperty, nationalityIndividual);
			countyNationalityIndividual.addLiteral(hasPopulationProperty, population);
		}
	}

	/** Save our Model to file */
	public static void writeKdeModelToFile(String archiveFile) throws FileNotFoundException {
		long startTime = System.nanoTime();
		System.out.println("Saving model to: " + archiveFile);
		try {
			kdeModel.write(new FileOutputStream(new File(archiveFile)), "TTL");
		} catch (FileNotFoundException exception) {
			exception.printStackTrace();
			System.exit(-1);
		}
		long endTime = System.nanoTime();
		System.out.println("Done in " + (endTime - startTime) / 1000000000. + "s.");
	}

	public static void loadSavedKdeModel() {
		kdeModel = ModelFactory.createDefaultModel();
		System.out.println("Loading pre-loaded model from: " + Constants.KDEontologyLoadedFile);
		kdeModel.read(Constants.KDEontologyLoadedFile, "TTL");
	}

	/**
	 * Load the datasets, compute the new information and fill our Ontology with
	 * individuals. At the end, save the model to a cache file.
	 */
	public static void main(String[] args) throws IOException, ParseException {

		// Revert locale to a region independent one.
		Locale.setDefault(Locale.ROOT);

		// load models

		loadAllDatasets();
		writeKdeModelToFile(Constants.KDEontologyLoadedFile);

		Boolean runTest = false;
		// verifications
		// test Areas
		if(runTest) {
			String queryCheckArea = "" + "prefix OO: <" + Constants.KDEontology + ">"
					+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + ""
					+ "SELECT DISTINCT ?subject ?typeOfRegion ?area " + " WHERE {" + "      ?subject a ?typeOfRegion ;"
					+ "	            OO:hasArea ?area ." + "}";
			System.out.println("Area verification:");
			OntologyQuerying.queryAndDisplay(kdeOntModel, queryCheckArea);

			// test neighbours
			String queryCheckNeighbours = "" + "prefix OO: <" + Constants.KDEontology + ">"
					+ "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + ""
					+ "SELECT ?subject ?neighbourLabel ?typeOfRegion ?neighbourTypeOfRegion" + " WHERE {"
					+ "    ?subject a ?typeOfRegion ;" + "             OO:hasNeighbour ?neighbour ."
					+ "    ?neighbour rdfs:label ?neighbourLabel ;" + "               a ?neighbourTypeOfRegion ." + "}";
			System.out.println("Neighbourgs verification:");
			OntologyQuerying.queryAndDisplay(kdeOntModel, queryCheckNeighbours);
			
			// test nationality
			String queryCheckNationality = ""
					+ "PREFIX OO: <" + Constants.KDEontology + "> "
							+ " SELECT ?c ?n ?p WHERE {"
							+ "	?cn OO:hasCounty ?c . "
							+ "	?cn OO:hasNationality ?n ."
							+ "	?cn OO:hasPopulation ?p . }";
			System.out.println("Nationality verification:");
			OntologyQuerying.queryAndDisplay(kdeOntModel, queryCheckNationality);
		}

	}
}
