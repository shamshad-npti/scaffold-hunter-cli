/*
 * Scaffold Hunter
 * Copyright (C) 2006-2008 PG504
 * Copyright (C) 2010-2011 PG552
 * See README.txt in the root directory of the Scaffold Hunter source tree
 * for details.
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

package edu.udo.scaffoldhunter.model;

/**
 * @author Till Schäfer
 * 
 */
public enum RuleType {
    /**
     * number of acyclic linker bonds
     */
    SCPnoLinkerBonds ("number of acyclic linker bonds"),
    /**
     * delta value indicating nonlinear ring fusions, spiro systems bridged
     * systems as defined in http://pubs.acs.org/doi/abs/10.1021/ci600338x
     */
    SCPdelta ("delta value indicating nonlinear ring fusions, spiro systems bridged systems as defined in http://pubs.acs.org/doi/abs/10.1021/ci600338x"),
    /**
     * absolute delta value indicating nonlinear ring fusions, spiro systems
     * bridged systems as defined in
     * http://pubs.acs.org/doi/abs/10.1021/ci600338x
     */
    SCPabsDelta ("absolute delta value indicating nonlinear ring fusions, spiro systems bridged systems as defined in http://pubs.acs.org/doi/abs/10.1021/ci600338x"), 
    /**
     * number of aromatic rings
     */
    SCPnoAroRings ("number of aromatic rings"), 
    /**
     * number of heteroatoms
     */
    SCPnoHetAt ("number of heteroatoms"), 
    /**
     * number of nitrogen atoms
     */
    SCPnoNAt ("number of nitrogen atoms"), 
    /**
     * number of oxygen atoms
     */
    SCPnoOAt ("number of oxygen atoms"), 
    /**
     * number of sulfur atoms
     */
    SCPnoSAt ("number of sulfur atoms"), 
    /**
     * number of rings
     */
    SCPnoRings ("number of rings"),
    /**
     * delta value
     */
    RAPdelta ("delta value"), 
    /**
     * absolute delta value
     */
    RAPabsDelta ("absolute delta value"), 
    /**
     * number of rings
     */
    RAPnoRings ("number of rings"), 
    /**
     * number of aromatic rings
     */
    RAPnoAroRings ("number of aromatic rings"), 
    /**
     * number of heteroatoms
     */
    RAPnoHetAt ("number of heteroatoms"), 
    /**
     * number of nitrogen atoms
     */
    RAPnoNAt ("number of nitrogen atoms"), 
    /**
     * number of oxygen atoms
     */
    RAPnoOAt ("number of oxygen atoms"),
    /**
     * number of sulfur atoms
     */
    RAPnoSAt ("number of sulfur atoms"), 
    /**
     * size of removed ring
     */
    RRPringSize ("size of removed ring"), 
    /**
     * number of heteroatoms
     */
    RRPnoHetAt ("number of heteroatoms"), 
    /**
     * number of nitrogen atoms
     */
    RRPnoNAt ("number of nitrogen atoms"), 
    /**
     * number of oxygen atoms
     */
    RRPnoOAt ("number of oxygen atoms"), 
    /**
     * number of sulfur atoms
     */
    RRPnoSAt ("number of sulfur atoms"),
    /**
     * binary descriptor (1=True, 0=False) indicating whether removed ring was
     * linked via a linker to a heteroatom in a ring
     */
    RRPhetAtLinked ("binary descriptor (1=True, 0=False) indicating whether removed ring was linked via a linker to a heteroatom in aring"), 
    /**
     * binary descriptor: removed ring of size 3
     */
    RRPsize3 ("binary descriptor: removed ring of size 3"), 
    /**
     * binary descriptor: removed ring of size 4
     */
    RRPsize4 ("binary descriptor: removed ring of size 4"), 
    /**
     * binary descriptor: removed ring of size 5
     */
    RRPsize5 ("binary descriptor: removed ring of size 5"), 
    /**
     * binary descriptor: removed ring of size 6
     */
    RRPsize6 ("binary descriptor: removed ring of size 6"), 
    /**
     * binary descriptor: removed ring of size 7
     */
    RRPsize7 ("binary descriptor: removed ring of size 7"), 
    /**
     * binary descriptor: removed ring of size 8
     */
    RRPsize8 ("binary descriptor: removed ring of size 8"),
    /**
     * binary descriptor: removed ring of size 9
     */
    RRPsize9 ("binary descriptor: removed ring of size 9"),
    /**
     * binary descriptor: removed ring of size 10
     */
    RRPsize10 ("binary descriptor: removed ring of size 10"),
    /**
     * binary descriptor: removed ring of size 11
     */
    RRPsize11 ("binary descriptor: removed ring of size 11"),
    /**
     * binary descriptor: removed ring of size more than 11
     */
    RRPsize11p ("binary descriptor: removed ring of size more than 11"),
    /**
     * binary descriptor: removed ring connected via linker of length 1
     */
    RRPlinkerLen1 ("binary descriptor: removed ring connected via linker of length 1"),
    /**
     * binary descriptor: removed ring connected via linker of length 2
     */
    RRPlinkerLen2 ("binary descriptor: removed ring connected via linker of length 2"),
    /**
     * binary descriptor: removed ring connected via linker of length 3
     */
    RRPlinkerLen3 ("binary descriptor: removed ring connected via linker of length 3"),
    /**
     * binary descriptor: removed ring connected via linker of length 4
     */
    RRPlinkerLen4 ("binary descriptor: removed ring connected via linker of length 4"),
    /**
     * binary descriptor: removed ring connected via linker of length 5
     */
    RRPlinkerLen5 ("binary descriptor: removed ring connected via linker of length 5"),
    /**
     * binary descriptor: removed ring connected via linker of length 6
     */
    RRPlinkerLen6 ("binary descriptor: removed ring connected via linker of length 6"),
    /**
     * binary descriptor: removed ring connected via linker of length 7
     */
    RRPlinkerLen7 ("binary descriptor: removed ring connected via linker of length 7"),
    /**
     * binary descriptor: removed ring connected via linker of length more than 7
     */
    RRPlinkerLen7p ("binary descriptor: removed ring connected via linker of length more than 7");
    
    
    private final String description;
    
    RuleType(String description) {
        this.description = description;
    }
    
    /**
     * Returns a detailed description of this <code>RuleType</code>
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }
}
