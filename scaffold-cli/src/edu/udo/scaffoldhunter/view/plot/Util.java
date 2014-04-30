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

package edu.udo.scaffoldhunter.view.plot;

/**
 * @author Michael Hesse
 *
 * - a vector is represented by 'double[3]'
 * - a matrix is 'double[3][3]'
 */
public class Util {


    /**
     * adds Vectors
     * 
     * result = v1+v2
     * 
     * @param v1
     *  a vector (double[3])
     * @param v2
     *  a vector (double[3])
     * @return
     *  result
     */
    public static double[] addVectors( double[] v1, double[] v2 ) {
        double[] result = new double[3];
        addVectors(v1, v2, result);
        return result;
    }

    /**
     * adds vectors
     * 
     * result = v1+v2
     * 
     * @param v1
     *  a vector (double[3])
     * @param v2
     *  a vector (double[3])
     * @param result
     *  a vector (double[3])
     */
    public static void addVectors( double[] v1, double[] v2, double[] result ) {
        result[0] = v1[0]+v2[0];
        result[1] = v1[1]+v2[1];
        result[2] = v1[2]+v2[2];
    }

    /**
     * subs Vectors
     * 
     * result = v1-v2
     * 
     * @param v1
     *  a vector (double[3])
     * @param v2
     *  a vector (double[3])
     * @return
     *  result
     */
    public static double[] subVectors( double[] v1, double[] v2 ) {
        double[] result = new double[3];
        subVectors(v1, v2, result);
        return result;
    }
    
    /**
     * adds vectors
     * 
     * result = v1-v2
     * 
     * @param v1
     *  a vector (double[3])
     * @param v2
     *  a vector (double[3])
     * @param result
     *  a vector (double[3])
     */
    public static void subVectors( double[] v1, double[] v2, double[] result ) {
        result[0] = v1[0] - v2[0];
        result[1] = v1[1] - v2[1];
        result[2] = v1[2] - v2[2];
    }

    /**
     * multiplies matrixes
     * 
     * result = m1*m2
     * 
     * @param m1
     *  a matrix (double[3][3])
     * @param m2
     *  a matrix (double[3][3])
     * @return
     *  a matrix (double[3][3])
     */
    public static double[][] multiplyMatrixes( double[][] m1, double[][] m2 ) {
        double[][] result = new double[3][3];
        multiplyMatrixes( m1, m2, result );
        return result;
    }

    /**
     * multiplies matrixes
     * 
     * result = m1*m2
     * 
     * @param m1
     *  a matrix (double[3][3])
     * @param m2
     *  a matrix (double[3][3])
     * @param result
     *  a matrix (double[3][3])
     */
    public static void multiplyMatrixes( double[][] m1, double[][] m2, double[][] result) {
        result[0][0] = m1[0][0]*m2[0][0] + m1[0][1]*m2[1][0] + m1[0][2]*m2[2][0];
        result[0][1] = m1[0][0]*m2[0][1] + m1[0][1]*m2[1][1] + m1[0][2]*m2[2][1];
        result[0][2] = m1[0][0]*m2[0][2] + m1[0][1]*m2[1][2] + m1[0][2]*m2[2][2];

        result[1][0] = m1[1][0]*m2[0][0] + m1[1][1]*m2[1][0] + m1[1][2]*m2[2][0];
        result[1][1] = m1[1][0]*m2[0][1] + m1[1][1]*m2[1][1] + m1[1][2]*m2[2][1];
        result[1][2] = m1[1][0]*m2[0][2] + m1[1][1]*m2[1][2] + m1[1][2]*m2[2][2];

        result[2][0] = m1[2][0]*m2[0][0] + m1[2][1]*m2[1][0] + +m1[2][2]*m2[2][0];
        result[2][1] = m1[2][0]*m2[0][1] + m1[2][1]*m2[1][1] + +m1[2][2]*m2[2][1];
        result[2][2] = m1[2][0]*m2[0][2] + m1[2][1]*m2[1][2] + +m1[2][2]*m2[2][2];
    }

