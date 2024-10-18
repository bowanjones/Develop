package MineSweeper;

import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

public class Minesweeper {
    private class MineTile extends JButton {
        int r;
        int c;
        boolean flagged = false; // Track flagged state

        public MineTile(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    int tileSize = 70;
    int numRows = 8;
    int numCols = numRows;
    int boardWidth = numCols * tileSize;
    int boardHeight = numRows * tileSize;

    JFrame frame = new JFrame("Minesweeper");
    JLabel textLabel = new JLabel();
    JPanel textPanel = new JPanel();
    JPanel boardPanel = new JPanel();

    JButton flagModeButton = new JButton("Toggle Flag Mode");
    JButton playAgainButton = new JButton("Play Again");
    boolean flagMode = false;

    int mineCount = 10;
    MineTile[][] board = new MineTile[numRows][numCols];
    ArrayList<MineTile> mineList;
    Random random = new Random();

    int tilesClicked = 0; // Goal is to click all tiles except the ones containing mines
    boolean gameOver = false;

    Minesweeper() {
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        textLabel.setFont(new Font("Arial", Font.BOLD, 25));
        textLabel.setHorizontalAlignment(JLabel.CENTER);
        textLabel.setText("Minesweeper: " + Integer.toString(mineCount));
        textLabel.setOpaque(true);

        textPanel.setLayout(new BorderLayout());
        textPanel.add(textLabel, BorderLayout.CENTER);

        // Flag mode button
        flagModeButton.setFont(new Font("Arial", Font.BOLD, 20));
        flagModeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                flagMode = !flagMode;
                if (flagMode) {
                    flagModeButton.setText("Select Mode");
                    textLabel.setText("Right Click to Flag 🚩");
                } else {
                    flagModeButton.setText("Flag Mode");
                    textLabel.setText("Minesweeper: " + mineCount);
                }
            }
        });
        textPanel.add(flagModeButton, BorderLayout.EAST);

        // Play Again button
        playAgainButton.setFont(new Font("Arial", Font.BOLD, 20));
        playAgainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });
        textPanel.add(playAgainButton, BorderLayout.SOUTH);

        frame.add(textPanel, BorderLayout.NORTH);

        boardPanel.setLayout(new GridLayout(numRows, numCols)); // 8x8
        frame.add(boardPanel);

        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                MineTile tile = new MineTile(r, c);
                board[r][c] = tile;

                tile.setFocusable(false);
                tile.setMargin(new Insets(0, 0, 0, 0));
                tile.setFont(new Font("Arial Unicode MS", Font.PLAIN, 45));
                tile.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if (gameOver) {
                            return;
                        }
                        MineTile tile = (MineTile) e.getSource();

                        // Left click
                        if (e.getButton() == MouseEvent.BUTTON1) {
                            if (!tile.flagged && tile.getText().isEmpty()) {
                                if (mineList.contains(tile)) {
                                    revealMines();
                                } else {
                                    checkMine(tile.r, tile.c);
                                }
                            }
                        }
                        // Right click
                        else if (e.getButton() == MouseEvent.BUTTON3) {
                            if (!tile.isEnabled()) {
                                return; // Can't flag an already revealed tile
                            }
                            if (flagMode) {
                                // Flagging logic
                                if (!tile.flagged) {
                                    tile.setForeground(Color.RED); // Set flag color to red
                                    tile.setText("🚩"); // Place a flag
                                    tile.flagged = true;
                                } else {
                                    // Unflagging logic
                                    tile.setText(""); // Remove the flag
                                    tile.flagged = false;
                                    tile.setForeground(Color.BLACK); // Reset text color
                                }
                                // Do nothing for selecting in flag mode
                                return;
                            }
                        }
                    }
                });

                boardPanel.add(tile);
            }
        }

        frame.setVisible(true);
        setMines();
    }

    void setMines() {
        mineList = new ArrayList<MineTile>();
        int mineLeft = mineCount;
        while (mineLeft > 0) {
            int r = random.nextInt(numRows); // 0-7
            int c = random.nextInt(numCols);

            MineTile tile = board[r][c];
            if (!mineList.contains(tile)) {
                mineList.add(tile);
                mineLeft -= 1;
            }
        }
    }

    void revealMines() {
        for (MineTile tile : mineList) {
            tile.setText("💣");
        }

        gameOver = true;
        textLabel.setText("Game Over!");
    }

    void checkMine(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) {
            return;
        }

        MineTile tile = board[r][c];
        if (!tile.isEnabled()) {
            return;
        }
        tile.setEnabled(false);
        tilesClicked += 1;

        int minesFound = 0;

        // Top 3
        minesFound += countMine(r - 1, c - 1);  // Top left
        minesFound += countMine(r - 1, c);      // Top
        minesFound += countMine(r - 1, c + 1);  // Top right

        // Left and right
        minesFound += countMine(r, c - 1);      // Left
        minesFound += countMine(r, c + 1);      // Right

        // Bottom 3
        minesFound += countMine(r + 1, c - 1);  // Bottom left
        minesFound += countMine(r + 1, c);      // Bottom
        minesFound += countMine(r + 1, c + 1);  // Bottom right

        if (minesFound > 0) {
            tile.setText(Integer.toString(minesFound));
        } else {
            tile.setText("");
            // Check surrounding tiles
            checkMine(r - 1, c - 1); // Top left
            checkMine(r - 1, c);     // Top
            checkMine(r - 1, c + 1); // Top right
            checkMine(r, c - 1);     // Left
            checkMine(r, c + 1);     // Right
            checkMine(r + 1, c - 1); // Bottom left
            checkMine(r + 1, c);     // Bottom
            checkMine(r + 1, c + 1); // Bottom right
        }

        if (tilesClicked == numRows * numCols - mineList.size()) {
            gameOver = true;
            textLabel.setText("Mines Cleared!");
        }
    }

    int countMine(int r, int c) {
        if (r < 0 || r >= numRows || c < 0 || c >= numCols) {
            return 0;
        }
        return mineList.contains(board[r][c]) ? 1 : 0;
    }

    void resetGame() {
        tilesClicked = 0;
        gameOver = false;
        mineList.clear();

        // Clear the board
        for (int r = 0; r < numRows; r++) {
            for (int c = 0; c < numCols; c++) {
                board[r][c].setText("");
                board[r][c].setEnabled(true);
                board[r][c].flagged = false; // Reset flagged state
                board[r][c].setForeground(Color.BLACK); // Reset text color
            }
        }

        // Reset the mines
        setMines();
        textLabel.setText("Minesweeper: " + mineCount);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Minesweeper());
    }
}
