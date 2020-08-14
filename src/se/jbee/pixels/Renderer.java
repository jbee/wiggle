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

    private static volatile int simCounter;

    private static boolean showMomenta = false;
    private static boolean addParticles = false;

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
            public void keyPressed(KeyEvent e) {
                char c = e.getKeyChar();
                if (c == 'm') {
                    showMomenta = !showMomenta;
                } else if (c == 'p') {
                    addParticles = !addParticles;
                } else if (c == 'q') {
                    Game.quit();
                }
            }
        });
        frame.setVisible(true);

        pixels = new GameMatrix(gameWidth, gameHeight, WorldMaterials.TEST, WorldMaterials.rock);

        startRendering();
        startSimulation();
    }

    private static void startSimulation() {

        Rnd rnd = new Rnd();
         putWalls(rnd);
        Thread sim = new Thread() {
            @Override
            public void run() {
                boolean simulate = true;
                int frame = 0;
                while (simulate) {
                    pixels.simulate();
                    simCounter++;
                    // some sources...
                    if (addParticles) {
                        if (frame % 2 == 0) {
                            pixels.insert(pixels.width / 2, 0, WorldMaterials.sand.variant(rnd));
                            pixels.insert(pixels.width / 2 - 2, 0, WorldMaterials.water.variant(rnd));
                        }
                        if (frame % 4 == 0) {
                            pixels.insert(pixels.width / 2 + 30, 0, WorldMaterials.oil.variant(rnd));
                        }
                        if (frame % 2 == 0) {
                            pixels.insert(pixels.width / 2 - 50, 0, WorldMaterials.slime.variant(rnd));
                        }
                    }
                    frame++;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        simulate = false;
                    }
                }
            }
        };
        sim.setDaemon(true);
        sim.setName("Simulation");
        sim.start();
    }

    private static void putWalls(Rnd rnd) {
        Material wall = WorldMaterials.rock;
        for (int x = 0; x < pixels.width; x++)
            pixels.insert(x, -10, wall); // draw bottom

        for (int x = pixels.width/2-5; x < pixels.width/2+5; x++)
            pixels.insert(x, pixels.height /2, wall); // draw bottom

        for (int i = 0; i < 5; i++)
            pixels.insert(pixels.width/2 + 3 +i, pixels.height - 50 , wall);

        for (int y = pixels.height-10; y > pixels.height-20; y--) {
            pixels.insert(pixels.width/2 - 10, y, wall);
            pixels.insert(pixels.width/2 + 10, y, wall);
        }

        for (int i = 0; i < 20; i++)
            pixels.insert(pixels.width/2 - 30, pixels.height-10-i, wall);

        for (int i = 0; i < 100; i++)
            pixels.insert(20, pixels.height-1-i, wall);

        for (int y = 10; y < 80; y++)
            for (int x = 50; x < 100; x++)
                pixels.insert(x, y, WorldMaterials.water.variant(rnd));

            if (true)
        for (int i = 0; i < 30; i++) {
            pixels.insert(40 + i, 80 + i, WorldMaterials.rock);
            pixels.insert(40 + i, 81 + i, WorldMaterials.rock);
        }
    }

    private static void startRendering() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                GraphicsConfiguration gc = canvas.getGraphicsConfiguration();
                BufferedImage main = gc.createCompatibleImage(gameWidth, gameHeight);
                long lastFpsTime = System.currentTimeMillis();
                int frameCounter = 0;
                int currentFPS = 0;
                int currentSPS = 0;
                boolean draw = true;
                while (draw) {
                    frameCounter ++;
                    long now = System.currentTimeMillis();
                    if (now > lastFpsTime + 1000) {
                        lastFpsTime += 1000;
                        currentFPS = frameCounter;
                        currentSPS = simCounter;
                        frameCounter = 0;
                        simCounter = 0;
                    }

                    Graphics g2d = main.getGraphics();
                    g2d.setColor(Color.BLACK);
                    g2d.fillRect(0, 0, gameWidth, gameHeight);

                    for (int y = 0; y < pixels.height; y++) {
                        for (int x = 0; x < pixels.width; x++) {
                            MaterialVariant material = pixels.getVariant(x, y);
                            if (material.isPainted()) {
                                int rgb = material.getRGB(frameCounter);
                                if (showMomenta && material.material().simulation == Simulation.FLUID && pixels.getMomenta(x,y).hasMomentum()) {
                                    Momenta m = pixels.getMomenta(x,y);
                                    rgb = Color.BLUE.getRGB();
                                    if (m.has(Momentum.LEFT))
                                        rgb = Color.CYAN.getRGB();
                                    if (m.has(Momentum.RIGHT))
                                        rgb = Color.MAGENTA.getRGB();
                                }
                                main.setRGB(x, y, rgb);
                            }
                        }
                    }

                    g2d.dispose();

                    g2d = canvas.getGraphics();

                    g2d.drawImage(main, 0,0, canvasWidth, canvasHeight, null);
                    g2d.setColor(Color.RED);
                    g2d.drawString("FPS: "+ currentFPS, 10, 11);
                    g2d.drawString("SPS:"+ currentSPS +" ("+(currentSPS == 0 ? 0 : 1000/currentSPS)+"ms avg)", 10, 21);

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
