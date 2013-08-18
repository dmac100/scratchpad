package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;

public class InputText {
	private final StyledText text;
	
	public InputText(Composite parent) {
		text = new StyledText(parent, SWT.V_SCROLL);
		text.setMargins(2, 2, 2, 2);
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