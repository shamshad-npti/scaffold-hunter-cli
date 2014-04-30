/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * See the file README.txt in the root directory of the Scaffold Hunter
 * source tree for details.
 *
 * Scaffold Hunter is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * Scaffold Hunter is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package edu.udo.scaffoldhunter.gui.util;

import javax.swing.Icon;
import javax.swing.JLabel;

/**
 * @author Philipp Lewe
 * 
 */
public class WordWrapLabel extends JLabel {
    private String text;
    private int maxWidth = 600;

    /**
     * @param text
     */
    public WordWrapLabel(String text) {
        super();
        setText(text);
    }

    /**
     * @param image
     */
    public WordWrapLabel(Icon image) {
        super(image);
    }

    /**
     * @param text
     * @param horizontalAlignment
     */
    public WordWrapLabel(String text, int horizontalAlignment) {
        super(text, horizontalAlignment);
        setText(text);
    }

    /**
     * @param image
     * @param horizontalAlignment
     */
    public WordWrapLabel(Icon image, int horizontalAlignment) {
        super(image, horizontalAlignment);
    }

    /**
     * @param text
     * @param icon
     * @param horizontalAlignment
     */
    public WordWrapLabel(String text, Icon icon, int horizontalAlignment) {
        super(icon, horizontalAlignment);
        setText(text);
    }

    @Override
    public void setText(String text) {
        this.text = text;
        super.setText(String.format("<html><table><td width=\"%s\">%s</td></html>", maxWidth, this.text));
    }

    /**
     * Sets the maximum width of the label. If the text of the label is longer
     * than this width, the text is wrapped into a new line
     * 
     * @param width
     */
    public void setMaximumWidth(int width) {
        maxWidth = width;
    }

    /**
     * Returns the original, unformatted text
     * 
     * @return the text
     */
    public String getUnformattedText() {
        return text;
    }

}
