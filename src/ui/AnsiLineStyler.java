package ui;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.widgets.Display;

import ansi.*;

/**
 * Styles any StyledText with ANSI colors.
 * Adapted from: https://gist.github.com/sporkmonger/113079.
 */
public class AnsiLineStyler implements LineStyleListener, VerifyListener, DisposeListener {
	private StyledText styledText = null;
	private Display display = null;
	private StyleRange lastStyle = null;
	private ArrayList<StyleRange> queuedStyles = new ArrayList<StyleRange>();
	private ColorCache colorCache;
	
	private AnsiParser ansiParser = new AnsiParser();

	public AnsiLineStyler(StyledText styledText) {
		this.colorCache = new ColorCache(display);
		this.styledText = styledText;
		this.display = this.styledText.getDisplay();
	}

	@Override
	public void verifyText(VerifyEvent event) {
		ParseResult parseResult = ansiParser.parseText(event.text, event.start);
		
		event.text = parseResult.getNewText();
		
		for(AnsiStyle ansiStyle:parseResult.getStyleRanges()) {
			StyleRange style = new StyleRange();
			style.start = ansiStyle.start;
			style.length = ansiStyle.length;
			
			if(ansiStyle.foreground != null)
				style.foreground = colorCache.getColor(ansiStyle.foreground);
			if(ansiStyle.background != null)
				style.background = colorCache.getColor(ansiStyle.background);
			
			if(ansiStyle.bold) style.fontStyle |= SWT.BOLD;
			if(ansiStyle.italic) style.fontStyle |= SWT.ITALIC;
			if(ansiStyle.underline) style.underline = true;
			if(ansiStyle.doubleUnderline) style.underlineStyle = SWT.UNDERLINE_DOUBLE;
			
			queuedStyles.add(style);
			lastStyle = style;
		}
	}
	
	@Override
	public void lineGetStyle(LineStyleEvent event) {
		int start = event.lineOffset;
		int end = event.lineOffset + event.lineText.length();
		StyleRange firstStyle = null;
		ArrayList<StyleRange> applicableStyles = new ArrayList<StyleRange>();
		for (int i = 0; i < queuedStyles.size(); i++) {
			StyleRange currentStyle = queuedStyles.get(i);
			if (currentStyle.start >= start && currentStyle.start <= end) {
				applicableStyles.add(currentStyle);
				queuedStyles.remove(i);
				if (firstStyle == null || currentStyle.start < firstStyle.start) {
					firstStyle = currentStyle;
				}
				i--;
			}
		}
		if (lastStyle != null) {
			StyleRange initialStyle = new StyleRange();
			initialStyle.start = start;
			if (firstStyle == null) {
				initialStyle.length = end - start;
			} else {
				initialStyle.length = firstStyle.start - start - 1;
			}
			if (initialStyle.length > 0) {
				if (lastStyle.fontStyle != SWT.NORMAL || lastStyle.foreground != null || lastStyle.background != null || lastStyle.underline != false) {
					initialStyle.fontStyle = lastStyle.fontStyle;
					initialStyle.foreground = lastStyle.foreground;
					initialStyle.background = lastStyle.background;
					initialStyle.underline = lastStyle.underline;
					initialStyle.underlineStyle = lastStyle.underlineStyle;
					initialStyle.underlineColor = lastStyle.underlineColor;
					applicableStyles.add(initialStyle);
					lastStyle = initialStyle;
				}
			}
		}
		StyleRange[] styles = new StyleRange[applicableStyles.size()];
		for (int i = 0; i < applicableStyles.size(); i++) {
			styles[i] = applicableStyles.get(i);
		}
		event.styles = styles;
	}
	
	@Override
	public void widgetDisposed(DisposeEvent event) {
		colorCache.dispose();
	}
}