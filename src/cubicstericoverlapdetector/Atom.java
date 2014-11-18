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

import java.util.ArrayList;

/**Class for holding information about Atoms. Each Atom may also,
 * optionally, hold an ArrayList of which Containers in the Space
 * that exist around it. These are the containers that are near the
 * container that this Atom falls in. By pre-calculating these
 * nearby containers, we can save time during the comparison phase.
 *
 * @author Johan Sjöblom
 *
 */
public class Atom {
    public final static double ATOMRADIUS = 2.0;

    private int      serial;
    private String   atomName;
    private String   altLoc;
    private String   resName;
    private String   chainID;
    private int      resSeq;
    private String   iCode;
    private Location centre;
    private ArrayList<Integer> relevantContainers = null;

    /**Constructor for an Atom. The coordinates of the Atom is the last
     * argument, and is in the form of a String array. The data is in
     * the same order as the *.pdb files.
     */
    public Atom(int serial,
                String atomName,
                String altLoc,
                String resName,
                String chainId,
                int resSeq,
                String iCode,
                String[] coords) {
        this.serial   = serial;
        this.atomName = atomName;
        this.altLoc   = altLoc;
        this.resName  = resName;
        this.chainID  = chainId;
        this.resSeq   = resSeq;
        this.iCode    = iCode;
        this.centre   = new Location(coords);
    }

    public int      getSerial()   { return serial;   }
    public String   getAtomName() { return atomName; }
    public String   getAltLoc()   { return altLoc;   }
    public String   getResName()  { return resName;  }
    public String   getChainID()  { return chainID;  }
    public int      getResSeq()   { return resSeq;   }
    public String   getICode()    { return iCode;    }
    public Location getCentre()   { return centre;   }

    /**Returns the coordinate for the given dimension i (i.e. x, y, z).
     *
     * @param i The dimension for which one wants the coordinate.
     * @return The coordinate of the given dimension.
     */
    public double getCoordinate(int i)  {
        return centre.getDoubleCoordinate(i);
    }

    public ArrayList<Integer> getRelevantContainers() {
        return relevantContainers;
        }
    public void setRelevantContainers(ArrayList<Integer> arr) {
        relevantContainers = arr;
    }

    /**Returns whether this Atom clashes with the other Atom, i.e. if the
     * volumes they span overlap.
     *
     * @param other Atom to check the clash against.
     * @return true if the Atoms clash, false otherwise.
     */
    public boolean clashes(Atom other) {
        return getDistance(other.getCentre()) < ATOMRADIUS*2;
    }
    private double getDistance(Location other) {
        return centre.getDistance(other);
    }
}
