package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

class FindDialog extends Dialog {
	private final StyledText styledText;

	public FindDialog(Shell parent, StyledText styledText) {
		super(parent, 0);
		this.styledText = styledText;
		setText("Find");
	}
	
	public String open() {
		Shell parent = getParent();
		final Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
		shell.setText(getText());
		shell.setLayout(new GridLayout());
		
		// Form Controls

		Composite formComposite = new Composite(shell, SWT.NONE);
		formComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		GridLayout formLayout = new GridLayout(2, false);
		formComposite.setLayout(formLayout);
		
		Label label = new Label(formComposite, SWT.NONE);
		label.setText("Find: ");
		
		final Text text = new Text(formComposite, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		
		// Button Composite
		
		Composite buttonComposite = new Composite(shell, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.NONE, false, false));
		
		GridLayout buttonLayout = new GridLayout(2, true);
		buttonLayout.marginHeight = 0;
		buttonComposite.setLayout(buttonLayout);
		
		Button findButton = new Button(buttonComposite, SWT.NONE);
		findButton.setText("Find");
		findButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));
		
		Button closeButton = new Button(buttonComposite, SWT.NONE);
		closeButton.setText("Close");
		closeButton.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));

		shell.setDefaultButton(findButton);
		
		findButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				find(text.getText());
			}
		});
		
		closeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				shell.dispose();
			}
		});
		
		// Open and wait for result.
		shell.setSize(200, 100);
		
		center(shell);
		
		shell.open();
		Display display = parent.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		
		return null;
	}
	
	private void center(Shell dialog) {
        Rectangle bounds = getParent().getBounds();
        Point size = dialog.getSize();

        dialog.setLocation(
        	bounds.x + (bounds.width - size.x) / 2,
        	bounds.y + (bounds.height - size.y) / 2
        );
	}

	/**
	 * Selects the next case-insensitive match after the current selection for searchText,
	 * or wraps to the beginning if it's not found.
	 */
	private void find(String searchText) {
		String text = styledText.getText();
		
		int start = styledText.getSelectionRange().x + 1;
		
		if(start >= text.length()) {
			start = 0;
		}
		
		String remaining = text.substring(start);
		
		int index = remaining.toLowerCase().indexOf(searchText.toLowerCase());
		if(index == -1) {
			start = 0;
			index = text.toLowerCase().indexOf(searchText.toLowerCase());
		}
		
		if(index >= 0) {
			styledText.setSelection(start + index, start + index + searchText.toLowerCase().length());
		}
	}
}