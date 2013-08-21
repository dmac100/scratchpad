package ui;

import java.io.File;
import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import controller.MainController;
import event.*;

public class Main {
	private final EventBus eventBus = new EventBus();
	private final Shell shell;
	private final MainController mainController;
	
	private LanguageCombo languageCombo;
	
	public Main(final Shell shell) {
		this.shell = shell;

		shell.setLayout(new GridLayout(1, false));
		Composite top = new Composite(shell, SWT.NONE);
		Composite bottom = new Composite(shell, SWT.BORDER);
		bottom.setLayout(new FillLayout());
		bottom.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
		
		SashForm verticalSash = new SashForm(bottom, SWT.VERTICAL);
		SashForm horizontalSash = new SashForm(verticalSash, SWT.HORIZONTAL);
		EditorText editorText = new EditorText(eventBus, shell, horizontalSash);
		InputText inputText = new InputText(horizontalSash);
		ConsoleText consoleText = new ConsoleText(verticalSash);
		
		horizontalSash.setWeights(new int[] { 80, 20 });
		verticalSash.setWeights(new int[] { 75, 25 });
		
		//horizontalSash.setMaximizedControl(editorText.getControl());
		
		mainController = new MainController(eventBus, editorText, inputText, consoleText);
		
		createMenuBar(shell);
		createToolBar(top);
		refreshTitle();
		
		eventBus.register(new Object() {
			@Subscribe @SuppressWarnings("unused")
			public void onEnabledChanged(EnabledChangedEvent event) {
				createMenuBar(shell);
			}
			
			@Subscribe @SuppressWarnings("unused")
			public void onModified(ModifiedEvent event) {
				refreshTitle();
			}
			
			@Subscribe @SuppressWarnings("unused")
			public void onLanguageChanged(LanguageChangedEvent event) {
				languageCombo.setLanguage(event.getLanguage());
			}
		});
	}
	
	private void createMenuBar(final Shell shell) {
		MenuBuilder menuBuilder = new MenuBuilder(shell);
		
		menuBuilder.addMenu("&File")
			.addItem("&Open...\tCtrl+O").addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					try {
						open();
					} catch(Exception e) {
						displayException(e);
					}
				}
			})
			.setAccelerator(SWT.CONTROL | 'o')
			.addSeparator()
			
			.addItem("&Save\tCtrl+S").addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					try {
						if(mainController.getSaveEnabled()) {
							mainController.save();
						} else {
							saveAs();
						}
					} catch(Exception e) {
						displayException(e);
					}
				}
			})
			.setAccelerator(SWT.CONTROL | 's')
			
			.addItem("Save &As...\tShift+Ctrl+S").addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					try {
						saveAs();
					} catch(Exception e) {
						displayException(e);
					}
				}
			})
			.setAccelerator(SWT.CONTROL | SWT.SHIFT | 's')
			.addSeparator()
			
			.addItem("E&xit\tCtrl+Q").addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					shell.dispose();
				}
			})
			.setAccelerator(SWT.CTRL | 'q');
		
		menuBuilder.addMenu("&Edit")
			.addItem("&Undo\tCtrl+Z").addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					try {
						mainController.undo();
					} catch(Exception e) {
						displayException(e);
					}
				}
			})
			.setAccelerator(SWT.CONTROL | 'z')
			.setEnabled(mainController.undoEnabled())
			
			.addItem("&Redo\tCtrl+Y").addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					try {
						mainController.redo();
					} catch(Exception e) {
						displayException(e);
					}
				}
			}).setEnabled(mainController.redoEnabled())
			.setAccelerator(SWT.CONTROL | 'y')
			.addSeparator()
			
			.addItem("Cu&t\tCtrl+X").addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					try {
						mainController.cut();
					} catch(Exception e) {
						displayException(e);
					}
				}
			})
			.setAccelerator(SWT.CONTROL | 'x')
			.setEnabled(mainController.cutEnabled())
			
			.addItem("&Copy\tCtrl+C").addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					try {
						mainController.copy();
					} catch(Exception e) {
						displayException(e);
					}
				}
			})
			.setAccelerator(SWT.CONTROL | 'c')
			.setEnabled(mainController.copyEnabled())
			
			.addItem("&Paste\tCtrl+V").addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					try {
						mainController.paste();
					} catch(Exception e) {
						displayException(e);
					}
				}
			})
			.setAccelerator(SWT.CONTROL | 'v')
			.setEnabled(mainController.pasteEnabled())
			.addSeparator()
			
			.addItem("&Find...\tCtrl+F").addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					try {
						mainController.find();
					} catch(Exception e) {
						displayException(e);
					}
				}
			})
			.setAccelerator(SWT.CONTROL | 'f')
			.addSeparator()
			
			.addItem("Convert Spaces to Tabs").addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					try {
						mainController.convertSpacesToTabs();
					} catch(Exception e) {
						displayException(e);
					}
				}
			})
			
			.addItem("Convert Tabs to Spaces").addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					try {
						mainController.convertTabsToSpaces();
					} catch(Exception e) {
						displayException(e);
					}
				}
			})
			
			.build();
	}
	
	private void open() throws IOException {
		FileDialog dialog = new FileDialog(shell, SWT.OPEN);
		dialog.setText("Open");
		dialog.setFilterExtensions(new String[] { "*.java", "*.*" });

		String selected = dialog.open();
		
		if(selected != null) {
			mainController.open(selected);
		}
	}
	
	private void saveAs() throws IOException {
		FileDialog dialog = new FileDialog(shell, SWT.SAVE);
		dialog.setText("Save");
		
		String selected = dialog.open();
		
		if(selected != null) {
			mainController.saveAs(selected);
		}
	}

	private void createToolBar(Composite parent) {
		parent.setLayout(new FillLayout());
		parent.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));
		
		Button compileButton = new Button(parent, SWT.NONE);
		compileButton.setText("Compile");
		compileButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
					mainController.compile();
				} catch(Exception e) {
					displayException(e);
				}
			}
		});
		
		final Button stopButton = new Button(parent, SWT.NONE);
		stopButton.setText("Stop");
		stopButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				try {
					mainController.stop();
				} catch(Exception e) {
					displayException(e);
				}
			}
		});
		
		mainController.setRunningChangedCallback(new Callback<Boolean>() {
			public void onCallback(Boolean running) {
				stopButton.setEnabled(running);
			}
		});
		
		this.languageCombo = new LanguageCombo(parent, mainController);
	}
	
	private void displayMessage(String message) {
		MessageBox alert = new MessageBox(shell, SWT.NONE);
		alert.setText("Message");
		alert.setMessage(message);
		alert.open();
	}
	
	private void displayException(Exception e) {
		MessageBox alert = new MessageBox(shell);
		alert.setText("Error");
		alert.setMessage((e.getMessage() == null) ? "Unknown error" : e.getMessage());
		e.printStackTrace();
		alert.open();
	}

	public void refreshTitle() {
		shell.setText("ScratchPad");

		String filename = "Untitled";
		String modified = "";
		
		File file = mainController.getFile();
		if(file != null) {
			filename = file.getName() + " (" + file.getParentFile() + ")";
		}
		
		if(mainController.getModified()) {
			modified = "*";
		}
		
		shell.setText(modified + filename + " - ScratchPad");
	}
	
	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = new Shell(display);

		Main main = new Main(shell);
		
		shell.setSize(700, 600);
		shell.open();

		while(!shell.isDisposed()) {
			if(!display.readAndDispatch()) {
				display.sleep();
			}
		}

		display.dispose();
	}
}
