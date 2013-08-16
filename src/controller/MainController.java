package controller;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.widgets.Display;

import ui.*;

import compiler.Compiler;
import compiler.Language;

public class MainController {
	private final EditorText editorText;
	private final InputText inputText;
	private final ConsoleText consoleText;
	
	private Language language;
	private Future<?> runningProgram;
	private Callback<Boolean> runningChangedCallback;

	public MainController(EditorText editorText, InputText inputText, ConsoleText consoleText) {
		this.editorText = editorText;
		this.inputText = inputText;
		this.consoleText = consoleText;
		
		editorText.setCompileCallback(new Callback<Void>() {
			public void onCallback(Void param) {
				compile();
			}
		});
	}

	public void setLanguage(Language language) {
		this.language = language;
		editorText.setLanguage(language);
		editorText.setText(language.getTemplate());
		compile();
	}

	public void open(String selected) throws IOException {
		String text = FileUtils.readFileToString(new File(selected));
		editorText.setText(text);
	}

	public void compile() {
		final String source = editorText.getText();
		final String input = inputText.getText();

		stop();
		consoleText.clear();
		
		ConsoleAppender out = new ConsoleAppender(consoleText, ConsoleAppender.COLOR_OFF);
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
