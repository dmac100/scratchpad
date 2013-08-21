package controller;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.widgets.Display;

import com.google.common.eventbus.EventBus;

import ui.*;
import util.StringUtil;

import compiler.Compiler;
import compiler.Language;
import event.*;

public class MainController {
	private final EditorText editorText;
	private final InputText inputText;
	private final ConsoleText consoleText;
	
	private EventBus eventBus;
	private Language language;
	private Future<?> runningProgram;
	private Callback<Boolean> runningChangedCallback;
	
	private File file = null;
	private boolean modified = false;
	
	public MainController(final EventBus eventBus, EditorText editorText, InputText inputText, ConsoleText consoleText) {
		this.eventBus = eventBus;
		this.editorText = editorText;
		this.inputText = inputText;
		this.consoleText = consoleText;
		
		editorText.setCompileCallback(new Callback<Void>() {
			public void onCallback(Void param) {
				compile();
			}
		});
		
		editorText.setModifiedCallback(new Callback<Void>() {
			public void onCallback(Void param) {
				modified = true;
				eventBus.post(new ModifiedEvent(modified));
			}
		});
	}

	public void setLanguageFromName(String name) {
		for(Language language:Languages.getLanguages()) {
			if(language.getName().equalsIgnoreCase(name)) {
				setLanguage(language);
				return;
			}
		}
	}
	
	public void setLanguageFromFilename(String name) {
		String extension = StringUtil.match(name, "\\..*$");
		if(extension != null) {
			for(Language language:Languages.getLanguages()) {
				if(language.getExtension().equals(extension)) {
					setLanguage(language);
					return;
				}
			}
		}
	}
	
	public void setLanguage(Language language) {
		this.language = language;
		this.file = null;
		
		editorText.setLanguage(language);
		editorText.setText(language.getTemplate());
		this.modified = false;
		eventBus.post(new ModifiedEvent(modified));
		eventBus.post(new LanguageChangedEvent(language));
		
		String defaultInput = language.getDefaultInput();
		if(defaultInput != null) {
			inputText.setText(defaultInput);
		}
		
		compile();
	}

	public void open(String selected) throws IOException {
		String text = FileUtils.readFileToString(new File(selected));
		setLanguageFromFilename(selected);
		file = new File(selected);
		editorText.setText(text);
		modified = false;
		eventBus.post(new ModifiedEvent(modified));
	}

	public boolean getSaveEnabled() {
		return file != null;
	}

	public void save() throws IOException {
		FileUtils.writeStringToFile(file, editorText.getText());
		modified = false;
		eventBus.post(new ModifiedEvent(modified));
	}
	
	public void saveAs(String selected) throws IOException {
		file = new File(selected);
		FileUtils.writeStringToFile(file, editorText.getText());
		modified = false;
		eventBus.post(new ModifiedEvent(modified));
	}
	
	public boolean getModified() {
		return modified;
	}
	
	public File getFile() {
		return file;
	}
	
	public void compile() {
		final String source = editorText.getText();
		final String input = inputText.getText();

		stop();
		consoleText.clear();
		
		ConsoleAppender out = new ConsoleAppender(consoleText, null);
		ConsoleAppender err = new ConsoleAppender(consoleText, ConsoleAppender.COLOR_RED);
		ConsoleAppender info = new ConsoleAppender(consoleText, ConsoleAppender.COLOR_BLUE);
		
		Compiler compiler = new Compiler(language);
		runningProgram = compiler.runFile(source, input, out, err, info, new Callback<Void>() {
			public void onCallback(Void param) {
				fireRunningChanged(false);
			}
		});
		
		fireRunningChanged(true);
	}
	
	public void stop() {
		if(runningProgram != null) {
			runningProgram.cancel(true);
		}
	}

	public void setRunningChangedCallback(Callback<Boolean> callback) {
		this.runningChangedCallback = callback;
	}
	
	private void fireRunningChanged(final boolean running) {
		if(runningChangedCallback != null) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					runningChangedCallback.onCallback(running);
				}
			});
		}
	}
	
	public void undo() {
		editorText.undo();
	}
	
	public void redo() {
		editorText.redo();
	}
	
	public void cut() {
		editorText.cut();
	}
	
	public void copy() {
		editorText.copy();
	}
	
	public void paste() {
		editorText.paste();
	}

	public void find() {
		editorText.find();
	}

	public boolean undoEnabled() {
		return editorText.undoEnabled();
	}

	public boolean redoEnabled() {
		return editorText.redoEnabled();
	}
	
	public boolean cutEnabled() {
		return editorText.cutEnabled();
	}
	
	public boolean copyEnabled() {
		return editorText.copyEnabled();
	}
	
	public boolean pasteEnabled() {
		return editorText.pasteEnabled();
	}

	public void convertSpacesToTabs() {
		editorText.convertSpacesToTabs();
	}

	public void convertTabsToSpaces() {
		editorText.convertTabsToSpaces();
	}
}
