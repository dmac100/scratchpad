package compiler;

import java.io.*;
import java.nio.file.Files;
import java.util.concurrent.*;

import org.apache.commons.io.FileUtils;

import ui.Callback;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

public class Compiler {
	private static ExecutorService executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).build());
	
	private final Language language;
	private final String jarDir;
	private final String classpath;
	
	private static class StreamReader implements Runnable {
		private BufferedReader reader;
		private Appender appender;
		private Appender info;
		private String name;
		
		public StreamReader(String name, InputStream inputStream, Appender appender, Appender info) {
			try {
				this.name = name;
				this.reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
				this.appender = appender;
				this.info = info;
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException("Unsupported encoding", e);
			}
		}
		
		public void run() {
			try {
				String line;
				while((line = reader.readLine()) != null) {
					appender.append(line + "\n");
				}
			} catch(IOException e) {
				info.append("ERROR: IOException reading stream: " + name + "\n");
				e.printStackTrace();
			}
		}
	}
	
	public Compiler(Language language, String jarDir, String classpath) {
		this.language = language;
		this.jarDir = jarDir;
		this.classpath = classpath;
	}

	/**
	 * Compiles and runs a source file. Displays output to out,
	 * and compilation and program errors to err.
	 * 
	 * @param contents The source code as a String.
	 * @param input The input to use as the standard input stream. 
	 * @param name The name of the Java class.
	 * @param out Appender to send output to.
	 * @param err Appender to send errors to.
	 * @param callback The callback to call when the run is finished.
	 */
	public Future<?> runFile(final String contents, final String input, final Appender out, final Appender err, final Appender info, final Callback<Void> finishedCallback) {
		return executor.submit(new Callable<Void>() {
			public Void call() throws Exception {
				runFileSync(contents, input, out, err, info, finishedCallback);
				return null;
			}
		});
	}
	
	private void runFileSync(String contents, String input, Appender out, Appender err, Appender info, Callback<Void> finishedCallback) throws InterruptedException {
		File dir = null;
		Process compilerProcess = null;
		Process runProcess = null;
		
		try {
			String name = language.getFileName(contents);
			
			dir = Files.createTempDirectory("scratchpad").toFile();
			File source = new File(dir, name + "." + language.getExtension());
			
			FileUtils.write(source, contents, "UTF-8");
			
			compilerProcess = language.createCompiler(dir, name, getClasspath());
			if(compilerProcess != null) {
				executor.submit(new StreamReader("Compiler Output", compilerProcess.getInputStream(), out, info));
				executor.submit(new StreamReader("Compiler Error", compilerProcess.getErrorStream(), err, info));
				int result = compilerProcess.waitFor();
				compilerProcess = null;
				if(result != 0) {
					return;
				}
			}
			
			runProcess = language.runProgram(dir, name, getClasspath());
			
			executor.submit(new StreamReader("Output", runProcess.getInputStream(), out, info));
			executor.submit(new StreamReader("Error", runProcess.getErrorStream(), err, info));

			try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(runProcess.getOutputStream(), "UTF-8"))) {
				writer.append(input);
			} catch(UnsupportedEncodingException e) {
				throw new RuntimeException("Unsupported encoding", e);
			}
			
			runProcess.waitFor();
			runProcess = null;
		} catch(IOException e) {
			info.append("ERROR: IOException running program: " + e.getMessage() + "\n");
			e.printStackTrace();
		} finally {
			if(compilerProcess != null) {
				compilerProcess.destroy();
			}
			
			if(runProcess != null) {
				runProcess.destroy();
			}
			
			if(dir != null) {
				try {
					FileUtils.deleteDirectory(dir);
				} catch (IOException e) {
				}
			}
			
			finishedCallback.onCallback(null);
		}
	}
	
	private String getClasspath() {
		if(classpath != null) {
			return classpath;
		} else if(jarDir != null) {
			return getClasspath(jarDir);
		} else {
			return ".";
		}
	}
	
	/**
	 * Returns the classpath containing all the jar files in jarDir.
	 */
	private static String getClasspath(String jarDir) {
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
}
