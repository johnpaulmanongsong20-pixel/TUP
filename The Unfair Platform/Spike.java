import java.awt.*;

public class Spike {
    int col, row, tile;
    double offset;
    int state = 0;
    int triggerDist = 50;
    double ease = 0.25;
    double maxHeight = 24;

    public Spike(int col, int row, int tile) {
        this.col = col;
        this.row = row;
        this.tile = tile;
        this.offset = maxHeight;
    }

    void reset() { offset = maxHeight; state = 0; }

    void update(Player p) {
        double px = p.x + p.w / 2.0;
        double sx = col * tile + tile / 2.0;
        double dist = Math.abs(px - sx);

        if (state == 0 && dist < triggerDist) state = 1;

        if (state == 1) {
            double target = 0;
            offset += (target - offset) * ease;
            if (Math.abs(offset - target) < 1.0) { offset = target; state = 2; }
        }
    }

    void render(Graphics2D g) {
        int x = col * tile;
        int baseY = row * tile;
        int currentHeight = (int) (maxHeight - offset);
        if (currentHeight < 2) return;

        g.setColor(new Color(200, 50, 50));
        int mid = x + tile / 2;
        int spikeTop = baseY - currentHeight;
        int spikeBottom = baseY;
        Polygon tri = new Polygon();
        tri.addPoint(mid, spikeTop);
        tri.addPoint(x + 5, spikeBottom);
        tri.addPoint(x + tile - 5, spikeBottom);
        g.fillPolygon(tri);
        g.setColor(Color.BLACK);
        g.drawPolygon(tri);
    }

    boolean isActive() { return state >= 1 && offset < maxHeight * 0.3; }

    Rectangle getRect() {
        int x = col * tile;
        int baseY = row * tile;
        int currentHeight = (int) (maxHeight - offset);
        int spikeTop = baseY - currentHeight;
        return new Rectangle(x + 4, spikeTop, tile - 8, currentHeight);
    }
}