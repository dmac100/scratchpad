package compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import syntaxhighlighter.brush.*;

public class ScalaLanguage extends AbstractLanguage {
	private String jarDir;
	
	public ScalaLanguage() {
	}
	
	public ScalaLanguage(String jarDir) {
		this.jarDir = jarDir;
	}
	
	@Override
	public String getName() {
		return "Scala";
	}
	
	@Override
	public String getExtension() {
		return ".scala";
	}
	
	@Override
	public Brush getBrush() {
		return new BrushScala();
	}
	
	@Override
	public Process createCompiler(File dir, String name) throws IOException {
		return new ProcessBuilder()
			.command("scalac", "-cp", getClassPath(), name + ".scala")
			.directory(dir)
			.start();
	}
	
	@Override
	public Process runProgram(File dir, String name) throws IOException {
		return new ProcessBuilder()
			.directory(dir)
			.command("scala", "-cp", getClassPath(), name)
			.start();
	}

	/**
	 * Returns the classpath containing all the jar files in jarDir.
	 */
	private String getClassPath() {
		if(jarDir == null) return ".";
		
		StringBuilder classpath = new StringBuilder(".");
		
		String[] files = new File(jarDir).list();
		
		if(files != null) {
			for(String file:files) {
				if(file.toLowerCase().endsWith(".jar")) {
					classpath.append(File.pathSeparator);
					classpath.append(new File(jarDir, file));
				}
			}
		}
		
		return classpath.toString();
	}
	
	@Override
	public String getTemplate() {
		return "object Main {\n" +
			"\tdef main(args:Array[String]) {\n" +
			"\t\tprintln(\"Hello World!\");\n" +
			"\t}\n" +
			"}";
	}

	@Override
	public String getFileName(String contents) {
		// Look for the name of the main class with simple string matching. Should cover the basic cases.
		Matcher match = match(contents, "(?s)object +([\\w\\d]+)((?!class).)*def +main *\\(");
		if(match != null) {
			return match.group(1);
		}
		return "Main";
	}
	
	private static Matcher match(String contents, String pattern) {
		Matcher matcher = Pattern.compile(pattern).matcher(contents);
		if(matcher.find()) {
			return matcher;
		} else {
			return null;
		}
	}
}
