package tango.rEditor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionListener;

import javax.swing.JPanel;
import javax.swing.JToolBar;

public class EditToolbar extends JToolBar {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8598025061970727075L;
        private boolean isWindows;
	
	public EditToolbar(Frame f, ActionListener al) {
		Dimension bSize = new Dimension(30,30);

		IconButton newButton = new IconButton("/icons/new.png", "New", al, "new");
		IconButton openButton = new IconButton("/icons/open.png", "Open", al, "open");
		IconButton saveButton = new IconButton("/icons/save.png", "Save", al, "save");
		IconButton undoButton = new IconButton("/icons/undo.png", "Undo", al, "undo");
		IconButton redoButton = new IconButton("/icons/redo.png", "Redo", al, "redo");
		IconButton cutButton = new IconButton("/icons/cut.png", "Cut", al, "cut");
		IconButton copyButton = new IconButton("/icons/copy.png", "Copy", al, "copy");
		IconButton pasteButton = new IconButton("/icons/paste.png", "Paste", al, "paste");
		IconButton findButton = new IconButton("/icons/find.png", "Search", al, "search");
		IconButton helpButton = new IconButton("/icons/help.png", "Help", al, "help");
		
		newButton.setMaximumSize(bSize);
		openButton.setMaximumSize(bSize);
		saveButton.setMaximumSize(bSize);
		undoButton.setMaximumSize(bSize);
		redoButton.setMaximumSize(bSize);
		cutButton.setMaximumSize(bSize);
		copyButton.setMaximumSize(bSize);
		pasteButton.setMaximumSize(bSize);
		findButton.setMaximumSize(bSize);
		helpButton.setMaximumSize(bSize);
		
		newButton.setPreferredSize(bSize);
		openButton.setPreferredSize(bSize);
		saveButton.setPreferredSize(bSize);
		undoButton.setPreferredSize(bSize);
		redoButton.setPreferredSize(bSize);
		cutButton.setPreferredSize(bSize);
		copyButton.setPreferredSize(bSize);
		pasteButton.setPreferredSize(bSize);
		findButton.setPreferredSize(bSize);
		helpButton.setPreferredSize(bSize);
		
		newButton.setMinimumSize(bSize);
		openButton.setMinimumSize(bSize);
		saveButton.setMinimumSize(bSize);
		undoButton.setMinimumSize(bSize);
		redoButton.setMinimumSize(bSize);
		cutButton.setMinimumSize(bSize);
		copyButton.setMinimumSize(bSize);
		pasteButton.setMinimumSize(bSize);
		findButton.setMinimumSize(bSize);
		helpButton.setMinimumSize(bSize);
		
		if(isWindows){
			newButton.setContentAreaFilled(false);
			openButton.setContentAreaFilled(false);
			saveButton.setContentAreaFilled(false);
			undoButton.setContentAreaFilled(false);
			redoButton.setContentAreaFilled(false);
			cutButton.setContentAreaFilled(false);
			copyButton.setContentAreaFilled(false);
			pasteButton.setContentAreaFilled(false);
			findButton.setContentAreaFilled(false);
			helpButton.setContentAreaFilled(false);
		}
		
		this.add(new Spacer(10));
		this.add(newButton);
		this.add(openButton);
		this.add(saveButton);
		this.add(new Spacer(20));
		this.add(undoButton);
		this.add(redoButton);
		this.add(new Spacer(20));
		this.add(cutButton);
		this.add(copyButton);
		this.add(pasteButton);
		this.add(new Spacer(20));
		this.add(findButton);
		this.add(helpButton);
		f.add(this,BorderLayout.NORTH);
	}
	
	class Spacer extends JPanel {

		private static final long serialVersionUID = 4515920574842835717L;

		public Spacer(int width) {
			this.setMinimumSize(new Dimension(width, 0));
			this.setMaximumSize(new Dimension(width, 0));
			this.setPreferredSize(new Dimension(width, 0));
		}
	}

}

