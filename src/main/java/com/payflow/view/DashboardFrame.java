package com.payflow.view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableModel;

import com.payflow.ai.OpenAIService;
import com.payflow.model.AppSettings;
import com.payflow.model.Employee;
import com.payflow.model.Payroll;
import com.payflow.repository.EmployeeRepository;
import com.payflow.repository.PayrollRepository;
import com.payflow.repository.SettingsRepository;
import com.payflow.service.PayrollInsightsService;

public class DashboardFrame extends JFrame {

    private final Color SIDEBAR = new Color(24, 35, 56);
    private final Color BACKGROUND = new Color(245, 247, 251);
    private final Color BLUE = new Color(48, 105, 220);
    private final Color HOVER = new Color(43, 59, 85);
    private final Color TEXT = new Color(35, 45, 65);
    private final Color MUTED = new Color(110, 120, 140);

    private final JButton[] navigationButtons = new JButton[8];
    private final CardLayout pageLayout = new CardLayout();

    private JPanel pageContainer;
    private JLabel pageTitle;

    private JLayeredPane layeredContent;
    private JPanel floatingLayer;

    private JButton aiButton;
    private JPanel aiChatPanel;
    private JTextArea aiConversation;
    private JTextField aiInput;

    private boolean aiChatOpen = false;

    private final EmployeeRepository employeeRepository =
            new EmployeeRepository();

    private final PayrollRepository payrollRepository =
            new PayrollRepository();

    private final SettingsRepository settingsRepository =
            new SettingsRepository();

    private JLabel employeeCountLabel;
    private JLabel monthlyPayrollLabel;
    private JLabel payrollStatusLabel;

    private JLabel employeeDescriptionLabel;
    private JLabel payrollDescriptionLabel;
    private JLabel statusDescriptionLabel;

    private JLabel companyNameLabel;
    private JLabel companyAddressLabel;
    private JLabel companyPhoneLabel;
    private JLabel companyEmailLabel;

    private DefaultTableModel activityTableModel;

    private EmployeePanel employeePanel;
    private PayslipPanel payslipPanel;
    private PayrollHistoryPanel payrollHistoryPanel;
    private ReportsPanel reportsPanel;
    private SettingsPanel settingsPanel;

