package denfinder.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;

/**
 * List of all zone objects
 * @author latch
 *
 */
public class Map {
	// zones in this list
	private List<ArrayList<Zone>> map;
	
	// User's input data for this map request
	String income;
	String relationshipStatus;
	String age;
	String schoolImportance;
	String address;
	
	private SchoolSet schoolList = new SchoolSet();

	//create new list
	public Map(String address, String newIncome, String newRelationshipStatus,
									   String newAge, String newSchoolImportance) throws JSONException, IOException {
		this.map = new ArrayList<ArrayList<Zone>>();
		
		this.address = address;
		
		Pair<Coordinates, Coordinates> viewport = 
				GeocodingAPI.getCoordinates(address);
		
		Coordinates topRight = viewport.getRight();
		Coordinates bottomLeft = viewport.getLeft();
		
		double latDiff = (topRight.getLatitude() - bottomLeft.getLatitude()) / Common.MAP_DIVISIONS;
		double lonDiff = (topRight.getLongitude() - bottomLeft.getLongitude()) / Common.MAP_DIVISIONS;
		
		System.out.println(" " + latDiff + " " + lonDiff);
		
		//get all states currently in viewport (all corners and center of viewpoint are tested)
		Coordinates topLeft = new Coordinates(bottomLeft.latitude,topRight.longitude);
		Coordinates bottomRight = new Coordinates(topRight.latitude,bottomLeft.longitude);
		Coordinates center = new Coordinates((topLeft.latitude + topRight.latitude) / 2, (topLeft.longitude + bottomLeft.longitude)/2);

		HashSet<String> states = new HashSet<String>();
		states.add(GeocodingAPI.getState(topRight));
		states.add(GeocodingAPI.getState(bottomLeft));
		states.add(GeocodingAPI.getState(topLeft));
		states.add(GeocodingAPI.getState(bottomRight));
		states.add(GeocodingAPI.getState(center));
		
		//debug information
		for(String s : states)
			System.out.println("[DEBUG] Adding all schools from state: " + s);
				
		schoolList.populate(states);
		
		for (int i = 0; i < Common.MAP_DIVISIONS; i++) {
			ArrayList<Zone> longitudes = new ArrayList<Zone>();
			for (int j = 0; j < Common.MAP_DIVISIONS; j++) {
				longitudes.add(new Zone(new Coordinates(bottomLeft.getLatitude() + latDiff * i,
														bottomLeft.getLongitude() + lonDiff * j)));
			}
			map.add(longitudes);
		}
		
		income 			   = newIncome;
		relationshipStatus = newRelationshipStatus;
		age   			   = newAge;
		schoolImportance   = newSchoolImportance;
	}
	
	public int getDimensions() {
		return map.size();
	}
	
	public Coordinates getMiddle() {
		return map.get(map.size()/2).get(map.get(map.size()/2).size()).getLocation();		
	}
	
	public Zone getZone(int x, int y) {
		return map.get(x).get(y);
	}
	
	public String getAddress() {
		return this.address;
	}
	
	public List<ArrayList<Zone>> getRawMap() {
		return map;
	}
	
	public void rateAllZones() {
		for (int i = 0; i < map.size(); i++) {
			for (int j = 0; j < map.get(i).size(); j++) {
				rateZone(map.get(i).get(j));
			}
		}
	}
	
