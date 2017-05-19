/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.Controller;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.Priority;
import com.compomics.coss.View.MainFrame;
import com.compomics.coss.Model.ConfigData;
import com.compomics.coss.Model.SpectralData;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.gui.spectrum.SpectrumPanel;
import java.awt.Color;
import java.awt.Toolkit;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import matching.Matching;
import matching.UseMsRoben;
import org.jfree.util.Log;

/**
 *
 * @author Genet
 */
public class MainFrameController implements UpdateListener {

    private static final Logger LOG = Logger.getLogger(MainFrameController.class);

    /**
     * Model fields.
     */
    private SwingWorkerThread workerThread;
    DefaultListModel model;
    ArrayList res;
    SpectralData d;

    /**
     * The views of this controller.
     */
    private final MainFrame mainFrame = new MainFrame();
    private ConfigData config = new ConfigData();
    Matching matching;

    /**
     * Init the controller.
     */
    public void init() {

        matching = new UseMsRoben(this);
        // add gui appender
        LogTextAreaAppender logTextAreaAppender = new LogTextAreaAppender();
        logTextAreaAppender.setLogArea(mainFrame);

        logTextAreaAppender.setThreshold(Priority.INFO);
        logTextAreaAppender.setImmediateFlush(true);
        PatternLayout layout = new org.apache.log4j.PatternLayout();
        layout.setConversionPattern("%d{yyyy-MM-dd HH:mm:ss} - %m%n");
        logTextAreaAppender.setLayout(layout);

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);

//        mainFrame.getTxtTargetSpectra().setText("D:/specA/SpecA.mgf");
//        mainFrame.getTxtDBSpectra().setText("D:/specB/SpecB.mgf");
        
        mainFrame.getTxtFragmentTolerance().setText(Integer.toString(6));
        mainFrame.getTxtPrecursorTolerance().setText(Integer.toString(20));
        mainFrame.getTxtPrecursorCharge().setText(Integer.toString(3));
        config.setTargetSpecFile(mainFrame.getTxtTargetSpectra().getText());
        config.setDBSpecFile(mainFrame.getTxtDBSpectra().getText());
        model = new DefaultListModel();
        mainFrame.getLstTargetSpec().setModel(model);

//        FileNameExtensionFilter filter = new FileNameExtensionFilter("MGF files", ".mgf");
//        fileChooser.setFileFilter(filter);
        LOG.addAppender(logTextAreaAppender);
        LOG.setLevel((Level) Level.INFO);

