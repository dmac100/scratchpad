package compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import syntaxhighlighter.brush.*;

public class MonoLanguage implements Language {
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
		return "using System;\r\r" +
			"class Prog {\r" +
			"\tpublic static void Main(string[] args) {\r" +
			"\t\tConsole.WriteLine(\"Hello World!\");\r" +
			"\t}\r" +
			"}";
	}

	@Override
	public String getFileName(String contents) {
		return "main";
	}
}
