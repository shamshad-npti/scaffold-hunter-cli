
package edu.udo.scaffoldhunter.model.util;

import java.awt.Color;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.geometry.GeometryTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.ringsearch.SSSRFinder;
import org.openscience.cdk.tools.SaturationChecker;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.RingSetManipulator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

import edu.udo.scaffoldhunter.model.treegen.CDKHelpers;
import edu.udo.scaffoldhunter.model.treegen.ScaffoldContainer;

/**
 * Class for the generation of SVG images of molecules.
 * 
 * 
 * @author Steffen Renner <steffen.renner@mpi-dortmund.mpg.de>
 *         Max-Planck-Institut für Molekulare Physiologie Otto-Hahn-Strasse 11
 *         D-44227 Dortmund Germany
 * @author Henning Garus
 */
public class SVGGen {
    private static Logger logger = LoggerFactory.getLogger(SVGGen.class);

    private static String colorToHex(Color color) {
        return String.format("#%06x", color.getRGB() & 0xFFFFFF);
    }

    private static String getBgCSS(Color color) {
        if (color == null) {
            return "#BG { visibility: hidden }";
        } else {
            return "#BG { fill: " + colorToHex(color) + "}";        
        }
    }
    
    /**
     * Generates a stylesheet to display a Structure svg
     * <p>
     * If svgs are displayed without applying a stylesheet, they may be
     * displayed incorrectly.
     * 
     * @param bondColor
     *            the color of the bonds in the svg
     * @param bgColor
     *            the background color
     * @param coloredChars 
     *            <code>true</code> if the characters denoting some molecules 
     *            should be colored, otherwise these characters have the same
     *            color as the bonds   
     * @return a css stylesheet to display a structure svg.
     */
    public static String getCSS(Color bondColor, Color bgColor, boolean coloredChars) {
        String bondColorString = colorToHex(bondColor);
        String css = "#bond { stroke: " + bondColorString + "}" + getBgCSS(bgColor)
                + "tspan { font-family:Arial,Helvetica,sans-serif;font-size: 14 px }" 
                + "tspan.implH { font-size: 7 px }";
        if (coloredChars) {
            css += "text.oxygen { fill: #c80000 }" + "text.nitrogen { fill: #0000c8 }";
        } else {
            css += "text { fill:" + bondColorString + "; text-anchor: middle }";
        }

        return css;
    }

    /**
     * Calculates the molecule SVG
     * 
     * @param mol
     *            org.openscience.cdk.Molecule the SVG-String is calculated for
     * 
     * @return String with the molecules SVG
     */
    public static SVGGenResult getSVG(IAtomContainer mol) {
        // check for explicit hydrogens
        boolean hasExpHs = false;
        for (IAtom a : mol.atoms()) {
            if (a.getSymbol().equals("H")) {
                hasExpHs = true;
                break;
            }
        }
        // remove explicit hydrogens
        if (hasExpHs) {
            mol = AtomContainerManipulator.removeHydrogens(mol);
            logger.debug("Removed explict hydrogens for SVG depiction.");
        }
        
        return calcSVGString(mol, null);
    }

    /**
     * Calculates the molecule SVG
     * 
     * @param container
     *            ScaffoldContainer the SVG-String is calculated for
     * 
     * @return String with the molecules SVG
     */
    public static SVGGenResult getSVG(ScaffoldContainer container) {
        IAtomContainer mol = container.getScaffoldMolecule();
        IRingSet rings = container.getRings();
        return calcSVGString(mol, rings);

    }

