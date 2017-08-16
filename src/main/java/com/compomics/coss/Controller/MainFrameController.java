/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.Controller;

import com.compomics.coss.Model.ConfigData;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import com.compomics.coss.View.SettingsView;
import com.compomics.coss.View.ProgressView;
import com.compomics.coss.View.ResultView;
import com.compomics.coss.Model.ConfigHolder;
import com.compomics.coss.Model.SpectralData;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.gui.spectrum.SpectrumPanel;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import com.compomics.matching.Matching;
import com.compomics.matching.UseMsRoben;
import javax.swing.table.DefaultTableModel;
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
 
    DefaultTableModel tblModelResult;
    DefaultTableModel tblModelTarget;
    ArrayList res;
    SpectralData d;
    ConfigData cf_data = new ConfigData();

    /**
     * The views of this controller.
     */
    private final SettingsView settingsView = new SettingsView();
    private final ProgressView progressView = new ProgressView();
    private final ResultView resultView = new ResultView();

    // private ConfigHolder config = new ConfigHolder();
    Matching matching;

    private int targSpectrumIndex, bestResultIndex;

    /**
     * Init the controller.
     */
    public void init() {

        matching = new UseMsRoben(this);
        // add gui appender
        LogTextAreaAppender logTextAreaAppender = new LogTextAreaAppender();
        logTextAreaAppender.setLogArea(progressView);

        logTextAreaAppender.setThreshold(Priority.INFO);
        logTextAreaAppender.setImmediateFlush(true);
        PatternLayout layout = new org.apache.log4j.PatternLayout();
        layout.setConversionPattern("%d{yyyy-MM-dd HH:mm:ss} - %m%n");
        logTextAreaAppender.setLayout(layout);

        JFileChooser fileChooser = new JFileChooser("D:/");
        fileChooser.setMultiSelectionEnabled(false);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false); 
        
        

        settingsView.getTxtTargetSpectra().setText("D:/specA/SpecA.msp");
        settingsView.getTxtDBSpectra().setText("D:/specB/SpecB.msp");

        settingsView.getTxtFragmentTolerance().setText(Integer.toString(6));
        settingsView.getTxtPrecursorTolerance().setText(Integer.toString(20));
        settingsView.getTxtPrecursorCharge().setText(Integer.toString(3));
        cf_data.setTargetSpecFile(settingsView.getTxtTargetSpectra().getText());
        cf_data.setDBSpecFile(settingsView.getTxtDBSpectra().getText());
     
        
        
        final String[] colNamesRes = {"No.", "ID","Name/Title", "M/Z","Charge", "No. Peaks","Score", "Confidence(%)"};
        final String[] colNamesTar = {"No.", "ID","Name/Title", "M/Z","Charge", "No. Peaks"};
        tblModelResult=new DefaultTableModel(colNamesRes, 0);
        tblModelTarget=new DefaultTableModel(colNamesTar, 0);
        
        resultView.getTblTargetSpec().setModel(tblModelTarget);
        resultView.getTblBestMatch().setModel(tblModelResult);
        
        resultView.getTblTargetSpec().setRowSelectionAllowed(true);
        resultView.getTblTargetSpec().setColumnSelectionAllowed(false);
        
        resultView.getTblBestMatch().setRowSelectionAllowed(true);
        resultView.getTblBestMatch().setColumnSelectionAllowed(false);
        


        LOG.addAppender(logTextAreaAppender);
        LOG.setLevel((Level) Level.INFO);


        resultView.getTblTargetSpec().getSelectionModel().addListSelectionListener(new ListSelectionListener()  {
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                if (!lse.getValueIsAdjusting()) {                   
                    targSpectrumIndex = resultView.getTblTargetSpec().getSelectedRow();
                    fillResultListControl(targSpectrumIndex);
                    mapIndex(0);

                    displayResult();

                }

            }
        });
        
        
        
        resultView.getTblBestMatch().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
           
            @Override
            public void valueChanged(ListSelectionEvent lse) {
                int index = resultView.getTblBestMatch().getSelectedRow();
                mapIndex(index);
                displayResult();
            }
        });



        settingsView.getBtnSave().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                List<String> validationMessages = validateInput();
                if (!validationMessages.isEmpty()) {
                    StringBuilder message = new StringBuilder();
                    for (String validationMessage : validationMessages) {
                        message.append(validationMessage).append(System.lineSeparator());
                    }
                    showMessageDialog("Validation errors", message.toString(), JOptionPane.WARNING_MESSAGE);
                } else {

                    ConfigHolder.getInstance().setProperty("matching.algorithm", settingsView.getCmbAlgorithmType().getSelectedIndex());
                    ConfigHolder.getInstance().setProperty("fragment.tolerance",  settingsView.getTxtFragmentTolerance().getText());
                    ConfigHolder.getInstance().setProperty("precursor.tolerance", settingsView.getTxtPrecursorTolerance().getText());
                    ConfigHolder.getInstance().setProperty("max.charge", settingsView.getTxtPrecursorCharge().getText());
                    ConfigHolder.getInstance().setProperty("db.spectra.path", settingsView.getTxtDBSpectra().getText());
                    ConfigHolder.getInstance().setProperty("target.spectra", settingsView.getTxtTargetSpectra().getText());
                   
                }

            }
        });
        resultView.getMnuSaveResult().addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {

                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Specify a file to save");

                int userSelection = fileChooser.showSaveDialog(resultView);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                        File filename = fileChooser.getSelectedFile();
                        BufferedWriter writer = null;
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

        settingsView.getBtnStartSearching()
                .addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        //check for input validation and display if one or more infalid value found

                        settingsView.dispose();
                        progressView.setVisible(true);
                        List<String> validationMessages = validateInput();
                        if (!validationMessages.isEmpty()) {
                            StringBuilder message = new StringBuilder();
                            for (String validationMessage : validationMessages) {
                                message.append(validationMessage).append(System.lineSeparator());
                            }
                            showMessageDialog("Validation errors", message.toString(), JOptionPane.WARNING_MESSAGE);
                        } else {
                            LoadData();
                            progressView.getPrgSearchProgress().setValue(0);
                            progressView.getlblProgress().setText(Integer.toString(0) + "%");
                            resultView.getSpltPanel().removeAll();

                            workerThread = new SwingWorkerThread();
                            workerThread.execute();

                        }

                        try {

                        } catch (Exception ex) {
                            java.util.logging.Logger.getLogger(com.compomics.main.ProjectMain.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
                        }

                    }
                }
                );

        settingsView.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent we
            ) {
                //System.exit(0);

                if (JOptionPane.showConfirmDialog(settingsView,
                        "Are you sure to close this window?", "Really Closing?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        }
        );

        settingsView.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent we
            ) {
                //System.exit(0);

                if (JOptionPane.showConfirmDialog(settingsView,
                        "Are you sure to close this window?", "Really Closing?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        }
        );

        resultView.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(final WindowEvent we
            ) {
                //System.exit(0);

                if (JOptionPane.showConfirmDialog(resultView,
                        "Are you sure to close this window?", "Really Closing?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
                    System.exit(0);
                }
            }
        }
        );

        //</editor-fold>
    }

    /**
     * Show the view of this controller.
     */
    public void showMainFrame() {

        //Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        // mainFrame.setBounds(0, 0, screenSize.width, screenSize.height - 15);
        settingsView.setVisible(true);
    }

    
      /**
     * Read user input from GUI and copy to configdata object
     *
     */
    
    private void LoadData()
    {
        cf_data.setDBSpecFile(settingsView.getTxtDBSpectra().getText());
        cf_data.setTargetSpecFile(settingsView.getTxtTargetSpectra().getText());
        cf_data.setMatchingAlgorithm(settingsView.getCmbAlgorithmType().getSelectedIndex());
        cf_data.setMaxPrecursorCharg(Integer.parseInt(settingsView.getTxtPrecursorCharge().getText()));
        cf_data.setPrecTol(Double.parseDouble(settingsView.getTxtPrecursorTolerance().getText()));
        cf_data.setfragTol(Double.parseDouble(settingsView.getTxtFragmentTolerance().getText()));
        
        
    }
    
    
    
    
    /**
     * Validate the user input and return a list of validation messages if input
     * value is not the right format.
     *
     * @return the list of validation messages
     */
    private List<String> validateInput() {
        List<String> validationMessages = new ArrayList<>();
        String fileExtnDB=settingsView.getTxtDBSpectra().getText();
        String fileExtnTar=settingsView.getTxtDBSpectra().getText();

        if (settingsView.getTxtTargetSpectra().getText().isEmpty()) {
            validationMessages.add("Please provide a spectra input directory.");
        } else if (!fileExtnTar.endsWith(".mgf") && !fileExtnTar.endsWith(".msp")) {
            validationMessages.add(" Targer Spectra file typenot valid");
        }
        if (settingsView.getTxtDBSpectra().getText().isEmpty()) {
            validationMessages.add("Please provide a comparison spectra input directory.");
        } else if (!fileExtnDB.endsWith(".mgf") && !fileExtnDB.endsWith(".msp")) {
            validationMessages.add(" Data Base Spectra file typenot valid");
        }

        if (settingsView.getTxtPrecursorTolerance().getText().isEmpty()) {
            validationMessages.add("Please provide a precursor tolerance value.");
        } else {
            try {
                Double tolerance = Double.valueOf(settingsView.getTxtPrecursorTolerance().getText());
                if (tolerance < 0.0) {
                    validationMessages.add("Please provide a positive precursor tolerance value.");
                }
            } catch (NumberFormatException nfe) {
                validationMessages.add("Please provide a numeric precursor tolerance value.");
            }
        }
        if (settingsView.getTxtPrecursorCharge().getText().isEmpty()) {
            validationMessages.add("Please provide a maximum precursor charge value in both data sets.");
        } else {
            try {
                Double maxCharge = Double.valueOf(settingsView.getTxtPrecursorCharge().getText());
                if (maxCharge < 0.0) {
                    validationMessages.add("Please provide a maximum precursor charge value.");
                }
            } catch (NumberFormatException nfe) {
                validationMessages.add("Please provide a numeric maximum precursor charge value.");
            }
        }
        if (settingsView.getTxtFragmentTolerance().getText().isEmpty()) {
            validationMessages.add("Please provide a fragment tolerance value.");
        } else {
            try {
                Double tolerance = Double.valueOf(settingsView.getTxtFragmentTolerance().getText());
                if (tolerance < 0.0) {
                    validationMessages.add("Please provide a positive fragment tolerance value.");
                }
            } catch (NumberFormatException nfe) {
                validationMessages.add("Please provide a numeric fragment tolerance value.");
            }
        }

        if (settingsView.getChkFilter().isSelected()) {
            if (settingsView.getTxtCutOff().getText().isEmpty()) {
                validationMessages.add("Please a provide peak cutoff number when choosing the TopN intense peak selection filter.");
            } else {
                try {
                    Integer number = Integer.valueOf(settingsView.getTxtCutOff().getText());
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

        JOptionPane.showMessageDialog(settingsView.getContentPane(), scrollPane, title, messageType);
    }

    private void mapIndex(int index) {

        double val = 0;
        String result = res.get(targSpectrumIndex).toString();
        result = result.substring(1, result.length() - 1);
        String[] s = result.split(", ");

        val = Double.parseDouble(s[6 + index]);
        bestResultIndex = (int) val;
    }

    private void displayResult() {

        try {

            this.resultView.getSpltPanel().removeAll();

            SpectrumPanel specPanel = new SpectrumPanel(d.getSpectra1().get(targSpectrumIndex).getMzValuesAsArray(), d.getSpectra1().get(targSpectrumIndex).getIntensityValuesAsArray(), 500, "+2", cf_data.getDBSpecFile().getName());

            specPanel.addMirroredSpectrum(d.getSpectra2().get(bestResultIndex).getMzValuesAsArray(), d.getSpectra2().get(bestResultIndex).getIntensityValuesAsArray(), 500, "+2", cf_data.getDBSpecFile().getName(), false, Color.blue, Color.blue);
            resultView.getSpltPanel().add(specPanel);

        } catch (Exception exception) {
            LOG.error(exception);
        }

        resultView.getSpltPanel().revalidate();
        resultView.getSpltPanel().repaint();
    }

    private void fillTargetListControl() {

        tblModelTarget.setRowCount(0);
        int i, size = d.getSpectra1().size();
        //String name;

        Object[][] rows=new Object[size][6];
        for (i = 0; i < size; i++) {
           // name = d.getSpectra1().get(i).getSpectrumTitle();

            rows[i][0]=i+1;
            rows[i][1]="ID"+ Integer.toString(i+1);
            rows[i][2]=d.getSpectra1().get(i).getSpectrumTitle();
            rows[i][3]=d.getSpectra1().get(i).getPrecursor().getMz();
            rows[i][4]=d.getSpectra1().get(i).getPrecursor().getPossibleCharges();
            rows[i][5]=d.getSpectra1().get(i).getNPeaks();   
            
            tblModelTarget.addRow(rows[i]);
        }
      

    }

    private void fillResultListControl(int target) {
        
        tblModelResult.setRowCount(0);
        int i, pos;
        double val = 0;
        String result = res.get(target).toString();
        result = result.substring(1, result.length() - 1);
        String[] s = result.split(", ");

       Object[][] rows=new Object[6][8];
        for (i = 0; i < 6; i++) {
          
            val = Double.parseDouble(s[6 + i]);
            pos = (int) val;  
            rows[i][0]=i+1;
            rows[i][1]="ID"+ Integer.toString(i);
            rows[i][2]=d.getSpectra2().get(pos).getSpectrumTitle();
            rows[i][3]=d.getSpectra2().get(pos).getPrecursor().getMz();
            rows[i][4]=d.getSpectra2().get(pos).getPrecursor().getPossibleCharges();
            rows[i][5]=d.getSpectra2().get(pos).getNPeaks();   
            rows[i][6]=s[i];
            rows[i][7]=(Double.parseDouble(s[i])/500)*100;  
           tblModelResult.addRow(rows[i]);
           
          
        }
        
         
    }

    @Override
    public void update(double percent) {
        final double PERCENT = percent;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int v = (int) (100 * PERCENT);
                progressView.getPrgSearchProgress().setValue(v);
                progressView.getlblProgress().setText(Integer.toString(v) + "%");
            }
        });
    }

    private class SwingWorkerThread extends SwingWorker<Void, Void> {

        @Override
        protected Void doInBackground() throws Exception {
            LOG.info("starting spectrum similarity score pipeline");
            d = new SpectralData();

            settingsView.getBtnStartSearching().setEnabled(false);
            //Read spectral data both target and db spectra

            ReadSpectralData r = new ReadSpectralData();
            //File targetSpectra = new File((ConfigHolder.getInstance().getString("target.spectra.path")));
            //File dbSpectra = new File((ConfigHolder.getInstance().getString("db.spectra.path")));

            ArrayList<MSnSpectrum> specA = r.readSpectra(cf_data.getTargetSpecFile());

            r = new ReadSpectralData();
            ArrayList<MSnSpectrum> specB = r.readSpectra(cf_data.getDBSpecFile());
            d.setSpectra1(specA);
            d.setSpectra2(specB);

//            Thread readerThread = new Thread(new Runnable() {
//                @Override
//                public void run() {
//
//                    while (d.getSpectra1() == null || d.getSpectra2() == null) {
//                    }
//
//                }
//            });
//
//            readerThread.start();
//            readerThread.join();
            matching.InpArgs(Integer.toString(cf_data.getMsRobinOption()), Integer.toString(cf_data.getIntensityOption()), Double.toString(cf_data.getfragTol()));
            res = new ArrayList<>();
            res = matching.compare(d.getSpectra1(), d.getSpectra2(), LOG);

            return null;
        }

        @Override
        protected void done() {
            try {
                LOG.info("Spectrum Similarity Comparison Completed");
                progressView.getPrgSearchProgress().setValue(100);
                progressView.getlblProgress().setText("100%");
                //Displaying Result Log
                if (res != null && res.size() > 0) {

                    resultView.setVisible(true);
                    fillTargetListControl();
                    fillResultListControl(0);
                    displayResult();
                    Iterator itr = res.iterator();
                    while (itr.hasNext()) {
                        LOG.info("Comparison Result: " + itr.next().toString());

                        //System.out.println("Comparison Result: " + itr.next().toString());
                    }
                } else {
                    Log.info("No comparison result.");
                }

                //Graphical Result for selected target spectrum
                settingsView.getBtnStartSearching().setEnabled(true);
            
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
