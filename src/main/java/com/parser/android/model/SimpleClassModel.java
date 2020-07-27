package com.parser.android.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleClassModel {
	
	private String filePath;
	
	private List<String> fields = new ArrayList<String>();
	
	private Map<String, String> methods = new HashMap<String, String>();
	
	private List<String> resources = new ArrayList<String>();

	public List<String> getFields() {
		return fields;
	}

	public void setFields(List<String> fields) {
		this.fields = fields;
	}

	public Map<String, String> getMethods() {
		return methods;
	}

	public void setMethods(Map<String, String> methods) {
		this.methods = methods;
	}

	public List<String> getResources() {
		return resources;
	}

	public void setResources(List<String> resources) {
		this.resources = resources;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	
}