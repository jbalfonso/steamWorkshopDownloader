package com.steamworkshop.downloader.model;

import java.util.List;

/**
 * Model class for download request
 */
public class DownloadRequest {
    private List<String> workshopIds;
    private String appId;
    private String downloadPath;
    private String username;
    private String password;
    private String authCode;
    private String steamCmdPath;
    
    public DownloadRequest() {}
    
    public List<String> getWorkshopIds() { return workshopIds; }
    public void setWorkshopIds(List<String> workshopIds) { this.workshopIds = workshopIds; }
    
    public String getAppId() { return appId; }
    public void setAppId(String appId) { this.appId = appId; }
    
    public String getDownloadPath() { return downloadPath; }
    public void setDownloadPath(String downloadPath) { this.downloadPath = downloadPath; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getAuthCode() { return authCode; }
    public void setAuthCode(String authCode) { this.authCode = authCode; }
    
    public String getSteamCmdPath() { return steamCmdPath; }
    public void setSteamCmdPath(String steamCmdPath) { this.steamCmdPath = steamCmdPath; }
}