	public void rateZone(Zone aZone) {
		double zoneRating = 0.0;
		
		// Income rating:
		double  incomePoints   = 0.0;
		boolean matchingIncome = false;
		int     medianIncome   = aZone.getMedianIncome();
		int     targetIncome   = 0;
		
		
		switch(income) {
			case Common.INCOME_STRING_TIER_1:
				if (medianIncome >= Common.INCOME_TIER_1 &&
					medianIncome <= Common.INCOME_TIER_2) {
					matchingIncome = true;
				}
				targetIncome = (Common.INCOME_TIER_1 + Common.INCOME_TIER_2) / 2;
			
				break;
		
			case Common.INCOME_STRING_TIER_2:
				if (medianIncome >= Common.INCOME_TIER_2 &&
					medianIncome <= Common.INCOME_TIER_3) {
					matchingIncome = true;
				}
				targetIncome = (Common.INCOME_TIER_2 + Common.INCOME_TIER_3) / 2;
				
				break;
				
			case Common.INCOME_STRING_TIER_3:
				if (medianIncome >= Common.INCOME_TIER_3 &&
					medianIncome <= Common.INCOME_TIER_4) {
					matchingIncome = true;
				}
				targetIncome = (Common.INCOME_TIER_3 + Common.INCOME_TIER_4) / 2;
				
				break;
				
			case Common.INCOME_STRING_TIER_4:
				if (medianIncome >= Common.INCOME_TIER_4 &&
					medianIncome <= Common.INCOME_TIER_5) {
					matchingIncome = true;
				}
				targetIncome = (Common.INCOME_TIER_4 + Common.INCOME_TIER_5) / 2;
				
				break;
				
			case Common.INCOME_STRING_TIER_5:
				if (medianIncome >= Common.INCOME_TIER_5) {
					matchingIncome = true;
				}
				targetIncome = Common.INCOME_TIER_5;
				
				break;
		}
		
		if (matchingIncome) {
			incomePoints = Common.MATCHING_INCOME_POINTS;
		}
		else {
			incomePoints = rateIncome(medianIncome, targetIncome);
		}
		
		
		zoneRating += incomePoints;
		
		
		// Age rating:
		double agePoints    = 0.0;
		boolean matchingAge = false;
		double medianAge    = aZone.getMedianAge();
		double targetAge    = 0.0;
		
		switch(age) {
			case Common.AGE_STRING_TIER_1:
				if (medianAge >= Common.AGE_TIER_1 &&
					medianAge <= Common.AGE_TIER_2) {
					matchingAge = true;
				}
				targetAge = (Common.AGE_TIER_1 + Common.AGE_TIER_2) / 2;
				
				break;
				
			case Common.AGE_STRING_TIER_2:
				if (medianAge >= Common.AGE_TIER_2 &&
					medianAge <= Common.AGE_TIER_3) {
					matchingAge = true;
				}
				targetAge = (Common.AGE_TIER_2 + Common.AGE_TIER_2) / 3;
				
				break;
				
			case Common.AGE_STRING_TIER_3:
				if (medianAge >= Common.AGE_TIER_3 &&
					medianAge <= Common.AGE_TIER_4) {
					matchingAge = true;
				}
				targetAge = (Common.AGE_TIER_3 + Common.AGE_TIER_4) / 2;
				
				break;
				
			case Common.AGE_STRING_TIER_4:
				if (medianAge >= Common.AGE_TIER_4 &&
					medianAge <= Common.AGE_TIER_5) {
					matchingAge = true;
				}
				targetAge = (Common.AGE_TIER_4 + Common.AGE_TIER_5) / 2;
				
				break;
				
			case Common.AGE_STRING_TIER_5:
				if (medianAge >= Common.AGE_TIER_5 &&
					medianAge <= Common.AGE_TIER_6) {
					matchingAge = true;
				}
				targetAge = (Common.AGE_TIER_5 + Common.AGE_TIER_6) / 2;
				
				break;
				
			case Common.AGE_STRING_TIER_6:
				if (medianAge >= Common.AGE_TIER_6) {
					matchingAge = true;
				}
				targetAge = Common.AGE_TIER_6;
				
				break;
		}
		
		if (matchingAge) {
			agePoints = Common.MATCHING_AGE_POINTS;
		}
		else {
			agePoints = rateAge(medianAge, targetAge);
		}
		
		
		zoneRating += agePoints;
		
		
		// Relationship status rating:
		double relationshipPoints = 0;
		
		double ratio      = 0;
		double numSingle  = aZone.getNumSingle();
		double numMarried = aZone.getNumMarried();
		
		if (relationshipStatus.equals(Common.RELATIONSHIP_STRING_SINGLE) &&
			numMarried != 0) {
			
			ratio = numSingle / numMarried;
		}
		
		else if (relationshipStatus.equals(Common.RELATIONSHIP_STRING_MARRIED) &&
			numSingle != 0) {
			
			ratio = numMarried / numSingle;
		}
		
		if (ratio != 0) {
			if (ratio >= Common.RELATIONSHIP_MATCHING_RATIO_TIER_1 && 
				ratio <= Common.RELATIONSHIP_MATCHING_RATIO_TIER_2) {
				
				relationshipPoints += Common.RELATIONSHIP_MATCHING_POINTS_TIER_1;
			}
			else if (ratio >= Common.RELATIONSHIP_MATCHING_RATIO_TIER_2 &&
					 ratio <= Common.RELATIONSHIP_MATCHING_RATIO_TIER_3) {
				
				relationshipPoints += Common.RELATIONSHIP_MATCHING_POINTS_TIER_2;
			}
			else if (ratio >= Common.RELATIONSHIP_MATCHING_RATIO_TIER_3) {
				
				relationshipPoints += Common.RELATIONSHIP_MATCHING_POINTS_TIER_3;
			}
			
			
			else if (ratio <= Common.RELATIONSHIP_NOT_MATCHING_RATIO_TIER_1 && 
					 ratio >= Common.RELATIONSHIP_NOT_MATCHING_RATIO_TIER_2) {
					
				relationshipPoints += Common.RELATIONSHIP_NOT_MATCHING_POINTS_TIER_1;
			}
			else if (ratio <= Common.RELATIONSHIP_NOT_MATCHING_RATIO_TIER_2 &&
					 ratio >= Common.RELATIONSHIP_NOT_MATCHING_RATIO_TIER_3) {
					
				relationshipPoints += Common.RELATIONSHIP_NOT_MATCHING_POINTS_TIER_2;
			}
			else if (ratio <= Common.RELATIONSHIP_NOT_MATCHING_RATIO_TIER_3) {
				
				relationshipPoints += Common.RELATIONSHIP_NOT_MATCHING_POINTS_TIER_3;
			}
		}
		
		zoneRating += relationshipPoints;
		
		
		// Education rating:
		double educationPoints = 0;
		


		
		
		
		// TODO: Education rating part of algorithm
		
		
		
		
		
		
		
		zoneRating += educationPoints;
		
		
		if (zoneRating > Common.MAXIMUM_RATING) {
			zoneRating = Common.MAXIMUM_RATING;
		}
		else if (zoneRating < Common.MINIMUM_RATING) {
			zoneRating = Common.MINIMUM_RATING;
		}
		
		aZone.setRating(zoneRating);
	}

	
	private double rateIncome(int actual, int target) {
		// The difference in income between the actual median income of the
		// zone and the middle of the user's desired income range:
		double incomeDifference = Math.abs(actual - target);
		// The further away from the target the median income is, the less
		// the zone is worth.
		double exponent = incomeDifference / Common.INCOME_RATING_DIVIDEND;
		exponent *= -1.0;
		// The number of points the income contributes to the zone.
		double rating = 0.0;
		
		// Rating for a median income closer to the target:
		if (incomeDifference <= Common.INCOME_RATING_DIFFERENCE_NEAR) {
			rating = Common.MATCHING_INCOME_POINTS * 
					 Math.pow(Common.INCOME_RATING_BASE_NEAR, exponent);
		}
		// Rating for a median income far from the target:
		else if (incomeDifference <= Common.INCOME_RATING_DIFFERENCE_FAR){
			rating = Common.MATCHING_INCOME_POINTS * 
					 Math.pow(Common.INCOME_RATING_BASE_FAR, exponent);
		}
		// Rating for a median income extremely far from the target (worth
		// negative points):
		else {
			double negativeExponent = Math.abs(incomeDifference - 
									  Common.INCOME_RATING_DIFFERENCE_FAR) /
									  Common.INCOME_RATING_DIVIDEND;
			
			rating = Math.pow(Common.INCOME_RATING_BASE_FARTHEST, negativeExponent);
			rating *= -1.0;
		}
		
		return rating;
	}
	
