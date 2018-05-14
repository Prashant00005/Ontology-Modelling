package kdeProjectLibs;

/** Regroup all the constants required for the project, file paths and ontology object and resource uris. */
public class Constants {
	
	//data files
	public static String cso_county_age_gender_uri = "data/data.cso.ie/CTY_age-group-gender-population.nt.gz";
	
	public static String cso_structure_age_gender_uri = "data/data.cso.ie/structure_age-group-gender-population.nt.gz";
	public static String cso_area_uri = "data/data.cso.ie/areas.nt.gz";
	
	public static String cso_county_nationality_uri = "data/data.cso.ie/CTY_population-by-nationality.nt.gz";
	public static String cso_electoral_division_age_gender_uri = "data/data.cso.ie/ED_age-group-gender-population.nt.gz";

//	public static String cso_ability_to_speak_irish_uri = "data/data.cso.ie/ability-to-speak-irish.nt.gz";
//	public static String cso_irish_speakers_uri = "data/data.cso.ie/CTY_irish-speakers.nt.gz";

	public static String cso_county_commuting_means_uri = "data/data.cso.ie/CTY_population-commuting-means.nt.gz";
	public static String cso_commuting_means_uri = "data/data.cso.ie/means-of-travel-to-work-school-college.nt.gz";
	public static String cso_disability_age_group = "data/data.cso.ie/CTY_persons-with-disability.nt.gz";
	public static String cso_disability_age_group_struc = "data/data.cso.ie/Q6age-group.nt.gz";
	
	public static String cso_county_population_study_uri = "data/data.cso.ie/CTY_population-field-of-study.nt.gz";//a
	public static String cso_town_city_population_study_uri = "data/data.cso.ie/LT_population-field-of-study.nt.gz";//a
	public static String cso_fiel_of_study_uri = "data/data.cso.ie/field-of-study.nt.gz";//a
	
	
	public static String cso_age_group_uri = "data/data.cso.ie/age-group.nt.gz";
	
	public static String geohive_county_100m = "data/data.geohive.ie/county_100m.ttl.gz";
	public static String geohive_electoral_division_100m = "data/data.geohive.ie/electoral_division_100m.ttl.gz";
	public static String geohive_settlements = "data/data.geohive.ie/settlements_from_fulldump.ttl.gz";
	public static String geohive_cities_and_legal_towns_100m = "data/data.geohive.ie/legal_towns_cities_100m.ttl.gz";
	
	
//	public static String geohive_townlands_100m = "data/data.geohive.ie/townland_100m.ttl";	
//	public static String cso_town_city_age_group_gender_uri = "data/data.cso.ie/ST_age-group-gender-population.nt.gz";
	public static String cso_legal_town_city_age_group_gender_uri = "data/data.cso.ie/LT_age-group-gender-population.nt.gz";
	public static String cso_nationality_uri = "data/data.cso.ie/nationality.nt.gz";
	
	
	public static String cso_CTY_irish_speakers_uri = "data/data.cso.ie/CTY_irish-speakers.nt.gz";
	public static String cso_ability_irish_speakers_uri = "data/data.cso.ie/ability-to-speak-irish.nt.gz";
	public static String cso_electoral_irish_speakers_uri = "data/data.cso.ie/ED_irish-speakers.nt.gz";


	//Our Ontology
	public static String KDEontologyFile = "ontologies/kdeOntology.ttl";
	public static String KDEontology = "http://www.semanticweb.org/kdeProject/group4/kdeOntology/";
	public static String KDEontologyLoadedFile = "ontologies/kdeOntologyLoaded.ttl";
	
	//Resources in our Ontology
	public static String linkedCsoRegionStr = "linkedCsoRegion";
	public static String linkedGeohiveRegionStr = "linkedGeohiveRegion";
	public static String ourLegalTownAndCity = "LegalTownAndCity";
	public static String ourElectoralDivision = "ElectoralDivision";

	//Classes
	public static String ourCounty = "County";
	public static String ourRegion = "Region";
	public static String regionArea = "RegionArea";
	public static String areaUnits = "AreaUnits";
	public static String ourCityAndTown = "CityAndTown";
	public static String ourSettlement = "Settlement";

	
	public static String ourCountyNationalityObs = "CountyNationalityObs";
	public static String ourNationality = "Nationality";
	
	//object properties
	public static String hasUnit = "hasUnit";
	public static String hasArea = "hasArea";
	public static String hasNeighbour = "hasNeighbour";
	
	public static String hasCounty = "hasCounty";
	public static String hasNationality = "hasNationality";
	public static String hasPopulation = "hasPopulation";
	public static String inRegion = "inRegion";
	public static String ourCity = "City";
	public static String ourTown = "Town";

	
	//data properties
	public static String valueDouble = "valueDouble";
	
	//Individuals
	public static String squareKilometre = "squareKilometre";
	
	public static int cityThreshold = 10000;
}
