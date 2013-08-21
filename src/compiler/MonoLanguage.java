package compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import syntaxhighlighter.brush.*;

public class MonoLanguage extends AbstractLanguage {
	@Override
	public String getName() {
		return "Mono";
	}
	
	@Override
	public String getExtension() {
		return ".cs";
	}
	
	@Override
	public Brush getBrush() {
		return new BrushCSharp();
	}
	
	@Override
	public Process createCompiler(File dir, String name) throws IOException {
		return new ProcessBuilder()
			.command("gmcs", "-out:main", name + getExtension())
			.directory(dir)
			.start();
	}
	
	@Override
	public Process runProgram(File dir, String name) throws IOException {
		return new ProcessBuilder()
			.directory(dir)
			.command("mono", "main")
			.start();
	}

	@Override
	public String getTemplate() {
		return "using System;\n\n" +
			"class Prog {\n" +
			"\tpublic static void Main(string[] args) {\n" +
			"\t\tConsole.WriteLine(\"Hello World!\");\n" +
			"\t}\n" +
			"}";
	}

	@Override
	public String getFileName(String contents) {
		return "main";
	}
}
