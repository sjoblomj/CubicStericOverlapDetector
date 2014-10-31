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

import java.util.ArrayList;
import java.util.List;

/**This class acts as an entry for the HashMap. It contains an
 * ArrayList of Pairs. The key of each Pair is an Integer, which
 * corresponds to the container ordinal of the atoms; this
 * is used to allow for hash collisions. The value of a Pair is
 * the Atom, that is present in the container that the HashEntry
 * corresponds to.
 *
 * @author Johan Sjöblom
 */
public class HashEntry {
    private List<Pair<Integer, Atom>> atomPairs =
            new ArrayList<Pair<Integer, Atom>>();

    public HashEntry() { }
    public HashEntry(Integer l, Atom a) { add(l, a); }

    /**Given an Atom and the container ordinal,
     * this method will place the atom in the ArrayList.
     *
     * @param location The block the Atom belongs to
     * @param atom The Atom to insert
     */
    public void add(Integer location, Atom atom) {
        atomPairs.add(new Pair<Integer, Atom>(location, atom));
    }

    public List<Atom> getContent(Integer container) {
        ArrayList<Atom> arr = new ArrayList<Atom>(atomPairs.size());
        for(int i = 0; i < atomPairs.size(); i++)
            if(atomPairs.get(i).getL().equals(container))
                arr.add(atomPairs.get(i).getR());
        return arr;
    }
    public boolean contains(Integer location, Atom atom) {
        for(int i = 0; i < atomPairs.size(); i++) {
            if (atomPairs.get(i).getL().equals(location) &&
                atomPairs.get(i).getR().equals(atom)) {
                    return true;
            }
        }
        return false;
    }


    @SuppressWarnings("rawtypes")
    public class Pair<L extends Comparable,R> implements Comparable<Pair> {
        private L l;
        private R r;
        public Pair(L l, R r) {
            this.l = l;
            this.r = r;
        }
        public L getL() { return l; }
        public R getR() { return r; }
        public void setL(L l) { this.l = l; }
        public void setR(R r) { this.r = r; }
        @SuppressWarnings("unchecked")
        public int compareTo(Pair o) {
            return l.compareTo(o.l);
        }
    }
}
