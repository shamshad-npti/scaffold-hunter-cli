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

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferInt;
import java.awt.image.DirectColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.util.Arrays;

/**
 * this class renderes the dots (and only the dots) to an offscreen image, using
 * a z-buffer
 *
 * @author Michael Hesse
 */
public class DotRenderer3D {


        protected int[] zBuffer;
        protected int[] cBuffer;    // stores the color for each pixel
        protected int bufferWidth, bufferHeight;
        protected int scanoffset, scanline;
        protected static final int horizontalAddition = 20;     // added to the left and to the right, to avoid testing for boundaries
        protected static final int verticalAddition = 20;     // added to the top and to the bottom, to avoid testing for boundaries

        protected double _zFactor, _zOffset;
        protected int _z;

        protected int _position;

        ColorModel colorModel;
        SampleModel sampleModel;


        /**
         * constructs a fresh DotRenderer3D
         */
        public DotRenderer3D() {
            bufferWidth = 0;
            bufferHeight = 0;
            scanoffset = 0;
            scanline = 0;
            zBuffer = new int[1];
            cBuffer = new int[1];
            colorModel = DirectColorModel.getRGBdefault();
//            colorModel = new DirectColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
//                    32, 0x00ff0000, 0x0000ff00, 0x000000ff, 0xff000000, false,
//                    Transparency.TRANSLUCENT);
        }

        /**
         * prepares the renderer for upcoming paintings. if the size of the painting
         * area differes from previous calls then new memory will be allocated
         *
         * @param width
         *    the width of the painting area
         * @param height
         *    the height of the paiting area
         * @param depthMin 
         * @param depthMax 
         */
        public void prepare( int width, int height, double depthMin, double depthMax ) {
            int newArraySize = (width+horizontalAddition*2) * (height+verticalAddition*2);
            if(newArraySize > zBuffer.length) {
                // allocate new memory
                zBuffer = new int[newArraySize];
                cBuffer = new int[newArraySize];
            }
            // create new sampleModel, if neccessary
            if( (width != bufferWidth) | (height != bufferHeight) )
                sampleModel = colorModel.createCompatibleSampleModel(width+2*horizontalAddition, height);
            // clear buffers
            Arrays.fill( zBuffer, 0, newArraySize, 0x7fffffff );
            Arrays.fill( cBuffer, 0, newArraySize, 0x00000000 );
            // set parameters
            bufferWidth = width;
            bufferHeight = height;
            scanline = width + horizontalAddition*2;
            scanoffset = verticalAddition * scanline + verticalAddition;
            // calculate zFactor + zOffset
            _zFactor = 0x7fff / (depthMax-depthMin);
            _zOffset = depthMin * _zFactor;
        }

    /*
        public void copyImage(Graphics g) {
            DataBuffer buffer = new DataBufferInt(cBuffer, scanline*bufferHeight, scanoffset);
            WritableRaster raster = Raster.createWritableRaster(sampleModel, buffer, null);
            BufferedImage image = new BufferedImage( colorModel, raster, false, null);
            g.drawImage(image, 0, 0, null);
        }
    */

        /**
         * @return
         *  returns the rendered image that can be displayed
         */
        public BufferedImage getContentImage() {
            DataBuffer buffer = new DataBufferInt(cBuffer, scanline*bufferHeight, scanoffset);
            WritableRaster raster = Raster.createWritableRaster(sampleModel, buffer, null);
            return new BufferedImage( colorModel, raster, false, null);
        }



        /**
         * @param x
         *  x-coordinate 
         * @param y
         *  y-coordinate 
         * @return
         *  the number of the dot that was painted at pixel-position x/y.
         *  needed for picking.
         */
        public int getObjectNumber(int x, int y) {
            int on = -1;
            int pos = (y+verticalAddition)*scanline + x + horizontalAddition;
            if(zBuffer != null) {
                if( (pos >= 0) & (pos < zBuffer.length) ) {
                    int _on = zBuffer[ pos ];
                    _on &= 0x1ffff;
                    if(_on != 0x1ffff)
                        on = _on;
                }
            }
            return on;
        }


    /*
        public void plotLine( Graphics g, double x0, double y0, double z0, double x1, double y1, double z1) {
            int dx =  (int)Math.abs(x1-x0);
            int dy = -(int)Math.abs(y1-y0);
            int sx = ( x0<x1 ? 1 : -1 );
            int sy = ( y0<y1 ? 1 : -1 );
            int err = dx+dy
            int e2;

            for(;;){
                //setPixel(x0,y0);
                if ( (x0==x1) && (y0==y1) )
                    break;
                e2 = (err << 1);
                if (e2 >= dy) {
                    err += dy;
                    x0 += sx;
                }
                if (e2 <= dx) {
                    err += dx;
                    y0 += sy;
                }
            }


        }
    */

