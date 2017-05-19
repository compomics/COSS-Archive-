/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.Model;

import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import java.util.ArrayList;

/**
 *
 * @author Genet
 */
public class SpectralData {
    ArrayList<MSnSpectrum> spectra1=null;
    ArrayList<MSnSpectrum> spectra2=null;
    
    

    public SpectralData() {
        spectra1=new ArrayList<>();
        spectra2=new ArrayList<>();
    }
   
    
    public ArrayList<MSnSpectrum> getSpectra1()
    {
        return this.spectra1;
        
    }
    
    public ArrayList<MSnSpectrum> getSpectra2()
    {
        return this.spectra2;
    }
  
      public void setSpectra1(ArrayList<MSnSpectrum> sp)
    {
        this.spectra1=sp;
    }
    
    public void setSpectra2(ArrayList<MSnSpectrum> sp)
    {
        this.spectra2=sp;
    }
    
   
}
