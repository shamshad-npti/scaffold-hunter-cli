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

package edu.udo.scaffoldhunter.view.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.GVTBuilder;
import org.apache.batik.bridge.UserAgentAdapter;
import org.apache.batik.dom.svg.SAXSVGDocumentFactory;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.swing.gvt.JGVTComponent;
import org.apache.batik.util.XMLResourceDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGDocument;
import org.w3c.dom.svg.SVGSVGElement;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import edu.udo.scaffoldhunter.model.db.DatabaseException;
import edu.udo.scaffoldhunter.model.db.DbManager;
import edu.udo.scaffoldhunter.model.db.Structure;
import edu.udo.scaffoldhunter.model.util.SVGGen;

/**
 * A Wrapper for Batik's {@link GraphicsNode} and {@link SVGDocument}. Provides
 * support for background loading. Allows to change specific SVG properties such
 * as color.
 * 
 * Note that this class is not thread-safe. All synchronous loading must be done
 * in the same thread!
 */
public class SVG {

    private static final Logger logger = LoggerFactory.getLogger(SVG.class);

    /* Objects used for creating a new SVG from a String representation */
    private static final String parser = XMLResourceDescriptor.getXMLParserClassName();
    private static final SAXSVGDocumentFactory docFactory = new SAXSVGDocumentFactory(parser);
    /* this instance is used for synchronous loading only */
    private static final SAXSVGDocumentFactory docFactorySync = new SAXSVGDocumentFactory(parser);
    private static GVTBuilder gvtBuilder = new GVTBuilder();

    /** {@link Executor} used for SVG loading */
    private static Executor svgExecutor = Executors.newSingleThreadExecutor();

    /** SVG to display when a requested svg cannot be loaded from the database */
    private static final String errorSVGStr = "<svg xmlns='http://www.w3.org/2000/svg' width='200' height='200' > "
            + "<rect fill='orangered' x='0' y='0' width='200' height='200'>  " + "</rect> </svg>";
    private static SVGDocument errorDocument = null;

    private final Structure structure;
    private final Color color;
    private final Color background;
    private final boolean coloredChars;
    private List<WeakReference<SVGLoadObserver>> observers = new CopyOnWriteArrayList<WeakReference<SVGLoadObserver>>();
    private JGVTComponent component = null;
    private volatile GraphicsNode graphicsnode = null;

    // from VISControl
    // TODO constant or not constant and configureable?

    private final static double NODE_STROKE_WIDTH = 2;

    /**
     * Create a new <code>SVG</code> with the given color and the specified
     * observer. If <code>color == null</code> the colors will be those given by
     * the <code>Structure</code>'s SVG String.
     * <p>
     * <code>SVG</code>s are loaded in a background thread, so the load is not
     * necessarily finished when the constructor returns. The <code>
     * SVGLoadObserver</code> will be notified on the AWT Event Dispatching
     * thread, once loading is finished.
     * 
     * @param db
     *            the DB manager
     * @param structure
     *            the structure, which this <code>SVG</code> visualizes
     * @param color
     *            the color, which will be applied to this <code>SVG</code>
     *            before it is painted. If color is <code>null</code> no color
     *            will be applied.
     * @param background
     *            the svg background, if it is <code>null</code> the background
     *            will be invisible
     * @param coloredChars 
     *            <code>true</code> if the characters denoting some molecules 
     *            should be colored, otherwise these characters have the same
     *            color as the bonds   
     * @param observer
     *            the observer, which will be notified when operations which
     *            change how the <code>SVG</code> is painted (such as loading
     *            and coloring) are finished.
     * @param inBackGround 
     *            load the svg asynchronously
     */
    public SVG(DbManager db, Structure structure, Color color, Color background, boolean coloredChars, SVGLoadObserver observer, boolean inBackGround) {
        Preconditions.checkNotNull(structure);
        this.structure = structure;
        this.color = color;
        this.background = background;
        this.coloredChars = coloredChars;
        if (observer != null)
            observers.add(new WeakReference<SVGLoadObserver>(observer));
        if (inBackGround) {
            svgExecutor.execute(new SVGLoader(db, docFactory));
        } else {
            new SVGLoader(db, docFactorySync).run();
        }
    }

    /**
     * Create a new SVG from the content provided by a Reader.
     * <p>
     * SVG created using this constructor are not created on a background
     * thread.
     * 
     * @param reader
     *            a reader providing the string represantation of some SVG
     */
    public SVG(Reader reader) {
        Preconditions.checkNotNull(reader);
        structure = null;
        color = null;
        background = null;
        coloredChars = false;
        new SVGLoader(reader, docFactorySync).run();
    }