        /**
         * plots a dot
         * 
         * @param x
         * @param y
         * @param z
         * @param color
         * @param objectNumber
         * @param dotsize
         */
        public void plotDot ( int x, int y, double z, int color, int objectNumber, int dotsize) {
            if ( (x<0) | (x>bufferWidth) | (y<0) | (y>bufferHeight) ) return;
            _z = ( (((int)(z*_zFactor-_zOffset)) ^0x4000) << 17 ) | objectNumber;
            switch(dotsize) {
                case 20: plotDot20(x, y, color, objectNumber); break;
                case 19: plotDot19(x, y, color, objectNumber); break;
                case 18: plotDot18(x, y, color, objectNumber); break;
                case 17: plotDot17(x, y, color, objectNumber); break;
                case 16: plotDot16(x, y, color, objectNumber); break;
                case 15: plotDot15(x, y, color, objectNumber); break;
                case 14: plotDot14(x, y, color, objectNumber); break;
                case 13: plotDot13(x, y, color, objectNumber); break;
                case 12: plotDot12(x, y, color, objectNumber); break;
                case 11: plotDot11(x, y, color, objectNumber); break;
                case 10: plotDot10(x, y, color, objectNumber); break;
                case  9: plotDot9(x, y, color, objectNumber); break;
                case  8: plotDot8(x, y, color, objectNumber); break;
                case  7: plotDot7(x, y, color, objectNumber); break;
                case  6: plotDot6(x, y, color, objectNumber); break;
                case  5: plotDot5(x, y, color, objectNumber); break;
                case  4: plotDot4(x, y, color, objectNumber); break;
                case  3: plotDot3(x, y, color, objectNumber); break;
                case  2: plotDot2(x, y, color, objectNumber); break;
                case  1: plotDot1(x, y, color, objectNumber); break;
                default: plotDot20(x, y, color, objectNumber);
            }
        }
        
        /**
         * plots a dot
         * 
         * @param x
         * @param y
         * @param color
         * @param objectNumber
         * @param dotsize
         */
        public void plotTransparentDot ( int x, int y, int color, int objectNumber, int dotsize) {
            if ( (x<0) | (x>bufferWidth) | (y<0) | (y>bufferHeight) ) return;
            _z = 0x80000000 | objectNumber;
            switch(dotsize) {
                case 20: plotDot20(x, y, color, objectNumber); break;
                case 19: plotDot19(x, y, color, objectNumber); break;
                case 18: plotDot18(x, y, color, objectNumber); break;
                case 17: plotDot17(x, y, color, objectNumber); break;
                case 16: plotDot16(x, y, color, objectNumber); break;
                case 15: plotDot15(x, y, color, objectNumber); break;
                case 14: plotDot14(x, y, color, objectNumber); break;
                case 13: plotDot13(x, y, color, objectNumber); break;
                case 12: plotDot12(x, y, color, objectNumber); break;
                case 11: plotDot11(x, y, color, objectNumber); break;
                case 10: plotDot10(x, y, color, objectNumber); break;
                case  9: plotDot9(x, y, color, objectNumber); break;
                case  8: plotDot8(x, y, color, objectNumber); break;
                case  7: plotDot7(x, y, color, objectNumber); break;
                case  6: plotDot6(x, y, color, objectNumber); break;
                case  5: plotDot5(x, y, color, objectNumber); break;
                case  4: plotDot4(x, y, color, objectNumber); break;
                case  3: plotDot3(x, y, color, objectNumber); break;
                case  2: plotDot2(x, y, color, objectNumber); break;
                case  1: plotDot1(x, y, color, objectNumber); break;
                default: plotDot20(x, y, color, objectNumber);
            }
        }


        /*********************************************************
         * here comes some java code generated by a perl script
         */

        /**
         * plots a single dot
         * @param x 
         * @param y 
         * @param color 
         * @param objectNumber 
         * 
         */
        public void plotDot1 ( int x, int y, int color, int objectNumber) {
            _position = scanoffset + (y-(1>>>1))*scanline + x - (1>>>1);
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
        }


        /**
         * plots a single dot
         * @param x 
         * @param y 
         * @param color 
         * @param objectNumber 
         * 
         */
        public void plotDot2 ( int x, int y, int color, int objectNumber) {
            _position = scanoffset + (y-(2>>>1))*scanline + x - (2>>>1);
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 1;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
        }


