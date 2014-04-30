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

package edu.udo.scaffoldhunter.util;

import java.io.File;
import java.util.Arrays;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.google.common.base.Joiner;

/**
 * @author Henning Garus
 * 
 */
public enum FileType {

    /** Portable Network Graphics */
    PNG(new String[] { "png" }, "Portable Network Graphics"),
    /** Scalable Vector Graphics */
    SVG(new String[] { "svg" }, "Scalable Vector Graphics");

    private final String[] extensions;
    private final String name;

    private FileType(String[] extensions, String name) {
        this.extensions = extensions;
        this.name = name;
    }

    /**
     * 
     * @return a Filter for the File Type
     */
    public FileFilter getFileFilter() {
        return new FileNameExtensionFilter(toString(), extensions);
    }

    /**
     * Add the file types' extension to a file
     * 
     * @param file
     *            the original file
     * @return if the file's path is missing the file types extension a new file
     *         will be returned with the same path as the old one and an added
     *         extension.
     */
    public File addExtension(File file) {
        String absolutePath = file.getAbsolutePath();
        for (String extension : extensions) {
            if (absolutePath.toLowerCase().endsWith(extension))
                return file;
        }
        return new File(absolutePath + "." + extensions[0]);
    }

    /**
     * Determine the File type belonging to a file filter
     * <p>
     * This method is only guaranteed to work for FileFilters constructed by
     * {@link #getFileFilter}
     * 
     * @param filter
     *            a file filter
     * @param defaultValue
     *            the default value which will be returned if no file type
     *            belongs to the given filter
     * @return the file type corresponding to the filter or the default value if
     *         there is none
     */
    public static FileType getFileType(FileFilter filter, FileType defaultValue) {
        if (filter instanceof FileNameExtensionFilter) {
            FileNameExtensionFilter f = (FileNameExtensionFilter) filter;
            for (FileType type : values()) {
                if (Arrays.equals(type.extensions, f.getExtensions())) {
                    return type;
                }
            }
        }
        return defaultValue;
    }

    /**
     * Determine the file type corresponding to an extension.
     * 
     * @param extension
     *            a file extension, without a dot
     * @param defaultValue
     *            the default value to be returned if no file type corresponds
     *            to the extension
     * @return the file type corresponding to the extension, if there is none
     *         the default value will be returned.
     */
    public static FileType getFileType(String extension, FileType defaultValue) {
        for (FileType type : values()) {
            if (Arrays.asList(type.extensions).contains(extension)) {
                return type;
            }
        }
        return defaultValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        StringBuilder b = new StringBuilder();
        b.append(name);
        b.append(" (*.");
        Joiner.on(", *.").appendTo(b, extensions);
        b.append(')');
        return b.toString();
    }

}
