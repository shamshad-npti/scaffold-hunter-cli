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

package edu.udo.scaffoldhunter.model.treegen.prioritization;

import edu.udo.scaffoldhunter.model.db.Rule;
import edu.udo.scaffoldhunter.model.treegen.ScaffoldContainer;

/**
 * Implementation of custom rules for parent scaffold selection.
 * 
 * @author Nils Kriege
 */
public class CustomScaffoldFilterRule extends AbstractMinScaffoldFilterRule {
    
    private Rule rule;
    
    /**
     * Creates a new custom rule filter.
     * @param rule the rule
     */
    public CustomScaffoldFilterRule(Rule rule) {
        this.rule = rule;
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.treegen.prioritization.ScaffoldFilterRule#getName()
     */
    @Override
    public String getName() {
        return "CustomRule_"+rule.getRule().toString()+"_"+(rule.isAscending() ? "MAX" : "MIN"); 
    }

    /* (non-Javadoc)
     * @see edu.udo.scaffoldhunter.model.treegen.prioritization.AbstractMinScaffoldFilterRule#getProperty(edu.udo.scaffoldhunter.model.treegen.ScaffoldContainer)
     */
    @Override
    public int getProperty(ScaffoldContainer sc) {
        int value;
        
        switch(rule.getRule()){
        case SCPnoLinkerBonds:  value = sc.getSCPnumALB(); break;
        case SCPdelta:          value = sc.getSCPdelta(); break;
        case SCPabsDelta:       value = sc.getSCPabsDelta(); break;
        case SCPnoAroRings:     value = sc.getSCPnumAroRings(); break;
        case SCPnoHetAt:        value = sc.getSCPnumHetAt(); break;
        case SCPnoNAt:          value = sc.getSCPnumNAt(); break;
        case SCPnoOAt:          value = sc.getSCPnumOAt(); break;
        case SCPnoSAt:          value = sc.getSCPnumSAt(); break;
        case RAPdelta:          value = sc.getRAPdelta(); break;
        case RAPabsDelta:       value = sc.getRAPabsDelta(); break;
        case RAPnoRings:        value = sc.getRAPnumRings(); break;
        case RAPnoAroRings:     value = sc.getRAPnumAroRings(); break;
        case RAPnoHetAt:        value = sc.getRAPnumHetAt(); break;
        case RAPnoNAt:          value = sc.getRAPnumNAt(); break;
        case RAPnoOAt:          value = sc.getRAPnumOAt(); break;
        case RAPnoSAt:          value = sc.getRAPnumSAt(); break;
        case RRPringSize:       value = sc.getRRPringSize(); break;
        case RRPnoHetAt:        value = sc.getRRPnumHetAt(); break;
        case RRPnoNAt:          value = sc.getRRPnumNAt(); break;
        case RRPnoOAt:          value = sc.getRRPnumOAt(); break;
        case RRPnoSAt:          value = sc.getRRPnumSAt(); break;
        case RRPhetAtLinked:    value = sc.getRRPhetatlinked() ? 1 : 0; break;
        case RRPsize3:          value = sc.getRRPringSize() == 3 ? 1 : 0; break;
        case RRPsize4:          value = sc.getRRPringSize() == 4 ? 1 : 0; break;
        case RRPsize5:          value = sc.getRRPringSize() == 5 ? 1 : 0; break;
        case RRPsize6:          value = sc.getRRPringSize() == 6 ? 1 : 0; break;
        case RRPsize7:          value = sc.getRRPringSize() == 7 ? 1 : 0; break;
        case RRPsize8:          value = sc.getRRPringSize() == 8 ? 1 : 0; break;
        case RRPsize9:          value = sc.getRRPringSize() == 9 ? 1 : 0; break;
        case RRPsize10:         value = sc.getRRPringSize() == 10 ? 1 : 0; break;
        case RRPsize11:         value = sc.getRRPringSize() == 11 ? 1 : 0; break;
        case RRPsize11p:        value = sc.getRRPringSize() > 11 ? 1 : 0; break;
        case RRPlinkerLen1:     value = sc.getRRPlinkerSize() == 1 ? 1 : 0; break;
        case RRPlinkerLen2:     value = sc.getRRPlinkerSize() == 2 ? 1 : 0; break;
        case RRPlinkerLen3:     value = sc.getRRPlinkerSize() == 3 ? 1 : 0; break;
        case RRPlinkerLen4:     value = sc.getRRPlinkerSize() == 4 ? 1 : 0; break;
        case RRPlinkerLen5:     value = sc.getRRPlinkerSize() == 5 ? 1 : 0; break;
        case RRPlinkerLen6:     value = sc.getRRPlinkerSize() == 6 ? 1 : 0; break;
        case RRPlinkerLen7:     value = sc.getRRPlinkerSize() == 7 ? 1 : 0; break;
        case RRPlinkerLen7p:    value = sc.getRRPlinkerSize() > 7 ? 1 : 0; break;
        default :
            throw new UnsupportedOperationException("Not implemented yet");
        }

        return rule.isAscending() ? -value : value;
    }

}