    /**
     * Generates an SVG.
     * 
     * @param mol the molecule for which to calculate the SVG
     * @param rings the ring set of the molecule, if null the ring set will be calculated
     * @return object containing the SVG
     */
    private static SVGGenResult calcSVGString(IAtomContainer mol, IRingSet rings) {
        String svgstr = null;
        int width = 1;
        int height = 1;

        SaturationChecker impHCalc = new SaturationChecker();

        double boundary = 30;
        double atomLetterRadius = 8; // radius around atom letters in the
        // graph without bonds shown
        double atomWithImpHydrogensRadius = 13;
        double doubleBondDist = 5;
        double tripleBondDist = 5;

        // Check if we have a molecule with 2D Coordinates, if not select
        // largest fragment and recalculate the Coordinates
        if (!CDKHelpers.hasValid2Dcoordinates(mol)) {
            IAtomContainer original = mol;
            mol = CDKHelpers.getLargestFragment(mol);
            logger.info("Molecule does not contain valid 2D coordinates. New 2D coordinates will be calculated for SVG generation!");
            mol = CDKHelpers.calculate2Dcoordinates(mol);
            // triggers recalculation of rings
            if (mol != original) rings = null;
        }
        
        // calculate rings if required
        if (rings == null) {
            SSSRFinder sssrf = new SSSRFinder(mol);
            rings = sssrf.findRelevantRings();
        }

        GeometryTools.scaleMolecule(mol, GeometryTools.getScaleFactor(mol, 30.0));
        GeometryTools.translateAllPositive(mol);
        double[] minmax = GeometryTools.getMinMax(mol);
        GeometryTools.translate2D(mol, boundary, boundary);

        // Generate Bond Graph
        StringBuilder bondString = new StringBuilder();

        for (IBond bond : mol.bonds()) {

            // ggf noch wasserstoffe abfangen - einfach vorher entfernen -
            // polare aber vielleicht doch behalten
            IAtom at1 = bond.getAtom(0);
            IAtom at2 = bond.getAtom(1);
            Preconditions.checkNotNull(at2);
            Point2d at1center = new Point2d(at1.getPoint2d());
            Point2d at2center = new Point2d(at2.getPoint2d());

            int impHAt1, impHAt2;
            try {
                impHAt1 = impHCalc.calculateNumberOfImplicitHydrogens(at1, mol);
            } catch (CDKException e) {
                impHAt1 = 0;
            }
            try {
                impHAt2 = impHCalc.calculateNumberOfImplicitHydrogens(at2, mol);
            } catch (CDKException e) {
                impHAt2 = 0;
            }

            // Single bonds
            if (bond.getOrder() == IBond.Order.SINGLE) {
                if (at1.getAtomicNumber() != null && at1.getAtomicNumber() != 6) {
                    Vector2d diff = new Vector2d(at2center);
                    diff.sub(at1center);
                    diff = adjust(diff, impHAt1, atomLetterRadius, atomWithImpHydrogensRadius);
                    at1center.add(diff);
                }
                if (at2.getAtomicNumber() != null && at2.getAtomicNumber() != 6) {
                    Vector2d diff = new Vector2d(at1center);
                    diff.sub(at2center);
                    diff = adjust(diff, impHAt2, atomLetterRadius, atomWithImpHydrogensRadius);
                    at2center.add(diff);
                }
                bondString.append("<line id=\"bond\" x1=\"" + at1center.x + "\" y1=\"" + at1center.y
                        + "\" x2=\"" + at2center.x + "\" y2=\"" + at2center.y + "\" />\n");
            }
            // Double bonds
            else if (bond.getOrder() == IBond.Order.DOUBLE) {
                // Coordinates for second bond
                Point2d at1bcenter = new Point2d(at1.getPoint2d());
                Point2d at2bcenter = new Point2d(at2.getPoint2d());

                // Ring double bond
                if (RingSetManipulator.isSameRing(rings, at1, at2)) {

                    // find ring in which the double bond should be drawn
                    org.openscience.cdk.interfaces.IRing theRing = null;
                    IRingSet ringlist = rings.getRings(bond);
                    for (IAtomContainer ac : ringlist.atomContainers()) {
                        IRing ring = (IRing) ac;

                        if (ring.getRingSize() == 6 && ring.getFlag(CDKConstants.ISAROMATIC)) {
                            theRing = ring;
                            break;
                        } else if (ring.getFlag(CDKConstants.ISAROMATIC)) {
                            theRing = ring;
                            break;
                        } else {
                            theRing = ring;
                        }
                    }

                    // Determine ring center
                    double xsum = 0;
                    double ysum = 0;
                    for (IAtom ringAtom : theRing.atoms()) {
                        xsum += ringAtom.getPoint2d().x;
                        ysum += ringAtom.getPoint2d().y;
                    }
                    Point2d ringcenter = new Point2d((xsum / theRing.getRingSize()), (ysum / theRing.getRingSize()));

                    // Calculate coordinates for double bonds
                    Vector2d diff1 = new Vector2d(ringcenter);
                    diff1.sub(at1bcenter);
                    diff1.scale(doubleBondDist / diff1.length());
                    at1bcenter.add(diff1);

                    Vector2d diff2 = new Vector2d(ringcenter);
                    diff2.sub(at2bcenter);
                    diff2.scale(doubleBondDist / diff2.length());
                    at2bcenter.add(diff2);

                    // Correct for non-carbon atoms letters
                    if (at1.getAtomicNumber() != null && at1.getAtomicNumber() != 6) {
                        Vector2d diff = new Vector2d(at2center);
                        diff.sub(at1center);
                        diff = adjust(diff, impHAt1, atomLetterRadius, atomWithImpHydrogensRadius);
                        at1center.add(diff);
                        at1bcenter.add(diff);
                    }
                    if (at2.getAtomicNumber() != null && at2.getAtomicNumber() != 6) {
                        Vector2d diff = new Vector2d(at1center);
                        diff.sub(at2center);
                        diff = adjust(diff, impHAt2, atomLetterRadius, atomWithImpHydrogensRadius);
                        at2center.add(diff);
                        at2bcenter.add(diff);
                    }

                    bondString.append("<line id=\"bond\" x1=\"" + at1center.x + "\" y1=\"" + at1center.y
                            + "\" x2=\"" + at2center.x + "\" y2=\"" + at2center.y + "\" />\n");
                    bondString.append("<line id=\"bond\" x1=\"" + at1bcenter.x + "\" y1=\"" + at1bcenter.y
                            + "\" x2=\"" + at2bcenter.x + "\" y2=\"" + at2bcenter.y + "\" />\n");

                }
                // non ring double bond
                else {

                    // terminal sidechain double bond
                    if ((mol.getConnectedBondsCount(at1) == 1 && mol.getConnectedBondsCount(at2) > 2)
                            || (mol.getConnectedBondsCount(at1) > 2 && mol.getConnectedBondsCount(at2) == 1)) {

                        // Calculate 90� vector for translation of the two
                        // bonds
                        Vector2d translate = new Vector2d(at1center);
                        translate.sub(at2center);
                        double tempx = translate.x;
                        double tempy = translate.y;
                        translate.x = tempy * (-1);
                        translate.y = tempx;
                        translate.scale((doubleBondDist / 2) / translate.length());
                        // translate
                        at1center.add(translate);
                        at2center.add(translate);
                        translate.negate();
                        at1bcenter.add(translate);
                        at2bcenter.add(translate);
                    }
                    // nonterminal double bond
                    else {
                        // Calculate 90� vector for translation of the two
                        // bonds
                        Vector2d translate = new Vector2d(at1center);
                        translate.sub(at2center);
                        double tempx = translate.x;
                        double tempy = translate.y;
                        translate.x = tempy * (-1);
                        translate.y = tempx;
                        translate.scale((doubleBondDist) / translate.length());
                        // translate
                        at1center.add(translate);
                        at2center.add(translate);
                    }

                    // Correct for non-carbon atoms letters
                    if (at1.getAtomicNumber() != null && at1.getAtomicNumber() != 6) {
                        Vector2d diff = new Vector2d(at2center);
                        diff.sub(at1center);
                        diff = adjust(diff, impHAt1, atomLetterRadius, atomWithImpHydrogensRadius);
                        at1center.add(diff);
                        at1bcenter.add(diff);
                    }
                    if (at2.getAtomicNumber() != null && at2.getAtomicNumber() != 6) {
                        Vector2d diff = new Vector2d(at1center);
                        diff.sub(at2center);
                        diff = adjust(diff, impHAt2, atomLetterRadius, atomWithImpHydrogensRadius);
                        at2center.add(diff);
                        at2bcenter.add(diff);
                    }

                    bondString.append("<line id=\"bond\" x1=\"" + at1center.x + "\" y1=\"" + at1center.y
                            + "\" x2=\"" + at2center.x + "\" y2=\"" + at2center.y + "\"/>\n");
                    bondString.append("<line id=\"bond\" x1=\"" + at1bcenter.x + "\" y1=\"" + at1bcenter.y
                            + "\" x2=\"" + at2bcenter.x + "\" y2=\"" + at2bcenter.y + "\"/>\n");
                }

            }
            // Triple bonds
            else if (bond.getOrder() == IBond.Order.TRIPLE) {

                // Coordinates for second and third bond
                Point2d at1bcenter = new Point2d(at1.getPoint2d());
                Point2d at2bcenter = new Point2d(at2.getPoint2d());
                Point2d at1ccenter = new Point2d(at1.getPoint2d());
                Point2d at2ccenter = new Point2d(at2.getPoint2d());

                // Calculate 90� vector for translation of the two bonds
                Vector2d translate = new Vector2d(at1center);
                translate.sub(at2center);
                double tempx = translate.x;
                double tempy = translate.y;
                translate.x = tempy * (-1);
                translate.y = tempx;
                translate.scale(tripleBondDist / translate.length());
                // translate
                at1bcenter.add(translate);
                at2bcenter.add(translate);
                translate.negate();
                at1ccenter.add(translate);
                at2ccenter.add(translate);

                // Correct for non-carbon atoms letters
                if (at1.getAtomicNumber() != null && at1.getAtomicNumber() != 6) {
                    Vector2d diff = new Vector2d(at2center);
                    diff.sub(at1center);
                    diff = adjust(diff, impHAt1, atomLetterRadius, atomWithImpHydrogensRadius);
                    at1center.add(diff);
                    at1bcenter.add(diff);
                    at1ccenter.add(diff);
                }
                if (at2.getAtomicNumber() != null && at2.getAtomicNumber() != 6) {
                    Vector2d diff = new Vector2d(at1center);
                    diff.sub(at2center);
                    diff = adjust(diff, impHAt2, atomLetterRadius, atomWithImpHydrogensRadius);
                    at2center.add(diff);
                    at2bcenter.add(diff);
                    at2ccenter.add(diff);
                }

                bondString.append("<line id=\"bond\" x1=\"" + at1center.x + "\" y1=\"" + at1center.y + "\" x2=\""
                        + at2center.x + "\" y2=\"" + at2center.y + "\"/>\n");
                bondString.append("<line id=\"bond\" x1=\"" + at1bcenter.x + "\" y1=\"" + at1bcenter.y + "\" x2=\""
                        + at2bcenter.x + "\" y2=\"" + at2bcenter.y + "\"/>\n");
                bondString.append("<line id=\"bond\" x1=\"" + at1ccenter.x + "\" y1=\"" + at1ccenter.y + "\" x2=\""
                        + at2ccenter.x + "\" y2=\"" + at2ccenter.y + "\"/>\n");
            }
        }

        // Generate Atom Annotation
        StringBuilder atomString = new StringBuilder();
        for (IAtom atom : mol.atoms()) {
            
            // skip if atom is a carbon or a Pseudoatom (whatever that is)
            if (atom.getAtomicNumber() == null || atom.getAtomicNumber() == 6) 
                continue;

            int impH;
            try {
                impH = impHCalc.calculateNumberOfImplicitHydrogens(atom, mol);
            } catch (CDKException e) {
                impH = 0;
            }

            int dx = -5;
            int dy = 5;
            Point2d atomcenter = atom.getPoint2d();

            String clazz;
            switch (Objects.firstNonNull(atom.getAtomicNumber(), -1)) {
            // Oxygen
            case 8:
                clazz = "oxygen";
                break;
            // Nitrogen
            case 7:
                clazz = "nitrogen";
                break;
            default:
                clazz = "atom";
            }

            atomString.append("<text class=\"" + clazz + "\" x=\"" + atomcenter.x + "\" y=\"" + atomcenter.y + "\">\n");

            // generate atom name
            if (impH <= 0) {
                atomString.append("<tspan dx=\"" + dx + "\" dy=\"" + dy + "\">" + atom.getSymbol() + "</tspan>\n");
            }
            // add "H" for implicit hydrogen
            else if (impH > 0) {
                atomString.append("<tspan dx=\"" + (2 * dx) + "\" dy=\"" + dy + "\">" + atom.getSymbol()
                        + "</tspan>\n");
                atomString.append("<tspan>H</tspan>\n");
            }
            // add number of implicit hydrogens
            if (impH > 1) {
                atomString.append("<tspan class=\"implH\" style=\"baseline-shift:sub;\">" + impH + "</tspan>\n");
            }

            // generate String for formal charge of atom
            String formalcharge = "";
            if (atom.getFormalCharge() > 0) {
                formalcharge = "+";
                if (atom.getFormalCharge() > 1) {
                    formalcharge = formalcharge + atom.getFormalCharge();
                }
            } else if (atom.getFormalCharge() < 0) {
                formalcharge = "-";
                if (atom.getFormalCharge() < (-1)) {
                    formalcharge = formalcharge + (-1) * atom.getFormalCharge();
                }
            }
            atomString.append("<tspan>" + formalcharge + "</tspan>\n");
            atomString.append("</text>");
        }

        int w = (int) Math.round(minmax[2]);
        int h = (int) Math.round(minmax[3]);
        int b = (int) boundary;
        String bg = String.format("<rect id=\"BG\" x=\"%d\" y=\"%d\" width=\"%d\" height=\"%d\"/>\n", b / 2, b / 2,
                w + b, h + b);
        width = (int) (w + 2 * boundary);
        height = (int) (h + 2 * boundary);
        svgstr = "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"" + width + "\" height=\"" + height + "\" >\n" + bg
                + bondString + atomString + "</svg>";
        return (new SVGGenResult(svgstr, width, height));

    }

