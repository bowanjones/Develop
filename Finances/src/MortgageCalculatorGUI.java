import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

public class MortgageCalculatorGUI extends JFrame {
    private static final int MONTHS_IN_YEAR = 12;

    private JTextField principalField;
    private JTextField interestRateField;
    private JTextField termField;
    private JTextArea resultArea;

    public MortgageCalculatorGUI() {
        setTitle("Mortgage Calculator");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(5, 2));

        // Input fields
        add(new JLabel("Principal Amount:"));
        principalField = new JTextField();
        add(principalField);

        add(new JLabel("Annual Interest Rate (%):"));
        interestRateField = new JTextField();
        add(interestRateField);

        add(new JLabel("Term (Years):"));
        termField = new JTextField();
        add(termField);

        JButton calculateButton = new JButton("Calculate");
        calculateButton.addActionListener(new CalculateButtonListener());
        add(calculateButton);

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        add(resultArea);

        setVisible(true);
    }

    private class CalculateButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            try {
                double principal = Double.parseDouble(principalField.getText());
                float annualInterestRate = Float.parseFloat(interestRateField.getText()) / 100;
                int termInYears = Integer.parseInt(termField.getText());

                // Monthly interest rate and number of payments
                float monthlyInterestRate = annualInterestRate / MONTHS_IN_YEAR;
                int numberOfPayments = termInYears * MONTHS_IN_YEAR;

                // Monthly payment calculation
                double monthlyPayment = principal * (
                        (monthlyInterestRate * Math.pow(1 + monthlyInterestRate, numberOfPayments)) /
                                (Math.pow(1 + monthlyInterestRate, numberOfPayments) - 1)
                );

                double totalPaid = monthlyPayment * numberOfPayments;

                // Display results
                resultArea.setText("Monthly Payment: " + NumberFormat.getCurrencyInstance().format(monthlyPayment) +
                        "\nTotal Amount Paid: " + NumberFormat.getCurrencyInstance().format(totalPaid));
            } catch (NumberFormatException ex) {
                resultArea.setText("Please enter valid numeric values.");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(MortgageCalculatorGUI::new);
    }
}
