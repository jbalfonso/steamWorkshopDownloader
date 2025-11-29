package com.steamworkshop.downloader.model;

/**
 * Model class for application configuration
 */
public class AppConfig {
    private String username;
    private String password;
    private boolean useAnonymous;
    private String downloadPath;
    private String appId;
    private String steamCmdPath;
    private String lastWorkshopId;
    
    public AppConfig() {
        useAnonymous = true;
        downloadPath = System.getProperty("user.home") + System.getProperty("file.separator") + "Downloads";
    }
    
    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public boolean isUseAnonymous() { return useAnonymous; }
    public void setUseAnonymous(boolean useAnonymous) { this.useAnonymous = useAnonymous; }
    
    public String getDownloadPath() { return downloadPath; }
    public void setDownloadPath(String downloadPath) { this.downloadPath = downloadPath; }
    
    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    
    public String getSteamCmdPath() { return steamCmdPath; }
    public void setSteamCmdPath(String steamCmdPath) { this.steamCmdPath = steamCmdPath; }
    
    public String getLastWorkshopId() { return lastWorkshopId; }
    public void setLastWorkshopId(String lastWorkshopId) { this.lastWorkshopId = lastWorkshopId; }
}

