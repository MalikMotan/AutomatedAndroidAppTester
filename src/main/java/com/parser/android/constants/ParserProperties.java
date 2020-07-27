package com.parser.android.constants;

import java.util.Properties;

public class ParserProperties {
	
	public static Properties p = new Properties();  
	static {
		p.setProperty("activities","AppCompatActivity,Activity,Fragment");
		
		//List of resources to check
		p.setProperty("resources","Camera,Camera2,External_Drive,Location");

		//Identify the usage of resource in an Activity file
		p.setProperty("Camera.check","Camera.open"); 
		p.setProperty("Camera2.check","getSystemService(Context.CAMERA_SERVICE)");  
		p.setProperty("External_Drive.check","Environment.getExternalStorage");
		p.setProperty("Location.check","LocationServices.getFusedLocationProviderClient");
		
		//Camera checks
		p.setProperty("Camera.check.onCreate","(\\w*)(\\s*)=(\\s*)Camera.open(.*)");
		p.setProperty("Camera.check.onResume","(\\s*)=(\\s*)Camera.open(.*)");
		p.setProperty("Camera.check.onPause",".release()");
		p.setProperty("Camera.check.onDestroy",".release()");
		
		//Camera2 checks
		p.setProperty("Camera2.check.onCreate","CameraManager(\\s+)(\\w*)(\\s*)=(\\s*)(.*)getSystemService(.*)");
		p.setProperty("Camera2.check.onResume","CameraManager(\\s+)(\\w*)(\\s*)=(\\s*)(.*)getSystemService(.*)");
		p.setProperty("Camera2.check.onPause",".close()");
		p.setProperty("Camera2.check.onDestroy",".close()");
		
		//Location checks
		p.setProperty("Location.check.onCreate",".requestLocationUpdates");
		p.setProperty("Location.check.onResume",".requestLocationUpdates");
		p.setProperty("Location.check.onPause",".removeLocationUpdates");
		p.setProperty("Location.check.onDestroy",".removeLocationUpdates");
		
		//External Drive checks
		p.setProperty("External_Drive.check.onCreate.FileOutputStream","(\\w*)(\\s*)=(\\s*)new(\\s+)FileOutputStream(.*)");
		p.setProperty("External_Drive.check.onCreate.FileOutputStream2","(\\w*)(\\s*)=(\\s*)openFileOutput(.*)");
		p.setProperty("External_Drive.check.onCreate.FileInputStream","(\\w*)(\\s*)=(\\s*)new(\\s+)FileInputStream(.*)");
		p.setProperty("External_Drive.check.onCreate.openFileDescriptor","try\\s*\\((.*)openFileDescriptor(.*)\\)");
		p.setProperty("External_Drive.check.onCreate.openInputStream","try\\s*\\((.*)openInputStream(.*)\\)");
		p.setProperty("External_Drive.check.onPause",".close");
		
	}
	
	public static String getValue(String key)
	{	
		return p.getProperty(key);
	}
}