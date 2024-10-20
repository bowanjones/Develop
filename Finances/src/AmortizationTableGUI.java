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
    private JTable amortizationTable;

    public AmortizationTableGUI() {
        setTitle("Amortization Table Calculator");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Input panel
        JPanel inputPanel = new JPanel(new GridLayout(4, 2));
        inputPanel.add(new JLabel("Principal Amount:"));
        principalField = new JTextField();
        inputPanel.add(principalField);

        inputPanel.add(new JLabel("Annual Interest Rate (%):"));
        interestRateField = new JTextField();
        inputPanel.add(interestRateField);

        inputPanel.add(new JLabel("Term (Years):"));
        termField = new JTextField();
        inputPanel.add(termField);

        JButton calculateButton = new JButton("Calculate Amortization Schedule");
        calculateButton.addActionListener(new CalculateButtonListener());
        inputPanel.add(calculateButton);

        JButton saveButton = new JButton("Save to CSV");
        saveButton.addActionListener(new SaveButtonListener());
        inputPanel.add(saveButton);

        add(inputPanel, BorderLayout.NORTH);

        // Table setup
        String[] columnNames = {"Payment#", "Principal", "Interest", "Total Payment", "Remaining Balance"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        amortizationTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(amortizationTable);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    private class CalculateButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                double principal = Double.parseDouble(principalField.getText());
                float annualInterestRate = Float.parseFloat(interestRateField.getText()) / 100;
                int termInYears = Integer.parseInt(termField.getText());

                float monthlyInterestRate = annualInterestRate / MONTHS_IN_YEAR;
                int numberOfPayments = termInYears * MONTHS_IN_YEAR;

                double monthlyPayment = principal * (
                        (monthlyInterestRate * Math.pow(1 + monthlyInterestRate, numberOfPayments)) /
                                (Math.pow(1 + monthlyInterestRate, numberOfPayments) - 1)
                );

                DefaultTableModel model = (DefaultTableModel) amortizationTable.getModel();
                model.setRowCount(0); // Clear existing rows

                double remainingBalance = principal;

                for (int paymentNumber = 1; paymentNumber <= numberOfPayments; paymentNumber++) {
                    double interest = remainingBalance * monthlyInterestRate;
                    double principalPayment = monthlyPayment - interest;
                    remainingBalance -= principalPayment;

                    model.addRow(new Object[]{
                            paymentNumber,
                            NumberFormat.getCurrencyInstance().format(principalPayment),
                            NumberFormat.getCurrencyInstance().format(interest),
                            NumberFormat.getCurrencyInstance().format(monthlyPayment),
                            NumberFormat.getCurrencyInstance().format(Math.max(0, remainingBalance))
                    });
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(AmortizationTableGUI.this, "Please enter valid numeric values.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
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
