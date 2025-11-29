package com.steamworkshop.downloader.controller;

import com.steamworkshop.downloader.model.AppConfig;
import com.steamworkshop.downloader.model.DownloadRequest;
import com.steamworkshop.downloader.service.ConfigService;
import com.steamworkshop.downloader.service.SteamCmdService;

import java.util.function.Consumer;

/**
 * Main controller following MVC pattern
 */
public class MainController {
    private ConfigService configService;
    private SteamCmdService steamCmdService;
    private AppConfig appConfig;
    
    public MainController(Consumer<String> logCallback,
                         Consumer<Integer> progressCallback,
                         Consumer<String> statusCallback) {
        configService = new ConfigService();
        steamCmdService = new SteamCmdService(logCallback, progressCallback, statusCallback);
        appConfig = configService.loadConfig();
    }
    
    public AppConfig getConfig() {
        return appConfig;
    }
    
    public void saveConfig() {
        configService.saveConfig(appConfig);
    }
    
    public void setSteamGuardPromptCallback(java.util.function.Function<String, String> callback) {
        steamCmdService.setPromptCallback(callback);
    }
    
    public void downloadWorkshopItems(DownloadRequest request) throws Exception {
        steamCmdService.downloadWorkshopItems(
            request.getWorkshopIds(),
            request.getAppId(),
            request.getDownloadPath(),
            request.getUsername(),
            request.getPassword(),
            request.getAuthCode(),
            request.getSteamCmdPath()
        );
    }
    
    public void updateConfig(String username, String password, boolean useAnonymous,
                           String downloadPath, String appId, String steamCmdPath) {
        appConfig.setUsername(username);
        appConfig.setPassword(password);
        appConfig.setUseAnonymous(useAnonymous);
        appConfig.setDownloadPath(downloadPath);
        appConfig.setAppId(appId);
        appConfig.setSteamCmdPath(steamCmdPath);
        saveConfig();
    }
}

