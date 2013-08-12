package compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import syntaxhighlighter.brush.Brush;
import syntaxhighlighter.brush.BrushJava;

public class JavaLanguage implements Language {
	private String jarDir;
	
	public JavaLanguage() {
	}
	
	public JavaLanguage(String jarDir) {
		this.jarDir = jarDir;
	}
	
	@Override
	public String getName() {
		return "Java";
	}
	
	@Override
	public String getExtension() {
		return ".java";
	}
	
	@Override
	public Brush getBrush() {
		return new BrushJava();
	}
	
	@Override
	public Process createCompiler(File dir, String name) throws IOException {
		String javac = getJDKExecutablePath("javac");
		
		return new ProcessBuilder()
			.command(javac, "-cp", getClassPath(), name + ".java")
			.directory(dir)
			.start();
	}
	
	@Override
	public Process runProgram(File dir, String name) throws IOException {
		String java = getJDKExecutablePath("java");
		
		return new ProcessBuilder()
			.directory(dir)
			.command(java, "-cp", getClassPath(), name)
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
	
	/**
	 * Returns the path to an executable in the jdk directory, or the executableName itself if it's
	 * not found. Method adapted from the BlueJ project.
	 */
	private static String getJDKExecutablePath(String executableName) {
		// look for it in the JDK bin directory
		String jdkPathName = System.getProperty("java.home");

		if (jdkPathName != null) {
			// first check the closest bin directory
			File jdkPath = new File(jdkPathName);
			File binPath = new File(jdkPath, "bin");

			// try to find normal (unix??) executable
			File potentialExe = new File(binPath, executableName);
			if (potentialExe.exists())
				return potentialExe.getAbsolutePath();

			// try to find windows executable
			potentialExe = new File(binPath, executableName + ".exe");
			if (potentialExe.exists())
				return potentialExe.getAbsolutePath();

			// we could be in a JRE directory INSIDE a JDK directory
			// so lets go up one level and try again
			jdkPath = jdkPath.getParentFile();
			if (jdkPath != null) {
				binPath = new File(jdkPath, "bin");

				// try to find normal (unix??) executable
				potentialExe = new File(binPath, executableName);
				if (potentialExe.exists())
					return potentialExe.getAbsolutePath();
				// try to find windows executable
				potentialExe = new File(binPath, executableName + ".exe");
				if (potentialExe.exists())
					return potentialExe.getAbsolutePath();
			}
		}

		return executableName;
	}

	@Override
	public String getTemplate() {
		return "public class Main {\r" +
			"\tpublic static void main(String[] args) throws Exception {\r" +
			"\t\tSystem.out.println(\"Hello World!\");\r" +
			"\t}\r" +
			"}";
	}

	@Override
	public String getFileName(String contents) {
		// Look for the name of the main class with simple string matching. Should cover the basic cases.
		Matcher match = match(contents, "(?s)public +class +([\\w\\d]+)((?!class).)*public +static +void +main *\\(");
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
