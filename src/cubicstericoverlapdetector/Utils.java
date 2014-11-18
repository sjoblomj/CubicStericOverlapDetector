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

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import cubicstericoverlapdetector.HashEntry.Pair;

/**Class with some miscellaneous utility methods
 *
 * @author Johan Sjöblom
 *
 */
public class Utils {
    public final static String PROGRAMNAME    = "Cubic Steric Overlap Detector";
    public final static String PROGRAMVERSION = "1.0";
    public final static String PROGRAMDATE    = "December 2013";
    public final static String AUTHORNAME     = "Johan Sjöblom";
    public final static String AUTHOREMAIL    = "sjoblomj88@gmail.com";
    public final static String AUTHORWEBSITE  = "http://www.thehomepageinternet.org";
    public final static String LICENSEFILE    = "LICENSE";

    /**This method basically calls three methods:<br />
     * 1: precalculate()<br />
     * 2: hashCompare() or bruteforceCompare()<br />
     * 3: writeResults()<br /><br />
     *
     * The time will be noted and printed between the three operations.
     * Which method to use in step two is decided from the hash parameter.
     *
     * @param hash True to use the hashCompare() method,
     * False to use the bruteforceCompare() method.
     * @param infile0 First  *.pdb file to read.
     * @param infile1 Second *.pdb file to read.
     * @param output OutputStream that receives the results from
     * the writeResults() method. Can be used to write to a file
     * or to a String.
     * @return True if the method succeeded without problems,
     * false otherwise.
     */
    @SuppressWarnings("rawtypes")
    public static boolean run(boolean hash,
                           String infile0,
                           String infile1,
                           OutputStream output) {

        // Create variables and note time.
        long startTime = System.nanoTime();
        ArrayList<Atom> arr0 = new ArrayList<Atom>();
        ArrayList<Atom> arr1 = new ArrayList<Atom>();
        Map<Integer, HashEntry> hashmap = new HashMap<Integer, HashEntry>();

        // Do pre-calculations. Returns false if there were errors.
        boolean success = precalculate(hash,
                                       infile0,
                                       infile1,
                                       arr0,
                                       arr1,
                                       hashmap);

        // Quit if the pre-calculations failed.
        if(!success) {
            log("Failed to do pre-calculations. Quitting.");
            return false;
        }
        long mainTime = System.nanoTime();
        log("Time taken for pre-calculations: " +
            (mainTime - startTime) / 1000000 + " ms.");

        // Create variables.
        ArrayList<Pair> resultlist = new ArrayList<Pair>();
        String method = "";
        int comparisons;

        // Call the hashCompare() or bruteforceCompare method
        if(hash) {
            method = "Hashing";
            comparisons = hashCompare(arr0, resultlist, hashmap);
        } else {
            method = "Bruteforce";
            comparisons = bruteforceCompare(arr0, arr1, resultlist);
        }

        // Sort the result list and log.
        sortResults(resultlist);
        long doneTime = System.nanoTime();
        log("For the " + method + " method: " + resultlist.size() +
            " matches found. Comparisons needed: " + comparisons +
            ". Time taken: " + (doneTime - mainTime) / 1000000 + " ms.");

        // Write the result to the OutputStream output.
        writeResults(output, resultlist);

        // Log.
        long endTime = System.nanoTime();
        log("Total time taken: " + (endTime - startTime) / 1000000 + " ms.");
        return true;
    }


