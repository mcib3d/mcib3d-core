package tango.gui.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 *
 **
 * /**
 * Copyright (C) 2012 Jean Ollion
 *
 *
 *
 * This file is part of tango
 *
 * tango is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jean Ollion
 */

public class DocumentFilterIllegalCharacters extends DocumentFilter {


    private char[] ILLEGAL_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':', '.', ';', ',', ' ', '-'};
    private char[] ILLEGAL_CHARACTERS_START = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    
    public DocumentFilterIllegalCharacters(char[] illegalCharacters, char[] illegalCharactersStart) {
        if (illegalCharacters!=null) ILLEGAL_CHARACTERS=illegalCharacters;
        if (illegalCharactersStart!=null) ILLEGAL_CHARACTERS_START=illegalCharactersStart;
    }
    
    public DocumentFilterIllegalCharacters() {
    }
    
    
    @Override
    public void insertString (DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException
    {
        fb.insertString (offset, fixText(offset, text).toUpperCase(), attr);
    }
    @Override
    public void replace (DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attr) throws BadLocationException
    {
        fb.replace(offset, length, fixText(offset, text), attr);
    }

    private String fixText (int offset, String s)
    {
        StringBuilder sb = new StringBuilder();
        if (offset==0) {
        if (!isIllegalFileNameCharStart(s.charAt(0)) && !isIllegalFileNameChar (s.charAt (0)))
                sb.append (s.charAt (0));
        }
        for(int i = offset==0?1:0; i < s.length(); ++i)
        {
            if (!isIllegalFileNameChar (s.charAt (i)))
                sb.append (s.charAt (i));
        }
        return sb.toString();
    }

    private boolean isIllegalFileNameChar (char c)
    {
        for (int i = 0; i < ILLEGAL_CHARACTERS.length; i++)
        {
            if (c == ILLEGAL_CHARACTERS[i])
                return true;
        }

        return false;
    }
    private boolean isIllegalFileNameCharStart (char c)
    {
        for (int i = 0; i < ILLEGAL_CHARACTERS_START.length; i++)
        {
            if (c == ILLEGAL_CHARACTERS_START[i])
                return true;
        }

        return false;
    }

}
