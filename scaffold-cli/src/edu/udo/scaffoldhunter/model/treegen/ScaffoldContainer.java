
package edu.udo.scaffoldhunter.model.treegen;

import java.io.StringWriter;
import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.aromaticity.CDKHueckelAromaticityDetector;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IAtomContainerSet;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IRing;
import org.openscience.cdk.interfaces.IRingSet;
import org.openscience.cdk.io.MDLV2000Writer;
import org.openscience.cdk.ringsearch.RingPartitioner;
import org.openscience.cdk.ringsearch.SSSRFinder;
import org.openscience.cdk.silent.Atom;
import org.openscience.cdk.silent.AtomContainer;
import org.openscience.cdk.silent.RingSet;
import org.openscience.cdk.tools.manipulator.AtomContainerManipulator;
import org.openscience.cdk.tools.manipulator.RingSetManipulator;

import edu.udo.scaffoldhunter.model.util.CanonicalSmilesGenerator;
import edu.udo.scaffoldhunter.model.util.MoleculeConfigurator;

/**
 * Class for the generation and work with molecular scaffolds.
 * 
 * 
 * @author Steffen Renner <steffen.renner@mpi-dortmund.mpg.de>
 *         Max-Planck-Institut f�r Molekulare Physiologie Otto-Hahn-Strasse 11
 *         D-44227 Dortmund Germany
 * 
 */
public class ScaffoldContainer implements Cloneable {

    /**
     * Properties to prioritize the parent scaffolds SCP - scaffold property of
     * newly created scaffold RRP - removed ring property RAP - property of ring
     * assembly of removed ring before removal
     */
    private int SCPnumAroRings;
    private int SCPnumALB;
    private int SCPdelta;
    private int SCPabsDelta;
    private int SCPnumAt;
    private int SCPnumHetAt;
    private int SCPnumNAt;
    private int SCPnumOAt;
    private int SCPnumSAt;

    private int RRPringSize;
    private int RRPlinkerSize;
    private int RRPnumHetAt;
    private int RRPnumNAt;
    private int RRPnumOAt;
    private int RRPnumSAt;
    private boolean RRParomatic;
    private boolean RRPhetatlinked;

    private int RAPdelta;
    private int RAPnumRings;
    private int RAPnumAroRings;
    private int RAPnumHetAt;
    private int RAPnumNAt;
    private int RAPnumOAt;
    private int RAPnumSAt;

    /**
     * Rule that was applied to select the scaffold.
     */
    private String deletionRule;

    private StringWriter sw = null;
    private MDLV2000Writer mwriter = null;

    /**
     * Stores the canonical SMILES of a molecule.
     */
    private String smile;

    /**
     * States if SMILES were already calculated.
     */
    private boolean smileCalculated = false;

    /**
     * Stores the canonical SMILES of the child molecule from which the scaffold
     * is calculated
     * 
     */
    private String childSmiles;

    /**
     * RingSet the scaffold.
     */
    private IRingSet rings;

    /**
     * RingSet the scaffold.
     */
    private List<IRingSet> ringAssemblies;

    /**
     * The scaffold.
     */
    private IAtomContainer mol;

    /**
     * Constructor
     * 
     * @param mol
     *            Molecule representation of the scaffold
     * @param generateMurckoScaffold
     *            boolean to control sidechain pruning
     * @param removeTerminalSugars
     *            boolean to control pruning of terminal sugar rings
     */
    public ScaffoldContainer(IAtomContainer mol, boolean generateMurckoScaffold, boolean removeTerminalSugars) {
        this.mol = mol;
        SSSRFinder sssrf = new SSSRFinder(this.mol);
        this.rings = sssrf.findRelevantRings();
        try {
            CDKHueckelAromaticityDetector.detectAromaticity(this.mol);
            RingSetManipulator.markAromaticRings(this.rings);
        } catch (CDKException e) {
            System.out.println("Problem with aromaticity detection");
        }

        if (removeTerminalSugars) {
            this.removeTerminalSugars();
        }
        this.ringAssemblies = RingPartitioner.partitionRings(this.rings);

        if (generateMurckoScaffold) {
            this.generateMurckoScaffold();
        }
        if (removeTerminalSugars || generateMurckoScaffold) {
            // the molecule has been modified, reconfigure it to assure correct 
            // computation of canonical SMILES
            MoleculeConfigurator.prepare(this.mol, true);
        }
        this.calculateSCProperties();
    }

    /**
     * Returns a clone of the ScaffoldContainer
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Returns the atoms and bonds of the scaffold.
     * 
     * @return the CDK Molecule representing scaffold in this container
     */
    public IAtomContainer getScaffoldMolecule() {
        return this.mol;
    }

    /**
     * Returns the rings of the scaffold.
     * 
     * @return <code>RingSet</code>
     */
    public IRingSet getRings() {
        return this.rings;
    }

    /**
     * Sets the deletion rule that was used to obtain the scaffold
     * 
     * @param rule
     *            a String which is the name of the rule used to select the
     *            parent scaffold
     */
    public void setDeletionRule(String rule) {
        this.deletionRule = rule;
    }

    /**
     * Returns the deletion rule that was used to obtain the scaffold
     * 
     * @return deletionRule deletion rule that was applied for the ring removal
     */
    public String getDeletionRule() {
        return this.deletionRule;
    }

    /**
     * Sets the SMILES of the child scaffold
     * 
     * @param smiles
     *            String with the smiles
     */
    public void setChildSmiles(String smiles) {
        this.childSmiles = smiles;
    }

    /**
     * Returns the child SMILES
     * 
     * @return child SMILES
     */
    public String getChildSmiles() {
        return this.childSmiles;
    }

    /**
     * Methods to return variables to prioritize parent scaffolds
     */

    /**
     * @return SCPnumRings
     */
    public int getSCPnumRings() {
        return this.rings.getAtomContainerCount();
    }

    /**
     * @return SCPnumAroRings
     */
    public int getSCPnumAroRings() {
        return this.SCPnumAroRings;
    }

    /**
     * @return SCPnumALB
     */
    public int getSCPnumALB() {
        return this.SCPnumALB;
    }

    /**
     * @return SCPdelta
     */
    public int getSCPdelta() {
        return this.SCPdelta;
    }

    /**
     * @return SCPabsDelta
     */
    public int getSCPabsDelta() {
        return this.SCPabsDelta;
    }

    /**
     * @return SCPnumAt
     */
    public int getSCPnumAt() {
        return this.SCPnumAt;
    }

    /**
     * @return SCPnumHetAt
     */
    public int getSCPnumHetAt() {
        return this.SCPnumHetAt;
    }

    /**
     * @return SCPnumNAt
     */
    public int getSCPnumNAt() {
        return this.SCPnumNAt;
    }

    /**
     * @return SCPnumOAt
     */
    public int getSCPnumOAt() {
        return this.SCPnumOAt;
    }

    /**
     * @return SCPnumSAt
     */
    public int getSCPnumSAt() {
        return this.SCPnumSAt;
    }

    /**
     * @return RRPringSize
     */
    public int getRRPringSize() {
        return this.RRPringSize;
    }

    /**
     * @return RRPlinkerSize
     */
    public int getRRPlinkerSize() {
        return this.RRPlinkerSize;
    }

    /**
     * @return RRPnumHetAt
     */
    public int getRRPnumHetAt() {
        return this.RRPnumHetAt;
    }

    /**
     * @return RRPnumNAt
     */
    public int getRRPnumNAt() {
        return this.RRPnumNAt;
    }

    /**
     * @return RRPnumOAt
     */
    public int getRRPnumOAt() {
        return this.RRPnumOAt;
    }

    /**
     * @return RRPnumSAt
     */
    public int getRRPnumSAt() {
        return this.RRPnumSAt;
    }

