package ui;

import java.util.ArrayList;
import java.util.List;

import syntaxhighlighter.brush.BrushGroovy;
import syntaxhighlighter.brush.BrushRuby;

import compiler.*;

public class Languages {
	public List<Language> getLanguages() {
		List<Language> languages = new ArrayList<Language>();
		
		languages.add(new JavaLanguage());
		languages.add(new AbstractLanguage("Ruby", ".rb", null, "ruby", new BrushRuby(), "puts 'Hello World'"));
		languages.add(new AbstractLanguage("Groovy", "groovy", null, "groovy", new BrushGroovy(), "println 'Hello World'"));
		languages.add(new ScalaLanguage());
		
		return languages;
	}
}