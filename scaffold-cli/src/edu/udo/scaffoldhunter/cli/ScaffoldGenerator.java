/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012-2013 LS11
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

package edu.udo.scaffoldhunter.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.SDFWriter;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.silent.SilentChemObjectBuilder;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.converters.FileConverter;
import com.google.common.io.Files;

import edu.udo.scaffoldhunter.model.db.Scaffold;
import edu.udo.scaffoldhunter.model.treegen.CDKHelpers;
import edu.udo.scaffoldhunter.model.treegen.GeneratorOptions;
import edu.udo.scaffoldhunter.model.treegen.ScaffoldContainer;
import edu.udo.scaffoldhunter.model.treegen.net.ScaffoldNetwork;
import edu.udo.scaffoldhunter.model.treegen.prioritization.ScaffoldPrioritization;

/**
 * Command line interface to split an sd file according to scaffold of a specified
 * size.
 * 
 * @author Nils Kriege
 */
public class ScaffoldGenerator {
    
    /**
     * Command line arguments
     */
    public static class Args {
        
        @SuppressWarnings("javadoc")
        @Parameter(names = {"-k","--scaffold-ring-size"}, description = "number of scaffold rings")
        public int k = 1;
        
        @SuppressWarnings("javadoc")
        @Parameter(description = "SDfile", converter = FileConverter.class, required = true)
        public List<File> file;

        @SuppressWarnings("javadoc")
        @Parameter(names = {"-n","--network"}, description = "generate scaffold network")
        public boolean network = false;
        
        @SuppressWarnings("javadoc")
        @Parameter(names = {"-h","--help"}, description = "show usage information", help = true)
        public boolean help = false;


    }

    private static GeneratorOptions generatorOptions = new GeneratorOptions();
    private static ScaffoldPrioritization prio = new ScaffoldPrioritization();
    
    private static int outFileCount = 0;
    private static HashMap<String, OutputScaffold> outScaffolds = new HashMap<String, OutputScaffold>();
    
    /**
     * Stores the information required for an output scaffold
     */
    public static class OutputScaffold {
        private String smiles;
        private File file;
        private int count = 0;

        /**
         * @param smiles
         * @throws FileNotFoundException 
         */
        public OutputScaffold(String smiles) throws FileNotFoundException {
            this.smiles = smiles;
            String fileName = (outFileCount++)+".sdf";
            this.file = new File(fileName);
            file.delete();
        }

        /**
         * @return the smiles
         */
        public String getSmiles() {
            return smiles;
        }

        /**
         * @return the file
         */
        public File getFile() {
            return file;
        }
        
        /**
         * @return the counter
         */
        public int getCount() {
            return count;
        }
        
        /**
         * Creates the final filename.
         * @throws IOException 
         */
        public void rename() throws IOException {
            String fileName = smiles;
            fileName = fileName.replaceAll("[^a-zA-Z0-9.-]", "-");
            // avoid to long file names
            if (fileName.length() > 200) {
                fileName = fileName.substring(0, 197)+"etc";
            }
            fileName += "_"+file.getName();
            fileName = count+"_"+fileName;
            File newFile = new File(fileName);
            Files.move(file, newFile);
            file = newFile;
        }

        /**
         * @param mol
         * @throws IOException 
         * @throws CDKException 
         */
        public void write(IMolecule mol) throws IOException, CDKException {
            // this results in many open/close operations
            // typically a process may only open a limited amount of files
            // and keeping them open will result in exceptions
            SDFWriter writer = new SDFWriter(new FileOutputStream(file,true));
            writer.write(mol);
            writer.close();
            count++;
        }
    }
    
    /**
     * Generates all ancestors and inserts them into the network.
     * @param source the scaffold
     * @param net the network
     * @param network generate scaffold network parent
     */
    private static void processParents(ScaffoldContainer source, ScaffoldNetwork net, boolean network) {
        Vector<ScaffoldContainer> parents = source.getAllParentScaffolds();
        ScaffoldNetwork.Node sourceNode = net.getScaffoldNode(source.getSMILES());
        
        if (!network && !parents.isEmpty()) {
            ScaffoldContainer scaffold = prio.selectParentScaffoldOriginalRules(parents);
            parents.clear();
            parents.add(scaffold);
        }
        
        for (ScaffoldContainer parent : parents) {
            if (addScaffold(parent, net)) {
                processParents(parent, net, network);
            }
            ScaffoldNetwork.Node parentNode = net.getScaffoldNode(parent.getSMILES());
            sourceNode.addParent(parentNode);
            parentNode.addChild(sourceNode);
        }
    }

