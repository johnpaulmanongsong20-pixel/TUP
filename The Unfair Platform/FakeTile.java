import java.awt.*;

public class FakeTile {
    int col, row, tile;
    boolean solid = true;
    int respawn = 180;
    int timer = 0;
    double alpha = 1.0;
    boolean fading = false;
    int fadeFrames = 0;
    int maxFadeFrames = 30; // Fade duration - about 0.5 seconds at 60 FPS
    boolean animationComplete = false;
    boolean hasAnimation; // Only Level 3 has animation

    public FakeTile(int c, int r, int t, boolean animated) { 
        col = c; row = r; tile = t; 
        hasAnimation = animated;
    }

    void reset() { 
        solid = true; 
        timer = 0; 
        alpha = 1.0; 
        fading = false;
        fadeFrames = 0;
        animationComplete = false;
    }

    void update(Player p) {
        Rectangle pr = new Rectangle((int)p.x, (int)p.y, p.w, p.h);
        Rectangle tileRect = getRect();
        
        if (solid && pr.intersects(tileRect)) {
            if (hasAnimation) {
                // Level 3: Start fade animation first
                if (!fading) {
                    fading = true;
                    fadeFrames = 0;
                    animationComplete = false;
                }
            } else {
                // Level 2: Immediate disappearance
                solid = false;
                timer = respawn;
            }
        }

        // Fading animation - only for Level 3
        if (hasAnimation && fading && !animationComplete) {
            fadeFrames++;
            // Fade from 1.0 to 0.0 in maxFadeFrames
            alpha = 1.0 - ((double)fadeFrames / maxFadeFrames);
            
            // When fade animation is complete, make it non-solid
            if (fadeFrames >= maxFadeFrames) {
                alpha = 0.0;
                solid = false; // Only now becomes non-solid
                animationComplete = true;
                timer = respawn; // Start respawn timer
            }
        }

        if (!solid) { 
            timer--; 
            if (timer <= 0) { 
                solid = true; 
                alpha = 1.0; 
                fading = false;
                fadeFrames = 0;
                animationComplete = false;
            } 
        }
    }

    void render(Graphics2D g) {
        int x = col * tile;
        int y = row * tile;
        
        // Only render if not completely faded out
        if (alpha > 0) {
            if (hasAnimation) {
                // Level 3: Apply alpha transparency for fade-out effect
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) alpha));
            }
            
            // ALL tiles use the same normal platform color
            g.setColor(new Color(90, 60, 30, (int)(alpha*255)));
            g.fillRect(x, y, tile, tile);
            g.setColor(new Color(70, 40, 20, (int)(alpha*255)));
            g.drawRect(x, y, tile, tile);
            
            if (hasAnimation) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            }
        }
    }

    boolean isSolid() { return solid; }
    
    Rectangle getRect() { 
        return new Rectangle(col * tile, row * tile, tile, tile); 
    }
}