package compiler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import syntaxhighlighter.brush.Brush;
import util.StringUtil;

public class Language {
	private final Map<String, File> dependencyDirs = new HashMap<>();
	
	private String name;
	private String extension;
	private Brush brush;
	private String compiler;
	private String run;
	private String filenameMatcher;
	private String template;
	private String defaultInput;
	private String standardImportJar;
	private String initCommand;
	private String depCommand;
	private String defaultClasspath;

	/**
	 * Creates a new language from the paramters. Compiler, run, filenameMatcher, defaultInput, and standardImportJar are optional.
	 * Commandlines are split by spaces and then variable substitutions are performed on $NAME, $EXT, and $CLASSPATH.
	 * @param name the name of the programming language.
	 * @param extension the file extension used for this language.
	 * @param brush the name of the brush to use for syntax highlighting such as 'BrushPlain'. Must exist in the JavaSyntaxHighlighter jar.
	 * @param compiler the commandline to compile the program, or null if no compilation is necessary.
	 * @param run the commandline to run the program, or null if the program is run by executing it directly.
	 * @param filenameMatcher the pattern to use to detect the filename from the file contents, or null for a default name.
	 * @param template the initial contents for the source code template.
	 * @param defaultInput the initial contents for the program input, or null if there is no default input.
	 * @param standardImportJar the jar that contains the libraries for the standard imports.
	 * @param initCommand the command to run before others to initialize a project.
	 * @param depCommand the command used to download dependencies.
	 * @param defaultClasspath the classpath to use if it is not specified elsewhere.
	 */
	public Language(String name, String extension, Brush brush, String compiler, String run,
			String filenameMatcher, String template, String defaultInput, String standardImportJar, String initCommand, String depCommand, String defaultClasspath) {
		
		this.name = name;
		this.extension = extension;
		this.brush = brush;
		this.compiler = compiler;
		this.run = run;
		this.filenameMatcher = filenameMatcher;
		this.template = template;
		this.defaultInput = defaultInput;
		this.standardImportJar = standardImportJar;
		this.initCommand = initCommand;
		this.depCommand = depCommand;
		this.defaultClasspath = defaultClasspath;
	}
	
	/**
	 * Returns a list of callable processes for any compilers that are needed.
	 * @param dir the directory to run in.
	 * @param name the name of the file excluding the extension.
	 * @param classpath the Java classpath.
	 */
	public List<Callable<Process>> createCompilers(File dir, String name, String contents, String classpath) throws IOException {
		List<Callable<Process>> compilers = new ArrayList<>();

		if(initCommand != null) {
			compilers.add(createProcess(dir, name, initCommand, classpath));
		}
		
		if(depCommand != null) {
			// Download any dependencies marked in the source code as "// DEP: ..."
			for(String line:contents.split("\n")) {
				Matcher matcher = Pattern.compile("// DEP: (.*)").matcher(line);
				if(matcher.find()) {
					String dep = matcher.group(1);
					compilers.addAll(createDepCommand(dir, dep));
				}
			}
		}
		
		if(compiler != null) {
			compilers.add(createProcess(dir, name, compiler, classpath));
		}
		
		return compilers;
	}

	/**
	 * Creates callable processes to copy a dependency dep into the directory dir.
	 */
	private List<Callable<Process>> createDepCommand(File dir, String dep) throws IOException {
		List<Callable<Process>> processes = new ArrayList<>();
		
		File dependencyDir = dependencyDirs.get(dep);
		
		if(dependencyDir == null) {
			// Create directory to store dependency.
			File newDependencyDir = Files.createTempDirectory("dependency-" + dep.replaceAll("[ .]", "_").replaceAll("\\W", "") + "-").toFile();
			dependencyDir = newDependencyDir;

			// Create process to download dependency.
			processes.add(createCallable(new ProcessBuilder()
				.directory(dependencyDir)
				.command(Arrays.asList((depCommand + " " + dep).split(" +")))));
			
			processes.add(() -> {
				dependencyDirs.put(dep, newDependencyDir);
				return new NullProcess();
			});
		}
		
		// Create process to copy dependency to dir.
		processes.add(createCopyProcess(dependencyDir, dir));
		
		return processes;
	}

	/**
	 * Returns a callable process to run a file in this language.
	 * @param dir the directory to run in.
	 * @param name the name of the file excluding the extension.
	 * @param classpath the Java classpath.
	 */
	public Callable<Process> runProgram(File dir, String name, String contents, String classpath) throws IOException {
		if(run == null) {
			return createCallable(new ProcessBuilder()
				.directory(dir)
				.command(new File(dir, "main").getPath()));
		} else {
			return createProcess(dir, name, run, classpath);
		}
	}

	/**
	 * Returns a builder for a new process.
	 * @param dir the directory to run in.
	 * @param name the name of the file excluding the extension.
	 * @param line the commandline to execute. The line is separated by spaces and then
	 *             variable substitutions are performed on $NAME, $EXT, and $CLASSPATH.
	 * @param classpath the Java classpath.
	 */
	private Callable<Process> createProcess(File dir, String name, String line, String classpath) throws IOException {
		String[] args = line.split(" ");
		for(int i = 0; i < args.length; i++) {
			args[i] = args[i]
				.replaceAll("\\$CLASSPATH", classpath)
				.replaceAll("\\$NAME", name)
				.replaceAll("\\$EXT", extension);
		}
		
		return createCallable(new ProcessBuilder()
			.directory(dir)
			.command(args));
	}
	
	/**
	 * Returns the name of the language.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the file extension of the language.
	 */
	public String getExtension() {
		return extension;
	}

	/**
	 * Returns the template to use as the default source code for the language.
	 */
	public String getTemplate() {
		return template;
	}
	
	/**
	 * Returns the default input to use, or null if there is no default input. 
	 */
	public String getDefaultInput() {
		return defaultInput;
	}

	/**
	 * Returns the brush to use for syntax highlighting.
	 */
	public Brush getBrush() {
		return brush;
	}
	
	/**
	 * Returns the path to the standard import jar, or null if there isn't any.
	 */
	public String getStandardImportJar() {
		return standardImportJar;
	}
	
	/**
	 * Returns the command used to initialize the project, or null if there isn't any.
	 */
	public String getInitCommand() {
		return initCommand;
	}
	
	/**
	 * Returns the command used to download dependencies.
	 */
	public String getDepCommand() {
		return depCommand;
	}

	/**
	 * Returns the default classpath.
	 */
	public String getDefaultClasspath() {
		return defaultClasspath;
	}

	/**
	 * Returns the detected filename, excluding extension, from the file contents
	 * or a default if it can't be detected.
	 */
	public String getFileName(String contents) {
		if(filenameMatcher == null) {
			return "Main";
		} else {
			String match = StringUtil.match(contents, filenameMatcher);
			return (match == null) ? "Main" : match;
		}
	}

	/**
	 * Creates a callable process that copies files from source to destination.
	 */
	private static Callable<Process> createCopyProcess(File source, File destination) {
		return () -> {
			FileUtils.copyDirectory(source, destination);
			return new NullProcess();
		};
	}
	
	/**
	 * Creates a callable process from a process builder.
	 */
	private static Callable<Process> createCallable(ProcessBuilder processBuilder) {
		return () -> processBuilder.start();
	}
}