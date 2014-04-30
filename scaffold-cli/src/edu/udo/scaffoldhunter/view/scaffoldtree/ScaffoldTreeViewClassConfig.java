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

import edu.udo.scaffoldhunter.gui.util.ConfigProperty;
import edu.udo.scaffoldhunter.model.ViewClassConfig;

/**
 * @author Bernhard Dick
 * 
 */
public class ScaffoldTreeViewClassConfig extends ViewClassConfig {
    
    /**
     * <code>true</code> if a scaffold should be focused after action,
     * <code>false</code> otherwise.
     */
    @ConfigProperty
    private boolean focusAfterAction = true;
    /**
     * <code>true</code> if layouting should be animated, <code>false</code>
     * otherwise.
     */
    @ConfigProperty
    private boolean layoutAnimation = true;
    /**
     * <code>true</code> if panning should be animated, <code>false</code>
     * otherwise.
     */
    @ConfigProperty
    private boolean cameraAnimation = true;
    @ConfigProperty
    private boolean cursorAnimation = false;
    @ConfigProperty
    private int animationSpeed = 1000;
    @ConfigProperty
    private boolean cursorSiblingWraparound = false;
    @ConfigProperty
    private boolean cutTreeStem = true;
    @ConfigProperty
    private int initiallyOpenRings = 2;
    
    /**
     * @return the focusAfterAction
     */
    public boolean isFocusAfterAction() {
        return focusAfterAction;
    }
    /**
     * @param focusAfterAction the focusAfterAction to set
     */
    public void setFocusAfterAction(boolean focusAfterAction) {
        this.focusAfterAction = focusAfterAction;
    }
    /**
     * @return the layoutAnimation
     */
    public boolean isLayoutAnimation() {
        return layoutAnimation;
    }
    /**
     * @param layoutAnimation the layoutAnimation to set
     */
    public void setLayoutAnimation(boolean layoutAnimation) {
        this.layoutAnimation = layoutAnimation;
    }
    /**
     * @return the cameraAnimation
     */
    public boolean isCameraAnimation() {
        return cameraAnimation;
    }
    /**
     * @param cameraAnimation the cameraAnimation to set
     */
    public void setCameraAnimation(boolean cameraAnimation) {
        this.cameraAnimation = cameraAnimation;
    }
    /**
     * @return the cursorAnimation
     */
    public boolean isCursorAnimation() {
        return cursorAnimation;
    }
    /**
     * @param cursorAnimation the cursorAnimation to set
     */
    public void setCursorAnimation(boolean cursorAnimation) {
        this.cursorAnimation = cursorAnimation;
    }
    /**
     * @return the animationSpeed
     */
    public int getAnimationSpeed() {
        return animationSpeed;
    }
    /**
     * @param animationSpeed the animationSpeed to set
     */
    public void setAnimationSpeed(int animationSpeed) {
        this.animationSpeed = animationSpeed;
    }
    /**
     * @return the cursorSiblingWraparound
     */
    public boolean isCursorSiblingWraparound() {
        return cursorSiblingWraparound;
    }
    /**
     * @param cursorSiblingWraparound the cursorSiblingWraparound to set
     */
    public void setCursorSiblingWraparound(boolean cursorSiblingWraparound) {
        this.cursorSiblingWraparound = cursorSiblingWraparound;
    }
    /**
     * @return the cutTreeStem
     */
    public boolean isCutTreeStem() {
        return cutTreeStem;
    }
    /**
     * @param cutTreeStem the cutTreeStem to set
     */
    public void setCutTreeStem(boolean cutTreeStem) {
        this.cutTreeStem = cutTreeStem;
    }
    /**
     * @return the initiallyOpenRings
     */
    public int getInitiallyOpenRings() {
        return initiallyOpenRings;
    }
    /**
     * @param initiallyOpenRings the initiallyOpenRings to set
     */
    public void setInitiallyOpenRings(int initiallyOpenRings) {
        this.initiallyOpenRings = initiallyOpenRings;
    }
    
    
}