        /**
         * plots a single dot
         * @param x 
         * @param y 
         * @param color 
         * @param objectNumber 
         * 
         */
        public void plotDot3 ( int x, int y, int color, int objectNumber) {
            _position = scanoffset + (y-(3>>>1))*scanline + x - (3>>>1);
            _position += 2;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 1;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 1;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
        }


        /**
         * plots a single dot
         * @param x 
         * @param y 
         * @param color 
         * @param objectNumber 
         * 
         */
        public void plotDot4 ( int x, int y, int color, int objectNumber) {
            _position = scanoffset + (y-(4>>>1))*scanline + x - (4>>>1);
            _position += 2;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 2;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 3;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 2;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
        }


        /**
         * plots a single dot
         * @param x 
         * @param y 
         * @param color 
         * @param objectNumber 
         * 
         */
        public void plotDot5 ( int x, int y, int color, int objectNumber) {
            _position = scanoffset + (y-(5>>>1))*scanline + x - (5>>>1);
            _position += 2;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 3;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 4;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 4;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 3;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
        }


        /**
         * plots a single dot
         * @param x 
         * @param y 
         * @param color 
         * @param objectNumber 
         * 
         */
        public void plotDot6 ( int x, int y, int color, int objectNumber) {
            plotDot4( x, y, color, objectNumber );
            _position = scanoffset + (y-(6>>>1))*scanline + x - (6>>>1);
            _position += 3;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 2;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 3;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 4;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 4;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 3;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 2;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
        }


        /**
         * plots a single dot
         * @param x 
         * @param y 
         * @param color 
         * @param objectNumber 
         * 
         */
        public void plotDot7 ( int x, int y, int color, int objectNumber) {
            plotDot5( x, y, color, objectNumber );
            _position = scanoffset + (y-(7>>>1))*scanline + x - (7>>>1);
            _position += 3;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 3;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 4;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 4;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 3;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
        }


        /**
         * plots a single dot
         * @param x 
         * @param y 
         * @param color 
         * @param objectNumber 
         * 
         */
        public void plotDot8 ( int x, int y, int color, int objectNumber) {
            plotDot6( x, y, color, objectNumber );
            _position = scanoffset + (y-(8>>>1))*scanline + x - (8>>>1);
            _position += 4;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 3;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 3;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 7;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 7;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 7;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 3;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 3;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
        }


        /**
         * plots a single dot
         * @param x 
         * @param y 
         * @param color 
         * @param objectNumber 
         * 
         */
        public void plotDot9 ( int x, int y, int color, int objectNumber) {
            plotDot7( x, y, color, objectNumber );
            _position = scanoffset + (y-(9>>>1))*scanline + x - (9>>>1);
            _position += 4;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 4;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 4;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 7;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 7;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 4;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 4;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
        }


        /**
         * plots a single dot
         * @param x 
         * @param y 
         * @param color 
         * @param objectNumber 
         * 
         */
        public void plotDot10 ( int x, int y, int color, int objectNumber) {
            plotDot8( x, y, color, objectNumber );
            _position = scanoffset + (y-(10>>>1))*scanline + x - (10>>>1);
            _position += 4;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 3;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 7;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 7;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 7;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 7;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 7;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 7;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 3;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
        }


        /**
         * plots a single dot
         * @param x 
         * @param y 
         * @param color 
         * @param objectNumber 
         * 
         */
        public void plotDot11 ( int x, int y, int color, int objectNumber) {
            plotDot9( x, y, color, objectNumber );
            _position = scanoffset + (y-(11>>>1))*scanline + x - (11>>>1);
            _position += 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 4;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 4;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 7;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 7;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 4;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 4;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
        }


        /**
         * plots a single dot
         * @param x 
         * @param y 
         * @param color 
         * @param objectNumber 
         * 
         */
        public void plotDot12 ( int x, int y, int color, int objectNumber) {
            plotDot10( x, y, color, objectNumber );
            _position = scanoffset + (y-(12>>>1))*scanline + x - (12>>>1);
            _position += 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
        }


        /**
         * plots a single dot
         * @param x 
         * @param y 
         * @param color 
         * @param objectNumber 
         * 
         */
        public void plotDot13 ( int x, int y, int color, int objectNumber) {
            plotDot11( x, y, color, objectNumber );
            _position = scanoffset + (y-(13>>>1))*scanline + x - (13>>>1);
            _position += 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 4;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 4;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
        }


