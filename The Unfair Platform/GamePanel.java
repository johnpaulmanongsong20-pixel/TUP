import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    int WIDTH, HEIGHT;
    javax.swing.Timer timer;
    int FPS = 60;
    int TILE = 32;
    TileMap map;
    Player player;
    Camera cam;
    boolean left, right, jumpPressed, jumpHeld;
    int currentLevel = 1;
    boolean showingIntro = true;
    int introTimer = 0;
    double fadeAlpha = 0.0;
    int fadeState = 0;
    int fadeFrames = FPS;
    int fadeCounter = 0;
    boolean playerDied = false;
    int deathTimer = 0;
    int deathDelay = 60; // 1 second death animation
    
    // Background images
    BufferedImage[] backgroundImages = new BufferedImage[3];

    public GamePanel(int w, int h) {
        WIDTH = w;
        HEIGHT = h;
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        addKeyListener(this);

        map = new TileMap(TILE);
        player = new Player(64, 64, TILE - 6, TILE - 2);
        
        // Load background images
        loadBackgroundImages();
        
        loadLevel(currentLevel);

        cam = new Camera(0, 0, WIDTH, HEIGHT);
        timer = new javax.swing.Timer(1000 / FPS, this);
    }
    
    void loadBackgroundImages() {
        try {
            backgroundImages[0] = ImageIO.read(new File("level1.jpg"));
            backgroundImages[1] = ImageIO.read(new File("level2.jpg"));
            backgroundImages[2] = ImageIO.read(new File("level3.jpg"));
        } catch (IOException e) {
            System.out.println("Error loading background images: " + e.getMessage());
            // If images fail to load, they will remain null and we'll use solid color backgrounds
        }
    }

    void start() {
        timer.start();
    }

    void loadLevel(int lvl) {
        map.createLevel(lvl);
        showingIntro = true;
        introTimer = 0;

        int groundLevel = (map.rows - 4) * map.tileSize - player.h;
        player.reset(64, groundLevel);

        fadeAlpha = 1.0;
        fadeState = 2;
        fadeCounter = 0;
        playerDied = false;
        deathTimer = 0;
    }

    void beginNextLevel() {
        fadeState = 1;
        fadeCounter = 0;
    }

    void doAdvanceLevel() {
        currentLevel++;
        if (currentLevel > 3) {
            JOptionPane.showMessageDialog(this, "You Beat All Levels!");
            currentLevel = 1;
        }
        loadLevel(currentLevel);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (fadeState == 1) {
            fadeCounter++;
            fadeAlpha = Math.min(1.0, (double) fadeCounter / fadeFrames);
            if (fadeCounter >= fadeFrames) {
                doAdvanceLevel();
                fadeState = 2;
                fadeCounter = 0;
            }
            repaint();
            return;
        } else if (fadeState == 2) {
            fadeCounter++;
            fadeAlpha = 1.0 - Math.min(1.0, (double) fadeCounter / fadeFrames);
            if (fadeCounter >= fadeFrames) {
                fadeState = 0;
                fadeAlpha = 0.0;
            }
        }

        if (showingIntro) {
            introTimer++;
            if (introTimer > FPS * 2) showingIntro = false;
            repaint();
            return;
        }

        // Death animation sequence
        if (playerDied) {
            deathTimer++;
            // Show falling animation for a moment before respawn
            if (deathTimer >= deathDelay) {
                playerDied = false;
                deathTimer = 0;
                map.resetTraps();
                int groundLevel = (map.rows - 4) * map.tileSize - player.h;
                player.reset(64, groundLevel);
            } else {
                // Continue physics during death animation so player falls
                player.updatePhysicsDuringDeath(map);
                cam.updateTarget(player);
                cam.lerpToTarget(0.08);
            }
            repaint();
            return;
        }

        double accel = 0.45;
        double maxSpeed = 5.2;
        double airControl = 0.5;

        if (left && !right)
            player.applyHorizontal(-accel, maxSpeed, jumpHeld ? 1.0 : airControl);
        else if (right && !left)
            player.applyHorizontal(accel, maxSpeed, jumpHeld ? 1.0 : airControl);
        else player.applyFriction();

        if (jumpPressed) {
            player.tryJump();
            jumpPressed = false;
        }

        player.setJumpHeld(jumpHeld);
        player.updatePhysics(map);
        map.update(player);
        cam.updateTarget(player);
        cam.lerpToTarget(0.08);

        if (map.checkGoal(player)) beginNextLevel();
        if (player.y > map.pixelHeight() + 200 || map.checkDeadlyHit(player)) {
            playerDied = true;
            deathTimer = 0;
            player.startDeathAnimation();
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();

        // Draw background image or fallback color
        int bgIndex = currentLevel - 1;
        if (backgroundImages[bgIndex] != null) {
            // Draw background image, scaled to fit the screen
            g.drawImage(backgroundImages[bgIndex], 0, 0, WIDTH, HEIGHT, null);
            
            // Add a semi-transparent dark overlay to dim the background
            g.setColor(new Color(0, 0, 0, 80)); // 80/255 ≈ 31% darkness
            g.fillRect(0, 0, WIDTH, HEIGHT);
        } else {
            // Fallback to solid color if image not loaded
            Color bg;
            switch (currentLevel) {
                default -> bg = new Color(10, 28, 60);
                case 2 -> bg = new Color(35, 12, 48);
                case 3 -> bg = new Color(60, 12, 18);
            }
            g.setColor(bg);
            g.fillRect(0, 0, WIDTH, HEIGHT);
            drawPixelStars(g, currentLevel);
        }

        g.translate(-cam.x, -cam.y);
        map.render(g);
        
        // Apply death effect to player
        if (playerDied) {
            float deathAlpha = 1.0f - ((float)deathTimer / deathDelay);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, deathAlpha));
            player.render(g);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        } else {
            player.render(g);
        }
        
        g.translate(cam.x, cam.y);

        if (showingIntro) drawIntro(g);
        else {
            g.setColor(Color.WHITE);
            g.drawString("← → Move   |   Space Jump   |   R Reset", 12, 18);
            g.drawString("Level " + currentLevel, WIDTH - 92, 18);
        }

        if (fadeState != 0 || fadeAlpha > 0) {
            g.setColor(new Color(0, 0, 0, (int) (Math.min(1.0, fadeAlpha) * 255)));
            g.fillRect(0, 0, WIDTH, HEIGHT);
        }

        g.dispose();
    }

    void drawPixelStars(Graphics2D g, int lvl) {
        g.setColor(new Color(255, 255, 255, 30));
        Random r = new Random(lvl * 99991L);
        for (int i = 0; i < 40; i++)
            g.fillRect(r.nextInt(WIDTH), r.nextInt(HEIGHT), 1, 1);
    }

    void drawIntro(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 30));

        String title = "", desc = "";
        switch (currentLevel) {
            case 1 -> { title = "🧱 Level 1 – The Tutorial Trap"; desc = "Watch for popping spike traps!"; }
            case 2 -> { title = "🌪️ Level 2 – The False Floor"; desc = "Some tiles vanish when stepped on. Spikes await below."; }
            case 3 -> { title = "🔥 Level 3 – The Cruel Machine"; desc = "Timing is everything. Watch for fake platforms and deadly gaps."; }
        }

        int y = HEIGHT / 3;
        drawCentered(g, title, y);
        g.setFont(new Font("SansSerif", Font.PLAIN, 18));
        drawCentered(g, desc, y + 44);
    }

    void drawCentered(Graphics2D g, String text, int y) {
        int x = (WIDTH - g.getFontMetrics().stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyPressed(KeyEvent e) {
        int kc = e.getKeyCode();
        if (kc == KeyEvent.VK_LEFT) left = true;
        if (kc == KeyEvent.VK_RIGHT) right = true;
        if (kc == KeyEvent.VK_SPACE) { jumpPressed = true; jumpHeld = true; }
        if (kc == KeyEvent.VK_R) {
            map.resetTraps();
            int groundLevel = (map.rows - 4) * map.tileSize - player.h;
            player.reset(64, groundLevel);
            playerDied = false;
            deathTimer = 0;
        }
    }
    @Override public void keyReleased(KeyEvent e) {
        int kc = e.getKeyCode();
        if (kc == KeyEvent.VK_LEFT) left = false;
        if (kc == KeyEvent.VK_RIGHT) right = false;
        if (kc == KeyEvent.VK_SPACE) jumpHeld = false;
    }
}