package compiler;

import java.io.File;
import java.io.IOException;

import syntaxhighlighter.brush.Brush;

public interface Language {
	String getName();
	String getFileName(String contents);
	String getExtension();
	Process createCompiler(File dir, String name) throws IOException;
	Process runProgram(File dir, String name) throws IOException;
	Brush getBrush();
	String getTemplate();
	String getDefaultInput();
}