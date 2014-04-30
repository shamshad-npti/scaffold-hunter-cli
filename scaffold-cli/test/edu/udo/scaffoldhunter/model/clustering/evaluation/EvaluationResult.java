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

package edu.udo.scaffoldhunter.model.clustering.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.udo.scaffoldhunter.model.clustering.Distance;
import edu.udo.scaffoldhunter.model.clustering.Distances;
import edu.udo.scaffoldhunter.model.clustering.HierarchicalClusterings;
import edu.udo.scaffoldhunter.model.clustering.Linkages;
import edu.udo.scaffoldhunter.model.clustering.NNSearch;
import edu.udo.scaffoldhunter.model.clustering.NNSearch.NNSearchParameters;
import edu.udo.scaffoldhunter.model.clustering.NNSearchs;
import edu.udo.scaffoldhunter.model.db.Dataset;
import edu.udo.scaffoldhunter.model.db.PropertyDefinition;
import edu.udo.scaffoldhunter.model.db.Session;
import edu.udo.scaffoldhunter.model.db.Subset;

/**
 * @author Till Schäfer
 * 
 */
public class EvaluationResult {
    private final ArrayList<HierarchicalClusterings> clusterings = new ArrayList<HierarchicalClusterings>();
    private final ArrayList<NNSearchs> nnSearchs = Lists.newArrayList();
    private final ArrayList<NNSearchParameters> nnSearchParameters = Lists.newArrayList();
    private final ArrayList<Linkages> linkages = Lists.newArrayList();
    private final ArrayList<Distances> distances = Lists.newArrayList();
    private final ArrayList<Collection<PropertyDefinition>> propDefs = Lists.newArrayList();
    private final String session;
    private String measurement;
    private final LinkedHashMap<String, String> results = Maps.newLinkedHashMap();
    private final String subset;
    private final int subsetSize;
    private final String dataset;

    /**
     * Constructor
     * 
     * @param session
     *            the used {@link Session}
     * @param dataset
     *            the used {@link Dataset}
     * @param measurement
     *            the type of the measurement
     * @param subset
     *            the used {@link Subset}
     * @param subsetSize
     *            the size of the {@link Subset}
     */
    public EvaluationResult(String session, String dataset, String measurement, String subset, int subsetSize) {
        this.session = session;
        this.dataset = dataset;
        this.setMeasurement(measurement);
        this.subset = subset;
        this.subsetSize = subsetSize;
    }

    /**
     * Adds a clustering that is part of the measurement
     * 
     * @param clustering
     *            the clustering algorithm
     * @param nnSearch
     *            the {@link NNSearch} strategy
     * @param nnSearchParameters
     *            the {@link NNSearchParameters}
     * @param linkage
     *            the used {@link Linkages}
     * @param distance
     *            the used {@link Distance}
     * @param propDefs
     *            the uses Properties
     */
    public void addClustering(HierarchicalClusterings clustering, NNSearchs nnSearch,
            NNSearchParameters nnSearchParameters, Linkages linkage, Distances distance,
            Collection<PropertyDefinition> propDefs) {
        clusterings.add(clustering);
        nnSearchs.add(nnSearch);
        this.nnSearchParameters.add(nnSearchParameters);
        linkages.add(linkage);
        distances.add(distance);
        this.propDefs.add(propDefs);
    }

    /**
     * Adds a measurement result. Key must be unique!
     * 
     * @param key
     *            the key/description
     * @param value
     *            the value
     */
    public void addResult(String key, String value) {
        Preconditions.checkArgument(!results.containsKey(key), "the result already contains that key");
        results.put(key, value);
    }

    /**
     * Wrapper for addResult(String, String)
     * 
     * @param key
     *            the key/description
     * @param l
     *            the value
     */
    public void addResult(String key, long l) {
        addResult(key, Long.toString(l));
    }

    /**
     * Returns only the results as CSV data and no meta information
     * 
     * @return the CSV results
     */
    public String getCSVString() {
        StringBuilder builder = new StringBuilder();

        // key / value header
        builder.append("key,\tvalue");
        builder.append(System.getProperty("line.separator").toString());

        // key / value pairs
        for (Entry<String, String> entry : results.entrySet()) {
            builder.append(entry.getKey());
            builder.append(",");
            builder.append(entry.getValue());
            builder.append(System.getProperty("line.separator").toString());
        }

        builder.deleteCharAt(builder.length() - 1);
        return builder.toString();
    }

    /**
     * @return the measurement
     */
    public String getMeasurement() {
        return measurement;
    }

