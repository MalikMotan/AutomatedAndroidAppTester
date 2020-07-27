package com.parser.android.constants;

public class Constant {
	
	//Methods
	public static enum Method { onCreate(), onResume(), onPause(), onDestroy() };  
	
	//Resources
	public static final String CAMERA 			= "Camera";
	public static final String CAMERA2 			= "Camera2";
	public static final String EXTERNAL_DRIVE 	= "External_Drive";
	public static final String LOCATION 		= "Location";
	public static final String ACQUIRED 		= "acquired";
	public static final String NOT_ACQUIRED 	= "not acquired";
	public static final String RELEASED 		= "released";
	public static final String NOT_RELEASED 	= "not released";
	
	
	public static final int DEFAULT_SPACE_FOR_FILE = 46;

	
	//Suggestions
	public static final String CAMERA_RECOMMENDED_TEXT 			= "You must release Camera by using ###.release() method in the onPause() or in onDestroy() activity lifecycle method.";
	public static final String CAMERA2_RECOMMENDED_TEXT 		= "You must release Camera by using ###.close() method in the onPause() or in onDestroy() activity lifecycle method.";
	public static final String EXTERNAL_DRIVE_RECOMMENDED_TEXT 	= "You must close external storage access by using ###.close() in $$$() method.";
	public static final String LOCATION_RECOMMENDED_TEXT 		= "You must stop Location Updates by using ###.removeLocationUpdates() method in the onPause() or onDestroy() activity lifecycle method.";
	
	
}