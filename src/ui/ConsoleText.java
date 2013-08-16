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
		text.addVerifyListener(ansiLineStyler);
		text.addLineStyleListener(ansiLineStyler);
		text.addDisposeListener(ansiLineStyler);
	}

	public void clear() {
		text.setText("");
	}
	
	public void append(String s) {
		text.append(trimLongLines(s));
		text.setTopIndex(text.getLineCount() - 1);
	}

	private static String trimLongLines(String s) {
		StringBuilder buffer = new StringBuilder();
		
		for(String line:s.split("(?=\\r?\\n)")) {
			if(line.length() > 1000) {
				line = line.substring(0, 1000);
			}
			buffer.append(line);
		}
		
		return buffer.toString();
	}
}