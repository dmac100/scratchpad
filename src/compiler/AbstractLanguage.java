package compiler;

import java.io.File;
import java.io.IOException;

import syntaxhighlighter.brush.Brush;
import syntaxhighlighter.brush.BrushPlain;

public class AbstractLanguage implements Language {
	private String name;
	private String extension;
	private String compiler;
	private String runtime;
	private Brush brush;
	private String template;
	
	public AbstractLanguage(String name, String extension, String compiler, String runtime, Brush brush, String template) {
		this.name = name;
		this.extension = extension;
		this.compiler = compiler;
		this.runtime = runtime;
		this.brush = brush;
		this.template = template;
	}
	
	protected AbstractLanguage() {
		name = "main";
		brush = new BrushPlain();
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String getExtension() {
		return extension;
	}
	
	@Override
	public String getFileName(String contents) {
		return "main";
	}

	@Override
	public Process createCompiler(File dir, String name) throws IOException {
		if(compiler == null) return null;
		
		return new ProcessBuilder()
			.command(compiler, name + extension)
			.directory(dir)
			.start();
	}

	@Override
	public Process runProgram(File dir, String name) throws IOException {
		return new ProcessBuilder()
			.command(runtime, name + extension)
			.directory(dir)
			.start();
	}

	@Override
	public Brush getBrush() {
		return brush;
	}
	
	@Override
	public String getTemplate() {
		return template;
	}

	@Override
	public String getDefaultInput() {
		return null;
	}
}