	private double rateAge(double actual, double target) {
		// The difference in age between the actual median age of the
		// zone and the middle of the user's desired age range:
		double ageDifference = Math.abs(actual - target);
		// The further away from the target the median age is, the less
		// the zone is worth.
		double exponent = ageDifference / Common.AGE_RATING_DIVIDEND;
		exponent *= -1.0;
		// The number of points the age contributes to the zone.
		double rating = 0.0;
		
		// Rating for a median age closer to the target:
		if (ageDifference <= Common.AGE_RATING_DIFFERENCE_NEAR) {
			rating = Common.MATCHING_AGE_POINTS * 
					 Math.pow(Common.AGE_RATING_BASE_NEAR, exponent);
		}
		// Rating for a median age far from the target:
		else if (ageDifference <= Common.AGE_RATING_DIFFERENCE_FAR){
			rating = Common.MATCHING_AGE_POINTS * 
					 Math.pow(Common.AGE_RATING_BASE_FAR, exponent);
		}
		// Rating for a median age extremely far from the target (worth
		// negative points):
		else {
			double negativeExponent = Math.abs(ageDifference - 
									  Common.AGE_RATING_DIFFERENCE_FAR) /
									  Common.AGE_RATING_DIVIDEND;
			
			rating = Math.pow(Common.INCOME_RATING_BASE_FARTHEST, negativeExponent);
			rating *= -1.0;
		}
		
		return rating;
	}
	
}