    // Adjusts the bond length in presence of atom letters
    private static Vector2d adjust(Vector2d diff, int impH, double atomLetterRadius, double atomWithImpHydrogensRadius) {
        Vector2d resultVector = new Vector2d(diff);
        Vector2d tempVector = null;
        Vector2d verticalVector = new Vector2d(0.0, 1.0);
        double refAngle = 0;
        if (impH < 1) {
            Vector2d textBoxDiagonal = new Vector2d(atomLetterRadius, atomLetterRadius);
            refAngle = verticalVector.angle(textBoxDiagonal);
            double vertAngle = diff.angle(verticalVector);
            if (vertAngle < refAngle || vertAngle > (Math.PI - refAngle)) {
                double x;
                if (diff.y != 0) {
                    x = (atomLetterRadius * Math.abs(diff.x / diff.y));
                } else {
                    x = atomLetterRadius;
                }
                double y = atomLetterRadius;
                tempVector = new Vector2d(x, y);
                resultVector.scale((tempVector.length() / diff.length()));
            } else {
                double x = atomLetterRadius;
                double y;
                if (diff.y != 0) {
                    y = atomLetterRadius / Math.abs(diff.x / diff.y);
                } else {
                    y = atomLetterRadius;
                }
                tempVector = new Vector2d(x, y);
                resultVector.scale((tempVector.length() / diff.length()));
            }
        } else {
            // Vector2d textBoxDiagonal = new
            // Vector2d(atomWithImpHydrogensRadius, atomLetterRadius);
            Vector2d textBoxDiagonal = new Vector2d(atomLetterRadius, atomLetterRadius);
            refAngle = verticalVector.angle(textBoxDiagonal);
            double vertAngle = diff.angle(verticalVector);
            if (vertAngle < refAngle || vertAngle > (Math.PI - refAngle)) {
                double x;
                if (diff.y != 0) {
                    x = atomWithImpHydrogensRadius * Math.abs(diff.x / diff.y);
                } else {
                    x = atomLetterRadius;
                }
                double y = atomLetterRadius;
                tempVector = new Vector2d(x, y);
                resultVector.scale((tempVector.length() / diff.length()));
            } else {
                double x = atomWithImpHydrogensRadius;
                double y;
                if (diff.y != 0) {
                    y = (atomLetterRadius / Math.abs(diff.x / diff.y));
                } else {
                    y = atomLetterRadius;
                }
                tempVector = new Vector2d(x, y);
                resultVector.scale((tempVector.length() / diff.length()));
            }
        }

        return resultVector;
    }
}
