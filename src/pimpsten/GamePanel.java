package pimpsten;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JPanel;

/**
 * Classe principal del joc?
 * 
 * @author Oscar Saleta
 *
 */
@SuppressWarnings("serial")
public class GamePanel extends JPanel implements Runnable {

	private Game game;

	/**
	 * Amplada de la finestra
	 */
	private static final int PWIDTH = 800;
	/**
	 * Alçada de la finestra
	 */
	private static final int PHEIGHT = 600;
	/**
	 * Màxim nombre de frame updates sense fer sleep abans de fer un yield
	 */
	private static final int NO_DELAYS_PER_YIELD = 16;
	/**
	 * Nombre màxim de frames no renderitzats
	 */
	private static final int MAX_FRAME_SKIPS = 5;

	/**
	 * Per tenir entre 80 i 85 FPS (està en nanosegons)
	 */
	private long period;

	private Thread animator;

	private boolean running = false;
	private boolean gameOver = false;
	private boolean isPaused = false;

	private Graphics2D g2d;
	private Image img = null;
	private Font arcadeFont;
	private FontMetrics metrics;

	private ShipEntity ship;
	private Vector<AsteroidEntity> asteroids = new Vector<AsteroidEntity>(0);
	private Vector<ShotEntity> shots = new Vector<ShotEntity>(0);

	//variables per manejar l'input per teclat (podrem premer 2 tecles alhora)
	public boolean keyLeft = false;
	public boolean keyRight = false;
	public boolean keyUp = false;
	public boolean keyDown = false;
	public boolean keyX = false;
	public boolean keySpace = false;

	public GamePanel(Game game, long period) {
		this.game = game;
		this.period = period;
		setBackground(Color.BLACK);
		setPreferredSize(new Dimension(PWIDTH,PHEIGHT));

		setFocusable(true);
		requestFocus();
		readyForTermination();
		addKeyListener(new KeyboardEventManager(this));

		//		TODO: create game components
		ship = new ShipEntity(PWIDTH/2, PHEIGHT/2);

		addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				testPress(e.getX(),e.getY());
			}
		});

		// carreguem una font alternativa
		try {
			arcadeFont = Font.createFont(Font.TRUETYPE_FONT,this.getClass().getResourceAsStream("resources/fonts/PressStart2P.ttf"));
			metrics = getFontMetrics(arcadeFont);
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(arcadeFont);
			setFont(arcadeFont);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FontFormatException e) {
			e.printStackTrace();
		}
	}

	public void addNotify() {
		super.addNotify();
		startGame();
	}

	private void startGame() {
		if (animator == null || !running) {
			animator = new Thread(this);
			animator.start();
		}
	}

	public void pauseGame() {
		isPaused = true;
	}

	public void resumeGame() {
		isPaused = false;
	}

	public void stopGame() {
		running = false;
	}

	@Override
	public void run() {
		// volem que el temps per loop sigui constant en tots els PCs
		long afterTime, beforeTime, timeDiff, sleepTime;
		long overSleepTime = 0L;
		int noDelays = 0;
		long excess = 0L;

		beforeTime = System.nanoTime();

		running = true;
		while (running) {

			gameUpdate();
			gameRender();
			paintScreen();

			afterTime = System.nanoTime();
			timeDiff = afterTime - beforeTime;
			sleepTime = (period - timeDiff) - overSleepTime;

			if (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime/1000000L);
				} catch (InterruptedException e) {
				}
				overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
			}
			/* si no podem dormir, ho deixarem passar màxim 16 cops abans de deixar lloc
			 * a altres threads */
			else {
				excess -= sleepTime;
				overSleepTime = 0L;
				if (++noDelays >= NO_DELAYS_PER_YIELD) {
					Thread.yield();
					noDelays = 0;
				}
			}

			beforeTime = System.nanoTime();

			/* Si l'animació triga massa, no aconseguirem els FPS desitjats. Per
			 * compensar, fem gameUpdates sense renderitzar quan hem perdut frames */
			int skips = 0;
			while ((excess > period) && (skips < MAX_FRAME_SKIPS)) {
				excess -= period;
				gameUpdate();
				skips++;
			}


		}
		System.exit(0);
	}

	private void paintScreen() {
		Graphics2D g;		
		try {
			g = (Graphics2D)this.getGraphics();
			if ((g != null) && (img != null))
				g.drawImage(img, 0, 0, null);
			g.dispose();
		} catch (Exception e) {
			System.err.println("Graphics context error: "+e);
		}
	}

	private void gameUpdate() {
		if (!isPaused && !gameOver) {
			//			TODO
		}
	}

	private void gameRender() {
		if (img == null) {
			img = createImage(PWIDTH, PHEIGHT);
			if (img == null) {
				System.err.println("img is null");
				return;
			} else {
				g2d = (Graphics2D)img.getGraphics();
			}
		}

		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, 0, PWIDTH, PHEIGHT);

		//		TODO: draw elements

		if (gameOver)
			gameOverMessage(g2d);
	}

	private void gameOverMessage(Graphics2D g2d) {
		//		TODO
	}

	private void readyForTermination() {
		addKeyListener( new KeyAdapter() {
			public void keyPressed (KeyEvent e) {
				int keyCode = e.getKeyCode();
				if ((keyCode == KeyEvent.VK_ESCAPE) ||
						(keyCode == KeyEvent.VK_Q) ||
						((keyCode == KeyEvent.VK_C) && e.isControlDown())) {
					running = false;
				}
			}
		});
	}

	private void testPress(int x, int y) {
		if (!isPaused && !gameOver) {
			//			TODO
		}
	}


}