    /**
     * multiply vector by matrix
     * @param v
     * @param m
     * @return
     *  result
     */
    public static double[] multiplyVectorByMatrix( double[] v, double[][] m ) {
        double[] result = new double[3];
        multiplyVectorByMatrix(v, m, result);
        return result;
    }
    /**
     * @param v
     * @param m
     * @param result
     */
    public static void multiplyVectorByMatrix( double[] v, double[][]m, double[] result ) {
        result[0] = v[0]*m[0][0] + v[1]*m[1][0] + v[2] * m[2][0];
        result[1] = v[0]*m[0][1] + v[1]*m[1][1] + v[2] * m[2][1];
        result[2] = v[0]*m[0][2] + v[1]*m[1][2] + v[2] * m[2][2];
    }

    // rotate vector around an axis
    /**
     * @param v
     *  vector that should become rotated
     * @param axis
     *  the rotation axis is another vector
     * @param degrees
     * @return
     *  the rotated vector v
     */
    public static double[] rotateVector( double[] v, double[]axis, double degrees ) {
        double[] result = new double[3];
        rotateVector( v, axis, degrees, result);
        return result;
    }
    /**
     * @param v
     * @param axis
     * @param degrees
     * @param result
     */
    public static void rotateVector( double[] v, double[] axis, double degrees, double[] result ) {
        double cos = Math.cos( Math.toRadians(degrees));
        double sin = Math.sin( Math.toRadians(degrees));

        result[0] = v[0] * (cos + axis[0]*axis[0]*(1-cos))
                + v[1] * (axis[0]*axis[1]*(1-cos) - axis[2]*sin)
                + v[2] * (axis[0]*axis[2]*(1-cos) + axis[1]*sin);
        result[1] = v[0] * (axis[1]*axis[0]*(1-cos) + axis[2]*sin)
                + v[1] * (cos + axis[1]*axis[1]*(1-cos))
                + v[2] * (axis[1]*axis[2]*(1-cos) - axis[0]*sin);
        result[2] = v[0] * (axis[2]*axis[0]*(1-cos) - axis[1]*sin)
                + v[1] * (axis[2]*axis[1]*(1-cos) + axis[0]*sin)
                + v[2] * (cos + axis[2]*axis[2]*(1-cos));
    }

    //
    /**
     * @param v
     * @return
     *  a string that shows the content of the vector
     */
    public static String vectorToString(double[] v) {
        return ("( "+v[0]+", "+v[1]+", "+v[2]+" )");
    }

    /**
     * calculates a linear mapping from a source interval to a destination interval.
     * the mapping consist of two doublevalues, 'factor' and 'offset'. the mapping
     * from to point s in the sourceinterval to a point d in the destinationinterval
     * can then be calculated by: d = s * factor + offset
     * the factor is the first value in the array, the offset is the second value
     * 
     * @param sourceMin 
     * @param sourceMax 
     * @param destMin 
     * @param destMax 
     * @return 
     *  the linear mapping in the form:
     *  double[0] = factor
     *  double[1] = offset
     */
    public static double[] getLinearMapping( double sourceMin, double sourceMax, double destMin, double destMax) {
        double[] linearMapping = new double[2];
        getLinearMapping( sourceMin, sourceMax, destMin, destMax, linearMapping );
        return linearMapping;
    }
    /**
     * @param sourceinterval
     * @param destMin
     * @param destMax
     * @return
     *  the linear mapping
     */
    public static double[] getLinearMapping( double[] sourceinterval, double destMin, double destMax) {
        double[] linearMapping = new double[2];
        getLinearMapping( sourceinterval[0], sourceinterval[1], destMin, destMax, linearMapping );
        return linearMapping;
    }
    /**
     * @param sourceMin
     * @param sourceMax
     * @param destMin
     * @param destMax
     * @param linearMapping
     */
    public static void getLinearMapping( double sourceMin, double sourceMax, double destMin, double destMax, double[] linearMapping ) {
        linearMapping[0] = (destMax - destMin) / (sourceMax - sourceMin);
        linearMapping[1] = destMin - sourceMin*linearMapping[0];
    }

    /**
     * performs the linear mapping. just for convenience.
     * 
     * @param value
     * @param linearMapping
     * @return
     *  the mapped value
     */
    public static double mapLinear( double value, double[] linearMapping ) {
        return value * linearMapping[0] + linearMapping[1];
    }
}

