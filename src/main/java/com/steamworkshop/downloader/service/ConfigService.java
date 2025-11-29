package com.steamworkshop.downloader.service;

import com.steamworkshop.downloader.model.AppConfig;

import java.io.*;
import java.util.Properties;

/**
 * Service for managing configuration persistence
 */
public class ConfigService {
    private static final String CONFIG_FILE = "steam-workshop-downloader.properties";
    
    public AppConfig loadConfig() {
        AppConfig config = new AppConfig();
        Properties properties = new Properties();
        
        try {
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    properties.load(fis);
                }
                
                config.setUsername(properties.getProperty("username", ""));
                config.setUseAnonymous(Boolean.parseBoolean(properties.getProperty("useAnonymous", "true")));
                config.setDownloadPath(properties.getProperty("downloadPath", 
                    System.getProperty("user.home") + File.separator + "Downloads"));
                config.setAppId(properties.getProperty("appId", ""));
                config.setSteamCmdPath(properties.getProperty("steamCmdPath", ""));
                config.setLastWorkshopId(properties.getProperty("lastWorkshopId", ""));
            }
        } catch (IOException e) {
            // Use defaults
        }
        
        return config;
    }
    
    public void saveConfig(AppConfig config) {
        Properties properties = new Properties();
        
        if (config.getUsername() != null && !config.getUsername().isEmpty()) {
            properties.setProperty("username", config.getUsername());
        }
        properties.setProperty("useAnonymous", String.valueOf(config.isUseAnonymous()));
        properties.setProperty("downloadPath", config.getDownloadPath());
        if (config.getAppId() != null && !config.getAppId().isEmpty()) {
            properties.setProperty("appId", config.getAppId());
        }
        if (config.getSteamCmdPath() != null && !config.getSteamCmdPath().isEmpty()) {
            properties.setProperty("steamCmdPath", config.getSteamCmdPath());
        }
        if (config.getLastWorkshopId() != null && !config.getLastWorkshopId().isEmpty()) {
            properties.setProperty("lastWorkshopId", config.getLastWorkshopId());
        }
        
        try {
            try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
                properties.store(fos, "Steam Workshop Downloader Configuration");
            }
        } catch (IOException e) {
            System.err.println("Error saving configuration: " + e.getMessage());
        }
    }
}

