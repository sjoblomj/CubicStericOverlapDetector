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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

/**Class for all GUI functionality. Contains no interesting code,
 * unless you're interested in messy GUI's.
 *
 * @author Johan Sjöblom
 *
 */
public class Gui extends JFrame {
    private static final long serialVersionUID = -5255853126005155667L;
    private JTextArea   outputarea;
    private JTextArea   logarea;
    private JButton     fstLoadButton;
    private JButton     sndLoadButton;
    private JTextField  fstTextField;
    private JTextField  sndTextField;
    private JButton     calculateButton;
    private JButton     saveButton;
    private JButton     helpButton;
    private JButton     aboutButton;
    private ButtonGroup radiogroup;
    private JSplitPane  horizontalPane;
    private JSplitPane  verticalPane;

    private int windowWidth  = 800;
    private int windowHeight = 600;
    private final String ICONPATH       = "res/molspace.png";
    private final String ABOUTIMAGEPATH = "res/molspace.png";

    /**Constructor. Creates the GUI.
     */
    public Gui() {
        initComponents();
        setVisible(true);
    }

    /**Method to append the given String at the end of the log area.
     * A newline is added after the String, and the cursor is placed
     * at the end of the log, so that the new text is visible in the
     * scroll bar.
     *
     * @param msg Message to place in the log area.
     */
    public void guiLog(String msg) {
        logarea.append(msg + "\n");
        logarea.setCaretPosition(logarea.getText().length() - 1);
    }

    /**Initializes the components and creates the GUI.
     */
    private void initComponents() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(windowWidth, windowHeight);
        setTitle(Utils.PROGRAMNAME);
        setIconImage(Toolkit.getDefaultToolkit().getImage(ICONPATH));

        // Create output area with its JPanel and scroll bars
        outputarea = new JTextArea("");
        outputarea.setEditable(false);
        outputarea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane outputScroller = new JScrollPane(outputarea);
        outputScroller.setPreferredSize(new Dimension(250, 80));
        outputScroller.setAlignmentX(LEFT_ALIGNMENT);
        JPanel outputareapanel = new JPanel();
        outputareapanel.setLayout(
                        new BoxLayout(outputareapanel, BoxLayout.PAGE_AXIS));
        outputareapanel.add(outputScroller);
        outputareapanel.setBorder(
                        BorderFactory.createTitledBorder("Output"));

        // Create log area with its JPanel and scroll bars
        logarea = new JTextArea("");
        logarea.setEditable(false);
        JScrollPane logScroller = new JScrollPane(logarea);
        logScroller.setPreferredSize(new Dimension(250, 80));
        logScroller.setAlignmentX(LEFT_ALIGNMENT);
        JPanel logareapanel = new JPanel();
        logareapanel.setLayout(
                        new BoxLayout(logareapanel, BoxLayout.PAGE_AXIS));
        logareapanel.add(logScroller);
        logareapanel.setBorder(BorderFactory.createTitledBorder("Log"));


        JLabel fstLoadLabel     = new JLabel("First File");
        JLabel sndLoadLabel     = new JLabel("Second File");
        fstTextField            = new JTextField("");
        sndTextField            = new JTextField("");
        fstLoadButton           = new JButton("Load");
        sndLoadButton           = new JButton("Load");
        JRadioButton hashRadio  = new JRadioButton("Hashing method");
        JRadioButton bruteRadio = new JRadioButton("Brute force method");
        calculateButton         = new JButton("Calculate");
        saveButton              = new JButton("Save Results");
        helpButton              = new JButton("Help");
        aboutButton             = new JButton("About");
        saveButton.setEnabled(false);
        saveButton     .setMnemonic(KeyEvent.VK_S);
        calculateButton.setMnemonic(KeyEvent.VK_C);
        helpButton     .setMnemonic(KeyEvent.VK_E);
        aboutButton    .setMnemonic(KeyEvent.VK_A);
        hashRadio      .setMnemonic(KeyEvent.VK_H);
        bruteRadio     .setMnemonic(KeyEvent.VK_B);

        hashRadio.setActionCommand("Hashing");
        hashRadio.setSelected(true);
        bruteRadio.setActionCommand("Bruteforce");
        radiogroup = new ButtonGroup();
        radiogroup.add(hashRadio);
        radiogroup.add(bruteRadio);

        // Process the button presses, and capture when the window
        // is resized.
        fstLoadButton  .addActionListener(new  loadFileActionListener());
        sndLoadButton  .addActionListener(new  loadFileActionListener());
        calculateButton.addActionListener(new calculateActionListener());
        saveButton     .addActionListener(new  saveFileActionListener());
        helpButton     .addActionListener(new      helpActionListener());
        aboutButton    .addActionListener(new     aboutActionListener());
        this           .addComponentListener(new resizeComponentListener());



