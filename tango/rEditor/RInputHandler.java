package tango.rEditor;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;

import tango.rEditor.DefaultInputHandler;
import tango.rEditor.JEditTextArea;
import tango.rEditor.TextUtilities;

import org.omancode.r.ui.RSwingConsole;

public class RInputHandler extends DefaultInputHandler {

	public static final ActionListener R_INSERT_TAB = new r_insert_tab();

	public static final ActionListener R_RUN_LINES = new r_run_lines();

	public static final ActionListener R_RUN_ALL = new r_run_all();

	public static final ActionListener R_COMMENT_LINES = new r_comment_lines();

	public static final ActionListener R_PREV_LINE = new r_prev_line(false);

	public static final ActionListener R_NEXT_LINE = new r_next_line(false);

	public static final ActionListener R_CLOSE_POPUPS = new r_close_popups();

	public static final ActionListener R_INSERT_BREAK = new r_insert_break();
        
        public RSwingConsole rConsole;

	public static Popup codeCompletion;

	private String funHelp = null;
	private JToolTip Tip;
	private static Popup funHelpTip = null;

	public static class r_insert_tab extends insert_tab {

		public void actionPerformed(ActionEvent evt) {
			if (codeCompletion != null) {
				codeCompletion.hide();
				codeCompletion = null;
			}

			JEditTextArea textArea = getTextArea(evt);

			int carPos = textArea.getCaretPosition();

			if (carPos > 0 && textArea.getText(carPos - 1, 1).trim().length() != 0) {
				int line = textArea.getCaretLine();
				String lineStr = textArea.getLineText(line);
				int start = 0, end = carPos;
				try {
					start = TextUtilities.findWordStart(lineStr, carPos - textArea.getLineStartOffset(line) - 1, ".");
				} catch (StringIndexOutOfBoundsException ex) {
				}
				try {
					end = TextUtilities.findWordEnd(lineStr, carPos - textArea.getLineStartOffset(line) - 1, ".");
				} catch (StringIndexOutOfBoundsException ex) {
				}

				boolean isfile = false;

				String pattern = "";

				try {
					pattern = lineStr.substring(start, end).trim();
				} catch (StringIndexOutOfBoundsException e) {
				}

				if (pattern.length() <= 0) {
					super.actionPerformed(evt);
					return;
				}

				try {
					isfile = lineStr.substring(start - 1, start).equalsIgnoreCase("\"");
				} catch (StringIndexOutOfBoundsException ex) {
				}

				int x = textArea._offsetToX(line, carPos);
				int y = textArea.lineToY(line);

				Point loc = new Point(x, y);
				SwingUtilities.convertPointToScreen(loc, (Component) evt.getSource());

				int posC = -1;

				if (isfile)
					posC = CodeCompletion.getInstance().updateFileList(pattern);
				else {
					posC = CodeCompletion.getInstance().updateList(pattern);
				}
				if (posC > 0) {
					codeCompletion = PopupFactory.getSharedInstance().getPopup((Component) evt.getSource(), (Component) CodeCompletion.getInstance(),
							loc.x, loc.y + ((Component) evt.getSource()).getFont().getSize() + 10);
					codeCompletion.show();
				}

			} else
				super.actionPerformed(evt);
		}
	}

	public static class r_run_lines extends insert_tab {
		public void actionPerformed(ActionEvent evt) {
			JEditTextArea textArea = getTextArea(evt);
			try {
				
				String s = textArea.getSelectedText();
				if(s==null)
					s="";
				s=s.trim();
				if(s.length()==0){
					int line = textArea.getSelectionStartLine();
					s = textArea.getLineText(line).trim();
					int pos = textArea.getLineStartOffset(line+1);
					if(pos>0)
						textArea.setCaretPosition(pos);
				}
				if (s.length() > 0)
					System.out.println(s.trim());
			} catch (Exception ex) {
			}
		}
	}

	public static class r_comment_lines extends insert_tab {
		public static final String COMMENT_CHAR = "#";

		public void actionPerformed(ActionEvent evt) {
			JEditTextArea textArea = getTextArea(evt);

			int startLine = textArea.getSelectionStartLine();
			int endLine = textArea.getSelectionEndLine();
			int so = textArea.getLineStartOffset(endLine);
			int ss = textArea.getSelectionEnd();

			if (so == ss)
				endLine--;

			if (startLine < 0)
				startLine = endLine = textArea.getCaretLine();

			for (int line = startLine; line <= endLine; line++) {
				int pos = textArea.getLineStartOffset(line);
				try {
					if (textArea.getLineText(line).trim().startsWith(COMMENT_CHAR)) {
						textArea.getDocument().remove(pos, textArea.getLineText(line).indexOf(COMMENT_CHAR) + 1);
					} else {
						textArea.getDocument().insertString(pos, COMMENT_CHAR, null);
					}
				} catch (BadLocationException e) {
					new ErrorMsg(e);
				}
			}
		}
	}

	public static class r_run_all extends insert_tab {
		public void actionPerformed(ActionEvent evt) {
			JEditTextArea textArea = getTextArea(evt);

			int startLine = 0;
			int endLine = textArea.getLineCount();

			for (int line = startLine; line < endLine; line++) {
				String lineText = textArea.getLineText(line).trim();
				if (lineText.length() > 0)
					System.out.println(lineText);
			}

		}
	}

