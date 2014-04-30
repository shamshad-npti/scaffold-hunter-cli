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
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

/**
 * Repeats the argument n times
 * 
 * @author Till Schäfer
 * 
 */
public class RepeaterMetaModule implements EvaluationMetaModule {
    private static Logger logger = LoggerFactory.getLogger(RepeaterMetaModule.class);

    private final int count;

    private final boolean leafFirstOut;

    /**
     * Constructor
     * 
     * @param count
     *            the count of repeats
     * @param leaveFirstOut
     *            when true the first run is not added to the
     *            {@link EvaluationResult}s
     */
    public RepeaterMetaModule(int count, boolean leaveFirstOut) {
        this.count = count;
        this.leafFirstOut = leaveFirstOut;
        Preconditions.checkArgument(count > 0);
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
        Preconditions.checkNotNull(module);

        LinkedList<EvaluationResult> retVals = Lists.newLinkedList();

        for (int i = 0; i < count; i++) {
            LinkedList<EvaluationResult> retVal = Lists.newLinkedList();
            logger.info("Running repeat {} of {}", i + 1, count);

            retVal.addAll(module.run());
            if (!leafFirstOut || i > 0) {
                retVals.addAll(retVal);
            }
        }

        return retVals;
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
        Preconditions.checkNotNull(module);
        Preconditions.checkNotNull(metaModules);

        LinkedList<EvaluationResult> retVals = Lists.newLinkedList();

        if (metaModules.isEmpty()) {
            return run(module);
        } else {
            EvaluationMetaModule metaModule = metaModules.iterator().next();
            List<EvaluationMetaModule> metaModulesRest = metaModules.subList(1, metaModules.size());

            for (int i = 0; i < count; i++) {
                LinkedList<EvaluationResult> retVal = Lists.newLinkedList();
                logger.info("Running repeat {} of {}", i + 1, count);

                retVal.addAll(metaModule.run(metaModulesRest, module));

                if (!leafFirstOut || i > 0) {
                    retVals.addAll(retVal);
                }
            }

            return retVals;
        }

    }

}
