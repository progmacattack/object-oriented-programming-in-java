package earthquakemap;

import de.fhpotsdam.unfolding.UnfoldingMap;
import de.fhpotsdam.unfolding.data.PointFeature;
import de.fhpotsdam.unfolding.geo.Location;
import de.fhpotsdam.unfolding.utils.GeoUtils;
import de.fhpotsdam.unfolding.utils.ScreenPosition;
import processing.core.PConstants;
import processing.core.PGraphics;

/** Implements a visual marker for earthquakes on an earthquake map
 * 
 * @author UC San Diego Intermediate Software Development MOOC team
 * @author Adam Sickmiller Threat radius display extension  
 */
public abstract class EarthquakeMarker extends CommonMarker implements Comparable<EarthquakeMarker>
{
	
	// Did the earthquake occur on land?  This will be set by the subclasses.
	protected boolean isOnLand;

	// The radius of the Earthquake marker
	// You will want to set this in the constructor, either
	// using the thresholds below, or a continuous function
	// based on magnitude. 
	protected float radius;

	//EXTENSION ASSIGNMENT
	//if set to true, the drawThreatRadius method will be called for the marker
	private boolean drawThreatRadius = false;

	//EXTENSION ASSIGNMENT
	private UnfoldingMap map;

	// constants for distance
	protected static final float kmPerMile = 1.6f;
	
	/** Greater than or equal to this threshold is a moderate earthquake */
	public static final float THRESHOLD_MODERATE = 5;
	/** Greater than or equal to this threshold is a light earthquake */
	public static final float THRESHOLD_LIGHT = 4;

	/** Greater than or equal to this threshold is an intermediate depth */
	public static final float THRESHOLD_INTERMEDIATE = 70;
	/** Greater than or equal to this threshold is a deep depth */
	public static final float THRESHOLD_DEEP = 300;

	// ADD constants for colors

	
	// abstract method implemented in derived classes
	public abstract void drawEarthquake(PGraphics pg, float x, float y);
		
	
	// constructor
	public EarthquakeMarker (PointFeature feature) 
	{
		super(feature.getLocation());
		// Add a radius property and then set the properties
		java.util.HashMap<String, Object> properties = feature.getProperties();
		float magnitude = Float.parseFloat(properties.get("magnitude").toString());
		properties.put("radius", 2*magnitude );
		setProperties(properties);
		this.radius = 1.75f*getMagnitude();
	}
	

	public int compareTo(EarthquakeMarker marker) {
		//sort in reverse order of magnitude e.g. this is 4 marker is 3, then 
		float diff = marker.getMagnitude() - this.getMagnitude();
		if(diff < 0) {
			return -1;
		} else if (diff > 0) {
			return 1;
		}
		return 0;
	}
	
	
	// calls abstract method drawEarthquake and then checks age and draws X if needed
	@Override
	public void drawMarker(PGraphics pg, float x, float y) {
		// save previous styling
		pg.pushStyle();
			
		// determine color of marker from depth
		colorDetermine(pg);
		
		// call abstract method implemented in child class to draw marker shape
		drawEarthquake(pg, x, y);
		
		// IMPLEMENT: add X over marker if within past day		
		String age = getStringProperty("age");
		if ("Past Hour".equals(age) || "Past Day".equals(age)) {
			
			pg.strokeWeight(2);
			int buffer = 2;
			pg.line(x-(radius+buffer), 
					y-(radius+buffer), 
					x+radius+buffer, 
					y+radius+buffer);
			pg.line(x-(radius+buffer), 
					y+(radius+buffer), 
					x+radius+buffer, 
					y-(radius+buffer));
			
		}
		
		// reset to previous styling
		pg.popStyle();
		
		//draw threat radius
		if(this.drawThreatRadius) {
			drawThreatRadius(pg, x, y);
		}
	}

