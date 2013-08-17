package ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;

import ansi.*;

/**
 * A text widget containing console output. Allows ANSI styles to control
 * output formatting.
 */
public class ConsoleText {
	private final StyledText text;
	private final ColorCache colorCache;
	
	private List<StyleRange> styles = new ArrayList<StyleRange>();
	private AnsiStyle lastStyle;
	
	public ConsoleText(Composite parent) {
		text = new StyledText(parent, SWT.V_SCROLL);
		text.setEditable(false);
		text.setMargins(3, 0, 3, 0);

		colorCache = new ColorCache(Display.getCurrent());
		text.addDisposeListener(colorCache);
		
		clear();
	}

	/**
	 * Clears the console.
	 */
	public void clear() {
		text.setText("");
		
		styles.clear();
		styles.add(new StyleRange());
		
		lastStyle = new AnsiStyle();
	}
	
	/**
	 * Appends some text to the end of the console.
	 */
	public void append(String newText) {
		String trimmed = trimLongLines(newText);
		addWithStyles(trimmed);
		text.setTopIndex(text.getLineCount() - 1);
	}
	
	/**
	 * Adds text to the console by extracting any new styles from it, appending
	 * the text, and applying the styles.
	 */
	private void addWithStyles(String newText) {
		ParseResult parseResult = new AnsiParser().parseText(lastStyle, newText);
		
		int offset = text.getCharCount();
		text.append(parseResult.getNewText());
		
		for(AnsiStyle ansiStyle:parseResult.getStyleRanges()) {
			StyleRange style = new StyleRange(styles.get(styles.size() - 1));
			style.start = offset + ansiStyle.start;
			style.length = ansiStyle.length;
			
			if(ansiStyle.foreground == null) {
				style.foreground = null;
			} else {
				style.foreground = colorCache.getColor(ansiStyle.foreground);
			}
			if(ansiStyle.background == null) {
				style.background = null;
			} else {
				style.background = colorCache.getColor(ansiStyle.background);
			}
			
			if(ansiStyle.bold) style.fontStyle |= SWT.BOLD;
			if(ansiStyle.italic) style.fontStyle |= SWT.ITALIC;
			if(ansiStyle.underline) style.underline = true;
			if(ansiStyle.doubleUnderline) style.underlineStyle = SWT.UNDERLINE_DOUBLE;
			
			styles.add(style);
			lastStyle = ansiStyle;
		}
		
		text.setStyleRanges(styles.toArray(new StyleRange[styles.size()]));
	}

	/**
	 * Returns a string with trimmed long lines.
	 */
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