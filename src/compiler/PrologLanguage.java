package compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import syntaxhighlighter.brush.*;

public class PrologLanguage extends AbstractLanguage {
	@Override
	public String getName() {
		return "Prolog";
	}
	
	@Override
	public String getExtension() {
		return ".pl";
	}
	
	@Override
	public Process runProgram(File dir, String name) throws IOException {
		return new ProcessBuilder()
		.command("swipl", "-t", "main", "-l", name + getExtension())
		.directory(dir)
		.start();
	}

	@Override
	public String getTemplate() {
		return "main :- write('Hello World!'), nl.";
	}

	@Override
	public String getFileName(String contents) {
		return "main";
	}
}
