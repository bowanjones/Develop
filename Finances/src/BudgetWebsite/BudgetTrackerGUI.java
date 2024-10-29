import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

public class BudgetTrackerGUI extends JFrame {
    private Map<String, Budget> budgets; // Store budgets for different months
    private String currentMonth; // Track the current month
    private JTextField amountField;
    private JTextField categoryField;
    private DefaultTableModel tableModel;
    private JTable table;
    private JLabel totalLabel;

    public BudgetTrackerGUI() {
        budgets = new HashMap<>();
        currentMonth = "January"; // Default month
        budgets.put(currentMonth, new Budget()); // Initialize budget for the default month

        setTitle("Budget Tracker");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Input Panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS)); // Change to BoxLayout

        inputPanel.add(new JLabel("Amount:"));
        amountField = new JTextField();
        inputPanel.add(amountField);

        inputPanel.add(Box.createVerticalStrut(10)); // Optional space

        inputPanel.add(new JLabel("Category:"));
        categoryField = new JTextField();
        inputPanel.add(categoryField);

        inputPanel.add(Box.createVerticalStrut(10)); // Optional space

        JButton addIncomeButton = new JButton("Add Income");
        addIncomeButton.setForeground(Color.WHITE);
        addIncomeButton.setBackground(new Color(0, 120, 0)); // Darker green
        inputPanel.add(addIncomeButton);

        inputPanel.add(Box.createVerticalStrut(10)); // Optional space

        JButton addExpenseButton = new JButton("Add Expense");
        addExpenseButton.setForeground(Color.WHITE);
        addExpenseButton.setBackground(new Color(150, 0, 0)); // Darker red
        inputPanel.add(addExpenseButton);

        inputPanel.add(Box.createVerticalStrut(10)); // Optional space

        // Month Switch Button
        JButton switchMonthButton = new JButton("Switch Month");
        switchMonthButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                switchMonth();
            }
        });
        inputPanel.add(switchMonthButton);

        add(inputPanel, BorderLayout.NORTH);

        // Table for displaying totals
        String[] columnNames = {"Category", "Amount"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make cells non-editable
            }
        };

        // Set the color renderer for the Amount column
        table.getColumnModel().getColumn(1).setCellRenderer(new ColorRenderer());

        add(new JScrollPane(table), BorderLayout.CENTER);

        // Total label at the bottom
        totalLabel = new JLabel("Total Balance: $0.00");
        add(totalLabel, BorderLayout.SOUTH);

        // Button Actions
        addIncomeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addIncome();
            }
        });

        addExpenseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addExpense();
            }
        });

        updateTotal();
    }

    private void switchMonth() {
        String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
        String month = (String) JOptionPane.showInputDialog(this, "Select Month:", "Month Selector", JOptionPane.PLAIN_MESSAGE, null, months, currentMonth);
        if (month != null && !month.isEmpty()) {
            currentMonth = month;
            budgets.putIfAbsent(currentMonth, new Budget()); // Initialize budget for the selected month if not present
            updateTable();
            updateTotal();
        }
    }

    private void addIncome() {
        try {
            double income = Double.parseDouble(amountField.getText());
            String category = categoryField.getText().trim();
            if (income >= 0) {
                budgets.get(currentMonth).addIncome(income, category);
                updateTable(category, income);
                updateTotal();
                amountField.setText("");
                categoryField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Income must be non-negative.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid income amount.");
        }
    }

    private void addExpense() {
        try {
            double expense = Double.parseDouble(amountField.getText());
            String category = categoryField.getText().trim();
            if (expense >= 0) {
                budgets.get(currentMonth).addExpense(expense, category);
                updateTable(category, -expense); // Use negative for expenses
                updateTotal();
                amountField.setText("");
                categoryField.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Expense must be non-negative.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid expense amount.");
        }
    }

    private void updateTable(String category, double amount) {
        boolean categoryExists = false;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(category)) {
                double currentTotal = (Double) tableModel.getValueAt(i, 1);
                tableModel.setValueAt(currentTotal + amount, i, 1); // Update total
                categoryExists = true;
                break;
            }
        }
        if (!categoryExists) {
            tableModel.addRow(new Object[]{category, amount});
        }
    }

    private void updateTable() {
        tableModel.setRowCount(0); // Clear existing data
        Map<String, Double> categoryTotals = budgets.get(currentMonth).getCategoryTotals();
        for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
            tableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }

    private void updateTotal() {
        double total = budgets.get(currentMonth).getBalance();
        totalLabel.setText("Total Balance: $" + String.format("%.2f", total));
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BudgetTrackerGUI tracker = new BudgetTrackerGUI();
            tracker.setVisible(true);
        });
    }
}

class Budget {
    private double income;
    private double expenses;
    private Map<String, Double> categoryTotals;

    public Budget() {
        income = 0;
        expenses = 0;
        categoryTotals = new HashMap<>();
    }

    public void addIncome(double amount, String category) {
        income += amount;
        categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) + amount);
    }

    public void addExpense(double amount, String category) {
        expenses += amount;
        categoryTotals.put(category, categoryTotals.getOrDefault(category, 0.0) - amount);
    }

    public double getBalance() {
        return income - expenses;
    }

    public Map<String, Double> getCategoryTotals() {
        return categoryTotals;
    }
}

class ColorRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (value instanceof Number) {
            if ((Double) value < 0) {
                setForeground(new Color(150, 0, 0)); // Darker red
            } else {
                setForeground(new Color(0, 120, 0)); // Darker green
            }
        }
        return this;
    }
}
