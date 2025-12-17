import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class Player {
    double x, y, dx, dy;
    int w, h;
    boolean onGround;
    double gravity = 0.55;
    double maxFall = 12;
    double jumpVel = -10.2;
    int coyote = 0;
    boolean jumpHeld = false;
    boolean isDying = false;

    // Animation
    BufferedImage idleImg, runRightImg, runLeftImg, jumpImg;
    int frameCounter = 0;
    int animSpeed = 10; // lower = faster animation
    boolean movingLeft = false, movingRight = false, jumping = false;

    public Player(double x, double y, int w, int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        loadImages();
    }

    private void loadImages() {
        try {
            idleImg = ImageIO.read(new File("land and when player is not moving.png"));
            runRightImg = ImageIO.read(new File("running right.png"));
            runLeftImg = ImageIO.read(new File("running left.png"));
            jumpImg = ImageIO.read(new File("jump.png"));
        } catch (IOException e) {
            System.out.println("Error loading player images: " + e.getMessage());
        }
    }

    public void applyHorizontal(double a, double max, double c) {
        dx += a * c;
        if (dx > max) dx = max;
        if (dx < -max) dx = -max;
    }

    public void applyFriction() {
        dx *= onGround ? 0.7 : 0.95;
        if (Math.abs(dx) < 0.1) dx = 0;
    }

    public void tryJump() {
        if (onGround || coyote > 0) {
            dy = jumpVel;
            onGround = false;
            coyote = 0;
            jumping = true;
        }
    }

    public void setJumpHeld(boolean h) {
        jumpHeld = h;
    }

    public void updatePhysics(TileMap map) {
        if (!onGround) dy += gravity;
        if (dy > maxFall) dy = maxFall;

        moveHorizontal(map);
        moveVertical(map);

        // Animation state logic
        movingLeft = dx < -0.2;
        movingRight = dx > 0.2;
        if (onGround) jumping = false;
    }

    public void updatePhysicsDuringDeath(TileMap map) {
        // Continue falling during death animation but no collision
        dy += gravity;
        if (dy > maxFall) dy = maxFall;
        y += dy;
        x += dx;
        
        // Rotate slightly during death fall for visual effect
        jumping = true; // Use jump animation during death fall
    }

    public void startDeathAnimation() {
        isDying = true;
        // Give a small upward boost for dramatic effect
        dy = -3.0;
    }

    void moveHorizontal(TileMap map) {
        x += dx;
        int left = (int) (x / map.tileSize);
        int right = (int) ((x + w) / map.tileSize);
        int top = (int) (y / map.tileSize);
        int bottom = (int) ((y + h - 1) / map.tileSize);

        for (int r = top; r <= bottom; r++) {
            if (r < 0 || r >= map.rows) continue;
            if (dx > 0 && right < map.cols && map.tiles[r][right] == 1) {
                x = right * map.tileSize - w - 0.01;
                dx = 0;
                break;
            }
            if (dx < 0 && left >= 0 && map.tiles[r][left] == 1) {
                x = (left + 1) * map.tileSize + 0.01;
                dx = 0;
                break;
            }
        }
    }

    void moveVertical(TileMap map) {
        y += dy;
        int left = (int) (x / map.tileSize);
        int right = (int) ((x + w - 1) / map.tileSize);
        int top = (int) (y / map.tileSize);
        int bottom = (int) ((y + h) / map.tileSize);
        onGround = false;

        for (int c = left; c <= right; c++) {
            if (c < 0 || c >= map.cols) continue;
            if (dy > 0 && bottom < map.rows && map.tiles[bottom][c] == 1) {
                y = bottom * map.tileSize - h - 0.01;
                dy = 0;
                onGround = true;
                coyote = 10;
                break;
            }
            if (dy < 0 && top >= 0 && map.tiles[top][c] == 1) {
                y = (top + 1) * map.tileSize + 0.01;
                dy = 0;
                break;
            }
        }

        if (!onGround && coyote > 0) coyote--;
    }

    public void render(Graphics2D g) {
        BufferedImage currentFrame = idleImg;

        // Pick correct image
        if (jumping || isDying) {
            currentFrame = jumpImg;
        } else if (movingRight) {
            frameCounter++;
            if (frameCounter > animSpeed) frameCounter = 0;
            currentFrame = runRightImg;
        } else if (movingLeft) {
            frameCounter++;
            if (frameCounter > animSpeed) frameCounter = 0;
            currentFrame = runLeftImg;
        } else {
            currentFrame = idleImg;
        }

        // Draw image
        if (currentFrame != null) {
            g.drawImage(currentFrame, (int) x, (int) y, w, h, null);
        } else {
            // fallback rectangle if image not loaded
            g.setColor(Color.BLUE);
            g.fillRect((int) x, (int) y, w, h);
        }
    }

    public void reset(double xx, double yy) {
        x = xx;
        y = yy;
        dx = dy = 0;
        onGround = true;
        coyote = 10;
        jumping = false;
        isDying = false;
    }
}