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

import java.awt.Insets;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.event.HyperlinkListener;

/**
 * Custom label implementation which smartly identifies URLs in its text and
 * presents them as clickable hyperlinks. Hyperlinks are opened in the default
 * browser by using {@link UrlOpener#browse}:
 * 
 * @author Philipp Lewe
 * 
 */
public class HyperlinkedLabel extends JEditorPane {
    private String text;
    private static final Pattern p = Pattern.compile("(http|https|ftp|ftps)://[^ ]*");

    /**
     * Creates a new HyperlinkedLabel with the given text
     * 
     * @param text
     *            the text to be shown
     */
    public HyperlinkedLabel(String text) {
        setContentType("text/html");
        setText(text);
        setMargin(new Insets(0, 0, 0, 0));
        setEditable(false);
        addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                // if clicked, open url in browser
                if (e.getEventType() == EventType.ACTIVATED) {
                    UrlOpener.browse(e.getURL());
                }
            }
        });
    }
    
    @Override
    public void setText(String text) {
        this.text = text;
        String t = String.format("<html>%s</html>", p.matcher(text).replaceAll("<a href=\"$0\">$0</a>"));
        super.setText(t);
        super.setToolTipText(t);
    }

    /**
     * Returns the original text without the wrapping html tags around urls
     * @return the original text
     */
    public String getOriginalText() {
        return text;
    }
}
