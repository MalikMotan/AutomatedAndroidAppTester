import java.awt.EventQueue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.SwingUtilities;

import com.github.javaparser.ParseException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.CompilationUnit.Storage;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.google.common.base.Strings;
import com.parser.android.operations.FilesOperation;
import com.parser.android.ui.JavaParser;
import com.parser.android.utility.DirExplorer;
import com.parser.android.utility.NodeIterator;

public class ListClassesExample {

    
    public static void listMethodCalls(File projectDir) {
        new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
            System.out.println(path);
            System.out.println(Strings.repeat("=", path.length()));
            try {
                new VoidVisitorAdapter<Object>() {
                    @Override
                    public void visit(MethodCallExpr n, Object arg) {
                        super.visit(n, arg);
                        System.out.println(" [L " + n.getBegin().get().line + "] " + n);
                    }
                }.visit(StaticJavaParser.parse(file), null);
                System.out.println(); // empty line
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).explore(projectDir);
    }
    
    
    public static void statementsByLine(File projectDir) {
        new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
            System.out.println(path);
            System.out.println(Strings.repeat("=", path.length()));
            try {
                StaticJavaParser.parse(file).findAll(Statement.class)
                        .forEach(statement -> System.out.println(" [Lines " + statement.getBegin().get().line
                                + " - " + statement.getEnd().get().line + " ] " + statement));
                System.out.println(); // empty line
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).explore(projectDir);
    }
    
    public static void statementsByLine2(File projectDir) {
        new DirExplorer((level, path, file) -> path.endsWith(".java"), (level, path, file) -> {
            System.out.println(path);
            System.out.println(Strings.repeat("=", path.length()));
            try {
                new NodeIterator(new NodeIterator.NodeHandler() {
                   
					@Override
					public boolean handle(Node node) {
						 if (node instanceof MethodDeclaration) {
							 //	System.out.println(" Name " + ((ClassOrInterfaceDeclaration) node).getName() );
							 	System.out.println(" [Lines " + node.getBegin() + " - " + node.getEnd() + " ] " + node);
							 	System.out.println(" [Lines " + node.getBegin() + " - " + node.getEnd() + " ] " + node);
	                            return false;
	                        } else {
	                            return true;
	                        }
					}
                }).explore(StaticJavaParser.parse(file));
                System.out.println(); // empty line
            } catch (IOException e) {
                new RuntimeException(e);
            }
        }).explore(projectDir);
    }
    
    /**
	 * Method used to insert comments when resource is not released
	 * @param file
	 */
	public static void insertCommet(File file, String pattern, String recommendation) throws ParseException
	{
		Pattern patternObj = Pattern.compile(pattern);
		
		try {
			CompilationUnit cu  = StaticJavaParser.parse(file);
        	new VoidVisitorAdapter<Object>() {
        		public void visit(ClassOrInterfaceDeclaration n, Object arg) {
                    super.visit(n, null);
                    NodeList<BodyDeclaration<?>> members = n.getMembers();
                    Matcher matcher = null;
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
                					if (matcher.find()) {
                						stmt.setLineComment("TODO: " + recommendation + "\n");
                						// Check method name is LifeCycle or not onCreate / onResume
                                		if(member.getComment().isPresent())
                                		{
                                			if (member.getComment().get().isLineComment())
                                			{
                                				Comment comment = member.getComment().get();
                        						comment.setContent(comment.getContent() + " \r\n " + "TODO: " + recommendation + "\r\n");
                        						method.setComment(comment);
                                			}
                                			else
                                				member.setComment(new LineComment("TODO: " + recommendation + "\n")); 
                                		}
                                		else
                                			member.setComment(new LineComment("TODO: " + recommendation + "\n"));
                            		}	
                				}
                				method.setBody(new BlockStmt(stmts));
                			}
                    		
                    	}
                    }
                    cu.getStorage().get().save();
                }
            }.visit(cu, null);
            
		 }
		 catch(Exception e)
		 {
			 
		 }
	}


    public static void main(String[] args) {
    	EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JavaParser frame = new JavaParser();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		/*
		 * File file = new File(
		 * "D:\\android-project\\app\\src\\main\\java\\com\\example\\javaapplication\\CameraActivity.java"
		 * ); String method = "onCreate"; String pattern = "(\\s*)Camera.open(.*)"; try
		 * { insertCommet(file, pattern, "Hello"); } catch (Exception e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }
		 */    }
}