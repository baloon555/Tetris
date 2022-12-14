import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

public class GameZone extends JPanel  {
    final int BOARD_WIDTH = 300;
    final int BOARD_HEIGHT = 540;
    final int UNIT_SIZE = 30;
    final int GAME_UNITS_X = BOARD_WIDTH / UNIT_SIZE;
    final int GAME_UNITS_Y = BOARD_HEIGHT / UNIT_SIZE;
    int curX;
    int curY;
    int speed = 500;
    int mode = 1;
    boolean moveLeft = true;
    boolean moveRight = true;
    boolean running = true;
    boolean endgame = false;
    boolean animation;
    Block shape;
    int[] curLast; //current last row - blocks the passage
    Color[][] colors; //current landed shapes
    boolean[] rowsToClean; //rows to clean - for the bubble effect
    Score score = new Score();
    Level level = new Level();
    NextShape nextShape = new NextShape();
    File fileMode = new File("changeMode.wav");
    AudioInputStream audioMode = AudioSystem.getAudioInputStream(fileMode);
    Clip clipMode = AudioSystem.getClip();
    File fileClear = new File("clear.wav");
    AudioInputStream audioClear = AudioSystem.getAudioInputStream(fileClear);
    Clip clipClear = AudioSystem.getClip();
    File fileDeath = new File("death.wav");
    AudioInputStream audioDeath = AudioSystem.getAudioInputStream(fileDeath);
    Clip clipDeath = AudioSystem.getClip();

    GameZone() throws UnsupportedAudioFileException, LineUnavailableException, IOException {
        this.setLayout(new BorderLayout());
        this.setOpaque(true);
        this.setFocusable(true);
        this.addKeyListener(new MyKeyAdapter());
        this.setBorder(BorderFactory.createLineBorder(Color.black, 2));
        this.setBackground(Color.WHITE);
        colors = new Color[GAME_UNITS_X][GAME_UNITS_Y];
        curLast = new int[GAME_UNITS_X];
        rowsToClean = new boolean[GAME_UNITS_Y];
        clipClear.open(audioClear);
        clipMode.open(audioMode);
        clipDeath.open(audioDeath);
        nextShape.chooseShape();
        createShape();
        restartt();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        Graphics2D g2D = (Graphics2D) g;


/* draw the landed figures*/
        for (int k = 0; k < BOARD_HEIGHT; k += UNIT_SIZE) {
            for (int m = 0; m < BOARD_WIDTH; m += UNIT_SIZE) {
                int yCor = k / UNIT_SIZE, xCor = m / UNIT_SIZE;
                if (colors[xCor][yCor] != null) {
                    g.setColor(colors[xCor][yCor]);
                    g.fillRect(m, k, UNIT_SIZE, UNIT_SIZE);
                    g2D.setStroke(new BasicStroke(2));
                    g.setColor(Color.black);
                    g.drawRect(m, k,
                            UNIT_SIZE, UNIT_SIZE);
                }
            }
        }

/*draw the falling shape*/
        for (int i = 0; i < shape.rows(); i++)
            for (int j = 0; j < shape.length(); j++)
                if (shape.checkNull(i, j)) {
                    g.setColor(shape.getColor());
                    g.fillRect((shape.getX(i, j)), shape.getY(i, j),
                            UNIT_SIZE, UNIT_SIZE);
                    g2D.setStroke(new BasicStroke(2));
                    g.setColor(Color.black);
                    g.drawRect((shape.getX(i, j)), shape.getY(i, j),
                            UNIT_SIZE, UNIT_SIZE);
                }
    }

    public void restartt(){
        running = true;
        for (int i = 0; i < GAME_UNITS_X; i++) {
            curLast[i] = BOARD_HEIGHT;
        }
        refreshRowsToClean();

        for(int i = 0; i<colors.length;i++)
            for(int j = 0; j<colors[0].length; j++){
                colors[i][j] = null;
            }
    }
    public void createShape() {
        shape = new Block(nextShape.curShape);
        curX = ((BOARD_WIDTH - shape.length()*UNIT_SIZE)/(2*UNIT_SIZE))*UNIT_SIZE;
        curY = 0;
        shape.posShape(curX, curY);
        nextShape.createShape();
        mode = 1;
        repaint();
    }


