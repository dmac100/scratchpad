package compiler;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;

import syntaxhighlighter.brush.*;

/**
 * Parses and retrieves the list of configured languages.
 */
public class Languages {
	private static List<Language> languages = new Languages().readLanguages();

	/**
	 * Returns a list of all configured languages.
	 */
	public static List<Language> getLanguages() {
		return languages;
	}

	/**
	 * Returns the parsed languages from a configuration file.
	 */
	private List<Language> readLanguages() {
		List<Language> languages = new ArrayList<>();
		
		try {
			Document document = getXmlDocument("/compiler/languages.xml");
			
			for(Element language:document.getRootElement().getChildren("language")) {
				Language parsed = parseLanguage(language);
				if(!StringUtils.isBlank(parsed.getName())) {
					languages.add(parsed);
				}
			}
		} catch(Exception e) {
			System.err.println("Error parsing languages: + e");
		}
		
		return languages;
	}
	
	/**
	 * Returns a language by parsing an xml language element.
	 */
	private Language parseLanguage(Element languageElement) {
		String name = getAttribute(languageElement, "name");
		String extension = getAttribute(languageElement, "extension");
		String brush = getAttribute(languageElement, "brush");
		String compiler = getChild(languageElement, "compiler");
		String run = getChild(languageElement, "run");
		String filenameMatcher = getChild(languageElement, "filenameMatcher");
		String template = getChild(languageElement, "template");
		String defaultInput = getChild(languageElement, "defaultInput");
		
		// Add defaults for non-optional parameters.
		if(extension == null) extension = "";
		if(brush == null) brush = "BrushPlain";
		if(template == null) template = "";
		
		// Load the brush by class name.
		Brush brushObject = new BrushPlain();
		try {
			brushObject = (Brush) Class.forName("syntaxhighlighter.brush." + brush).newInstance();
		} catch(Exception e) {
			System.err.println("Error loading brush: " + brush + " - " + e);
		}
		
		return new Language(
			name,
			extension,
			brushObject,
			compiler,
			run,
			filenameMatcher,
			template,
			defaultInput
		);
	}

	/**
	 * Returns the trimmed text of the named child of the specified element,
	 * or null is the child doesn't exist.
	 */
	private String getChild(Element element, String name) {
		Element child = element.getChild(name);
		if(child == null) return null;
		return child.getTextTrim();
	}

	/**
	 * Returns the attribute value of the named attribute of the specified element,
	 * or null if the attribute doesn't exist.
	 */
	private String getAttribute(Element element, String name) {
		return element.getAttributeValue(name);
	}

	/**
	 * Returns the xml document created by reading a file from the classpath.
	 */
	private Document getXmlDocument(String path) throws IOException, JDOMException {
		InputStream inputStream = getClass().getResourceAsStream(path);
		try {
			String text = IOUtils.toString(inputStream);
			return new SAXBuilder().build(new StringReader(text));
		} finally {
			IOUtils.closeQuietly(inputStream);
		}
	}
}
