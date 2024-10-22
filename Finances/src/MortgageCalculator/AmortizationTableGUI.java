package MortgageCalculator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;

public class AmortizationTableGUI extends JFrame {
    private static final int MONTHS_IN_YEAR = 12;

    private JTextField principalField;
    private JTextField interestRateField;
    private JTextField termField;
    private JTextField extraPaymentField;
    private JTable amortizationTable;
    private JLabel totalPaidLabel;
    private JLabel termLabel;
    private JRadioButton monthlyPaymentButton;
    private JRadioButton oneTimePaymentButton;

    public AmortizationTableGUI() {
        setTitle("Amortization Table Calculator");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));

        principalField = createInputField(inputPanel, "Principal Amount:");
        interestRateField = createInputField(inputPanel, "Annual Interest Rate (%):");
        termField = createInputField(inputPanel, "Term (Years):");

        JPanel paymentTypePanel = new JPanel();
        monthlyPaymentButton = new JRadioButton("Monthly Extra Payment", true);
        oneTimePaymentButton = new JRadioButton("One-Time Extra Payment");
        ButtonGroup paymentTypeGroup = new ButtonGroup();
        paymentTypeGroup.add(monthlyPaymentButton);
        paymentTypeGroup.add(oneTimePaymentButton);
        paymentTypePanel.add(monthlyPaymentButton);
        paymentTypePanel.add(oneTimePaymentButton);
        inputPanel.add(paymentTypePanel);

        extraPaymentField = createInputField(inputPanel, "Extra Payment Amount:");

        JButton calculateButton = new JButton("Calculate Amortization Schedule");
        calculateButton.addActionListener(new CalculateButtonListener());
        inputPanel.add(calculateButton);

        JButton addExtraPaymentButton = new JButton("Add Extra Payment");
        addExtraPaymentButton.addActionListener(new AddExtraPaymentButtonListener());
        inputPanel.add(addExtraPaymentButton);

        JButton saveButton = new JButton("Save to CSV");
        saveButton.addActionListener(new SaveButtonListener());
        inputPanel.add(saveButton);

        totalPaidLabel = new JLabel("Total Paid: $0.00");
        termLabel = new JLabel("Term: 0 Years, 0 Months");
        inputPanel.add(totalPaidLabel);
        inputPanel.add(termLabel);

        add(inputPanel, BorderLayout.NORTH);

        // Table setup
        String[] columnNames = {"Payment#", "Principal", "Interest", "Total Payment", "Extra Monthly Payment", "Remaining Balance"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        amortizationTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(amortizationTable);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private JTextField createInputField(JPanel panel, String label) {
        panel.add(new JLabel(label));
        JTextField textField = new JTextField();
        panel.add(textField);
        return textField;
    }

    private class CalculateButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                double principal = Double.parseDouble(principalField.getText());
                float annualInterestRate = Float.parseFloat(interestRateField.getText()) / 100;
                int termInYears = Integer.parseInt(termField.getText());

                float monthlyInterestRate = annualInterestRate / MONTHS_IN_YEAR;
                int numberOfPayments = termInYears * MONTHS_IN_YEAR;

                DefaultTableModel model = (DefaultTableModel) amortizationTable.getModel();
                model.setRowCount(0); // Clear existing rows

                double monthlyPayment = principal * (
                        (monthlyInterestRate * Math.pow(1 + monthlyInterestRate, numberOfPayments)) /
                                (Math.pow(1 + monthlyInterestRate, numberOfPayments) - 1)
                );

                double remainingBalance = principal;
                double totalPaid = 0.0;

                for (int paymentNumber = 1; paymentNumber <= numberOfPayments; paymentNumber++) {
                    double interest = remainingBalance * monthlyInterestRate;
                    double principalPayment = monthlyPayment - interest; // Calculate principal payment
                    remainingBalance -= principalPayment;

                    totalPaid += monthlyPayment;

                    model.addRow(new Object[]{
                            paymentNumber,
                            NumberFormat.getCurrencyInstance().format(principalPayment),
                            NumberFormat.getCurrencyInstance().format(interest),
                            NumberFormat.getCurrencyInstance().format(monthlyPayment),
                            NumberFormat.getCurrencyInstance().format(0), // No extra payment initially
                            NumberFormat.getCurrencyInstance().format(Math.max(0, remainingBalance))
                    });
                }

                totalPaidLabel.setText("Total Paid: " + NumberFormat.getCurrencyInstance().format(totalPaid));
                termLabel.setText("Term: " + termInYears + " Years, " + (numberOfPayments % MONTHS_IN_YEAR) + " Months");

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(AmortizationTableGUI.this, "Please enter valid numeric values.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private class AddExtraPaymentButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String extraPaymentText = extraPaymentField.getText();
            if (extraPaymentText.isEmpty()) {
                JOptionPane.showMessageDialog(AmortizationTableGUI.this, "Please enter an extra payment amount.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double extraPayment = Double.parseDouble(extraPaymentText);
                if (monthlyPaymentButton.isSelected()) {
                    addMonthlyExtraPayment(extraPayment);
                } else {
                    addOneTimeExtraPayment(extraPayment);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(AmortizationTableGUI.this, "Invalid amount entered.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addMonthlyExtraPayment(double extraPayment) {
        DefaultTableModel model = (DefaultTableModel) amortizationTable.getModel();
        model.setRowCount(0); // Clear existing rows

        double principal = Double.parseDouble(principalField.getText());
        float annualInterestRate = Float.parseFloat(interestRateField.getText()) / 100;
        float monthlyInterestRate = annualInterestRate / MONTHS_IN_YEAR;

        double remainingBalance = principal;
        double totalPaid = 0.0;
        int paymentNumber = 0;

        // Calculate fixed monthly payment
        double monthlyPayment = principal * (
                (monthlyInterestRate * Math.pow(1 + monthlyInterestRate, Integer.parseInt(termField.getText()) * MONTHS_IN_YEAR)) /
                        (Math.pow(1 + monthlyInterestRate, Integer.parseInt(termField.getText()) * MONTHS_IN_YEAR) - 1)
        );

        while (remainingBalance > 0) {
            paymentNumber++;
            double interest = remainingBalance * monthlyInterestRate;

            double principalPayment = Math.min(monthlyPayment - interest + extraPayment, remainingBalance); // Ensure we do not exceed remaining balance
            remainingBalance -= principalPayment; // Subtract principal payment from remaining balance
            totalPaid += (monthlyPayment + extraPayment); // Include total payment in total paid

            model.addRow(new Object[]{
                    paymentNumber,
                    NumberFormat.getCurrencyInstance().format(principalPayment),
                    NumberFormat.getCurrencyInstance().format(interest),
                    NumberFormat.getCurrencyInstance().format(monthlyPayment + extraPayment),
                    NumberFormat.getCurrencyInstance().format(extraPayment), // Show the extra payment
                    NumberFormat.getCurrencyInstance().format(Math.max(0, remainingBalance))
            });
        }

        // Update total paid and term labels
        totalPaidLabel.setText("Total Paid: " + NumberFormat.getCurrencyInstance().format(totalPaid));
        int years = paymentNumber / MONTHS_IN_YEAR;
        int months = paymentNumber % MONTHS_IN_YEAR;
        termLabel.setText("Term: " + years + " Years, " + months + " Months");
    }


    private void addOneTimeExtraPayment(double extraPayment) {
        DefaultTableModel model = (DefaultTableModel) amortizationTable.getModel();
        model.setRowCount(0); // Clear existing rows

        double principal = Double.parseDouble(principalField.getText());
        float annualInterestRate = Float.parseFloat(interestRateField.getText()) / 100;
        float monthlyInterestRate = annualInterestRate / MONTHS_IN_YEAR;

        double remainingBalance = principal - extraPayment; // Apply the one-time extra payment immediately
        double totalPaid = extraPayment; // Start total paid with the extra payment
        int paymentNumber = 0;

        // Calculate fixed monthly payment
        double monthlyPayment = principal * (
                (monthlyInterestRate * Math.pow(1 + monthlyInterestRate, Integer.parseInt(termField.getText()) * MONTHS_IN_YEAR)) /
                        (Math.pow(1 + monthlyInterestRate, Integer.parseInt(termField.getText()) * MONTHS_IN_YEAR) - 1)
        );

        while (remainingBalance > 0) {
            paymentNumber++;
            double interest = remainingBalance * monthlyInterestRate;

            // For the first payment, include the extra payment, otherwise just the regular monthly payment
            double principalPayment = Math.min((paymentNumber == 1 ? monthlyPayment + extraPayment : monthlyPayment) - interest, remainingBalance);
            remainingBalance -= principalPayment; // Subtract principal payment from remaining balance

            totalPaid += (paymentNumber == 1 ? monthlyPayment + extraPayment : monthlyPayment); // Include total payment in total paid

            model.addRow(new Object[]{
                    paymentNumber,
                    NumberFormat.getCurrencyInstance().format(principalPayment),
                    NumberFormat.getCurrencyInstance().format(interest),
                    NumberFormat.getCurrencyInstance().format((paymentNumber == 1 ? monthlyPayment + extraPayment : monthlyPayment)),
                    NumberFormat.getCurrencyInstance().format((paymentNumber == 1 ? extraPayment : 0)), // Show extra payment only for the first payment
                    NumberFormat.getCurrencyInstance().format(Math.max(0, remainingBalance))
            });
        }

        // Update total paid and term labels
        totalPaidLabel.setText("Total Paid: " + NumberFormat.getCurrencyInstance().format(totalPaid));
        int years = paymentNumber / MONTHS_IN_YEAR;
        int months = paymentNumber % MONTHS_IN_YEAR;
        termLabel.setText("Term: " + years + " Years, " + months + " Months");
    }


    private class SaveButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Specify a file to save");

            // Set the default directory to the Downloads folder
            String downloadsPath = System.getProperty("user.home") + File.separator + "Downloads";
            fileChooser.setCurrentDirectory(new File(downloadsPath));

            int userSelection = fileChooser.showSaveDialog(AmortizationTableGUI.this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                saveToCSV(fileToSave);
            }
        }
    }

    private void saveToCSV(File file) {
        // Ensure the file has a .csv extension
        if (!file.getName().endsWith(".csv")) {
            file = new File(file.getAbsolutePath() + ".csv");
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            DefaultTableModel model = (DefaultTableModel) amortizationTable.getModel();

            // Write column names
            for (int i = 0; i < model.getColumnCount(); i++) {
                writer.write("\"" + model.getColumnName(i) + "\""); // Use quotes to preserve formatting
                if (i < model.getColumnCount() - 1) writer.write(",");
            }
            writer.newLine();

            // Write data
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    Object value = model.getValueAt(row, col);
                    String formattedValue;

                    if (value instanceof Number) {
                        formattedValue = currencyFormat.format(value); // Format as currency
                    } else {
                        formattedValue = value.toString(); // Convert other types to string
                    }

                    writer.write("\"" + formattedValue + "\""); // Use quotes to preserve formatting
                    if (col < model.getColumnCount() - 1) writer.write(",");
                }
                writer.newLine(); // New line for the next row
            }
            JOptionPane.showMessageDialog(this, "Table saved successfully!", "Save Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving file: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AmortizationTableGUI::new);
    }
}