    /**
     * @param measurement
     *            the measurement to set
     */
    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }

    /**
     * @return the subsetSize
     */
    public int getSubsetSize() {
        return subsetSize;
    }

    /**
     * @return the results
     */
    public Map<String, String> getResults() {
        return Collections.unmodifiableMap(results);
    }

    /**
     * @return the clusterings
     */
    public ArrayList<HierarchicalClusterings> getClusterings() {
        return clusterings;
    }

    /**
     * @return the nnSearchs
     */
    public ArrayList<NNSearchs> getNnSearchs() {
        return nnSearchs;
    }

    /**
     * @return the nnSearchParameters
     */
    public ArrayList<NNSearchParameters> getNnSearchParameters() {
        return nnSearchParameters;
    }

    /**
     * @return the linkages
     */
    public ArrayList<Linkages> getLinkages() {
        return linkages;
    }

    /**
     * @return the distances
     */
    public ArrayList<Distances> getDistances() {
        return distances;
    }

    /**
     * @return the propDefs
     */
    public ArrayList<Collection<PropertyDefinition>> getPropDefs() {
        return propDefs;
    }

    /**
     * @return the session
     */
    public String getSession() {
        return session;
    }

    /**
     * @return the subset
     */
    public String getSubset() {
        return subset;
    }

    /**
     * @return the dataset
     */
    public String getDataset() {
        return dataset;
    }

    /**
     * Returns if this {@link EvaluationResult} was run with the same settings
     * as another {@link EvaluationResult}. Only result values may be different.
     * The result keys must be same. Sensitive to the ordering of clusterings,
     * nnSearchs, result keys, etc because of performance reasons.
     * 
     * @param other
     *            the {@link EvaluationResult} to compare with
     * @return if other was run with the same settings
     */
    public boolean equalSettings(EvaluationResult other) {
        if (!clusterings.equals(other.clusterings)) {
            return false;
        }
        if (!nnSearchs.equals(other.nnSearchs)) {
            return false;
        }
        if (!nnSearchParameters.equals(other.nnSearchParameters)) {
            return false;
        }
        if (!linkages.equals(other.linkages)) {
            return false;
        }
        if (!distances.equals(other.distances)) {
            return false;
        }
        if (!propDefs.equals(other.propDefs)) {
            return false;
        }
        if (!session.equals(other.session)) {
            return false;
        }
        if (!getMeasurement().equals(other.getMeasurement())) {
            return false;
        }
        if (!subset.equals(other.subset)) {
            return false;
        }
        if (subsetSize != other.subsetSize) {
            return false;
        }
        if (!dataset.equals(other.dataset)) {
            return false;
        }
        if (!results.keySet().equals(other.results.keySet())) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        // session
        builder.append("Session: ");
        builder.append(session);
        builder.append(System.getProperty("line.separator").toString());

        // dataset
        builder.append("Dataset: ");
        builder.append(dataset);
        builder.append(System.getProperty("line.separator").toString());

        // subset
        builder.append("Subset: ");
        builder.append(subset);
        builder.append(System.getProperty("line.separator").toString());

        // subset size
        builder.append("Subset size: ");
        builder.append(subsetSize);
        builder.append(System.getProperty("line.separator").toString());

        // measurement method
        builder.append("Meausrement: ");
        builder.append(getMeasurement());
        builder.append(System.getProperty("line.separator").toString());

        // clusterings
        for (int i = 0; i < clusterings.size(); i++) {
            // clustering
            builder.append("Clustring ");
            builder.append(i);
            builder.append(": ");
            builder.append(clusterings.get(i) != null ? clusterings.get(i).getName() : "none");
            builder.append(System.getProperty("line.separator").toString());

            // NN search strategy
            builder.append("NN search strategy ");
            builder.append(i);
            builder.append(": ");
            builder.append(nnSearchs.get(i) != null ? nnSearchs.get(i).getName() : "none");
            builder.append(System.getProperty("line.separator").toString());

            // NN search parameters
            builder.append("NN search parameters ");
            builder.append(i);
            builder.append(": ");
            builder.append(nnSearchParameters.get(i) != null ? nnSearchParameters.get(i).toString() : "none");
            builder.append(System.getProperty("line.separator").toString());

            // linkage
            builder.append("Linkage ");
            builder.append(i);
            builder.append(": ");
            builder.append(linkages.get(i) != null ? linkages.get(i).getName() : "none");
            builder.append(System.getProperty("line.separator").toString());

            // distance
            builder.append("Distance ");
            builder.append(i);
            builder.append(": ");
            builder.append(distances.get(i) != null ? distances.get(i).getName() : "none");
            builder.append(System.getProperty("line.separator").toString());

            // properties
            builder.append("Used Properties ");
            builder.append(i);
            builder.append(": ");
            if (propDefs.get(i) != null) {
                for (PropertyDefinition propDef : propDefs.get(i)) {
                    builder.append(propDef.getTitle());
                    builder.append(",");
                }
            } else {
                builder.append("none");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append(System.getProperty("line.separator").toString());
        }

        // the results
        builder.append(System.getProperty("line.separator").toString());
        builder.append(getCSVString());

        // end separation
        builder.append(System.getProperty("line.separator").toString());
        builder.append("--------------------------------------------------");
        builder.append(System.getProperty("line.separator").toString());
        builder.append(System.getProperty("line.separator").toString());

        return builder.toString();
    }

    /**
     * Copy function <b>only</b> for settings. Results are not copied!
     * 
     * @param evaluationResult
     * 
     * @return a new {@link EvaluationResult} with the same settings
     */
    public static EvaluationResult copyEvaluationResultSettings(EvaluationResult evaluationResult) {
        EvaluationResult retVal = new EvaluationResult(evaluationResult.getSession(), evaluationResult.getDataset(),
                evaluationResult.getMeasurement(), evaluationResult.getSubset(), evaluationResult.getSubsetSize());
        retVal.clusterings.addAll(evaluationResult.clusterings);
        retVal.nnSearchs.addAll(evaluationResult.nnSearchs);
        retVal.nnSearchParameters.addAll(evaluationResult.nnSearchParameters);
        retVal.linkages.addAll(evaluationResult.linkages);
        retVal.distances.addAll(evaluationResult.distances);
        retVal.propDefs.addAll(evaluationResult.propDefs);

        return retVal;
    }
}
