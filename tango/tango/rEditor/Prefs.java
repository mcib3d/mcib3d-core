/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tango.rEditor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author musicien
 */

public class Prefs {
    static ArrayList<String> KEYWORDS = new ArrayList<String>(Arrays.asList(
            "print", 
            "cat"
    ));
    static ArrayList<String> OBJECTS = new ArrayList<String>(Arrays.asList(
            "experiment",
            "nucleus"
    ));
    static boolean AUTOTAB = true;
    static int tabWidth = 100;
    static Color QUOTEColor = Color.GREEN;
    static Color KEYWORDColor= Color.MAGENTA;
    static Color COMMENTColor=Color.GRAY;
    static Color OBJECTColor=Color.ORANGE;
    static boolean KEYWORD_BOLD=true;
    static boolean COMMENT_IT=true;
    static boolean OBJECT_IT=true;
    static Color HIGHLIGHTColor=Color.YELLOW;
    static boolean LINE_HIGHLIGHT=true;
    static Color BRACKETHighLight=Color.LIGHT_GRAY;
    static boolean LINE_NUMBERS=true;
}


