package compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import syntaxhighlighter.brush.*;

public class HaskellLanguage extends AbstractLanguage {
	@Override
	public String getName() {
		return "Haskell";
	}
	
	@Override
	public String getExtension() {
		return ".hs";
	}
	
	@Override
	public Brush getBrush() {
		return new BrushPlain();
	}
	
	@Override
	public Process createCompiler(File dir, String name) throws IOException {
		return new ProcessBuilder()
			.command("ghc", "-o", "main", name + getExtension())
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
		return "main = putStrLn \"Hello World!\"";
	}

	@Override
	public String getFileName(String contents) {
		return "main";
	}
}
