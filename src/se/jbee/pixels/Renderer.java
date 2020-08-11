package se.jbee.pixels;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class Renderer {

    private static final int GAME_WIDTH = 384, GAME_HEIGHT = 196;
    private static final int TARGET_FPS = 60;
    private static final long TARGET_FRAME_DURATION = 1000 / TARGET_FPS;

    private static GameMatrix pixels;

    private static Frame frame;
    private static Canvas canvas;

    private static int canvasWidth, canvasHeight;
    private static int gameWidth = 0, gameHeight = 0;

    public static void init() {
        getBestSize();

        frame = new Frame();
        canvas = new Canvas();

        canvas.setPreferredSize(new Dimension(canvasWidth, canvasHeight));

        frame.add(canvas);

        makeFullScreen();

        frame.setResizable(false);
        frame.pack();

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                Game.quit();
            }
        });
        frame.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                Game.quit();
            }
        });
        frame.setVisible(true);

        pixels = new GameMatrix(gameWidth, gameHeight, WorldMaterials.TEST, WorldMaterials.wall);

        startRendering();
        startSimulation();
    }

    private static void startSimulation() {


         putWalls();
        Thread sim = new Thread() {
            @Override
            public void run() {
                boolean simulate = true;
                int run = 0;
                Rnd rnd = new Rnd();
                while (simulate) {
                    for (int y = pixels.height - 1; y >= 0; y--) {
                        if (y % 2 == 0) { // left to right
                            for (int x = 0; x < pixels.width; x++) {
                                pixels.simulate(x,y, run);
                            }
                        } else { // right to left
                            for (int x = pixels.width-1; x >= 0; x--) {
                                pixels.simulate(x,y, run);
                            }
                        }
                    }
                    if (run % 2 == 0) {
                        pixels.set(pixels.width/2, 0, WorldMaterials.sand.variant(rnd));
                        pixels.set(pixels.width/2-2, 0, WorldMaterials.water);
                    }
                    run++;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        simulate = false;
                    }
                    /*


                     */

                }
            }
        };
        sim.setDaemon(true);
        sim.setName("Simulation");
        sim.start();
    }

    private static void putWalls() {
        Material wall = WorldMaterials.wall;
        for (int x = 0; x < pixels.width; x++)
            pixels.set(x, -10, wall); // draw bottom

        for (int x = pixels.width/2-5; x < pixels.width/2+5; x++)
            pixels.set(x, pixels.height /2, wall); // draw bottom

        for (int i = 0; i < 5; i++)
            pixels.set(pixels.width/2 + 3 +i, pixels.height - 50 , wall);

        for (int y = pixels.height-10; y > pixels.height-20; y--) {
            pixels.set(pixels.width/2 - 10, y, wall);
            pixels.set(pixels.width/2 + 10, y, wall);
        }

        for (int i = 0; i < 20; i++)
            pixels.set(pixels.width/2 - 30, pixels.height-10-i, wall);
    }

    private static void startRendering() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                GraphicsConfiguration gc = canvas.getGraphicsConfiguration();
                BufferedImage main = gc.createCompatibleImage(gameWidth, gameHeight);
                long lastFpsTime = System.currentTimeMillis();
                int totalFrames = 0;
                int currentFps = 0;
                boolean draw = true;
                while (draw) {
                    totalFrames ++;
                    long now = System.currentTimeMillis();
                    if (now > lastFpsTime + 1000) {
                        lastFpsTime += 1000;
                        currentFps = totalFrames;
                        totalFrames = 0;
                    }

                    Graphics g2d = main.getGraphics();
                    g2d.setColor(Color.BLACK);
                    g2d.fillRect(0, 0, gameWidth, gameHeight);

                    for (int y = 0; y < pixels.height; y++) {
                        for (int x = 0; x < pixels.width; x++) {
                            MaterialVariant material = pixels.getVariant(x, y);
                            if (material.isPainted()) {
                                main.setRGB(x, y, material.getRGB(totalFrames));
                            }
                        }
                    }

                    g2d.dispose();

                    g2d = canvas.getGraphics();

                    g2d.drawImage(main, 0,0, canvasWidth, canvasHeight, null);
                    g2d.setColor(Color.RED);
                    g2d.drawString(""+ currentFps, 10, 11);


                    g2d.dispose();

                    long left = TARGET_FRAME_DURATION - (System.currentTimeMillis() - now);
                    if (left > 0) {
                        try {
                            Thread.sleep(left);
                        } catch (InterruptedException e) {
                            draw = false;
                        }
                    }
                }
            }
        };
        thread.setName("Rendering");
        thread.setDaemon(true);
        thread.start();
    }

    private static void makeFullScreen() {
        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice gd = env.getDefaultScreenDevice();
        if (gd.isFullScreenSupported()) {
            frame.setUndecorated(true);
            gd.setFullScreenWindow(frame);
        }
    }

    private static void getBestSize() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();

        boolean done = false;
        while (!done) {
            canvasWidth += GAME_WIDTH;
            canvasHeight += GAME_HEIGHT;

            if (canvasWidth > screenSize.width || canvasHeight > screenSize.height) {
                canvasWidth -= GAME_WIDTH;
                canvasHeight -= GAME_HEIGHT;
                done = true;
            }
        }

        int xDiff = screenSize.width - canvasWidth;
        int yDiff = screenSize.height - canvasHeight;
        int factor = canvasWidth / GAME_WIDTH;

        System.out.println("factor: "+factor);

        gameWidth = canvasWidth / factor + xDiff / factor;
        gameHeight = canvasHeight / factor + yDiff / factor;

        canvasWidth = gameWidth *  factor;
        canvasHeight = gameHeight * factor;

        System.out.println("Size: "+ gameWidth + " x "+gameHeight);
    }
}
