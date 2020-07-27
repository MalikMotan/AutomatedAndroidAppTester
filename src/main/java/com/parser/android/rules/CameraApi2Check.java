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

public class CameraApi2Check implements ResourceCheck {

	SimpleClassModel model;
	String fieldName = "";
	String methodName = "";
	List<String> msgs = new ArrayList<String>();
	StringBuilder acquiredSb = new StringBuilder(Strings.padStart("",150,' '));
	int textPostion = 0;
	
	public CameraApi2Check(SimpleClassModel model) {
		this.model = model;
	}

	/**
	 * Used to check camera resource acquired in onCreate or not in activity lifecycle method
	 *  
	 */
	@Override
	public boolean isAcquired() {
		acquiredSb.setLength(100);
		if(model.getMethods().containsKey("onCreate")) {
			if(checkResourceAcquired(ParserProperties.getValue("Camera2.check.onCreate"),"onCreate")) {
				acquiredSb.insert(textPostion, Constant.CAMERA + " " + Constant.ACQUIRED);
				textPostion = textPostion + Constant.DEFAULT_SPACE_FOR_FILE;
				return true;
			} else {
				acquiredSb.insert(textPostion, Constant.CAMERA + " " + Constant.NOT_ACQUIRED );
				textPostion = textPostion + Constant.DEFAULT_SPACE_FOR_FILE;
			}
		}
		return false;
	}

	/**
	 * Used to check a camera resource released or not in onPause activity lifecycle  method
	 */
	@Override
	public boolean isReleased() {
		if(model.getMethods().containsKey("onPause")) {
			if(checkResourceReleased(ParserProperties.getValue("Camera2.check.onPause"),"onPause")) {
				acquiredSb.insert(textPostion, Constant.CAMERA + " " + Constant.RELEASED);
				textPostion = textPostion + Constant.DEFAULT_SPACE_FOR_FILE;
				return true;
			} else {
				acquiredSb.insert(textPostion, Constant.CAMERA + " " + Constant.NOT_RELEASED);
				textPostion = textPostion + Constant.DEFAULT_SPACE_FOR_FILE;
			}
		}
		return false;
	}

	/**
	 * Used to check camera resource acquired or not in onResume activity lifecycle method 
	 */
	@Override
	public boolean isReAcquired() {
		if(model.getMethods().containsKey("onResume")) {
			if(checkResourceAcquired(ParserProperties.getValue("Camera2.check.onResume"), "onResume")) {
				acquiredSb.insert(textPostion, Constant.CAMERA + " " + Constant.ACQUIRED);
				textPostion = textPostion + Constant.DEFAULT_SPACE_FOR_FILE;
				return true;
			} else {
				acquiredSb.insert(textPostion, Constant.CAMERA + " " + Constant.NOT_ACQUIRED);
				textPostion = textPostion + Constant.DEFAULT_SPACE_FOR_FILE;
			}
		}
		return false;
	}

	/**
	 * Used to check camera resource is released or not in onDestroy activity lifecycle method
	 */
	@Override
	public boolean isDestroyed() {
		if(model.getMethods().containsKey("onDestroy")) {
			if(checkResourceReleased(ParserProperties.getValue("Camera2.check.onDestroy"),"onDestroy")) {
				acquiredSb.insert(textPostion, Constant.CAMERA + " " + Constant.RELEASED);
				return true;
			} else {
				acquiredSb.insert(textPostion, Constant.CAMERA + " " + Constant.NOT_RELEASED);
			}
		}
		return false;
	}
	
	/**
	 * This method is used to send recommended text in case if resource is not released properly
	 */
	@Override
	public String recommendedMsg() {
		return Constant.CAMERA2_RECOMMENDED_TEXT.replace("###", fieldName);
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
	 * Check camera resource acquired or not
	 * @param pattern - pattern to match
	 * @param lifeCycleMethod - in which activity lifecycle method need to check
	 * @return
	 */
	boolean checkResourceAcquired(String pattern, String lifeCycleMethod) {
		for (Map.Entry<String, String> entry : model.getMethods().entrySet()) {
			Pattern patternObj = Pattern.compile(pattern);
			Matcher matcher = patternObj.matcher(entry.getValue());
			if (matcher.find()) {
				methodName = entry.getKey();
				fieldName = matcher.group(2);
				if(!fieldName.isEmpty() && !methodName.isEmpty()) {
					if(model.getMethods().get(methodName).contains(fieldName + ".openCamera(")) {
						if (methodName.equals(lifeCycleMethod)) {
							return true;
						} else {
							if (checkMethodCall(methodName, model.getMethods(), model.getFields(), lifeCycleMethod)) {
								return true;
							} 
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Check camera resource released or not
	 * @param pattern - pattern to match
	 * @param lifeCycleMethod - in which activity lifecycle method need to check
	 * @return
	 */
	boolean checkResourceReleased(String pattern,String lifeCycleMethod) {
		for(String field : model.getFields()) {
			String getCameraDeviceObj = "CameraDevice(\\s+)(\\w*)"; 
			Pattern patternObj = Pattern.compile(getCameraDeviceObj);
			Matcher matcher = patternObj.matcher(field);
			if (matcher.find()) {
				fieldName = matcher.group(2).trim();
				break;
			}
		}
		if (checkMethodCall(fieldName + pattern, model.getMethods(), model.getFields(), lifeCycleMethod)) {
			return true;
		}
		return false;
	}

	/**
	 * This method is used to add todo comment in Activity where resource is acquired
	 */
	@Override
	public void addComment() {
		insertCommet(model, ParserProperties.getValue("Camera2.check.onCreate"), Constant.CAMERA2_RECOMMENDED_TEXT, "onCreate");
		insertCommet(model, ParserProperties.getValue("Camera2.check.onResume"), Constant.CAMERA2_RECOMMENDED_TEXT, "onResume");
	}
}