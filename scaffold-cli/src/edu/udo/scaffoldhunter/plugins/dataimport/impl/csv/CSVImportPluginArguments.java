/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * See README.txt in the root directory of the Scaffold Hunter source tree
 * for details.
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

package edu.udo.scaffoldhunter.plugins.dataimport.impl.csv;

/**
 * @author Bernhard Dick
 * 
 */
public class CSVImportPluginArguments {
    private String filename;
    private char separator;
    private char quotechar;
    private boolean strictQuotes;
    private boolean firstRowHeader;
    private int smilesColumnId;

    /**
     * 
     * @param filename
     * @param separator
     * @param quotechar
     * @param strictQuotes
     * @param firstRowHeader
     * @param smilesColumnId 
     */
    public CSVImportPluginArguments(String filename, char separator, char quotechar, boolean strictQuotes,
            boolean firstRowHeader, int smilesColumnId) {
        this.filename = filename;
        this.separator = separator;
        this.quotechar = quotechar;
        this.smilesColumnId = smilesColumnId;
        this.setStrictQuotes(strictQuotes);
        this.setFirstRowHeader(firstRowHeader);
    }

    /**
     * @param filename
     *            the filename to set
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @param separator
     *            the separator to set
     */
    public void setSeparator(char separator) {
        this.separator = separator;
    }

    /**
     * @return the separator
     */
    public char getSeparator() {
        return separator;
    }

    /**
     * @param quotechar
     *            the quotechar to set
     */
    public void setQuotechar(char quotechar) {
        this.quotechar = quotechar;
    }

    /**
     * @return the quotechar
     */
    public char getQuotechar() {
        return quotechar;
    }

    /**
     * @param strictQuotes
     *            the strictQuotes to set
     */
    public void setStrictQuotes(boolean strictQuotes) {
        this.strictQuotes = strictQuotes;
    }

    /**
     * @return the strictQuotes
     */
    public boolean isStrictQuotes() {
        return strictQuotes;
    }

    /**
     * @param firstRowHeader
     *            the firstRowHeader to set
     */
    public void setFirstRowHeader(boolean firstRowHeader) {
        this.firstRowHeader = firstRowHeader;
    }

    /**
     * @return the firstRowHeader
     */
    public boolean isFirstRowHeader() {
        return firstRowHeader;
    }

    /**
     * @param smilesColumnId
     *          the id of the column containing a SMILES
     */
    public void setSmilesColumnId(int smilesColumnId) {
        this.smilesColumnId = smilesColumnId;
    }

    /**
     * @return the id of the column containing a SMILES
     */
    public int getSmilesColumnId() {
        return smilesColumnId;
    }

}
