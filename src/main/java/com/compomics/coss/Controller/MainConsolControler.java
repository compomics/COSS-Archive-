/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.Controller;

import com.compomics.coss.Model.ConfigHolder;
import com.compomics.coss.Model.ConfigData;
import com.compomics.coss.Model.SpectralData;
import com.compomics.matching.Matching;
import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;
import org.apache.log4j.Logger;
import org.jfree.util.Log;

/**
 *
 * @author Genet
 */
public class MainConsolControler {

    /**
     * @param args the command line arguments
     */
    // static ConfigHolder config = new ConfigHolder();
    static SpectralData d;
    static Matching matching;
    static ArrayList res;
    static ConfigData cf_data = new ConfigData();
    private static final Logger LOG = Logger.getLogger(MainConsolControler.class);

    /**
     * The views of this controller.
     */
    public static void main(String[] args) {
        try {

            MainConsolControler mc = new MainConsolControler();

            //Load user inputs from properties file
            mc.loadSettings();

            //validate user input data
            List<String> valdMsg = mc.validateSettings();

            if (!valdMsg.isEmpty()) {
                StringBuilder message = new StringBuilder();
                for (String validationMessage : valdMsg) {
                    message.append(validationMessage).append(System.lineSeparator());
                }
                LOG.info("Validation errors" + message.toString());
            } else {
                //Read spectral data both target and db spectra
                ReadSpectralData r = new ReadSpectralData();
                ArrayList<MSnSpectrum> specA = r.readSpectra(cf_data.getTargetSpecFile());

                r = new ReadSpectralData();
                ArrayList<MSnSpectrum> specB = r.readSpectra(cf_data.getDBSpecFile());

                d = new SpectralData();
                d.setSpectra1(specA);
                d.setSpectra2(specB);

                matching.InpArgs(Integer.toString(cf_data.getMatchingAlgorithm()), Integer.toString(cf_data.getIntensityOption()), Double.toString(cf_data.getfragTol()));
                res = new ArrayList<>();
                res = matching.compare(d.getSpectra1(), d.getSpectra2(), LOG);

                mc.saveResult(res, cf_data.getOutputFilePath());
            }

        } catch (Exception ex) {
            LOG.info(null + " " + ex);
        }

    }

    private void loadSettings() {
        //Reading User inputs and set to config data 

        cf_data.setDBSpecFile(ConfigHolder.getInstance().getString("db.spectra.path"));
        cf_data.setTargetSpecFile(ConfigHolder.getInstance().getString("target.spectra.path"));
        cf_data.setIntensityOption(ConfigHolder.getInstance().getInt("intensity.option"));
        cf_data.setMatchingAlgorithm(ConfigHolder.getInstance().getInt("matching.algorithm"));
        cf_data.setMaxPrecursorCharg(ConfigHolder.getInstance().getInt("max.charge"));
        cf_data.setPrecTol(ConfigHolder.getInstance().getDouble("precursor.tolerance"));
        cf_data.setfragTol(ConfigHolder.getInstance().getDouble("fragment.tolerance"));
        cf_data.setOutputFilePath(ConfigHolder.getInstance().getString("result.path"));

    }

    private List<String> validateSettings() {
        List<String> validationMessages = new ArrayList<>();

        String fileExtnTar=cf_data.getTargetSpecFile().getName();
        String fileExtnDB=cf_data.getDBSpecFile().getName();
        if (!cf_data.getDBSpecFile().exists()) {
            validationMessages.add("Database spectra file not found");
        } else if (!fileExtnDB.endsWith(".mgf") || !fileExtnDB.endsWith(".msp")) {
            validationMessages.add(" Database Spectra file typenot valid");
        }
        if (!cf_data.getTargetSpecFile().exists()) {
            validationMessages.add("Target spectra file not found");
        } else if (!fileExtnTar.endsWith(".mgf") || !fileExtnTar.endsWith(".msp")) {
            validationMessages.add(" Targer Spectra file typenot valid");
        }

        if (cf_data.getPrecTol() < 0.0) {
            validationMessages.add("Please provide a positive precursor tolerance value.");
        }

        if (cf_data.getMaxPrecursorCharg() < 0.0) {
            validationMessages.add("Please provide a positive precursor charge value.");
        }

        if (cf_data.getfragTol() < 0.0) {
            validationMessages.add("Please provide a positive fragment tolerance value.");
        }

        return validationMessages;

    }

    private void saveResult(ArrayList result, String path) {

        BufferedWriter writer = null;
        File fname = new File(path + File.separator + "COSS_Result.txt");

        try {

            if (res != null && res.size() > 0) {
                Iterator itr = res.iterator();
                writer = new BufferedWriter(new FileWriter(fname));
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
