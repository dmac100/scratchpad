package controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import ui.*;
import util.StringUtil;

import com.google.common.eventbus.EventBus;
import compiler.*;
import compiler.Compiler;

import event.*;

public class MainController {
	private final Shell shell;
	private final EditorText editorText;
	private final InputText inputText;
	private final ConsoleText consoleText;
	
	private EventBus eventBus;
	private Language language;
	private Future<?> runningProgram;
	private Callback<Boolean> runningChangedCallback;
	
	private File file = null;
	private boolean modified = false;
	private String jarDir = null;
	private String classpath = null;
	
	public MainController(Shell shell, final EventBus eventBus, EditorText editorText, InputText inputText, ConsoleText consoleText) {
		this.shell = shell;
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
		String extension = StringUtil.match(name, "\\.(.*)$");
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
		eventBus.post(new EnabledChangedEvent());
		
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
		
		Compiler compiler = new Compiler(language, jarDir, classpath);
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
	
	private EditFunctions getFocusedEditFunctions() {
		if(editorText.hasFocus()) {
			return editorText.getEditFunctions();
		}
		
		if(inputText.hasFocus()) {
			return inputText.getEditFunctions();
		}
		
		return null;
	}
	
	public void undo() {
		EditFunctions control = getFocusedEditFunctions();
		if(control != null) {
			control.undo();
		}
	}

	public void redo() {
		EditFunctions control = getFocusedEditFunctions();
		if(control != null) {
			control.redo();
		}
	}
	
	public void cut() {
		EditFunctions control = getFocusedEditFunctions();
		if(control != null) {
			control.cut();
		}
	}
	
	public void copy() {
		EditFunctions control = getFocusedEditFunctions();
		if(control != null) {
			control.copy();
		}
	}
	
	public void paste() {
		EditFunctions control = getFocusedEditFunctions();
		if(control != null) {
			control.paste();
		}
	}

	public void find() {
		editorText.find();
	}

	public boolean undoEnabled() {
		EditFunctions control = getFocusedEditFunctions();
		if(control != null) {
			return control.isUndoEnabled();
		} else {
			return false;
		}
	}

	public boolean redoEnabled() {
		EditFunctions control = getFocusedEditFunctions();
		if(control != null) {
			return control.isRedoEnabled();
		} else {
			return false;
		}
	}
	
	public boolean cutEnabled() {
		EditFunctions control = getFocusedEditFunctions();
		if(control != null) {
			return control.isCutEnabled();
		} else {
			return false;
		}
	}
	
	public boolean copyEnabled() {
		EditFunctions control = getFocusedEditFunctions();
		if(control != null) {
			return control.isCopyEnabled();
		} else {
			return false;
		}
	}
	
	public boolean pasteEnabled() {
		EditFunctions control = getFocusedEditFunctions();
		if(control != null) {
			return control.isPasteEnabled();
		} else {
			return false;
		}
	}

	public void convertSpacesToTabs() {
		editorText.convertSpacesToTabs();
	}

	public void convertTabsToSpaces() {
		editorText.convertTabsToSpaces();
	}

	public void setJarDir(String jarDir) {
		this.jarDir = jarDir;
	}

	public void setClasspath(String classpath) {
		this.classpath = classpath;
	}
	
	public boolean importEnabled() {
		return language != null && language.getStandardImportJar() != null;
	}

	public void addImport() {
		List<String> jars = new ArrayList<String>();
		
		if(language.getStandardImportJar() != null) {
			jars.add(language.getStandardImportJar());
		}
		
		if(jarDir != null) {
			String[] files = new File(jarDir).list();
			if(files != null) {
				for(String file:files) {
					if(file.toLowerCase().endsWith(".jar")) {
						jars.add(new File(jarDir, file).getAbsolutePath());
					}
				}
			}
		}
		
		if(classpath != null) {
			for(String path:classpath.split("[;:]")) {
				jars.add(new File(path).getAbsolutePath());
			}
		}
		
		Importer importer = new Importer(jars);
		ImportDialog dialog = new ImportDialog(shell, importer, editorText.getStyledText());
		dialog.open(editorText.getSelectedText());
	}
}