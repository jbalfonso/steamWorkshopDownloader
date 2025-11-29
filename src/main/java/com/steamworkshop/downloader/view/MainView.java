package com.steamworkshop.downloader.view;

import com.steamworkshop.downloader.controller.MainController;
import com.steamworkshop.downloader.model.AppConfig;
import com.steamworkshop.downloader.model.DownloadRequest;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Modern main view following MVC pattern
 */
public class MainView extends JFrame {
    private MainController controller;
    
    // Components
    private JTextField workshopIdField;
    private JTextArea workshopIdsArea;
    private JTextField txtFileField;
    private JButton browseTxtButton;
    private JTextField appIdField;
    private JTextField downloadPathField;
    private JButton browseButton;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JTextField authCodeField;
    private JCheckBox useAnonymousCheckbox;
    private JTextField steamCmdPathField;
    private JButton browseSteamCmdButton;
    private JButton downloadButton;
    private JTextArea logArea;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    
    private enum InputMode { SINGLE, LIST, FILE }
    private InputMode currentMode = InputMode.SINGLE;
    
    // Modern color scheme
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    private static final Color SECONDARY_COLOR = new Color(240, 240, 240);
    private static final Color ACCENT_COLOR = new Color(0, 153, 76);
    private static final Color ERROR_COLOR = new Color(204, 0, 0);
    
    public MainView(MainController controller) {
        this.controller = controller;
        initializeComponents();
        setupModernLayout();
        setupEventHandlers();
        loadConfiguration();
    }
    