	/** Show the title of the earthquake if this marker is selected */
	public void showTitle(PGraphics pg, float x, float y)
	{
		String title = getTitle();
		pg.pushStyle();
		
		pg.rectMode(PConstants.CORNER);
		
		pg.stroke(110);
		pg.fill(255,255,255);
		pg.rect(x, y + 15, pg.textWidth(title) +6, 18, 5);
		
		pg.textAlign(PConstants.LEFT, PConstants.TOP);
		pg.fill(0);
		pg.text(title, x + 3 , y +18);
		
		
		pg.popStyle();
		
	}

	
	/**
	 * Return the "threat circle" radius, or distance up to 
	 * which this earthquake can affect things, for this earthquake.   
	 * DISCLAIMER: this formula is for illustration purposes
	 *  only and is not intended to be used for safety-critical 
	 *  or predictive applications.
	 */
	public double threatCircle() {	
		double miles = 20.0f * Math.pow(1.8, 2*getMagnitude()-5);
		double km = (miles * kmPerMile);
		return km;
	}
	
	// determine color of marker from depth
	// We use: Deep = red, intermediate = blue, shallow = yellow
	private void colorDetermine(PGraphics pg) {
		float depth = getDepth();
		
		if (depth < THRESHOLD_INTERMEDIATE) {
			pg.fill(255, 255, 0);
		}
		else if (depth < THRESHOLD_DEEP) {
			pg.fill(0, 0, 255);
		}
		else {
			pg.fill(255, 0, 0);
		}
	}
	
	
	/** toString
	 * Returns an earthquake marker's string representation
	 * @return the string representation of an earthquake marker.
	 */
	public String toString()
	{
		return getTitle();
	}
	/*
	 * getters for earthquake properties
	 */
	
	public float getMagnitude() {
		return Float.parseFloat(getProperty("magnitude").toString());
	}
	
	public float getDepth() {
		return Float.parseFloat(getProperty("depth").toString());	
	}
	
	public String getTitle() {
		return (String) getProperty("title");	
		
	}
	
	public float getRadius() {
		return Float.parseFloat(getProperty("radius").toString());
	}
	
	public boolean isOnLand()
	{
		return isOnLand;
	}

	//EXTENSION ASSIGNMENT
	/** Draw the threat radius. This method will run continuously. 
	 * 
	 * @param pg
 	 * @param x x-coord on map to center the radius
	 * @param y y-coor on map to center the radius
	 */
	private void drawThreatRadius(PGraphics pg, float x, float y) {
		float threatRadius = (float)this.threatCircle();
		//here we solve the problem of the threat circle being in km but we need it in pixels
		//use geoutils to create a second location at the edge of the threat radius bearing 90 degrees (straight east).
		//this will help us get the pixel location of both this and the new location
		Location threatEdge = GeoUtils.getDestinationLocation(this.getLocation(), 90, threatRadius);
		ScreenPosition mainPos = map.getScreenPosition(this.getLocation()); 
		ScreenPosition threatEdgePos = map.getScreenPosition(threatEdge);
		float threatRadiusPx;
		if(threatEdgePos.x > mainPos.x) {
			threatRadiusPx = threatEdgePos.x - mainPos.x;
			pg.noFill();
			pg.stroke(255,255,255);
			pg.ellipse(x, y, threatRadiusPx * 2, threatRadiusPx * 2);
			pg.stroke(0,0,0); //reset stroke color
		} else { //this means the threat radius goes off the edge of the map. eventually implement circles that start over on other side of map
			threatRadiusPx = mainPos.x + threatEdgePos.x;
		}
	}

	//EXTENSION ASSIGNMENT
	//we must bring in the map so that to draw the threat radius
	public void setMap(UnfoldingMap map) {
		this.map = map;
	}
	
	//EXTENSION ASSIGNMENT
	//set flag that says whether to draw the threat radius for this marker
	public void setDrawThreatRadius(boolean drawThreatRadius) {
		this.drawThreatRadius = drawThreatRadius;
	}

}
