package tango.rEditor;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;

import javax.swing.JTextPane;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.plaf.ComponentUI;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;

/**
 * SyntaxArea - extends JTextPane and provides bracketmatching.
 * 
 * @author Markus Helbig RoSuDa 2003 - 2004
 */

public class SyntaxArea extends JTextPane implements CaretListener, DropTargetListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1025997191896098082L;
	private HighlightPainter ParanthesisHighlightMissing;
	private HighlightPainter ParanthesisHighlight;

	private boolean wrap = true;

	/** SyntaxArea, with highlighting matching brackets */
	public SyntaxArea() {
                this.setColors(Color.orange, Color.black);
		this.setContentType("text/rtf");
		this.setDocument(new SyntaxDocument());
		this.addCaretListener(this);
		// this.setTransferHandler(new TextTransferHandler());
		this.setDragEnabled(true);
	}
        
        public void setColors(Color ErrorColor, Color BracketColor){
            ParanthesisHighlightMissing = new HighlightPainter(ErrorColor);
            ParanthesisHighlight = new HighlightPainter(BracketColor);
        }
	/**
	 * Append text.
	 */
	public void append(String str) {
		append(str, null);
	}

	/**
	 * Append text with supplied attributeset.
	 */
	public void append(String str, AttributeSet attr) {
		try {
			Document doc = this.getDocument();
			doc.insertString(doc.getLength(), str, attr);
		} catch (BadLocationException e) {
			/***/
		}
	}

	/**
	 * Insert text at position.
	 */
	public void insertAt(int offset, String str) {
		try {
			Document doc = this.getDocument();
			doc.insertString(offset, str, null);
		} catch (BadLocationException e) {
			/***/
		}
	}

	/**
	 * Get text.
	 */
	public String getText() {
		try {
			Document doc = this.getDocument();
			return doc.getText(0, doc.getLength());
		} catch (BadLocationException e) {
			return null;
		}
	}

	/**
	 * Get text from offset with supplied length.
	 */
	public String getText(int offs, int len) {
		try {
			Document doc = this.getDocument();
			return doc.getText(offs, len);
		} catch (BadLocationException e) {
			return null;
		}
	}

	/**
	 * Set text.
	 */
	public void setText(String str) {
		try {
			Document doc = this.getDocument();
			doc.remove(0, doc.getLength());
			doc.insertString(0, str, null);
		} catch (BadLocationException e) {
			/***/
		}
	}

	/**
	 * Cut text.
	 */
	public void cut() {
		this.removeCaretListener(this);
		super.cut();
		// Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new
		// StringSelection(this.getSelectedText()),null);
		// this.replaceSelection("");
		this.addCaretListener(this);
	}

	/**
	 * Copy text.
	 */
	public void copy() {
		this.removeCaretListener(this);
		super.copy();
		this.addCaretListener(this);
	}

	/**
	 * Paste from clipboard.
	 */
	public void paste() {
		this.removeCaretListener(this);
		super.paste();
		this.addCaretListener(this);
	}

	/**
	 * Get amount of lines.
	 */
	protected int getLineCount() {
		Element map = getDocument().getDefaultRootElement();
		return map.getElementCount();
	}

	protected int getLineStartOffset(int line) throws BadLocationException {
		int lineCount = getLineCount();
		if (line < 0) {
			throw new BadLocationException("Negative line", -1);
		} else if (line >= lineCount) {
			throw new BadLocationException("No such line", getDocument().getLength() + 1);
		} else {
			Element map = getDocument().getDefaultRootElement();
			Element lineElem = map.getElement(line);
			return lineElem.getStartOffset();
		}
	}

	protected int getLineEndOffset(int line) throws BadLocationException {
		int lineCount = getLineCount();
		if (line < 0) {
			throw new BadLocationException("Negative line", -1);
		} else if (line >= lineCount) {
			throw new BadLocationException("No such line", getDocument().getLength() + 1);
		} else {
			Element map = getDocument().getDefaultRootElement();
			Element lineElem = map.getElement(line);
			int endOffset = lineElem.getEndOffset();
			return ((line == lineCount - 1) ? (endOffset - 1) : endOffset);
		}
	}

	protected int getLineOfOffset(int offset) throws BadLocationException {
		Document doc = getDocument();
		if (offset < 0) {
			throw new BadLocationException("Can't translate offset to line", -1);
		} else if (offset > doc.getLength()) {
			throw new BadLocationException("Can't translate offset to line", doc.getLength() + 1);
		} else {
			Element map = getDocument().getDefaultRootElement();
			return map.getElementIndex(offset);
		}
	}

	/**
	 * Set word wrap behavior.
	 * 
	 * @param wrap
	 *            true if wrap, false if not
	 */
	public void setWordWrap(boolean wrap) {
		this.wrap = wrap;
	}

	/**
	 * Set word wrap behavior.
	 * 
	 * @return true if wrap, false if not
	 */
	public boolean getWordWrap() {
		return this.wrap;
	}

	public boolean getScrollableTracksViewportWidth() {
		if (!wrap) {
			Component parent = this.getParent();
			ComponentUI ui1 = this.getUI();
			boolean bool = (parent != null) ? (ui1.getPreferredSize(this).width < parent.getSize().width) : true;
			return bool;
		}
		return super.getScrollableTracksViewportWidth();
	}

	public void setBounds(int x, int y, int width, int height) {
		if (wrap)
			super.setBounds(x, y, width, height);
		else {
			Dimension size = this.getPreferredSize();
			super.setBounds(x, y, Math.max(size.width, width), Math.max(size.height, height));
		}
	}

	/**
	 * Checks wether character is escaped.
	 * 
	 * @param pos
	 *            postion where to check
	 * @return true if escaped, false if not
	 */
	public boolean isEscaped(int pos) {
		boolean escaped = false;
		try {
			escaped = lastChar(pos - 1, "\\");
		} catch (Exception e) {
			escaped = false;
		}

		return escaped;
	}

	/**
	 * Check wether last character matches cont.
	 * 
	 * @param pos
	 *            postion
	 * @param cont
	 *            pattern
	 * @return true if matches, fals if not
	 */
	public boolean lastChar(int pos, String cont) {
		if (pos == 0) {
			return false;
		}
		if (this.getText(pos - 1, 1) != null && this.getText(pos - 1, 1).equals(cont)) {
			return true;
		}
		return false;
	}

	/**
	 * Highlight the corresponding brackets (forward).
	 * 
	 * @param par
	 *            String which bracket
	 * @param pos
	 *            int current position
	 */
	public void highlightParanthesisForward(String par, int pos) throws BadLocationException {
		int open = pos;
		int cend = this.getText().length();

		String end = null;

		if (par.equals("{")) {
			end = "}";
		}
		if (par.equals("(")) {
			end = ")";
		}
		if (par.equals("[")) {
			end = "]";
		}

		if (end == null)
			return;

		String cchar = null;

		int pcount = 1;

		int line = this.getLineOfOffset(open);
		int lend = this.getLineEndOffset(line);

		while (++pos <= cend) {
			cchar = this.getText(pos - 1, 1);
			if (cchar.matches("\"") && !isEscaped(pos)) {
				boolean found = true;
				int i = pos;
				while (++i <= lend) {
					found = false;
					String schar = this.getText(i - 1, 1);
					if (schar.equals("\"") && !isEscaped(i)) {
						pos = i;
						found = true;
						break;
					}
				}
				if (!found)
					return;
			} else if (cchar.matches("[(]|[\\[]|[{]") && !isEscaped(pos))
				pcount++;
			else if (cchar.matches("[)]|[\\]]|[}]") && !isEscaped(pos)) {
				pcount--;
				if (pcount == 0) {
					if (cchar.equals(end)) {
						highlight(this, par, open, ParanthesisHighlight);
						highlight(this, end, pos, ParanthesisHighlight);
					} else {
						highlight(this, par, open, ParanthesisHighlightMissing);
						highlight(this, end, pos, ParanthesisHighlightMissing);
					}
					return;
				}
			}
		}
	}

	/**
	 * Highlight the corresponding brackets (backward).
	 * 
	 * @param par
	 *            String which bracket
	 * @param pos
	 *            int current position
	 */

	public void highlightParanthesisBackward(String par, int pos) throws BadLocationException {

		int end = pos;

		String open = null;

		if (par.equals("}")) {
			open = "{";
		}
		if (par.equals(")")) {
			open = "(";
		}
		if (par.equals("]")) {
			open = "[";
		}

		if (open == null)
			return;

		String cchar = null;

		int pcount = 1;

		int line = this.getLineOfOffset(end);
		int lstart = this.getLineStartOffset(line);
		while (--pos > 0) {
			cchar = this.getText(pos - 1, 1);
			if (cchar.matches("\"") && !isEscaped(pos)) {
				boolean found = true;
				int i = pos;
				while (--i > lstart) {
					found = false;
					String schar = this.getText(i - 1, 1);
					if (schar.equals("\"") && !isEscaped(i)) {
						pos = i;
						found = true;
						break;
					}
				}
				if (!found)
					return;
			} else if (cchar.matches("[)]|[\\]]|[}]") && !isEscaped(pos))
				pcount++;
			else if (cchar.matches("[(]|[\\[]|[{]") && !isEscaped(pos)) {
				pcount--;
				if (pcount == 0) {
					if (cchar.equals(open)) {
						highlight(this, par, end, ParanthesisHighlight);
						highlight(this, open, pos, ParanthesisHighlight);
					} else {
						highlight(this, par, end, ParanthesisHighlightMissing);
						highlight(this, open, pos, ParanthesisHighlightMissing);
					}
					return;
				}
			}
		}
	}

	/**
	 * Highlight pattern at position.
	 * 
	 * @param textComp
	 *            textcomponent
	 * @param pattern
	 *            pattern
	 * @param pos
	 *            position
	 * @param hipainter
	 *            highlightpainter
	 */
	public void highlight(JTextComponent textComp, String pattern, int pos, HighlightPainter hipainter) {
		try {
			Highlighter hilite = textComp.getHighlighter();
			if (pos == 0) {
				pos++;
			}
			hilite.addHighlight(pos - 1, pos, hipainter);
		} catch (BadLocationException e) {
			/***/
		}
	}

	/**
	 * Remove current highlights.
	 */
	public void removeHighlights() {
		Highlighter hilite = this.getHighlighter();
		Highlighter.Highlight[] hilites = hilite.getHighlights();

		for (int i = 0; i < hilites.length; i++) {
			if (hilites[i].getPainter() instanceof HighlightPainter) {
				hilite.removeHighlight(hilites[i]);
			}
		}
	}

	/**
	 * caretUpdate: handle caret event: if it was a bracket, highlight the
	 * matching one if there is one.
	 */
	public void caretUpdate(final CaretEvent e) {
		final SyntaxArea sa = this;
		removeHighlights();
		try {
			if (e.getDot() == 0)
				return;
			if (getText(e.getDot() - 1, 1).matches("[(]|[\\[]|[{]|[)]|[\\]]|[}]")) /*
																					 * t.
																					 * start
																					 * (
																					 * )
																					 * ;
																					 */{

				removeCaretListener(sa);
				String c;
				int pos;
				try {
					pos = e.getDot();
					c = getText(pos - 1, 1);
					if (sa.isEscaped(pos)) {
						addCaretListener(sa);
						return;
					}
				} catch (Exception ex1) {
					new ErrorMsg(ex1);
					addCaretListener(sa);
					return;
				}
				try {
					if (c.matches("[(]|[\\[]|[{]"))
						highlightParanthesisForward(c, pos);
					else if (c.matches("[)]|[\\]]|[}]"))
						highlightParanthesisBackward(c, pos);
				} catch (Exception ex2) {
					new ErrorMsg(ex2);
				}
				addCaretListener(sa);
			}
		} catch (Exception ex3) {
			new ErrorMsg(ex3);
		}
	}

	/**
	 * dragEnter: handle drag event.
	 */
	public void dragEnter(DropTargetDragEvent evt) {
	}

	/**
	 * dragOver: handle drag event.
	 */
	public void dragOver(DropTargetDragEvent evt) {
	}

	/**
	 * dragExit: handle drag event.
	 */
	public void dragExit(DropTargetEvent evt) {
	}

	/**
	 * dropActionChanged: handle drop event.
	 */
	public void dropActionChanged(DropTargetDragEvent evt) {
	}

	/**
	 * drop: handle drop target event: insert string representation of dragged
	 * object.
	 */
	public void drop(DropTargetDropEvent evt) {
		try {
			Transferable t = evt.getTransferable();

			if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				evt.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				evt.getDropTargetContext().dropComplete(true);
			} else {
				evt.rejectDrop();
			}
		} catch (Exception e) {
			evt.rejectDrop();
		}
	}

	class HighlightPainter extends DefaultHighlighter.DefaultHighlightPainter {
		public HighlightPainter(Color color) {
			super(color);
		}
	}

}
