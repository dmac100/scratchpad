package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Composite;

public class InputText {
	private final StyledText text;
	
	public InputText(Composite parent) {
		text = new StyledText(parent, SWT.V_SCROLL);
		text.setMargins(2, 2, 2, 2);
		
		text.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if((event.stateMask & SWT.CTRL) > 0) {
					if(event.keyCode == 'a') {
						selectAll();
					}
				}
			}
		});
	}
	
	public void selectAll() {
		text.setSelection(0, text.getText().length());
	}

	public void clear() {
		text.setText("");
	}
	
	public String getText() {
		return text.getText();
	}

	public void setText(String s) {
		text.setText(s);
	}
}