    public boolean endGame() {
        for (int i = 0; i < shape.rows(); i++)
            for (int j = 0; j < shape.length(); j++)
                if (shape.checkNull(i, j))
                    if(shape.getY(i,j)<=0) {
                        endgame = true;
                        clipDeath.start();
                        String name = JOptionPane.showInputDialog("Game Over!\nPlease enter your name.");
                       if(!name.equals(""))
                            Main.endGame(name, score.getScore());
                        return true;
                    }
        return false;

    }
/* check if the shape has reached the bottom*/
    public boolean checkLast() throws LineUnavailableException, IOException {
        for (int i = 0; i < shape.rows(); i++)
            for (int j = 0; j < shape.length(); j++)
                if (shape.checkNull(i, j))
                    if (curLast[shape.getX(i, j) / UNIT_SIZE] == shape.getY(i, j)+UNIT_SIZE) {
                        if(!endGame()){
                            build();
                            checkRow();
                        }
                        return false;
                    }

        return true;
    }

    public void build() {
        for (int i = 0; i < shape.rows(); i++)
            for (int j = 0; j < shape.length(); j++)
                if (shape.checkNull(i, j)) {
                    colors[shape.getX(i, j) / UNIT_SIZE][shape.getY(i, j) / UNIT_SIZE] = shape.getColor();
                    if (shape.getY(i, j) < curLast[shape.getX(i, j) / UNIT_SIZE])
                        curLast[shape.getX(i, j) / UNIT_SIZE] = shape.getY(i, j);
                }

        nextShape.chooseShape();
        createShape();


    }
/*check all the rows if they are full*/
    public void checkRow() throws LineUnavailableException, IOException {
        int check = 0;
        for (int row = 0; row < BOARD_HEIGHT; row += UNIT_SIZE) {
            for (int i = 0; i < BOARD_WIDTH; i += UNIT_SIZE) {
                if (colors[i / UNIT_SIZE][row / UNIT_SIZE] != null) check++;
            }
            if (check == GAME_UNITS_X) {
                rowsToClean[row/UNIT_SIZE] = true; //CLEAN
                score.addScore();
                if (score.getScore() % 100 == 0 &&level.getLevel()<=16) {//every 100 point raise the level
                    speed -= 25;
                    level.addLevel();
                }
            }
            check = 0;
        }
        cleanRow();
    }

    //clean the full rows
    public void cleanRow() {
        int rows = 0;
        for(int i = 0; i < rowsToClean.length; i++)
            if(rowsToClean[i])
                rows++;
        if(rows == 0)
            return;

        int[] numOfRowsToClean = new int[rows];

        for (int i = 0; i < GAME_UNITS_Y && rows>=1; i++)
                if (rowsToClean[i]) {
                    numOfRowsToClean[rows-1] = i;
                    rows--;
                }



        /*copy of the current colors map - landed figures*/
        Color[][] curColors = new Color[GAME_UNITS_X][GAME_UNITS_Y];
        for(int j = 0; j < GAME_UNITS_Y; j++)
            for (int i = 0; i < GAME_UNITS_X; i++)
                if(rowsToClean[j]) //copy only rows that need cleaning
                    curColors[i][j] = colors[i][j];


        /*blup blup blup*/
        for (int j = 0; j < 3; j++) {
            clipClear.start();

            for (int i = 0; i < GAME_UNITS_X; i++)
                for(int k = 0; k < numOfRowsToClean.length; k++) {
                    int row = numOfRowsToClean[k];
                    colors[i][row] = null;
                }
            repaint();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < GAME_UNITS_X; i++)
                for(int k = 0; k < numOfRowsToClean.length; k++) {
                    int row = numOfRowsToClean[k];
                    colors[i][row] = curColors[i][row];
                }
            repaint();
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            clipClear.setMicrosecondPosition(0);
        }