	public static class r_close_popups implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			if (codeCompletion != null) {
				codeCompletion.hide();
				codeCompletion = null;
			}
			if (funHelpTip != null) {
				funHelpTip.hide();
				funHelpTip = null;
			}
		}
	}

	public static class r_next_line extends next_line {
		public r_next_line(boolean select) {
			super(select);
		}

		public void actionPerformed(ActionEvent evt) {
			if (codeCompletion != null)
				CodeCompletion.getInstance().next();
			else if (funHelpTip != null) {
				funHelpTip.hide();
				funHelpTip = null;
			} else
				super.actionPerformed(evt);

		}
	}

	public static class r_prev_line extends prev_line {
		public r_prev_line(boolean select) {
			super(select);
		}

		public void actionPerformed(ActionEvent evt) {
			if (codeCompletion != null)
				CodeCompletion.getInstance().previous();
			else if (funHelpTip != null) {
				funHelpTip.hide();
				funHelpTip = null;
			} else
				super.actionPerformed(evt);
		}
	}

	public static class r_insert_break extends insert_break {
		public void actionPerformed(ActionEvent evt) {
			JEditTextArea textArea = getTextArea(evt);

			if (!textArea.isEditable()) {
				textArea.getToolkit().beep();
				return;
			}

			if (codeCompletion != null) {
				String completion = CodeCompletion.getInstance().getCompletion();
				textArea.setSelectedText(completion);
				codeCompletion.hide();
				codeCompletion = null;
			} else if (funHelpTip != null) {
				funHelpTip.hide();
				funHelpTip = null;
			} else
				super.actionPerformed(evt);
		}
	}

	static {
		actions.put("insert-tab", R_INSERT_TAB);
		actions.put("run-lines", R_RUN_LINES);
		actions.put("run-all", R_RUN_ALL);
		actions.put("comment-lines", R_COMMENT_LINES);
		actions.put("prev-line", R_PREV_LINE);
		actions.put("next-line", R_NEXT_LINE);
		actions.put("close-popups", R_CLOSE_POPUPS);
		actions.put("insert-break", R_INSERT_BREAK);

	}

	public void addKeyBindings() {
		addDefaultKeyBindings();
		addKeyBinding("TAB", R_INSERT_TAB);
		addKeyBinding("M+ENTER", R_RUN_LINES);
		// addKeyBinding("MS+R", R_RUN_ALL);
		addKeyBinding("M+7", R_COMMENT_LINES);
		addKeyBinding("UP", R_PREV_LINE);
		addKeyBinding("DOWN", R_NEXT_LINE);
		addKeyBinding("ESCAPE", R_CLOSE_POPUPS);
		addKeyBinding("ENTER", R_INSERT_BREAK);
	}

	private String getLastCommand(KeyEvent evt) {
		if (funHelpTip != null)
			funHelpTip.hide();

		JEditTextArea textArea = getTextArea(evt);

		String text = textArea.getText();
		int pos = textArea.getCaretPosition();
		int lastb = textArea.getText(0, pos + 1).lastIndexOf('(');
		int lasteb = textArea.getText(0, pos).lastIndexOf(')');
		if (lasteb > lastb)
			return null;
		if (lastb < 0)
			return null;
		if (pos < 0)
			return null;
		int line, loffset, lend;
		try {
			line = textArea.getLineOfOffset(pos);
			loffset = textArea.getLineStartOffset(line);
			lend = textArea.getLineEndOffset(line);
			if (lastb > lend)
				return null;
		} catch (Exception e) {
			return null;
		}
		if (text.substring(loffset, pos).indexOf("#") >= 0)
			return null; // comment line
		int offset = lastb--, end = lastb;
		pos = lastb;
		if (text == null)
			return null;
		while (offset > -1 && pos > -1) {
			char c = text.charAt(pos);
			if ((c >= '0' && c <= '9') || ((c >= 'a') && (c <= 'z')) || ((c >= 'A') && (c <= 'Z')) || c == '.' || c == '_')
				offset--;
			else
				break;
			pos--;
		}
		offset = offset == -1 ? 0 : offset;
		try {
			line = textArea.getLineOfOffset(textArea.getCaretPosition());
			loffset = textArea.getLineStartOffset(line);
			lend = textArea.getLineEndOffset(line);
			if (offset < loffset || end > lend)
				return null;
		} catch (Exception e) {
			return null;
		}
		if (textArea.getCaretPosition() < offset)
			return null;
		end = ++lastb;
		return (offset != end) ? text.substring(offset, end).trim() : null;
	}

	public void keyReleased(KeyEvent evt) {
		if (funHelpTip != null) {
			funHelpTip.hide();
			funHelpTip = null;
		}

		if (codeCompletion != null && evt.getKeyCode() != KeyEvent.VK_UP && evt.getKeyCode() != KeyEvent.VK_DOWN) {
			JEditTextArea textArea = getTextArea(evt);

			int carPos = textArea.getCaretPosition();

			int line = textArea.getCaretLine();
			String lineStr = textArea.getLineText(line);
			int start = 0, end = carPos;
			try {
				start = TextUtilities.findWordStart(lineStr, carPos - textArea.getLineStartOffset(line) - 1, ".");
			} catch (StringIndexOutOfBoundsException ex) {
			}

			try {
				end = TextUtilities.findWordEnd(lineStr, carPos - textArea.getLineStartOffset(line) - 1, ".");
			} catch (StringIndexOutOfBoundsException ex) {
			}

			String pattern = null;

			try {
				pattern = lineStr.substring(start, end);
			} catch (StringIndexOutOfBoundsException ex) {
			}

			if (pattern == null || pattern.trim().length() == 0 || CodeCompletion.getInstance().updateList(pattern) < 1) {
				codeCompletion.hide();
				codeCompletion = null;
			}
		}
		super.keyReleased(evt);
	}

}