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

package edu.udo.scaffoldhunter.view;

import edu.udo.scaffoldhunter.util.I18n;
import edu.umd.cs.piccolo.PCanvas;
import edu.umd.cs.piccolo.util.PPaintContext;

/**
 * @author Bernhard Dick
 *
 */

/**
 * Used to set the rendering quality for classes extending <code>PCanvas</code>.
 */
public enum RenderingQuality {
    /**
     * High Rendering Quality. Anti Aliasing is always enabled.
     */
    HIGH(PPaintContext.HIGH_QUALITY_RENDERING, PPaintContext.HIGH_QUALITY_RENDERING,
            PPaintContext.HIGH_QUALITY_RENDERING), 
    /**
     * Low Rendering Quality. Anti Aliasing is always disabled.
     */
    LOW(PPaintContext.LOW_QUALITY_RENDERING, PPaintContext.LOW_QUALITY_RENDERING,
            PPaintContext.LOW_QUALITY_RENDERING), 
    /**
     * Automatic switch between low quality rendering during animations and high quality
     *  rendering for static content.
     */
    AUTO(PPaintContext.HIGH_QUALITY_RENDERING, PPaintContext.LOW_QUALITY_RENDERING,
            PPaintContext.LOW_QUALITY_RENDERING);
    
    private int defaultQuality;
    private int interactionQuality;
    private int animationQuality;

    private RenderingQuality(int defaultQuality, int interactionQuality, int animationQuality) {
        this.defaultQuality = defaultQuality;
        this.interactionQuality = interactionQuality;
        this.animationQuality = animationQuality;
    }

    /**
     * Sets rendering quality values for the specified <code>PCanvas</code> to
     * the ones defined for this quality level.
     * 
     * @param canvas
     */
    public void setQuality(PCanvas canvas) {
        canvas.setDefaultRenderQuality(defaultQuality);
        canvas.setInteractingRenderQuality(interactionQuality);
        canvas.setAnimatingRenderQuality(animationQuality);
    }
    
    @Override
    public String toString() {
        return I18n.get("View.RenderingQuality.ToString." + this.name());
    }
}