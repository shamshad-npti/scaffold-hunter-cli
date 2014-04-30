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
import java.util.List;

/**
 * A {@link EvaluationMetaModule} takes an {@link EvaluationModule} as
 * arguments, runs and processes the {@link EvaluationResult}
 * 
 * @author Till Schäfer
 */
public interface EvaluationMetaModule {
    /**
     * Runs the {@link EvaluationMetaModule} for the specified
     * {@link EvaluationModule}
     * 
     * @param module
     *            the {@link EvaluationModule} to run
     * @return a {@link Collection} of {@link EvaluationResult}s
     * @throws EvaluationException
     */
    public Collection<EvaluationResult> run(EvaluationModule module) throws EvaluationException;

    /**
     * Runs a stack of {@link EvaluationMetaModule}s for the specified
     * {@link EvaluationModule}. The first Element in the metaModules
     * {@link List} is run last on the results of the second
     * {@link EvaluationMetaModule}, and so on.
     * 
     * @param metaModules
     *            the stack of {@link EvaluationMetaModule}s
     * @param module
     *            the
     * @return a {@link Collection} of {@link EvaluationResult}s
     * @throws EvaluationException
     */
    public Collection<EvaluationResult> run(List<EvaluationMetaModule> metaModules, EvaluationModule module)
            throws EvaluationException;
}
