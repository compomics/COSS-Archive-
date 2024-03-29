/*
 * Copyright 2017 Genet.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.compomics.matching;

import com.compomics.util.experiment.massspectrometry.MSnSpectrum;
import java.util.ArrayList;

/**
 *
 * @author Genet
 */
public abstract class Matching {
    
    
    
   public abstract void InpArgs(java.lang.String ... args);

    public abstract ArrayList compare(ArrayList<MSnSpectrum> speca, ArrayList<MSnSpectrum> specb,  org.apache.log4j.Logger log);
    
    
    
    
}
