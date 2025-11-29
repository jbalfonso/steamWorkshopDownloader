package com.steamworkshop.downloader;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Consumer;

/**
 * Service class for downloading Steam Workshop items
 */
public class SteamWorkshopService {
    private static final Logger logger = LoggerFactory.getLogger(SteamWorkshopService.class);
    private static final String STEAM_WORKSHOP_API = "https://api.steampowered.com/ISteamRemoteStorage/GetPublishedFileDetails/v1/";
    private static final String STEAM_WORKSHOP_DOWNLOAD = "https://steamcommunity.com/sharedfiles/filedetails/?id=";

    private Consumer<String> logCallback;
    private Consumer<Integer> progressCallback;
    private Consumer<String> statusCallback;

    public SteamWorkshopService(Consumer<String> logCallback, 
                                Consumer<Integer> progressCallback,
                                Consumer<String> statusCallback) {
        this.logCallback = logCallback;
        this.progressCallback = progressCallback;
        this.statusCallback = statusCallback;
    }

    public void downloadWorkshopItem(String workshopId, String downloadPath) throws Exception {
        log("Starting download for Workshop ID: " + workshopId);
        updateStatus("Fetching item information...");

        // Validate workshop ID
        if (!workshopId.matches("\\d+")) {
            throw new IllegalArgumentException("Invalid Workshop ID format. Must be numeric.");
        }

        // Get item details from Steam API
        WorkshopItemInfo itemInfo = getWorkshopItemInfo(workshopId);
        log("Item found: " + itemInfo.title);
        log("File size: " + formatFileSize(itemInfo.fileSize));

        // Download the file
        updateStatus("Downloading file...");
        String filePath = downloadFile(itemInfo.downloadUrl, downloadPath, itemInfo.filename, itemInfo.fileSize);

        log("Download completed: " + filePath);
        updateStatus("Download completed!");
        updateProgress(100);
    }

    private WorkshopItemInfo getWorkshopItemInfo(String workshopId) throws Exception {
        log("Fetching item details from Steam API...");

        // Build API request with POST form data
        URL url = new URL(STEAM_WORKSHOP_API);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);

