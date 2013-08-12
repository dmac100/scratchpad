package compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import syntaxhighlighter.brush.*;

public class PascalLanguage implements Language {
	@Override
	public String getName() {
		return "Pascal";
	}
	
	@Override
	public String getExtension() {
		return ".ps";
	}
	
	@Override
	public Brush getBrush() {
		return new BrushDelphi();
	}
	
	@Override
	public Process createCompiler(File dir, String name) throws IOException {
		return new ProcessBuilder()
			.command("fpc", "-omain", name + getExtension())
			.directory(dir)
			.start();
	}
	
	@Override
	public Process runProgram(File dir, String name) throws IOException {
		return new ProcessBuilder()
			.directory(dir)
			.command(new File(dir, "main").getPath())
			.start();
	}

	@Override
	public String getTemplate() {
		return "program main;\r\r" +
			"begin\r" +
			"\tWriteLn('Hello World!');\r" +
			"end.";
	}

	@Override
	public String getFileName(String contents) {
		return "main";
	}
}
