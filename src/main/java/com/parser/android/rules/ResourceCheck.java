package com.parser.android.rules;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.javaparser.ParseException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.parser.android.model.SimpleClassModel;

/**
 * 
 * @author @MalikMotan
 * This interface is to be used by all such classes which need to perform the resource check. 
 * For example CameraCheck, CameraApi2Check, DriveCheck and LocationCheck etc. implement this interface.
 * 
 */
public interface ResourceCheck {
	
	public boolean isAcquired();
	
	public boolean isReAcquired();
	
	public boolean isReleased();
	
	public boolean isDestroyed();
	
	public void addComment();
	
	public String recommendedMsg();
	
	public List<String> getMsgs();
	
	/**
	 * This method is used to check resource usage iteratively
	 * it checks the usage in instance variable as well as method and find out the final call from life cycle method
	 * @param value
	 * @param methods
	 * @param checkMethod
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public default boolean checkMethodCall(String value, Map<String, String> methods, List<String> fields, String checkMethod) {
		methods = (Map<String, String>) ((HashMap<String, String>)methods).clone();
		if (!value.equals("")) {
			if(methods.containsKey(checkMethod)) 
			{
				if(methods.get(checkMethod).contains(value)) 
					return true;
				else
				{
					String valuec = value;
					methods.remove(value);
					if (methods.values().stream().anyMatch(e -> e.contains(valuec)))
					{
						for (Map.Entry<String, String> entry : methods.entrySet()) {
							if (entry.getValue().toString().contains(value)) {
								String methodName = entry.getKey();
								if (methodName.equalsIgnoreCase(checkMethod))
									return true;
								else if(methodName.equalsIgnoreCase(value))
									return false;
								else 
									return checkMethodCall(methodName, methods, fields, checkMethod);
							}
						}
					}
					else
					{
						String pattern = "(\\s*)(\\w+)Listener(\\s+)(\\w+)(\\s*)=";
						Pattern patternObj = Pattern.compile(pattern);
						Matcher matcher = null;
						for (String field : fields) {
							matcher = patternObj.matcher(field);
							if (matcher.find()) {
								value = matcher.group(4);
								for (Map.Entry<String, String> entry : methods.entrySet()) {
									if (entry.getValue().toString().contains(value)) {
										String methodName = entry.getKey();
										if (methodName.equalsIgnoreCase(checkMethod))
											return true;
										else if(methodName.equalsIgnoreCase(value))
											return false;
										else 
											return checkMethodCall(methodName, methods, fields, checkMethod);
									}
								}
							}
						}
					}
				}
			} 
			else
				return false;

			
		}
		return false;
	}
	
	
	/**
	 * This method is used to insert comment inside the activity file if resource is not released as per specification
	 * @param file
	 * @param methodName
	 * @param pattern
	 * @param recommendation
	 * @throws ParseException
	 */
	public default void insertCommet(SimpleClassModel model, String pattern, String recommendation, String cycleMethod)
	{
		Pattern patternObj = Pattern.compile(pattern);
		File file = new File(model.getFilePath());
		try {
			CompilationUnit cu  = StaticJavaParser.parse(file);
        	new VoidVisitorAdapter<Object>() {
        		public void visit(ClassOrInterfaceDeclaration n, Object arg) {
                    super.visit(n, null);
                    NodeList<BodyDeclaration<?>> members = n.getMembers();
                    Matcher matcher = null;
                    boolean exist = false;
                    String methodName = "";
                    for (BodyDeclaration<?> member: members)
                    {
                    	if(member.isMethodDeclaration())
                    	{
                    		MethodDeclaration method = member.asMethodDeclaration();
                			if(method.getBody().isPresent())
                			{
                				NodeList<Statement> stmts = method.getBody().get().getStatements();
                				for (Statement stmt : stmts)
                				{
                					matcher = patternObj.matcher(stmt.toString());
                					if (matcher.find())
                					{
                						exist = true;
                						methodName = method.getName().getIdentifier();
                						stmt.setLineComment("TODO: " + recommendation + System.lineSeparator() );
                					}
                				}
                				method.setBody(new BlockStmt(stmts));
                			}
                			if(exist) break;
                    	}	
                    }
                    if (exist)
                	{
                		if(checkMethodCall(methodName, model.getMethods(), model.getFields(), cycleMethod))
                		{
                			for (BodyDeclaration<?> member: members)
                            {
                            	if(member.isMethodDeclaration())
                            	{
                            		MethodDeclaration method = member.asMethodDeclaration();
                            		if(method.getName().getIdentifier().equals(cycleMethod))
                            		{
                            			if(method.getComment().isPresent())
                                		{
                                			if (method.getComment().get().isLineComment() || method.getComment().get().isBlockComment())
                                			{
                                				Comment comment = member.getComment().get();
                                				String content = comment.getContent().replace("//","");
                                				if(!content.contains(recommendation))
                                					method.setBlockComment(System.lineSeparator() + "\t" + content + System.lineSeparator() + "\t" + "TODO: " + recommendation + System.lineSeparator() + "\t");
                                			}
                                			else
                                				method.setComment(new LineComment("TODO: " + recommendation)); 
                                		}
                                		else
                                			method.setComment(new LineComment("TODO: " + recommendation));
                            		}
                            	}
                            }
                		}
                	}
                    cu.getStorage().get().save();
                }
            }.visit(cu, null);
		 }
		 catch(Exception e)
		 {
			e.printStackTrace();
		 }
	}
}