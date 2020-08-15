package se.jbee.pixels;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class Renderer {

    private static final int GAME_WIDTH = 640, GAME_HEIGHT = 360;
    private static final int TARGET_FPS = 60;
    private static final int TARGET_SLPS = 120;
    private static final long TARGET_FRAME_DURATION = 1000 / TARGET_FPS;
    private static final long TARGET_SIM_LOOP_DURATION = 1000 / TARGET_SLPS;

    private static GameSimulation pixels;

    private static Frame frame;
    private static Canvas canvas;

    private static int canvasWidth, canvasHeight;
    private static int gameWidth = 0, gameHeight = 0;
    private static int factor = 1;

    private static volatile int simCounter;
    private static volatile long sumDuration;

    private static boolean showMomenta = false;
    private static boolean addParticles = false;

    static int toolMaterialId = Materials.Water.id;

    public static void init() {
        getBestSize();

        frame = new Frame();
        canvas = new Canvas();

        canvas.setFocusable(false);
        canvas.setPreferredSize(new Dimension(canvasWidth, canvasHeight));

        createAndSetCursor();

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
        canvas.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    int x = e.getX() / factor;
                    int y = e.getY() / factor;
                    int size = 20;
                    for (int yi = 0; yi < size; yi++)
                        for (int xi = 0; xi < size; xi++)
                            pixels.replaceAt(x + xi, y + yi, Materials.TEST.byId(toolMaterialId).variant(pixels.rnd));
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    toolMaterialId = (toolMaterialId +1) % Materials.TEST.count();
                    createAndSetCursor();
                }
            }
        });
        frame.setVisible(true);

        pixels = new GameSimulation(gameWidth, gameHeight, Materials.TEST, Materials.HardRock);

        startRendering();
        startSimulation();
    }

    private static void createAndSetCursor() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        int size = 20 * factor;
        BufferedImage cursor = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        MaterialVariant variant = Materials.TEST.byId(toolMaterialId).variant(0);
        int rgb = variant.isPainted() ? variant.getRGB(0) : Color.WHITE.getRGB();
        for (int y = 0; y < size; y++)
            cursor.setRGB(0,y, rgb);
        for (int y = 0; y < size; y++)
            cursor.setRGB(size-1,y, rgb);
        for (int x = 0; x < size; x++)
            cursor.setRGB(x, 0, rgb);
        for (int x = 0; x < size; x++)
            cursor.setRGB(x, size-1, rgb);

        Cursor c = toolkit.createCustomCursor(cursor , new Point(0,0), "img");
        canvas.setCursor(c);
    }

    private static void startSimulation() {
        Rnd rnd = pixels.rnd;
        putWalls(rnd);
        Thread sim = new Thread() {
            @Override
            public void run() {
                boolean run = true;
                while (run) {
                    long before = System.currentTimeMillis();
                    pixels.simulate();
                    simCounter++;
                    // some sources...
                    if (addParticles) {
                        randomPixelStream(rnd);
                    }
                    long duration = System.currentTimeMillis() - before;
                    sumDuration += duration;
                    long sleep = TARGET_SIM_LOOP_DURATION - duration;
                    if (sleep > 0) {
                        try {
                            Thread.sleep(sleep);
                        } catch (InterruptedException e) {
                            run = false;
                        }
                    }
                }
            }
        };
        sim.setDaemon(true);
        sim.setName("Simulation");
        sim.start();
    }

    private static void randomPixelStream(Rnd rnd) {
        int frame = pixels.loopCount();
        if (frame % 2 == 0) {
            pixels.replaceAt(pixels.width / 2, 0, Materials.Dirt.variant(rnd));
            pixels.replaceAt(pixels.width / 2 - 2, 0, Materials.Water.variant(rnd));
        }
        if (frame % 4 == 0) {
            pixels.replaceAt(pixels.width / 2 + 30, 0, Materials.Oil.variant(rnd));
        }
        if (frame % 2 == 0) {
            pixels.replaceAt(pixels.width / 2 - 50, 0, Materials.Slime.variant(rnd));
        }
    }

    private static void putWalls(Rnd rnd) {
        Material wall = Materials.HardRock;
        for (int x = 0; x < pixels.width; x++)
            pixels.replaceAt(x, -10, wall); // draw bottom

        for (int x = pixels.width/2-5; x < pixels.width/2+5; x++)
            pixels.replaceAt(x, pixels.height /2, wall); // draw bottom

        for (int i = 0; i < 5; i++)
            pixels.replaceAt(pixels.width/2 + 3 +i, pixels.height - 50 , wall);

        for (int y = pixels.height-10; y > pixels.height-20; y--) {
            pixels.replaceAt(pixels.width/2 - 10, y, wall);
            pixels.replaceAt(pixels.width/2 + 10, y, wall);
        }

        for (int i = 0; i < 20; i++)
            pixels.replaceAt(pixels.width/2 - 30, pixels.height-10-i, wall);

        for (int i = 0; i < 100; i++)
            pixels.replaceAt(20, pixels.height-1-i, wall);

        for (int y = 10; y < 80; y++)
            for (int x = 50; x < 100; x++)
                pixels.replaceAt(x, y, Materials.Water.variant(rnd));

        if (true)
            for (int i = 0; i < 30; i++) {
                pixels.replaceAt(40 + i, 80 + i, Materials.HardRock);
                pixels.replaceAt(40 + i, 81 + i, Materials.HardRock);
            }
    }

    private static void startRendering() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                GraphicsConfiguration gc = canvas.getGraphicsConfiguration();
                BufferedImage main = gc.createCompatibleImage(gameWidth, gameHeight, Transparency.TRANSLUCENT);
                long lastFpsTime = System.currentTimeMillis();
                int frameCounter = 0;
                int currentFPS = 0;
                int currentSLPS = 0;
                long currentAvgSimDuration = 0L;
                boolean draw = true;
                while (draw) {
                    frameCounter ++;
                    long now = System.currentTimeMillis();
                    if (now > lastFpsTime + 1000) {
                        lastFpsTime += 1000;
                        currentFPS = frameCounter;
                        currentSLPS = simCounter;
                        currentAvgSimDuration = currentSLPS == 0 ? 0L : sumDuration / currentSLPS;
                        frameCounter = 0;
                        simCounter = 0;
                        sumDuration = 0L;
                    }

                    Graphics g2d = main.getGraphics();
                    g2d.setColor(Color.BLACK);
                    g2d.fillRect(0, 0, gameWidth, gameHeight);

                    for (int y = 0; y < pixels.height; y++) {
                        for (int x = 0; x < pixels.width; x++) {
                            MaterialVariant material = pixels.materialVariantAt(x, y);
                            if (material.isPainted()) {
                                int rgb = material.getRGB(frameCounter);
                                if (showMomenta) {
                                    Momenta m = pixels.momentaAt(x,y);
                                    if (m.isLeft())
                                        rgb = Color.CYAN.getRGB();
                                    if (m.isRight())
                                        rgb = Color.MAGENTA.getRGB();
                                    if (m.isDown())
                                        rgb = Color.YELLOW.getRGB();
                                    if (m.isUp())
                                        rgb = Color.BLUE.getRGB();
                                }
                                if (material.material() == Materials.Poison) {
                                    if (x > 0 && pixels.materialAt(x-1, y) != Materials.Poison
                                            || x < pixels.width-1 && pixels.materialAt(x+1,y) != Materials.Poison)
                                        rgb = new Color(rgb).brighter().brighter().getRGB();
                                }
                                main.setRGB(x, y, rgb);
                            }
                        }
                    }

                    g2d.dispose();

                    g2d = canvas.getGraphics();

                    g2d.drawImage(main, 0,0, canvasWidth, canvasHeight, null);
                    g2d.setColor(Color.RED);
                    g2d.drawString("FPS: "+ currentFPS  +" ("+(currentFPS >= TARGET_FPS ? "on point" : "degraded")+")", 10, 10);
                    g2d.drawString("SLPS:"+ currentSLPS +" ("+currentAvgSimDuration+"ms avg = "+(100*currentAvgSimDuration/TARGET_SIM_LOOP_DURATION)+"% CPU Time)", 10, 26);
                    g2d.drawString("Tool: "+ Materials.TEST.byId(toolMaterialId).name, 10, 42);

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
        factor = canvasWidth / GAME_WIDTH;

        System.out.println("factor: "+factor);

        gameWidth = canvasWidth / factor + xDiff / factor;
        gameHeight = canvasHeight / factor + yDiff / factor;

        canvasWidth = gameWidth *  factor;
        canvasHeight = gameHeight * factor;

        System.out.println("Size: "+ gameWidth + " x "+gameHeight);
    }
}
