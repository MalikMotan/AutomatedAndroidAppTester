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

public class DriveCheck implements ResourceCheck {

	List<String> msgs = new ArrayList<String>();
	StringBuilder acquiredSb = new StringBuilder(Strings.padStart("", 150, ' '));
	int textPostion = 0;
	String fieldName = "";
	String methodName = "";
	String methodBody = "";
	SimpleClassModel model;
	String resourceAcquiredPattern = "";

	public DriveCheck(SimpleClassModel model) {
		this.model = model;
	}

	/**
	 * Used this method to check external drive resource acquired or not in Activity
	 */
	@Override
	public boolean isAcquired() {
		for (Map.Entry<String, String> entry : model.getMethods().entrySet()) {
			if (entry.getValue().contains("FileOutputStream")) {
				methodName = entry.getKey();
				methodBody = entry.getValue();
				String pattern = ParserProperties.getValue("External_Drive.check.onCreate.FileOutputStream");
				Pattern patternObj = Pattern.compile(pattern);
				Matcher matcher = patternObj.matcher(entry.getValue());
				if (matcher.find()) {
					fieldName = matcher.group(1);
					acquiredSb.insert(textPostion, Constant.EXTERNAL_DRIVE + " " + Constant.ACQUIRED);
					resourceAcquiredPattern = pattern;
					return true;
				}
				
				pattern = ParserProperties.getValue("External_Drive.check.onCreate.FileOutputStream2");
				patternObj = Pattern.compile(pattern);
				matcher = patternObj.matcher(entry.getValue());
				if (matcher.find()) {
					fieldName = matcher.group(1);
					acquiredSb.insert(textPostion, Constant.EXTERNAL_DRIVE + " " + Constant.ACQUIRED);
					resourceAcquiredPattern = pattern;
					return true;
				}
			}
			if (entry.getValue().contains("FileInputStream")) {
				methodName = entry.getKey();
				methodBody = entry.getValue();
				String pattern = ParserProperties.getValue("External_Drive.check.onCreate.FileInputStream");
				Pattern patternObj = Pattern.compile(pattern);
				Matcher matcher = patternObj.matcher(entry.getValue());
				if (matcher.find()) {
					fieldName = matcher.group(1);
					acquiredSb.insert(textPostion, Constant.EXTERNAL_DRIVE + " " + Constant.ACQUIRED);
					resourceAcquiredPattern = pattern;
					return true;
				}
			}
			if (entry.getValue().contains("openFileDescriptor")) {
				methodName = entry.getKey();
				methodBody = entry.getValue();
				String pattern = ParserProperties.getValue("External_Drive.check.onCreate.openFileDescriptor");
				Pattern patternObj = Pattern.compile(pattern);
				Matcher matcher = patternObj.matcher(entry.getValue());
				if (matcher.find()) {
					acquiredSb.insert(textPostion, Constant.EXTERNAL_DRIVE + " " + Constant.ACQUIRED + "\n"
							+ Constant.EXTERNAL_DRIVE + " " + Constant.RELEASED);
					return true;
				} else {
					pattern = "ParcelFileDescriptor(\\w*)";
					patternObj = Pattern.compile(pattern);
					matcher = patternObj.matcher(entry.getValue());
					if (matcher.find()) {
						fieldName = matcher.group(1);
						acquiredSb.insert(textPostion, Constant.EXTERNAL_DRIVE + " " + Constant.ACQUIRED);
						resourceAcquiredPattern = pattern;
						return true;
					}
				}
			}
			if (entry.getValue().contains("openInputStream")) {
				methodName = entry.getKey();
				methodBody = entry.getValue();
				String pattern = ParserProperties.getValue("External_Drive.check.onCreate.openInputStream");
				Pattern patternObj = Pattern.compile(pattern);
				Matcher matcher = patternObj.matcher(entry.getValue());
				if (matcher.find()) {
					acquiredSb.insert(textPostion, Constant.EXTERNAL_DRIVE + " " + Constant.ACQUIRED + "\n"
							+ Constant.EXTERNAL_DRIVE + " " + Constant.RELEASED);
					return true;
				} else {
					pattern = "InputStream(\\w*)";
					patternObj = Pattern.compile(pattern);
					matcher = patternObj.matcher(entry.getValue());
					if (matcher.find()) {
						fieldName = matcher.group(1);
						acquiredSb.insert(textPostion, Constant.EXTERNAL_DRIVE + " " + Constant.ACQUIRED);
						resourceAcquiredPattern = pattern;
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Used this method to check acquired resource released or not in Activity
	 */
	@Override
	public boolean isReleased() {
			if (methodBody.contains("FileOutputStream")) {
					if (! methodBody.contains(fieldName + ".close()")) {
						if (!propagationCheck(methodBody, "OutputStream")) {
							acquiredSb.append("\n" + Constant.EXTERNAL_DRIVE + " " + Constant.NOT_RELEASED);
							return false;
						}
					}
				}
			if (methodBody.contains("FileInputStream")) {
					if (!methodBody.contains(fieldName + ".close()")) {
						if (!propagationCheck(methodBody, "InputStream")) {
							acquiredSb.append("\n" + Constant.EXTERNAL_DRIVE + " " + Constant.NOT_RELEASED);
							return false;
						}
					}
			}
			if (methodBody.contains("openFileDescriptor")) {
				String pattern = ParserProperties.getValue("External_Drive.check.onCreate.openInputStream");
				Pattern patternObj = Pattern.compile(pattern);
				Matcher matcher = patternObj.matcher(methodBody);
				if (matcher.find()) {
					return true;
				} else {
					pattern = "ParcelFileDescriptor(\\w*)";
					patternObj = Pattern.compile(pattern);
					matcher = patternObj.matcher(methodBody);
					if (matcher.find()) {
						fieldName = matcher.group(1);
						if (fieldName != null) {
							if (!methodBody.contains(fieldName + ".close")) {
								acquiredSb.append("\n" +  Constant.EXTERNAL_DRIVE + " " + Constant.NOT_RELEASED);
								return false;
							}
						}
					}
				}
			}
			if (methodBody.contains("openInputStream")) {
				String pattern = ParserProperties.getValue("External_Drive.check.onCreate.openInputStream");
				Pattern patternObj = Pattern.compile(pattern);
				Matcher matcher = patternObj.matcher(methodBody);
				if (matcher.find()) {
					return true;
				} else {
					pattern = "InputStream(\\w*)";
					patternObj = Pattern.compile(pattern);
					matcher = patternObj.matcher(methodBody);
					if (matcher.find()) {
						fieldName = matcher.group(1);
						if (fieldName != null) {
							if (!methodBody.contains(fieldName + ".close")) {
								if (!propagationCheck(methodBody, "InputStream")) {
									acquiredSb.append("\n" + Constant.EXTERNAL_DRIVE + " " + Constant.NOT_RELEASED);
									return false;
								}
							}
						}
					}
				}
			}
		acquiredSb.append("\n" + Constant.EXTERNAL_DRIVE + " " + Constant.RELEASED);
		return true;
	}

	@Override
	public boolean isReAcquired() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isDestroyed() {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * This method is used to send recommended text in case if resource is not released properly
	 */
	@Override
	public String recommendedMsg() {
		String msg = Constant.EXTERNAL_DRIVE_RECOMMENDED_TEXT.replace("###", fieldName);
		return msg.replace("$$$", methodName);
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
	 * 
	 * @param body
	 * @param steam
	 * @return
	 */
	private boolean propagationCheck(String body, String stream) {
		// Check at one more level
		String pattern = "(\\w+).close(\\s*)";
		Pattern patternObj = Pattern.compile(pattern);
		Matcher matcher = patternObj.matcher(body);
		boolean check = false;
		while (matcher.find()) {
			fieldName = matcher.group(0).substring(0, matcher.group(0).indexOf("."));
			pattern = "(\\s*)(.*)(\\s+)" + fieldName + "(\\s*)=(\\s*)new(\\s+)(.*)" + stream + "(.*)";
			patternObj = Pattern.compile(pattern);
			Matcher matcher1 = patternObj.matcher(body);
			if (matcher1.find()) {
				check = true;
				break;
			}
		}
		return check;
	}

	/**
	 * This method is used to add todo comment in Activity where resource is acquired
	 */
	@Override
	public void addComment() {
		insertCommet(model, resourceAcquiredPattern, Constant.EXTERNAL_DRIVE_RECOMMENDED_TEXT, "onCreate");
	}
}