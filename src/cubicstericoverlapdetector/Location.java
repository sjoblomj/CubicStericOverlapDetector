/** Cubic Steric Overlap Detector, for detecting clashes between proteins.
 *  Copyright (C) 2014  Johan Sjöblom
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 */

package cubicstericoverlapdetector;

import java.math.BigDecimal;
import java.util.Arrays;

/**The Location class stores coordinates. Any number of dimensions
 * can be handled. Internally, the coordinates are stored in
 * BigDecimals, which are less prone to rounding errors than doubles.
 *
 * @author Johan Sjöblom
 *
 */
public class Location {
    private BigDecimal[] a;
    private int dimension;
    private final int DECIMALPLACES = 3;
    // The constant DECIMALPLACES denotes how many decimal places
    // of the input coordinates that will be kept.

    public Location(int d) {
        dimension = d;
        a = new BigDecimal[d];
    }
    public Location(Double[] v) {
        dimension = v.length;
        this.a = new BigDecimal[dimension];

        int dp = (int)Math.pow(10, DECIMALPLACES);
        for(int i = 0; i < dimension; i++) {
            // Only keep 'dp' decimal places
            this.a[i] = new BigDecimal(
                    String.valueOf(Math.floor(v[i] * dp) / dp));
        }
    }
    public Location(String[] v) {
        dimension = v.length;
        this.a = new BigDecimal[dimension];

        for(int i = 0; i < dimension; i++)
            this.a[i] = new BigDecimal(v[i]);
    }

    public Location clone() {
        Location out = new Location(dimension);
        for(int i = 0; i < dimension; i++)
            out.setCoordinate(i, a[i]);
        return out;
    }

    public void setCoordinate(int i, BigDecimal c) { a[i] = c; }
    public BigDecimal getCoordinate(int i) { return a[i]; }
    public BigDecimal[] getCoordinates()   { return a; }
    public int getDimension()              { return dimension; }

    /**Return an array of the coordinates in double format.
     *
     * @return Array of the coordinates, converted to doubles
     */
    public double[] getDoubleCoordinates() {
        double[] out = new double[dimension];
        for(int i = 0; i < dimension; i++)
            out[i] = a[i].doubleValue();
        return out;
    }
    /**Return coordinate i, converted to double format.
     *
     * @param i Coordinate to return.
     * @return Coordinate i in double format.
     */
    public double getDoubleCoordinate(int i) { return a[i].doubleValue(); }

    /**Multiplies every coordinate with d.
     *
     * @param d Value to multiply each coordinate with.
     */
    public void multiply(BigDecimal d) {
        for(int i = 0; i < dimension; i++)
            a[i] = a[i].multiply(d);
    }
    /**Adds two Locations together, i.e. for each of their coordinates,
     * add their values together.
     *
     * @param other Value to add to each coordinate.
     */
    public void add(Location other) {
        if(dimension != other.getDimension())
            throw new RuntimeException("Dimensions don't agree");

        for(int i = 0; i < dimension; i++)
            a[i] = a[i].add(other.getCoordinate(i));
    }


    /**Given an other Location, this will calculate the
     * Euclidean distance to it.
     *
     * @param other Location to calculate the distance to.
     * @return The distance to the other Location.
     */
    public double getDistance(Location other) {
        if(dimension != other.getDimension())
            throw new RuntimeException("Dimensions don't agree");

        BigDecimal[] otherdata = other.getCoordinates();
        BigDecimal result = new BigDecimal("0.0");
        for(int i = 0; i < dimension; i++)
            result = result.add(otherdata[i].subtract(a[i]).pow(2));
        return Math.sqrt(result.doubleValue());
    }

    public int compareTo(Location other, int coord) {
        if(dimension != other.getDimension())
            throw new RuntimeException("Dimensions don't agree");
        return a[coord].compareTo(other.getCoordinate(coord));
    }

    @Override
    public String toString() {
        String out = "(";
        for(int i = 0; i < dimension; i++) {
            out += a[i];
            if(i < dimension - 1)
                out += ", ";
        }
        out += ")";
        return out;
    }


    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + dimension;
        result = prime * result + Arrays.hashCode(a);
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Location))
            return false;
        Location other = (Location) obj;
        if (dimension != other.dimension)
            return false;
        if (!Arrays.equals(a, other.a))
            return false;
        return true;
    }
}
