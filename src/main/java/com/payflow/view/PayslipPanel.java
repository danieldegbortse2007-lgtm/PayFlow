
package com.payflow.view;

import com.payflow.model.Payroll;
import com.payflow.repository.PayrollRepository;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.print.PrinterException;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.time.Month;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public class PayslipPanel extends JPanel {

    private final PayrollRepository payrollRepository =
            new PayrollRepository();

    private final JTable payrollTable;
    private final DefaultTableModel tableModel;

    private List<Payroll> payrollRecords;

    private final DecimalFormat moneyFormat =
            new DecimalFormat("#,##0.00");

    public PayslipPanel() {

        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(245, 247, 250));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        JLabel heading = new JLabel("Payslip Management");
        heading.setFont(new Font("Arial", Font.BOLD, 25));

        JLabel subtitle = new JLabel(
                "Preview, print and download employee payslips."
        );

        subtitle.setForeground(Color.GRAY);

        JPanel header = new JPanel(new GridLayout(2, 1, 5, 5));
        header.setOpaque(false);
        header.add(heading);
        header.add(subtitle);

        add(header, BorderLayout.NORTH);

        String[] columns = {
                "Employee No.",
                "Employee Name",
                "Payroll Period",
                "Gross Salary (GH₵)",
                "Net Salary (GH₵)",
                "Status"
        };

        tableModel = new DefaultTableModel(columns, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        payrollTable = new JTable(tableModel);
        payrollTable.setRowHeight(30);

        payrollTable.setSelectionMode(
                ListSelectionModel.SINGLE_SELECTION
        );

        JScrollPane scrollPane = new JScrollPane(payrollTable);

        add(scrollPane, BorderLayout.CENTER);

        JButton refreshButton = new JButton("Refresh Records");
        JButton previewButton = new JButton("Preview Payslip");
        JButton printButton = new JButton("Print Payslip");
        JButton downloadButton = new JButton("Download PDF");

        downloadButton.setBackground(new Color(35, 150, 95));
        downloadButton.setForeground(Color.WHITE);
        downloadButton.setFocusPainted(false);

        JPanel buttons = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 5)
        );

        buttons.setOpaque(false);

        buttons.add(refreshButton);
        buttons.add(previewButton);
        buttons.add(printButton);
        buttons.add(downloadButton);

        add(buttons, BorderLayout.SOUTH);

        refreshButton.addActionListener(
                e -> loadPayrollRecords()
        );

        previewButton.addActionListener(
                e -> previewSelectedPayslip()
        );

        printButton.addActionListener(
                e -> printSelectedPayslip()
        );

        downloadButton.addActionListener(
                e -> downloadSelectedPayslip()
        );

        loadPayrollRecords();
    }

    // --------------------------------------------------
    // LOAD PAYROLL RECORDS
    // --------------------------------------------------

    private void loadPayrollRecords() {

        try {

            payrollRecords = payrollRepository.findAll();

            tableModel.setRowCount(0);

            for (Payroll payroll : payrollRecords) {

                String period = Month.of(payroll.getPayMonth())
                        .name() + " " + payroll.getPayYear();

                tableModel.addRow(new Object[]{

                        payroll.getEmployeeNumber(),
                        payroll.getEmployeeName(),
                        period,
                        formatMoney(payroll.getGrossSalary()),
                        formatMoney(payroll.getNetSalary()),
                        payroll.getPaymentStatus()
                });
            }

        } catch (SQLException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to load payslip records: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void refreshRecords() {
        loadPayrollRecords();
    }

    // --------------------------------------------------
    // SELECT PAYROLL
    // --------------------------------------------------

    private Payroll getSelectedPayroll() {

        int selectedRow = payrollTable.getSelectedRow();

        if (selectedRow == -1) {

            JOptionPane.showMessageDialog(
                    this,
                    "Please select a payroll record first."
            );

            return null;
        }

        int modelRow =
                payrollTable.convertRowIndexToModel(selectedRow);

        if (payrollRecords == null
                || modelRow >= payrollRecords.size()) {

            JOptionPane.showMessageDialog(
                    this,
                    "Unable to identify the selected payroll record."
            );

            return null;
        }

        return payrollRecords.get(modelRow);
    }

    // --------------------------------------------------
    // PREVIEW PAYSLIP
    // --------------------------------------------------

    private void previewSelectedPayslip() {

        Payroll payroll = getSelectedPayroll();

        if (payroll == null) {
            return;
        }

        JTextPane payslip = createPayslipDocument(payroll);

        payslip.setPreferredSize(new Dimension(550, 650));

        JScrollPane scrollPane = new JScrollPane(payslip);

        scrollPane.setPreferredSize(
                new Dimension(600, 650)
        );

        JOptionPane.showMessageDialog(
                this,
                scrollPane,
                "Payslip Preview",
                JOptionPane.PLAIN_MESSAGE
        );
    }

    // --------------------------------------------------
    // PRINT PAYSLIP
    // --------------------------------------------------

    private void printSelectedPayslip() {

        Payroll payroll = getSelectedPayroll();

        if (payroll == null) {
            return;
        }

        JTextPane payslip = createPayslipDocument(payroll);

        try {

            boolean printed = payslip.print();

            if (printed) {

                JOptionPane.showMessageDialog(
                        this,
                        "Payslip sent to the printer successfully."
                );
            }

        } catch (PrinterException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Printing failed: " + e.getMessage(),
                    "Printing Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // --------------------------------------------------
    // DOWNLOAD PAYSLIP AS PDF
    // --------------------------------------------------

    private void downloadSelectedPayslip() {

        Payroll payroll = getSelectedPayroll();

        if (payroll == null) {
            return;
        }

        String employeeName = payroll.getEmployeeName()
                .replaceAll("[^a-zA-Z0-9_-]", "_");

        String filename = "Payslip_"
                + employeeName
                + "_"
                + Month.of(payroll.getPayMonth()).name()
                + "_"
                + payroll.getPayYear()
                + ".pdf";

        JFileChooser fileChooser = new JFileChooser();

        fileChooser.setDialogTitle("Save Employee Payslip");

        fileChooser.setSelectedFile(
                new java.io.File(filename)
        );

        fileChooser.setFileFilter(
                new javax.swing.filechooser.FileNameExtensionFilter(
                        "PDF Documents (*.pdf)",
                        "pdf"
                )
        );

        int result = fileChooser.showSaveDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            return;
        }

        java.io.File file = fileChooser.getSelectedFile();

        if (!file.getName().toLowerCase().endsWith(".pdf")) {

            file = new java.io.File(
                    file.getAbsolutePath() + ".pdf"
            );
        }

        if (file.exists()) {

            int overwrite = JOptionPane.showConfirmDialog(
                    this,
                    "This file already exists. Do you want to replace it?",
                    "Confirm Replacement",
                    JOptionPane.YES_NO_OPTION
            );

            if (overwrite != JOptionPane.YES_OPTION) {
                return;
            }
        }

        try {

            generatePayslipPDF(payroll, file);

            JOptionPane.showMessageDialog(
                    this,
                    "Payslip downloaded successfully!\n\n"
                    + "Saved to:\n"
                    + file.getAbsolutePath(),
                    "PDF Download Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IOException e) {

            JOptionPane.showMessageDialog(
                    this,
                    "Could not generate PDF:\n" + e.getMessage(),
                    "PDF Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // --------------------------------------------------
    // GENERATE PDF DOCUMENT
    // --------------------------------------------------

    private void generatePayslipPDF(
            Payroll payroll,
            java.io.File file) throws IOException {

        try (PDDocument document = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);

            document.addPage(page);

            PDType1Font regularFont = new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA
            );

            PDType1Font boldFont = new PDType1Font(
                    Standard14Fonts.FontName.HELVETICA_BOLD
            );

            try (PDPageContentStream content =
                         new PDPageContentStream(document, page)) {

                float left = 55;
                float y = 780;

                // Header

                drawText(
                        content,
                        boldFont,
                        26,
                        left,
                        y,
                        "PAYFLOW",
                        new Color(23, 37, 84)
                );

                drawText(
                        content,
                        boldFont,
                        16,
                        380,
                        y + 5,
                        "EMPLOYEE PAYSLIP",
                        Color.DARK_GRAY
                );

                y -= 35;

                drawLine(content, left, y, 540, y);

                // Employee information

                y -= 35;

                drawSectionTitle(
                        content,
                        boldFont,
                        left,
                        y,
                        "EMPLOYEE INFORMATION"
                );

                y -= 25;

                y = drawField(
                        content, regularFont, boldFont,
                        left, y, "Employee Number",
                        payroll.getEmployeeNumber()
                );

                y = drawField(
                        content, regularFont, boldFont,
                        left, y, "Employee Name",
                        payroll.getEmployeeName()
                );

                String period = Month.of(payroll.getPayMonth())
                        .name() + " " + payroll.getPayYear();

                y = drawField(
                        content, regularFont, boldFont,
                        left, y, "Payroll Period",
                        period
                );

                y = drawField(
                        content, regularFont, boldFont,
                        left, y, "Payment Status",
                        payroll.getPaymentStatus()
                );

                y -= 15;

                drawLine(content, left, y, 540, y);

                // Earnings

                y -= 30;

                drawSectionTitle(
                        content,
                        boldFont,
                        left,
                        y,
                        "EARNINGS"
                );

                y -= 25;

                y = drawField(
                        content, regularFont, boldFont,
                        left, y, "Basic Salary",
                        formatMoney(payroll.getBasicSalary())
                );

                y = drawField(
                        content, regularFont, boldFont,
                        left, y, "Allowances",
                        formatMoney(payroll.getAllowances())
                );

                y = drawField(
                        content, regularFont, boldFont,
                        left, y, "Overtime",
                        formatMoney(payroll.getOvertime())
                );

                y = drawField(
                        content, regularFont, boldFont,
                        left, y, "Gross Salary",
                        formatMoney(payroll.getGrossSalary())
                );

                y -= 15;

                drawLine(content, left, y, 540, y);

                // Deductions

                y -= 30;

                drawSectionTitle(
                        content,
                        boldFont,
                        left,
                        y,
                        "DEDUCTIONS"
                );

                y -= 25;

                y = drawField(
                        content, regularFont, boldFont,
                        left, y, "Tax Deduction",
                        formatMoney(payroll.getTaxDeduction())
                );

                y = drawField(
                        content, regularFont, boldFont,
                        left, y, "SSNIT Deduction",
                        formatMoney(payroll.getSsnitDeduction())
                );

                y = drawField(
                        content, regularFont, boldFont,
                        left, y, "Other Deductions",
                        formatMoney(payroll.getOtherDeductions())
                );

                y = drawField(
                        content, regularFont, boldFont,
                        left, y, "Total Deductions",
                        formatMoney(payroll.getTotalDeductions())
                );

                y -= 20;

                drawLine(content, left, y, 540, y);

                // Net salary

                y -= 45;

                drawText(
                        content,
                        boldFont,
                        17,
                        left,
                        y,
                        "NET SALARY",
                        new Color(22, 101, 52)
                );

                drawText(
                        content,
                        boldFont,
                        19,
                        380,
                        y,
                        "GHS " + formatMoney(payroll.getNetSalary()),
                        new Color(22, 101, 52)
                );

                // Footer

                y -= 75;

                drawLine(content, left, y, 540, y);

                y -= 25;

                drawText(
                        content,
                        regularFont,
                        9,
                        150,
                        y,
                        "This is a computer-generated payslip.",
                        Color.GRAY
                );

                y -= 15;

                drawText(
                        content,
                        regularFont,
                        9,
                        190,
                        y,
                        "PAYFLOW PAYROLL MANAGEMENT SYSTEM",
                        Color.GRAY
                );
            }

            document.save(file);
        }
    }

    // --------------------------------------------------
    // PDF DRAWING HELPERS
    // --------------------------------------------------

    private float drawField(
            PDPageContentStream content,
            PDType1Font regularFont,
            PDType1Font boldFont,
            float x,
            float y,
            String label,
            String value) throws IOException {

        drawText(
                content,
                regularFont,
                11,
                x,
                y,
                label,
                Color.DARK_GRAY
        );

        drawText(
                content,
                boldFont,
                11,
                350,
                y,
                safePdfText(value),
                Color.BLACK
        );

        return y - 23;
    }

    private void drawSectionTitle(
            PDPageContentStream content,
            PDType1Font font,
            float x,
            float y,
            String title) throws IOException {

        drawText(
                content,
                font,
                13,
                x,
                y,
                title,
                new Color(48, 105, 220)
        );
    }

    private void drawText(
            PDPageContentStream content,
            PDType1Font font,
            float size,
            float x,
            float y,
            String text,
            Color color) throws IOException {

        content.beginText();

        content.setFont(font, size);

        content.setNonStrokingColor(color);

        content.newLineAtOffset(x, y);

        content.showText(safePdfText(text));

        content.endText();
    }

    private void drawLine(
            PDPageContentStream content,
            float x1,
            float y1,
            float x2,
            float y2) throws IOException {

        content.setStrokingColor(
                new Color(210, 215, 225)
        );

        content.setLineWidth(1);

        content.moveTo(x1, y1);
        content.lineTo(x2, y2);
        content.stroke();
    }

    private String safePdfText(String value) {

        if (value == null) {
            return "";
        }

        return value.replaceAll("[^\\x20-\\x7E]", "?");
    }

    // --------------------------------------------------
    // HTML PREVIEW DOCUMENT
    // --------------------------------------------------

    private JTextPane createPayslipDocument(Payroll payroll) {

        JTextPane document = new JTextPane();

        document.setContentType("text/html");
        document.setEditable(false);

        String period = Month.of(payroll.getPayMonth())
                .name() + " " + payroll.getPayYear();

        String html = """
                <html>
                <body style='font-family:Arial; padding:25px;'>
                    <h1 style='text-align:center;color:#172554;'>
                        PAYFLOW
                    </h1>

                    <h2 style='text-align:center;'>EMPLOYEE PAYSLIP</h2>
                    <hr>

                    <h3>Employee Information</h3>
                    <p><b>Employee Number:</b> %s</p>
                    <p><b>Employee Name:</b> %s</p>
                    <p><b>Payroll Period:</b> %s</p>
                    <p><b>Payment Status:</b> %s</p>

                    <hr>

                    <h3>Earnings</h3>
                    <p>Basic Salary: GHS %s</p>
                    <p>Allowances: GHS %s</p>
                    <p>Overtime: GHS %s</p>
                    <p><b>Gross Salary: GHS %s</b></p>

                    <hr>

                    <h3>Deductions</h3>
                    <p>Tax Deduction: GHS %s</p>
                    <p>SSNIT Deduction: GHS %s</p>
                    <p>Other Deductions: GHS %s</p>
                    <p><b>Total Deductions: GHS %s</b></p>

                    <hr>

                    <h2 style='text-align:center;color:#166534;'>
                        NET SALARY: GHS %s
                    </h2>

                    <br><br>

                    <p style='text-align:center;color:gray;'>
                        This is a computer-generated payslip.
                    </p>
                </body>
                </html>
                """.formatted(
                escapeHtml(payroll.getEmployeeNumber()),
                escapeHtml(payroll.getEmployeeName()),
                period,
                escapeHtml(payroll.getPaymentStatus()),
                formatMoney(payroll.getBasicSalary()),
                formatMoney(payroll.getAllowances()),
                formatMoney(payroll.getOvertime()),
                formatMoney(payroll.getGrossSalary()),
                formatMoney(payroll.getTaxDeduction()),
                formatMoney(payroll.getSsnitDeduction()),
                formatMoney(payroll.getOtherDeductions()),
                formatMoney(payroll.getTotalDeductions()),
                formatMoney(payroll.getNetSalary())
        );

        document.setText(html);
        document.setCaretPosition(0);

        return document;
    }

    // --------------------------------------------------
    // GENERAL HELPERS
    // --------------------------------------------------

    private String formatMoney(BigDecimal amount) {

        return moneyFormat.format(amount);
    }

    private String escapeHtml(String value) {

        if (value == null) {
            return "";
        }

        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