    /**Perform pre-calculations. The method will read the *.pdb files given
     * by infile0 and infile1 and create Atoms out of the file content.
     * These Atoms will be placed in the ArrayLists arr0 and arr1. If
     * hashMode is false, then the method is done after that step. Otherwise,
     * a Space object will be created, and arr1 will be put into the given
     * hashmap using the space. Finally, arr0 will be iterated through, and
     * all Atoms in it will have their relevantContainers list set.
     *
     * @param hashMode If false, the *.pdb files will be read and then the
     *        method finishes. If not, the rest of the method will be run.
     * @param infile0 The filename of the first  *.pdb file to read.
     * @param infile1 The filename of the second *.pdb file to read.
     * @param arr0 ArrayList that will be filled with the Atoms of the
     *        first  *.pdb file
     * @param arr1 ArrayList that will be filled with the Atoms of the
     *        second *.pdb file
     * @param hashmap The Map which arr1's Atoms will be put into, together
     *        with their container ordinal from the created Space object.
     * @return true if the files were read correctly, false otherwise.
     */
    public static boolean precalculate(boolean hashMode,
                                       String infile0,
                                       String infile1,
                                       ArrayList<Atom> arr0,
                                       ArrayList<Atom> arr1,
                                       Map<Integer, HashEntry> hashmap) {
        Double[] dmin = {Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        Double[] dmax = {Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};

        readPDBFile(arr0, infile0, null, null);
        readPDBFile(arr1, infile1, dmin, dmax);
        if(arr0.size() == 0 || arr1.size() == 0) {
            return false;
        }
        log("Size of molecules: " + arr0.size() + " atoms and " +
            arr1.size() + " atoms.");

        // If we are not about to do a Hashing comparison, we're done now.
        if(!hashMode) {
            return true;
        }

        Location min = new Location(dmin);
        Location max = new Location(dmax);


        // Create the Space which will determine the containers:
        Space space = new Space(Atom.ATOMRADIUS * 2, min, max);


        // Put arr1 into the hash map:
        for(int i = 0; i < arr1.size(); i++) {
            Atom atom = arr1.get(i);
            Integer container = space.getContainer(atom.getCentre());

            HashEntry he = null;
            if(hashmap.containsKey(container)) {
                // Atoms are already present in the hash map in that place.
                he = hashmap.get(container);
                if(he.contains(container, atom))
                    continue;
                he.add(container, atom);
            }
            else
                he = new HashEntry(container, atom);
            hashmap.put(container, he);
        }

        // Loop though arr0. For reach Atom in it, find the containers
        // that are near it (including the container of the Atom
        // itself) and store those as a property of the Atom object.
        // Doing this now will save time later.
        for(int i = 0; i < arr0.size(); i++) {
            Atom atom = arr0.get(i);
            atom.setRelevantContainers(
                    space.getNearbyContainers(atom.getCentre()));
        }
        return true;
    }


    /**Given the ArrayList arr of Atoms and the Map hashmap, this method will
     * go through all Atoms of arr and get the container ordinals that are
     * nearby each Atom. Using the hashmap, the method will then lookup the
     * container ordinal and see if there is a clashing atom in the other
     * molecule. All clashes are placed in resultlist.
     *
     * @param arr ArrayList of all Atoms of a molecule.
     * @param resultlist ArrayList that all clashing atoms are placed in.
     * @param hashmap Map of container ordinals and Atoms of a molecule.
     * @return The number of comparisons that were needed.
     */
    @SuppressWarnings("rawtypes")
    public static int hashCompare(ArrayList<Atom> arr,
                                  ArrayList<Pair> resultlist,
                                  Map<Integer, HashEntry> hashmap) {
        int comparisons = 0;
        for(int i = 0; i < arr.size(); i++) {
            Atom atom = arr.get(i);
            ArrayList<Integer> containers = atom.getRelevantContainers();
            for(int j = 0; j < containers.size(); j++) {
                Integer container = containers.get(j);
                HashEntry he = hashmap.get(container);
                if(he == null)
                    continue;

                List<Atom> lst = he.getContent(container);
                for(int k = 0; k < lst.size(); k++) {
                    comparisons++;
                    Atom a = lst.get(k);
                    if(atom.clashes(a)) {
                        resultlist.add(
                            he.new Pair<Integer, Atom>(a.getSerial(), a));
                    }
                }
            }
        }
        return comparisons;
    }


    /**Brute force method for finding atom clashes between two molecules.
     * Each atom of arr0 is compared against each atom in arr1 to see if
     * there are any clashes.
     *
     * @param arr0 ArrayList of Atoms of a molecule.
     * @param arr1 ArrayList of Atoms of a different molecule.
     * @param resultlist ArrayList which will be filled with all clashes.
     * @return The number of comparisons that were needed.
     */
    @SuppressWarnings("rawtypes")
    public static int bruteforceCompare(ArrayList<Atom> arr0,
                                        ArrayList<Atom> arr1,
                                        ArrayList<Pair> resultlist) {
        int comparisons = 0;
        HashEntry he = new HashEntry();

        for(int i = 0; i < arr0.size(); i++) {
            for(int j = 0; j < arr1.size(); j++) {
                comparisons++;
                Atom a = arr0.get(i);
                Atom b = arr1.get(j);
                if(a.clashes(b)) {
                    resultlist.add(
                            he.new Pair<Integer, Atom>(b.getSerial(), b));
                }
            }
        }
        return comparisons;
    }

    /**Method to log the given String. If the program is run through the GUI,
     * the message will be written to the GUI log. If run through the command
     * line, the message will be written to standard output.
     *
     * @param msg Message to log
     */
    public static void log(String msg) {
        if(Main.runFromGui())
            Main.guiLog(msg);
        else
            System.out.println(msg);
    }

    /**This method will take the name of a *.pdb file, read it, and create as
     * many Atoms as there are data in the PDB file. All created Atoms are
     * placed in the given ArrayList. If the parameters dmin and dmax are not
     * null, the smallest and largest coordinates of all Atoms will be found
     * and stored in the lists.
     *
     * @param arr ArrayList which will contain all atoms created from the
     *        given *.pdb-file.
     * @param filename Name of *.pdb file to read.
     * @param dmin If not null, this list will contain the smallest
     *        coordinates of all the Atoms.
     * @param dmax If not null, this list will contain the largest
     *        coordinates of all the Atoms.
     */
    public static void readPDBFile(ArrayList<Atom> arr,
                                   String filename,
                                   Double[] dmin,
                                   Double[] dmax) {

        Scanner in = null;
        try {
            // Read the file line by line
            in = new Scanner(new FileReader(filename));
            while(in.hasNextLine()) {
                String line = in.nextLine();

                String fstword = line.split("\\s+")[0];
                // Only parse lines beginning with "ATOM" or "HEATM"
                if(!fstword.equals("ATOM") && !fstword.equals("HETATM"))
                    continue;

                // Parse the line
                String[] coords = new String[3];
                int serial      = Integer.parseInt(line.substring(6, 12).trim());
                String atomName = line.substring(12, 16).trim();
                String altLoc   = line.substring(16, 17).trim();
                String resName  = line.substring(17, 21).trim();
                String chainID  = line.substring(21, 22).trim();
                int resSeq      = Integer.parseInt(line.substring(22, 26).trim());
                String iCode    = line.substring(26, 27).trim();
                coords[0]       = line.substring(30, 38).trim();
                coords[1]       = line.substring(38, 46).trim();
                coords[2]       = line.substring(46, 54).trim();

                // Create Atom from the data in line.
                Atom atom = new Atom(serial,
                                     atomName,
                                     altLoc,
                                     resName,
                                     chainID,
                                     resSeq,
                                     iCode,
                                     coords);

                // Find the largest and smallest points in among all atoms
                if(dmin != null && dmax != null) {
                    for(int i = 0; i < dmin.length; i++) {
                        // Is any of atom[i]'s coordinates less than the
                        // current minimum or greater than the current
                        // maximum? Add some error margin.
                        double coord = atom.getCoordinate(i);
                        if(coord < dmin[i])
                            dmin[i] = coord - 0.001;
                        if(coord > dmax[i])
                            dmax[i] = coord + 0.001;
                    }
                }
                // Add atom to the ArrayList
                arr.add(atom);
            }
        } catch(FileNotFoundException e) {
            log("Could not open file " + filename);
        } finally {
            if(in != null) {
                in.close();
            }
        }
    }

    /**Method for writing results to a given OutputStream. The ArrayList
     * list should be Pairs of Integers and Atoms, where the Integer
     * denotes the sorting order. Data from the Atoms will be written to
     * the OutputStream output, along with a counter of how many unique
     * Atoms were found (size of list).
     *
     * @param output OutputStream to write data to.
     * @param list ArrayList of Pairs of Integers and Atoms.
     */
    @SuppressWarnings("rawtypes")
    public static void writeResults(OutputStream output,
                                    ArrayList<Pair> list) {

        Formatter fmt = new Formatter(output);
        for(int i = 0; i < list.size(); i++) {
            Atom atom = (Atom)list.get(i).getR();
            fmt.format("%d ",      atom.getSerial());
            fmt.format(            atom.getResName());
            fmt.format(" %4d",     atom.getResSeq());
            fmt.format("  %-3s%n", atom.getAtomName());
            fmt.flush();
        }
        fmt.format("Number of clashing atoms: %s%n", list.size());
        fmt.flush();
        fmt.close();
    }


    /**Method for sorting an ArrayList of Pairs of Integers and Atoms,
     * where the Integer denotes the sorting order. The ArrayList will
     * be sorted, and duplicate adjacent Atoms will be removed.
     *
     * @param list ArrayList of Pairs of Integers and Atoms.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static void sortResults(ArrayList<Pair> list) {

        Collections.sort(list);
        Atom prev = null;
        for(int i = list.size() - 1; i >= 0; i--) {
            Atom match = (Atom)list.get(i).getR();

            // Remove any duplicates.
            if(match.equals(prev))
                list.remove(i + 1);
            prev = match;
        }
    }

    /**Reads the file specified by the LICENSEFILE variable,
     * and returns the content of the file.
     *
     * @return The content of the file specified by LICENSEFILE.
     */
    public static String getLicenseText() {

        String output = "Licensed under GPL 2.0 or later\n\n";

        // Read the file line by line
        try {
            Scanner in = new Scanner(new FileReader(LICENSEFILE));
            while(in.hasNextLine())
                output += in.nextLine() + "\n";
            in.close();
        } catch(FileNotFoundException e) {
            output += "Could not open file " + LICENSEFILE;
        }
        return output;
    }
}
