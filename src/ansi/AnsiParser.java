package ansi;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;

/**
 * A parser to extract the ansi styles from text.
 * Adapted from: https://gist.github.com/sporkmonger/113079.
 */
public class AnsiParser {
	private Pattern controlSequencePattern = Pattern.compile("((\u009B|\u001B\\[)(\\d+;)*(\\d+)?[m])");
	private RGB[] colorTable = new RGB[20];
	
	public AnsiParser() {
		// normal colors
		this.colorTable[0] = new RGB(0, 0, 0); // black
		this.colorTable[1] = new RGB(204, 0, 0); // red
		this.colorTable[2] = new RGB(0, 204, 0); // green
		this.colorTable[3] = new RGB(204, 204, 0); // yellow
		this.colorTable[4] = new RGB(0, 0, 204); // blue
		this.colorTable[5] = new RGB(204, 0, 204); // magenta
		this.colorTable[6] = new RGB(0, 204, 204); // cyan
		this.colorTable[7] = new RGB(192, 192, 192); // white
		this.colorTable[8] = null; // not used
		this.colorTable[9] = null; // not used
		// bright colors
		this.colorTable[10] = new RGB(128, 128, 128); // black
		this.colorTable[11] = new RGB(255, 0, 0); // red
		this.colorTable[12] = new RGB(0, 255, 0); // green
		this.colorTable[13] = new RGB(255, 255, 0); // yellow
		this.colorTable[14] = new RGB(0, 0, 255); // blue
		this.colorTable[15] = new RGB(255, 0, 255); // magenta
		this.colorTable[16] = new RGB(0, 255, 255); // cyan
		this.colorTable[17] = new RGB(255, 255, 255); // white
		this.colorTable[18] = null; // not used
		this.colorTable[19] = null; // not used
	}
	
	/**
	 * Parses text and extracts ANSI escape sequences from it.
	 * Returns the modified text and a list of styles to apply,
	 * with their positions offset by 'offset'.
	 */
	public ParseResult parseText(String text, int offset) {
		List<AnsiStyle> styles = new ArrayList<AnsiStyle>();
		StringBuffer modified = new StringBuffer();

		int removedCharacters = 0;
		
		Matcher matcher = controlSequencePattern.matcher(text);
		while(matcher.find()) {
			matcher.appendReplacement(modified, "");
			
			String controlSequence = matcher.group();
			
			int[] codes = parseSequenceCodes(controlSequence);
			
			AnsiStyle newStyleRange = buildStyleRange(codes);
			newStyleRange.start = offset + matcher.start() - removedCharacters;
			styles.add(newStyleRange);
			
			removedCharacters += controlSequence.length();
		}
		matcher.appendTail(modified);
		
		for(int i = 0; i < styles.size() - 1; i++) {
			AnsiStyle thisRange = styles.get(i);
			AnsiStyle nextRange = styles.get(i + 1);
			thisRange.length = nextRange.start - thisRange.start;
		}
		
		if(!styles.isEmpty()) {
			AnsiStyle lastRange = styles.get(styles.size() - 1);
			lastRange.length = offset + text.length() - lastRange.start - removedCharacters;
		}
		
		return new ParseResult(modified.toString(), styles);
	}
	
	private int[] parseSequenceCodes(String controlSequence) {
		String codeSequence = null;
		if (controlSequence.charAt(0) == '\u009B') {
			codeSequence = controlSequence.substring(1, controlSequence.length() - 1);
		} else if (controlSequence.charAt(0) == '\u001B') {
			codeSequence = controlSequence.substring(2, controlSequence.length() - 1);
		} else {
			throw new RuntimeException("Invalid ANSI control sequence: '" + controlSequence + "'");
		}
		String[] codeStrings = codeSequence.toString().split(";");
		if (codeStrings.length == 0) {
			return new int[] { 0 };
		} else {
			int[] codes = new int[codeStrings.length];
			for (int i = 0; i < codeStrings.length; i++) {
				if (codeStrings[i].equals("")) {
					codes[i] = 0;
				} else {
					codes[i] = Integer.parseInt(codeStrings[i]);
				}
			}
			return codes;
		}
	}
	
	private AnsiStyle buildStyleRange(int[] codes) {
		AnsiStyle newStyleRange = new AnsiStyle();
		for (int i = 0; i < codes.length; i++) {
			RGB tempColor = null;
			switch (codes[i]) {
			case 0:
				newStyleRange.foreground = null;
				newStyleRange.background = null;
				newStyleRange.bold = false;
				newStyleRange.underline = false;
				newStyleRange.doubleUnderline = false;
				break;
			case 1:
				newStyleRange.bold = true;
				break;
			case 2:
				// It's actually supposed to be faint, but there's no way to
				// display that
				newStyleRange.bold = false;
				newStyleRange.italic = false;
				break;
			case 3:
				newStyleRange.italic = true;
				break;
			case 4:
				newStyleRange.underline = true;
				newStyleRange.doubleUnderline = false;
				break;
			case 7:
				// Swap foreground and background
				tempColor = newStyleRange.foreground;
				newStyleRange.foreground = newStyleRange.background;
				newStyleRange.background = tempColor;
				break;
			case 21:
				newStyleRange.underline = true;
				newStyleRange.doubleUnderline = true;
				break;
			case 22:
				newStyleRange.bold = false;
				newStyleRange.italic = false;
				break;
			case 24:
				newStyleRange.underline = false;
				newStyleRange.doubleUnderline = false;
				break;
			case 27:
				// Technically, this should just unset reversed foreground, but
				// we're
				// just going to reverse again
				tempColor = newStyleRange.foreground;
				newStyleRange.foreground = newStyleRange.background;
				newStyleRange.background = tempColor;
				break;
			default:
				if (codes[i] >= 30 && codes[i] < 40) {
					newStyleRange.foreground = colorTable[codes[i] - 30];
				} else if (codes[i] >= 40 && codes[i] < 50) {
					newStyleRange.background = colorTable[codes[i] - 40];
				} else if (codes[i] >= 90 && codes[i] < 100) {
					newStyleRange.foreground = colorTable[codes[i] - 90 + 10];
				} else if (codes[i] >= 100 && codes[i] < 110) {
					newStyleRange.background = colorTable[codes[i] - 100 + 10];
				}
				break;
			}
		}
		return newStyleRange;
	}
}