    /**
     * Inserts a scaffold into the network.
     * @param scaffoldContainer the scaffold
     * @param net the network
     * @return false iff the scaffold was already contained in the network
     */
    private static boolean addScaffold(ScaffoldContainer scaffoldContainer, ScaffoldNetwork net) {

        String smiles = scaffoldContainer.getSMILES();

        if (net.contains(smiles)) {
            return false;
        }

        Scaffold scaffold = new Scaffold();
        scaffold.setSmiles(smiles);
        scaffold.setTitle(smiles);
        scaffold.setHierarchyLevel(scaffoldContainer.getSCPnumRings());

        net.addScaffold(scaffold);

        return true;
    }

    /**
     * Main procedure.
     * @param args
     * @throws CDKException
     * @throws IOException
     * @throws CloneNotSupportedException
     */
    public static void main(String[] args) throws CDKException, IOException, CloneNotSupportedException {

        // parse arguments
        Args arg = new Args();
        JCommander jc = new JCommander(arg, args);
        if (arg.help) {
            jc.usage();
            System.exit(0);
        }

        // create network
        ScaffoldNetwork net = new ScaffoldNetwork();

        // initialize statistics
        int molCount = 0;
        int molWritten = 0;
        
        // process molecules in the input file
        IteratingMDLReader reader = new IteratingMDLReader(new FileInputStream(arg.file.get(0)),
                SilentChemObjectBuilder.getInstance());
        while (reader.hasNext()) {

            IMolecule molIn = (IMolecule) reader.next();
            IMolecule mol = molIn.clone();

            // only the largest fragment is used to build the scaffold tree
            // the rest (i.e. solvents) is ignored
            mol = (IMolecule) CDKHelpers.getLargestFragment(mol);

            // MurckoScaffold
            ScaffoldContainer murckoScaffold = new ScaffoldContainer(mol, true, generatorOptions.isDeglycosilate());

            // Insert murcko scaffold
            boolean isNew = addScaffold(murckoScaffold, net);
            if (isNew) {
                processParents(murckoScaffold, net, arg.network);
            }

            // write output
            ArrayList<String> outputScaffolds = findAncestors(arg.k, murckoScaffold.getSMILES(), net);
            for (String s : outputScaffolds) {
                writeOut(s, molIn);
                molWritten++;
            }
            
            molCount++;
            System.out.print('.');
            if (molCount % 60 == 0) System.out.println(); 
        }
        
        // rename files
        for (OutputScaffold out : outScaffolds.values()) {
            out.rename();
        }
        writeOut(net);
        
        // print report
        System.out.println();
        System.out.println("Moleules processed:      "+molCount);
        System.out.println("Unique scaffolds:        "+net.nodes().size());
        System.out.println(arg.k+"-ring scaffolds:        "+outScaffolds.size());
        System.out.println("Molecules written:       "+molWritten);
    }
    
    /**
     * Writes out the specified molecule to the file associated with the scaffold
     * associated with the given SMILES.
     * @param smiles smiles of the scaffold
     * @param mol the molecule
     * @throws CDKException
     * @throws IOException 
     */
    private static void writeOut(String smiles, IMolecule mol) throws CDKException, IOException {
        OutputScaffold out = outScaffolds.get(smiles);
        if (out == null) {
            out = new OutputScaffold(smiles);
            outScaffolds.put(smiles, out);
        }
        out.write(mol);
    }
    
    /**
     * Writes out the parent child relationships of a scaffold network.
     * @param net the scaffold network
     * @throws IOException
     */
    private static void writeOut(ScaffoldNetwork net) throws IOException {
        FileWriter writer = new FileWriter("scaffolds.smi");
        for (ScaffoldNetwork.Node n : net.nodes()) {
            String smiles = n.getScaffold().getSmiles();
            for (ScaffoldNetwork.Node c : n.getChildren()) {
                String childSmiles = c.getScaffold().getSmiles();
                writer.append(smiles + "\t" + childSmiles + "\n");
            }
        }
        writer.close();
    }
    
    /**
     * Finds the ancestors of the given scaffold in the network with k rings.
     * @param k number of rings/hierarchy
     * @param scaffoldSMILES smiles of the scaffold that must be contained in the network
     * @param net the scaffold network
     * @return list smiles of the ancestors
     */
    private static ArrayList<String> findAncestors(int k, String scaffoldSMILES, ScaffoldNetwork net) {
        ArrayList<String> result = new ArrayList<String>();
        
        if (k==0 && net.contains(scaffoldSMILES)) {
            result.add("ROOT");
            return result;
        }
        
        ScaffoldNetwork.Node n = net.getScaffoldNode(scaffoldSMILES);
        LinkedList<ScaffoldNetwork.Node> queue = new LinkedList<ScaffoldNetwork.Node>();
        queue.add(n);
        
        while (!queue.isEmpty()) {
            n = queue.poll();
            if (n.getHierarchyLevel() == k) {
                result.add(n.getScaffold().getSmiles());
            }
            if (n.getHierarchyLevel() > k) {
                queue.addAll(n.getParents());
            }
        }
        
        return result;
    }

}
