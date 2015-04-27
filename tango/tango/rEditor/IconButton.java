package tango.rEditor;

// JGR - Java Gui for R, see http://www.rosuda.org/JGR/
// Copyright (C) 2003 - 2005 Markus Helbig
// --- for licensing information see LICENSE file in the original JGR
// distribution ---

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**
 * IconButton - special button with an icon for JGR toolbars.
 * 
 * @author Markus Helbig RoSuDa 2003 - 2005
 */

public class IconButton extends JButton implements MouseListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 999558047052505650L;

	/**
	 * Create a button whith icon on it.
	 * 
	 * @param iconUrl
	 *            url to icon
	 * @param tooltip
	 *            Tooltip
	 * @param al
	 *            ActionListener
	 * @param cmd
	 *            ActionCommand
	 */
	public IconButton(String iconUrl, String tooltip, ActionListener al, String cmd) {
		ImageIcon icon = null;
		try {
			URL url = getClass().getResource(iconUrl);
			Image img = ImageIO.read(url);
			icon = new ImageIcon(img);
			this.setIcon(icon);
			this.setMinimumSize(new Dimension(26, 26));
			this.setPreferredSize(new Dimension(26, 26));
			this.setMaximumSize(new Dimension(26, 26));
		} catch (Exception e) {
			this.setText(tooltip);
		}
		this.setActionCommand(cmd);
		this.setBorder(BorderFactory.createEmptyBorder());
		this.setToolTipText(tooltip);
		this.addActionListener(al);
		this.addMouseListener(this);
	}

	/**
	 * mouseClicked: handle mouse event.
	 */
	public void mouseClicked(MouseEvent e) {
	}

	/**
	 * mouseEntered: handle mouse event: show border when mouse is over this
	 * icon.
	 */
	public void mouseEntered(MouseEvent e) {
		this.setBorder(BorderFactory.createEtchedBorder());
	}

	/**
	 * mousePressed: handle mouse event.
	 */
	public void mousePressed(MouseEvent e) {
	}

	/**
	 * mouseReleased: handle mouse event.
	 */
	public void mouseReleased(MouseEvent e) {
	}

	/**
	 * mouseExited: handle mouse event: border should now disappear when mouse
	 * isn't over icon.
	 */
	public void mouseExited(MouseEvent e) {
		this.setBorder(BorderFactory.createEmptyBorder());
	}
}