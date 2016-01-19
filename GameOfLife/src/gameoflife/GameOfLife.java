package gameoflife;

import java.awt.*;
import javax.swing.*;
import java.util.Random;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class GameOfLife extends JPanel implements KeyListener, MouseListener {
    static int rows = 100;
    static int columns = 100;
    static int cellSize = 10;
    static String saveFile = "save.txt"; // Change to load from different file, haven't quite figured out filechoosers yet. save.txt starts as a copy of rake.txt.
    int delay = 100;
    boolean paused = true;
    boolean[][] currentGen = new boolean[columns][rows];
    boolean[][] nextGen;
    Pattern p = Pattern.compile("\\D+");
    
    public static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (Exception e) {
        }
    }
        
    public void generateRandomCondition() { // Randomly assign every cell to true or false
        Random r = new Random();
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                currentGen[i][j] = r.nextBoolean();
            }
        }
    }
    
    public void clear() {
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                currentGen[i][j] = false;
            }
        }
    }
    
    public int count(int col, int row) { // Returns the number of live neighbours of a cell
        int count = 0;
        for (int dx = -1; dx < 2; dx++) {
            for (int dy = -1; dy < 2; dy++) {
                try {
                    if (currentGen[col + dx][row + dy] && !(dx == 0 && dy == 0)) { // if the cell is alive and not the one we're counting neighbours of, increment the count
                        count++;
                    }
                } catch (IndexOutOfBoundsException e) { // Treat edges of array as dead
                }
            }
        }
        return count;
    }
    
    public void step() { // Implements game rules to create next generation
        nextGen = new boolean[columns][rows]; // Creating it every time allows us to simply move the reference of currentGen to nextGen instead of copying with a for loop or external library
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                int neighbours = count(i, j); // Count number of alive cells
                boolean self = currentGen[i][j]; // Whether or not the cell is alive
                nextGen[i][j] = (neighbours == 3 || (neighbours == 2 && self)); // Game of Life Conditions
            }
        }
        currentGen = nextGen; // move nextGen array to currentGen
    }
    
    protected void paintComponent(Graphics g) {
        BufferedImage bufferedImage = new BufferedImage(columns * cellSize, rows * cellSize, BufferedImage.TYPE_INT_ARGB); // Buffer image to draw grid all at once and avoid "rolling shutter" effect
        Graphics2D g2d = bufferedImage.createGraphics();
        
        // Clear image
        g2d.setColor(Color.white);
        g2d.fillRect(9, 38, bufferedImage.getWidth(), bufferedImage.getHeight());
        
        // Draw Grid
        g2d.setColor(Color.black);
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                if (currentGen[i][j]) {
                    g2d.fillRect(i * cellSize, j * cellSize, cellSize, cellSize);
                } else {
                    g2d.drawRect(i * cellSize, j * cellSize, cellSize, cellSize);
                }
            }
        }
        
        Graphics2D g2dComponent = (Graphics2D) g;
        g2dComponent.drawImage(bufferedImage, null, 0, 0); // Draws entire screen at once
    }
    
    public void load() throws IOException { // Load a grid from the file defined in the saveFile field
        Scanner s = new Scanner(new FileReader(saveFile));
        clear();
        s.useDelimiter(p);
        while (s.hasNextInt()) {
            currentGen[s.nextInt()][s.nextInt()] = true;
        }
        s.close();
    }
    
    public void save() throws IOException { // Save the current grid to the file defined in the saveFile field
        FileWriter f = new FileWriter(saveFile);
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                if (currentGen[i][j]) {
                    f.write(i + "," + j + "/");
                }
            }
        }
        f.close();
    }
    
    public static void main(String[] args) {
        // Create JFrame to house the game and set its properties
        JFrame frame = new JFrame();
        frame.setTitle("Conway's Game of Life");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBackground(Color.white);
        frame.setSize(9 + columns * cellSize, 38 + rows * cellSize);
                
        // Initialize game of life
        GameOfLife game = new GameOfLife();
        game.addKeyListener(game);
        game.addMouseListener(game);
        game.setFocusable(true);
        game.setSize(columns * cellSize, rows * cellSize);
        
        // Default to random condition
        game.generateRandomCondition();

        frame.add(game);
        frame.setVisible(true);

        
        while (true) {
            if (!game.paused) {
                game.step();
                sleep(game.delay);
            }
            frame.repaint(); // repaint outside of loop to respond to state changes while paused
        }
    }
    
    // Keyboard Input
    public void keyTyped(KeyEvent e) {
        char key = Character.toLowerCase(e.getKeyChar());
        if (e.getKeyChar() == ' ') { // Space to pause/unpause
            System.out.println("Pause/Unpause");
            paused = !paused;
        } else if (key == 's') { // s to save
            System.out.println("Save");
            try {
                save();
            } catch (IOException ex) {}
        } else if (key == 'l') { // l to load
            System.out.println("Load");
            try {
                load();
            } catch (IOException ex) {}
        } else if (key == 'r') { // r to randomize
            System.out.println("Generating Random Condition");
            generateRandomCondition();
        } else if (key == 'c') { // c to clear
            System.out.println("Clear");
            clear();
        } else if (key == '+' || key == '=') { // + (or =, same key without shift) to speed up
            System.out.println("Speeding up");
            if (delay/2 > 0) {
                delay /= 2;
            }
        } else if (key == '-') { // - to slow down
            System.out.println("Slowing down");
            delay *= 2;
        }
    }
    
    // Mouse Input
    public void mouseClicked(MouseEvent e) {
        int x = e.getX() / cellSize;
        int y = e.getY() / cellSize;
        System.out.println("Mouse clicked at " + x + ", " + y);
        currentGen[x][y] = !currentGen[x][y];
        
    }

    
    // Unused
    public void keyPressed(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    
}
