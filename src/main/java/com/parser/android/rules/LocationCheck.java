package com.parser.android.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Strings;
import com.parser.android.constants.Constant;
import com.parser.android.constants.ParserProperties;
import com.parser.android.model.SimpleClassModel;

public class LocationCheck implements ResourceCheck {

	SimpleClassModel model;
	String methodName = "";
	String fieldName = "";
	List<String> msgs = new ArrayList<String>();
	StringBuilder acquiredSb = new StringBuilder(Strings.padStart("", 150, ' '));
	int textPostion = 0;
	
	public LocationCheck(SimpleClassModel model) {
		this.model = model;
	}

	/**
	 * Used to check location resource acquired or not in onCreate activity lifecycle method
	 */
	@Override
	public boolean isAcquired() {
		acquiredSb.setLength(100);
		if (model.getMethods().containsKey("onCreate")) {
			for (String field : model.getFields()) {
				if (field.contains("FusedLocationProviderClient")) {
					// check instance initialization
					for (Map.Entry<String, String> entry : model.getMethods().entrySet()) {
						String pattern = "(\\w*)(\\s*)=(\\s*)LocationServices.getFusedLocationProviderClient(.*)";;
						Pattern patternObj = Pattern.compile(pattern);
						Matcher matcher = patternObj.matcher(entry.getValue());
						if (matcher.find()) {
							fieldName = matcher.group(1);
							if (field.contains(fieldName)) {
								methodName = entry.getKey();
								if (methodName.equals("onCreate")) {
									if (checkResourceAcquired(ParserProperties.getValue("Location.check.onCreate"),"onCreate")) {
										acquiredSb.insert(textPostion, Constant.LOCATION + " " + Constant.ACQUIRED);
										textPostion = textPostion + Constant.DEFAULT_SPACE_FOR_FILE;
										return true;
									} 
								} else {
									if (checkMethodCall(methodName, model.getMethods(), model.getFields(), "onCreate")) {
										if (checkResourceAcquired(ParserProperties.getValue("Location.check.onCreate"),"onCreate")) {
											acquiredSb.insert(textPostion, Constant.LOCATION + " " + Constant.ACQUIRED);
											textPostion = textPostion + Constant.DEFAULT_SPACE_FOR_FILE;
											return true;
										}
									}
								}
							}
						}
					}
				}
			}
			acquiredSb.insert(textPostion, Constant.LOCATION + " " + Constant.NOT_ACQUIRED);
			textPostion = textPostion + Constant.DEFAULT_SPACE_FOR_FILE;
		}
		return false;
	}

	/**
	 * Used to check location resource released or not in onPause activity lifecycle method
	 */
	@Override
	public boolean isReleased() {
		if (model.getMethods().containsKey("onPause")) {
			if (checkResourceReleased(ParserProperties.getValue("Location.check.onPause"),"onPause")) {
				acquiredSb.insert(textPostion, Constant.LOCATION + " " + Constant.RELEASED);
				textPostion = textPostion + Constant.DEFAULT_SPACE_FOR_FILE;
				return true;
			}
			acquiredSb.insert(textPostion, Constant.LOCATION + " " + Constant.NOT_RELEASED);
			textPostion = textPostion + Constant.DEFAULT_SPACE_FOR_FILE;
		}
		return false;
	}
	

	/**
	 * Used to check location resource acquired or not in onResume activity lifecycle method
	 */
	@Override
	public boolean isReAcquired() {
		if (model.getMethods().containsKey("onResume")) {
			if (checkResourceAcquired(ParserProperties.getValue("Location.check.onResume"),"onResume")) {
				acquiredSb.insert(textPostion, Constant.LOCATION + " " + Constant.ACQUIRED);
				textPostion = textPostion + Constant.DEFAULT_SPACE_FOR_FILE;
				return true;
			}
			acquiredSb.insert(textPostion, Constant.LOCATION + " " + Constant.NOT_ACQUIRED);
			textPostion = textPostion + Constant.DEFAULT_SPACE_FOR_FILE;
		}
		return false;
	}

	/**
	 * This method is used to send recommended text in case if resource is not released properly
	 */
	@Override
	public String recommendedMsg() {
		return Constant.LOCATION_RECOMMENDED_TEXT.replace("###", fieldName);
	}

	/**
	 * This method is used to send text to write in text file
	 */
	@Override
	public List<String> getMsgs() {
		msgs.add(acquiredSb.toString());
		return msgs;
	}

	/**
	 * Used to check location resource released or not in onDestroy activity lifecycle method
	 */
	@Override
	public boolean isDestroyed() {
		if (model.getMethods().containsKey("onDestroy")) {
			if (checkResourceReleased(ParserProperties.getValue("Location.check.onDestroy"), "onDestroy")) {
				acquiredSb.insert(textPostion, Constant.LOCATION + " " + Constant.RELEASED);
				textPostion = textPostion + Constant.DEFAULT_SPACE_FOR_FILE;
				return true;
			}
			acquiredSb.insert(textPostion, Constant.LOCATION + " " + Constant.NOT_RELEASED);
			textPostion = textPostion + Constant.DEFAULT_SPACE_FOR_FILE;
		}
		return false;
	}

	/**
	 * Check location resource acquired or not
	 * 
	 * @param pattern - pattern to match
	 * @param lifeCycleMethod - in which activity lifecycle method need to check
	 * @return
	 */
	boolean checkResourceAcquired(String pattern,String lifeCycleMethod) {
		if (!fieldName.isEmpty()) {
			String search = fieldName + pattern;
			for (Map.Entry<String, String> entry : model.getMethods().entrySet()) {
				if (entry.getValue().contains(search)) {
					if (entry.getKey().equals(lifeCycleMethod)) {
						return true;
					} else {
						if (checkMethodCall(entry.getKey(), model.getMethods(), model.getFields(), lifeCycleMethod)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * This method is used to check the release of location is done in
	 * onPause/onDestroy or its called method
	 * 
	 * @param pattern - pattern to match
	 * @param lifeCycleMethodName - in which activity lifecycle method need to check
	 * @return
	 */
	boolean checkResourceReleased(String pattern, String lifeCycleMethod) {
		if (!fieldName.isEmpty()) {
			String search = fieldName + pattern;
			for (Map.Entry<String, String> entry : model.getMethods().entrySet()) {
				if (entry.getValue().contains(search)) {
					if (entry.getKey().equals(lifeCycleMethod)) {
						return true;
					} else {
						if (checkMethodCall(entry.getKey(), model.getMethods(), model.getFields(), lifeCycleMethod)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * This method is used to add todo comment in activity file where resource is acquired
	 */
	@Override
	public void addComment() {
		insertCommet(model, ParserProperties.getValue("Location.check.onCreate"), Constant.LOCATION_RECOMMENDED_TEXT, "onCreate");
		insertCommet(model, ParserProperties.getValue("Location.check.onResume"), Constant.LOCATION_RECOMMENDED_TEXT, "onResume");
	}
}