        //<editor-fold defaultstate="collapsed" desc=" Control Action Listeners ">
        mainFrame.getBtnTargetSpectra().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int returnVal = fileChooser.showOpenDialog(mainFrame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    //show spectra directory path in text field

                    String filename=fileChooser.getSelectedFile().getAbsolutePath().replace('\\','/');              
                    mainFrame.getTxtTargetSpectra().setText(filename);
                    config.setTargetSpecFile(mainFrame.getTxtTargetSpectra().getText());

                }
            }
        });

        mainFrame.getBtnDBSpectra().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //in response to the button click, show open dialog

                int returnVal = fileChooser.showOpenDialog(mainFrame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                   
                    String filename=fileChooser.getSelectedFile().getAbsolutePath().replace('\\','/');         
                    
                    mainFrame.getTxtDBSpectra().setText(filename);
                    config.setDBSpecFile(mainFrame.getTxtDBSpectra().getText());
                }
            }
        });

        mainFrame.getChkRemovePrecursor().addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {

                if (mainFrame.getChkFilter().isSelected()) {
                    mainFrame.getlblCutoff().enableInputMethods(true);
                    mainFrame.getTxtCutOff().enableInputMethods(true);
                } else {
                    mainFrame.getlblCutoff().enableInputMethods(false);
                    mainFrame.getTxtCutOff().enableInputMethods(false);
                }
            }
        });

        mainFrame.getCmbAlgorithmType().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                config.setMatchingAlgorithm(mainFrame.getCmbAlgorithmType().getSelectedItem().toString());
            }
        });

        mainFrame.getTxtTargetSpectra().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filename = mainFrame.getTxtTargetSpectra().getText();
                if (filename != "") {
                    config.setTargetSpecFile(filename);

                }

            }
        });

        mainFrame.getTxtDBSpectra().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String filename = mainFrame.getTxtDBSpectra().getText();
                if (filename != "") {
                    config.setTargetSpecFile(filename);

                }
            }
        });

        mainFrame.getTxtPrecursorTolerance().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double precTol = Double.valueOf(mainFrame.getTxtPrecursorTolerance().getText());
                if (precTol > 0) {
                    config.setPrecTol(precTol);

                }
            }
        });

        mainFrame.getTxtFragmentTolerance().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double fragTol = Double.valueOf(mainFrame.getTxtFragmentTolerance().getText());
                if (fragTol > 0) {
                    config.setfragTol(fragTol);

                }
            }
        });

        mainFrame.getTxtPrecursorCharge().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int precCharg = Integer.valueOf(mainFrame.getTxtPrecursorCharge().getText());
                if (precCharg > 0) {
                    config.setMaxPrecursorCharg(precCharg);

                }
            }
        });

        mainFrame.getLstTargetSpec().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                if (!lse.getValueIsAdjusting()) {
                    JList source = (JList) lse.getSource();
                    int indx = source.getSelectedIndex();
                    displayResult(indx);

                }

            }
        });

        mainFrame.getMnuSaveResult().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Specify a file to save");
                
                int userSelection = fileChooser.showSaveDialog(mainFrame);
                

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File filename=fileChooser.getSelectedFile();
                    BufferedWriter writer=null;
                    try {

                        if (res != null && res.size() > 0) {
                            Iterator itr = res.iterator();
                            writer = new BufferedWriter(new FileWriter(filename));
                            while (itr.hasNext()) {
                                writer.write(itr.next().toString());
                                writer.newLine();

                            }
                        } else {
                            Log.info("No comparison result.");
                        }

                    } catch (IOException e) {

                    } finally {
                        try {
                            if (writer != null) {
                                writer.close();
                            }
                        } catch (IOException e) {
                        }
                    }

                }

            }
        }
        );

        mainFrame.getBtnStartSearching()
                .addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e
                    ) {
                        //check for input validation and display if one or more infalid value found

                        List<String> validationMessages = validateInput();
                        if (!validationMessages.isEmpty()) {
                            StringBuilder message = new StringBuilder();
                            for (String validationMessage : validationMessages) {
                                message.append(validationMessage).append(System.lineSeparator());
                            }
                            showMessageDialog("Validation errors", message.toString(), JOptionPane.WARNING_MESSAGE);
                        } else {
                    mainFrame.getPrgSearchProgress().setValue(0);
                    mainFrame.getlblProgress().setText(Integer.toString(0)+"%");
                    mainFrame.getSpltPanel().removeAll();
                    

                            workerThread = new SwingWorkerThread();
                            workerThread.execute();

                        }

                        try {

                        } catch (Exception ex) {
                            java.util.logging.Logger.getLogger(main.ProjectMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                        }

                    }
                }
                );

        mainFrame.addWindowListener(
                new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent we
            ) {
                System.exit(0);
            }
        }
        );

        //</editor-fold>
    }

    /**
     * Show the view of this controller.
     */
    public void showMainFrame() {

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setBounds(0, 0, screenSize.width, screenSize.height - 15);
        mainFrame.setVisible(true);
    }

    /**
     * Validate the user input and return a list of validation messages if input
     * value is not the right format.
     *
     * @return the list of validation messages
     */
    private List<String> validateInput() {
        List<String> validationMessages = new ArrayList<>();

        if (mainFrame.getTxtTargetSpectra().getText().isEmpty()) {
            validationMessages.add("Please provide a spectra input directory.");
        } else if (!mainFrame.getTxtTargetSpectra().getText().endsWith(".mgf")) {
            validationMessages.add(" Targer Spectra file typenot valid");
        }
        if (mainFrame.getTxtDBSpectra().getText().isEmpty()) {
            validationMessages.add("Please provide a comparison spectra input directory.");
        } else if (!mainFrame.getTxtDBSpectra().getText().endsWith(".mgf")) {
            validationMessages.add(" Data Base Spectra file typenot valid");
        }

        if (mainFrame.getTxtPrecursorTolerance().getText().isEmpty()) {
            validationMessages.add("Please provide a precursor tolerance value.");
        } else {
            try {
                Double tolerance = Double.valueOf(mainFrame.getTxtPrecursorTolerance().getText());
                if (tolerance < 0.0) {
                    validationMessages.add("Please provide a positive precursor tolerance value.");
                }
            } catch (NumberFormatException nfe) {
                validationMessages.add("Please provide a numeric precursor tolerance value.");
            }
        }
        if (mainFrame.getTxtPrecursorCharge().getText().isEmpty()) {
            validationMessages.add("Please provide a maximum precursor charge value in both data sets.");
        } else {
            try {
                Double maxCharge = Double.valueOf(mainFrame.getTxtPrecursorCharge().getText());
                if (maxCharge < 0.0) {
                    validationMessages.add("Please provide a maximum precursor charge value.");
                }
            } catch (NumberFormatException nfe) {
                validationMessages.add("Please provide a numeric maximum precursor charge value.");
            }
        }
        if (mainFrame.getTxtFragmentTolerance().getText().isEmpty()) {
            validationMessages.add("Please provide a fragment tolerance value.");
        } else {
            try {
                Double tolerance = Double.valueOf(mainFrame.getTxtFragmentTolerance().getText());
                if (tolerance < 0.0) {
                    validationMessages.add("Please provide a positive fragment tolerance value.");
                }
            } catch (NumberFormatException nfe) {
                validationMessages.add("Please provide a numeric fragment tolerance value.");
            }
        }

        if (mainFrame.getChkFilter().isSelected()) {
            if (mainFrame.getTxtCutOff().getText().isEmpty()) {
                validationMessages.add("Please a provide peak cutoff number when choosing the TopN intense peak selection filter.");
            } else {
                try {
                    Integer number = Integer.valueOf(mainFrame.getTxtCutOff().getText());
                    if (number < 0) {
                        validationMessages.add("Please provide a positive peak cutoff number value.");
                    }
                } catch (NumberFormatException nfe) {
                    validationMessages.add("Please provide a numeric peak cutoff number value.");
                }
            }
        }

        return validationMessages;
    }

    /**
     * Shows a message dialog.
     *
     * @param title the dialog title
     * @param message the dialog message
     * @param messageType the dialog message type
     */
    private void showMessageDialog(final String title, final String message, final int messageType) {
        //add message to JTextArea
        JTextArea textArea = new JTextArea(message);
        //put JTextArea in JScrollPane
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 200));
        scrollPane.getViewport().setOpaque(false);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);

        JOptionPane.showMessageDialog(mainFrame.getContentPane(), scrollPane, title, messageType);
    }

    private void displayResult(int targSpectrum) {

        //Graphic Result Display
        //ReadSpectralData readSpectralData = new ReadSpectralData();
        //config.setDBSpecFile("D:/specA/SpecA.mgf");
        try {
            String result = res.get(targSpectrum).toString();
            result = result.substring(1, result.length() - 1);

            String[] s = result.split(", ");
            double val = Double.parseDouble(s[6]);
            int pos = (int) val;

            mainFrame.getSpltPanel().removeAll();
            //readSpectralData.readSpectra(config.getDBSpecFile());
            SpectrumPanel specPanel = new SpectrumPanel(d.getSpectra1().get(targSpectrum).getMzValuesAsArray(), d.getSpectra1().get(targSpectrum).getIntensityValuesAsArray(), 500, "+2", config.getDBSpecFile().getName());

            specPanel.addMirroredSpectrum(d.getSpectra2().get(pos).getMzValuesAsArray(), d.getSpectra2().get(pos).getIntensityValuesAsArray(), 500, "+2", config.getDBSpecFile().getName(), false, Color.blue, Color.blue);
            mainFrame.getSpltPanel().add(specPanel);

            val = Double.parseDouble(s[7]);
            pos = (int) val;
            SpectrumPanel specPanel2 = new SpectrumPanel(d.getSpectra1().get(targSpectrum).getMzValuesAsArray(), d.getSpectra1().get(targSpectrum).getIntensityValuesAsArray(), 500, "+2", config.getDBSpecFile().getName());

            specPanel2.addMirroredSpectrum(d.getSpectra2().get(pos).getMzValuesAsArray(), d.getSpectra2().get(pos).getIntensityValuesAsArray(), 500, "+2", config.getDBSpecFile().getName(), false, Color.blue, Color.blue);
            mainFrame.getSpltPanel().add(specPanel2);

            val = Double.parseDouble(s[8]);
            pos = (int) val;
            SpectrumPanel specPanel3 = new SpectrumPanel(d.getSpectra1().get(targSpectrum).getMzValuesAsArray(), d.getSpectra1().get(targSpectrum).getIntensityValuesAsArray(), 500, "+2", config.getDBSpecFile().getName());

            specPanel3.addMirroredSpectrum(d.getSpectra2().get(pos).getMzValuesAsArray(), d.getSpectra2().get(pos).getIntensityValuesAsArray(), 500, "+2", config.getDBSpecFile().getName(), false, Color.blue, Color.blue);
            mainFrame.getSpltPanel().add(specPanel3);

            val = Double.parseDouble(s[9]);
            pos = (int) val;
            SpectrumPanel specPanel4 = new SpectrumPanel(d.getSpectra1().get(targSpectrum).getMzValuesAsArray(), d.getSpectra1().get(targSpectrum).getIntensityValuesAsArray(), 500, "+2", config.getDBSpecFile().getName());

            specPanel4.addMirroredSpectrum(d.getSpectra2().get(pos).getMzValuesAsArray(), d.getSpectra2().get(pos).getIntensityValuesAsArray(), 500, "+2", config.getDBSpecFile().getName(), false, Color.blue, Color.blue);
            mainFrame.getSpltPanel().add(specPanel4);

            val = Double.parseDouble(s[10]);
            pos = (int) val;
            SpectrumPanel specPanel5 = new SpectrumPanel(d.getSpectra1().get(targSpectrum).getMzValuesAsArray(), d.getSpectra1().get(targSpectrum).getIntensityValuesAsArray(), 500, "+2", config.getDBSpecFile().getName());

            specPanel5.addMirroredSpectrum(d.getSpectra2().get(pos).getMzValuesAsArray(), d.getSpectra2().get(pos).getIntensityValuesAsArray(), 500, "+2", config.getDBSpecFile().getName(), false, Color.blue, Color.blue);
            mainFrame.getSpltPanel().add(specPanel5);

            val = Double.parseDouble(s[11]);
            pos = (int) val;
            SpectrumPanel specPanel6 = new SpectrumPanel(d.getSpectra1().get(targSpectrum).getMzValuesAsArray(), d.getSpectra1().get(targSpectrum).getIntensityValuesAsArray(), 500, "+2", config.getDBSpecFile().getName());

            specPanel6.addMirroredSpectrum(d.getSpectra2().get(pos).getMzValuesAsArray(), d.getSpectra2().get(pos).getIntensityValuesAsArray(), 500, "+2", config.getDBSpecFile().getName(), false, Color.blue, Color.blue);
            mainFrame.getSpltPanel().add(specPanel6);

        } catch (Exception exception) {
            LOG.error(exception);
        }

        mainFrame.getSpltPanel().revalidate();
        mainFrame.getSpltPanel().repaint();
    }

    private void fillTargetSpectrumList() {

        model.clear();
        int i, size = d.getSpectra1().size();
        String name;
        int indxScan;
        int lenthofString;
        for (i = 0; i < size; i++) {
            name = d.getSpectra1().get(i).getSpectrumTitle();
            lenthofString = name.length();
            indxScan = name.indexOf("scan");
            name = name.substring(indxScan, lenthofString - 1);
            model.addElement(name);
        }
    }

    @Override
    public void update(double percent) {
        final double PERCENT = percent;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int v = (int) (100 * PERCENT);
                mainFrame.getPrgSearchProgress().setValue(v);
                mainFrame.getlblProgress().setText(Integer.toString(v) + "%");
            }
        });
    }

    private class SwingWorkerThread extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            LOG.info("starting spectrum similarity score pipeline");
            d = new SpectralData();

            mainFrame.getBtnStartSearching().setEnabled(false);
            //Read spectral data both target and db spectra
            ReadSpectralData r = new ReadSpectralData();
            ArrayList<MSnSpectrum> specA = r.readSpectra(config.getTargetSpecFile());

            r = new ReadSpectralData();
            ArrayList<MSnSpectrum> specB = r.readSpectra(config.getDBSpecFile());
            d.setSpectra1(specA);
            d.setSpectra2(specB);

            matching.InpArgs(Integer.toString(config.getMsRobinOption()), Integer.toString(config.getIntensityOption()), Double.toString(config.getfragTol()));
            res = new ArrayList<>();
            res = matching.compare(specA, specB);

            return null;
        }

        @Override
        protected void done() {
            try {

                Thread.sleep(1);
                LOG.info("finished spectrum similarity");
                mainFrame.getPrgSearchProgress().setValue(100);
                mainFrame.getlblProgress().setText("100%");
                //Displaying Result Log
                if (res != null && res.size() > 0) {
                    fillTargetSpectrumList();
                    displayResult(0);
                    Iterator itr = res.iterator();
                    while (itr.hasNext()) {
                        LOG.info("Comparison Result: " + itr.next().toString());

                        //System.out.println("Comparison Result: " + itr.next().toString());
                    }
                } else {
                    Log.info("No comparison result.");
                }

                //Graphical Result for selected target spectrum
                mainFrame.getBtnStartSearching().setEnabled(true);
                get();

            } catch (InterruptedException | ExecutionException ex) {
                LOG.error(ex.getMessage(), ex);
                showMessageDialog("Unexpected error", ex.getMessage(), JOptionPane.ERROR_MESSAGE);
            } catch (CancellationException ex) {
                LOG.info("the spectrum similarity score pipeline run was cancelled");
            } finally {

            }
        }

    }

}
