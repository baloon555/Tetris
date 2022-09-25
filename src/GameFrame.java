import javax.swing.*;
import java.awt.*;

public class GameFrame extends JFrame {
    GameFrame(){

        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(600,600);
        this.setLayout(null);
        this.getContentPane().setBackground(Color.gray);
        GameZone gameZone = new GameZone();
        this.add(gameZone);
        gameZone.setBounds(20,20, gameZone.BOARD_WIDTH, gameZone.BOARD_HEIGHT);
        this.add(gameZone.score);
        gameZone.score.setBounds(325,gameZone.score.BOARD_Y, gameZone.score.BOARD_WIDTH, gameZone.score.BOARD_HEIGHT);
        this.add(gameZone.level);
        gameZone.level.setBounds(325,gameZone.level.BOARD_Y, gameZone.level.BOARD_WIDTH, gameZone.level.BOARD_HEIGHT);
        new GameThread(gameZone).start();
        this.setVisible(true);
    }
}
