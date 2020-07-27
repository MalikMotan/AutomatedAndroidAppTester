package com.parser.android.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class LogWriter {

	private static volatile LogWriter instance;
	private static Object mutex = new Object();

	private LogWriter() {
	}

	public static LogWriter getInstance() {
		LogWriter result = instance;
		if (result == null) {
			synchronized (mutex) {
				result = instance;
				if (result == null)
					instance = result = new LogWriter();
			}
		}
		return result;
	}
	
	
	/**
	 * 
	 * @param path
	 * @param data
	 * @param noOfLines
	 */
	public void log(String path, String fileName, List<String> messages) {
		BufferedWriter writer = null;
	    String dirPath = path + System.getProperty("file.separator") + "log";
	    String filePath = dirPath + System.getProperty("file.separator") + fileName + ".txt";

	    File directory = new File(dirPath);
	    if (! directory.exists()){
	        directory.mkdir();
	    }
	    
	    File file = new File(filePath);
	    try{
	    	file.delete();
	    	file.createNewFile();
	    	writer = new BufferedWriter(new FileWriter(file));
	        for (String message:messages) {
	            writer.write(message + System.getProperty("line.separator") );
	        }
	    } catch (IOException e) {
	        e.printStackTrace();
	    }finally{
	        try {
	        	if (writer != null) {
	                writer.close();
	              }
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	}
}