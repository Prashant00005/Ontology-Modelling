package kdeProjectLibs;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.ontology.Individual;

import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;


/** Provide a set of GIS function for importing WKT and computing the area and the neighbours of a geometry, using the Geotools library*/
public class GISUtils {
	public static String irishTM65wkt = "PROJCS[\"TM65 / Irish Grid\",GEOGCS[\"TM65\",DATUM[\"TM65\",SPHEROID[\"Airy Modified 1849\",6377340.189,299.3249646," +
			"AUTHORITY[\"EPSG\",\"7002\"]],AUTHORITY[\"EPSG\",\"6299\"]],PRIMEM[\"Greenwich\",0,AUTHORITY[\"EPSG\",\"8901\"]],UNIT[\"degree\",0.01745329251994328," +
			"AUTHORITY[\"EPSG\",\"9122\"]],AUTHORITY[\"EPSG\",\"4299\"]],UNIT[\"metre\",1,AUTHORITY[\"EPSG\",\"9001\"]],PROJECTION[\"Transverse_Mercator\"]," +
			"PARAMETER[\"latitude_of_origin\",53.5],PARAMETER[\"central_meridian\",-8],PARAMETER[\"scale_factor\",1.000035],PARAMETER[\"false_easting\",200000]," +
			"PARAMETER[\"false_northing\",250000],AUTHORITY[\"EPSG\",\"29902\"],AXIS[\"Easting\",EAST],AXIS[\"Northing\",NORTH]]";

	/** Read the WKT into a Geotools geometry, projecting the WGS84 to the Irish TM65 */
	public static List<Geometry> getGeometryFromWKT(List<String> wkts){
		
		// Convert from input georeference system WGS84 to Irish TM65
		com.vividsolutions.jts.geom.GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory( null );
		WKTReader reader = new WKTReader(geometryFactory);
		
		
		MathTransform transform = null;
		try {
			CoordinateReferenceSystem dataCRS = DefaultGeographicCRS.WGS84;
			CoordinateReferenceSystem targetCRS = CRS.parseWKT(irishTM65wkt);
			boolean lenient = true; // 
			transform = CRS.findMathTransform(dataCRS, targetCRS, lenient);
		}catch (Exception problem){
			problem.printStackTrace();
			System.err.println("Error, unable to initilise the Coordinate system.");
			System.exit(-1);
		}
		assert transform != null;
		
		List<Geometry> allGeom = new ArrayList<Geometry>();
		for(String w: wkts){
			try {
				Geometry geom = reader.read(w);
				allGeom.add(JTS.transform(geom, transform));
			}catch (Exception problem){
				problem.printStackTrace();
				System.err.println("Error, unable to parse wkt.");
				System.exit(-1);
			}
		}
		return allGeom;
	}
	
	/** Compute the Area of a list of Irish geometries and return result in square km. */
	public static List<Double> computeArea(List<Geometry> geoms) {
		List<Double> areas = new ArrayList<Double>();
		
		for(Geometry geo: geoms){
				areas.add(geo.getArea() * .001 * .001);
		}
		return areas;
	}

	/** compute the list of neighbours for all regions with their provided geometries */
	public static ArrayList<ArrayList<Integer>> computeNeighbours(List<Individual> regions, List<Geometry> allGeom) { 
		ArrayList<ArrayList<Integer>> allNeighbours = new ArrayList<ArrayList<Integer>>();
		assert regions.size() == allGeom.size();
		
		for(int i=0; i<allGeom.size(); i++){
			ArrayList<Integer> neighbours = new ArrayList<Integer>();
			Geometry currentGeom = allGeom.get(i);
			for(int j=0; j<allGeom.size(); j++){
				if(j==i) continue;
				if (currentGeom.intersects(allGeom.get(j))){
					neighbours.add(j);
				}
			}
			allNeighbours.add(neighbours);
		}
		return allNeighbours;
	}

	/** Compute the County in which the region is included */
	public static List<Individual> getParentGeom(List<Geometry> ourRegionsGeom,
			List<Geometry> ourCountiesGeo, List<Individual> ourCountiesInd) {
		assert ourCountiesGeo.size() == ourCountiesInd.size();

		List<Individual> countyInd = new ArrayList<Individual>();

		for(int i=0; i<ourRegionsGeom.size(); i++){
			Geometry currentGeom = ourRegionsGeom.get(i);
			Boolean found = false;
			for(int j=0; j<ourCountiesGeo.size(); j++){
				if (currentGeom.intersects(ourCountiesGeo.get(j))){
					countyInd.add(ourCountiesInd.get(j));
					found = true;
					break;
				}
			}
			if(!found){
				countyInd.add(null);
			}
		}
		return countyInd;
	}
}
