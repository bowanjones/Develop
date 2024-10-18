package RockPaperScissors;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class RockPaperScissors {
    private static String[] rps = {"Rock", "Paper", "Scissors"};
    private static Random random = new Random();
    private static int playerScore = 0;
    private static int computerScore = 0;
    private static int tieScore = 0;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Rock Paper Scissors");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        JLabel playerLabel = new JLabel("Choose your move:");
        playerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 3;
        frame.add(playerLabel, gbc);

        // Load icons using relative paths and resize them
        JButton rockButton = new JButton(resizeImage("rock.png", 80, 80));
        JButton paperButton = new JButton(resizeImage("paper.png", 80, 80));
        JButton scissorsButton = new JButton(resizeImage("scissors.png", 80, 80));

        gbc.gridwidth = 1;
        gbc.gridx = 0; gbc.gridy = 1;
        frame.add(rockButton, gbc);
        gbc.gridx = 1;
        frame.add(paperButton, gbc);
        gbc.gridx = 2;
        frame.add(scissorsButton, gbc);

        JLabel resultLabel = new JLabel("");
        resultLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 3;
        frame.add(resultLabel, gbc);

        JLabel scoreLabel = new JLabel("Score - You: 0 | Computer: 0 | Ties: 0");
        scoreLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        gbc.gridy = 3;
        frame.add(scoreLabel, gbc);

        JButton playAgainButton = new JButton("Play Again");
        playAgainButton.setVisible(false);
        playAgainButton.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridy = 4;
        frame.add(playAgainButton, gbc);

        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String playerMove = e.getActionCommand().split(" ")[0];
                String computerMove = rps[random.nextInt(rps.length)];
                String result;

                if (playerMove.equals(computerMove)) {
                    result = "The game was a tie!";
                    tieScore++;
                } else if ((playerMove.equals("Rock") && computerMove.equals("Scissors")) ||
                        (playerMove.equals("Paper") && computerMove.equals("Rock")) ||
                        (playerMove.equals("Scissors") && computerMove.equals("Paper"))) {
                    result = "You win!";
                    playerScore++;
                } else {
                    result = "You lose!";
                    computerScore++;
                }

                resultLabel.setText("Computer played: " + computerMove + ". " + result);
                scoreLabel.setText("Score - You: " + playerScore + " | Computer: " + computerScore + " | Ties: " + tieScore);
                rockButton.setEnabled(false);
                paperButton.setEnabled(false);
                scissorsButton.setEnabled(false);
                playAgainButton.setVisible(true);
            }
        };

        rockButton.addActionListener(actionListener);
        paperButton.addActionListener(actionListener);
        scissorsButton.addActionListener(actionListener);

        playAgainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resultLabel.setText("");
                rockButton.setEnabled(true);
                paperButton.setEnabled(true);
                scissorsButton.setEnabled(true);
                playAgainButton.setVisible(false);
            }
        });

        frame.setVisible(true);
    }

    private static ImageIcon resizeImage(String path, int width, int height) {
        ImageIcon icon = new ImageIcon(RockPaperScissors.class.getResource(path));
        Image img = icon.getImage();
        Image newImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(newImg);
    }
}
