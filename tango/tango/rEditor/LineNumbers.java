package tango.rEditor;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.text.Element;
import javax.swing.text.JTextComponent;

public class LineNumbers extends JComponent implements AdjustmentListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2196475189826669853L;

	private Dimension d = new Dimension();

	private boolean showing = true;

	private int BAR = 4;

	private JTextComponent src;

	private JScrollPane scroller;

	private Dimension sizeCache = new Dimension();

	private Point locCache = new Point();

	public LineNumbers(JTextComponent src, JScrollPane scroller) {
		super();
		this.src = src;
		this.scroller = scroller;
		addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (showing)
					hideBar();
				else
					showBar();
			}
		});
		scroller.getVerticalScrollBar().addAdjustmentListener(this);
		super.setFont(src.getFont());
		setBorder(BorderFactory.createRaisedBevelBorder());
	}

	public void adjustmentValueChanged(AdjustmentEvent ae) {
		scroller.validate();
	}

	private void hideBar() {
		showing = false;
		scroller.setRowHeaderView(this);
	}

	private void showBar() {
		showing = true;
		scroller.setRowHeaderView(this);
	}

	private void paintNumbers(Graphics g) {

		g.setColor(UIManager.getColor("InternalFrame.activeTitleBackground"));

		Rectangle r = g.getClipBounds();

		Insets insets = getBorder().getBorderInsets(this);

		// adjust the clip
		// trim the width by border insets
		r.width -= insets.right + insets.left;
		// slide the clip over by the left insets
		r.x += insets.left;
		// we never trimmed the top or bottom.
		// this will paint over the border.

		((Graphics2D) g).fill(r);

		int ascent = getFontMetrics(getFont()).getAscent();
		int h = getFontMetrics(getFont()).getHeight();
		int y = (int) (r.getY() / h) * h;
		int max = (int) (r.getY() + r.getHeight()) / h;

		g.setColor(UIManager.getColor("Label.foreground"));
		for (int i = (int) Math.floor(y / h) + 1; i <= max + 1; i++) {
			g.drawString(i + "", insets.left, y + ascent);
			y += h;
		}
	}

	public Dimension getPreferredSize() {
		d.width = getMyWidth();
		d.height = src.getHeight();
		return d;
	}

	public int getLineCount() {
		Element map = src.getDocument().getDefaultRootElement();
		return map.getElementCount();
	}

	private int getMyWidth() {
		FontMetrics fm = src.getFontMetrics(src.getFont());
		return showing ? fm.stringWidth(getVisibleEndLine() + "") + 4 + BAR : BAR;
	}

	public int getVisibleStartLine() {
		scroller.getViewport().getView().getLocation(locCache);
		int h = getFontMetrics(getFont()).getHeight();
		int y = (int) (locCache.getY() / h) * h;
		return (int) Math.floor(y / h) + 1;
	}

	public int getVisibleEndLine() {
		scroller.getViewport().getView().getLocation(locCache);
		scroller.getViewport().getSize(sizeCache);
		int h = getFontMetrics(getFont()).getHeight();
		return (int) Math.abs(-locCache.getY() + sizeCache.getHeight()) / h;
	}

	public void paint(Graphics g) {
		// draw the border one pixel bigger in height so bottom left bevel
		// can look like it doesn't turn.
		// we will paint over the top and bottom center portions of the border
		// in paintNumbers
		getBorder().paintBorder(this, g, 0, 0, d.width, d.height + 1);
		if (showing)
			paintNumbers(g);
	}
}