        // The container and the GridBagConstraints are for placing the
        // components in the dialog
        Container cont = new Container();
        GridBagConstraints c0 = new GridBagConstraints(),
                           c1 = new GridBagConstraints(),
                           c2 = new GridBagConstraints();
        cont.setLayout(new GridBagLayout());

        c0.fill = GridBagConstraints.HORIZONTAL;
        c0.weightx = 0.5;
        c0.gridwidth = 2;
        c0.gridy = 0;
        c0.gridx = 0;

        c1.fill = GridBagConstraints.HORIZONTAL;
        c1.weightx = 0.0;
        c1.gridwidth = 1;
        c1.gridy = 0;
        c1.gridx = 2;

        c2.fill = GridBagConstraints.HORIZONTAL;
        c2.weightx = 0.0;
        c2.gridwidth = 3;
        c2.gridy = 0;
        c2.gridx = 0;


        cont.add(fstLoadLabel, c0);
        c0.gridy++;
        c1.gridy++;
        cont.add(Box.createRigidArea(new Dimension(0, 10)), c0);
        c0.gridy++;
        c1.gridy++;
        cont.add(fstTextField, c0);
        cont.add(fstLoadButton, c1);
        c0.gridy += 2;
        c1.gridy += 2;

        cont.add(Box.createRigidArea(new Dimension(0, 10)), c0);
        c0.gridy++;
        c1.gridy++;
        cont.add(sndLoadLabel, c0);
        c0.gridy++;
        c1.gridy++;
        cont.add(sndTextField, c0);
        cont.add(sndLoadButton, c1);


        c2.gridy = c0.gridy + 2;
        addComponent(cont, c2, 10, hashRadio);
        addComponent(cont, c2, 10, bruteRadio);
        addComponent(cont, c2, 10, calculateButton);
        addComponent(cont, c2, 10, saveButton);
        addComponent(cont, c2, 20, helpButton);
        addComponent(cont, c2, 10, aboutButton);

        horizontalPane = new JSplitPane(
                    JSplitPane.HORIZONTAL_SPLIT, outputareapanel, cont);
        horizontalPane.setOneTouchExpandable(true);
        horizontalPane.setDividerLocation((int)(this.getWidth() * 0.75));

        verticalPane = new JSplitPane(
                    JSplitPane.VERTICAL_SPLIT, horizontalPane, logareapanel);
        verticalPane.setOneTouchExpandable(true);
        verticalPane.setDividerLocation((int)(this.getHeight() * 0.75));

