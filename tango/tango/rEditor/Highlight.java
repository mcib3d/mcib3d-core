package tango.rEditor;

import java.awt.Graphics;
import java.awt.event.MouseEvent;

public interface Highlight {

	/**
	 * Called after the highlight painter has been added.
	 * 
	 * @param textArea
	 *            The text area
	 * @param next
	 *            The painter this one should delegate to
	 */
	public abstract void init(JEditTextArea textArea, Highlight next);

	/**
	 * This should paint the highlight and delgate to the next highlight
	 * painter.
	 * 
	 * @param gfx
	 *            The graphics context
	 * @param line
	 *            The line number
	 * @param y
	 *            The y co-ordinate of the line
	 */
	public abstract void paintHighlight(Graphics gfx, int line, int y);

	/**
	 * Returns the tool tip to display at the specified location. If this
	 * highlighter doesn't know what to display, it should delegate to the
	 * next highlight painter.
	 * 
	 * @param evt
	 *            The mouse event
	 */
	public abstract String getToolTipText(MouseEvent evt);

	public abstract void removeHighlight(int start, int end);
	
	public abstract void addHighlight(int start, int end);

	public abstract void removeHighlights();
}