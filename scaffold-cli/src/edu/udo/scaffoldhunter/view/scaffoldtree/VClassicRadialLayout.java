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

package edu.udo.scaffoldhunter.view.scaffoldtree;

/**
 * This layout is the radial layout which is described in the paper 'Drawing
 * Free Trees' from Peter Eades
 * 
 * @author wiesniewski
 */
public class VClassicRadialLayout extends VLayout {
    //TODO make configureable?
    private int radius = 10000;

    /**
     * Create a new Classic redial layout 
     * @param vtree the vtree to layout
     * @param state 
     */
    public VClassicRadialLayout(VTree vtree, ScaffoldTreeViewState state) {
        super(vtree, state);
    }

    @Override
    public void drawLayout() {
        drawLayout(this.vtree);
    }

    /**
     * layout a vtree
     * @param tree the vtree to layout
     */
    private void drawLayout(VTree tree) {
        tree.countSubTreeLeaves(tree.getRoot());
        drawSubTree(tree.getRoot(), 0, 0, Math.PI * 2);
    };

    /**
     * 
     * @param v
     * @param pRadius
     * @param angle1
     * @param angle2
     */
    public void drawSubTree(VNode v, int pRadius, double angle1, double angle2) {

        // 1. Koordianten berechnen
        double azimut = (angle1 + angle2);
        double x = Math.cos(azimut) * pRadius; // +
                                               // v.getFullBounds().getCenterX();
        double y = Math.sin(azimut) * pRadius; // +
                                               // v.getFullBounds().getCenterY();

        v.centerFullBoundsOnPoint(x, y);

        // 2. Parameter neu berechnen
        double s;
        double alpha;

        // 2.a) tao berechnen
        double tao = 2 * Math.acos(((double)pRadius / (pRadius + radius)));

        // 2.b) Parameter setzen
        if (tao < angle2 - angle1) {
            s = tao / v.getNumLeaves();
            alpha = (angle1 + angle2 - tao) / 2;
        } else {
            s = (angle2 - angle1) / v.getNumLeaves();
            alpha = angle1;
        }

        // 3. rekursiver Aufruf

        for (int i = 0; i < v.getChildCount(); i++) {
            VNode u = v.getTreeChildren().get(i);
            drawSubTree(u, pRadius + radius, alpha, alpha + (s * u.getNumLeaves()));
            alpha = alpha + (s * u.getNumLeaves());
        }

    }

}
