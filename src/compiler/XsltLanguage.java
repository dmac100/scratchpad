package compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import syntaxhighlighter.brush.*;

public class XsltLanguage implements Language {
	public XsltLanguage() {
	}
	
	@Override
	public String getName() {
		return "XSLT";
	}
	
	@Override
	public String getExtension() {
		return ".xslt";
	}
	
	@Override
	public Brush getBrush() {
		return new BrushXml();
	}
	
	@Override
	public Process createCompiler(File dir, String name) throws IOException {
		return null;
	}
	
	@Override
	public Process runProgram(File dir, String name) throws IOException {
		return new ProcessBuilder()
			.directory(dir)
			.command("xsltproc", name + getExtension(), "-")
			.start();
	}

	@Override
	public String getDefaultInput() {
		return "<?xml version='1.0' encoding='UTF-8'?>\r" +
			"<xml></xml>";
	}
	
	@Override
	public String getTemplate() {
		return "<?xml version='1.0' encoding='UTF-8'?>\r" +
			"<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version='1.0'>\r" +
			"\t<xsl:output method='xml' indent='yes'/>\r" +
			"\t<xsl:template match='*'>\r" +
			"\t\t<xsl:copy><xsl:apply-templates/></xsl:copy>\r" +
			"\t</xsl:template>\r" +
			"</xsl:stylesheet>\r";
	}

	@Override
	public String getFileName(String contents) {
		return "main";
	}
}
