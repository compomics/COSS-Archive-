/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.Controller;

import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import com.compomics.util.experiment.massspectrometry.SpectrumFactory;
import com.compomics.util.gui.waiting.waitinghandlers.WaitingHandlerCLIImpl;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author Genet
 */
public class ReadSpectralData {

    static SpectrumFactory fct = SpectrumFactory.getInstance();
    ArrayList<MSnSpectrum> spectra;

    public ReadSpectralData() {

        this.spectra = new ArrayList<>();

    }

    public ArrayList<MSnSpectrum> readSpectra(File file) throws Exception {

        fct.clearFactory();
        
        fct.addSpectra(file, new WaitingHandlerCLIImpl());
        
        this.spectra.clear();
        for (String title : fct.getSpectrumTitles(file.getName())) {
            MSnSpectrum ms = (MSnSpectrum) fct.getSpectrum(file.getName(), title);

            if (!ms.getPeakList().isEmpty()) {
                spectra.add(ms);

            }

        }

        fct.clearFactory();
        return spectra;

    }

}
