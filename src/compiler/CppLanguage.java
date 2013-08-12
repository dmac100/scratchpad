package compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import syntaxhighlighter.brush.*;

public class CppLanguage implements Language {
	@Override
	public String getName() {
		return "C++";
	}
	
	@Override
	public String getExtension() {
		return ".cpp";
	}
	
	@Override
	public Brush getBrush() {
		return new BrushCpp();
	}
	
	@Override
	public Process createCompiler(File dir, String name) throws IOException {
		return new ProcessBuilder()
			.command("g++", "-o", "main", name + getExtension())
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
		return "#include <iostream>\r\r" +
			"int main(int argc, char *argv[]) {\r" +
			"\tstd::cout << \"Hello World!\" << std::endl;\r" +
			"\treturn 0;\r" +
			"}";
	}

	@Override
	public String getFileName(String contents) {
		return "main";
	}
}