        // Write POST data
        String postData = "itemcount=1&publishedfileids[0]=" + workshopId;
        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = postData.getBytes("UTF-8");
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Failed to fetch item info. HTTP code: " + responseCode);
        }

        // Parse JSON response
        StringBuilder response = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        }

        JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
        JsonObject responseObj = json.getAsJsonObject("response");
        
        if (!responseObj.has("publishedfiledetails") || 
            responseObj.getAsJsonArray("publishedfiledetails").size() == 0) {
            throw new Exception("Workshop item not found or not accessible");
        }

        JsonObject itemDetails = responseObj.getAsJsonArray("publishedfiledetails")
                .get(0).getAsJsonObject();

        String title = itemDetails.has("title") ? 
            itemDetails.get("title").getAsString() : "Unknown";
        long fileSize = itemDetails.has("file_size") ? 
            itemDetails.get("file_size").getAsLong() : 0;
        String downloadUrl = itemDetails.has("file_url") ? 
            itemDetails.get("file_url").getAsString() : null;

        if (downloadUrl == null || downloadUrl.isEmpty()) {
            // If no direct download URL, try to extract it from the Steam page
            log("Warning: No direct download URL found. Attempting to extract from Steam page...");
            downloadUrl = extractDownloadUrlFromPage(workshopId);
            if (downloadUrl == null || downloadUrl.isEmpty()) {
                String errorMsg = String.format(
                    "No se pudo obtener la URL de descarga para el Workshop ID: %s%n%n" +
                    "SOLUCIONES ALTERNATIVAS:%n" +
                    "1. Usar SteamCMD (herramienta oficial de Steam):%n" +
                    "   steamcmd +login anonymous +workshop_download_item [APP_ID] %s +quit%n%n" +
                    "2. Usar servicios públicos de descarga:%n" +
                    "   - https://steamworkshopdownloader.io/%n" +
                    "   - https://steamworkshop.download/%n%n" +
                    "3. El archivo puede requerir autenticación de Steam o no estar disponible para descarga pública.",
                    workshopId, workshopId);
                throw new Exception(errorMsg);
            }
        }

        String filename = sanitizeFilename(title) + ".zip";
        if (filename.length() > 200) {
            filename = workshopId + ".zip";
        }

        return new WorkshopItemInfo(title, downloadUrl, filename, fileSize);
    }

    private String extractDownloadUrlFromPage(String workshopId) throws Exception {
        String pageUrl = STEAM_WORKSHOP_DOWNLOAD + workshopId;
        log("Fetching Steam page to extract download URL...");
        
        URL url = new URL(pageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        connection.setInstanceFollowRedirects(true);
        
        int responseCode = connection.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            log("Failed to fetch Steam page. HTTP code: " + responseCode);
            return null;
        }
        
        // Read the HTML page
        StringBuilder html = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                html.append(line).append("\n");
            }
        }
        
        String htmlContent = html.toString();
        
        // First, try to extract from JSON data embedded in the page
        // Look for g_rgPublishedFileData or similar JSON structures
        String[] jsonPatterns = {
            "g_rgPublishedFileData\\s*=\\s*\\{.*?\"file_url\"\\s*:\\s*\"([^\"]+)\"",
            "\"PublishedFileDetails\".*?\"file_url\"\\s*:\\s*\"([^\"]+)\"",
            "\"file_url\"\\s*:\\s*\"([^\"]+)\"",
            "file_url\":\"([^\"]+)\""
        };
        
        for (String pattern : jsonPatterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern, java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher m = p.matcher(htmlContent);
            while (m.find()) {
                String foundUrl = m.group(1).replace("\\/", "/").replace("&amp;", "&");
                // Filter out image URLs
                if (!foundUrl.contains("imw=") && !foundUrl.contains("ima=") && 
                    !foundUrl.contains("impolicy=") && !foundUrl.contains("imcolor=")) {
                    log("Found download URL in JSON: " + foundUrl);
                    return foundUrl;
                }
            }
        }
        
        // Try to find download button or link
        String[] linkPatterns = {
            "href=\"(https://[^\"]*steamusercontent[^\"]*)\"",
            "data-download-url=\"([^\"]+)\"",
            "downloadUrl\":\"([^\"]+)\""
        };
        
        for (String pattern : linkPatterns) {
            java.util.regex.Pattern p = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher m = p.matcher(htmlContent);
            while (m.find()) {
                String foundUrl = m.group(1).replace("\\/", "/").replace("&amp;", "&");
                // Filter out image URLs and preview URLs
                if (!foundUrl.contains("imw=") && !foundUrl.contains("ima=") && 
                    !foundUrl.contains("impolicy=") && !foundUrl.contains("imcolor=") &&
                    !foundUrl.contains("/ugc/") && // UGC images
                    (foundUrl.contains(".zip") || foundUrl.contains("download") || 
                     foundUrl.contains("file") || !foundUrl.contains("images."))) {
                    log("Found download URL in links: " + foundUrl);
                    return foundUrl;
                }
            }
        }
        
        log("Could not extract valid download URL from Steam page. " +
            "The file may require Steam authentication or may not be available for public download.");
        return null;
    }

    private String downloadFile(String fileUrl, String downloadPath, String filename, long expectedSize) 
            throws Exception {
        log("Connecting to download server...");

        URL url = new URL(fileUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(60000); // Increased timeout for large files
        connection.setInstanceFollowRedirects(true);

        // Follow redirects (up to 5 redirects)
        int redirectCount = 0;
        int responseCode = connection.getResponseCode();
        while ((responseCode == HttpURLConnection.HTTP_MOVED_TEMP || 
                responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                responseCode == HttpURLConnection.HTTP_SEE_OTHER) && redirectCount < 5) {
            String redirectUrl = connection.getHeaderField("Location");
            log("Following redirect " + (redirectCount + 1) + " to: " + redirectUrl);
            url = new URL(redirectUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(60000);
            connection.setInstanceFollowRedirects(true);
            responseCode = connection.getResponseCode();
            redirectCount++;
        }

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Failed to download file. HTTP code: " + responseCode);
        }

        // Check content type to ensure we're downloading a file, not HTML
        String contentType = connection.getContentType();
        if (contentType != null && contentType.contains("text/html")) {
            log("Warning: Server returned HTML instead of file. This usually means the file requires Steam authentication.");
            throw new Exception("No se pudo descargar el archivo. " +
                "El archivo puede requerir autenticación de Steam o no estar disponible para descarga pública. " +
                "Intenta usar SteamCMD o herramientas alternativas.");
        }

        // Get file size from header if available
        long fileSize = expectedSize;
        String contentLength = connection.getHeaderField("Content-Length");
        if (contentLength != null && !contentLength.isEmpty()) {
            try {
                fileSize = Long.parseLong(contentLength);
            } catch (NumberFormatException e) {
                // Use expected size if header is invalid
            }
        }

        Path outputPath = Paths.get(downloadPath, filename);
        log("Saving to: " + outputPath.toString());

        // Download with progress tracking
        try (InputStream inputStream = connection.getInputStream();
             FileOutputStream outputStream = new FileOutputStream(outputPath.toFile())) {

            byte[] buffer = new byte[8192];
            long totalBytesRead = 0;
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
                totalBytesRead += bytesRead;

                if (fileSize > 0) {
                    int progress = (int) ((totalBytesRead * 100) / fileSize);
                    updateProgress(progress);
                    // Only log every 5% to reduce spam
                    if (progress % 5 == 0 || bytesRead == -1) {
                        log(String.format("Downloaded: %s / %s (%.1f%%)",
                            formatFileSize(totalBytesRead),
                            formatFileSize(fileSize),
                            (totalBytesRead * 100.0 / fileSize)));
                    }
                } else {
                    // Log every 100KB if file size is unknown
                    if (totalBytesRead % (100 * 1024) < buffer.length) {
                        log("Downloaded: " + formatFileSize(totalBytesRead));
                    }
                }
            }
            
            outputStream.flush();

            // Verify file was downloaded completely
            long actualFileSize = outputPath.toFile().length();
            if (fileSize > 0 && actualFileSize < fileSize * 0.9) {
                throw new Exception(String.format(
                    "Descarga incompleta. Esperado: %s, Descargado: %s",
                    formatFileSize(fileSize), formatFileSize(actualFileSize)));
            }

            log("File saved successfully. Size: " + formatFileSize(actualFileSize));
        }

        return outputPath.toString();
    }

    private String sanitizeFilename(String filename) {
        // Remove invalid characters for filenames
        return filename.replaceAll("[<>:\"/\\|?*]", "_")
                      .replaceAll("\\s+", " ")
                      .trim();
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
        return String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0));
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

    /**
     * Inner class to hold workshop item information
     */
    private static class WorkshopItemInfo {
        String title;
        String downloadUrl;
        String filename;
        long fileSize;

        WorkshopItemInfo(String title, String downloadUrl, String filename, long fileSize) {
            this.title = title;
            this.downloadUrl = downloadUrl;
            this.filename = filename;
            this.fileSize = fileSize;
        }
    }
}

