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

package edu.udo.scaffoldhunter.util;

/**
 * A simple 2d Position class
 * 
 * @author Lappie
 */
public class Position implements Cloneable {

    private double x;
    private double y;

    /**
     * Sets the default values
     * 
     * @param x
     * @param y
     */
    public Position(double x, double y) {
        this.setX(x);
        this.setY(y);
    }

    /**
     * @return the x
     */
    public double getX() {
        return x;
    }

    /**
     * @param x
     *            the x to set
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Adds the given value to X
     * 
     * @param add
     */
    public void addX(double add) {
        this.x += add;
    }

    /**
     * Subtract the given value from x
     * 
     * @param subb
     */
    public void subtractX(double subb) {
        this.x -= subb;
    }

    /**
     * @return the y
     */
    public double getY() {
        return y;
    }

    /**
     * @param y
     *            the y to set
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Add the given value to y
     * 
     * @param add
     */
    public void addY(double add) {
        this.y += add;
    }

    /**
     * Subtract the given value from y
     * 
     * @param subb
     */
    public void subtractY(double subb) {
        this.y -= subb;
    }

    @Override
    public Object clone() {
        return new Position(this.x, this.y);
    }
}
