package games;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class Games extends JPanel implements KeyListener{

    private int x = 80;
    private int y = 80;
    private final int BOX_SIZE = 50;
    private int speed = 20;

    private boolean up, down, left, right;
    private boolean showText = false;
    private boolean canFly = false; 
    private Timer flightTimer; 

    private int yVelocity = 0;
    private int gravity = 2;
    private boolean isJumping = false;
    private int groundY = 1300;

    private ArrayList<Rectangle> obstacles = new ArrayList<>();
    private Rectangle collectible; 

    private int health = 3;
    private boolean isDead = false;
    private String answerText = "";

    private ArrayList<Rectangle> spikes = new ArrayList<>();
    private ArrayList<Integer> spikeDirections = new ArrayList<>();
    private int spikeSpeed = 10;

    private Rectangle yesBox;
    private Rectangle noBox;
    private boolean nextPage = false;

    private boolean showLevelQuestion = false;

    private int score = 0;
    private int timeLeft = 60;
    private Timer countdownTimer;

    private int level = 1;

    public Games() {
        setFocusable(true);
        addKeyListener(this);

        Timer timer = new Timer(20, e -> moveBox());
        timer.start();

        Timer showTextTimer = new Timer(2000, e -> {
            showText = true;
            repaint();
            ((Timer) e.getSource()).stop();
        });
        showTextTimer.setRepeats(false);
        showTextTimer.start();

        yesBox = new Rectangle(600, 170, 60, 30);  
        noBox = new Rectangle(720, 170, 60, 30);  
        countdownTimer = new Timer(1000, e -> {
            if (timeLeft > 0) {
                timeLeft--;
            } else {
                if (!nextPage) {
                    health--;
                    if (health <= 0) {
                        isDead = true;
                        answerText = "Time's up! - No health left";
                    } else {
                        answerText = "Time's up! -1 health";
                        respawnPlayerPartial();
                    }
                }
                timeLeft = 0;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });
        countdownTimer.start();

        loadLevel(level);

        requestFocusInWindow();
    }

    private void loadLevel(int lvl) {
        x = 80;
        y = 80;
        yVelocity = 0;
        isJumping = false;
        canFly = false;
        if (flightTimer != null) flightTimer.stop();

        showText = false;
        nextPage = false;
        answerText = "";
        timeLeft = 60;
        countdownTimer.start();

        obstacles.clear();
        spikes.clear();
        spikeDirections.clear();
        collectible = null;
        showLevelQuestion = true; 

        yesBox = new Rectangle(600, 170, 60, 30);
        noBox = new Rectangle(720, 170, 60, 30);

        switch (lvl) {
            case 1:
                obstacles.add(new Rectangle(300, 300, 200, 50));
                obstacles.add(new Rectangle(600, 500, 250, 50));
                obstacles.add(new Rectangle(80, 700, 900, 50));
                collectible = new Rectangle(1000, 450, 30, 30);
                spikes.add(new Rectangle(500, 200, 50, 50));
                spikeDirections.add(1);
                break;
            case 2:
                obstacles.add(new Rectangle(400, 400, 300, 50));
                obstacles.add(new Rectangle(100, 700, 300, 50));
                obstacles.add(new Rectangle(300, 200, 300, 50));
                collectible = new Rectangle(1000, 450, 30, 30);
                spikes.add(new Rectangle(300, 300, 50, 50));
                spikeDirections.add(1);
                break;
            case 3:
                obstacles.add(new Rectangle(200, 300, 200, 50));
                obstacles.add(new Rectangle(500, 600, 300, 50));
                collectible = new Rectangle(1100, 500, 30, 30);
                spikes.add(new Rectangle(600, 400, 50, 50));
                spikeDirections.add(1);
                break;
            case 4:
                obstacles.add(new Rectangle(100, 400, 300, 50));
                obstacles.add(new Rectangle(700, 500, 200, 50));
                collectible = new Rectangle(900, 300, 30, 30);
                spikes.add(new Rectangle(400, 200, 50, 50));
                spikeDirections.add(1);
                break;
            case 5:
                obstacles.add(new Rectangle(300, 300, 400, 50));
                obstacles.add(new Rectangle(600, 600, 250, 50));
                collectible = new Rectangle(1000, 400, 30, 30);
                spikes.add(new Rectangle(500, 350, 50, 50));
                spikeDirections.add(1);
                break;
            default:
                obstacles.clear();
                collectible = null;
                spikes.clear();
                answerText = "YOU WON THE GAME!";
                isDead = true;
                showLevelQuestion = false;
                break;
        }
    }

    private void moveBox() {
        if (isDead) {
            repaint();
            return;
        }

        int nextX = x;
        int nextY = y;
        if (left) nextX -= speed;
        if (right) nextX += speed;
        if (canFly) {
            if (up) nextY -= speed;
            if (down) nextY += speed;
        } else {
            if (isJumping) {
                yVelocity += gravity;
                nextY += yVelocity;
            } else {
                if (y < groundY) {
                    yVelocity += gravity;
                    nextY += yVelocity;
                    isJumping = true;
                }
            }
        }

        Rectangle nextPlayerX = new Rectangle(nextX, y, BOX_SIZE, BOX_SIZE);
        boolean horizontalCollision = false;
        for (Rectangle o : obstacles) {
            if (nextPlayerX.intersects(o)) {
                horizontalCollision = true;
                break;
            }
        }
        if (!horizontalCollision) {
            x = nextX;
        }

        if (!canFly) {
            Rectangle nextPlayerY = new Rectangle(x, nextY, BOX_SIZE, BOX_SIZE);
            boolean verticalCollision = false;
            for (Rectangle o : obstacles) {
                if (nextPlayerY.intersects(o)) {
                    verticalCollision = true;

                    if (y + BOX_SIZE <= o.y) {
                        y = o.y - BOX_SIZE;
                        isJumping = false;
                        yVelocity = 0;
                    } else if (y >= o.y + o.height) {
                        y = o.y + o.height;
                        yVelocity = 0;
                    } else {
                        nextY = y;
                    }
                    break;
                }
            }

            if (!verticalCollision) {
                y = nextY;
            }

            if (y >= groundY) {
                y = groundY;
                isJumping = false;
                yVelocity = 0;
            }
        } else {
            y = nextY;
        }

        // Collectible for flying
        if (collectible != null && new Rectangle(x, y, BOX_SIZE, BOX_SIZE).intersects(collectible)) {
            canFly = true;
            score += 100;
            collectible = null;

            if (flightTimer != null) flightTimer.stop();

            flightTimer = new Timer(20000, e -> {
                canFly = false; 
                ((Timer) e.getSource()).stop();
            });
            flightTimer.setRepeats(false);
            flightTimer.start();
        }

        // Spikes collision
        for (int i = 0; i < spikes.size(); i++) {
            Rectangle spike = spikes.get(i);
            if (new Rectangle(x, y, BOX_SIZE, BOX_SIZE).intersects(spike)) {
                health--;
                if (health <= 0) {
                    isDead = true;
                    answerText = "You hit a spike! - No health left";
                } else {
                    answerText = "You hit a spike! -1 health";
                    respawnPlayerPartial();
                }
                repaint();
                return;
            }
        }

        // Move spikes
        for (int i = 0; i < spikes.size(); i++) {
            Rectangle spike = spikes.get(i);
            int dir = spikeDirections.get(i);
            spike.x += spikeSpeed * dir;
            if (spike.x < 0 || spike.x + spike.width > getWidth()) {
                spikeDirections.set(i, dir * -1);
            }
        }

        // Yes/No level choice
        if (showLevelQuestion && !nextPage) {
            Rectangle player = new Rectangle(x, y, BOX_SIZE, BOX_SIZE);
            if (player.intersects(yesBox) || player.intersects(noBox)) {
                answerText = player.intersects(yesBox) ? "You chose YES!" : "You chose NO!";
                score++; // Gain 1 point for completing level
                showLevelQuestion = false;
                level++;
                loadLevel(level);
            }
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.RED);
        for (int i = 0; i < health; i++) {
            g.fillOval(20 + i*35, 70, 30, 30);
        }

        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        g.drawString("Score: " + score, 20, 120);
        g.drawString("Time: " + timeLeft, 20, 160);
        g.drawString("Level: " + level, 20, 200);

        if (isDead) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 60));
            g.drawString("GAME OVER", 500, 300);

            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.drawString(answerText, 520, 360);
            g.drawString("Press R to Respawn", 520, 420);
            g.drawString("Press E to go back to start", 520, 460);
            return;
        }

        g.setColor(Color.RED);
        g.fillRect(x, y, BOX_SIZE, BOX_SIZE);

        g.setColor(Color.GRAY);
        for (Rectangle r : obstacles) {
            g.fillRect(r.x, r.y, r.width, r.height);
        }

        g.setColor(Color.BLUE);
        for (Rectangle s : spikes) {
            g.fillRect(s.x, s.y, s.width, s.height);
        }

        if (collectible != null) {
            g.setColor(Color.YELLOW);
            g.fillOval(collectible.x, collectible.y, collectible.width, collectible.height);
        }

        if (canFly) {
            g.setColor(Color.BLUE);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("MOVE FAST THERE'S A TIMER", 700, 60);
        }

        if (showLevelQuestion) {
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 18));
            g.drawString("YES or NO?", 500, 100);

            g.setColor(Color.LIGHT_GRAY);
            g.fillRect(yesBox.x, yesBox.y, yesBox.width, yesBox.height);
            g.fillRect(noBox.x, noBox.y, noBox.width, noBox.height);

            g.setColor(Color.BLACK);
            g.drawRect(yesBox.x, yesBox.y, yesBox.width, yesBox.height);
            g.drawRect(noBox.x, noBox.y, noBox.width, noBox.height);

            g.drawString("YES", yesBox.x + 10, yesBox.y + 20);
            g.drawString("NO", noBox.x + 10, noBox.y + 20);
        }

        // Add "dizz nuts" text
        g.setColor(Color.MAGENTA);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("dizz nuts", getWidth() - 200, getHeight() - 50);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_W) up = true;
        if (key == KeyEvent.VK_S) down = true;
        if (key == KeyEvent.VK_A) left = true;
        if (key == KeyEvent.VK_D) right = true;

        if (key == KeyEvent.VK_SPACE && !isJumping && !canFly) {
            isJumping = true;
            yVelocity = -30;
        }

        if (isDead && key == KeyEvent.VK_R) {
            respawnPlayer();
        }

        if (key == KeyEvent.VK_Q) { // Restart current level
            loadLevel(level);
            answerText = "Level Restarted!";
        }

        if (isDead && key == KeyEvent.VK_E) { // Go back to start
            level = 1;
            score = 0;
            health = 3;
            respawnPlayer();
            answerText = "Back to start!";
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_W) up = false;
        if (key == KeyEvent.VK_S) down = false;
        if (key == KeyEvent.VK_A) left = false;
        if (key == KeyEvent.VK_D) right = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    private void respawnPlayerPartial() {
        x = 80;
        y = 80;
        yVelocity = 0;
        isDead = false;
        showText = false;
        nextPage = false;
        canFly = false;
        if (flightTimer != null) flightTimer.stop();
        if (!spikes.isEmpty()) spikeDirections.set(0, 1);

        timeLeft = 60;
        countdownTimer.start();
    }

    private void respawnPlayer() {
        x = 80;
        y = 80;
        yVelocity = 0;

        health = 3;
        isDead = false;
        showText = false;
        nextPage = false;
        answerText = "";
        score = 0;
        timeLeft = 60;

        if (!spikes.isEmpty()) spikeDirections.set(0, 1);

        level = 1;
        loadLevel(level);

        repaint();
    }

public static void main(String[] args) {
        JFrame frame = new JFrame("LOST IN TIME");
        Games movingBox = new Games();
        frame.add(movingBox);
        frame.setSize(1400, 1400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}