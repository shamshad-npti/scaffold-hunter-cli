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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import edu.udo.scaffoldhunter.model.AccumulationFunction;

/**
 * Computes the accumulated values for all {@link EvaluationResult}s with the
 * same settings.
 * 
 * Works only with numerical Values!
 * 
 * @author Till Schäfer
 */
public class AccumulatorMetaModule implements EvaluationMetaModule {
    private static Logger logger = LoggerFactory.getLogger(AccumulatorMetaModule.class);

    private final AccumulationFunction accumulationFuncition;

    /**
     * Constructor
     * 
     * @param accumulationFuncition
     *            the {@link AccumulationFunction}
     */
    public AccumulatorMetaModule(AccumulationFunction accumulationFuncition) {
        this.accumulationFuncition = accumulationFuncition;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * edu.udo.scaffoldhunter.model.clustering.evaluation.EvaluationMetaModule
     * #run(edu.udo.scaffoldhunter.model.clustering.evaluation.EvaluationModule)
     */
    @Override
    public Collection<EvaluationResult> run(EvaluationModule module) throws EvaluationException {
        return accumulate(module.run());
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
    public Collection<EvaluationResult> run(List<EvaluationMetaModule> metaModules, EvaluationModule module)
            throws EvaluationException {
        if (metaModules.isEmpty()) {
            return run(module);
        } else {
            EvaluationMetaModule metaModule = metaModules.iterator().next();
            List<EvaluationMetaModule> metaModulesRest = metaModules.subList(1, metaModules.size());

            Collection<EvaluationResult> results = metaModule.run(metaModulesRest, module);
            return accumulate(results);
        }
    }

    /**
     * Calculates the accumulation of the results
     * 
     * @param results
     *            the {@link EvaluationResult}s to accumulate
     * @return the accumulated {@link EvaluationResult}s
     */
    private Collection<EvaluationResult> accumulate(Collection<EvaluationResult> results) {
        LinkedList<EvaluationResult> retVal = Lists.newLinkedList();
        Collection<Collection<EvaluationResult>> groupedResults = groupResults(results);

        for (Collection<EvaluationResult> sameMeasurement : groupedResults) {
            retVal.add(accumulateSingleGroup(sameMeasurement));
        }

        return retVal;
    }

    /**
     * Return one new {@link EvaluationResult} with the accumulated values of
     * the {@link EvaluationResult}s with the same settings.
     * 
     * @param results
     * @return
     */
    protected EvaluationResult accumulateSingleGroup(Collection<EvaluationResult> results) {
        Preconditions.checkArgument(results.size() > 0);

        int collectionSize = results.size();
        EvaluationResult arbitraryResult = results.iterator().next();
        EvaluationResult retVal = EvaluationResult.copyEvaluationResultSettings(arbitraryResult);
        retVal.setMeasurement(retVal.getMeasurement() + "-" + accumulationFuncition.name() + " of " + collectionSize
                + " values");
        LinkedHashMap<String, Double> accumulatedValues = Maps.newLinkedHashMap();

        // initialise retVal Results
        for (String key : arbitraryResult.getResults().keySet()) {
            switch (accumulationFuncition) {
            case Minimum:
                accumulatedValues.put(key, Double.POSITIVE_INFINITY);
                break;
            case Maximum:
                accumulatedValues.put(key, Double.NEGATIVE_INFINITY);
                break;
            case Average:
                // same as Sum
            case Sum:
                accumulatedValues.put(key, 0.0);
                break;
            default:
                break;
            }
        }

        // accumulate
        for (String key : arbitraryResult.getResults().keySet()) {
            int accumulationCount = 0;
            for (EvaluationResult result : results) {
                try {
                    Double currentVal = Double.parseDouble(result.getResults().get(key));
                    Double oldVal = accumulatedValues.get(key);

                    accumulationCount++;
                    switch (accumulationFuncition) {
                    case Minimum:
                        accumulatedValues.put(key, Math.min(currentVal, oldVal));
                        break;
                    case Maximum:
                        accumulatedValues.put(key, Math.max(currentVal, oldVal));
                        break;
                    case Average:
                        // accumulatedValues.put(key, oldVal + 1.0d / collectionSize * currentVal);
                        accumulatedValues.put(key, ((accumulationCount - 1) * oldVal + currentVal) / accumulationCount);
                        break;
                    case Sum:
                        accumulatedValues.put(key, oldVal + currentVal);
                        break;
                    default:
                        break;
                    }
                } catch (NumberFormatException ex) {
                    logger.warn("Number format {} could not be parsed. Skipping value for key {} in accumulation.",
                            result.getResults().get(key), key);
                }
            }
        }

        // add new accumulated results to retVal
        for (Entry<String, Double> singleResult : accumulatedValues.entrySet()) {
            retVal.addResult(singleResult.getKey(), singleResult.getValue().toString());
        }

        return retVal;
    }

    /**
     * Group {@link EvaluationResult}s based on their settings
     * 
     * @param results
     *            all {@link EvaluationResult}s
     * @return the groups of {@link EvaluationResult}s
     */
    private Collection<Collection<EvaluationResult>> groupResults(Collection<EvaluationResult> results) {
        LinkedList<Collection<EvaluationResult>> retVal = Lists.newLinkedList();
        while (!results.isEmpty()) {
            LinkedList<EvaluationResult> currentGroup = Lists.newLinkedList();
            Iterator<EvaluationResult> it = results.iterator();
            /*
             * the leader is compared to all other objects and defines the
             * settings for this group
             */
            EvaluationResult currentLeader = it.next();

            // remove leader from results
            it.remove();
            // find results with equal settings
            for (EvaluationResult result : results) {
                if (currentLeader.equalSettings(result)) {
                    currentGroup.add(result);
                }
            }
            // remove results with equal settings from results
            results.removeAll(currentGroup);
            currentGroup.add(currentLeader);

            retVal.add(currentGroup);
        }

        return retVal;
    }

}
