package MortgageCalculator;

import java.text.NumberFormat;
import java.util.Scanner;

public class MortgageCalculator {

    private static final int MONTHS_IN_YEAR = 12;
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        //Principle amount of loan
        System.out.println("Enter the principal amount:");
        double principal = scanner.nextDouble();

        System.out.println("Enter the annual interest rate:");
        float annualInterestRate = scanner.nextFloat() / 100; //so we can type 4 instead of .04 percent

        System.out.println("Enter the term in years:");
        int termInYears = scanner.nextInt();

        //closing the scanner so it doesn't stay open to users input
        scanner.close();

        //Converting to months with nonchangeable final variable
        float monthlyInterestRate = annualInterestRate / MONTHS_IN_YEAR;

        // Initializing number of Payments until done
        int numberOfPayments = termInYears * MONTHS_IN_YEAR;

        double monthlyPayment = principal * (
                (monthlyInterestRate * (Math.pow(1 + monthlyInterestRate, numberOfPayments)))
                / // separating out the denominator from numerator to make it easier to read
                ((Math.pow(1 + monthlyInterestRate, numberOfPayments)) - 1)
        );
        System.out.println("The monthly payment is " + NumberFormat.getCurrencyInstance().format(monthlyPayment));
        System.out.println("The total amount paid is " + NumberFormat.getCurrencyInstance().format(monthlyPayment * numberOfPayments));
    }
}
