package tango.rEditor;

import java.awt.Color;
import java.util.HashMap;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class EditorPreferences {

	public static final MutableAttributeSet NUMBER = new SimpleAttributeSet();
	public static final MutableAttributeSet OBJECT = new SimpleAttributeSet();
	public static final MutableAttributeSet KEYWORD = new SimpleAttributeSet();
	public static final HashMap KEYWORDS_OBJECTS = new HashMap(); 
	public static int tabWidth = 4;
	public static final String SINGLELINECOMMENT = "#";
	public static final HashMap KEYWORDS = new HashMap();
	public static final String QUOTESTRING = "\"'";
	public static final String DELIMITERS = ",;:{}()[]+-/%<=>!&|^~*$";
	public static final MutableAttributeSet NORMAL = new SimpleAttributeSet();
	public static final MutableAttributeSet COMMENT = new SimpleAttributeSet();
	public static final MutableAttributeSet QUOTE = new SimpleAttributeSet();
	
	public static void initialize() {
        StyleConstants.setForeground(NORMAL, Color.black);
        StyleConstants.setFontSize(NORMAL,12);
        StyleConstants.setForeground(NUMBER, Color.red);
        StyleConstants.setForeground(COMMENT, Color.green);
        StyleConstants.setForeground(KEYWORD, Color.blue);
        StyleConstants.setBold(KEYWORD, true);
        StyleConstants.setForeground(OBJECT, Color.cyan);
        StyleConstants.setItalic(OBJECT, true);
        StyleConstants.setForeground(QUOTE, Color.orange);
        
        KEYWORDS.put("int",new Object());
        KEYWORDS.put("double",new Object());
        KEYWORDS.put("float",new Object());
        KEYWORDS.put("byte",new Object());
        
        KEYWORDS_OBJECTS.put("area",new Object());
        KEYWORDS_OBJECTS.put("pane",new Object());
        KEYWORDS_OBJECTS.put("label",new Object());
	}
	
}