    /**
     * @return RRParomatic
     */
    public boolean getRRParomatic() {
        return this.RRParomatic;
    }

    /**
     * @return RRPhetatlinked
     */
    public boolean getRRPhetatlinked() {
        return this.RRPhetatlinked;
    }

    /**
     * @return RAPdelta
     */
    public int getRAPdelta() {
        return this.RAPdelta;
    }

    /**
     * @return RAPabsDelta
     */
    public int getRAPabsDelta() {
        return Math.abs(this.RAPdelta);
    }

    /**
     * @return RAPnumRings
     */
    public int getRAPnumRings() {
        return this.RAPnumRings;
    }

    /**
     * @return RAPnumAroRings
     */
    public int getRAPnumAroRings() {
        return this.RAPnumAroRings;
    }

    /**
     * @return RAPnumHetAt
     */
    public int getRAPnumHetAt() {
        return this.RAPnumHetAt;
    }

    /**
     * @return RAPnumNAt
     */
    public int getRAPnumNAt() {
        return this.RAPnumNAt;
    }

    /**
     * @return RAPnumOAt
     */
    public int getRAPnumOAt() {
        return this.RAPnumOAt;
    }

    /**
     * @return RAPnumSAt
     */
    public int getRAPnumSAt() {
        return this.RAPnumSAt;
    }

    /**
     * @param ringSize
     * @param linkerSize
     * @param numHetAt
     * @param numNAt
     * @param numOAt
     * @param numSAt
     * @param aromatic
     * @param hetatlinked
     */
    public void setRRProperties(int ringSize, int linkerSize, int numHetAt, int numNAt, int numOAt, int numSAt,
            boolean aromatic, boolean hetatlinked) {
        this.RRPringSize = ringSize;
        this.RRPlinkerSize = linkerSize;
        this.RRPnumHetAt = numHetAt;
        this.RRPnumNAt = numNAt;
        this.RRPnumOAt = numOAt;
        this.RRPnumSAt = numSAt;
        this.RRParomatic = aromatic;
        this.RRPhetatlinked = hetatlinked;
    }

    /**
     * Calculates Ring Assembly Properties
     * 
     * @param rs
     *            ring set that defines the ring assembly
     */
    public void calculateRAProperties(IRingSet rs) {
        this.RAPdelta = this.calculateDelta(rs);
        this.RAPnumRings = rs.getAtomContainerCount();

        int numAro = 0;
        int numHet = 0;
        int numNAt = 0;
        int numOAt = 0;
        int numSAt = 0;

        for (IAtomContainer ac : rs.atomContainers()) {
            IRing ring = (IRing) ac;
            // count aromatic rings
            if (ring.getFlag(CDKConstants.ISAROMATIC)) {
                numAro++;
            }
            // count heteroatoms
            for (IAtom atom : ring.atoms()) {
                if (atom.getAtomicNumber() == null) {
                    continue;
                }
                if (atom.getAtomicNumber() != 6) {
                    if (atom.getAtomicNumber() != 1) {
                        numHet++;
                    }
                    if (atom.getAtomicNumber() == 7) {
                        numNAt++;
                    } else if (atom.getAtomicNumber() == 8) {
                        numOAt++;
                    } else if (atom.getAtomicNumber() == 16) {
                        numSAt++;
                    }
                }
            }
        }

        this.RAPnumAroRings = numAro;
        this.RAPnumHetAt = numHet;
        this.RAPnumNAt = numNAt;
        this.RAPnumOAt = numOAt;
        this.RAPnumSAt = numSAt;
    }

    /**
     * Calculates Scaffold Properties
     * 
     */
    public void calculateSCProperties() {

        // Determine number of atoms and heteroatoms
        int numAt = 0;
        int numHet = 0;
        int numNAt = 0;
        int numOAt = 0;
        int numSAt = 0;
        for (IAtom atom : this.mol.atoms()) {
            numAt++;
            // count heteroatoms
            if (atom.getAtomicNumber() != null && atom.getAtomicNumber() != 6) {
                if (atom.getAtomicNumber() != 1) {
                    numHet++;
                }
                if (atom.getAtomicNumber() == 7) {
                    numNAt++;
                } else if (atom.getAtomicNumber() == 8) {
                    numOAt++;
                } else if (atom.getAtomicNumber() == 16) {
                    numSAt++;
                }
            }
        }
        this.SCPnumAt = numAt;
        this.SCPnumHetAt = numHet;
        this.SCPnumNAt = numNAt;
        this.SCPnumOAt = numOAt;
        this.SCPnumSAt = numSAt;

        // count aromatic rings
        int numAroRings = 0;
        for (int i = 0; i < rings.getAtomContainerCount(); i++) {
            if (rings.getAtomContainer(i).getFlag(CDKConstants.ISAROMATIC)) {
                numAroRings++;
            }
        }
        this.SCPnumAroRings = numAroRings;

        // count acyclic linker bonds and calculate delta
        this.SCPnumALB = this.calculateNumALB();
        this.SCPdelta = this.calculateSCPdelta();
        this.SCPabsDelta = this.calculateSCPabsDelta();

    }

    /**
     * Calculates the number of acyclic linker bonds exocyclic double bonds /
     * exolinker double bonds and attachment points are not considered here
     */
    private int calculateNumALB() {

        int[] murckoType = new int[mol.getAtomCount()];

        // All ring atoms are assigned the Murcko type "ring"
        for (IAtom atom : mol.atoms()) {
            if (rings.contains(atom)) {
                murckoType[mol.getAtomNumber(atom)] = 2;
            } else {
                murckoType[mol.getAtomNumber(atom)] = 0;
            }
        }

        // Loop over all atoms of a molecule assignment of Murcko types
        // Starting points are atoms in rings that have at least
        // one additional bonded atom
        for (IAtom atom : mol.atoms()) {
            // If the atom is in a ring and has more than 2 connected atoms
            if (rings.contains(atom) && mol.getConnectedBondsCount(atom) > 2) {
                // Assignment of Murcko types of neighboring atoms
                List<IAtom> neighborAtomsList = mol.getConnectedAtomsList(atom);
                Iterator<IAtom> neighborAtomIterator = neighborAtomsList.iterator();
                while (neighborAtomIterator.hasNext()) {
                    // If the atom is not part of a ring
                    IAtom neighborAtom = neighborAtomIterator.next();
                    if (!rings.contains(neighborAtom)) {
                        this.assignMurckoType(mol, murckoType, neighborAtom, atom);
                    }
                }
            }
        }

        // Loop over all bonds and count nonring bonds with either
        // two acyclic liner atoms (ala)
        // one ala and one ring atom
        // two ring atoms and the bond not being part of a ring
        int alb = 0;

        for (IBond bond : mol.bonds()) {
            // Bond is not a ring bond
            if (rings.getRings(bond).getAtomContainerCount() == 0) {
                int atType01 = murckoType[mol.getAtomNumber((bond.getAtom(0)))];
                int atType02 = murckoType[mol.getAtomNumber((bond.getAtom(1)))];
                // type 3 = ala atom
                // type 2 = ring atom
                if (atType01 == 3 && atType02 == 3 || atType01 == 2 && atType02 == 3 || atType01 == 3 && atType02 == 2
                        || atType01 == 2 && atType02 == 2) {
                    alb++;
                }
            }
        }
        return alb;
    }

    /**
     * Calculate delta Sum over all ring assembly delta values
     * 
     * @return delta
     */
    private int calculateSCPdelta() {
        int tempdelta = 0;
        for (IRingSet ra : this.ringAssemblies) {
            tempdelta += this.calculateDelta(ra);
        }
        return tempdelta;
    }

