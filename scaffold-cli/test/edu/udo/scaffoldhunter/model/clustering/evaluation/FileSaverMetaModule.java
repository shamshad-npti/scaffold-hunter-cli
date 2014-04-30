/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * Copyright (C) 2012 LS11
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

package edu.udo.scaffoldhunter.model.clustering.evaluation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.io.FilenameUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * Saves the EvaluationResults to disk
 * 
 * @author Till Schäfer
 */
public class FileSaverMetaModule implements EvaluationMetaModule {

    private final String path;
    private final boolean singleFile;
    private final boolean saveCSV;
    private final boolean orderCSVBySubsetSize;
    private int count = 0;

    /**
     * Constructor
     * 
     * @param path
     *            the path to the file to store
     * @param singleFile
     *            If true all results are stored in a single file. If false
     *            store separate files for each {@link EvaluationResult}. In the
     *            second case the file name is extended by "-moduleName_count",
     *            where count is incremented for each {@link EvaluationResult}.
     * @param saveCSV
     *            store a second file with the CSV data only.
     * @param orderCSVBySubsetSize
     *            If true store one CSV line for each subset size. All
     *            {@link EvaluationResult} keys are used as columns. If
     *            saveCVS=false this option is ignored. Default is false.
     */
    public FileSaverMetaModule(String path, boolean singleFile, boolean saveCSV, boolean orderCSVBySubsetSize) {
        Preconditions.checkArgument(!(singleFile == true && saveCSV == true && orderCSVBySubsetSize == false),
                "cannot store single csv file if orderCSVBySubsetSize is false");

        this.path = path;
        this.singleFile = singleFile;
        this.saveCSV = saveCSV;
        this.orderCSVBySubsetSize = orderCSVBySubsetSize;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.evaluation.EvaluationMetaModule
     * #run(edu.udo.scaffoldhunter.model.clustering.evaluation.EvaluationModule)
     */
    @Override
    public Collection<EvaluationResult> run(EvaluationModule module) {
        Collection<EvaluationResult> results = module.run();
        save(results);
        return results;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.evaluation.EvaluationMetaModule
     * #run(java.util.List,
     * edu.udo.scaffoldhunter.model.clustering.evaluation.EvaluationModule)
     */
    @Override
    public Collection<EvaluationResult> run(List<EvaluationMetaModule> metaModules, EvaluationModule module) {
        // reset separate file counter
        count = 0;

        if (metaModules.isEmpty()) {
            return run(module);
        } else {
            EvaluationMetaModule metaModule = metaModules.iterator().next();
            List<EvaluationMetaModule> metaModulesRest = metaModules.subList(1, metaModules.size());

            Collection<EvaluationResult> results = metaModule.run(metaModulesRest, module);
            save(results);
            return results;
        }
    }

    /**
     * Save the {@link EvaluationResult}s on disc
     * 
     * @param results
     *            the {@link EvaluationResult}s to save
     */
    private void save(Collection<EvaluationResult> results) {
        if (singleFile) {
            for (EvaluationResult result : results) {
                saveToFile(path, result.toString());
            }

            if (saveCSV) {
                if (orderCSVBySubsetSize) {
                    saveCSVOrderedBySubsetSize(results);
                } else {
                    throw new IllegalStateException();
                }
            }
        } else {
            if (saveCSV) {
                if (orderCSVBySubsetSize) {
                    saveSingleResults(results, false);
                    saveCSVOrderedBySubsetSize(results);
                } else {
                    saveSingleResults(results, true);
                }
            } else {
                saveSingleResults(results, false);
            }
        }
    }

    /**
     * Save the each result to a single File
     * 
     * @param results
     */
    private void saveSingleResults(Collection<EvaluationResult> results, boolean csv) {
        for (EvaluationResult result : results) {
            String uniqueFile = getUniqueBaseName(result);
            saveToFile(uniqueFile + FilenameUtils.EXTENSION_SEPARATOR + FilenameUtils.getExtension(path),
                    result.toString());
            if (csv) {
                saveToFile(uniqueFile + FilenameUtils.EXTENSION_SEPARATOR + "csv", result.getCSVString());
            }
        }
    }

    /**
     * Construct and save the OrderedBySubsetSize CSV string / file
     * 
     * @param results
     */
    private void saveCSVOrderedBySubsetSize(Collection<EvaluationResult> results) {
        StringBuilder csvString = new StringBuilder();

        // ordered keys of all results
        TreeSet<String> keys = Sets.newTreeSet();
        for (EvaluationResult result : results) {
            keys.addAll(result.getResults().keySet());
        }

        // create csv header
        csvString.append("subset size,");
        for (String key : keys) {
            csvString.append(key);
            csvString.append(",");
        }
        csvString.deleteCharAt(csvString.length() - 1);

        // create csv data & store single result file (not storing
        // csv file at this point)
        for (EvaluationResult result : results) {
            csvString.append(System.getProperty("line.separator").toString());
            csvString.append(result.getSubsetSize());
            csvString.append(",");
            for (String key : keys) {
                String value = result.getResults().get(key);
                if (value != null) {
                    csvString.append(value);
                }
                csvString.append(",");
            }
            csvString.deleteCharAt(csvString.length() - 1);
        }

        saveToFile(FilenameUtils.concat(FilenameUtils.getFullPathNoEndSeparator(path), FilenameUtils.getBaseName(path))
                + FilenameUtils.EXTENSION_SEPARATOR + "csv", csvString.toString());
    }

    private void saveToFile(String path, String content) {

        try {
            FileWriter fstream = new FileWriter(path, true);
            BufferedWriter out = new BufferedWriter(fstream);
            out.write(content);
            out.close();
            fstream.close();
        } catch (IOException e) {
            throw new EvaluationException("Saving failed", e);
        }
    }

    private String getUniqueBaseName(EvaluationResult result) {
        count++;
        return FilenameUtils.concat(FilenameUtils.getFullPathNoEndSeparator(path), FilenameUtils.getBaseName(path))
                + "-" + result.getMeasurement() + "_" + Integer.toString(count - 1);
    }
}
