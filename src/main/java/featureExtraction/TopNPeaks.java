/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package featureExtraction;

import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import java.util.ArrayList;
import org.apache.log4j.Logger;
/**
 *
 * @author Genet
 */
public abstract class TopNPeaks {
    protected Spectrum expSpectrum;
    protected ArrayList<Peak> filteredPeaks=new ArrayList<>();
    protected Logger LOGGER;

    /**
     * Apply filtering-process
     */
    protected abstract void process() ;

    public Spectrum getExpMSnSpectrum() {
        return expSpectrum;
    }

    public void setExpMSnSpectrum(Spectrum expMSnSpectrum) {
        this.expSpectrum = expMSnSpectrum;
    }
    
    /**
     * This method returns a list of filtered-peaks from given MSnSpectrum.
     * 
     * @return an arraylist of Peaks
     */
    public ArrayList<Peak> getFilteredPeaks() {
        if (filteredPeaks.isEmpty()&& !expSpectrum.getPeakList().isEmpty())
        {
            process();
        }
        return filteredPeaks;
    }
    
    
}