    /**
     * Calculate absolute delta Sum over all ring assembly absolute delta values
     * 
     * @return delta
     */

    private int calculateSCPabsDelta() {
        int tempdelta = 0;
        for (IRingSet ra : this.ringAssemblies) {
            tempdelta += Math.abs(this.calculateDelta(ra));
        }
        return tempdelta;
    }

    /**
     * Calculates delta to prioritize bridged rings, spiro rings and nonlinear
     * fusion patterns
     * 
     * Calculation for rings assembly
     * 
     * @return delta
     */
    private int calculateDelta(IRingSet ringAssembly) {

        int nr = ringAssembly.getAtomContainerCount(); // number of rings
        int nrrb = 0; // number of bonds being a member of more than one ring
        for (IBond bond : mol.bonds()) {
            // bond is member of more than one ring
            if (ringAssembly.getRings(bond).getAtomContainerCount() > 1) {
                nrrb++;
            }
        }
        return ((nrrb - (nr - 1)));
    }

    /**
     * Calculates canonical SMILES of a scaffold
     * 
     * @return String SMILES of the scaffold
     */
    public String getSMILES() {

        if (smileCalculated) {
            return this.smile;
        } else {
            this.smile = CanonicalSmilesGenerator.createSMILES(this.mol, false);
            this.smileCalculated = true;
            return this.smile;
        }
    }