        /**
         * plots a single dot
         * @param x 
         * @param y 
         * @param color 
         * @param objectNumber 
         * 
         */
        public void plotDot14 ( int x, int y, int color, int objectNumber) {
            plotDot12( x, y, color, objectNumber );
            _position = scanoffset + (y-(14>>>1))*scanline + x - (14>>>1);
            _position += 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
        }


        /**
         * plots a single dot
         * @param x 
         * @param y 
         * @param color 
         * @param objectNumber 
         * 
         */
        public void plotDot15 ( int x, int y, int color, int objectNumber) {
            plotDot13( x, y, color, objectNumber );
            _position = scanoffset + (y-(15>>>1))*scanline + x - (15>>>1);
            _position += 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
        }


        /**
         * plots a single dot
         * @param x 
         * @param y 
         * @param color 
         * @param objectNumber 
         * 
         */
        public void plotDot16 ( int x, int y, int color, int objectNumber) {
            plotDot14( x, y, color, objectNumber );
            _position = scanoffset + (y-(16>>>1))*scanline + x - (16>>>1);
            _position += 7;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 15;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 15;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 15;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 15;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 15;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 15;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 15;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
        }


        /**
         * plots a single dot
         * @param x 
         * @param y 
         * @param color 
         * @param objectNumber 
         * 
         */
        public void plotDot17 ( int x, int y, int color, int objectNumber) {
            plotDot15( x, y, color, objectNumber );
            _position = scanoffset + (y-(17>>>1))*scanline + x - (17>>>1);
            _position += 7;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 15;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 16;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 16;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 16;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 16;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 16;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 16;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 16;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 16;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 16;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 15;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
        }


        /**
         * plots a single dot
         * @param x 
         * @param y 
         * @param color 
         * @param objectNumber 
         * 
         */
        public void plotDot18 ( int x, int y, int color, int objectNumber) {
            plotDot16( x, y, color, objectNumber );
            _position = scanoffset + (y-(18>>>1))*scanline + x - (18>>>1);
            _position += 7;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 7;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 15;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 15;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 16;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 15;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 17;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 17;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 17;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 17;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 17;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 17;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 17;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 17;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 17;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 15;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 16;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 15;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 15;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 9;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 5;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 7;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
        }


        /**
         * plots a single dot
         * @param x 
         * @param y 
         * @param color 
         * @param objectNumber 
         * 
         */
        public void plotDot19 ( int x, int y, int color, int objectNumber) {
            plotDot17( x, y, color, objectNumber );
            _position = scanoffset + (y-(19>>>1))*scanline + x - (19>>>1);
            _position += 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 15;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 16;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 16;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 16;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 17;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 18;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 18;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 18;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 18;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 18;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 18;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 18;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 18;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 18;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 17;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 16;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 16;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 16;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 15;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 12;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 10;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 6;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
        }


        /**
         * plots a single dot
         * @param x 
         * @param y 
         * @param color 
         * @param objectNumber 
         * 
         */
        public void plotDot20 ( int x, int y, int color, int objectNumber) {
            plotDot18( x, y, color, objectNumber );
            _position = scanoffset + (y-(20>>>1))*scanline + x - (20>>>1);
            _position += 8;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 7;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 7;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 15;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 15;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 16;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 17;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 17;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 17;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 18;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 19;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 19;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 19;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 19;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 19;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 19;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 19;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 19;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 19;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 19;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 19;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 18;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 17;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 17;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 17;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 16;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 15;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 15;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 13;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 14;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 11;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += 7;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position += scanline - 7;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
            _position++;    if( zBuffer[_position] > _z) {        zBuffer[_position] = _z;        cBuffer[_position] = addColor(cBuffer[_position],color);    }
        }

        private int addColor(int oldColor, int newColor) {
            if((newColor & 0xff000000) == 0xff000000)
                return newColor;
            else {
                int weight = (newColor >> 24) & 0x000000ff;
                int oldRed = (oldColor & 0x00ff0000) >> 16;
                int oldGreen = (oldColor & 0x0000ff00) >> 8;
                int oldBlue = (oldColor & 0x000000ff);
                int newRed = (newColor & 0x00ff0000) >> 16;
                int newGreen = (newColor & 0x0000ff00) >> 8;
                int newBlue = (newColor & 0x000000ff);
                newRed = ((255 - weight) * newRed + weight * oldRed) / 255;
                newGreen = ((255 - weight) * newGreen + weight * oldGreen) / 255;
                newBlue = ((255 - weight) * newBlue + weight * oldBlue) / 255;
                return 0xff000000 + (newRed<<16) + (newGreen<<8) + (newBlue);
            }
        }

}
