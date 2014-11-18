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
 */

package cubicstericoverlapdetector;

import java.math.BigDecimal;
import java.util.ArrayList;

/**This class takes a min and max Location, and the size of a unit inside
 * the Space. It then divides the spanned space into containers of the
 * unit size. It has methods for checking whether a given Location is
 * inside the spanned Space, for returning the container ordinal of a
 * Location, and for returning all nearby container ordinals of a given
 * Location.
 *
 * @author Johan Sjöblom
 *
 */
public class Space {
    private Location min, max;
    private int[] numberofcontainers;
    private BigDecimal unitsize;

    public Space(double unitsize, Location min, Location max) {
        if(min.getDimension() != max.getDimension()) {
            throw new RuntimeException("Dimensions don't agree");
        }

        this.unitsize = new BigDecimal(unitsize);
        this.min = min;
        this.max = max;

        double[] mincoords = min.getDoubleCoordinates();
        double[] maxcoords = max.getDoubleCoordinates();

        // Count how many containers the space spans, in each direction.
        numberofcontainers = new int[maxcoords.length];
        for(int i = 0; i < maxcoords.length; i++) {
            numberofcontainers[i] = (int) Math.ceil(
                    (maxcoords[i] - mincoords[i]) / unitsize);
        }
    }


    /**Returns whether the given Location is within this Space. If false
     * is returned, then the Location is outside one of the dimensions
     * of the Space.
     *
     * @param l Location to check.
     * @return True if the given Location is within the Space,
     * false otherwise.
     */
    private boolean isInsideSpace(Location l) {
        if(l.getDimension() != max.getDimension())
            throw new RuntimeException("Dimensions don't agree");

        for(int i = 0; i < l.getDimension(); i++) {
            // If at least one of the objects' coordinates is
            // either smaller than the min element of the space
            // (which basically acts as the Origin), or is larger
            // than the last container of the dimension, then
            // return false
            BigDecimal t = BigDecimal.ONE;
            t = (t.add(max.getCoordinate(i)).subtract(min.getCoordinate(i)))
                    .multiply(unitsize);
            if(l.compareTo(min, i)<0 || l.getCoordinate(i).compareTo(t)>0)
                return false;
        }
        return true;
    }

    /**Given a coordinate and a dimension (x, y, z, ...), this method will
     * return which container for that dimension the coordinate lies in.
     *
     * @param coord Coordinate to find container for
     * @param dim Which dimension the coordinate is in
     * @return The container ordinal in the given dimension that
     *         the coordinate is in.
     */
    private int getContainerForDimension(BigDecimal coord, int dim) {
        coord = coord.subtract(min.getCoordinates()[dim]);
        double div = coord.divide(unitsize).doubleValue();
        return (int)(coord.compareTo(BigDecimal.ZERO) > 0 ?
                Math.ceil (div) :
                Math.floor(div));
    }

    /**Will return the container ordinal for the given Location.
     *
     * @param l Location to find the container ordinal for.
     * @return The container ordinal for the given Location.
     * If the Location is outside of the space, -1 is returned.
     */
    public int getContainer(Location l) {
        if(!isInsideSpace(l))
            return -1;

        // For 2 dimensions, the container can be calculated as follows:
        // container(x, y) = y*maxX + x;
        // For 3 dimensions, the container can be calculated as follows:
        // container(x, y, z) = z*maxY*maxX + y*maxX + x;
        int container = 0;
        BigDecimal[] coord = l.getCoordinates();
        for(int j = coord.length - 1; j >= 0; j--) {
            // getContainerForDimension returns which container
            // in the provided dimension the coordinate is in.
            int tmp = getContainerForDimension(coord[j], j);
            for(int k = j - 1; k >= 0; k--)
                tmp = tmp * numberofcontainers[k];
            container += tmp;
        }
        return container;
    }

    /**Given a Location l, this function will return a list of all
     * container ordinals near it (including l's own container ordinal).
     * The ordinals of the adjacent containers of the one that l is in
     * will be put in the list.<br />
     * For example, if there are two dimensions, the container of l is
     * added, as well as the next and previous containers for each
     * dimension. This will mean that 3 (l's container, the next one and
     * the previous one) containers will be added for each of the two
     * dimensions, i.e. 3*3 = 9 containers. In the illustration below,
     * the container of the Location is marked with 'l', and the adjacent
     * ones are marked with 'x'. The x's and l will have their container
     * ordinals added to the list that will be returned.<br /><pre>
     * ..........
     * ....xxx...
     * ....xlx...
     * ....xxx...
     * ..........</pre><br />
     *
     * For three dimensions, there are 3*3*3 = 27 containers. If some or
     * all of the containers are outside of the space, the list has as
     * many fewer elements as the number of containers falling outside.
     *
     * @param l Location to get the nearby container ordinals for.
     * @return ArrayList of Integer container ordinals near the Location.
     */
    public ArrayList<Integer> getNearbyContainers(Location l) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        Location offset = new Location(l.getDimension());
        recCalcContainers(list, l, offset, 0);
        return list;
    }


    /**This function will recursively calculate the result for the
     * getNearbyContainers method.<br /><br />
     *
     * This function will start from the Location orig. For each of the
     * dimensions in orig, we wish to get the adjacent container. This is
     * accomplished by offsetting orig with unitsize distances in each
     * dimension. For example, in three dimensions, to get the container
     * above, to the right and on the same depth as orig, we would add
     * unitdistance * {1, 1, 0} (x, y, z coordinates). To get the
     * container below, to the left and inside as orig, add unitdistance *
     * {-1, -1, -1}. To get orig itself, unitdistance * {0, 0, 0} can be
     * used.<br />
     * This function works in two steps; first of all offset is populated
     * by recursively setting each dimension in it to -1, 0 and 1, and
     * then calling the same method again, but for the next dimension.
     * When offset has been populated with values of -1, 0 and 1, these
     * values are multiplied with unitdistance and added to orig. If
     * this container is inside the space, its ordinal is added to the
     * ArrayList list. When all iterations of the method are finished,
     * the list will be populated with the nearby container ordinals.
     *
     * @param list ArrayList of the container ordinals. Will be
     * recursively built up. Should be created (i.e. not null) before
     * calling the function.
     * @param orig Location to get the containers nearby of.
     * @param offset Location that will recursively have its coordinates
     * set to -1, 0 and 1.
     * @param dim Dimension to set the coordinate for. Will be updated
     * in each recursive call.
     */
    private void recCalcContainers(ArrayList<Integer> list,
                                   Location orig,
                                   Location offset,
                                   int dim) {

        // Keep recursively call this method, until all
        // dimensions are set.
        if(dim < orig.getDimension()) {
            for(int i = -1; i < 2; i++) {  // i = {-1, 0, 1}
                Location off = offset.clone();
                off.setCoordinate(dim, new BigDecimal(i));
                recCalcContainers(list, orig, off, dim + 1);
            }
        }
        else {
            // All dimensions have been set to -1, 0 or 1.
            Location l = orig.clone();
            offset.multiply(unitsize);
            l.add(offset);

            // If the location we have calculated is inside
            // the space, then add its ordinal to the list.
            if(isInsideSpace(l))
                list.add(getContainer(l));
        }
    }
}
