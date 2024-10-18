//package RockPaperScissors;
//
//import java.util.Random;
//import java.util.Scanner;
//
//public class RockPaperScissors {
//    public static void main(String[] args) {
//
//        while (true) {
//            String[] rps = {"Rock", "Paper", "Scissors"};
//            String computerMove = rps[new Random().nextInt(rps.length)]; //returns 0, 1, 2 for r, p, s
//
//            Scanner scanner = new Scanner(System.in);
//            String playerMove;
//
//            while (true) {
//                System.out.println("Please enter your move (Rock r, Paper p, or Scissors s)");
//                playerMove = scanner.nextLine();
//                if (playerMove.equals("Rock") || playerMove.equals("Paper") || playerMove.equals("Scissors") || playerMove.equals("R") || playerMove.equals("P") || playerMove.equals("S")) {
//                    break;
//                }
//                System.out.println(playerMove + " is not a valid move. Please use r, p, s");
//            }
//
//            System.out.println("Computer played: " + computerMove);
//
//            if (playerMove.equals(computerMove)) {
//                System.out.println("The game was a tie!");
//            } else if (playerMove.equals("R")) {
//                if (computerMove.equals("P")) {
//                    System.out.println("You lose!");
//                } else if (computerMove.equals("S")) {
//                    System.out.println("You win!");
//                }
//            } else if (playerMove.equals("P")) {
//                if (computerMove.equals("R")) {
//                    System.out.println("You Win!");
//                } else if (computerMove.equals("S")) {
//                    System.out.println("You lose!");
//                }
//            } else if (playerMove.equals("S")) {
//                if (computerMove.equals("P")) {
//                    System.out.println("You Win!");
//                } else if (computerMove.equals("R")) {
//                    System.out.println("You lose!");
//                }
//            }
//
//            System.out.println("Play Again? (y/n)");
//            String playAgain = scanner.nextLine();
//
//            if (!playAgain.equals("y")) {
//                break;
//            }
//        }
//    }
//}