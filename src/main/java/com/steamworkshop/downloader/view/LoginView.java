package com.steamworkshop.downloader.view;

import com.steamworkshop.downloader.controller.MainController;
import com.steamworkshop.downloader.model.AppConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Modern login view
 */
public class LoginView extends JDialog {
    private MainController controller;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JCheckBox useAnonymousCheckbox;
    private JButton loginButton;
    private boolean loginSuccessful = false;
    
    private static final Color PRIMARY_COLOR = new Color(0, 102, 204);
    
    public LoginView(JFrame parent, MainController controller) {
        super(parent, "Steam Login", true);
        this.controller = controller;
        initializeComponents();
        setupLayout();
        setupEventHandlers();
        loadSavedCredentials();
    }
    
    private void initializeComponents() {
        setSize(450, 280);
        setLocationRelativeTo(null);
        setResizable(false);
        
        usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        
        useAnonymousCheckbox = new JCheckBox("Use anonymous login");
        useAnonymousCheckbox.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        
        loginButton = new JButton("Login");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        loginButton.setBackground(PRIMARY_COLOR);
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        loginButton.setBorderPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        useAnonymousCheckbox.addActionListener(e -> {
            boolean enabled = !useAnonymousCheckbox.isSelected();
            usernameField.setEnabled(enabled);
            passwordField.setEnabled(enabled);
        });
    }
    
    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));
        ((JComponent) getContentPane()).setBorder(new EmptyBorder(20, 20, 20, 20));
        getContentPane().setBackground(Color.WHITE);
        
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        centerPanel.add(useAnonymousCheckbox, gbc);
        
        addLabeledField(centerPanel, "Username:", usernameField, 1, gbc);
        addLabeledField(centerPanel, "Password:", passwordField, 2, gbc);
        
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        JLabel infoLabel = new JLabel("<html><small>Configure your Steam login. You can change this later in the Login tab.</small></html>");
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        centerPanel.add(infoLabel, gbc);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.add(loginButton);
        
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);
    }
    
    private void addLabeledField(JPanel panel, String label, JComponent field, int row, GridBagConstraints gbc) {
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        JLabel jLabel = new JLabel(label);
        jLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        panel.add(jLabel, gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        panel.add(field, gbc);
    }
    
    private void setupEventHandlers() {
        loginButton.addActionListener(e -> {
            saveCredentials();
            loginSuccessful = true;
            dispose();
        });
        
        passwordField.addActionListener(e -> loginButton.doClick());
    }
    
    private void loadSavedCredentials() {
        AppConfig config = controller.getConfig();
        usernameField.setText(config.getUsername() != null ? config.getUsername() : "");
        useAnonymousCheckbox.setSelected(config.isUseAnonymous());
    }
    
    private void saveCredentials() {
        AppConfig config = controller.getConfig();
        config.setUsername(useAnonymousCheckbox.isSelected() ? null : usernameField.getText().trim());
        config.setUseAnonymous(useAnonymousCheckbox.isSelected());
        controller.saveConfig();
    }
    
    public boolean isLoginSuccessful() {
        return loginSuccessful;
    }
}

