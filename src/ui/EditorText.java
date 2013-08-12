package ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import syntaxhighlight.ParseResult;
import syntaxhighlight.Style;
import syntaxhighlight.Theme;
import syntaxhighlighter.SyntaxHighlighterParser;
import syntaxhighlighter.brush.Brush;

import com.google.common.eventbus.EventBus;
import compiler.Language;

import event.EnabledChangedEvent;

public class EditorText {
	private final Shell shell;
	private final StyledText styledText;
	private final ColorCache colorCache;
	private final UndoRedoImpl undoRedo;
	
	private Language language;
	private Callback<Void> compileCallback;
	
	public EditorText(final EventBus eventBus, Shell shell, Composite parent) {
		colorCache = new ColorCache(Display.getCurrent());

		this.shell = shell;
		
		styledText = new StyledText(parent, SWT.MULTI | SWT.V_SCROLL);
		styledText.setMargins(2, 1, 2, 1);
		styledText.setTabs(4);
		
		undoRedo = new UndoRedoImpl(styledText);
		
		// Set monospaced font.
		final Font font = new Font(Display.getCurrent(), "Consolas", 10, SWT.NORMAL);
		styledText.setFont(font);
		styledText.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				font.dispose();
				colorCache.dispose();
			}
		});

		// Disable traverse to allow tab and shift+tab for selection indentation.
		styledText.addTraverseListener(new TraverseListener() {
			public void keyTraversed(TraverseEvent event) {
				if(event.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
					event.doit = false;
				}
			}
		});

		styledText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent event) {
				refreshStyle();
			}
		});

		styledText.addVerifyKeyListener(new VerifyKeyListener() {
			public void verifyKey(VerifyEvent event) {
				if(event.character == '\r') {
					if((event.stateMask & SWT.CTRL) > 0) {
						// Compile code on CTRL+ENTER.
						if(compileCallback != null) {
							compileCallback.onCallback(null);
						}
					} else {
						// Preserve indentation on newline.
						newline();
					}
					event.doit = false;
				}
				
				// Indent on tab.
				if(event.character == '\t') {
					if((event.stateMask & SWT.SHIFT) > 0) {
						unindentSelection();
						event.doit = false;
					} else {
						Point selection = styledText.getSelection();
						if(selection.x != selection.y) {
							indentSelection();
							event.doit = false;
						}
					}
				}
				
				// Scroll or Ctrl+Up/Down.
				if((event.stateMask & SWT.CTRL) > 0) {
					if(event.keyCode == SWT.ARROW_UP) {
						styledText.setTopPixel(styledText.getTopPixel() - 20);
					} else if(event.keyCode == SWT.ARROW_DOWN) {
						styledText.setTopPixel(styledText.getTopPixel() + 20);
					}
				}
				
				eventBus.post(new EnabledChangedEvent());
			}
		});
		
		styledText.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				eventBus.post(new EnabledChangedEvent());
			}
		});
		
		styledText.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if((event.stateMask & SWT.CTRL) > 0) {
					if(event.keyCode == 'a') {
						selectAll();
					} else if(event.keyCode == 'd') {
						deleteLine();
					} else if(event.keyCode == 'f') {
						find();
					}
				}
			}
		});
		
		styledText.setFocus();
	}
	
	private void newline() {
		String line = styledText.getText().substring(0, styledText.getCaretOffset());
		line = line.replaceAll("[\\r\\n]$", "");
		int lineStart = lastIndexOf(line, '\r', '\n') + 1;
		line = line.substring(lineStart);
		String indent = line.replaceAll("\\S.*", "");
		styledText.insert("\n" + indent);
		styledText.setCaretOffset(styledText.getCaretOffset() + indent.length() + 1);
	}
	
	private void indentSelection() {
		Point selection = styledText.getSelection();
		
		String oldText = styledText.getText();
		StringBuilder newText = new StringBuilder();
		int selectionStartOffset = 0;
		int selectionEndOffset = 0;

		String line = styledText.getText().substring(0, selection.x);
		int lineStart = lastIndexOf(line, '\r', '\n') + 1;

		if(lineStart == 0) {
			newText.append("\t");
			selectionStartOffset += 1;
			selectionEndOffset += 1;
		}

		for(int i = 0; i < oldText.length(); i++) {
			char c = oldText.charAt(i);
			newText.append(c);
			if((c == '\n' || c == '\r') && i >= lineStart - 1 && i < selection.y) {
				newText.append("\t");
				if (i < selection.x) {
					selectionStartOffset += 1;
				}
				selectionEndOffset += 1;
			}
		}

		styledText.setText(newText.toString());
		styledText.setSelection(selection.x + selectionStartOffset, selection.y + selectionEndOffset);
	}
	
	private void unindentSelection() {
		Point selection = styledText.getSelection();
		
		String oldText = styledText.getText();
		StringBuilder newText = new StringBuilder();
		int selectionStartOffset = 0;
		int selectionEndOffset = 0;

		String line = styledText.getText().substring(0, selection.x);
		int lineStart = lastIndexOf(line, '\r', '\n') + 1;

		int unindentLeft = 0;
		
		if(lineStart == 0) {
			unindentLeft = 4;
		}

		for(int i = 0; i < oldText.length(); i++) {
			char c = oldText.charAt(i);
			
			if((c == ' ' || c == '\t') && unindentLeft > 0) {
				int space = (c == '\t') ? 4 : 1;
				unindentLeft -= space;
				
				if(i <= lineStart) {
					selectionStartOffset -= 1;
				}
				selectionEndOffset -= 1;
			} else {
				newText.append(c);
			}
			
			if(c != ' ' && c != '\t') {
				unindentLeft = 0;
			}
			
			if((c == '\n' || c == '\r') && i >= lineStart - 1 && i < selection.y) {
				unindentLeft = 4;
			}
		}

		styledText.setText(newText.toString());
		styledText.setSelection(selection.x + selectionStartOffset, selection.y + selectionEndOffset);
	}
	
	public void selectAll() {
		styledText.setSelection(0, styledText.getText().length());
	}
	
	public void deleteLine() {
		String s = styledText.getText();
		int offset = styledText.getCaretOffset();
		
		String prefix = s.substring(0, offset);
		String suffix = s.substring(offset);
		
		int lastIndex = lastIndexOf(prefix, '\r', '\n');
		if(lastIndex >= 0) {
			prefix = prefix.substring(0, lastIndex + 1);
		} else {
			prefix = "";
		}
		
		int firstIndex = indexOf(suffix, '\r', '\n');
		if(firstIndex >= 0) {
			suffix = suffix.substring(firstIndex + 1);
		} else {
			suffix = "";
		}
		
		styledText.setText(prefix + suffix);
		styledText.setSelection(prefix.length());
	}
	
	private static int lastIndexOf(String s, char... cs) {
		int index = -1;
		for(char c:cs) {
			index = Math.max(index, s.lastIndexOf(c));
		}
		return index;
	}
	
	private static int indexOf(String s, char... cs) {
		int index = -1;
		for(char c:cs) {
			int i = s.indexOf(c);
			if(i != -1) {
				if(i < index || index == -1) {
					index = i;
				}
			}
		}
		return index;
	}

	public void refreshStyle() {
		if(language == null) return;
		
		Theme theme = new ThemeSublime();
		
		Brush brush = language.getBrush();
		
		SyntaxHighlighterParser parser = new SyntaxHighlighterParser(brush);
		
		List<ParseResult> results = filterResults(parser.parse(null, styledText.getText()));
		
		java.awt.Color background = theme.getBackground();
		
		styledText.setBackground(colorCache.getColor(background));
		
		java.awt.Color normal = theme.getPlain().getColor();
		
		styledText.setForeground(colorCache.getColor(normal));

		StyleRange[] styleRanges = new StyleRange[results.size()];
		for(int i = 0; i < styleRanges.length; i++) {
			ParseResult result = results.get(i);
			
			StyleRange range = new StyleRange();
			range.start = result.getOffset();
			range.length = result.getLength();
			range.fontStyle = SWT.NORMAL;
			
			Style foregroundStyle = theme.getStyles().get(result.getStyleKeys().get(0));
			if(foregroundStyle != null) {
				java.awt.Color foreground = foregroundStyle.getColor();
				range.foreground = colorCache.getColor(foreground);
			} else {
				range.foreground = colorCache.getColor(normal);
			}
			
			styleRanges[i] = range;
		}
		
		styledText.setStyleRanges(styleRanges);
	}

	private List<ParseResult> filterResults(List<ParseResult> results) {
		List<ParseResult> filtered = new ArrayList<>();
		
		int lastIndex = -1;
		for(ParseResult result:results) {
			if(result.getOffset() <= lastIndex) {
				continue;
			}
			
			filtered.add(result);
			
			lastIndex = result.getOffset() + result.getLength();
		}
		
		return filtered;
	}

	public String getText() {
		return styledText.getText();
	}

	public void setText(String string) {
		styledText.setText(string);
		undoRedo.clear();
	}
	
	public void setCompileCallback(Callback<Void> callback) {
		this.compileCallback = callback;
	}

	public Control getControl() {
		return styledText;
	}

	public void setLanguage(Language language) {
		this.language = language;
		refreshStyle();
	}
	
	public void undo() {
		undoRedo.undo();
	}
	
	public void redo() {
		undoRedo.redo();
	}
	
	public void cut() {
		styledText.cut();
	}
	
	public void copy() {
		styledText.copy();
	}
	
	public void paste() {
		styledText.paste();
	}

	public boolean undoEnabled() {
		return undoRedo.hasUndo();
	}

	public boolean redoEnabled() {
		return undoRedo.hasRedo();
	}

	public boolean cutEnabled() {
		return styledText.getSelectionText().length() > 0;
	}

	public boolean copyEnabled() {
		return styledText.getSelectionText().length() > 0;
	}

	public boolean pasteEnabled() {
		return true;
	}

	public void find() {
		new FindDialog(shell, styledText).open();
	}

	public void convertSpacesToTabs() {
		styledText.setText(styledText.getText().replaceAll("    ", "\t"));
	}
	
	public void convertTabsToSpaces() {
		styledText.setText(styledText.getText().replaceAll("\t", "    "));
	}
}