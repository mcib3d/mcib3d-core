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


    private char[] ILLEGAL_CHARACTERS = {'/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':', '.', ';', ',', ' '};
    
    public DocumentFilterIllegalCharacters(char[] illegalCharacters) {
        if (illegalCharacters!=null) ILLEGAL_CHARACTERS=illegalCharacters;
    }
    
    public DocumentFilterIllegalCharacters() {
    }
    
    
    @Override
    public void insertString (DocumentFilter.FilterBypass fb, int offset, String text, AttributeSet attr) throws BadLocationException
    {
        fb.insertString (offset, fixText(text).toUpperCase(), attr);
    }
    @Override
    public void replace (DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attr) throws BadLocationException
    {
        fb.replace(offset, length, fixText(text), attr);
    }

    private String fixText (String s)
    {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < s.length(); ++i)
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

}
