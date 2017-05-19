/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.Model;

import java.io.File;

/**
 *
 * @author Genet
 */
public class ConfigData {

    File targetSpecFile;
    File dbSpecFile;
    double precTol = 0;
    double fragTol = 0.5;
    String MsRobinOptn = "";
    int MSRobinOption; // 0-sqrt(Intensities), 1-Intensities
    int intensityOption; // 0-Summed Up 1-Multiply intensities   
    int maxPrecursorCharge;
    String matchingAlgorithm;
   

    
    
     public int geMaxPrecursorCharg() {
        return this.maxPrecursorCharge;
    }
    
    public void setMaxPrecursorCharg(int maxPrecCharge) {
        this.maxPrecursorCharge=maxPrecCharge;
    }
    
     public String getMatchingAlgorithm() {
        return this.matchingAlgorithm;
    }
    
    public void setMatchingAlgorithm(String matchAlgorithm) {
        this.matchingAlgorithm=matchAlgorithm;
    }
        
    
    public File getTargetSpecFile() {
        return this.targetSpecFile;
    }

    public File getDBSpecFile() {
        return this.dbSpecFile;
    }

    public void setTargetSpecFile(String filename){
        this.targetSpecFile= new File(filename);
        
    }
      public void setDBSpecFile(String filename){
        this.dbSpecFile= new File(filename);
        
    }
    public int getIntensityOption() {
        return this.intensityOption;
    }

    public void setIntensityOption(int sp) {
        this.intensityOption = sp;
    }

    public int getMsRobinOption() {
        return this.MSRobinOption;
    }

    public void setMsRobinOption(int sp) {
        this.MSRobinOption = sp;
    }

    public void setPrecTol(double prcTol) {
        this.precTol = prcTol;
    }

    public double getPrecTol() {
        return this.precTol;
    }

    public void setfragTol(double frTol) {
        this.fragTol = frTol;
    }

    public double getfragTol() {
        return this.fragTol;
    }
}