        //delete row
        for(int k = numOfRowsToClean.length-1; k>=0; k--) {
            int row = numOfRowsToClean[k];
            for (int i = 0; i < GAME_UNITS_X; i++)
                colors[i][row] = null;
            int counter = 0;
            for (int i = row; i > 0 && counter < GAME_UNITS_X; i--) {
                counter = 0;
                for (int j = 0; j < GAME_UNITS_X; j++) {
                    if(colors[j][i - 1] == null) counter++;
                    colors[j][i] = colors[j][i - 1];
                }
            }
            for (int i = 0; i < GAME_UNITS_X; i++) //change the limit line
                curLast[i] += UNIT_SIZE;
        }


        refreshRowsToClean();
        checkLimit(); //checks the limit, if there are "holes" it changes the limit

    }

    public void refreshRowsToClean(){
        for (int i = 0; i < GAME_UNITS_Y; i++)
            rowsToClean[i] = false;
    }

    public void checkLimit(){
        boolean found;
        for(int i = 0; i<curLast.length; i++) { //check all x
            found = false;
            while (curLast[i] <= BOARD_HEIGHT-UNIT_SIZE && !found) {
                if (colors[i][curLast[i] / UNIT_SIZE] != null) //there's a color block - can break from loop
                    found = true;
                else //HOLE
                    curLast[i] += UNIT_SIZE;
            }
        }
    }
    //rotate the shape
    public void changeMode() {
        updatePoint(0,1);
        switch(nextShape.curShape){
            case(0): //red
                break;
            case(1): //yellow
                switch(mode){
                        case(1):
                            updatePoint(0,1); //the rightest point
                            if(curX==0 || colors[(curX-UNIT_SIZE)/UNIT_SIZE][(curY)/UNIT_SIZE]!=null){ //there's a shape there
                                mode = 4; //return to previous state
                                break;
                            }
                            //else
                            shape.setLocation(1,1, curX, curY+UNIT_SIZE);
                            shape.setLocation(0,0, curX-UNIT_SIZE, curY);
                            shape.setLocation(0,2, curX+UNIT_SIZE, curY);
                            clipMode.start();
                            break;
                        case (2):
                            if(curY+UNIT_SIZE==BOARD_HEIGHT || curY == 0|| colors[(curX)/UNIT_SIZE][(curY-UNIT_SIZE)/UNIT_SIZE]!=null){ //
                                mode = 1;
                                break;
                            }
                            //else
                            shape.setLocation(1,1, curX-UNIT_SIZE, curY);
                            shape.setLocation(0,0, curX, curY-UNIT_SIZE);
                            shape.setLocation(0,2, curX, curY+UNIT_SIZE);
                            clipMode.start();
                            break;

                    case(3):
                        if(curX==BOARD_WIDTH-UNIT_SIZE || colors[(curX+UNIT_SIZE)/UNIT_SIZE][(curY)/UNIT_SIZE]!=null){
                            mode = 2;
                            break;
                        }
                        //else
                        shape.setLocation(1,1,curX,curY-UNIT_SIZE);
                        shape.setLocation(0,0, curX+UNIT_SIZE, curY);
                        shape.setLocation(0,2, curX-UNIT_SIZE, curY);
                        clipMode.start();
                        break;
                    case(4):
                        if(curY==BOARD_HEIGHT-UNIT_SIZE || colors[(curX)/UNIT_SIZE][(curY-UNIT_SIZE)/UNIT_SIZE]!=null){
                            mode = 3;
                            break;
                        }
                        //else
                        shape.setLocation(1,1, curX+UNIT_SIZE, curY);
                        shape.setLocation(0,0, curX, curY+UNIT_SIZE);
                        shape.setLocation(0,2, curX, curY-UNIT_SIZE);
                        clipMode.start();
                        break;
                }
            break;
            case(2): //purple
                switch(mode){
                    case(1):
                        if(curX==0 || colors[(curX-UNIT_SIZE)/UNIT_SIZE][(curY)/UNIT_SIZE]!=null
                                ||colors[(curX+UNIT_SIZE)/UNIT_SIZE][(curY)/UNIT_SIZE]!=null
                                ||colors[(curX+UNIT_SIZE)/UNIT_SIZE][(curY+UNIT_SIZE)/UNIT_SIZE]!=null){ //check space from left
                            mode = 4;
                            break;
                        }
                        //else
                        shape.setLocation(0,0, curX-UNIT_SIZE, curY);
                        shape.setLocation(0,2, curX+UNIT_SIZE, curY);
                        shape.setLocation(1,2, curX+UNIT_SIZE, curY+UNIT_SIZE);
                        clipMode.start();
                        break;
                    case(2):
                        if(curY==0|| colors[(curX)/UNIT_SIZE][(curY-UNIT_SIZE)/UNIT_SIZE]!=null
                                ||colors[(curX)/UNIT_SIZE][(curY+UNIT_SIZE)/UNIT_SIZE]!=null
                                ||colors[(curX-UNIT_SIZE)/UNIT_SIZE][(curY+UNIT_SIZE)/UNIT_SIZE]!=null) {//check one space from top and two spaces bottom-left
                            mode= 1;
                            break;
                        }
                        shape.setLocation(0,0, curX, curY-UNIT_SIZE);
                        shape.setLocation(0,2, curX, curY+UNIT_SIZE);
                        shape.setLocation(1,2, curX-UNIT_SIZE, curY+UNIT_SIZE);
                        clipMode.start();
                        break;
                    case(3):
                        if(curX>=BOARD_WIDTH-UNIT_SIZE || colors[(curX+UNIT_SIZE)/UNIT_SIZE][(curY)/UNIT_SIZE]!=null
                                ||colors[(curX-UNIT_SIZE)/UNIT_SIZE][(curY)/UNIT_SIZE]!=null
                                ||colors[(curX-UNIT_SIZE)/UNIT_SIZE][(curY-UNIT_SIZE)/UNIT_SIZE]!=null){ //check two spaces from left
                            mode = 2;
                            break;
                        }
                        //else
                        shape.setLocation(0,0, curX+UNIT_SIZE, curY);
                        shape.setLocation(0,2, curX-UNIT_SIZE, curY);
                        shape.setLocation(1,2, curX-UNIT_SIZE, curY-UNIT_SIZE);
                        clipMode.start();
                        break;
                    case(4):
                        if(curY>=BOARD_HEIGHT-UNIT_SIZE|| colors[(curX)/UNIT_SIZE][(curY+UNIT_SIZE)/UNIT_SIZE]!=null
                                ||colors[(curX)/UNIT_SIZE][(curY-UNIT_SIZE)/UNIT_SIZE]!=null
                                ||colors[(curX+UNIT_SIZE)/UNIT_SIZE][(curY-UNIT_SIZE)/UNIT_SIZE]!=null) {//check two spaces from top)
                            mode= 3;
                            break;
                        }
                        shape.setLocation(0,0, curX, curY+UNIT_SIZE);
                        shape.setLocation(0,2, curX, curY-UNIT_SIZE);
                        shape.setLocation(1,2, curX+UNIT_SIZE, curY-UNIT_SIZE);
                        clipMode.start();
                        break;
                }
            break;
            case(3)://blue
                switch(mode){
                        case(1):
                            if(curX==0 || colors[(curX-UNIT_SIZE)/UNIT_SIZE][(curY)/UNIT_SIZE]!=null//(0,0)
                                    ||colors[(curX+UNIT_SIZE)/UNIT_SIZE][(curY)/UNIT_SIZE]!=null//(0,2)
                                    ||colors[(curX-UNIT_SIZE)/UNIT_SIZE][(curY+UNIT_SIZE)/UNIT_SIZE]!=null){ //(1,0)
                                mode = 4;
                                break;
                            }
                            //else
                            shape.setLocation(0,0, curX-UNIT_SIZE, curY);
                            shape.setLocation(0,2, curX+UNIT_SIZE, curY);
                            shape.setLocation(1,0, curX-UNIT_SIZE, curY+UNIT_SIZE);
                            clipMode.start();
                            break;
                        case(2):
                            if(curY==0|| colors[(curX)/UNIT_SIZE][(curY-UNIT_SIZE)/UNIT_SIZE]!=null
                                    ||colors[(curX)/UNIT_SIZE][(curY+UNIT_SIZE)/UNIT_SIZE]!=null
                                    ||colors[(curX-UNIT_SIZE)/UNIT_SIZE][(curY-UNIT_SIZE)/UNIT_SIZE]!=null) {//check one space from top and two spaces bottom-left)
                                mode= 1;
                                break;
                            }
                            shape.setLocation(0,0, curX, curY-UNIT_SIZE);
                            shape.setLocation(0,2, curX, curY+UNIT_SIZE);
                            shape.setLocation(1,0, curX-UNIT_SIZE, curY-UNIT_SIZE);
                            clipMode.start();
                            break;
                        case(3):
                            if(curX>=BOARD_WIDTH-UNIT_SIZE || colors[(curX+UNIT_SIZE)/UNIT_SIZE][(curY)/UNIT_SIZE]!=null
                                    ||colors[(curX-UNIT_SIZE)/UNIT_SIZE][(curY)/UNIT_SIZE]!=null
                                    ||colors[(curX+UNIT_SIZE)/UNIT_SIZE][(curY-UNIT_SIZE)/UNIT_SIZE]!=null){ //check two spaces from left
                                mode = 2;
                                break;
                            }
                            //else
                            shape.setLocation(0,0, curX+UNIT_SIZE, curY);
                            shape.setLocation(0,2, curX-UNIT_SIZE, curY);
                            shape.setLocation(1,0, curX+UNIT_SIZE, curY-UNIT_SIZE);
                            clipMode.start();
                            break;
                        case(4):
                            if(curY>=BOARD_HEIGHT-UNIT_SIZE|| colors[(curX)/UNIT_SIZE][(curY+UNIT_SIZE)/UNIT_SIZE]!=null
                                    ||colors[(curX)/UNIT_SIZE][(curY-UNIT_SIZE)/UNIT_SIZE]!=null
                                    ||colors[(curX+UNIT_SIZE)/UNIT_SIZE][(curY+UNIT_SIZE)/UNIT_SIZE]!=null) {//check two spaces from top)
                                mode= 3;
                                break;
                            }
                            shape.setLocation(0,0, curX, curY+UNIT_SIZE);
                            shape.setLocation(0,2, curX, curY-UNIT_SIZE);
                            shape.setLocation(1,0, curX+UNIT_SIZE, curY+UNIT_SIZE);
                            clipMode.start();
                            break;
                    }
                    break;
            case(4): //orange
                switch(mode){
                    case(1):
                    case(3):
                        if(curX>=BOARD_WIDTH-2*UNIT_SIZE||curX<=UNIT_SIZE|| colors[(curX-UNIT_SIZE)/UNIT_SIZE][(curY)/UNIT_SIZE]!=null //(0,0)
                                ||colors[(curX+UNIT_SIZE)/UNIT_SIZE][(curY)/UNIT_SIZE]!=null//(0,2)
                                ||colors[(curX+2*UNIT_SIZE)/UNIT_SIZE][(curY)/UNIT_SIZE]!=null) {//(0,3)
                            mode= 2;
                            break;
                        }
                        //else
                        shape.setLocation(0,0, curX-UNIT_SIZE, curY);
                        shape.setLocation(0,2, curX+UNIT_SIZE, curY);
                        shape.setLocation(0,3, curX+2*UNIT_SIZE, curY);
                        clipMode.start();
                    break;

                    case(2):
                    case(4):
                        if(curY>=BOARD_HEIGHT-2*UNIT_SIZE||curY<=UNIT_SIZE|| colors[(curX)/UNIT_SIZE][(curY-UNIT_SIZE)/UNIT_SIZE]!=null //(0,0)
                                ||colors[(curX)/UNIT_SIZE][(curY+UNIT_SIZE)/UNIT_SIZE]!=null//(0,2)
                                ||colors[(curX)/UNIT_SIZE][(curY+2*UNIT_SIZE)/UNIT_SIZE]!=null) {//(0,3)
                            mode= 1;
                            break;
                        }
                        //else
                        shape.setLocation(0,0, curX, curY-UNIT_SIZE);
                        shape.setLocation(0,2, curX, curY+UNIT_SIZE);
                        shape.setLocation(0,3, curX, curY+2*UNIT_SIZE);
                        clipMode.start();
                        break;
                }
                break;
            case(5)://green
                switch(mode){
                    case(1):
                    case(3):
                        if(curX>=BOARD_WIDTH-UNIT_SIZE||
                        colors[(curX)/UNIT_SIZE][(curY+UNIT_SIZE)/UNIT_SIZE]!=null||
                        colors[(curX+UNIT_SIZE)/UNIT_SIZE][(curY+UNIT_SIZE)/UNIT_SIZE]!=null){
                            mode = 2;
                            break;
                        }
                        //else
                        shape.setLocation(0,0, curX-UNIT_SIZE, curY);
                        shape.setLocation(1,1, curX, curY+UNIT_SIZE);
                        shape.setLocation(1,2, curX+UNIT_SIZE, curY+UNIT_SIZE);
                        clipMode.start();
                        break;
                    case(2):
                    case(4):
                        if(curY == 0||
                                colors[(curX)/UNIT_SIZE][(curY-UNIT_SIZE)/UNIT_SIZE]!=null||
                                colors[(curX-UNIT_SIZE)/UNIT_SIZE][(curY+UNIT_SIZE)/UNIT_SIZE]!=null){
                            mode = 1;
                            break;
                        }
                        //else
                        shape.setLocation(0,0, curX, curY-UNIT_SIZE);
                        shape.setLocation(1,1, curX-UNIT_SIZE, curY);
                        shape.setLocation(1,2, curX-UNIT_SIZE, curY+UNIT_SIZE);
                        clipMode.start();
                        break;
                }
            break;
            case(6):
                switch(mode){
                    case(1):
                    case(3):
                        if(curX>=BOARD_WIDTH-UNIT_SIZE||
                                colors[(curX-UNIT_SIZE)/UNIT_SIZE][(curY-UNIT_SIZE)/UNIT_SIZE]!=null||
                                colors[(curX+UNIT_SIZE)/UNIT_SIZE][(curY)/UNIT_SIZE]!=null){
                            mode = 2;
                            break;
                        }
                        //else
                        shape.setLocation(0,2, curX+UNIT_SIZE, curY);
                        shape.setLocation(1,1, curX, curY+UNIT_SIZE);
                        shape.setLocation(1,0, curX-UNIT_SIZE, curY+UNIT_SIZE);
                        clipMode.start();
                        break;
                    case(2):
                    case(4):
                        if(curY == 0||
                                colors[(curX-UNIT_SIZE)/UNIT_SIZE][(curY)/UNIT_SIZE]!=null|| //(1,1)
                                colors[(curX-UNIT_SIZE)/UNIT_SIZE][(curY-UNIT_SIZE)/UNIT_SIZE]!=null){//(1,0)
                            mode = 1;
                            break;
                        }
                        //else
                        shape.setLocation(0,2, curX, curY+UNIT_SIZE);
                        shape.setLocation(1,1, curX-UNIT_SIZE, curY);
                        shape.setLocation(1,0, curX-UNIT_SIZE, curY-UNIT_SIZE);
                        clipMode.start();
                        break;
                }
            }
        clipMode.setMicrosecondPosition(0);
        }


                public void move () {
                for (int i = 0; i < shape.rows(); i++)
                    for (int j = 0; j < shape.length(); j++)
                        if (shape.checkNull(i, j))
                            shape.setLocation(i, j, shape.getX(i, j),
                                    shape.getY(i, j) + UNIT_SIZE);
                repaint();
            }

    public class MyKeyAdapter extends KeyAdapter {
                @Override
                public void keyPressed(KeyEvent e) {
                    if(!running)
                        return;
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT:
                            moveLeft = true;

                            for (int i = 0; i < shape.rows() && moveLeft; i++)
                                for (int j = 0; j < shape.length() && moveLeft; j++) {
                                    if (shape.checkNull(i, j)) {
                                        if (shape.getX(i, j) == 0 || colors[(shape.getX(i,j)-UNIT_SIZE) / UNIT_SIZE][shape.getY(i,j)/ UNIT_SIZE] != null) {
                                            moveLeft = false;
                                        }
                                    }

                                }
                            if (moveLeft)
                                for (int i = 0; i < shape.rows(); i++)
                                    for (int j = 0; j < shape.length(); j++) {
                                        if (shape.checkNull(i, j))
                                            shape.setLocation(i, j, shape.getX(i, j) - UNIT_SIZE,
                                                    shape.getY(i, j));
                                    }
                            break;

                        case KeyEvent.VK_RIGHT:
                            moveRight = true;
                            for (int i = 0; i < shape.rows() && moveRight; i++)
                                for (int j = 0; j < shape.length() && moveRight; j++) {
                                    if (shape.checkNull(i, j)) {
                                        if (shape.getX(i, j) + UNIT_SIZE == BOARD_WIDTH
                                                || colors[(shape.getX(i, j)+UNIT_SIZE) / UNIT_SIZE][shape.getY(i, j) / UNIT_SIZE] != null)
                                            moveRight = false;
                                    }
                                }
                            if (moveRight)
                                for (int i = 0; i < shape.rows(); i++)
                                    for (int j = 0; j < shape.length(); j++)
                                        if (shape.checkNull(i, j))
                                            shape.setLocation(i, j, shape.getX(i, j) + UNIT_SIZE,
                                                    shape.getY(i, j));
                            break;
                        case KeyEvent.VK_UP:
                            switch (mode) {
                                case 1:
                                    mode = 2;
                                    break;
                                case 2:
                                    mode = 3;
                                    break;
                                case 3:
                                    mode = 4;
                                    break;
                                case 4:
                                    mode = 1;
                                    break;
                            }
                            changeMode();
                            break;
                        case KeyEvent.VK_DOWN:
                            boolean moveDown = true;
                            for (int i = 0; i < shape.rows() && moveDown; i++)
                                for (int j = 0; j < shape.length() && moveDown; j++)
                                    if (shape.checkNull(i, j) && shape.getY(i,j)>=BOARD_HEIGHT-UNIT_SIZE
                                            || colors[(shape.getX(i, j)) / UNIT_SIZE][(shape.getY(i, j)+UNIT_SIZE) / UNIT_SIZE]!=null)
                                        moveDown = false;
                            for (int i = 0; i < shape.rows() && moveDown; i++)
                                for (int j = 0; j < shape.length() && moveDown; j++)
                                    if (shape.checkNull(i, j)){
                                        shape.setLocation(i, j, shape.getX(i, j),
                                                shape.getY(i, j) + UNIT_SIZE);
                    }               }
                }
            }
        

        private void updatePoint (int i, int j) {
            curY = shape.getY(i,j);
            curX = shape.getX(i,j);
        }

}
