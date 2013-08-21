package compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import syntaxhighlighter.brush.*;

public class CLanguage extends AbstractLanguage {
	@Override
	public String getName() {
		return "C";
	}
	
	@Override
	public String getExtension() {
		return ".c";
	}
	
	@Override
	public Brush getBrush() {
		return new BrushCpp();
	}
	
	@Override
	public Process createCompiler(File dir, String name) throws IOException {
		return new ProcessBuilder()
			.command("gcc", "-o", "main", name + getExtension())
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
		return "#include <stdio.h>\n\n" +
			"int main(int argc, char *argv[]) {\n" +
			"\tprintf(\"Hello World!\\n\");\n" +
			"\treturn 0;\n" +
			"}";
	}

	@Override
	public String getFileName(String contents) {
		return "main";
	}
}
