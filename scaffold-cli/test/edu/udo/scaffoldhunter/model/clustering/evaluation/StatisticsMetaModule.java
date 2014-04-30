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
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

/**
 * Computes the statistics for all {@link EvaluationResult}s with the same
 * settings. It is similar to {@link AccumulatorMetaModule}, but it saves
 * different statistics for one original result key: min, max, average, standard
 * deviation
 * 
 * @author Till Schäfer
 * 
 */
public class StatisticsMetaModule extends AccumulatorMetaModule {
    private static Logger logger = LoggerFactory.getLogger(StatisticsMetaModule.class);

    /**
     * Constructor
     */
    public StatisticsMetaModule() {
        super(null);
    }

    /**
     * Return one new {@link EvaluationResult} with the statistics of the
     * {@link EvaluationResult}s with the same settings.
     * 
     * @param results
     * @return
     */
    @Override
    protected EvaluationResult accumulateSingleGroup(Collection<EvaluationResult> results) {
        Preconditions.checkArgument(results.size() > 0);

        int collectionSize = results.size();
        EvaluationResult arbitraryResult = results.iterator().next();
        EvaluationResult retVal = EvaluationResult.copyEvaluationResultSettings(arbitraryResult);
        retVal.setMeasurement(retVal.getMeasurement() + "-statistics of " + collectionSize + " values");
        LinkedHashMap<String, Double> statistics = Maps.newLinkedHashMap();

        for (String key : arbitraryResult.getResults().keySet()) {
            int accumulationCount = 0;
            for (EvaluationResult result : results) {
                try {
                    Double currentVal = Double.parseDouble(result.getResults().get(key));

                    /*
                     * calculate min, max average
                     */
                    accumulationCount++;
                    Double oldVal = statistics.get(key + "__statistics_min");
                    statistics
                            .put(key + "__statistics_min", Math.min(currentVal, oldVal == null ? currentVal : oldVal));

                    oldVal = statistics.get(key + "__statistics_max");
                    statistics
                            .put(key + "__statistics_max", Math.max(currentVal, oldVal == null ? currentVal : oldVal));

                    oldVal = statistics.get(key + "__statistics_avg");
                    statistics.put(key + "__statistics_avg",
                            ((accumulationCount - 1) * (oldVal == null ? 0 : oldVal) + currentVal) / accumulationCount);
                } catch (NumberFormatException ex) {
                    logger.warn("Number format {} could not be parsed. Skipping value for key {} in accumulation.",
                            result.getResults().get(key), key);
                }
            }

            if (accumulationCount == 0) {
                statistics.put(key + key + "__statistics_min", null);
                statistics.put(key + key + "__statistics_max", null);
                statistics.put(key + key + "__statistics_avg", null);
                statistics.put(key + key + "__statistics_dev", null);
            } else {
                // calculate standard deviation
                Double average = statistics.get(key + "__statistics_avg");
                double standardDeviation = 0;
                for (EvaluationResult result : results) {
                    try {
                        double currentVal = Double.parseDouble(result.getResults().get(key));
                        standardDeviation += Math.pow(currentVal - average, 2);
                    } catch (NumberFormatException ex) {
                        /*
                         * no warning here because we already warned about
                         * number format above
                         */
                    }
                }
                standardDeviation = Math.sqrt(1.0 / accumulationCount * standardDeviation);
                statistics.put(key + "__statistics_dev", standardDeviation);
            }
        }

        // add new accumulated results to retVal
        for (Entry<String, Double> singleResult : statistics.entrySet()) {
            Double value = singleResult.getValue();
            retVal.addResult(singleResult.getKey(), value == null ? "NA" : value.toString());
        }

        return retVal;
    }
}
