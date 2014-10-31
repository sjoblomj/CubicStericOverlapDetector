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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;

/**Main class. Reads two *pdb-files, calculates the number of Atom clashes
 * between them, and writes the clashing Atoms to a file. The calculations
 * are done in two ways:<br /><br />
 *
 * A brute forcing method is implemented for the sake of comparison.
 * It will loop through both the lists of Atoms, and compare the
 * distances between each Atom. This method is O(n<sup>2</sup>).<br /><br />
 *
 * The efficient solution uses hash maps for comparisons. The main problem
 * is that atoms might clash even though their centres have different
 * coordinates, since the atoms span a volume. Thus, storing this in a hash
 * map seems impossible, since different centres may clash, but will
 * be placed in different places by the hash map. This is overcome by
 * noting the lowest and greatest coordinate in the volume that the atoms
 * of the molecule spans, when reading the files. This data, along
 * with the atom radius is given to the Space class. The Space class
 * thus knows how big the volume of the entire molecule is. It will
 * slice up the volume spanned into smaller cubes (called "containers",
 * since the Space class supports any number of dimensions, and "cube"
 * refers to volumes in three dimensions), of the same size as the atom
 * radius. These containers can be enumerated. The Space class has a
 * method that can calculate the container an atom would be placed in,
 * and this allows us to use the ordinal of the container as a hash
 * map key.<br /><br />
 *
 * The second file is looped over, and every atom gets its container
 * ordinal calculated. A HashEntry object is created, which is an
 * ArrayList of Pairs, where one value in the pair is the container
 * ordinal, and the second value is the Atom object. This somewhat
 * complicated solution is needed, for a few reasons: Two different
 * values can get the same hash, so keeping track of the container
 * ordinal is needed to minimize the number of comparisons. Many
 * Atoms could potentially be within the same container, which makes
 * the list needed.<br /><br />
 *
 * The Space class will, as said, divide the molecule volume into
 * containers of the size of the atom radius. For an Atom in
 * container i, there are thus several locations where a clashing
 * atom could lie: in container i, or in container i + 1 in each
 * dimension, since an atom in container i could clash with an
 * atom in container i + 1. Thus, for each dimension, the possible
 * containers that could hold a clash is i, i + 1 and i - 1;
 * a total of 3. For our three dimensions, the number of containers
 * we need to look in is thus 3*3*3=27, unless the container is in
 * the border regions of the molecule, in which case some containers
 * would lie outside it, and there is no need to look there.<br /><br />
 *
 * While looking in 27 containers for each lookup may seem
 * expensive, this number is a constant, depending on the number
 * of dimensions (three in our case). Thus, this number does
 * not change with the size of the molecule. Since the task at
 * hand was to minimize the amount of lookups needed, and any
 * pre-calculations could be done, the method implemented here
 * is O(n).
 *
 * @author Johan Sjöblom
 *
 */
public class Main {
    private static Gui gui = null;

    /**Main method. If given no parameters (args.length == 0), the GUI will
     * start. If the wrong amount of parameters are given, usage info will
     * be printed and the program will quit.
     * If the right amount of parameters are given, the program will
     * calculate a result from the given files using the given calculation
     * method, and will write the result to an appropriate file (either
     * provided by the user, or default to "output.txt" if no file name
     * to write to is given).
     *
     * @param args Parameters to the program.
     */
    public static void main(String[] args) {

        if(args.length == 0) {
            gui = new Gui();
        }
        else if(args.length != 3 && args.length != 4) {
            if(args[0].compareTo("--version") == 0 ||
                    args[0].compareTo("-v") == 0) {
                Utils.log(Utils.PROGRAMNAME + " " + Utils.PROGRAMVERSION);
                Utils.log(Utils.PROGRAMDATE);
                Utils.log("Written by " + Utils.AUTHORNAME + ",  " +
                        Utils.AUTHOREMAIL);
                Utils.log(Utils.AUTHORWEBSITE);
                System.exit(0);
            }
            if(args[0].compareTo("--license") == 0 ||
                    args[0].compareTo("-l") == 0) {
                Utils.log(Utils.getLicenseText());
                System.exit(0);
            }
            Utils.log(Utils.PROGRAMNAME + ".  Usage:");
            Utils.log("java -jar csod.jar INPUT1.pdb INPUT2.pdb -METHOD OUTPUT.txt\n");
            Utils.log("Arguments:");
            Utils.log("INPUT1.pdb and INPUT2.pdb  :  Filenames to *.pdb files to compare");
            Utils.log("METHOD                     :  Valid options are '-h' or '-b' for");
            Utils.log("                              hash comparison or bruteforce");
            Utils.log("                              comparison, respectively.");
            Utils.log("OUTPUT.txt                 :  File to write result to. Optional,");
            Utils.log("                              'output.txt' is used as default.\n");
            Utils.log("If no arguments are given, the GUI will start up.");
            System.exit(1);
        }
        else {
            String in0 = args[0];
            String in1 = args[1];
            String outfile = "output.txt";
            if(args.length == 4) {
                outfile = args[3];
            }
            boolean hash = args[2].equals("-h") | args[2].equals("h");

            PrintStream ps = null;
            try {
                ps = new PrintStream(new File(outfile));
            } catch (FileNotFoundException e) {
                Utils.log("Cannot open file " + outfile + " for writing.");
                e.printStackTrace();
                System.exit(1);
            }

            if(Utils.run(hash, in0, in1, ps))
                Utils.log("Result written to " + outfile);
            else
                Utils.log("Errors during computation.");
            ps.close();
        }
    }


    /**Returns whether or not the program is run from the GUI
     * (as opposed to the command line).
     *
     * @return True if run from the GUI, False if run from command line.
     */
    public static boolean runFromGui() {
        return gui != null;
    }
    /**Method to send message to the GUI log area.
     *
     * @param msg Message to be logged in the GUI log area.
     */
    public static void guiLog(String msg) {
        gui.guiLog(msg);
    }
}