    private void initializeComponents() {
        setTitle("Steam Workshop Downloader");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        
        // Apply modern look
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Button.background", PRIMARY_COLOR);
            UIManager.put("Button.foreground", Color.WHITE);
            UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 12));
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Initialize fields
        workshopIdField = createStyledTextField();
        workshopIdsArea = new JTextArea(5, 30);
        workshopIdsArea.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        workshopIdsArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        txtFileField = createStyledTextField();
        browseTxtButton = createStyledButton("Browse...");
        appIdField = createStyledTextField();
        downloadPathField = createStyledTextField();
        browseButton = createStyledButton("Browse...");
        usernameField = createStyledTextField();
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        authCodeField = createStyledTextField();
        useAnonymousCheckbox = new JCheckBox("Use anonymous login");
        useAnonymousCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        steamCmdPathField = createStyledTextField();
        browseSteamCmdButton = createStyledButton("Browse...");
        
        downloadButton = new JButton("Download");
        downloadButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        downloadButton.setBackground(ACCENT_COLOR);
        downloadButton.setForeground(Color.WHITE);
        downloadButton.setFocusPainted(false);
        downloadButton.setBorderPainted(false);
        downloadButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        logArea = new JTextArea(15, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        logArea.setBackground(new Color(30, 30, 30));
        logArea.setForeground(new Color(0, 255, 0));
        logArea.setCaretColor(Color.WHITE);
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        progressBar.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        progressBar.setBackground(SECONDARY_COLOR);
        progressBar.setForeground(ACCENT_COLOR);
        
        statusLabel = new JLabel("Ready to download");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
    }
    
    private JTextField createStyledTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        return field;
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        button.setBackground(PRIMARY_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
    
    private void setupModernLayout() {
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(15, 15, 15, 15));
        getContentPane().setBackground(Color.WHITE);
        
        // Create modern tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabbedPane.setBackground(Color.WHITE);
        
        tabbedPane.addTab("Download", createDownloadPanel());
        tabbedPane.addTab("Steam Login", createLoginPanel());
        tabbedPane.addTab("Settings", createSettingsPanel());
        
        // Log area with modern styling
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "Log Output",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 12)));
        logScroll.setBackground(Color.WHITE);
        
        // Bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        bottomPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(tabbedPane, BorderLayout.NORTH);
        add(logScroll, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createDownloadPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Input mode selection with modern radio buttons
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        JLabel modeLabel = new JLabel("Select input method:");
        modeLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        panel.add(modeLabel, gbc);
        
        ButtonGroup modeGroup = new ButtonGroup();
        JRadioButton singleRadio = new JRadioButton("Single Workshop ID", true);
        JRadioButton listRadio = new JRadioButton("List of IDs (comma-separated)");
        JRadioButton fileRadio = new JRadioButton("TXT file (one ID per line)");
        
        singleRadio.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        listRadio.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        fileRadio.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        modeGroup.add(singleRadio);
        modeGroup.add(listRadio);
        modeGroup.add(fileRadio);
        
        singleRadio.addActionListener(e -> {
            currentMode = InputMode.SINGLE;
            workshopIdField.setEnabled(true);
            workshopIdsArea.setEnabled(false);
            txtFileField.setEnabled(false);
            browseTxtButton.setEnabled(false);
        });
        
        listRadio.addActionListener(e -> {
            currentMode = InputMode.LIST;
            workshopIdField.setEnabled(false);
            workshopIdsArea.setEnabled(true);
            txtFileField.setEnabled(false);
            browseTxtButton.setEnabled(false);
        });
        
        fileRadio.addActionListener(e -> {
            currentMode = InputMode.FILE;
            workshopIdField.setEnabled(false);
            workshopIdsArea.setEnabled(false);
            txtFileField.setEnabled(true);
            browseTxtButton.setEnabled(true);
        });
        
        gbc.gridy = 1; gbc.gridwidth = 1;
        panel.add(singleRadio, gbc);
        gbc.gridx = 1;
        panel.add(listRadio, gbc);
        gbc.gridx = 2;
        panel.add(fileRadio, gbc);
        
        // Fields
        addLabeledField(panel, "Workshop ID:", workshopIdField, 2, gbc);
        addLabeledField(panel, "Workshop IDs:", new JScrollPane(workshopIdsArea), 3, gbc);
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(new JLabel("TXT File:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(txtFileField, gbc);
        gbc.gridx = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        panel.add(browseTxtButton, gbc);
        
        addLabeledField(panel, "App ID:", appIdField, 5, gbc);
        addLabeledField(panel, "Download Path:", downloadPathField, 6, gbc, browseButton);
        
        // Download button
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(downloadButton);
        panel.add(buttonPanel, gbc);
        
        workshopIdsArea.setEnabled(false);
        txtFileField.setEnabled(false);
        browseTxtButton.setEnabled(false);
        
        return panel;
    }
    
    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(useAnonymousCheckbox, gbc);
        useAnonymousCheckbox.addActionListener(e -> {
            boolean enabled = !useAnonymousCheckbox.isSelected();
            usernameField.setEnabled(enabled);
            passwordField.setEnabled(enabled);
            authCodeField.setEnabled(enabled);
        });
        
        addLabeledField(panel, "Username:", usernameField, 1, gbc);
        addLabeledField(panel, "Password:", passwordField, 2, gbc);
        addLabeledField(panel, "Steam Guard Code:", authCodeField, 3, gbc);
        
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);
        authCodeField.setEnabled(false);
        
        return panel;
    }
    
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;
        
        addLabeledField(panel, "SteamCMD Path:", steamCmdPathField, 0, gbc, browseSteamCmdButton);
        
        return panel;
    }
    
    private void addLabeledField(JPanel panel, String label, JComponent field, int row, GridBagConstraints gbc) {
        addLabeledField(panel, label, field, row, gbc, null);
    }
    
    private void addLabeledField(JPanel panel, String label, JComponent field, int row, GridBagConstraints gbc, JButton button) {
        gbc.gridx = 0; gbc.gridy = row;
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        panel.add(jLabel, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(field, gbc);
        if (button != null) {
            gbc.gridx = 2;
            gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
            panel.add(button, gbc);
        }
    }
    
    private void setupEventHandlers() {
        browseButton.addActionListener(e -> browseDirectory(downloadPathField));
        browseTxtButton.addActionListener(e -> browseFile(txtFileField, "Text Files", "txt"));
        browseSteamCmdButton.addActionListener(e -> browseFile(steamCmdPathField, "Executable Files", "exe"));
        
        downloadButton.addActionListener(e -> handleDownload());
    }
    
    private void browseDirectory(JTextField field) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setCurrentDirectory(new File(field.getText()));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            field.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void browseFile(JTextField field, String description, String extension) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(description, extension));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            field.setText(chooser.getSelectedFile().getAbsolutePath());
        }
    }
    
    private void handleDownload() {
        List<String> workshopIds = getWorkshopIds();
        if (workshopIds.isEmpty()) {
            showError("Please enter at least one Workshop ID");
            return;
        }
        
        String appId = appIdField.getText().trim();
        if (appId.isEmpty() || !appId.matches("\\d+")) {
            showError("Please enter a valid App ID (numeric)");
            return;
        }
        
        String downloadPath = downloadPathField.getText().trim();
        if (downloadPath.isEmpty() || !new File(downloadPath).isDirectory()) {
            showError("Please select a valid download path");
            return;
        }
        
        // Save configuration
        saveConfiguration();
        
        // Create download request
        DownloadRequest request = new DownloadRequest();
        request.setWorkshopIds(workshopIds);
        request.setAppId(appId);
        request.setDownloadPath(downloadPath);
        request.setUsername(useAnonymousCheckbox.isSelected() ? null : usernameField.getText().trim());
        request.setPassword(useAnonymousCheckbox.isSelected() ? null : new String(passwordField.getPassword()));
        String authCode = authCodeField.getText().trim();
        request.setAuthCode(authCode.isEmpty() ? null : authCode);
        request.setSteamCmdPath(steamCmdPathField.getText().trim().isEmpty() ? null : steamCmdPathField.getText().trim());
        
        // Disable button and start download
        downloadButton.setEnabled(false);
        progressBar.setValue(0);
        logArea.setText("");
        
        new Thread(() -> {
            try {
                controller.downloadWorkshopItems(request);
                SwingUtilities.invokeLater(() -> {
                    downloadButton.setEnabled(true);
                    showSuccess("Download completed successfully!");
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    downloadButton.setEnabled(true);
                    showError("Download failed: " + ex.getMessage());
                });
            }
        }).start();
    }
    
    private List<String> getWorkshopIds() {
        List<String> ids = new ArrayList<>();
        switch (currentMode) {
            case SINGLE:
                String singleId = workshopIdField.getText().trim();
                if (!singleId.isEmpty() && singleId.matches("\\d+")) {
                    ids.add(singleId);
                }
                break;
            case LIST:
                String listText = workshopIdsArea.getText().trim();
                if (!listText.isEmpty()) {
                    for (String id : listText.split(",")) {
                        String trimmed = id.trim();
                        if (!trimmed.isEmpty() && trimmed.matches("\\d+")) {
                            ids.add(trimmed);
                        }
                    }
                }
                break;
            case FILE:
                String filePath = txtFileField.getText().trim();
                if (!filePath.isEmpty()) {
                    try {
                        Files.readAllLines(Paths.get(filePath)).forEach(line -> {
                            String trimmed = line.trim();
                            if (!trimmed.isEmpty() && trimmed.matches("\\d+")) {
                                ids.add(trimmed);
                            }
                        });
                    } catch (Exception e) {
                        logMessage("Error reading file: " + e.getMessage());
                    }
                }
                break;
        }
        return ids;
    }
    
    private void loadConfiguration() {
        AppConfig config = controller.getConfig();
        downloadPathField.setText(config.getDownloadPath());
        appIdField.setText(config.getAppId());
        steamCmdPathField.setText(config.getSteamCmdPath());
        useAnonymousCheckbox.setSelected(config.isUseAnonymous());
        usernameField.setText(config.getUsername() != null ? config.getUsername() : "");
        if (!config.getLastWorkshopId().isEmpty()) {
            workshopIdField.setText(config.getLastWorkshopId());
        }
    }
    
    private void saveConfiguration() {
        AppConfig config = controller.getConfig();
        config.setDownloadPath(downloadPathField.getText().trim());
        config.setAppId(appIdField.getText().trim());
        config.setSteamCmdPath(steamCmdPathField.getText().trim());
        config.setUseAnonymous(useAnonymousCheckbox.isSelected());
        config.setUsername(useAnonymousCheckbox.isSelected() ? null : usernameField.getText().trim());
        List<String> ids = getWorkshopIds();
        if (!ids.isEmpty()) {
            config.setLastWorkshopId(ids.get(0));
        }
        controller.saveConfig();
    }
    
    public void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    public void updateProgress(int progress) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(progress);
            progressBar.setString(progress + "%");
        });
    }
    
    public void updateStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(status);
        });
    }
    
    public String showSteamGuardDialog(String message) {
        final String[] result = new String[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                result[0] = JOptionPane.showInputDialog(
                    this,
                    "Steam está pidiendo el código de Steam Guard.\n\n" +
                    "Revisa tu email o aplicación de autenticación y ingresa el código:",
                    "Steam Guard Code Required",
                    JOptionPane.QUESTION_MESSAGE
                );
                result[0] = (result[0] != null) ? result[0].trim() : "";
            });
        } catch (Exception e) {
            logMessage("Error showing Steam Guard dialog: " + e.getMessage());
            result[0] = "";
        }
        return result[0];
    }
    
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    private void showSuccess(String message) {
        JOptionPane.showMessageDialog(this, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}

