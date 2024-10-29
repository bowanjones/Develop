package BudgetWebsite;

import java.util.Scanner;

public class BudgetTracker {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Budget budget = new Budget();
        String command;

        System.out.println("Welcome to your Budget Tracker!");

        do {
            System.out.println("Enter a command (1: Add Income, 2: Add Expense, 3: View Balance, 4: View Transactions, 5: View Category Expenses, 6: Exit):");
            command = scanner.nextLine();

            switch (command) {
                case "1":
                    addIncome(scanner, budget);
                    break;
                case "2":
                    addExpense(scanner, budget);
                    break;
                case "3":
                    System.out.println("Current balance: $" + budget.getBalance());
                    break;
                case "4":
                    budget.printTransactions();
                    break;
                case "5":
                    budget.printCategoryExpenses();
                    break;
                case "6":
                    System.out.println("Exiting Budget Tracker. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid command. Please try again.");
            }
        } while (!command.equals("6"));

        scanner.close();
    }

    private static void addIncome(Scanner scanner, Budget budget) {
        System.out.print("Enter income amount: ");
        if (scanner.hasNextDouble()) {
            double income = scanner.nextDouble();
            if (income >= 0) {
                System.out.print("Enter category for this income: ");
                scanner.nextLine(); // Clear the buffer
                String category = scanner.nextLine(); // Read category input
                budget.addIncome(income, category);
                System.out.println("Income added: $" + income + " in category: " + category);
            } else {
                System.out.println("Income must be non-negative.");
            }
        } else {
            System.out.println("Invalid input. Please enter a number.");
        }
    }

    private static void addExpense(Scanner scanner, Budget budget) {
        System.out.print("Enter expense amount: ");
        if (scanner.hasNextDouble()) {
            double expense = scanner.nextDouble();
            if (expense >= 0) {
                System.out.print("Enter category for this expense: ");
                scanner.nextLine(); // Clear the buffer
                String category = scanner.nextLine(); // Read category input
                budget.addExpense(expense, category);
                System.out.println("Expense added: $" + expense + " in category: " + category);
            } else {
                System.out.println("Expense must be non-negative.");
            }
        } else {
            System.out.println("Invalid input. Please enter a number.");
        }
    }
}
