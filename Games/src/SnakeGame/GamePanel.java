package SnakeGame;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Random;

import javax.swing.JPanel;

public class GamePanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;

    static final int WIDTH = 500;
    static final int HEIGHT = 500;
    static final int UNIT_SIZE = 20;
    static final int NUMBER_OF_UNITS = (WIDTH * HEIGHT) / (UNIT_SIZE * UNIT_SIZE);

    // hold x and y coordinates for body parts of the snake
    final int x[] = new int[NUMBER_OF_UNITS];
    final int y[] = new int[NUMBER_OF_UNITS];

    // initial length of the snake
    int length = 5;
    int foodEaten;
    int foodX;
    int foodY;
    char direction = 'D';
    boolean running = false;
    Random random;
    Timer timer;
    JButton playAgainButton; // I'm adding for a play again option

    GamePanel() {
        random = new Random();
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.DARK_GRAY);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());

        // Initializing the Play again Button
        playAgainButton = new JButton("Play Again");
        playAgainButton.setBounds((WIDTH - 150) / 2, HEIGHT / 2 + 50, 150, 30); // Center it below the score
        playAgainButton.setVisible(false); // Hide it initially
        playAgainButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetGame();
            }
        });
        this.setLayout(null); // Required to set bounds for the button
        this.add(playAgainButton);

        play();
    }

    public void play() {
        // Starting snake position initialized
        x[0] = WIDTH / 2; // Start in the center of the panel
        y[0] = HEIGHT / 2;
        for (int i = 1; i < length; i++) {
            x[i] = x[0]; // Initialize body parts to the head's position
            y[i] = y[0];
        }

        addFood();
        running = true;

        timer = new Timer(80, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        draw(graphics);
    }

    public void move() {
        for (int i = length; i > 0; i--) {
            // Shift the snake one unit to the desired direction to create a move
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        if (direction == 'L') {
            x[0] = x[0] - UNIT_SIZE;
        } else if (direction == 'R') {
            x[0] = x[0] + UNIT_SIZE;
        } else if (direction == 'U') {
            y[0] = y[0] - UNIT_SIZE;
        } else {
            y[0] = y[0] + UNIT_SIZE;
        }
    }

    public void checkFood() {
        /* Debugging output
        System.out.println("Head Position: (" + x[0] + ", " + y[0] + ")");
        System.out.println("Food Position: (" + foodX + ", " + foodY + ")");
        */

        // Check if the head is close enough to the food to count as "eaten"
        if (Math.abs(x[0] - foodX) < UNIT_SIZE && Math.abs(y[0] - foodY) < UNIT_SIZE) {
            length++;
            foodEaten++;
            System.out.println("Food eaten! New score: " + foodEaten); // Debugging output
            addFood(); // Add new food
        }
    }

    public void draw(Graphics graphics) {
        if (running) {
            graphics.setColor(new Color(210, 115, 90));
            graphics.fillOval(foodX, foodY, UNIT_SIZE, UNIT_SIZE);

            graphics.setColor(Color.WHITE);
            graphics.fillRect(x[0], y[0], UNIT_SIZE, UNIT_SIZE);

            for (int i = 1; i < length; i++) {
                graphics.setColor(new Color(40, 200, 150));
                graphics.fillRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE);
            }

            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font("Sans serif", Font.ROMAN_BASELINE, 25));
            FontMetrics metrics = getFontMetrics(graphics.getFont());
            graphics.drawString("Score: " + foodEaten, (WIDTH - metrics.stringWidth("Score: " + foodEaten)) / 2, graphics.getFont().getSize());

        } else {
            gameOver(graphics);
        }
    }

    public void addFood() {
        boolean onSnake;
        do {
            foodX = random.nextInt((WIDTH / UNIT_SIZE)) * UNIT_SIZE;
            foodY = random.nextInt((HEIGHT / UNIT_SIZE)) * UNIT_SIZE;

            onSnake = false;
            for (int i = 0; i < length; i++) {
                if (foodX == x[i] && foodY == y[i]) {
                    onSnake = true;
                    break;
                }
            }
        } while (onSnake); // Ensure food doesn't spawn on the snake
    }

    public void checkHit() {
        // Check if head run into its body
        for (int i = length; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) {
                running = false;
            }
        }

        // Check if head run into walls
        if (x[0] < 0 || x[0] >= WIDTH || y[0] < 0 || y[0] >= HEIGHT) {
            running = false;
        }

        if (!running) {
            timer.stop();
        }
    }

    public void gameOver(Graphics graphics) {
        graphics.setColor(Color.red);
        graphics.setFont(new Font("Sans serif", Font.ROMAN_BASELINE, 50));
        FontMetrics metrics = getFontMetrics(graphics.getFont());
        graphics.drawString("Game Over", (WIDTH - metrics.stringWidth("Game Over")) / 2, HEIGHT / 2);

        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Sans serif", Font.ROMAN_BASELINE, 25));
        metrics = getFontMetrics(graphics.getFont());
        graphics.drawString("Score: " + foodEaten, (WIDTH - metrics.stringWidth("Score: " + foodEaten)) / 2, graphics.getFont().getSize());

        playAgainButton.setVisible(true); // Show the button when game is over to offer another try
    }

    // Play Again Option
    public void resetGame() {
        length = 5;
        foodEaten = 0;
        direction = 'D';
        running = false;
        play(); // Call play to start the game again
        playAgainButton.setVisible(false); // Hide the button again
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        if (running) {
            move();
            checkFood();
            checkHit();
        }
        repaint();
    }

    public class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R') {
                        direction = 'L';
                    }
                    break;

                case KeyEvent.VK_RIGHT:
                    if (direction != 'L') {
                        direction = 'R';
                    }
                    break;

                case KeyEvent.VK_UP:
                    if (direction != 'D') {
                        direction = 'U';
                    }
                    break;

                case KeyEvent.VK_DOWN:
                    if (direction != 'U') {
                        direction = 'D';
                    }
                    break;
                case KeyEvent.VK_ENTER:
                    if (!running) { // Only allow enter when not playing
                        resetGame();
                    }
                    break;
            }
        }
    }
}