    /**
     * Paints the <code>SVG</code> onto the specified {@link Graphics2D} object.
     * <p>
     * Since <code>SVG</code>s are loaded in a background thread this SVG may
     * not yet be loaded when <code>paint</code> is called. In this case paint
     * will do nothing and return <code>false</code>. If this <code>SVG</code>
     * has finished loading before paint is called it will be drawn and
     * <code>true</code> will be returned.
     * 
     * @param g
     *            graphics object which this svg will be painted on.
     * @return <code>true</code> if this svg was already loaded and could be
     *         painted, <code>false</code> otherwise.
     */
    public boolean paint(Graphics2D g) {
        if (graphicsnode != null) {
            graphicsnode.paint(g);
            return true;
        }
        return false;
    }

    /**
     * Paints the <code>SVG</code> onto the specified {@link Graphics2D} object
     * and scales it so that it fits into the given bounds, while keeping the
     * same aspect ratio. The rescaled SVG is then moved to the middle of the
     * given bounds if <code>moveToMiddle</code> is <code>true</code> otherwise
     * it is translated by <code>x</code> and <code>y</code>.
     * <p>
     * Since <code>SVG</code>s are loaded in a background thread this SVG may
     * not yet be loaded when <code>paint</code> is called. In this case paint
     * will do nothing and return <code>false</code>. If this <code>SVG</code>
     * has finished loading before paint is called it will be drawn and
     * <code>true</code> will be returned.
     * 
     * @param g
     *            graphics object which this svg will be painted on.
     * @param x
     *            for translation
     * @param y
     *            for translation
     * @param height
     *            for rescaling
     * @param width
     *            for rescaling
     * @param moveToMiddle
     *            should the SVG be moved to the middle after scaling
     * @return <code>true</code> if this svg was already loaded and could be
     *         painted, <code>false</code> otherwise.
     */
    public boolean paint(Graphics2D g, double x, double y, double width, double height, boolean moveToMiddle) {
        if (graphicsnode == null)
            return false;
        Rectangle2D bounds = graphicsnode.getBounds();

        double w, h, tx, ty;
        if (structure == null) {
            w = width / bounds.getWidth();
            h = height / bounds.getHeight();
        } else {
            w = width / structure.getSvgWidth();
            h = height / structure.getSvgHeight();
        }
        // take the minimum, to keep the aspect ratio
        double s = Math.min(w, h);
        if (moveToMiddle) {
            // add translation to move scaled image to the middle
            if (structure == null) {
                tx = w > h ? x + (width - h * bounds.getWidth()) / 2 : x;
                ty = w < h ? y + (height - w * bounds.getHeight()) / 2 : y;
            } else {
                tx = w > h ? x + (width - h * structure.getSvgWidth()) / 2 : x;
                ty = w < h ? y + (height - w * structure.getSvgHeight()) / 2 : y;
            }
        } else {
            tx = x;
            ty = y;
        }
        // if scale factor is 0 simply don't paint
        if (s != 0) {
            AffineTransform t = new AffineTransform(new double[] { s, 0, 0, s, tx, ty });
            graphicsnode.setTransform(t);
            graphicsnode.paint(g);
            graphicsnode.setTransform(new AffineTransform());
        }
        return true;

    }

    /**
     * Paints the <code>SVG</code> onto the specified {@link Graphics2D} object
     * and scales it so that it fits into the given bounds, while keeping the
     * same aspect ratio. The rescaled SVG is then moved by <code>x</code> and
     * <code>y</code>.
     * <p>
     * Since <code>SVG</code>s are loaded in a background thread this SVG may
     * not yet be loaded when <code>paint</code> is called. In this case paint
     * will do nothing and return <code>false</code>. If this <code>SVG</code>
     * has finished loading before paint is called it will be drawn and
     * <code>true</code> will be returned.
     * 
     * @param g
     *            graphics object which this svg will be painted on.
     * @param x
     *            for translation
     * @param y
     *            for translation
     * @param height
     *            for rescaling
     * @param width
     *            for rescaling
     * @return <code>true</code> if this svg was already loaded and could be
     *         painted, <code>false</code> otherwise.
     */
    public boolean paint(Graphics2D g, double x, double y, double width, double height) {
        return paint(g, x, y, width, height, false);
    }

    /**
     * Paints the <code>SVG</code> onto the specified {@link Graphics2D} object
     * and scales it so that if fits into the given bounds, while keeping the
     * same aspect ratio. The rescaled SVG is then moved to the middle of the
     * given bounds.
     * <p>
     * Since <code>SVG</code>s are loaded in a background thread this SVG may
     * not yet be loaded when <code>paint</code> is called. In this case paint
     * will do nothing and return <code>false</code>. If this <code>SVG</code>
     * has finished loading before paint is called it will be drawn and
     * <code>true</code> will be returned.
     * 
     * @param g
     *            graphics object which this svg will be painted on.
     * @param height
     *            for rescaling
     * @param width
     *            for rescaling
     * @return <code>true</code> if this svg was already loaded and could be
     *         painted, <code>false</code> otherwise.
     */
    public boolean paint(Graphics2D g, double width, double height) {
        return paint(g, 0, 0, width, height, true);
    }

