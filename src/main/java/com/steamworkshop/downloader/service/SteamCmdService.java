package com.steamworkshop.downloader.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

/**
 * Service class for downloading Steam Workshop items using SteamCMD
 */
public class SteamCmdService {
    private static final Logger logger = LoggerFactory.getLogger(SteamCmdService.class);
    
    private Consumer<String> logCallback;
    private Consumer<Integer> progressCallback;
    private Consumer<String> statusCallback;
    private java.util.function.Function<String, String> promptCallback; // For showing dialogs

    public SteamCmdService(Consumer<String> logCallback, 
                          Consumer<Integer> progressCallback,
                          Consumer<String> statusCallback) {
        this.logCallback = logCallback;
        this.progressCallback = progressCallback;
        this.statusCallback = statusCallback;
    }
    
    public void setPromptCallback(java.util.function.Function<String, String> promptCallback) {
        this.promptCallback = promptCallback;
    }

    public void downloadWorkshopItems(List<String> workshopIds, 
                                     String appId, 
                                     String downloadPath,
                                     String username,
                                     String password,
                                     String authCode,
                                     String steamCmdPath) throws Exception {
        log("Starting download for " + workshopIds.size() + " Workshop item(s)");
        updateStatus("Preparing download...");
        
        // Find SteamCMD
        String steamCmd = findSteamCmd(steamCmdPath);
        if (steamCmd == null) {
            throw new Exception("SteamCMD not found. Please install SteamCMD and specify its path in Settings tab.");
        }
        
        log("Using SteamCMD: " + steamCmd);
        
        // Create download directory if it doesn't exist
        Path downloadDir = Paths.get(downloadPath);
        if (!Files.exists(downloadDir)) {
            Files.createDirectories(downloadDir);
            log("Created download directory: " + downloadDir);
        }
        
        // Build SteamCMD command
        for (int i = 0; i < workshopIds.size(); i++) {
            String workshopId = workshopIds.get(i);
            log(String.format("Downloading item %d/%d: Workshop ID %s", 
                i + 1, workshopIds.size(), workshopId));
            updateStatus(String.format("Downloading %d/%d: %s", i + 1, workshopIds.size(), workshopId));
            
            downloadSingleItem(steamCmd, workshopId, appId, downloadPath, username, password, authCode);
            
            int progress = ((i + 1) * 100) / workshopIds.size();
            updateProgress(progress);
        }
        
        log("All downloads completed!");
        updateStatus("All downloads completed!");
        updateProgress(100);
    }
    
    private void downloadSingleItem(String steamCmd, 
                                   String workshopId, 
                                   String appId,
                                   String downloadPath,
                                   String username,
                                   String password,
                                   String authCode) throws Exception {
        // Build SteamCMD command
        StringBuilder command = new StringBuilder();
        command.append("\"").append(steamCmd).append("\"");
        command.append(" +force_install_dir \"").append(downloadPath).append("\"");
        
        // Login
        if (username == null || username.isEmpty()) {
            command.append(" +login anonymous");
        } else {
            command.append(" +login \"").append(username).append("\"");
            if (password != null && !password.isEmpty()) {
                command.append(" \"").append(password).append("\"");
                if (authCode != null && !authCode.isEmpty()) {
                    command.append(" \"").append(authCode).append("\"");
                }
            }
        }
        
        // Download command
        command.append(" +workshop_download_item ").append(appId).append(" ").append(workshopId);
        command.append(" +quit");
        
        log("Executing: " + maskPassword(command.toString()));
        
        // Execute SteamCMD interactively
        ProcessBuilder processBuilder = new ProcessBuilder();
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            processBuilder.command("cmd.exe", "/c", command.toString());
        } else {
            processBuilder.command("sh", "-c", command.toString());
        }
        
        processBuilder.directory(new File(downloadPath));
        processBuilder.redirectErrorStream(true);
        
        Process process = processBuilder.start();
        
