package com.parser.android.operations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.base.Strings;
import com.parser.android.constants.Constant;
import com.parser.android.constants.Constant.Method;
import com.parser.android.constants.ParserProperties;
import com.parser.android.model.SimpleClassModel;
import com.parser.android.rules.ResourceCheck;
import com.parser.android.rules.ResourceCheckFactory;
import com.parser.android.ui.JavaParser;
import com.parser.android.utility.DirExplorer;
import com.parser.android.writer.LogWriter;

public class FilesOperation{

	Logger logger = Logger.getLogger(FilesOperation.class.getName()); 
	
	private File dirPath;
	
	public FilesOperation(File dirPath) {
		this.dirPath = dirPath;
	}

	
	/**
	 * Method used to get list of Activity Files used in the project. 
	 * Activity files which extends AppCompatActivity or Activity or Fragment are considered. 
	 * @return
	 */
	public List<File> getActivityFiles() {
		logger.log(Level.INFO, "Enter to getActivityFiles");
		List<File> list = new ArrayList<File>();
		String activities = ParserProperties.getValue("activities");
        String[] activitiesList = activities.split(",");
		new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
        	logger.log(Level.INFO,path);
        	logger.log(Level.INFO,(Strings.repeat("=", path.length())));
            try {
                new VoidVisitorAdapter<List<File>>() {
                    @Override
                    public void visit(ClassOrInterfaceDeclaration n, List<File> list) {
                        super.visit(n, list);
                        if(n.getExtendedTypes().size() > 0)
                        {
                        	for (String activity : activitiesList)
    	                    {
    	                    	if(n.getExtendedTypes().get(0).getName().getIdentifier().equals(activity))
    	                    	{
    	                    		list.add(file);
    	                    		break;
    	                    	}
    	                    }
                        }
                    }
                }.visit(StaticJavaParser.parse(file), list);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).explore(dirPath);
        logger.log(Level.INFO, "Exit from getActivityFiles :" +  list.size());
        return list;
    }
	
	
	/**
	 * This method is used to take out all fields and methods used in an Activity file. 
	 * This information is stored in SimpleClassModel to process in next step
	 * @param files
	 * @return
	 */
	public List<SimpleClassModel> getActivityDetails(List<File> files)
	{
		logger.log(Level.INFO, "Enter to getActivityDetails : " + files.size());
		List<SimpleClassModel> details = new ArrayList<SimpleClassModel>();
		for(File file:files){  
			logger.log(Level.INFO,file.getPath());
			SimpleClassModel model = new SimpleClassModel();
	        try {
	        	new VoidVisitorAdapter<SimpleClassModel>() { 
	        		public void visit(ClassOrInterfaceDeclaration n, SimpleClassModel model) {
	                    super.visit(n, model);
	                    NodeList<BodyDeclaration<?>> members = n.getMembers();
	                    for (BodyDeclaration<?> member: members)
                		{
	                    	if(member.isFieldDeclaration())
	                    	{
	                    		model.getFields().add(member.asFieldDeclaration().toString());
	                    	}
	                    	else if(member.isMethodDeclaration())
	                    	{
	                    		MethodDeclaration method = member.asMethodDeclaration();
	                    		if(method.getBody().isPresent())
	                    			model.getMethods().put(method.getName().getIdentifier(), remove(method.getBody().get().toString()));
	                    	}
                		}
	                    
	                    String nodeDetails = n.toString();
	                    //Get list of resources from property file
	                    String resources = ParserProperties.getValue("resources");
	                    String[] resourceList = resources.split(",");
	                    String check = "";
	                    for (String resource : resourceList)
	                    {
	                    	check = ParserProperties.getValue(resource.trim()+".check");
	                    	if(nodeDetails.contains(check))
	 	                    	model.getResources().add(resource);
	                    }
	                   	logger.log(Level.FINEST, "field @ " + n.getRange().get());
	                }
	            }.visit(StaticJavaParser.parse(file), model);
	           
	        } catch (IOException e) {
	            throw new RuntimeException(e);
	        }
	        model.setFilePath(file.getPath());
	        details.add(model);
	        logger.log(Level.INFO,"Exit from to getActivityDetails : "+ details.size());
	    }
		return details;  
	}
	

	/**
	 * This methods is used to perform Resource acquire/release check on each model created on selected Activity file
	 * @param models
	 */
	public void performResourceCheck(List<SimpleClassModel> models)
	{
		logger.log(Level.INFO, "Enter to performResourceCheck : " + models.size());
		ResourceCheckFactory factory = null;
		ResourceCheck check = null;
		String fileName = null;
		int noOfFilesProcessed = 0;
		List<String> msgs;
		for(SimpleClassModel model : models) {  
			logger.log(Level.INFO, "Resource used :" + model.getResources().size());
			msgs = new ArrayList<>();
			factory = new ResourceCheckFactory();
			fileName = model.getFilePath().substring(model.getFilePath().lastIndexOf(System.getProperty("file.separator"))+1);
			StringBuilder methodTransition = new StringBuilder();
			for (Method m : Constant.Method.values())
			{	
				if(model.getMethods().containsKey(m.toString()))
				{
					if(methodTransition.length() > 0)
					{
						methodTransition.append(Strings.padStart("".toString(),15,' '));
						methodTransition.append(" ---> ");
						methodTransition.append(Strings.padEnd("",15,' '));
					}
	            	methodTransition.append(m+"()");
				}     	
		    }
			msgs.add(methodTransition.toString());
			int sizeCheck = msgs.size();
			for(String resource:model.getResources()) { 
				check = factory.getResourceCheckRequired(resource.trim(), model);
				boolean success = false;
				if(check.isAcquired()) {
					success = true;
					check.isReAcquired();
					if(check.isReleased()) {
						success = true;
					} else {
						if(check.isDestroyed()) {
							success = true;
						} else {
							success = false;
							check.addComment();
						}
					}
					//Call to show result in application window
					JavaParser.showResult(fileName, success, resource, check.recommendedMsg());
					msgs.addAll(check.getMsgs());
				} else if(check.isReAcquired()) {
					success = true;
					if(check.isReleased()) {
						success = true;
					} else {
						if(check.isDestroyed()) {
							success = true;
						} else {
							success = false;
							check.addComment();
						}
					}
					//Call to show result in application window
					JavaParser.showResult(fileName, success, resource, check.recommendedMsg());
					msgs.addAll(check.getMsgs());
				}
			}
			//Generate file for each activity
			if(msgs.size() > sizeCheck)
				LogWriter.getInstance().log(JavaParser.dirPath, fileName.substring(0,fileName.lastIndexOf(".")), msgs);
			JavaParser.fill(++noOfFilesProcessed, models.size());
		}
		logger.log(Level.INFO,"Exit from to performResourceCheck");
	}
	

	/**
	 * Utility method to remove comments inside the Activity class methods
	 */
	private String remove(String body)
	{
		String pattern1 = "/\\*(\\*)?(((?!\\*/)[\\s\\S])+)?\\*/";
		String pattern2 = "//.*";
		return body.replaceAll(pattern1, " ").replaceAll(pattern2, " ");
	}	
}