    /**
     * Adds an Observer to this <code>SVG</code>. The observer will be notified
     * on the AWT Event Dispatch thread, when operation which change how this
     * <code>SVG</code> is painted are finished.
     * <p>
     * If an observer has already been added once it is silently ignored.
     * 
     * @param observer
     *            observer to be added
     */
    public void addObserver(SVGLoadObserver observer) {
        WeakReference<SVGLoadObserver> r = new WeakReference<SVGLoadObserver>(observer);
        if (!observers.contains(r) && observer != null)
            observers.add(r);
    }

    /**
     * Removes the specified observer.
     * 
     * @param observer
     *            observer to be removed
     * @return <code>true</code> if the observer was one of this
     *         <code>SVG</code>'s observers. <code>false</code> otherwise.
     */
    public boolean removeObserver(SVGLoadObserver observer) {
        return observers.remove(new WeakReference<SVGLoadObserver>(observer));
    }

    /**
     * @return a Swing Component which displays this SVG
     */
    public JGVTComponent getComponent() {
        if (component != null)
            return component;
        component = new JGVTComponent();
        component.setSize(structure.getSvgWidth(), structure.getSvgHeight());
        component.setPreferredSize(component.getSize());
        if (graphicsnode != null) {
            component.setGraphicsNode(graphicsnode);
        } else {
            addObserver(new SVGLoadObserver() {

                @Override
                public void svgLoaded(SVG svg) {
                    component.setGraphicsNode(graphicsnode);
                }
            });
        }
        return component;
    }

    private void notifyObservers() {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                List<WeakReference<SVGLoadObserver>> toRemove = Lists.newArrayList();
                for (WeakReference<SVGLoadObserver> r : observers) {
                    SVGLoadObserver o = r.get();
                    if (o == null)
                        toRemove.add(r);
                    else
                        o.svgLoaded(SVG.this);
                }
                observers.removeAll(toRemove);
            }
        });

    }

    private class SVGLoader implements Runnable {

        private final DbManager db;
        private Reader reader;
        private SAXSVGDocumentFactory f;

        public SVGLoader(DbManager db, SAXSVGDocumentFactory f) {
            this.db = db;
            this.reader = null;
            this.f = f;
        }

        public SVGLoader(Reader reader, SAXSVGDocumentFactory f) {
            this.reader = reader;
            this.db = null;
            this.f = f;
        }

        @Override
        public void run() {
            SVGDocument doc;
            if (reader == null) {
                try {
                    reader = new StringReader(db.getSvgString(structure));
                } catch (DatabaseException e) {
                    reader = new StringReader(errorSVGStr);
                }
            }
            doc = loadDocument(reader);
            BridgeContext bridge = new BridgeContext(new UserAgentAdapter());
            bridge.setDynamic(false);

            Element defs = doc.createElementNS("http://www.w3.org/2000/svg", "defs");
            Element style = doc.createElementNS("http://www.w3.org/2000/svg", "style");
            style.setAttribute("type", "text/css");
            style.appendChild(doc.createCDATASection(SVGGen.getCSS(Objects.firstNonNull(color, Color.BLACK),
                    background, coloredChars)));
            defs.appendChild(style);
            doc.getDocumentElement().insertBefore(defs, doc.getDocumentElement().getFirstChild());

            // log svg string
            if (logger.isTraceEnabled()) {
                TransformerFactory transfac = TransformerFactory.newInstance();
                Transformer trans;
                try {
                    trans = transfac.newTransformer();
                    trans.setOutputProperty(OutputKeys.INDENT, "yes");

                    // create string from xml tree
                    StringWriter sw = new StringWriter();
                    StreamResult result = new StreamResult(sw);
                    DOMSource source = new DOMSource(doc);
                    trans.transform(source, result);
                    String xmlString = sw.toString();
                    logger.trace(xmlString);
                } catch (TransformerConfigurationException e) {
                    logger.trace("Could not create transformer.", e);
                } catch (TransformerException e) {
                    logger.trace("Error during transform.", e);
                }
            }

            graphicsnode = gvtBuilder.build(bridge, doc);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    notifyObservers();
                }
            });
        }

        private SVGDocument loadDocument(Reader svgReader) {

            SVGDocument doc;
            try {
                doc = (SVGDocument) f.createDocument(null, svgReader);
            } catch (Exception ex) {
                // The createDocument() statement may throw an IOException
                // and also unchecked exception (e.g. DOMException).
                // In either case this should not stop the loader thread
                // and is handled by displaying a special SVG.
                logger.warn("Error loading svg: " + ex.getMessage());
                return getErrorSVGDoc();
            }

            // Changes the stroke-width and the stroke-linecap
            SVGSVGElement root = doc.getRootElement();
            root.setAttributeNS(null, "stroke-linecap", "round");
            String stroke_width = Double.toString(NODE_STROKE_WIDTH);
            root.setAttributeNS(null, "stroke-width", stroke_width);

            return doc;
        }

        private SVGDocument getErrorSVGDoc() {
            if (errorDocument == null)
                errorDocument = loadDocument(new StringReader(errorSVGStr));
            return errorDocument;
        }
    }
}