        // Get input and output streams
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(process.getOutputStream()), true);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        
        // Read output and handle prompts
        String line;
        StringBuilder outputBuffer = new StringBuilder();
        
        // Read all output line by line
        while ((line = reader.readLine()) != null) {
            log(line);
            outputBuffer.append(line).append("\n");
            
            String lowerLine = line.toLowerCase();
            
            // Check for Steam Guard code prompt
            if ((lowerLine.contains("steam guard") || 
                 lowerLine.contains("two-factor") || 
                 lowerLine.contains("verification code") ||
                 lowerLine.contains("enter code") ||
                 lowerLine.contains("two-factor code")) &&
                (lowerLine.contains(":") || lowerLine.contains("?") || lowerLine.contains("enter"))) {
                
                log("⚠ Steam Guard code required!");
                updateStatus("Steam Guard code required - showing dialog...");
                
                String code = promptForSteamGuardCode();
                if (code != null && !code.isEmpty()) {
                    log("Sending Steam Guard code...");
                    writer.println(code);
                    writer.flush();
                } else {
                    log("No code provided, sending empty line");
                    writer.println();
                    writer.flush();
                }
            }
            // Check for password prompt (if not already provided)
            else if ((lowerLine.contains("password:") || lowerLine.contains("password for")) && 
                     password != null && !password.isEmpty()) {
                log("Password prompt detected, sending password...");
                writer.println(password);
                writer.flush();
            }
            // Check for errors
            else if (line.contains("ERROR") || line.contains("Failed") || line.contains("FAILED")) {
                log("❌ Error detected: " + line);
            }
            // Check for success
            else if (line.contains("Success") || line.contains("Downloaded") || 
                     line.contains("SUCCESS") || line.contains("OK") ||
                     line.contains("Downloading")) {
                log("✅ " + line);
            }
        }
        
        writer.close();
        reader.close();
        
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new Exception("SteamCMD exited with code: " + exitCode + "\nOutput: " + outputBuffer.toString());
        }
        
        // Reorganize downloaded files: move from steamapps/workshop/content/APP_ID/WORKSHOP_ID to just WORKSHOP_ID
        reorganizeDownloadedFiles(downloadPath, appId, workshopId);
        
        log("Workshop item " + workshopId + " downloaded successfully");
    }
    
    private void reorganizeDownloadedFiles(String downloadPath, String appId, String workshopId) {
        try {
            // SteamCMD downloads to: downloadPath/steamapps/workshop/content/APP_ID/WORKSHOP_ID
            Path steamPath = Paths.get(downloadPath, "steamapps", "workshop", "content", appId, workshopId);
            Path targetPath = Paths.get(downloadPath, workshopId);
            
            if (Files.exists(steamPath) && Files.isDirectory(steamPath)) {
                log("Reorganizing files from " + steamPath + " to " + targetPath);
                
                // If target already exists, delete it first
                if (Files.exists(targetPath)) {
                    deleteDirectory(targetPath.toFile());
                }
                
                // Move the directory
                Files.move(steamPath, targetPath);
                log("Files moved successfully to: " + targetPath);
                
                // Clean up empty steamapps directory structure
                cleanupEmptyDirectories(Paths.get(downloadPath, "steamapps"));
            } else {
                log("Warning: Expected download directory not found: " + steamPath);
            }
        } catch (Exception e) {
            log("Warning: Could not reorganize files: " + e.getMessage());
            // Don't throw - download was successful, just organization failed
        }
    }
    
    private void cleanupEmptyDirectories(Path dir) {
        try {
            if (Files.exists(dir)) {
                deleteIfEmpty(dir);
            }
        } catch (Exception e) {
            // Ignore cleanup errors
        }
    }
    
    private void deleteIfEmpty(Path dir) throws IOException {
        if (Files.isDirectory(dir)) {
            File[] files = dir.toFile().listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteIfEmpty(file.toPath());
                    }
                }
            }
            // Try to delete if empty
            try {
                Files.deleteIfExists(dir);
            } catch (Exception e) {
                // Directory not empty or other error, ignore
            }
        }
    }
    
    private void deleteDirectory(File directory) {
        if (directory.exists()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
            directory.delete();
        }
    }
    
    private String findSteamCmd(String customPath) {
        // If custom path is provided, use it
        if (customPath != null && !customPath.trim().isEmpty()) {
            File customFile = new File(customPath);
            if (customFile.exists() && customFile.isFile()) {
                return customPath;
            }
        }
        
        // Try common locations
        String[] commonPaths = {
            "steamcmd.exe",
            "steamcmd",
            System.getProperty("user.home") + File.separator + "steamcmd" + File.separator + "steamcmd.exe",
            "C:\\Program Files\\SteamCMD\\steamcmd.exe",
            "C:\\Program Files (x86)\\SteamCMD\\steamcmd.exe",
            System.getProperty("user.home") + File.separator + "Desktop\\steamcmd\\steamcmd.exe",
            System.getProperty("user.home") + File.separator + "Downloads\\steamcmd\\steamcmd.exe",
            System.getProperty("user.home") + File.separator + "Downloads\\Steamcmd\\steamcmd.exe",
            System.getProperty("user.home") + File.separator + "Downloads\\SteamCmd\\steamcmd.exe",
            System.getProperty("user.home") + File.separator + "Downloads\\SteamCMD\\steamcmd.exe"
        };
        
        for (String path : commonPaths) {
            File file = new File(path);
            if (file.exists() && file.isFile()) {
                log("Found SteamCMD at: " + path);
                return path;
            }
        }
        
        // Try PATH
        try {
            ProcessBuilder pb = new ProcessBuilder("steamcmd.exe", "+quit");
            Process p = pb.start();
            p.waitFor();
            return "steamcmd.exe";
        } catch (Exception e) {
            // Not in PATH
        }
        
        return null;
    }
    
    private String maskPassword(String command) {
        // Mask password in log output
        return command.replaceAll("\\+login \"[^\"]+\" \"[^\"]+\"", "+login \"***\" \"***\"");
    }
    
    private void log(String message) {
        logger.info(message);
        if (logCallback != null) {
            logCallback.accept(message);
        }
    }
    
    private void updateProgress(int progress) {
        if (progressCallback != null) {
            progressCallback.accept(progress);
        }
    }
    
    private void updateStatus(String status) {
        if (statusCallback != null) {
            statusCallback.accept(status);
        }
    }
    
    private String promptForSteamGuardCode() {
        if (promptCallback != null) {
            return promptCallback.apply("Steam Guard Code Required");
        }
        // Fallback: return null (will send empty line)
        return null;
    }
}

