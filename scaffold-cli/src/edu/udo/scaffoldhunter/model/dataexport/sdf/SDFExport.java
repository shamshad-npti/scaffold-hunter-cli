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

package edu.udo.scaffoldhunter.model.dataexport.sdf;

import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.SDFWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.udo.scaffoldhunter.model.dataexport.ExportInterface;

/**
 * @author bernhard
 * 
 */
public class SDFExport implements ExportInterface {
    private static Logger logger = LoggerFactory.getLogger(SDFExport.class);
    JPanel configurationPanel = new JPanel();
    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.dataexport.ExportInterface#writeData(java
     * .lang.Iterable, java.lang.String[], java.lang.String)
     */
    @Override
    public void writeData(Iterable<IAtomContainer> molecules, String[] propertyNames, String filename) {
        SDFWriter myWriter = null;
        try {
            myWriter = new SDFWriter(new FileWriter(filename));
            for (IAtomContainer cur : molecules) {
                myWriter.write(cur);
            }

            JOptionPane.showMessageDialog(configurationPanel.getParent(), "Export successful", "Export", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(configurationPanel.getParent(), "Export failed. The resulting file may be corrupted or incomplete.", "Export", JOptionPane.INFORMATION_MESSAGE);
        } catch (CDKException e) {
            JOptionPane.showMessageDialog(configurationPanel.getParent(), "Export failed. The resulting file may be corrupted or incomplete.", "Export", JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException e) {
            if(e.getCause() instanceof CDKException)
                JOptionPane.showMessageDialog(configurationPanel.getParent(), "Export failed. The resulting file may be corrupted or incomplete.", "Export", JOptionPane.INFORMATION_MESSAGE);
            else {
                logger.warn("Non-CDKException occured during write process");
                throw e;
            }
        }
        finally{
            if(myWriter != null) {
                try {
                    myWriter.close();
                } catch (IOException e) {
                    logger.warn("Exception occured on closing the SDFWriter");
                }
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.dataexport.ExportInterface#configurationPanel
     * ()
     */
    @Override
    public JPanel getConfigurationPanel() {
        return configurationPanel;
    }

}
