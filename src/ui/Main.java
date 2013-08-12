package ui;

import java.io.IOException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import controller.MainController;
import event.EnabledChangedEvent;

public class Main {
	private final EventBus eventBus = new EventBus();
	private final Shell shell;
	private final MainController mainController;
	
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
		
		mainController = new MainController(editorText, inputText, consoleText);
		
		createMenuBar(shell);
		createToolBar(top);
		
		eventBus.register(new Object() {
			@Subscribe @SuppressWarnings("unused")
			public void onEnabledChanged(EnabledChangedEvent event) {
				createMenuBar(shell);
			}
		});
	}
	
	private void createMenuBar(final Shell shell) {
		MenuBuilder menuBuilder = new MenuBuilder(shell);
		
		menuBuilder.addMenu("File")
			.addItem("Open...").addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					try {
						open();
					} catch(Exception e) {
						displayException(e);
					}
				}
			})
			.addSeparator()
			.addItem("Exit").addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					shell.dispose();
				}
			});
		
		menuBuilder.addMenu("Edit")
			.addItem("Undo").addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					try {
						mainController.undo();
					} catch(Exception e) {
						displayException(e);
					}
				}
			})
			.setEnabled(mainController.undoEnabled())
			.addItem("Redo").addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					try {
						mainController.redo();
					} catch(Exception e) {
						displayException(e);
					}
				}
			}).setEnabled(mainController.redoEnabled())
			.addSeparator()
			.addItem("Cut").addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					try {
						mainController.cut();
					} catch(Exception e) {
						displayException(e);
					}
				}
			})
			.setEnabled(mainController.cutEnabled())
			.addItem("Copy").addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					try {
						mainController.copy();
					} catch(Exception e) {
						displayException(e);
					}
				}
			})
			.setEnabled(mainController.copyEnabled())
			.addItem("Paste").addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					try {
						mainController.paste();
					} catch(Exception e) {
						displayException(e);
					}
				}
			})
			.setEnabled(mainController.pasteEnabled())
			.addSeparator()
			.addItem("Find...").addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent event) {
					try {
						mainController.find();
					} catch(Exception e) {
						displayException(e);
					}
				}
			})
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
		
		LanguageCombo languageCombo = new LanguageCombo(parent, mainController);
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

	public static void main(String[] args) {
		Display display = new Display();

		Shell shell = new Shell(display);

		Main test = new Main(shell);

		shell.setText("ScratchPad");
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
