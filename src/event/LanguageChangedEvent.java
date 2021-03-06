package event;

import compiler.Language;

public class LanguageChangedEvent {
	private final Language language;

	public LanguageChangedEvent(Language language) {
		this.language = language;
	}
	
	public Language getLanguage() {
		return language;
	}
}