    /**
     * Calculates SD Format String
     * 
     * @return String SD formatted connection table
     */
    public String getMDLCTab() {
        String ctab = null;

        try {
            sw = new StringWriter();
            mwriter = new MDLV2000Writer(sw);
            this.mwriter.write(mol);
            ctab = sw.toString();
            // "$$$$" is not added automatically when single molecules are
            // written
            ctab = ctab + "$$$$\n";
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return ctab;
    }

    /**
     * Method to remove terminal sugars Deletes sugars having substructures
     * OC1OCCCC1 OC1OCCC1 OC1OCCCOC1 ??? OC1OCCOC1 ??? all ring atoms and
     * directly connected atoms are deleted
     */
    private void removeTerminalSugars() {

        // Atom container with atoms to be deleted
        IAtomContainer terminalSugarAtoms = new AtomContainer();

        IRingSet sugarRings = new RingSet();
        IRingSet terminalSugarRings = new RingSet();
        IRingSet nonTerminalSugarRings = new RingSet();
        IRingSet nonSugarRings = new RingSet();

        // IDENTIFY SUGAR RINGS
        for (int i = 0; i < rings.getAtomContainerCount(); i++) {
            IRing ring = (IRing) rings.getAtomContainer(i);
            if (ring.getFlag(CDKConstants.ISAROMATIC) || rings.getConnectedRings(ring).getAtomContainerCount() > 0
                    || (!(ring.getRingSize() == 5) && !(ring.getRingSize() == 6))) {
                nonSugarRings.addAtomContainer(ring);
            } else {
                // Non-aromatic, non-fused rings of size 5 and 6 are sugar
                // candidates
                boolean ringHasSugarRingAtomComposition = false;
                boolean ringHasOnlySingleBonds = false;
                boolean ringIsSugar = false;

                int numCAt = 0;
                int numOAt = 0;
                @SuppressWarnings("unused")
                int numOtherAt = 0;

                IAtom ringOAt = null;

                // Required 5 ring sugar element composition
                // is 4 C and 1 O
                if (ring.getRingSize() == 5) {
                    for (IAtom atom : ring.atoms()) {
                        if (atom.getAtomicNumber() == null) {
                            numOtherAt++;
                            continue;
                        }
                        if (atom.getAtomicNumber() == 6) {
                            numCAt++;
                        } else if (atom.getAtomicNumber() == 8) {
                            numOAt++;
                            ringOAt = atom;
                        } else {
                            numOtherAt++;
                        }
                    }
                    if (numCAt == 4 && numOAt == 1) {
                        ringHasSugarRingAtomComposition = true;
                    }
                }

                // Required 6 ring sugar element composition
                // is 5 C and 1 O
                if (ring.getRingSize() == 6) {
                    for (IAtom atom : ring.atoms()) {
                        if (atom.getAtomicNumber() == null) {
                            numOtherAt++;
                            continue;
                        }
                        if (atom.getAtomicNumber() == 6) {
                            numCAt++;
                        } else if (atom.getAtomicNumber() == 8) {
                            numOAt++;
                            ringOAt = atom;
                        } else {
                            numOtherAt++;
                        }
                    }
                    if (numCAt == 5 && numOAt == 1) {
                        ringHasSugarRingAtomComposition = true;
                    }
                }

                // All bonds must be single bonds
                if (ringHasSugarRingAtomComposition) {
                    boolean nonSingleBond = false;
                    for (IBond bond : ring.bonds()) {
                        if (bond.getOrder() != IBond.Order.SINGLE) {
                            nonSingleBond = true;
                        }
                    }
                    if (!nonSingleBond) {
                        ringHasOnlySingleBonds = true;
                    }
                }

                // O sidechain required next to ring O
                if (ringHasSugarRingAtomComposition && ringHasOnlySingleBonds) {
                    List<IAtom> ringONeighborAtomsList = ring.getConnectedAtomsList(ringOAt);
                    Iterator<IAtom> ringONeighborAtomsIterator = ringONeighborAtomsList.iterator();
                    while (ringONeighborAtomsIterator.hasNext()) {
                        IAtom ringONeighborAtom = ringONeighborAtomsIterator.next();
                        List<IAtom> ringONeighborNeighborAtomsList = mol.getConnectedAtomsList(ringONeighborAtom);
                        Iterator<IAtom> ringONeighborNeighborAtomIterator = ringONeighborNeighborAtomsList.iterator();
                        while (ringONeighborNeighborAtomIterator.hasNext()) {
                            IAtom ringONeighborNeighbor = ringONeighborNeighborAtomIterator.next();
                            // if O sidechains next to ring O
                            if (!ring.contains(ringONeighborNeighbor)
                                    && ringONeighborNeighbor.getAtomicNumber() != null
                                    && ringONeighborNeighbor.getAtomicNumber() == 8) {
                                // if O sidechain single bonded to the ring
                                if (mol.getBond(ringONeighborNeighbor, ringONeighborAtom).getOrder() == IBond.Order.SINGLE) {
                                    ringIsSugar = true;
                                }
                            }
                        }
                    }
                }

                // Collect sugar rings
                if (ringIsSugar) {
                    sugarRings.addAtomContainer(ring);
                } else {
                    nonSugarRings.addAtomContainer(ring);
                }
            }
        }

        // IDENTIFY TERMINAL SUGARS
        // -> terminal sugars = sugar rings that are connected to either:
        // a) only a single additional connected ring
        // b) one non-sugar ring and at least one terminal sugar ring
        // c) one non-terminal sugar ring and at least one terminal sugar ring
        boolean sugarClassified = true;
        while (sugarRings.getAtomContainerCount() > 0 && sugarClassified) {
            sugarClassified = false;
            int numRings = sugarRings.getAtomContainerCount();
            for (int i = 0; i < numRings; i++) {
                int numNonTerminalSidechains = 0;
                @SuppressWarnings("unused")
                int numTerminalSidechains = 0;
                int numNonclassifiedSidechains = 0;

                IRing sugarRing = (IRing) sugarRings.getAtomContainer(i);
                for (IAtom atom : sugarRing.atoms()) {
                    if (mol.getConnectedBondsCount(atom) > 2) {
                        List<IAtom> neighborAtomsList = mol.getConnectedAtomsList(atom);
                        Iterator<IAtom> neighborAtomsIterator = neighborAtomsList.iterator();
                        while (neighborAtomsIterator.hasNext()) {
                            IAtom neighborAtom = neighborAtomsIterator.next();
                            if (!sugarRing.contains(neighborAtom)) {
                                int sidechainType = connectionTypeOfSidechain(mol, terminalSugarRings,
                                        nonTerminalSugarRings, sugarRings, nonSugarRings, neighborAtom, atom);
                                if (sidechainType == 0) {
                                    numTerminalSidechains++;
                                } else if (sidechainType == 1) {
                                    numNonTerminalSidechains++;
                                } else if (sidechainType == 2) {
                                    numNonclassifiedSidechains++;
                                }
                            }
                        }
                    }
                }

                // Classify sugars
                if (numNonTerminalSidechains > 1) {
                    nonTerminalSugarRings.addAtomContainer(sugarRing);
                    sugarRings.removeAtomContainer(sugarRing);
                    numRings--;
                    sugarClassified = true;
                } else if (numNonclassifiedSidechains == 0) {
                    terminalSugarRings.addAtomContainer(sugarRing);
                    terminalSugarAtoms.add(sugarRing);
                    sugarRings.removeAtomContainer(sugarRing);
                    this.rings.removeAtomContainer(sugarRing);
                    numRings--;
                    sugarClassified = true;
                } else if ((numNonTerminalSidechains + numNonclassifiedSidechains) == 1) {
                    terminalSugarRings.addAtomContainer(sugarRing);
                    terminalSugarAtoms.add(sugarRing);
                    sugarRings.removeAtomContainer(sugarRing);
                    this.rings.removeAtomContainer(sugarRing);
                    numRings--;
                    sugarClassified = true;
                }
            }
        }

        // DELETE TERMINAL SUGARS
        // if sugars were detected
        if (terminalSugarAtoms.getAtomCount() > 0) {

            // Ring deletion
            for (IAtom removeAtom : terminalSugarAtoms.atoms()) {
                mol.removeAtomAndConnectedElectronContainers(removeAtom);
            }
            // If more than one fragment remaining, return the fragment
            // containing rings
            if (!ConnectivityChecker.isConnected(mol)) {
                String moleculeTitle = (String) mol.getProperty(CDKConstants.TITLE);
                IAtomContainerSet fragmentSet = ConnectivityChecker.partitionIntoMolecules(mol);
                boolean ringFragmentFound = false;
                for (int i = 0; i < fragmentSet.getAtomContainerCount(); i++) {
                    IAtomContainer fragment = fragmentSet.getAtomContainer(i);
                    for (IAtom fragmentAtom : fragment.atoms()) {
                        if (this.rings.contains(fragmentAtom)) {
                            mol = fragment;
                            mol.setProperty(CDKConstants.TITLE, moleculeTitle);
                            ringFragmentFound = true;
                            break;
                        }
                    }
                    if (ringFragmentFound) {
                        break;
                    }
                }

                // if no fragment with ring remaining set molecule to an empty
                // molecule
                if (!ringFragmentFound) {
                    mol = new AtomContainer();
                }

                
//                IAtomContainerSet fragmentSet = ConnectivityChecker.partitionIntoMolecules(mol);
//                //Find the largest fragment 
//                int maxAtomCount = 0;
//                for (int i=0; i < fragmentSet.getAtomContainerCount(); i++) {
//                    IAtomContainer fragment = fragmentSet.getAtomContainer(i);
//                    if (fragment.getAtomCount() > maxAtomCount){
//                        maxAtomCount = fragment.getAtomCount();
//                        mol = fragment;
//                    }
//                }
            }
            // Removal of bonds that only have a single atom remaining
            IAtomContainer removeBonds = new AtomContainer();
            for (IBond bond : mol.bonds()) {
                if (bond.getAtomCount() < 2) {
                    removeBonds.addBond(bond);
                }
            }
            mol.remove(removeBonds);
        }
        // this.mol = mol;
    }

    /**
     * Method for Sugar Removal Determines the connection type of sidechain of a
     * ring starting from a ring atom (origin atom) and the first sidechain atom
     * (atom)
     * 
     * Types of sidechain connection a) terminal (not connected to ring or
     * connected to terminal ring) - return value 0 b) nonterminal (connected to
     * non-sugar ring or nonterminal sugar ring) - return value 1 c)
     * undeterminable at the moment (connected to unclassified sugar ring) -
     * return value 2
     */
    private int connectionTypeOfSidechain(IAtomContainer mol, IRingSet terminalSugarRings, IRingSet nonTerminalSugarRings,
            IRingSet unclassifiedSugarRings, IRingSet nonSugarRings, IAtom atom, IAtom originAtom) {
        int connectionType = 2;

        if (terminalSugarRings.contains(atom)) {
            connectionType = 0;
        } else if (nonTerminalSugarRings.contains(atom) || nonSugarRings.contains(atom)) {
            connectionType = 1;
        } else if (unclassifiedSugarRings.contains(atom)) {
            connectionType = 2;
        }
        // if atom is not part of a ring:

        // ### If atom is only connected with the origin atom: it is terminal
        else if (mol.getConnectedBondsCount(atom) == 1) {
            connectionType = 0;
        }
        // ### Linear graph (2 connected atoms)
        else if (mol.getConnectedBondsCount(atom) == 2) {
            List<IAtom> neighborAtomsList = mol.getConnectedAtomsList(atom);
            Iterator<IAtom> neighborAtomsIterator = neighborAtomsList.iterator();
            while (neighborAtomsIterator.hasNext()) {
                IAtom neighborAtom = neighborAtomsIterator.next();
                if (!neighborAtom.equals(originAtom)) {
                    connectionType = connectionTypeOfSidechain(mol, terminalSugarRings, nonTerminalSugarRings,
                            unclassifiedSugarRings, nonSugarRings, neighborAtom, atom);
                }
            }
        }
        // ### Branching point in the graph
        else if (mol.getConnectedBondsCount(atom) > 2) {
            // Determine types of connections of the branches
            @SuppressWarnings("unused")
            int numTerminal = 0;
            int numNonTerminal = 0;
            int numNonDetermined = 0;
            List<IAtom> neighborAtomsList = mol.getConnectedAtomsList(atom);
            Iterator<IAtom> neighborAtomsIterator = neighborAtomsList.iterator();
            while (neighborAtomsIterator.hasNext()) {
                IAtom neighborAtom = neighborAtomsIterator.next();
                if (!neighborAtom.equals(originAtom)) {
                    int type = connectionTypeOfSidechain(mol, terminalSugarRings, nonTerminalSugarRings,
                            unclassifiedSugarRings, nonSugarRings, neighborAtom, atom);
                    if (type == 0) {
                        numTerminal++;
                    } else if (type == 1) {
                        numNonTerminal++;
                    } else if (type == 2) {
                        numNonDetermined++;
                    }
                }
            }
            // Determine type of connection of branching point
            if (numNonTerminal > 0) {
                connectionType = 1;
            } else if (numNonDetermined == 0) {
                connectionType = 0;
            } else {
                connectionType = 2;
            }
        }

        return connectionType;
    }

    /**
     * Calculates the Murcko Scaffold of the current Scaffold Molecule
     * 
     * Note: The molecule should be reconfigured after this method is called!
     */
    private void generateMurckoScaffold() {
        // Test for rings in the molecule
        if (rings.getAtomContainerCount() == 0) {
            mol.removeAllElements();
        } else {
            int[] murckoType = new int[mol.getAtomCount()];

            // All ring atoms are assigned the Murcko type "ring"
            for (IAtom atom : mol.atoms()) {
                if (rings.contains(atom)) {
                    murckoType[mol.getAtomNumber(atom)] = 2;
                } else {
                    murckoType[mol.getAtomNumber(atom)] = 0;
                }
            }

            // Loop over all atoms of a molecule assignment of Murcko types
            // Starting points are atoms in rings that have at least
            // one additional bonded atom
            for (IAtom atom : mol.atoms()) {
                // If the atom is in a ring and has more than 2 connected atoms
                if (rings.contains(atom) && mol.getConnectedBondsCount(atom) > 2) {
                    // Assignment of Murcko types of neighboring atoms
                    List<IAtom> neighborAtomsList = mol.getConnectedAtomsList(atom);
                    Iterator<IAtom> neighborAtomIterator = neighborAtomsList.iterator();
                    while (neighborAtomIterator.hasNext()) {
                        // If the atom is not part of a ring
                        IAtom neighborAtom = neighborAtomIterator.next();
                        if (!rings.contains(neighborAtom)) {
                            this.assignMurckoType(mol, murckoType, neighborAtom, atom);

                        }
                    }
                }
            }

            // ### Delete terminal sidechain atoms
            for (int j = Array.getLength(murckoType); j > 0; j--) {
                if (murckoType[j - 1] == 1) {
                    mol.removeAtomAndConnectedElectronContainers(mol.getAtom(j - 1));
                }
            }

        }
    }

    /**
     * Calculates the Murcko Type for an atom, where Murcko Types are 0 = not
     * yet assigned 1 = terminal sidechain 2 = ring 3 = linker 4 = excocyclic
     * double bond 5 = exolinker double bond 6 = potential exolinker double bond
     * - intermediate type for the determination of exolinker double bonds
     * (depending if the connected atom is linker of terminal sidechain)
     * 
     * IMPORTANT - method assumes that ring types were already assigned to int[]
     * murckoType
     * 
     * @param atom
     *            Atom for which the type is calculated
     * @param originAtom
     *            Atom that was assigned before
     * @param mol
     *            Molecule of Atoms atom and parent
     * @param murckoType
     *            array that is used to store the types
     * 
     */
    private void assignMurckoType(IAtomContainer mol, int[] murckoType, IAtom atom, IAtom originAtom) {

        // ### Stopping Criterion 1
        // Test if atom is already assigned to a Murcko Type
        if (murckoType[mol.getAtomNumber(atom)] == 0) {
            // ### Stopping Criterion 2
            // If an atom is only connected with the origin atom,
            // potential Murcko types are:
            // 1) exocyclic double bond or
            // 2) potential exolinker double bond or
            // 3) terminal sidechain
            if (mol.getConnectedBondsCount(atom) == 1) {
                IBond.Order bondOrder = (mol.getBond(atom, originAtom)).getOrder();
                // Double bond
                if (bondOrder == IBond.Order.DOUBLE) {
                    // Exocyclic double bond
                    if (murckoType[mol.getAtomNumber(originAtom)] == 2) {
                        murckoType[mol.getAtomNumber(atom)] = 4;
                    }
                    // Potential exolinker double bond
                    else {
                        murckoType[mol.getAtomNumber(atom)] = 6;
                    }
                }
                // Terminal sidechain
                else {
                    murckoType[mol.getAtomNumber(atom)] = 1;
                }
            }
            // ### Linear graph (2 connected atoms)
            if (mol.getConnectedBondsCount(atom) == 2) {
                List<IAtom> neighborAtomsList = mol.getConnectedAtomsList(atom);
                Iterator<IAtom> neighborAtomIterator = neighborAtomsList.iterator();
                while (neighborAtomIterator.hasNext()) {
                    IAtom neighborAtom = neighborAtomIterator.next();
                    if (!neighborAtom.equals(originAtom)) {
                        this.assignMurckoType(mol, murckoType, neighborAtom, atom);
                        int neighborAtomType = murckoType[mol.getAtomNumber((neighborAtom))];
                        // If the neighbor atom is terminal -> atom is terminal
                        // or exocyclic double bond
                        if (neighborAtomType == 1) {
                            // Exocyclic double bond in terminal chain
                            if ((mol.getBond(atom, originAtom)).getOrder() == IBond.Order.DOUBLE
                                    && murckoType[mol.getAtomNumber(originAtom)] == 2) {
                                murckoType[mol.getAtomNumber(atom)] = 4;
                            }
                            // Terminal
                            else {
                                murckoType[mol.getAtomNumber(atom)] = 1;
                            }
                        }
                        // If the neighbor atom is a potential exolinker double
                        // bond
                        // both atom and neighbor atom are terminal
                        else if (neighborAtomType == 6) {
                            murckoType[mol.getAtomNumber(atom)] = 1;
                            murckoType[mol.getAtomNumber((neighborAtom))] = 1;
                        }
                        // If the neighbor atom is ring or linker -> atom is
                        // linker
                        else if (neighborAtomType == 2 || neighborAtomType == 3) {
                            murckoType[mol.getAtomNumber(atom)] = 3;
                        }
                    }
                }
            }
            // ### Branching point in the graph
            if (mol.getConnectedBondsCount(atom) > 2) {
                // Determine type of atom
                int atomType = 1; // default = 1 (terminal), if >= 1 child
                                  // ring/linker -> atom is linker
                List<IAtom> neighborAtomsList = mol.getConnectedAtomsList(atom);
                Iterator<IAtom> neighborAtomIterator = neighborAtomsList.iterator();
                while (neighborAtomIterator.hasNext()) {
                    IAtom neighborAtom = neighborAtomIterator.next();
                    if (!neighborAtom.equals(originAtom)) {
                        this.assignMurckoType(mol, murckoType, neighborAtom, atom);
                        int neighborAtomType = murckoType[mol.getAtomNumber((neighborAtom))];
                        if (neighborAtomType == 2 || neighborAtomType == 3) {
                            atomType = 3;
                        }
                    }
                }
                murckoType[mol.getAtomNumber(atom)] = atomType;

                // Update types of neighboring atoms that are dependent on the
                // type of the atom
                // - exolinker double bonds
                // - exolinker double bonds within terminal sidechains
                neighborAtomIterator = neighborAtomsList.iterator();
                while (neighborAtomIterator.hasNext()) {
                    IAtom neighborAtom = neighborAtomIterator.next();
                    if (!neighborAtom.equals(originAtom)) {
                        int neighborAtomType = murckoType[mol.getAtomNumber(neighborAtom)];
                        // Update potential exolinker double bond
                        if (neighborAtomType == 6 && atomType == 1) {
                            murckoType[mol.getAtomNumber(neighborAtom)] = 1;
                        } else if (neighborAtomType == 6 && atomType == 3) {
                            murckoType[mol.getAtomNumber(neighborAtom)] = 5;
                        }
                        // Correct for exolinker double bonds within terminal
                        // sidechains
                        else if (neighborAtomType == 1 && atomType == 3
                                && (mol.getBond(atom, neighborAtom)).getOrder() == IBond.Order.DOUBLE) {
                            murckoType[mol.getAtomNumber(neighborAtom)] = 5;
                        }
                    }
                }
                // Update types of branching atom that are dependent on the
                // types of the neighbor atoms
                // Correction for exocyclic double bonds within terminal
                // sidechains
                if ((mol.getBond(atom, originAtom)).getOrder() == IBond.Order.DOUBLE) {

                    // Exocyclic double bond
                    if (murckoType[mol.getAtomNumber(originAtom)] == 2 && murckoType[mol.getAtomNumber(atom)] == 1) {
                        murckoType[mol.getAtomNumber(atom)] = 4;
                    }
                }

            }
        }
    }

    /**
     * Generates the set of new parent scaffolds with one ring removed.
     * Scaffolds with disconnected structures discarded.
     * 
     * @return Vector of parent scaffolds with one ring removed
     */
    public Vector<ScaffoldContainer> getAllParentScaffolds() {

        Vector<ScaffoldContainer> parentScaffoldsVector = new Vector<ScaffoldContainer>();
        IAtomContainer atomsAndBondsToBeRemoved = new AtomContainer();
        // AtomContainer retainedAtomsAndBonds = new AtomContainer();

        // SmilesGenerator local_smigen = new SmilesGenerator();

        // LOOP OVER ALL RINGS AND GENERATION OF PARENTS WITH
        // EACH RING REMOVED ONCE
        for (int i = 0; i < rings.getAtomContainerCount(); i++) {

            try {
                // New clone of the child molecule for ring deletion
                IAtomContainer pruneMol = mol.clone();

                // Ring to be deleted
                IRing ring = (IRing) rings.getAtomContainer(i);

                // Identify RingAssembly of the ring to be deleted
                IRingSet ringAssembly = new RingSet();
                for (IRingSet tempRingAssembly : this.ringAssemblies) {
                    if (tempRingAssembly.contains(ring)) {
                        ringAssembly = tempRingAssembly;
                    }
                }

                // Variable is used to collect all atoms and
                // bonds that are to be deleted from the molecule
                // using the command AtomContainer.remove(AtomContainer)
                // atomsAndBondsToBeRemoved = new AtomContainer();

                // Count the number of heteroatoms in the ring
                // used for parent scaffold priorization
                int numHet = 0;
                int numNAt = 0;
                int numOAt = 0;
                int numSAt = 0;

                // Variable is set true if a linker is connected to ring
                // heteroatoms
                // used for parent scaffolds priorization
                boolean heteroAtomAttachedLinker = false;

                // LOOP OVER ALL ATOMS OF THE RING
                for (IAtom ringAtom : ring.atoms()) {
                    if (ringAtom.getAtomicNumber() == null) {
                        continue;
                    }
                    // Count number of heteroatoms
                    if (ringAtom.getAtomicNumber() != 6) {
                        numHet++;
                        if (ringAtom.getAtomicNumber() == 7) {
                            numNAt++;
                        } else if (ringAtom.getAtomicNumber() == 8) {
                            numOAt++;
                        } else if (ringAtom.getAtomicNumber() == 16) {
                            numSAt++;
                        }
                    }

                    IRingSet ringsOfRingAtom = rings.getRings(ringAtom);

                    // IF ATOM IS ONLY PART OF A SINGLE RING, IT CAN BE DELETED
                    if (ringsOfRingAtom.getAtomContainerCount() == 1) {

                        // If atom without linkers -> deletion
                        if (mol.getConnectedBondsCount(ringAtom) == 2) {
                            atomsAndBondsToBeRemoved.addAtom(pruneMol.getAtom(mol.getAtomNumber(ringAtom)));
                        }

                        // IF > 2 NEIGHBOURS
                        // LINKER DELETION HAS TO BE EVOKED
                        else if (mol.getConnectedBondsCount(ringAtom) > 2) {

                            // Connected to heteroatom?
                            // if (ringAtom.getAtomicNumber() != 6) {
                            // heteroAtomAttachedLinker = true;
                            // }

                            // Delete linker atoms
                            List<IAtom> neighborAtomsList = mol.getConnectedAtomsList(ringAtom);
                            Iterator<IAtom> neighborAtomsIterator = neighborAtomsList.iterator();
                            while (neighborAtomsIterator.hasNext()) {
                                IAtom neighborAtom = neighborAtomsIterator.next();
                                if (!ringsOfRingAtom.contains(neighborAtom)) {
                                    IAtomContainer attachmentAtoms = new AtomContainer(this.removeLinkerOfDeletedRing(
                                            atomsAndBondsToBeRemoved, pruneMol, ringAtom, neighborAtom));
                                    // heteroAtomAttachedLinker =
                                    // this.removeLinkerOfDeletedRing(atomsAndBondsToBeRemoved,
                                    // pruneMol, ringAtom, neighborAtom) ||
                                    // heteroAtomAttachedLinker;

                                    // AtomContainer retainedAtomsAndBonds = new
                                    // AtomContainer();
                                    if (attachmentAtoms.getAtomCount() == 2) {
                                        // test for attachment to heteroatom
                                        heteroAtomAttachedLinker = (ringAtom.getAtomicNumber() == null || ringAtom
                                                .getAtomicNumber() != 6)
                                                || (attachmentAtoms.getAtom(0).getAtomicNumber() == null || attachmentAtoms
                                                        .getAtom(0).getAtomicNumber() != 6);
                                    }
                                    // atomsAndBondsToBeRemoved.remove(retainedAtomsAndBonds);

                                }
                            }
                        }

                    }

                    // ATOM IS PART OF OTHER RINGS
                    // ring atom and linkers must not be deleted in this case
                    else {
                        // IF DELETED RING IS AROMATIC
                        // BONDS MAY BE CHANGED AT FUSED RINGS
                        if (ring.getFlag(CDKConstants.ISAROMATIC)) {

                            // Loop over all connected rings
                            for (int n = 0; n < ringsOfRingAtom.getAtomContainerCount(); n++) {
                                // if current ring is not the parent itself
                                if (!ring.equals(ringsOfRingAtom.getAtomContainer(n))) {

                                    // Determine intersecting atoms of both
                                    // rings
                                    // that are connected to the current atom
                                    // other intersecting atoms are considered
                                    // in later
                                    IAtomContainer intersectingRingNeighborAtoms = new AtomContainer();
                                    List<IAtom> neighborAtomsList = ring.getConnectedAtomsList(ringAtom);
                                    Iterator<IAtom> neighborAtomsIterator = neighborAtomsList.iterator();
                                    while (neighborAtomsIterator.hasNext()) {
                                        IAtom neighborAtom = neighborAtomsIterator.next();
                                        if (((IRing) ringsOfRingAtom.getAtomContainer(n)).contains(neighborAtom)) {
                                            intersectingRingNeighborAtoms.addAtom(neighborAtom);
                                        }
                                    }

                                    // FUSED RING IS NON-AROMATIC
                                    // shared bond has to be set to double bond
                                    if (!((IRing) ringsOfRingAtom.getAtomContainer(n)).getFlag(CDKConstants.ISAROMATIC)) {
                                        if (intersectingRingNeighborAtoms.getAtomCount() == 1) {
                                            for (int m = 0; m < intersectingRingNeighborAtoms.getAtomCount(); m++) {
                                                // this is the bond to be set to
                                                // order 2
                                                pruneMol.getBond(
                                                        mol.getBondNumber(intersectingRingNeighborAtoms.getAtom(m),
                                                                ringAtom)).setOrder(IBond.Order.DOUBLE);
                                            }
                                        }
                                    }

                                    // FUSED RING IS ALSO AROMATIC
                                    // If shared bond is a single bond and both
                                    // intersecting atoms are part of double
                                    // bonds in the ring to be removed
                                    // the shared bond is set to double (e.g.
                                    // indole with removed benzene)
                                    else {
                                        // If both rings have two atoms in
                                        // common
                                        if (intersectingRingNeighborAtoms.getAtomCount() == 1) {
                                            // If bond order if shared bond is
                                            // one
                                            if (pruneMol.getBond(
                                                    mol.getBondNumber(intersectingRingNeighborAtoms.getAtom(0),
                                                            ringAtom)).getOrder() == IBond.Order.SINGLE) {
                                                boolean firstAtomWithDoubleBondInDeletedRing = false;
                                                boolean secondAtomWithDoubleBondInDeletedRing = false;

                                                // Test if first atom of fused
                                                // bond participates in a double
                                                // bond in the ring to be
                                                // deleted
                                                List<IAtom> neighborAtomsOfFirstBondAtomList = ring
                                                        .getConnectedAtomsList(ringAtom);
                                                Iterator<IAtom> neighborAtomsOfFirstBondAtomIterator = neighborAtomsOfFirstBondAtomList
                                                        .iterator();
                                                while (neighborAtomsOfFirstBondAtomIterator.hasNext()) {
                                                    IAtom neighborAtomOfFirst = neighborAtomsOfFirstBondAtomIterator
                                                            .next();
                                                    if (neighborAtomOfFirst != intersectingRingNeighborAtoms
                                                            .getAtom(0)) {
                                                        if (pruneMol.getBond(
                                                                mol.getBondNumber(neighborAtomOfFirst, ringAtom))
                                                                .getOrder() == IBond.Order.DOUBLE) {
                                                            firstAtomWithDoubleBondInDeletedRing = true;
                                                        }
                                                    }
                                                }

                                                // Test if second atom of fused
                                                // bond participates in a double
                                                // bond in the ring to be
                                                // deleted
                                                List<IAtom> neighborAtomsOfSecondBondAtomList = ring
                                                        .getConnectedAtomsList(intersectingRingNeighborAtoms.getAtom(0));
                                                Iterator<IAtom> neighborAtomsOfSecondBondAtomIterator = neighborAtomsOfSecondBondAtomList
                                                        .iterator();
                                                while (neighborAtomsOfSecondBondAtomIterator.hasNext()) {
                                                    IAtom neighborAtomOfSecond = neighborAtomsOfSecondBondAtomIterator
                                                            .next();
                                                    if (neighborAtomOfSecond != ringAtom) {
                                                        if (pruneMol.getBond(
                                                                mol.getBondNumber(neighborAtomOfSecond,
                                                                        intersectingRingNeighborAtoms.getAtom(0)))
                                                                .getOrder() == IBond.Order.DOUBLE) {
                                                            secondAtomWithDoubleBondInDeletedRing = true;
                                                        }
                                                    }
                                                }

                                                // If both true set fused bond
                                                // to double order
                                                if (firstAtomWithDoubleBondInDeletedRing
                                                        && secondAtomWithDoubleBondInDeletedRing) {
                                                    pruneMol.getBond(
                                                            mol.getBondNumber(ringAtom,
                                                                    intersectingRingNeighborAtoms.getAtom(0)))
                                                            .setOrder(IBond.Order.DOUBLE);
                                                }
                                                // catches case where shared
                                                // atom of fused benzenes is a
                                                // nitrogen
                                                // 2nd condition necessary to
                                                // prevent errors with
                                                // Indolizines.
                                                else if (ringAtom.getAtomicNumber() != null
                                                        && ringAtom.getAtomicNumber() == 7
                                                        && ringsOfRingAtom.getAtomContainer(n).getAtomCount() == 6) {
                                                    pruneMol.getBond(
                                                            mol.getBondNumber(ringAtom,
                                                                    intersectingRingNeighborAtoms.getAtom(0)))
                                                            .setOrder(IBond.Order.DOUBLE);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Mark bonds for deletion that are only
                // found in the ring to be deleted
                // Iterator<IBond> ringBondsIterator = ring.bonds();
                // while (ringBondsIterator.hasNext()){
                // IBond ringBond = ringBondsIterator.next();
                // //if bond only in a single ring, it can be deleted
                // if (rings.getRings(ringBond).size() == 1) {
                // atomsAndBondsToBeRemoved.addBond(pruneMol.getBond(mol.getBondNumber(ringBond)));
                // }
                // }

                // ORIGINAL RULE THREE (now part of the ring pruning procedure)
                // Convert shared bond of fused hetero-3-rings to double bonds
                // no control here for fusion - if not, new double bond is
                // deleted anyway
                if (ring.getRingSize() == 3 && numHet == 1) {
                    IAtom firstFusedAtom = new Atom();
                    IAtom secondFusedAtom = new Atom();
                    boolean firstFound = false;
                    for (IAtom ringAtom : ring.atoms()) {
                        if (ringAtom.getAtomicNumber() != null && ringAtom.getAtomicNumber() == 6 && !firstFound) {
                            firstFusedAtom = ringAtom;
                            firstFound = true;
                        } else if (ringAtom.getAtomicNumber() != null && ringAtom.getAtomicNumber() == 6 && firstFound) {
                            secondFusedAtom = ringAtom;
                        }
                    }
                    // set the fused bond to a double bond
                    (pruneMol.getBond(mol.getBondNumber(firstFusedAtom, secondFusedAtom))).setOrder(IBond.Order.DOUBLE);
                }

                // #### REMOVE SELECTED ATOMS AND BONDS FROM THE MOLECULE
                // Remove ring bonds
                IAtomContainer ringBonds = new AtomContainer();
                for (IBond ringBond : ring.bonds()) {
                    // if bond only in a single ring, it can be deleted
                    if (rings.getRings(ringBond).getAtomContainerCount() == 1) {
                        ringBonds.addBond(pruneMol.getBond(mol.getBondNumber(ringBond)));
                    }
                }
                pruneMol.remove(ringBonds);
                // Remove atoms and connected bonds
                for (IAtom removeAtom : atomsAndBondsToBeRemoved.atoms()) {
                    pruneMol.removeAtomAndConnectedElectronContainers(removeAtom);
                }

                // Control for bonds that are only connected to a single atom
                for (IBond pruneMolBond : pruneMol.bonds()) {
                    if (!pruneMol.contains(pruneMolBond.getAtom(0))) {
                        System.out.println("missing atom of bond");
                        pruneMol.removeBond(pruneMolBond);
                    }
                    if (!pruneMol.contains(pruneMolBond.getAtom(1))) {
                        System.out.println("missing atom of bond");
                        pruneMol.removeBond(pruneMolBond);
                    }
                }

                // Control for atoms without bonds
                for (IAtom pruneMolAtom : pruneMol.atoms()) {
                    if (pruneMol.getConnectedBondsCount(pruneMolAtom) == 0) {
                        System.out.println("unbound atom ");
                        pruneMol.removeAtom(pruneMolAtom);

                    }
                }

                // Control bond order of atoms in the pruned molecule
                // (currently only for carbons)
                boolean bondOrderFine = true;
                for (IAtom pruneMolAtom : pruneMol.atoms()) {
                    if (pruneMolAtom.getAtomicNumber() != null && pruneMolAtom.getAtomicNumber() == 6
                            && AtomContainerManipulator.getBondOrderSum(pruneMol, pruneMolAtom) > 4) {
                        bondOrderFine = false;
                    }
                }
                // System.out.println("after atom removal 02");

                // Accept pruned molecule only if the following conditions hold:
                // 1) there are more than 0 atoms left
                // 2) there are less atoms than before
                // 3) the molecule is still connected
                if (pruneMol.getAtomCount() > 0 && pruneMol.getAtomCount() < mol.getAtomCount()
                        && ConnectivityChecker.isConnected(pruneMol) && bondOrderFine) {

                    MoleculeConfigurator.prepare(pruneMol, true);
                    ScaffoldContainer parentScaffold = new ScaffoldContainer(pruneMol, false, false);

                    // Add and calculate properties
                    parentScaffold.setRRProperties(ring.getRingSize(),
                            (this.getSCPnumALB() - parentScaffold.getSCPnumALB()), numHet, numNAt, numOAt, numSAt,
                            ring.getFlag(CDKConstants.ISAROMATIC), heteroAtomAttachedLinker);
                    parentScaffold.calculateRAProperties(ringAssembly);

                    parentScaffold.setChildSmiles(this.getSMILES());

                    // Control for correct removal of aromatic rings
                    // if an aromatic ring was removed, the number of aromatic
                    // rings
                    // must be decreased exactly by one - otherwise the parent
                    // scaffold is not valid
                    if (!(ring.getFlag(CDKConstants.ISAROMATIC) && (this.SCPnumAroRings > (1 + parentScaffold
                            .getSCPnumAroRings())))) {
                        parentScaffoldsVector.addElement(parentScaffold);
                    }
                }
                // else{
                // System.out.println("NO VALID PARENT GENERATED");
                // System.out.println(pruneMol.getAtomCount() > 0);
                // System.out.println(pruneMol.getAtomCount() <
                // mol.getAtomCount());
                // //System.out.println(ConnectivityChecker.isConnected(pruneMol)
                // );
                // System.out.println(bondOrderFine);
                // String ctab = null;
                // String ctab2 = "";
                // try {
                // sw = new StringWriter();
                // mwriter = new MDLWriter(sw);
                // this.mwriter.write(pruneMol);
                // ctab2 = sw.toString();
                // // "$$$$" is not added automatically when single molecules
                // are written
                // ctab2 = ctab2 + "$$$$\n";
                // } catch (Exception e) {
                // System.out.println(e.toString());
                // }
                // System.out.println(ctab2);

                // }

            } catch (java.lang.CloneNotSupportedException e) {
                System.out.println("Molecule cannot be cloned");
            }
        }
        return parentScaffoldsVector;
    }

    /**
     * Method to remove linker atoms and exocyclic double bonds of a deleted
     * ring
     * 
     * @param heteroAtomAttachedLinker
     *            boolean that is set true if the linker is connected to a ring
     *            hetero atom
     * 
     */
    // Returns the atom with which the linker is connected to the remaining
    // scaffold
    private IAtomContainer removeLinkerOfDeletedRing(IAtomContainer atomsAndBondsToBeRemoved, IAtomContainer pruneMol,
            IAtom originAtom, IAtom atom) {
        // boolean heteroAtomAttachedLinker = false;

        IAtomContainer attachmentAtoms = new AtomContainer();

        // Atom rescueOriginAtom = new Atom();
        // Atom rescueAtom = new Atom();

        // Atom is not part of a ring
        if (!rings.contains(atom)) {

            // # Terminal sidechain
            // e.g. Exocyclic double bond
            if (mol.getConnectedAtomsCount(atom) == 1) {
                // add atom to linker
                atomsAndBondsToBeRemoved.addAtom(pruneMol.getAtom(mol.getAtomNumber(atom)));
                atomsAndBondsToBeRemoved.addAtom(pruneMol.getAtom(mol.getAtomNumber(originAtom)));
                // add connected bonds to linker
                // atomsAndBondsToBeRemoved.addBond(pruneMol.getBond(mol.getBondNumber(atom,
                // originAtom)));
            }

            // # Atom with two neighbors (including origin atom)
            else if (mol.getConnectedAtomsCount(atom) == 2) {
                // add parent atom to linker
                atomsAndBondsToBeRemoved.addAtom(pruneMol.getAtom(mol.getAtomNumber(originAtom)));
                // add connected bonds to linker
                // atomsAndBondsToBeRemoved.addBond(pruneMol.getBond(mol.getBondNumber(atom,
                // originAtom)));

                // Traverse on through the linker
                List<IAtom> neighborAtomsList = mol.getConnectedAtomsList(atom);
                Iterator<IAtom> neighborAtomsIterator = neighborAtomsList.iterator();
                while (neighborAtomsIterator.hasNext()) {
                    IAtom neighborAtom = neighborAtomsIterator.next();
                    if (!neighborAtom.equals(originAtom)) {
                        // heteroAtomAttachedLinker = heteroAtomAttachedLinker
                        // ||
                        // this.removeLinkerOfDeletedRing(atomsAndBondsToBeRemoved,
                        // pruneMol, atom, neighborAtom);
                        attachmentAtoms.add(this.removeLinkerOfDeletedRing(atomsAndBondsToBeRemoved, pruneMol, atom,
                                neighborAtom));
                    }
                }
            }

            // # Branching point
            // Two possible scenarios:
            // 1) atom is part of a linker between two other rings -> atom not
            // to be deleted
            // 2) atom has an exolinker double bond -> atom to be deleted
            else if (mol.getConnectedAtomsCount(atom) > 2) {

                // AtomContainer exolinkerDoubleBonds = new AtomContainer();

                // Determine number of nonterminal branches and
                // collect exolinker double bonds for potential deletion
                List<IAtom> neighborAtomsList = mol.getConnectedAtomsList(atom);
                Iterator<IAtom> neighborAtomsIterator = neighborAtomsList.iterator();
                int numNonTerminalBranches = 0;
                while (neighborAtomsIterator.hasNext()) {
                    IAtom neighborAtom = neighborAtomsIterator.next();
                    if (mol.getConnectedAtomsCount(neighborAtom) == 1) {
                        // exolinkerDoubleBonds.addAtom(pruneMol.getAtom(mol.getAtomNumber(neighborAtom)));
                        // exolinkerDoubleBonds.addBond(pruneMol.getBond(mol.getBondNumber(mol.getBond(atom,
                        // neighborAtom))));
                    } else {
                        numNonTerminalBranches++;
                    }
                }

                // If LESS than two non-terminal branches
                // (or < 3 including parent) -> Go on with deletion
                // Otherwise the atom is part of a linker
                // between two additional rings
                if (numNonTerminalBranches < 3) {
                    // atomsAndBondsToBeRemoved.add(exolinkerDoubleBonds);
                    atomsAndBondsToBeRemoved.addAtom(pruneMol.getAtom(mol.getAtomNumber(originAtom)));
                    // atomsAndBondsToBeRemoved.addBond(pruneMol.getBond(mol.getBondNumber(atom,
                    // originAtom)));

                    // Traverse on through the linker
                    neighborAtomsIterator = neighborAtomsList.iterator();
                    while (neighborAtomsIterator.hasNext()) {
                        IAtom neighborAtom = neighborAtomsIterator.next();
                        if (!neighborAtom.equals(originAtom)) {
                            // heteroAtomAttachedLinker =
                            // heteroAtomAttachedLinker ||
                            // this.removeLinkerOfDeletedRing(atomsAndBondsToBeRemoved,
                            // pruneMol, atom, neighborAtom);
                            attachmentAtoms.add(this.removeLinkerOfDeletedRing(atomsAndBondsToBeRemoved, pruneMol,
                                    atom, neighborAtom));
                        }
                    }
                }
                // Branching point is linking two rings
                else {
                    // Test for exolinker double bonds between atom and origin
                    if (!((mol.getBond(atom, originAtom)).getOrder() == IBond.Order.DOUBLE)) {
                        atomsAndBondsToBeRemoved.addAtom(pruneMol.getAtom(mol.getAtomNumber(originAtom)));
                        // atomsAndBondsToBeRemoved.addBond(pruneMol.getBond(mol.getBondNumber(atom,
                        // originAtom)));

                    }
                }
            }

        }
        // Atom is part of a ring that is not deleted - Stop the traversal
        // Test for exocyclic double bonds from within linkers
        // Test for heteroatom linkage parent priorization
        else {
            // Test for connection via double bond
            // -> exocyclic double bond via linker
            if (!((mol.getBond(atom, originAtom)).getOrder() == IBond.Order.DOUBLE)) {
                // add parent atom to linker
                atomsAndBondsToBeRemoved.addAtom(pruneMol.getAtom(mol.getAtomNumber(originAtom)));
                // add connected bonds to linker
                // atomsAndBondsToBeRemoved.addBond(pruneMol.getBond(mol.getBondNumber(atom,
                // originAtom)));

                attachmentAtoms.addAtom(atom);
                attachmentAtoms.addAtom(originAtom);
            }

            // Test for heteroatom linkage
            // if (atom.getAtomicNumber() != 6){
            // heteroAtomAttachedLinker = true;
            // }

        }

        return attachmentAtoms;
    }

}
