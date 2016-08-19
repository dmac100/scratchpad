package ui;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;

public class FontList {
	private static final String monospaceName = getMonospaceName();
	
	public static final Font consolas10 = new Font(Display.getCurrent(), monospaceName, 10, SWT.NORMAL);
	public static final Font consolas8 = new Font(Display.getCurrent(), monospaceName, 8, SWT.NORMAL);
	
	/**
	 * Returns the first found monospace font.
	 */
	private static String getMonospaceName() {
		Set<String> fonts = new HashSet<>();
		
		for(FontData fontData:Display.getCurrent().getFontList(null, true)) {
			fonts.add(fontData.getName());
		}

		System.out.println(fonts);
		
		for(String name:Arrays.asList("Consolas", "Courier", "Courier New")) {
			if(fonts.contains(name)) {
				System.out.println("Using: " + name);
				return name;
			}
		}
		
		return "Monospace";
	}
}

