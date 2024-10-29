package BudgetWebsite;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Budget {
    private Map<String, MonthlyBudget> monthlyBudgets;

    public Budget() {
        monthlyBudgets = new HashMap<>();
        // Initialize the budget for the default month
        monthlyBudgets.put("January", new MonthlyBudget());
    }

    public void addIncome(double amount, String category, String month) {
        MonthlyBudget monthlyBudget = getOrCreateMonthlyBudget(month);
        monthlyBudget.addIncome(amount, category);
    }

    public void addExpense(double amount, String category, String month) {
        MonthlyBudget monthlyBudget = getOrCreateMonthlyBudget(month);
        monthlyBudget.addExpense(amount, category);
    }

    public double getBalance(String month) {
        return getOrCreateMonthlyBudget(month).getBalance();
    }

    public double getTotalIncome(String month) {
        return getOrCreateMonthlyBudget(month).getTotalIncome();
    }

    public void printTransactions(String month) {
        getOrCreateMonthlyBudget(month).printTransactions();
    }

    public void printCategoryExpenses(String month) {
        getOrCreateMonthlyBudget(month).printCategoryExpenses();
    }

    private MonthlyBudget getOrCreateMonthlyBudget(String month) {
        return monthlyBudgets.computeIfAbsent(month, k -> new MonthlyBudget());
    }

    private class MonthlyBudget {
        private double income;
        private double expenses;
        private ArrayList<Transaction> transactions;
        private Map<String, Double> categoryExpenses;

        public MonthlyBudget() {
            this.income = 0;
            this.expenses = 0;
            this.transactions = new ArrayList<>();
            this.categoryExpenses = new HashMap<>();
        }

        public void addIncome(double amount, String category) {
            if (amount < 0) {
                System.out.println("Income amount must be non-negative.");
                return;
            }
            this.income += amount;
            transactions.add(new Transaction("Income", amount, "Income added", category));
        }

        public void addExpense(double amount, String category) {
            if (amount < 0) {
                System.out.println("Expense amount must be non-negative.");
                return;
            }
            this.expenses += amount;
            transactions.add(new Transaction("Expense", amount, "Expense added", category));

            // Track expenses by category
            categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + amount);
        }

        public double getBalance() {
            return income - expenses;
        }

        public double getTotalIncome() {
            return income;
        }

        public void printTransactions() {
            for (Transaction transaction : transactions) {
                System.out.println(transaction);
            }
        }

        public void printCategoryExpenses() {
            System.out.println("Total Income: $" + getTotalIncome());
            System.out.println("Expenses by Category:");
            for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
                System.out.println(entry.getKey() + ": $" + entry.getValue());
            }
        }
    }
}

class Transaction {
    private String type;
    private double amount;
    private String description;
    private String category;

    public Transaction(String type, double amount, String description, String category) {
        this.type = type;
        this.amount = amount;
        this.description = description;
        this.category = category;
    }

    @Override
    public String toString() {
        return type + ": $" + amount + " (" + description + (category != null ? ", Category: " + category : "") + ")";
    }
}
