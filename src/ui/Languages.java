package ui;

import java.util.ArrayList;
import java.util.List;

import syntaxhighlighter.brush.*;

import compiler.*;

public class Languages {
	public List<Language> getLanguages() {
		List<Language> languages = new ArrayList<Language>();
		
		languages.add(new JavaLanguage());
		languages.add(new CLanguage());
		languages.add(new CppLanguage());
		languages.add(new MonoLanguage());
		languages.add(new HaskellLanguage());
		languages.add(new PascalLanguage());
		languages.add(new AbstractLanguage("JavaScript", ".js", null, "js", new BrushJScript(), "print('Hello World');"));
		languages.add(new AbstractLanguage("Ruby", ".rb", null, "ruby", new BrushRuby(), "puts 'Hello World'"));
		languages.add(new AbstractLanguage("Groovy", "groovy", null, "groovy", new BrushGroovy(), "println 'Hello World'"));
		languages.add(new ScalaLanguage());
		
		return languages;
	}
}