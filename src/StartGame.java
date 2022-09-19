import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class StartGame extends JFrame implements ActionListener {
    JButton startBtn;
    JButton leaderBtn;
    JButton quitBtn;
    public StartGame(){
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setSize(600,600);
        this.setLayout(null);
        this.getContentPane().setBackground(Color.gray);
        startBtn = new JButton();
        startBtn.setBounds(200, 250, 200, 50);
        startBtn.setText("Start");
        startBtn.setBackground(Color.PINK);
        startBtn.setFocusable(false);
        startBtn.addActionListener(this);
        this.add(startBtn);


    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==startBtn){
            new GameFrame();
        }
    }
}
