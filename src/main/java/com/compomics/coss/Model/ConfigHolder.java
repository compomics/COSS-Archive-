/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.compomics.coss.Model;

import java.io.IOException;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.springframework.core.io.Resource;

/**
 *
 * @author Genet
 */
public class ConfigHolder extends PropertiesConfiguration {
    
    
     /**
     * Logger instance.
     */
    private static final Logger LOGGER = Logger.getLogger(ConfigHolder.class);
    /**
     * ConfigHolder singleton instance.
     */
    private static ConfigHolder ourInstance;

    static {
        try {
            //Resource propertiesResource = ResourceUtils.getResourceByRelativePath("BookChapter.properties");
            Resource propertiesResource = ResourceUtils.getResourceByRelativePath("ms2.properties");
            ourInstance = new ConfigHolder(propertiesResource);
        } catch (IOException | ConfigurationException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    /**
     * Private constructor to prevent instantiation.
     *
     * @param propertiesResource the properties resource
     * @throws ConfigurationException in case of a configuration initialization
     * problem
     * @throws IOException in case of an I/O related problem
     */
    private ConfigHolder(Resource propertiesResource) throws ConfigurationException, IOException {
        super(propertiesResource.getURL());
    }

    /**
     * Gets the PropertiesConfiguration instance
     *
     * @return the PropertiesConfigurationHolder instance
     */
    public static ConfigHolder getInstance() {
        return ourInstance;
    }

    public static ConfigHolder getInstanceProteinDiversity() throws ConfigurationException, IOException {
        Resource propertiesResource = ResourceUtils.getResourceByRelativePath("resources/ProteinDiversity.properties");
        ourInstance = new ConfigHolder(propertiesResource);
        return ourInstance;
    }
    
    
    
    

}
