
package com.payflow.view;

import com.payflow.model.AppSettings;
import com.payflow.repository.SettingsRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.sql.SQLException;

public class SettingsPanel extends JPanel {

    private final Color BACKGROUND = new Color(245, 247, 251);
    private final Color BLUE = new Color(48, 105, 220);
    private final Color TEXT = new Color(35, 45, 65);
    private final Color MUTED = new Color(110, 120, 140);
    private final Color GREEN = new Color(35, 150, 95);

    private final SettingsRepository settingsRepository =
            new SettingsRepository();

    private JTextField companyNameField;
    private JTextField companyAddressField;
    private JTextField companyPhoneField;
    private JTextField companyEmailField;

    private JComboBox<String> currencyCombo;
    private JSpinner workingDaysSpinner;

    private JTextField taxRateField;
    private JTextField ssnitRateField;

    private JButton reloadButton;
    private JButton saveButton;

    public SettingsPanel() {

        setLayout(new BorderLayout(0, 15));
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(15, 10, 15, 10));

        add(createHeading(), BorderLayout.NORTH);
        add(createMainContent(), BorderLayout.CENTER);

        loadSettings();
    }

    // --------------------------------------------------
    // HEADING
    // --------------------------------------------------

    private JPanel createHeading() {

        JPanel panel = new JPanel();

        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS)
        );

        panel.setBackground(BACKGROUND);

        JLabel heading = new JLabel("Application Settings");

        heading.setFont(new Font("Arial", Font.BOLD, 23));
        heading.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Manage company information and payroll preferences."
        );

        subtitle.setFont(new Font("Arial", Font.PLAIN, 13));
        subtitle.setForeground(MUTED);

        panel.add(heading);
        panel.add(Box.createVerticalStrut(7));
        panel.add(subtitle);

        return panel;
    }

    // --------------------------------------------------
    // MAIN CONTENT
    // --------------------------------------------------

    private JPanel createMainContent() {

        JPanel content = new JPanel();

        content.setLayout(
                new BoxLayout(content, BoxLayout.Y_AXIS)
        );

        content.setBackground(BACKGROUND);

        content.add(createCompanySection());
        content.add(Box.createVerticalStrut(15));
        content.add(createPayrollSection());
        content.add(Box.createVerticalStrut(15));
        content.add(createButtons());

        return content;
    }

    // --------------------------------------------------
    // COMPANY PROFILE
    // --------------------------------------------------

    private JPanel createCompanySection() {

        JPanel card = createCard();

        JLabel heading = createSectionHeading("Company Profile");

        JPanel fields = new JPanel(
                new GridLayout(0, 2, 15, 12)
        );

        fields.setBackground(Color.WHITE);

        companyNameField = new JTextField();
        companyAddressField = new JTextField();
        companyPhoneField = new JTextField();
        companyEmailField = new JTextField();

        addField(fields, "Company Name", companyNameField);
        addField(fields, "Company Address", companyAddressField);
        addField(fields, "Phone Number", companyPhoneField);
        addField(fields, "Email Address", companyEmailField);

        card.add(heading, BorderLayout.NORTH);
        card.add(fields, BorderLayout.CENTER);

        return card;
    }

    // --------------------------------------------------
    // PAYROLL PREFERENCES
    // --------------------------------------------------

    private JPanel createPayrollSection() {

        JPanel card = createCard();

        JLabel heading = createSectionHeading(
                "Payroll Preferences"
        );

        JPanel fields = new JPanel(
                new GridLayout(0, 2, 15, 12)
        );

        fields.setBackground(Color.WHITE);

        currencyCombo = new JComboBox<>(
                new String[]{"GHS", "USD", "GBP", "EUR"}
        );

        workingDaysSpinner = new JSpinner(
                new SpinnerNumberModel(22, 1, 31, 1)
        );

        taxRateField = new JTextField("0.0");
        ssnitRateField = new JTextField("0.0");

        addField(fields, "Currency", currencyCombo);

        addField(
                fields,
                "Working Days Per Month",
                workingDaysSpinner
        );

        addField(
                fields,
                "Default Tax Rate (%)",
                taxRateField
        );

        addField(
                fields,
                "Default SSNIT Rate (%)",
                ssnitRateField
        );

        JLabel note = new JLabel(
                "<html>New payroll calculations use the saved tax and "
                + "SSNIT rates.<br>"
                + "Previously saved payroll records are not changed.</html>"
        );

        note.setFont(new Font("Arial", Font.ITALIC, 11));
        note.setForeground(MUTED);

        JPanel center = new JPanel(
                new BorderLayout(0, 10)
        );

        center.setBackground(Color.WHITE);
        center.add(fields, BorderLayout.CENTER);
        center.add(note, BorderLayout.SOUTH);

        card.add(heading, BorderLayout.NORTH);
        card.add(center, BorderLayout.CENTER);

        return card;
    }

    // --------------------------------------------------
    // BUTTONS
    // --------------------------------------------------

    private JPanel createButtons() {

        JPanel panel = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 0)
        );

        panel.setBackground(BACKGROUND);

        reloadButton = createButton(
                "Reload Settings",
                new Color(100, 110, 125)
        );

        saveButton = createButton(
                "Save Settings",
                GREEN
        );

        reloadButton.addActionListener(event -> reloadSettings());
        saveButton.addActionListener(event -> saveSettings());

        panel.add(reloadButton);
        panel.add(saveButton);

        return panel;
    }

    // --------------------------------------------------
    // CARD AND FIELD HELPERS
    // --------------------------------------------------

    private JPanel createCard() {

        JPanel card = new JPanel(
                new BorderLayout(0, 15)
        );

        card.setBackground(Color.WHITE);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 238)
                        ),
                        new EmptyBorder(18, 20, 18, 20)
                )
        );

        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        return card;
    }

    private JLabel createSectionHeading(String text) {

        JLabel heading = new JLabel(text);

        heading.setFont(new Font("Arial", Font.BOLD, 17));
        heading.setForeground(TEXT);

        return heading;
    }

    private void addField(
            JPanel panel,
            String label,
            JComponent component) {

        JPanel fieldPanel = new JPanel(
                new BorderLayout(0, 5)
        );

        fieldPanel.setBackground(Color.WHITE);

        JLabel fieldLabel = new JLabel(label);

        fieldLabel.setFont(new Font("Arial", Font.BOLD, 12));
        fieldLabel.setForeground(TEXT);

        component.setFont(new Font("Arial", Font.PLAIN, 13));

        fieldPanel.add(fieldLabel, BorderLayout.NORTH);
        fieldPanel.add(component, BorderLayout.CENTER);

        panel.add(fieldPanel);
    }

    // --------------------------------------------------
    // BUTTON STYLE
    // --------------------------------------------------

    private JButton createButton(String text, Color color) {

        JButton button = new JButton(text);

        button.setUI(new BasicButtonUI());

        button.setBackground(color);
        button.setForeground(Color.WHITE);

        button.setFont(new Font("Arial", Font.BOLD, 12));

        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(false);

        button.setHorizontalAlignment(SwingConstants.CENTER);

        button.setBorder(
                new EmptyBorder(10, 16, 10, 16)
        );

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        return button;
    }

    // --------------------------------------------------
    // RELOAD SETTINGS
    // --------------------------------------------------

    private void reloadSettings() {

        reloadButton.setEnabled(false);

        try {

            loadSettingsOrThrow();

            JOptionPane.showMessageDialog(
                    this,
                    "Saved settings have been reloaded successfully.",
                    "Settings Reloaded",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (SQLException e) {

            showError(
                    "Could not reload settings:\n"
                    + e.getMessage()
            );

        } finally {

            reloadButton.setEnabled(true);
        }
    }

    // --------------------------------------------------
    // LOAD SETTINGS
    // --------------------------------------------------

    public void loadSettings() {

        try {

            loadSettingsOrThrow();

        } catch (SQLException e) {

            showError(
                    "Could not load settings:\n"
                    + e.getMessage()
            );
        }
    }

    private void loadSettingsOrThrow() throws SQLException {

        AppSettings settings =
                settingsRepository.findSettings();

        if (settings == null) {
            throw new SQLException(
                    "No application settings were returned."
            );
        }

        companyNameField.setText(
                safeText(settings.getCompanyName())
        );

        companyAddressField.setText(
                safeText(settings.getCompanyAddress())
        );

        companyPhoneField.setText(
                safeText(settings.getCompanyPhone())
        );

        companyEmailField.setText(
                safeText(settings.getCompanyEmail())
        );

        String currency = settings.getCurrency();

        if (currency == null || !containsCurrency(currency)) {
            currency = "GHS";
        }

        currencyCombo.setSelectedItem(currency);

        int workingDays = settings.getWorkingDays();

        if (workingDays < 1 || workingDays > 31) {
            workingDays = 22;
        }

        workingDaysSpinner.setValue(workingDays);

        taxRateField.setText(
                String.valueOf(settings.getDefaultTaxRate())
        );

        ssnitRateField.setText(
                String.valueOf(settings.getDefaultSsnitRate())
        );
    }

    private boolean containsCurrency(String currency) {

        for (int i = 0; i < currencyCombo.getItemCount(); i++) {

            if (currencyCombo.getItemAt(i).equals(currency)) {
                return true;
            }
        }

        return false;
    }

    private String safeText(String value) {

        return value == null ? "" : value;
    }

    // --------------------------------------------------
    // SAVE SETTINGS
    // --------------------------------------------------

    private void saveSettings() {

        saveButton.setEnabled(false);

        try {

            String companyName =
                    companyNameField.getText().trim();

            String companyAddress =
                    companyAddressField.getText().trim();

            String companyEmail =
                    companyEmailField.getText().trim();

            String companyPhone =
                    companyPhoneField.getText().trim();

            if (companyName.isEmpty()) {

                showError("Company name cannot be empty.");
                companyNameField.requestFocusInWindow();
                return;
            }

            if (!companyEmail.isEmpty()
                    && !companyEmail.matches(
                            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

                showError("Enter a valid company email address.");
                companyEmailField.requestFocusInWindow();
                return;
            }

            if (!companyPhone.isEmpty()
                    && !companyPhone.matches("[0-9+() -]{7,20}")) {

                showError("Enter a valid company phone number.");
                companyPhoneField.requestFocusInWindow();
                return;
            }

            double taxRate = Double.parseDouble(
                    taxRateField.getText().trim()
            );

            double ssnitRate = Double.parseDouble(
                    ssnitRateField.getText().trim()
            );

            if (!Double.isFinite(taxRate)
                    || !Double.isFinite(ssnitRate)
                    || taxRate < 0
                    || ssnitRate < 0
                    || taxRate > 100
                    || ssnitRate > 100) {

                showError(
                        "Tax and SSNIT rates must each be between 0 and 100."
                );

                return;
            }

            AppSettings settings = new AppSettings(
                    companyName,
                    companyAddress,
                    companyPhone,
                    companyEmail,
                    (String) currencyCombo.getSelectedItem(),
                    (Integer) workingDaysSpinner.getValue(),
                    taxRate,
                    ssnitRate
            );

            settingsRepository.saveSettings(settings);

            JOptionPane.showMessageDialog(
                    this,
                    "Application settings saved successfully!",
                    "Settings Saved",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (NumberFormatException e) {

            showError(
                    "Enter valid numeric values for tax and SSNIT rates."
            );

        } catch (SQLException e) {

            showError(
                    "Could not save settings:\n"
                    + e.getMessage()
            );

        } finally {

            saveButton.setEnabled(true);
        }
    }

    // --------------------------------------------------
    // ERROR MESSAGE
    // --------------------------------------------------

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "PayFlow Settings",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