    public DashboardFrame() {

        setTitle("PayFlow | Payroll Management System");
        setSize(1200, 750);
        setMinimumSize(new Dimension(950, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BACKGROUND);

        root.add(createSidebar(), BorderLayout.WEST);
        root.add(createMainContent(), BorderLayout.CENTER);

        setContentPane(root);

        refreshDashboardData();

        setVisible(true);
    }

    // SIDEBAR

    private JPanel createSidebar() {

        JPanel sidebar = new JPanel();

        sidebar.setPreferredSize(new Dimension(230, 0));
        sidebar.setBackground(SIDEBAR);

        sidebar.setLayout(
                new BoxLayout(sidebar, BoxLayout.Y_AXIS)
        );

        sidebar.setBorder(
                new EmptyBorder(25, 15, 25, 15)
        );

        JLabel logo = new JLabel("PayFlow");

        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Arial", Font.BOLD, 27));

        logo.setBorder(
                new EmptyBorder(0, 10, 35, 0)
        );

        sidebar.add(logo);

        addNavigationButton(sidebar, "Dashboard", true);
        addNavigationButton(sidebar, "Employees", false);
        addNavigationButton(sidebar, "Payroll", false);
        addNavigationButton(sidebar, "Payslips", false);
        addNavigationButton(sidebar, "Payroll History", false);
        addNavigationButton(sidebar, "Reports", false);

        sidebar.add(Box.createVerticalGlue());

        addNavigationButton(sidebar, "Settings", false);
        addNavigationButton(sidebar, "Logout", false);

        return sidebar;
    }

    private void addNavigationButton(
            JPanel sidebar,
            String name,
            boolean active) {

        JButton button = createNavButton(name, active);

        sidebar.add(button);

        navigationButtons[getNavigationIndex(name)] = button;

        button.addActionListener(event -> {

            if ("Logout".equals(name)) {

                int choice = JOptionPane.showConfirmDialog(
                        this,
                        "Are you sure you want to log out?",
                        "Logout Confirmation",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

                if (choice == JOptionPane.YES_OPTION) {

                    dispose();

                    SwingUtilities.invokeLater(LoginFrame::new);
                }

                return;
            }

            for (JButton navButton : navigationButtons) {

                if (navButton != null) {

                    navButton.putClientProperty("active", false);

                    animateButtonColour(navButton, SIDEBAR);
                }
            }

            button.putClientProperty("active", true);

            animateButtonColour(button, BLUE);

            showPage(name);
        });
    }

    private int getNavigationIndex(String name) {

        switch (name) {
            case "Dashboard":
                return 0;
            case "Employees":
                return 1;
            case "Payroll":
                return 2;
            case "Payslips":
                return 3;
            case "Payroll History":
                return 4;
            case "Reports":
                return 5;
            case "Settings":
                return 6;
            case "Logout":
                return 7;
            default:
                return 0;
        }
    }

    // NAVIGATION BUTTON DESIGN

    private JButton createNavButton(
            String name,
            boolean active) {

        JButton button = new JButton(name);

        button.setUI(new BasicButtonUI());

        button.setMaximumSize(new Dimension(200, 45));
        button.setPreferredSize(new Dimension(200, 45));
        button.setMinimumSize(new Dimension(200, 45));

        button.setHorizontalAlignment(SwingConstants.LEFT);

        button.setFont(new Font("Arial", Font.BOLD, 15));
        button.setForeground(Color.WHITE);
        button.setBackground(active ? BLUE : SIDEBAR);

        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setRolloverEnabled(false);

        button.putClientProperty("active", active);

        button.setBorder(
                new EmptyBorder(10, 15, 10, 10)
        );

        button.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent event) {

                if (!Boolean.TRUE.equals(
                        button.getClientProperty("active"))) {

                    animateButtonColour(button, HOVER);
                }
            }

            @Override
            public void mouseExited(MouseEvent event) {

                if (!Boolean.TRUE.equals(
                        button.getClientProperty("active"))) {

                    animateButtonColour(button, SIDEBAR);
                }
            }
        });

