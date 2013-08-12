package ui;

import java.util.Map;
import java.util.TreeMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

import compiler.*;

import controller.MainController;

public class LanguageCombo {
	private final Combo combo;
	private final MainController mainController;

	private final Map<String, Language> languageMap = new TreeMap<>();
	
	public LanguageCombo(Composite parent, MainController mainController) {
		this.combo = new Combo(parent, SWT.READ_ONLY);
		this.mainController = mainController;
		
		for(Language language:new Languages().getLanguages()) {
			languageMap.put(language.getName(), language);
		}
		
		for(String name:languageMap.keySet()) {
			combo.add(name);
		}
		
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				selectLanguage();
			}
		});
		
		combo.select(0);
		selectLanguage();
	}
	
	private void selectLanguage() {
		int index = combo.getSelectionIndex();
		if(index >= 0) {
			String selected = combo.getItem(index);
			Language language = languageMap.get(selected);
			
			mainController.setLanguage(language);
		}
	}
}
