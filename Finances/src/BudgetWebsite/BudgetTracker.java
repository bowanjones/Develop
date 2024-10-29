package BudgetWebsite;

import java.util.Scanner;

public class BudgetTracker {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Budget budget = new Budget();
        String command;

        System.out.println("Welcome to your Budget Tracker!");

        do {
            System.out.println("Enter a command (1: Add Income, 2: Add Expense, 3: View Balance, 4: View Transactions, 5: View Category Expenses, 6: Switch Month, 7: Exit):");
            command = scanner.nextLine();

            switch (command) {
                case "1":
                    addIncome(scanner, budget);
                    break;
                case "2":
                    addExpense(scanner, budget);
                    break;
                case "3":
                    viewBalance(scanner, budget);
                    break;
                case "4":
                    viewTransactions(scanner, budget);
                    break;
                case "5":
                    viewCategoryExpenses(scanner, budget);
                    break;
                case "6":
                    switchMonth(scanner, budget);
                    break;
                case "7":
                    System.out.println("Exiting Budget Tracker. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid command. Please try again.");
            }
        } while (!command.equals("7"));

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
                System.out.print("Enter month for this income: ");
                String month = scanner.nextLine(); // Read month input
                budget.addIncome(income, category, month);
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
                System.out.print("Enter month for this expense: ");
                String month = scanner.nextLine(); // Read month input
                budget.addExpense(expense, category, month);
                System.out.println("Expense added: $" + expense + " in category: " + category);
            } else {
                System.out.println("Expense must be non-negative.");
            }
        } else {
            System.out.println("Invalid input. Please enter a number.");
        }
    }

    private static void viewBalance(Scanner scanner, Budget budget) {
        System.out.print("Enter month to view balance: ");
        String month = scanner.nextLine(); // Read month input
        System.out.println("Current balance for " + month + ": $" + budget.getBalance(month));
    }

    private static void viewTransactions(Scanner scanner, Budget budget) {
        System.out.print("Enter month to view transactions: ");
        String month = scanner.nextLine(); // Read month input
        budget.printTransactions(month);
    }

    private static void viewCategoryExpenses(Scanner scanner, Budget budget) {
        System.out.print("Enter month to view category expenses: ");
        String month = scanner.nextLine(); // Read month input
        budget.printCategoryExpenses(month);
    }

    private static void switchMonth(Scanner scanner, Budget budget) {
        System.out.print("Enter the month to switch to (e.g., January, February): ");
        String month = scanner.nextLine().trim();
        // No additional functionality needed here, as operations are already based on the month.
        System.out.println("Switched to month: " + month);
    }
}
