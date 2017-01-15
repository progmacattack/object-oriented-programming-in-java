### object-oriented-programming-in-java
Summary
========
This is my extension of the earthquake app built for this class. It draws a threat radius circle around earthquakes that are pulled from a data feed. This program extends on the earthquake threat radius concept introduced in this coursera course.
By clicking a button, all markers disappear except those representing the strongest earthquakes. 
A threat radius for each of these markers is drawn, giving the user a quick view of the entire area potentially affected by an earthquake.

![Project Screenshot](http://i.imgur.com/hsa8jfM.jpg)



In 1-3 paragraphs (3-5 sentences each) or a bulleted list (1-3 sentences per bullet) describe the additions/modifications you made to the code to support your extension (step 3 from the assignment instructions).
Changes were made in two classes - the EarthquakeCityMap class and the EarthquakeMarker class. Broadly, the changes in the EarthquakeMarker class allow a scale-independent threat circle to be drawn for each EarthquakeMarker and the changes to EarthquakeCityMap define the logic that says when the threat circle will be drawn.
+ In the EarthquakeMarker class: A new boolean member variable was added to to flag whether or not the threat circle should be drawn on the map for the earthquakeMarker instance. If this flag is set to true using setDrawThreatRadius, then the drawThreatRadius method will be continually executed.
+ In the EarthquakeMarker class: Member variables with setters were set up to allow EarthquakeCityMap to set the map object on the EarthquakeMarker instance. This was necessary in order to translate the threat circle distance from kilometers into projected pixels on the map.
+ In the EarthquakeMarker class: This method, the drawThreatRadius, was added to earthquake marker class and is called from the drawMarker method if the drawThreatRadius boolean member variable is set to true. 
+ In the EarthquakeMarker class: The new drawThreatRadius method receives as arguments the working PGraphics, and the center point of the EarthquakeMarker as float x and float y. This method relies on the threatCircle method to calculate the radius of the threat circle in kilometers and it relies on the map object being set so that we can convert the km to pixels. This method then uses unfolding maps GeoUtils utility class to create a new Location object due east at X km away, with X being the radius of the threatCircle in km. Creating this location object allows us to convert the distance between the EarthquakeMarker and the edge of the threat radius from kilometers to pixels and then draw the ellipse on the map of the threat circle.
+ In the EarthquakeCityMap class: A new member variable, a list of EarthquakeMarkers was added to contain the sorted list of earthquakes. During the setup method, the new private method sortEqMarkers is called to populate this list based and sort it based on earthquake magnitude.
+ In the EarthquakeCityMap class: The EarthquakeCityMap class was expanded to draw and detect clicks on a new button that was added to the top right of the map. This button is initially drawn in showTopTenClickArea method. This method draws the box and sets the text.
+ In the EarthquakeCityMap class: The mouseClicked() method was expanded to call the showTopTenClick() method whenever the mouse button is clicked. The showTopTenClick() method creates a new instance of a newly-created subclass called 'ClickArea' and then looks to see if the mouse is within this area when it was clicked. Please note that the screen show does not show all ten earthquakes, because some are in the Fiji and Vanuatu area, which is not displayed.
+ In the EarthquakeCityMap class: Based on the above design, when the user clicks on the new button, the new setDrawThreatRadius is set to true for each of the top ten earthquakeMarkers. Next, all other earthquakeMarkers are hidden. If the user clicks again, the map is restored to original state. This click patten is designed the same as the current click pattern, which sets the lastClicked member variable.
