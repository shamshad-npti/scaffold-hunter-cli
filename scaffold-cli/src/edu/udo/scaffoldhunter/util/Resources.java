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

package edu.udo.scaffoldhunter.util;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.udo.scaffoldhunter.view.util.SVG;

/**
 * A utility class with static methods to easily access different kinds of
 * resources in the edu.udo.scaffoldhunter.resources package.
 * 
 * @author Dominic Sacré
 */
public class Resources {

    private static final Logger logger = LoggerFactory.getLogger(Resources.class);

    private static final String RESOURCE_PATH = "edu/udo/scaffoldhunter/resources/";

    /**
     * @param name
     *            a filename relative to or
     *            edu/udo/scaffoldhunter/resources/icons/16x16
     * @return the ImageIcon constructed from the given resource
     */
    public static ImageIcon getIcon(String name) {
        URL url = getResource("icons/16x16/" + name);
        if (url != null) {
            return new ImageIcon(url);
        } else {
            logger.error("icon '16x16/{}' is missing", name);
            return new ImageIcon(getResource("icons/16x16/image-missing.png"));
        }
    }

    /**
     * @param name
     *            a filename relative to
     *            edu/udo/scaffoldhunter/resources/icons/22x22
     * @return the ImageIcon constructed from the given resource
     */
    public static ImageIcon getLargeIcon(String name) {
        URL url = getResource("icons/22x22/" + name);
        if (url != null) {
            return new ImageIcon(url);
        } else {
            logger.error("icon '22x22/{}' is missing", name);
            return new ImageIcon(getResource("icons/22x22/image-missing.png"));
        }
    }

    /**
     * @param name
     *            a filename relative to edu/udo/scaffoldhunter/resources
     * @return the ImageIcon constructed from the given resource
     */
    public static ImageIcon getImageIcon(String name) {
        return new ImageIcon(getResource(name));
    }

    /**
     * @param name
     *            a filename relative to edu/udo/scaffoldhunter/resources
     * @return the BufferedImage constructed from the given resource
     */
    public static BufferedImage getBufferedImage(String name) {
        try {
            return ImageIO.read(getResource(name));
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Construct a SVG from a resource. The SVG is <b>not</b> constructed in a
     * background thread. No caching is performed.
     * 
     * @param name
     *            a filename relative to edu/udo/scaffoldhunter/resources
     * @return a SVG constructed from the given resource
     */
    public static SVG getSVG(String name) {
        Reader svgReader = null;
        try {
            InputStream svgStream = getResource("images/" + name).openStream();
            svgReader = new BufferedReader(new InputStreamReader(svgStream, Charset.forName("UTF-8")));
            return new SVG(svgReader);
        } catch (IOException e) {
            return null;
        } finally {
            if (svgReader != null) {
                try {
                    svgReader.close();
                } catch (IOException e) {
                    logger.error("Could not close SVG reader");
                }
            }
        }

    }

    /**
     * Loads a properties file. If no file is found at the specified path, or
     * reading the file fails and empty <code>Properties</code> Object will be
     * returned.
     * 
     * @param name
     *            a filename relative to edu/udo/scaffoldhunter/resources
     * @return a properties object containing the values in the file, or an
     *         empty properties object if the file could not be read.
     */
    public static Properties getProperties(String name) {
        Properties prop = new Properties();
        InputStream s = Resources.class.getClassLoader().getResourceAsStream(RESOURCE_PATH + name);
        if (s == null) {
            return prop;
        }
        try {
            prop.load(s);
        } catch (IOException e) {
            return new Properties();
        } finally {
            try {
                s.close();
            } catch (IOException e) {
                logger.error("could not close properties stream", e);
            }
        }
        return prop;
    }

    private static URL getResource(String name) {
        return Resources.class.getClassLoader().getResource(RESOURCE_PATH + name);
    }
}
