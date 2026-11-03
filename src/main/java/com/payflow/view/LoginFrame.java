
package com.payflow.view;

import com.payflow.repository.AdministratorRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

public class LoginFrame extends JFrame {

    private final AdministratorRepository administratorRepository =
            new AdministratorRepository();

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    private JLabel titleLabel;
    private JLabel messageLabel;
    private JLabel confirmPasswordLabel;

    private JButton actionButton;

    private boolean setupMode = false;

    public LoginFrame() {

        setTitle("PayFlow - Administrator Login");
        setSize(460, 540);
        setMinimumSize(new Dimension(420, 500));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        initializeComponents();
        checkAdministratorAccount();

        setVisible(true);
    }

    private void initializeComponents() {

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(35, 40, 35, 40));

        // BRANDING
        JPanel brandingPanel = new JPanel();
        brandingPanel.setBackground(Color.WHITE);
        brandingPanel.setLayout(new BoxLayout(
                brandingPanel, BoxLayout.Y_AXIS
        ));

        JLabel logoLabel = new JLabel("PAYFLOW");
        logoLabel.setFont(new Font("Arial", Font.BOLD, 30));
        logoLabel.setForeground(new Color(31, 78, 121));
        logoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel =
                new JLabel("PAYROLL MANAGEMENT SYSTEM");

        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        subtitleLabel.setForeground(Color.GRAY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        brandingPanel.add(logoLabel);
        brandingPanel.add(Box.createVerticalStrut(5));
        brandingPanel.add(subtitleLabel);
        brandingPanel.add(Box.createVerticalStrut(30));

        // FORM
        JPanel formPanel = new JPanel();
        formPanel.setBackground(Color.WHITE);
        formPanel.setLayout(new BoxLayout(
                formPanel, BoxLayout.Y_AXIS
        ));

        titleLabel = new JLabel("Administrator Login");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 21));
        titleLabel.setForeground(new Color(40, 40, 40));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        messageLabel = new JLabel("Sign in to continue to PayFlow.");
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        messageLabel.setForeground(Color.GRAY);
        messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        formPanel.add(titleLabel);
        formPanel.add(Box.createVerticalStrut(8));
        formPanel.add(messageLabel);
        formPanel.add(Box.createVerticalStrut(25));

        // USERNAME
        addFormLabel(formPanel, "Username");

        usernameField = new JTextField();
        styleTextField(usernameField);
        formPanel.add(usernameField);

        formPanel.add(Box.createVerticalStrut(18));

        // PASSWORD
        addFormLabel(formPanel, "Password");

        passwordField = new JPasswordField();
        styleTextField(passwordField);
        formPanel.add(passwordField);

        formPanel.add(Box.createVerticalStrut(18));

        // CONFIRM PASSWORD
        confirmPasswordLabel =
                new JLabel("Confirm Password");

        confirmPasswordLabel.setFont(
                new Font("Arial", Font.BOLD, 12)
        );

        confirmPasswordLabel.setAlignmentX(
                Component.LEFT_ALIGNMENT
        );

        JPasswordField confirmField = new JPasswordField();

        confirmPasswordField = confirmField;
        styleTextField(confirmPasswordField);

        formPanel.add(confirmPasswordLabel);
        formPanel.add(Box.createVerticalStrut(7));
        formPanel.add(confirmPasswordField);

        formPanel.add(Box.createVerticalStrut(25));

        // BUTTON
        actionButton = new JButton("Login");
        actionButton.setFont(new Font("Arial", Font.BOLD, 14));
        actionButton.setForeground(Color.WHITE);
        actionButton.setBackground(new Color(31, 78, 121));
        actionButton.setFocusPainted(false);
        actionButton.setBorderPainted(false);
        actionButton.setCursor(
                Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        );
        actionButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        actionButton.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 45)
        );

        actionButton.addActionListener(e -> handleAction());

        formPanel.add(actionButton);

        // FOOTER
        JLabel footerLabel =
                new JLabel("Secure Payroll Management");

        footerLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        footerLabel.setForeground(Color.GRAY);
        footerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        mainPanel.add(brandingPanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(footerLabel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        getRootPane().setDefaultButton(actionButton);

        confirmPasswordLabel.setVisible(false);
        confirmPasswordField.setVisible(false);
    }

    private void addFormLabel(JPanel panel, String text) {

        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(label);
        panel.add(Box.createVerticalStrut(7));
    }

    private void styleTextField(JTextField field) {

        field.setFont(new Font("Arial", Font.PLAIN, 14));
        field.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 42)
        );
        field.setPreferredSize(new Dimension(350, 42));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                        new Color(210, 210, 210)
                ),
                new EmptyBorder(8, 10, 8, 10)
        ));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
    }

    private void checkAdministratorAccount() {

        try {

            setupMode =
                    !administratorRepository.administratorExists();

            updateInterface();

        } catch (Exception e) {

            actionButton.setEnabled(false);

            JOptionPane.showMessageDialog(
                    this,
                    "Could not check administrator accounts:\n"
                            + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void updateInterface() {

        if (setupMode) {

            titleLabel.setText("Create Administrator");
            messageLabel.setText(
                    "Create the first administrator account."
            );

            actionButton.setText("Create Administrator");

            confirmPasswordLabel.setVisible(true);
            confirmPasswordField.setVisible(true);

        } else {

            titleLabel.setText("Administrator Login");
            messageLabel.setText(
                    "Sign in to continue to PayFlow."
            );

            actionButton.setText("Login");

            confirmPasswordLabel.setVisible(false);
            confirmPasswordField.setVisible(false);
        }

        revalidate();
        repaint();
    }

    private void handleAction() {

        if (setupMode) {
            createFirstAdministrator();
        } else {
            loginAdministrator();
        }
    }

    private void createFirstAdministrator() {

        String username = usernameField.getText().trim();

        char[] password = passwordField.getPassword();
        char[] confirmation =
                confirmPasswordField.getPassword();

        if (username.length() < 3) {

            clearPasswords(password, confirmation);

            showError(
                    "Username must contain at least 3 characters."
            );
            return;
        }

        if (password.length < 8) {

            clearPasswords(password, confirmation);

            showError(
                    "Password must contain at least 8 characters."
            );
            return;
        }

        if (!Arrays.equals(password, confirmation)) {

            clearPasswords(password, confirmation);

            showError("Passwords do not match.");
            return;
        }

        actionButton.setEnabled(false);

        try {

            administratorRepository.createAdministrator(
                    username,
                    password
            );

            // The repository clears the password array.
            Arrays.fill(confirmation, '\0');

            JOptionPane.showMessageDialog(
                    this,
                    "Administrator account created successfully.\n"
                            + "You can now log in.",
                    "Account Created",
                    JOptionPane.INFORMATION_MESSAGE
            );

            setupMode = false;
            passwordField.setText("");
            confirmPasswordField.setText("");

            updateInterface();

        } catch (Exception e) {

            Arrays.fill(password, '\0');
            Arrays.fill(confirmation, '\0');

            showError(
                    "Could not create administrator:\n"
                            + e.getMessage()
            );

        } finally {

            actionButton.setEnabled(true);
        }
    }

    private void loginAdministrator() {

        String username = usernameField.getText().trim();
        char[] password = passwordField.getPassword();

        if (username.isEmpty() || password.length == 0) {

            Arrays.fill(password, '\0');

            showError("Enter your username and password.");
            return;
        }

        actionButton.setEnabled(false);

        try {

            boolean authenticated =
                    administratorRepository.authenticate(
                            username,
                            password
                    );

            if (authenticated) {

                JOptionPane.showMessageDialog(
                        this,
                        "Welcome to PayFlow!",
                        "Login Successful",
                        JOptionPane.INFORMATION_MESSAGE
                );

                dispose();

                SwingUtilities.invokeLater(
                        DashboardFrame::new
                );

            } else {

                passwordField.setText("");

                showError(
                        "Incorrect username or password."
                );
            }

        } catch (Exception e) {

            Arrays.fill(password, '\0');
            passwordField.setText("");

            showError(
                    "Login failed:\n" + e.getMessage()
            );

        } finally {

            actionButton.setEnabled(true);
        }
    }

    private void clearPasswords(
            char[] password,
            char[] confirmation) {

        Arrays.fill(password, '\0');
        Arrays.fill(confirmation, '\0');
    }

    private void showError(String message) {

        JOptionPane.showMessageDialog(
                this,
                message,
                "PayFlow",
                JOptionPane.ERROR_MESSAGE
        );
    }
}
