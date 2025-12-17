import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class TileMap {
    int tileSize;
    int cols = 60;
    int rows = 36;
    int[][] tiles;
    List<Spike> spikes = new ArrayList<>();
    List<FakeTile> fakeTiles = new ArrayList<>();
    List<EnemyBlock> enemies = new ArrayList<>();

    public TileMap(int t) {
        tileSize = t;
        tiles = new int[rows][cols];
    }

    public void createLevel(int lvl) {
        spikes.clear();
        fakeTiles.clear();
        enemies.clear();

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                tiles[r][c] = 0;

        // Level 1 - No fake tiles
        if (lvl == 1) {
            for (int c = 0; c < 28; c++) tiles[rows - 4][c] = 1;
            tiles[rows - 7][15] = 1;
            spikes.add(new Spike(8, rows - 4, tileSize));
            spikes.add(new Spike(9, rows - 4, tileSize));
            spikes.add(new Spike(18, rows - 4, tileSize));
            spikes.add(new Spike(19, rows - 4, tileSize));
            spikes.add(new Spike(25, rows - 4, tileSize));
            tiles[rows - 4][28] = 2;
            tiles[rows - 4][29] = 2;
        }

        // Level 2 - Fake tiles without animation (immediate disappearance)
        if (lvl == 2) {
            for (int c = 0; c < 35; c++)
                if (c % 5 != 0) tiles[rows - 4][c] = 1;
            spikes.add(new Spike(6, rows - 4, tileSize));
            spikes.add(new Spike(7, rows - 4, tileSize));
            spikes.add(new Spike(11, rows - 4, tileSize));
            spikes.add(new Spike(12, rows - 4, tileSize));
            spikes.add(new Spike(16, rows - 4, tileSize));
            spikes.add(new Spike(17, rows - 4, tileSize));
            spikes.add(new Spike(26, rows - 4, tileSize));
            spikes.add(new Spike(27, rows - 4, tileSize));
            spikes.add(new Spike(31, rows - 4, tileSize));
            spikes.add(new Spike(32, rows - 4, tileSize));

            // Level 2 fake tiles - immediate disappearance
            fakeTiles.add(new FakeTile(8, rows - 4, tileSize, false));
            fakeTiles.add(new FakeTile(9, rows - 4, tileSize, false));
            fakeTiles.add(new FakeTile(18, rows - 4, tileSize, false));
            fakeTiles.add(new FakeTile(19, rows - 4, tileSize, false));
            fakeTiles.add(new FakeTile(28, rows - 4, tileSize, false));
            fakeTiles.add(new FakeTile(29, rows - 4, tileSize, false));

            tiles[rows - 6][13] = 1;
            tiles[rows - 8][23] = 1;
            tiles[rows - 6][33] = 1;
            tiles[rows - 4][35] = 2;
            tiles[rows - 4][36] = 2;
            tiles[rows - 4][37] = 2;
        }

        // Level 3 - Fake tiles WITH animation
        if (lvl == 3) {
            for (int c = 0; c < 50; c++)
                if (c % 4 != 0) tiles[rows - 4][c] = 1;

            int[] spikeCols = {5,6,9,10,13,14,17,18,21,22,25,26,29,30,33,34,37,38,41,42};
            for (int sc : spikeCols) spikes.add(new Spike(sc, rows - 4, tileSize));

            // Level 3 fake tiles - WITH fade animation
            for(int c=7;c<10;c++) fakeTiles.add(new FakeTile(c, rows-4, tileSize, true));
            for(int c=15;c<18;c++) fakeTiles.add(new FakeTile(c, rows-4, tileSize, true));
            for(int c=23;c<26;c++) fakeTiles.add(new FakeTile(c, rows-4, tileSize, true));
            for(int c=31;c<34;c++) fakeTiles.add(new FakeTile(c, rows-4, tileSize, true));
            for(int c=39;c<42;c++) fakeTiles.add(new FakeTile(c, rows-4, tileSize, true));

            tiles[rows-4][50]=2;
            tiles[rows-4][51]=2;
            tiles[rows-4][52]=2;
            tiles[rows-4][53]=2;
        }
    }

    public void resetTraps() {
        for (Spike s : spikes) s.reset();
        for (FakeTile ft : fakeTiles) ft.reset();
    }

    public void render(Graphics2D g) {
        for(int r=0;r<rows;r++)
            for(int c=0;c<cols;c++){
                int t = tiles[r][c];
                if(t==1){
                    g.setColor(new Color(90,60,30));
                    g.fillRect(c*tileSize,r*tileSize,tileSize,tileSize);
                    g.setColor(new Color(70,40,20));
                    g.drawRect(c*tileSize,r*tileSize,tileSize,tileSize);
                } else if(t==2){
                    g.setColor(new Color(80,200,120));
                    g.fillRect(c*tileSize,r*tileSize,tileSize,tileSize);
                    g.setColor(new Color(60,180,100));
                    g.drawRect(c*tileSize,r*tileSize,tileSize,tileSize);
                }
            }
        for(FakeTile ft: fakeTiles) ft.render(g);
        for(EnemyBlock eb: enemies) eb.render(g);
        for(Spike s: spikes) s.render(g);
    }

    public void update(Player p){
        for(FakeTile ft: fakeTiles) ft.update(p);
        for(Spike s: spikes) s.update(p);
        for(EnemyBlock eb: enemies) eb.update(p);
    }

    public boolean checkGoal(Player p){
        Rectangle pr = new Rectangle((int)p.x,(int)p.y,p.w,p.h);
        for(int r=0;r<rows;r++)
            for(int c=0;c<cols;c++)
                if(tiles[r][c]==2)
                    if(pr.intersects(new Rectangle(c*tileSize,r*tileSize,tileSize,tileSize)))
                        return true;
        return false;
    }

    public boolean checkDeadlyHit(Player p){
        Rectangle pr = new Rectangle((int)p.x,(int)p.y,p.w,p.h);
        for(Spike s: spikes) if(s.isActive() && s.getRect().intersects(pr)) return true;
        for(FakeTile ft: fakeTiles) if(!ft.isSolid() && ft.getRect().intersects(pr)) return true;
        for(EnemyBlock eb: enemies) if(eb.getRect().intersects(pr)) return true;
        return false;
    }

    public int pixelWidth(){return cols*tileSize;}
    public int pixelHeight(){return rows*tileSize;}
}