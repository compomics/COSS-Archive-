/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package featureExtraction;


import matching.*;
import preprocessing.*;
import com.compomics.util.experiment.massspectrometry.Peak;
import com.compomics.util.experiment.massspectrometry.Spectrum;
import java.util.ArrayList;
import java.util.Collections;
/**
 *
 * @author Genet
 */
public class DivideAndTopNPeaks extends TopNPeaks{
    
    private int topN;
    private double windowMassSize = 100;

    /**
     * This constructs an object to filter out spectra based on 100Da window and
     * it selects X peaks with highest intensities for each window.
     *
     * @param expSpectrum is an experimental spectrum
     * @param topN is picked peak numbers with highest intensities
     */
    public DivideAndTopNPeaks(Spectrum expSpectrum, int topN) {
        super.expSpectrum = expSpectrum;
        this.topN = topN;
//        LOGGER = Logger.getLogger(ConfigHolder.class);
    }

    /**
     * This constructs an object with a given window size instead of a default
     * value.
     *
     * The default window size is 100Da
     *
     * @param expSpectrum is an experimental spectrum
     * @param topN is picked peak numbers with highest intensities
     * @param windowMassSize size of window, based on this a given spectrum is
     * divided into smaller parts.
     *
     */
    public DivideAndTopNPeaks(Spectrum expSpectrum, int topN, double windowMassSize) {
        super.expSpectrum = expSpectrum;
        this.topN = topN;
        this.windowMassSize = windowMassSize;
//        LOGGER = Logger.getLogger(ConfigHolder.class);
    }

    @Override
    protected void process() {
//        LOGGER.info(expSpectrum.getSpectrumTitle());
        double startMz = expSpectrum.getMinMz(),
                limitMz = startMz + windowMassSize;
        
        ArrayList<Peak> cPeaks = new ArrayList<Peak>();
        for (int index_exp = 0; index_exp < expSpectrum.getOrderedMzValues().length; index_exp++) {
            double tmpMZ = expSpectrum.getOrderedMzValues()[index_exp];
            Peak tmpPeak = expSpectrum.getPeakMap().get(tmpMZ);
            if (tmpMZ < limitMz) {
                cPeaks.add(tmpPeak);
            } else {
                Collections.sort(cPeaks, Peak.DescendingIntensityComparator);
                int tmp_num = topN;
                if (topN > cPeaks.size()) {
                    tmp_num = cPeaks.size();
                }
                for (int num = 0; num < tmp_num; num++) {
                    Peak tmpCPeakToAdd = cPeaks.get(num);
                    filteredPeaks.add(tmpCPeakToAdd);
                }
                cPeaks.clear();
                limitMz = limitMz + windowMassSize;
                index_exp = index_exp - 1;
            }
        }
        if (!cPeaks.isEmpty()) {
            Collections.sort(cPeaks, Peak.DescendingIntensityComparator);
            int tmp_num = topN;
            if (topN > cPeaks.size()) {
                tmp_num = cPeaks.size();
            }
            for (int num = 0; num < tmp_num; num++) {
                Peak tmpCPeakToAdd = cPeaks.get(num);
                filteredPeaks.add(tmpCPeakToAdd);
            }
        }
    }

    public int getTopN() {
        return topN;
    }

    public void setTopN(int topN) {
        this.topN = topN;
    }

    public double getWindowMassSize() {
        return windowMassSize;
    }

    public void setWindowMassSize(double windowMassSize) {
        this.windowMassSize = windowMassSize;
    }

}
