import java.awt.*;

public class EnemyBlock {
    double cx, cy;
    int col, row, tile;
    double amp, spd;
    boolean horiz;
    double t = 0.0;

    public EnemyBlock(int c, int r, int tsize, double ampTiles, double spdPix, boolean horiz) {
        col = c; row = r; tile = tsize;
        cx = c * tsize;
        cy = r * tsize;
        amp = ampTiles * tsize;
        spd = spdPix / 60.0;
        this.horiz = horiz;
    }

    void update(Player p) {
        t += spd;
        if (horiz) cx = col * tile + Math.sin(t) * amp;
        else cy = row * tile + Math.sin(t) * amp;
    }

    void render(Graphics2D g) {
        g.setColor(new Color(80, 20, 120));
        g.fillRect((int) cx, (int) cy, tile, tile);
        g.setColor(Color.BLACK);
        g.drawRect((int) cx, (int) cy, tile, tile);
    }

    Rectangle getRect() { return new Rectangle((int) cx, (int) cy, tile, tile); }
}