        getContentPane().add(verticalPane);
    }

    /**This method will add the Component component to the Container
     * container, using the GridBagContraints c. An empty space of
     * size vspace is added before the component. The gridy property
     * of c will be increased twice.
     *
     * @param container Container to add component in.
     * @param c The GridBagContraints to use when adding the component.
     * @param vspace Vertical space to add before component.
     * @param component Component to be added to container.
     */
    private void addComponent(Container container,
                              GridBagConstraints c,
                              int vspace,
                              Component component) {
        container.add(Box.createRigidArea(new Dimension(0, vspace)), c);
        c.gridy++;
        container.add(component, c);
        c.gridy++;
    }

    /**Action to be performed when the Load buttons are pressed.
     * The user will be presented with an Open Dialog, and the path
     * of the chosen file will be placed in the appropriate field.
     */
    private class loadFileActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter(
                            "Protein Data Bank Files (*.pdb)", "pdb"));
            if(fc.showOpenDialog(Gui.this) == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                JTextField field = null;
                if(e.getSource() == fstLoadButton) {
                    field = fstTextField;
                }
                else if(e.getSource() == sndLoadButton) {
                    field = sndTextField;
                }
                if(field != null) {
                    field.setText(file.getAbsolutePath());
                }
            }
        }
    }

    /**Action to be performed when the Save button is pressed.
     * The user will be presented with a Save Dialog, and the text
     * in the output area will be saved to the file.
     */
    private class saveFileActionListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            // Show Save file dialog
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter(
                            "Text Files (*.txt)", "txt"));

            if(fc.showSaveDialog(Gui.this) == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                BufferedWriter writer = null;
                try {
                    // Write to file
                    writer = new BufferedWriter(new FileWriter(file));
                    writer.write(outputarea.getText());
                } catch(Exception e) {
                    guiLog("Could not save to file!");
                    e.printStackTrace();
                } finally {
                    try {
                        writer.close();
                    } catch(Exception e) {}
                }
            }
        }
    }


    /**Action to be performed when the Calculate button is pressed.
     * A new thread will be created where the work is done, so that
     * the GUI doesn't freeze. The appropriate method to use for
     * the calculations is determined from the radio buttons (hashing
     * or brute force). The files to load are read from the input file
     * text fields. Utils.run() is called, which will do the calculations.
     * A PrintStream is created, so that Utils.writeResults() writes
     * to a String rather than a file. This String with the result is
     * instead written to the outputarea.
     */
    private class calculateActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            guiLog("\n");

            // Do work in a worker thread, so that the GUI doesn't freeze.
            class ProcessThread extends Thread {
                public void run() {
                    // Disable buttons and empty old output
                    saveButton.setEnabled(false);
                    calculateButton.setEnabled(false);
                    outputarea.setText("");

                    // Create Streams for getting formatted result
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    PrintStream ps = new PrintStream(baos);

                    // Determine method to use for calculation from
                    // the radio buttons.
                    ButtonModel radioModel = radiogroup.getSelection();
                    String actionCommand = (radioModel == null) ?
                                    "" : radioModel.getActionCommand();
                    boolean hash = actionCommand.equals("Hashing");

                    // Do the actual work
                    boolean success = Utils.run(hash,
                                                fstTextField.getText(),
                                                sndTextField.getText(),
                                                ps);

                    // Fill output area and re-enable buttons
                    if(success) {
                        outputarea.setText(baos.toString());
                        outputarea.setCaretPosition(
                                    outputarea.getText().length() - 1);
                        saveButton.setEnabled(true);
                    }
                    calculateButton.setEnabled(true);
                }
            };
            Thread processThread = new Thread(new ProcessThread());
            processThread.start();
        }
    }

    /**Action to be performed when the About button is pressed.
     * The About dialog is created and displayed. It has two
     * tabs; one with a picture and text about the version,
     * author etc, and one with the license text.
     */
    private class aboutActionListener implements ActionListener {
        public class aboutDialog extends JFrame implements ActionListener {
            private static final long serialVersionUID =
                            -5043817534943419400L;

            public aboutDialog() {
                this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                setSize(650, 500);
                setTitle("About " + Utils.PROGRAMNAME);
                setIconImage(Toolkit.getDefaultToolkit().getImage(ICONPATH));

                JButton close = new JButton("Close");
                close.addActionListener(this);

                JTextArea licensetext = new JTextArea();
                licensetext.setText(Utils.getLicenseText());
                licensetext.setCaretPosition(0);
                licensetext.setEditable(false);
                JScrollPane licenseScroller = new JScrollPane(licensetext);
                licenseScroller.setPreferredSize(new Dimension(550, 500));
                licenseScroller.setAlignmentX(LEFT_ALIGNMENT);

                JTabbedPane tabbedPane = new JTabbedPane();
                JComponent panel0 = new JPanel();
                panel0.setLayout(new FlowLayout());

                // Move the image and text down a bit:
                panel0.add(Box.createRigidArea(
                                new Dimension(this.getWidth(), 50)));

                try{
                    panel0.add(new ImagePanel(ABOUTIMAGEPATH));
                } catch(NullPointerException e) { }

                panel0.add(new JLabel("<html><h1><b><u>" + Utils.PROGRAMNAME+
                           "</u></b></h1><br />Version " +
                           Utils.PROGRAMVERSION + "<br />" +
                           Utils.PROGRAMDATE + "<br /><br />" +
                           "Written by " + Utils.AUTHORNAME + "<br />" +
                           "<a href='mailto:" + Utils.AUTHOREMAIL + "'>" +
                           Utils.AUTHOREMAIL   + "</a><br />" +
                           "<a href='" + Utils.AUTHORWEBSITE + "'>" +
                           Utils.AUTHORWEBSITE + "</a><br /><br /><br />" +
                           "Program for detecting the number of clashing"+
                           "<br />atoms between two molecules (the Steric " +
                           "overlap).<br /><br /><br />" +
                           "<small>Icon is of the Crambin molecule (1CRN)" +
                           "<br />drawn inside a space</small></html>"));
                panel0.setVisible(true);

                JComponent panel1 = new JPanel();
                panel1.setLayout(new GridLayout(0, 1));
                panel1.add(licenseScroller);


                tabbedPane.addTab("About",   panel0);
                tabbedPane.addTab("License", panel1);
                getContentPane().add(tabbedPane, BorderLayout.CENTER);
                getContentPane().add(close,      BorderLayout.PAGE_END);
            }


            /**Class that extends the JPanel, and simply displays the
             * picture to load from the given path in a JPanel.
             */
            public class ImagePanel extends JPanel {
                private static final long serialVersionUID =
                                -8036801047071456735L;
                private BufferedImage image;
                public ImagePanel(String path) {
                   try {
                      image = ImageIO.read(new File(path));
                      this.setPreferredSize(new Dimension(
                                      image.getWidth(), image.getHeight()));
                   } catch (IOException ex) { }
                }
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    g.drawImage(image, 0, 0, null);
                }
            }

            /**Closes the About dialog
             */
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }

        /**Creates and shows the About dialog
         */
        public void actionPerformed(ActionEvent e) {
            new aboutDialog().setVisible(true);
        }
    }

    /**Action to be performed when the Help button is pressed.
     * The Help dialog is created and displayed.
     */
    private class helpActionListener implements ActionListener {
        public class helpDialog extends JFrame implements ActionListener {
            private static final long serialVersionUID =
                            -5043817534943419400L;

            public helpDialog() {
                this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                setSize(600, 500);
                setTitle("Help");
                setIconImage(Toolkit.getDefaultToolkit().getImage(ICONPATH));

                JButton close = new JButton("Close");
                close.addActionListener(this);

                String text = "When modelling proteins, or when docking a " +
                              "pair of proteins together, one might want " +
                              "to test whether the solution is free from " +
                              "steric clashes. That is, when considering " +
                              "atoms to be hard spheres, one wants to know "+
                              "whether any atoms are so close together " +
                              "that the spheres representing the atoms " +
                              "intersect in 3-D. This program will detect " +
                              "any such steric overlaps between two " +
                              "proteins read from Protein Data Bank files " +
                              "(*.pdb files). Proteins are complicated 3-D "+
                              "structures, and are often ''folded'' in " +
                              "such ways that it would be very complicated "+
                              "to predict whether an arbitrary atom is " +
                              "close to another in the molecule.\n\n" +
                              "This program can find steric clashes " +
                              "between two molecules, using two methods. " +
                              "The fast method, called Hashing will find " +
                              "clashes in O(n) time. The brute force " +
                              "method is much slower and intended as a " +
                              "reference, and finds clashes in O(n^2) " +
                              "time.\n\n" +
                              "Program usage:\n" +
                              "Load Protein Databank Files (*.pdb) using " +
                              "the buttons on the right. Choose either the "+
                              "hashing or brute force method. Click the " +
                              "Calculate button to look for the steric " +
                              "clashes. The results (i.e. the atoms that " +
                              "clash) will be written to the output area " +
                              "to the left. Press the Save Results button " +
                              "to save the content of the output area to a "+
                              "file. The button becomes clickable after " +
                              "the calculations are finished, as there is " +
                              "nothing to save before that.";

                JTextArea helptext = new JTextArea();
                helptext.setText(text);
                helptext.setCaretPosition(0);
                helptext.setLineWrap(true);
                helptext.setWrapStyleWord(true);
                helptext.setEditable(false);
                JScrollPane helptextScroller = new JScrollPane(
                                helptext,
                                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                helptextScroller.setPreferredSize(new Dimension(550, 500));
                helptextScroller.setAlignmentX(LEFT_ALIGNMENT);

                getContentPane().add(helptextScroller, BorderLayout.CENTER);
                getContentPane().add(close, BorderLayout.PAGE_END);
            }

            /**Closes the Help dialog
             */
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        }

        /**Creates and shows the Help dialog
         */
        public void actionPerformed(ActionEvent e) {
            new helpDialog().setVisible(true);
        }
    }

    /**This class will be called when the main window is resized. The output
     * area is supposed to be resized, while the proportions of the size of
     * the log area and button area is constant.
     *
     * The vertical and horizontal panes will have their dividers set to the
     * same ratio as they were on previous to the window resizing.
     * This code is not flawless, but good enough.
     */
    private class resizeComponentListener implements ComponentListener {
        public void componentResized(ComponentEvent e) {
            Component c = e.getComponent();
            double oldVerticalHeightRatio   = (double)  verticalPane.getDividerLocation() / windowHeight;
            double oldHorizontalWidthRatio  = (double)horizontalPane.getDividerLocation() / windowWidth;

            verticalPane.setDividerLocation(  (int)(c.getHeight() * oldVerticalHeightRatio));
            horizontalPane.setDividerLocation((int)(c.getWidth()  * oldHorizontalWidthRatio));
            windowWidth  = c.getWidth();
            windowHeight = c.getHeight();
        }
        @Override
        public void componentHidden(ComponentEvent arg0) {
        }
        @Override
        public void componentMoved(ComponentEvent arg0) {
        }
        @Override
        public void componentShown(ComponentEvent arg0) {
        }
    }
}
