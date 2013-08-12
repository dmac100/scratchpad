package ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;

public class ConsoleText {
	private final StyledText text;
	
	public ConsoleText(Composite parent) {
		text = new StyledText(parent, SWT.V_SCROLL);
		text.setEditable(false);
		text.setMargins(3, 0, 3, 0);
		
		final AnsiLineStyler ansiLineStyler = new AnsiLineStyler(text);
		text.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent event) {
				ansiLineStyler.verifyText(event);
			}
		});
		text.addLineStyleListener(new LineStyleListener() {
			public void lineGetStyle(LineStyleEvent event) {
				ansiLineStyler.lineGetStyle(event);
			}
		});
		text.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				ansiLineStyler.dispose();
			}
		});
	}

	public void clear() {
		text.setText("");
	}
	
	public void append(String s) {
		text.append(s);
	}
}