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

    private static World world;

    private static Frame frame;
    private static Canvas canvas;

    private static int canvasWidth, canvasHeight;
    private static int gameWidth = 0, gameHeight = 0;
    private static int factor = 1;

    private static volatile int simCounter;
    private static volatile long sumDuration;

    private static boolean showMomenta = false;
    private static boolean addParticles = false;

    static int toolMaterialId = Substances.Water.id;

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
                            world.replaceAt(x + xi, y + yi, Substances.TEST.byId(toolMaterialId).variety(world.rnd));
                } else if (e.getButton() == MouseEvent.BUTTON3) {
                    toolMaterialId = (toolMaterialId +1) % Substances.TEST.count();
                    createAndSetCursor();
                }
            }
        });
        frame.setVisible(true);

        world = new World(gameWidth, gameHeight, Substances.TEST, Substances.HardRock);

        startRendering();
        startSimulation();
    }

    private static void createAndSetCursor() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        int size = 20 * factor;
        BufferedImage cursor = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        SubstanceVariety variant = Substances.TEST.byId(toolMaterialId).variety(0);
        int rgb = variant.isPainted() ? variant.animation.rgba(0,0, world, 1L) : Color.WHITE.getRGB();
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
        Rnd rnd = world.rnd;
        putWalls(rnd);
        Thread sim = new Thread() {
            @Override
            public void run() {
                boolean run = true;
                while (run) {
                    long before = System.currentTimeMillis();
                    world.tick();
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
        int frame = world.loopCount();
        if (frame % 2 == 0) {
            world.replaceAt(world.width / 2, 0, Substances.Dirt.variety(rnd));
            world.replaceAt(world.width / 2 - 2, 0, Substances.Water.variety(rnd));
        }
        if (frame % 4 == 0) {
            world.replaceAt(world.width / 2 - 2, world.height - 20, Substances.Steam.variety(rnd));
        }
        if (frame % 2 == 0) {
            world.replaceAt(world.width / 2 - 50, 0, Substances.Slime.variety(rnd));
        }
    }

    private static void putWalls(Rnd rnd) {
        Substance wall = Substances.HardRock;
        for (int x = 0; x < world.width; x++)
            world.replaceAt(x, -10, wall); // draw bottom

        for (int x = world.width/2-5; x < world.width/2+5; x++)
            world.replaceAt(x, world.height /2, wall); // draw bottom

        for (int i = 0; i < 5; i++)
            world.replaceAt(world.width/2 + 3 +i, world.height - 50 , wall);

        for (int y = world.height-10; y > world.height-20; y--) {
            world.replaceAt(world.width/2 - 10, y, wall);
            world.replaceAt(world.width/2 + 10, y, wall);
        }

        for (int i = 0; i < 20; i++)
            world.replaceAt(world.width/2 - 30, world.height-10-i, wall);

        for (int i = 0; i < 100; i++)
            world.replaceAt(20, world.height-1-i, wall);

        for (int y = 10; y < 80; y++)
            for (int x = 50; x < 100; x++)
                world.replaceAt(x, y, Substances.Water.variety(rnd));

        if (true)
            for (int i = 0; i < 30; i++) {
                world.replaceAt(40 + i, 80 + i, Substances.HardRock);
                world.replaceAt(40 + i, 81 + i, Substances.HardRock);
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
                long frame = 0;
                while (draw) {
                    frameCounter ++;
                    frame++;
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

                    for (int y = 0; y < world.height; y++) {
                        for (int x = 0; x < world.width; x++) {
                            SubstanceVariety material = world.varietyAt(x, y);
                            if (material.isPainted()) {
                                int rgb = material.animation.rgba(x,y, world, frame);
                                if (showMomenta) {
                                    Momenta m = world.momentaAt(x,y);
                                    if (m.isLeft())
                                        rgb = Color.CYAN.getRGB();
                                    if (m.isRight())
                                        rgb = Color.MAGENTA.getRGB();
                                    if (m.isDown())
                                        rgb = Color.YELLOW.getRGB();
                                    if (m.isUp())
                                        rgb = Color.BLUE.getRGB();
                                }
                                if (material.material() == Substances.Poison) {
                                    if (x > 0 && world.substanceAt(x-1, y) != Substances.Poison
                                            || x < world.width-1 && world.substanceAt(x+1,y) != Substances.Poison)
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
                    g2d.drawString("Tool: "+ Substances.TEST.byId(toolMaterialId).name, 10, 42);

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
