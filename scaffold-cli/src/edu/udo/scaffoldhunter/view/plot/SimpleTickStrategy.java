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

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * @author Michael Hesse
 * 
 * this class uses BigInteger to get rid of rounding errors
 * (a number formatter might have the same effect..)
 * 
 */
public class SimpleTickStrategy implements TickStrategy {

    @Override
    public double[] getTicks(double min, double max) {
        double[] tickPosition;
        double delta;
        double minTick, maxTick, deltaTick;

        // do some checks
        if( min > max ) {
            double t = min; min = max; max = t;
        }
        if( min == max ) {
            tickPosition = new double[1];
            tickPosition[0] = min;
            return tickPosition;
        }

        delta = max-min;

        { // calculate deltaTick
            int p = (int) Math.log10(delta);
            deltaTick = Math.pow( 10, p);
            while( (delta / deltaTick) < 2 )
                deltaTick /= 10;
        }

        { // calculate suggested minimum
            int d = (int)( min / deltaTick );
            BigDecimal _min = BigDecimal.valueOf(d);
            BigDecimal _deltaTick = BigDecimal.valueOf(deltaTick);
            _min = _min.multiply( _deltaTick );
            while ( _min.doubleValue() > min )
                _min = _min.subtract(_deltaTick);
            while ( _min.doubleValue() < min )
                _min = _min.add(_deltaTick);
            minTick = _min.doubleValue();
        }

        { // calculate suggested maximum
            int d = (int)( max / deltaTick );
            BigDecimal _max = BigDecimal.valueOf(d);
            BigDecimal _deltaTick = BigDecimal.valueOf(deltaTick);
            _max = _max.multiply(_deltaTick);
            while ( _max.doubleValue() < max )
                _max = _max.add(_deltaTick);
            while ( _max.doubleValue() > max )
                _max = _max.subtract(_deltaTick);
            maxTick = _max.doubleValue();
        }

        { // generate tickmarks
            BigDecimal _tick = BigDecimal.valueOf(minTick);
            BigDecimal _deltaTick = BigDecimal.valueOf(deltaTick);
            ArrayList <Double> _tickPosition = new ArrayList <Double> ();
            do {
                _tickPosition.add( _tick.doubleValue() );
                _tick = _tick.add( _deltaTick );
            } while ( _tick.doubleValue() <= maxTick );
            tickPosition = new double[ _tickPosition.size()];
            for(int i=0; i<_tickPosition.size(); i++)
                tickPosition[i] = _tickPosition.get(i);
        }

        return tickPosition;
    }


    @Override
    public double[] suggestedInterval(double min, double max) {
        double[] suggestedMinMax = { 0, 10 };
        double delta = max-min;
        double deltaTick;

        if(delta == 0) {
            min -= 0.05;
            max += 0.05;
            delta = 0.1;
        }

        { // calculate deltaTick
            int p = (int) Math.log10(delta);
            deltaTick = Math.pow( 10, p);
            while( (delta / deltaTick) < 2 )
                deltaTick /= 10;
        }

        { // calculate suggested minimum
            int d = (int)( min / deltaTick );
            BigDecimal _min = BigDecimal.valueOf(d);
            BigDecimal _deltaTick = BigDecimal.valueOf(deltaTick);
            _min = _min.multiply( _deltaTick );
            while ( _min.doubleValue() > min )
                _min = _min.subtract(_deltaTick);
            suggestedMinMax[0] = _min.doubleValue();
        }

        { // calculate suggested maximum
            int d = (int)( max / deltaTick );
            BigDecimal _max = BigDecimal.valueOf(d);
            BigDecimal _deltaTick = BigDecimal.valueOf(deltaTick);
            _max = _max.multiply(_deltaTick);
            while ( _max.doubleValue() < max )
                _max = _max.add(_deltaTick);
            suggestedMinMax[1] = _max.doubleValue();
        }

        return suggestedMinMax;
    }
}