        return button;
    }

    // BUTTON COLOUR ANIMATION

    private void animateButtonColour(
            JButton button,
            Color target) {

        Timer timer =
                (Timer) button.getClientProperty("colourTimer");

        if (timer != null && timer.isRunning()) {
            timer.stop();
        }

        Color start = button.getBackground();

        final int[] step = {0};

        Timer newTimer = new Timer(15, null);

        newTimer.addActionListener(event -> {

            step[0]++;

            float progress = Math.min(step[0] / 8.0f, 1.0f);

            int red = (int) (
                    start.getRed()
                    + (target.getRed() - start.getRed()) * progress
            );

            int green = (int) (
                    start.getGreen()
                    + (target.getGreen() - start.getGreen()) * progress
            );

            int blue = (int) (
                    start.getBlue()
                    + (target.getBlue() - start.getBlue()) * progress
            );

            button.setBackground(new Color(red, green, blue));

            if (progress >= 1.0f) {
                newTimer.stop();
            }
        });

        button.putClientProperty("colourTimer", newTimer);

        newTimer.start();
    }

    // MAIN CONTENT

    private JPanel createMainContent() {

        JPanel content = new JPanel(
                new BorderLayout(0, 25)
        );

        content.setBackground(BACKGROUND);

        content.setBorder(
                new EmptyBorder(30, 30, 30, 30)
        );

        content.add(createHeader(), BorderLayout.NORTH);

        pageContainer = new JPanel(pageLayout);
        pageContainer.setBackground(BACKGROUND);

        pageContainer.add(createDashboardPage(), "Dashboard");

        employeePanel = new EmployeePanel(
                this::refreshAfterEmployeeDeletion
        );

        payslipPanel = new PayslipPanel();
        payrollHistoryPanel = new PayrollHistoryPanel();
        reportsPanel = new ReportsPanel();
        settingsPanel = new SettingsPanel();

        pageContainer.add(employeePanel, "Employees");
        pageContainer.add(new PayrollPanel(), "Payroll");
        pageContainer.add(payslipPanel, "Payslips");
        pageContainer.add(payrollHistoryPanel, "Payroll History");
        pageContainer.add(reportsPanel, "Reports");
        pageContainer.add(settingsPanel, "Settings");

        layeredContent = new JLayeredPane();
        layeredContent.setLayout(null);
        layeredContent.setOpaque(false);

        layeredContent.add(pageContainer, Integer.valueOf(0));

        floatingLayer = new JPanel(null);
        floatingLayer.setOpaque(false);

        createFloatingAIButton();

        layeredContent.add(floatingLayer, Integer.valueOf(100));

        layeredContent.addComponentListener(
                new java.awt.event.ComponentAdapter() {

                    @Override
                    public void componentResized(
                            java.awt.event.ComponentEvent event) {

                        layoutFloatingComponents();
                    }
                }
        );

        content.add(layeredContent, BorderLayout.CENTER);

        return content;
    }

    private void layoutFloatingComponents() {

        if (layeredContent == null || aiButton == null) {
            return;
        }

        int width = layeredContent.getWidth();
        int height = layeredContent.getHeight();

        pageContainer.setBounds(0, 0, width, height);

        floatingLayer.setBounds(0, 0, width, height);

        int buttonWidth = 165;
        int buttonHeight = 48;

        aiButton.setBounds(
                Math.max(10, width - buttonWidth - 20),
                Math.max(10, height - buttonHeight - 20),
                Math.min(buttonWidth, Math.max(100, width - 20)),
                buttonHeight
        );

        if (aiChatPanel != null && aiChatOpen) {

            int chatWidth = Math.min(350, width - 30);
            int chatHeight = Math.min(440, height - 30);

            aiChatPanel.setBounds(
                    width - chatWidth - 20,
                    height - chatHeight - 80,
                    chatWidth,
                    chatHeight
            );
        }

        floatingLayer.revalidate();
        floatingLayer.repaint();
    }

    private void showPage(String pageName) {

        pageLayout.show(pageContainer, pageName);

        pageTitle.setText(pageName);

        if ("Dashboard".equals(pageName)) {
            refreshDashboardData();
        }

        if ("Payroll History".equals(pageName)
                && payrollHistoryPanel != null) {

            payrollHistoryPanel.loadPayrollHistory();
        }

        if ("Reports".equals(pageName)
                && reportsPanel != null) {

            reportsPanel.loadReport();
        }

        if ("Settings".equals(pageName)
                && settingsPanel != null) {

            settingsPanel.loadSettings();
        }

        pageContainer.revalidate();
        pageContainer.repaint();
    }

    // FLOATING AI BUTTON

    private void createFloatingAIButton() {

        aiButton = new JButton("🤖 Ask PayFlow AI");

        aiButton.setUI(new BasicButtonUI());
        aiButton.setFont(new Font("Arial", Font.BOLD, 14));
        aiButton.setForeground(Color.WHITE);
        aiButton.setBackground(new Color(91, 75, 220));

        aiButton.setOpaque(true);
        aiButton.setContentAreaFilled(true);
        aiButton.setBorderPainted(false);
        aiButton.setFocusPainted(false);

        aiButton.setCursor(
                new Cursor(Cursor.HAND_CURSOR)
        );

        aiButton.setBorder(
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        );

        aiButton.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent event) {
                aiButton.setBackground(new Color(71, 55, 195));
            }

            @Override
            public void mouseExited(MouseEvent event) {
                aiButton.setBackground(new Color(91, 75, 220));
            }
        });

        aiButton.addActionListener(event -> toggleAIChat());

        floatingLayer.add(aiButton);
    }

    private void toggleAIChat() {

        if (aiChatOpen) {

            floatingLayer.remove(aiChatPanel);

            aiChatPanel = null;
            aiChatOpen = false;

        } else {

            createAIChatPanel();

            aiChatOpen = true;

            floatingLayer.add(aiChatPanel, Integer.valueOf(50));
        }

        layoutFloatingComponents();

        floatingLayer.revalidate();
        floatingLayer.repaint();
    }

    // AI CHAT PANEL

    private void createAIChatPanel() {

        aiChatPanel = new JPanel(new BorderLayout());

        aiChatPanel.setBackground(Color.WHITE);

        aiChatPanel.setBorder(
                BorderFactory.createLineBorder(
                        new Color(220, 225, 235)
                )
        );

        JPanel chatHeader = new JPanel(new BorderLayout());

        chatHeader.setBackground(new Color(55, 75, 170));

        chatHeader.setBorder(
                new EmptyBorder(15, 15, 15, 12)
        );

        JPanel titlePanel = new JPanel();

        titlePanel.setOpaque(false);

        titlePanel.setLayout(
                new BoxLayout(titlePanel, BoxLayout.Y_AXIS)
        );

        JLabel title = new JLabel("PayFlow AI");

        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 17));

        JLabel subtitle = new JLabel(
                "Your intelligent payroll assistant"
        );

        subtitle.setForeground(new Color(225, 230, 255));
        subtitle.setFont(new Font("Arial", Font.PLAIN, 11));

        titlePanel.add(title);
        titlePanel.add(Box.createVerticalStrut(4));
        titlePanel.add(subtitle);

        JButton closeButton = new JButton("×");

        closeButton.setUI(new BasicButtonUI());
        closeButton.setFont(new Font("Arial", Font.BOLD, 25));
        closeButton.setForeground(Color.WHITE);
        closeButton.setBackground(new Color(55, 75, 170));
        closeButton.setOpaque(true);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        closeButton.addActionListener(event -> toggleAIChat());

        chatHeader.add(titlePanel, BorderLayout.CENTER);
        chatHeader.add(closeButton, BorderLayout.EAST);

        aiConversation = new JTextArea();

        aiConversation.setEditable(false);
        aiConversation.setLineWrap(true);
        aiConversation.setWrapStyleWord(true);
        aiConversation.setFont(new Font("Arial", Font.PLAIN, 13));
        aiConversation.setForeground(TEXT);
        aiConversation.setBackground(new Color(248, 249, 253));

        aiConversation.setBorder(
                new EmptyBorder(15, 15, 15, 15)
        );

        aiConversation.setText(
                "Hello, Administrator!\n\n"
                + "Welcome to PayFlow AI.\n\n"
                + "Ask me questions about payroll, salary calculations, "
                + "tax, SSNIT and payroll management.\n\n"
                + "Try asking:\n"
                + "• Analyse my payroll\n"
                + "• Show me payroll alerts\n"
                + "• What is my total net payroll?\n\n"
                + "Offline payroll insights are available without API credits."
        );

        JScrollPane conversationScroll =
                new JScrollPane(aiConversation);

        conversationScroll.setBorder(null);

        JPanel inputPanel = new JPanel(new BorderLayout(8, 0));

        inputPanel.setBackground(Color.WHITE);

        inputPanel.setBorder(
                new EmptyBorder(10, 10, 10, 10)
        );

        aiInput = new JTextField();

        aiInput.setFont(new Font("Arial", Font.PLAIN, 13));

        aiInput.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(215, 220, 230)
                        ),
                        BorderFactory.createEmptyBorder(
                                9, 10, 9, 10
                        )
                )
        );

        JButton sendButton = new JButton("Send");

        sendButton.setUI(new BasicButtonUI());
        sendButton.setFont(new Font("Arial", Font.BOLD, 12));
        sendButton.setForeground(Color.WHITE);
        sendButton.setBackground(BLUE);
        sendButton.setOpaque(true);
        sendButton.setContentAreaFilled(true);
        sendButton.setBorderPainted(false);
        sendButton.setFocusPainted(false);
        sendButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        sendButton.setMargin(
                new Insets(9, 13, 9, 13)
        );

        sendButton.addActionListener(
                event -> sendAIMessage()
        );

        aiInput.addActionListener(
                event -> sendAIMessage()
        );

        inputPanel.add(aiInput, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        aiChatPanel.add(chatHeader, BorderLayout.NORTH);
        aiChatPanel.add(conversationScroll, BorderLayout.CENTER);
        aiChatPanel.add(inputPanel, BorderLayout.SOUTH);
    }

    // AI MESSAGE HANDLING

    private void sendAIMessage() {

        String message = aiInput.getText().trim();

        if (message.isEmpty()) {
            return;
        }

        JTextArea conversationTarget = aiConversation;
        JTextField inputTarget = aiInput;

        conversationTarget.append(
                "\n\nYou:\n" + message + "\n"
        );

        conversationTarget.append(
                "\nPayFlow AI:\nThinking...\n"
        );

        conversationTarget.setCaretPosition(
                conversationTarget.getDocument().getLength()
        );

        inputTarget.setText("");
        inputTarget.setEnabled(false);

        SwingWorker<String, Void> worker = new SwingWorker<>() {

            @Override
            protected String doInBackground() throws Exception {

                // First, check whether the request can be answered
                // using the local payroll database.
                if (isPayrollInsightsRequest(message)) {

                    PayrollInsightsService insightsService =
                            new PayrollInsightsService();

                    return insightsService.generateInsights();
                }

                // General questions still use the OpenAI API.
                OpenAIService aiService = new OpenAIService();

                return aiService.askAI(message);
            }

            @Override
            protected void done() {

                try {

                    String answer = get();

                    conversationTarget.append(answer + "\n");

                } catch (Exception exception) {

                    Throwable cause = exception.getCause();

                    String errorMessage = cause != null
                            ? cause.getMessage()
                            : exception.getMessage();

                    if (errorMessage == null || errorMessage.isBlank()) {
                        errorMessage = "An unexpected error occurred.";
                    }

                    conversationTarget.append(
                            "I couldn't complete that request. "
                            + errorMessage + "\n"
                    );

                    System.err.println(
                            "PayFlow AI request failed: " + errorMessage
                    );

                } finally {

                    inputTarget.setEnabled(true);

                    conversationTarget.setCaretPosition(
                            conversationTarget.getDocument().getLength()
                    );

                    if (aiInput == inputTarget) {
                        inputTarget.requestFocusInWindow();
                    }
                }
            }
        };

        worker.execute();
    }

    // IDENTIFY REQUESTS THAT CAN BE ANSWERED OFFLINE

    private boolean isPayrollInsightsRequest(String message) {

        String normalized = message.toLowerCase(Locale.ROOT);

        return normalized.contains("analyse my payroll")
                || normalized.contains("analyze my payroll")
                || normalized.contains("analyse payroll")
                || normalized.contains("analyze payroll")
                || normalized.contains("payroll insight")
                || normalized.contains("payroll alert")
                || normalized.contains("high deduction")
                || normalized.contains("unusual deduction")
                || normalized.contains("total net payroll")
                || normalized.contains("total gross payroll")
                || normalized.contains("payroll overview")
                || normalized.contains("payroll summary")
                || normalized.contains("pending payroll")
                || normalized.contains("paid payroll");
    }

    // REFRESH AFTER EMPLOYEE DELETION

    private void refreshAfterEmployeeDeletion() {

        refreshDashboardData();

        if (payslipPanel != null) {
            payslipPanel.refreshRecords();
        }

        if (payrollHistoryPanel != null) {
            payrollHistoryPanel.loadPayrollHistory();
        }

        if (reportsPanel != null) {
            reportsPanel.loadReport();
        }
    }

    // DASHBOARD PAGE

    private JScrollPane createDashboardPage() {

        JPanel body = new JPanel();

        body.setBackground(BACKGROUND);

        body.setLayout(
                new BoxLayout(body, BoxLayout.Y_AXIS)
        );

        body.add(createWelcomeSection());
        body.add(Box.createVerticalStrut(15));

        body.add(createCompanyInformation());
        body.add(Box.createVerticalStrut(25));

        body.add(createStatistics());
        body.add(Box.createVerticalStrut(25));

        body.add(createActivitySection());

        JScrollPane scrollPane = new JScrollPane(body);

        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(BACKGROUND);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        return scrollPane;
    }

    // HEADER

    private JPanel createHeader() {

        JPanel header = new JPanel(new BorderLayout());

        header.setBackground(BACKGROUND);

        pageTitle = new JLabel("Dashboard");

        pageTitle.setFont(new Font("Arial", Font.BOLD, 25));
        pageTitle.setForeground(TEXT);

        JLabel profile = new JLabel("Administrator");

        profile.setFont(new Font("Arial", Font.BOLD, 14));
        profile.setForeground(TEXT);

        header.add(pageTitle, BorderLayout.WEST);
        header.add(profile, BorderLayout.EAST);

        return header;
    }

    // WELCOME SECTION

    private JPanel createWelcomeSection() {

        JPanel panel = new JPanel();

        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS)
        );

        panel.setBackground(BACKGROUND);
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel welcome = new JLabel(
                "Welcome back, Administrator!"
        );

        welcome.setFont(new Font("Arial", Font.BOLD, 22));
        welcome.setForeground(TEXT);

        JLabel subtitle = new JLabel(
                "Here's an overview of your payroll operations."
        );

        subtitle.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitle.setForeground(MUTED);

        panel.add(welcome);
        panel.add(Box.createVerticalStrut(8));
        panel.add(subtitle);

        return panel;
    }

    // COMPANY INFORMATION

    private JPanel createCompanyInformation() {

        JPanel panel = new JPanel();

        panel.setLayout(
                new BoxLayout(panel, BoxLayout.Y_AXIS)
        );

        panel.setBackground(Color.WHITE);

        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 238)
                        ),
                        new EmptyBorder(15, 18, 15, 18)
                )
        );

        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sectionHeading = new JLabel(
                "Company Information"
        );

        sectionHeading.setFont(
                new Font("Arial", Font.BOLD, 13)
        );

        sectionHeading.setForeground(MUTED);

        companyNameLabel = new JLabel("My Company");

        companyNameLabel.setFont(
                new Font("Arial", Font.BOLD, 20)
        );

        companyNameLabel.setForeground(BLUE);

        companyAddressLabel = new JLabel("Address: Not provided");
        companyPhoneLabel = new JLabel("Phone: Not provided");
        companyEmailLabel = new JLabel("Email: Not provided");

        Font detailsFont = new Font(
                "Arial",
                Font.PLAIN,
                13
        );

        companyAddressLabel.setFont(detailsFont);
        companyPhoneLabel.setFont(detailsFont);
        companyEmailLabel.setFont(detailsFont);

        companyAddressLabel.setForeground(TEXT);
        companyPhoneLabel.setForeground(TEXT);
        companyEmailLabel.setForeground(TEXT);

        panel.add(sectionHeading);
        panel.add(Box.createVerticalStrut(8));
        panel.add(companyNameLabel);
        panel.add(Box.createVerticalStrut(8));
        panel.add(companyAddressLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(companyPhoneLabel);
        panel.add(Box.createVerticalStrut(4));
        panel.add(companyEmailLabel);

        return panel;
    }

    private void refreshCompanyInformation() {

        try {

            AppSettings settings =
                    settingsRepository.findSettings();

            String companyName = settings.getCompanyName();

            if (companyName == null
                    || companyName.trim().isEmpty()) {

                companyName = "My Company";
            }

            companyNameLabel.setText(companyName.trim());

            companyAddressLabel.setText(
                    "Address: " + displayValue(settings.getCompanyAddress())
            );

            companyPhoneLabel.setText(
                    "Phone: " + displayValue(settings.getCompanyPhone())
            );

            companyEmailLabel.setText(
                    "Email: " + displayValue(settings.getCompanyEmail())
            );

        } catch (Exception e) {

            companyNameLabel.setText(
                    "Company information unavailable"
            );

            companyAddressLabel.setText("Address: Could not load");
            companyPhoneLabel.setText("Phone: Could not load");
            companyEmailLabel.setText("Email: Could not load");

            System.err.println(
                    "Could not load company information: "
                    + e.getMessage()
            );
        }
    }

    private String displayValue(String value) {

        if (value == null || value.trim().isEmpty()) {
            return "Not provided";
        }

        return value.trim();
    }

    // STATISTICS CARDS

    private JPanel createStatistics() {

        JPanel cards = new JPanel(
                new GridLayout(1, 3, 18, 0)
        );

        cards.setBackground(BACKGROUND);
        cards.setAlignmentX(Component.LEFT_ALIGNMENT);

        cards.setMaximumSize(
                new Dimension(Integer.MAX_VALUE, 145)
        );

        cards.add(createStatCard(
                "Total Employees",
                "0",
                "Registered employees",
                1
        ));

        cards.add(createStatCard(
                "Monthly Payroll",
                "GH₵ 0.00",
                "Current month's net payroll",
                2
        ));

        cards.add(createStatCard(
                "Payroll Status",
                "Not processed",
                "Current payroll period",
                3
        ));

        return cards;
    }

    private JPanel createStatCard(
            String heading,
            String initialValue,
            String description,
            int cardType) {

        JPanel card = new JPanel();

        card.setLayout(
                new BoxLayout(card, BoxLayout.Y_AXIS)
        );

        card.setBackground(Color.WHITE);

        card.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 238)
                        ),
                        new EmptyBorder(18, 18, 18, 18)
                )
        );

        JLabel headingLabel = new JLabel(heading);

        headingLabel.setFont(
                new Font("Arial", Font.PLAIN, 14)
        );

        headingLabel.setForeground(MUTED);

        JLabel valueLabel = new JLabel(initialValue);

        valueLabel.setFont(
                new Font("Arial", Font.BOLD, 23)
        );

        valueLabel.setForeground(TEXT);

        JLabel descriptionLabel = new JLabel(description);

        descriptionLabel.setFont(
                new Font("Arial", Font.PLAIN, 12)
        );

        descriptionLabel.setForeground(
                new Color(130, 140, 155)
        );

        if (cardType == 1) {

            employeeCountLabel = valueLabel;
            employeeDescriptionLabel = descriptionLabel;

        } else if (cardType == 2) {

            monthlyPayrollLabel = valueLabel;
            payrollDescriptionLabel = descriptionLabel;

        } else if (cardType == 3) {

            payrollStatusLabel = valueLabel;
            statusDescriptionLabel = descriptionLabel;
        }

        card.add(headingLabel);
        card.add(Box.createVerticalStrut(12));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(descriptionLabel);

        return card;
    }

    // RECENT PAYROLL ACTIVITY

    private JPanel createActivitySection() {

        JPanel section = new JPanel(
                new BorderLayout(0, 15)
        );

        section.setBackground(Color.WHITE);

        section.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(
                                new Color(225, 230, 238)
                        ),
                        new EmptyBorder(20, 20, 20, 20)
                )
        );

        JLabel heading = new JLabel("Recent Payroll Activity");

        heading.setFont(
                new Font("Arial", Font.BOLD, 18)
        );

        heading.setForeground(TEXT);

        String[] columns = {
                "Payroll Period",
                "Employee",
                "Net Amount",
                "Status"
        };

        activityTableModel = new DefaultTableModel(
                columns,
                0
        ) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable table = new JTable(activityTableModel);

        table.setRowHeight(35);
        table.setFont(new Font("Arial", Font.PLAIN, 13));

        table.getTableHeader().setFont(
                new Font("Arial", Font.BOLD, 13)
        );

        table.setFillsViewportHeight(true);

        JScrollPane tableScroll = new JScrollPane(table);

        tableScroll.setPreferredSize(
                new Dimension(700, 180)
        );

        section.add(heading, BorderLayout.NORTH);
        section.add(tableScroll, BorderLayout.CENTER);

        return section;
    }

    // LOAD DATABASE STATISTICS

    private void refreshDashboardData() {

        try {

            refreshCompanyInformation();

            List<Employee> employees =
                    employeeRepository.findAll();

            employeeCountLabel.setText(
                    String.valueOf(employees.size())
            );

            employeeDescriptionLabel.setText(
                    employees.size() == 1
                            ? "Registered employee"
                            : "Registered employees"
            );

            LocalDate today = LocalDate.now();

            List<Payroll> currentMonthPayroll =
                    payrollRepository.findByPeriod(
                            today.getMonthValue(),
                            today.getYear()
                    );

            BigDecimal totalNetPayroll = BigDecimal.ZERO;

            int pendingPayments = 0;

            for (Payroll payroll : currentMonthPayroll) {

                if (payroll.getNetSalary() != null) {

                    totalNetPayroll = totalNetPayroll.add(
                            payroll.getNetSalary()
                    );
                }

                if ("PENDING".equalsIgnoreCase(
                        payroll.getPaymentStatus())) {

                    pendingPayments++;
                }
            }

            monthlyPayrollLabel.setText(
                    formatCurrency(totalNetPayroll)
            );

            payrollDescriptionLabel.setText(
                    "Net payroll for "
                    + today.format(
                            DateTimeFormatter.ofPattern(
                                    "MMMM yyyy",
                                    Locale.ENGLISH
                            )
                    )
            );

            if (currentMonthPayroll.isEmpty()) {

                payrollStatusLabel.setText("Not processed");

                statusDescriptionLabel.setText(
                        "No payroll records this month"
                );

            } else if (pendingPayments > 0) {

                payrollStatusLabel.setText("Pending");

                statusDescriptionLabel.setText(
                        pendingPayments
                        + " payment(s) awaiting confirmation"
                );

            } else {

                payrollStatusLabel.setText("Completed");

                statusDescriptionLabel.setText(
                        "All current payroll payments are marked paid"
                );
            }

            loadRecentPayrollActivity();

        } catch (Exception e) {

            System.err.println(
                    "Could not refresh dashboard: "
                    + e.getMessage()
            );

            e.printStackTrace();

            employeeCountLabel.setText("Error");
            monthlyPayrollLabel.setText("Error");
            payrollStatusLabel.setText("Error");

            employeeDescriptionLabel.setText(
                    "Could not load employee records"
            );

            payrollDescriptionLabel.setText(
                    "Could not load payroll records"
            );

            statusDescriptionLabel.setText(
                    "Check the database connection"
            );
        }
    }

    // LOAD RECENT PAYROLL RECORDS

    private void loadRecentPayrollActivity()
            throws Exception {

        activityTableModel.setRowCount(0);

        List<Payroll> records =
                payrollRepository.findAll();

        int limit = Math.min(records.size(), 8);

        for (int i = 0; i < limit; i++) {

            Payroll payroll = records.get(i);

            String period = String.format(
                    Locale.ROOT,
                    "%02d/%04d",
                    payroll.getPayMonth(),
                    payroll.getPayYear()
            );

            activityTableModel.addRow(new Object[]{
                    period,
                    payroll.getEmployeeName(),
                    formatCurrency(payroll.getNetSalary()),
                    payroll.getPaymentStatus()
            });
        }
    }

    // CURRENCY FORMATTING

    private String formatCurrency(BigDecimal amount) {

        if (amount == null) {
            amount = BigDecimal.ZERO;
        }

        return String.format(
                Locale.US,
                "GH₵ %,.2f",
                amount
        );
    }
}