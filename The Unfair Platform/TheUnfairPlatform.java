import javax.swing.*;

public class TheUnfairPlatform {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame("The Unfair Platform");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            GamePanel gp = new GamePanel(960, 640);
            f.setContentPane(gp);
            f.pack();
            f.setResizable(false);
            f.setLocationRelativeTo(null);
            f.setVisible(true);
            gp.start();
        });
    }
}