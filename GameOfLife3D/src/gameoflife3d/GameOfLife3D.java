package gameoflife3d;

import java.awt.*;
import javax.swing.*;
import java.util.Random;
import java.awt.image.BufferedImage;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class GameOfLife3D extends JPanel implements KeyListener {
    // Basic geometry
    static int rows = 50;
    static int columns = 50;
    static int height = 50; 
    static int cellSize = 10;
    
    // Used for drawing
    static int sizeCos30 = (int)Math.round(Math.cos(Math.PI / 6) * cellSize);
    Color drawColor = Color.black;

    // Saving and Loading
    static String saveFile = "save.txt";
    static Pattern p = Pattern.compile("\\D+"); // one or more non-digit characters 
    
    // General
    int delay = 100;
    boolean paused = true;
    boolean[][][] currentGen = new boolean[columns][rows][height];
    boolean[][][] nextGen; // nextGen declared in each step statement, explanation in step() method
    
    // Rules
    int minCondition = 3;
    int maxCondition = 10;
    int zombieCondition = 6; // Brings a cell to life
    
    public static void sleep(int duration) {
        try {
            Thread.sleep(duration);
        } catch (Exception e) {
        }
    }
        
    public void generateRandomCondition() { // Randomly assign every cell to true or false
        boolean wasPaused = paused;
        paused = true; // Pause to avoid saving partway through generation change
        sleep(500); // Let the current generation finish updating
        Random r = new Random();
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                for (int k = 0; k < height; k++) {
                    currentGen[i][j][k] = r.nextBoolean();
                }
            }
        }
        paused = wasPaused;
    }
    
    public void clear() {
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                for (int k = 0; k < height; k++) {
                    currentGen[i][j][k] = false;
                }
            }
        }
    }
    
    public int count(int col, int row, int height) { // Returns the number of live neighbours of a cell
        int count = 0;
        for (int dx = -1; dx < 2; dx++) {
            for (int dy = -1; dy < 2; dy++) {
                for (int dz = -1; dz < 2; dz++) {
                    try {
                        if (currentGen[col + dx][row + dy][height + dz] && !(dx == 0 && dy == 0 && dz == 0)) { // if the cell is alive and not the one we're counting neighbours of, increment the count
                            count++;
                        }
                    } catch (IndexOutOfBoundsException e) { // Treat edges of array as dead
                    }
                }
            }
        }
        return count;
    }
    
    public void randomizeGameRules() { // Randomize conditions for life
        Random r = new Random();
        maxCondition = r.nextInt(26);
        minCondition = r.nextInt(maxCondition);
        zombieCondition = r.nextInt(maxCondition - minCondition) + minCondition; 
        generateRandomCondition();
    }
    
    public void step() { // Implements game rules to create next generation
        nextGen = new boolean[columns][rows][height]; 
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                for (int k = 0; k < height; k++) {
                    int neighbours = count(i, j, k); // Count number of alive cells
                    boolean self = currentGen[i][j][k]; // Whether or not the cell is alive
                    // nextGen[i][j][k] = (neighbours == 5 || ((neighbours == 4 || neighbours == 10 || neighbours == 12) && self)); this one is pretty neat, looks like a brain or a mold or something
                    nextGen[i][j][k] = (neighbours == zombieCondition || ((neighbours > minCondition && neighbours < maxCondition) && self));
                }
            }
        }
        currentGen = nextGen; // move nextGen array to currentGen
    }
    
    public Color cubeColor(int x, int y, int z) { // calculates what colour to draw the cube in based on the cube's position to ease in visualization of 3d space
        float h = (float) x / rows;
        float s = (float) y / columns;
        float b = (float) z / height;
        
        Color c = Color.getHSBColor(h, s, b);
        
        return c;
    }
    
    protected void paintComponent(Graphics g) {
        BufferedImage bufferedImage = new BufferedImage(columns * cellSize + rows * cellSize, height * cellSize + (int)((rows + columns) * cellSize / 2), BufferedImage.TYPE_INT_ARGB); // Buffer image to draw grid all at once and avoid "rolling shutter" effect
        Graphics2D g2d = bufferedImage.createGraphics();
        
        // Clear image
        g2d.setColor(Color.white);
        g2d.fillRect(9, 38, bufferedImage.getWidth(), bufferedImage.getHeight());
        int originX = bufferedImage.getWidth() / 2;
        int originY = bufferedImage.getHeight() - 2 * cellSize;
        
        
        // Draw Grid
        for (int i = columns - 1; i >= 0; i--) { // Draw cells at back first
            for (int j = columns - 1; j >= 0; j--) { // Draw cells at back first
                for (int k = 0; k < height; k++) { // Draw cells at bottom first
                    int xStart = originX + sizeCos30 * i - sizeCos30 * j;
                    int yStart = (int)Math.round(originY - (0.5 * (i + j) + k) * cellSize);
                    int[] xPoints = {xStart, xStart + sizeCos30, xStart + sizeCos30, xStart, xStart - sizeCos30, xStart - sizeCos30};
                    int[] yPoints = {yStart + 2 * cellSize, yStart + (int)(1.5 * cellSize), yStart + (int)(0.5 * cellSize), yStart, yStart + (int)(0.5 * cellSize), yStart + (int)(1.5 * cellSize)};
                    int middleX = xStart;
                    int middleY = yStart + cellSize;
                    
                    if (currentGen[i][j][k]) {
                        // Fill Cube
                        g2d.setColor(cubeColor(i, j, k));
                        g2d.fillPolygon(xPoints, yPoints, 6);
                        
                        // Draw Wireframe
                        g2d.setColor(Color.black);
                        g2d.drawPolygon(xPoints, yPoints, 6);
                        g2d.drawLine(middleX, middleY, xPoints[0], yPoints[0]);
                        g2d.drawLine(middleX, middleY, xPoints[2], yPoints[2]);
                        g2d.drawLine(middleX, middleY, xPoints[4], yPoints[4]);
                    }
                }
            }
        }
        
        Graphics2D g2dComponent = (Graphics2D) g;
        g2dComponent.drawImage(bufferedImage, null, 0, 0);
        
        // Draw label for game rules
        g.drawString("Cells persist between " + minCondition + " and " + maxCondition + " neighbours.", 10, 20);
        g.drawString("Cells become alive with exactly " + zombieCondition + " neighbours.", 10, 40);
    }
    
    public void load() throws IOException {
        boolean wasPaused = paused;
        paused = true; // Pause to avoid saving partway through generation change
        sleep(500); // Let current generation finish updating
        boolean[][][] loaded = new boolean[rows][columns][height];
        Scanner s = new Scanner(new FileReader(saveFile));
        s.useDelimiter(p);
        clear();
        minCondition = s.nextInt();
        maxCondition = s.nextInt();
        zombieCondition = s.nextInt();
        while (s.hasNextInt()) {
            loaded[s.nextInt()][s.nextInt()][s.nextInt()] = true;
        }
        s.close();
        currentGen = loaded;
        repaint();
        paused = wasPaused;
    }
    
    public void save() throws IOException {
        boolean wasPaused = paused;
        paused = true; // Pause to avoid saving partway through generation change
        sleep(500); // Let current generation finish updating
        FileWriter f = new FileWriter(saveFile);
        f.write("Game Rules: " + minCondition + "," + maxCondition + "/" + zombieCondition + "\nCoordinates: ");
        for (int i = 0; i < columns; i++) {
            for (int j = 0; j < rows; j++) {
                for (int k = 0; k < height; k++) {
                    if (currentGen[i][j][k]) {
                        f.write(i + "," + j + "," + k + "/");
                    }
                }
            }
        }
        f.close();
        paused = wasPaused;
    }
    
    public static void main(String[] args) {
        // Create JFrame to display game
        JFrame frame = new JFrame();
        frame.setTitle("Conway's Game of Life");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBackground(Color.white);
        frame.setSize(9 + columns * cellSize + rows * cellSize, 38 + height * cellSize + (int)((rows + columns) * cellSize / 2));
                
        // Initialize Game
        GameOfLife3D game = new GameOfLife3D();
        game.addKeyListener(game);
        game.setFocusable(true);
        game.setSize(columns * cellSize + rows * cellSize, height * cellSize + (int)((rows + columns) / 2));
        game.generateRandomCondition();

        frame.add(game);
        frame.setVisible(true);

        
        while (true) {
            if (!game.paused) {
                //Instant start = Instant.now();
                game.step();
                //int frameDelay = Max(0, game.delay - start.compareTo(Instant.now()));
                //sleep(frameDelay);
                sleep(game.delay);
            }
            frame.repaint();
        }
    }
    
    // Input
    public void keyTyped(KeyEvent e) {
        char key = Character.toLowerCase(e.getKeyChar());
        if (key == ' ') {
            System.out.println("Pause/Unpause");
            paused = !paused;
        } else if (key == 's') {
            System.out.println("Save");
            try {
                save();
            } catch (IOException ex) {}
        } else if (key == 'l') {
            System.out.println("Load");
            try {
                load();
            } catch (IOException ex) {}
        } else if (key == 'r') { 
            System.out.println("Generating Random Condition");
            generateRandomCondition();
        } else if (key == 'c') {
            System.out.println("Clear");
            clear();
        } else if (key == '+' || key == '=') {
            System.out.println("Speeding up");
            if (delay/2 > 0) {
                delay /= 2;
            }
        } else if (key == '-') {
            System.out.println("Slowing down");
            delay *= 2;
        } else if (key == 'w') {
            randomizeGameRules();
        }
    }
    

    
    // Unused
    public void keyPressed(KeyEvent e) {}
    public void keyReleased(KeyEvent e) {}    
}
