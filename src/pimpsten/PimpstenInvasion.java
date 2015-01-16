package pimpsten;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFrame;

/**
 * Classe principal del joc?
 * 
 * @author Oscar Saleta
 *
 */
@SuppressWarnings("serial")
public class PimpstenInvasion extends JFrame implements Runnable {

	//	private Game game;

	private static final int NUM_BUFFERS = 2;
	private static final int DEFAULT_FPS = 60;
	/**
	 * Màxim nombre de frame updates sense fer sleep abans de fer un yield
	 */
	private static final int NO_DELAYS_PER_YIELD = 16;
	/**
	 * Nombre màxim de frames no renderitzats
	 */
	private static final int MAX_FRAME_SKIPS = 5;
	/**
	 * Amplada i alçada de la finestra
	 */
	private int pWidth, pHeight;

	/**
	 * Per tenir entre 80 i 85 FPS (està en nanosegons)
	 */
	private long period;

	private Thread animator;

	// booleans de control
	private volatile boolean waitingToBegin = true;
	private volatile boolean running = false;
	private volatile boolean gameOver = false;
	private boolean finishedOff = false;
	private volatile boolean isPaused = false;

	// botons pause i quit (ingame)
	private volatile boolean isOverQuitInGameButton = false;
	private Rectangle quitInGameArea;
	private volatile boolean isOverPauseInGameButton = false;
	private Rectangle pauseInGameArea;

	// botons start i quit (menu ppal)
	private volatile boolean isOverStartMenuButton = false;
	private Rectangle startMenuArea;
	private volatile boolean isOverQuitMenuButton = false;
	private Rectangle quitMenuArea;

	// botons play again i quit (gameover)
	private volatile boolean isOverPlayAgainGameOverButton = false;
	private Rectangle playAgainGameOverArea;
	private volatile boolean isOverQuitGameOverButton = false;
	private Rectangle quitGameOverArea;

	// variables gràfiques
	private GraphicsDevice gd;
	private Graphics2D g2d;
	private BufferStrategy bs;
	//	private Image img = null;
	GraphicsManager gameGM;

	// objectes del joc
	ShipEntity ship;
	Vector<AsteroidEntity> asteroids = new Vector<AsteroidEntity>(0);
	Vector<ShotEntity> shots = new Vector<ShotEntity>(0);

	// booleans per input per teclat
	public boolean keyLeft = false;
	public boolean keyRight = false;
	public boolean keyUp = false;
	public boolean keyDown = false;
	public boolean keyX = false;
	public boolean keySpace = false;

	// variables temporals pels trets i teleports
	private long lastShotTime = 0;
	private long timeBetweenShots = 300;
	private long lastTeleportTime = 0;
	private long lastTeleportTryTime = 0;
	private long timeBetweenTeleports = 3000;
	int deadFramesCounter = 0;

	// puntuació i nivell
	private int score = 0;
	private int level = 1;

	//variables per missatges en pantalla
	MessageManager gameMM;
	public boolean teleportMessage = false;
	public long lastVisibilitySwapTime = 0;
	public long visibleTime = 1000;
	public long invisibileTime = 500;
	public boolean isMessageVisible = true;
	Font arcadeFont;


	public PimpstenInvasion(long period) {
		this.period = period;
		initFullScreen();

		// carreguem una font alternativa
		try {
			arcadeFont = Font.createFont(Font.TRUETYPE_FONT,this.getClass().getResourceAsStream("resources/fonts/PressStart2P.ttf"));
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(arcadeFont);
			setFont(arcadeFont);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FontFormatException e) {
			e.printStackTrace();
		}

		readyForTermination();
		addKeyListener(new KeyboardEventManager(this));

		addMouseListener( new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				testPress(e.getX(),e.getY());
			}
		});
		addMouseMotionListener( new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				testMove(e.getX(),e.getY());
			}
		});

		// arees pels botons del joc (no són botons reals)
		pauseInGameArea = new Rectangle(pWidth-100,pHeight-100,75,15);
		quitInGameArea = new Rectangle(pWidth-100,pHeight-60,75,15);
		startMenuArea = new Rectangle(pWidth/2-100,pHeight/2-50,200,50);
		quitMenuArea = new Rectangle(pWidth/2-100,pHeight/2+75,200,50);
		playAgainGameOverArea = new Rectangle(pWidth/2-125,pHeight/2-50,250,50);
		quitGameOverArea = new Rectangle(pWidth/2-100,pHeight/2+75,200,50);

		// inicialitzar level 1
		restartGame();

		gameGM = new GraphicsManager(pWidth, pHeight, this);
		gameMM = new MessageManager(pWidth, pHeight, this);

		gameStart();
	}

	private void initFullScreen() {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		gd = ge.getDefaultScreenDevice();

		setUndecorated(true);
		setIgnoreRepaint(true);
		setResizable(false);

		if (!gd.isFullScreenSupported()) {
			System.err.println("Fullscreen not supported.");
			System.exit(0);
		}
		gd.setFullScreenWindow(this);

		pWidth = getBounds().width;
		pHeight = getBounds().height;

		setBufferStrategy();
	}

	private void setBufferStrategy() {
		try {
			createBufferStrategy(NUM_BUFFERS);
		} catch (Exception e) {
			System.err.println("Error creating buffer strategy.");
			System.exit(0);
		}

		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {}

		bs = getBufferStrategy();
	}

	private void addAsteroids(int n) {
		int i;
		double x,y;
		if (asteroids.size() == 0) {
			for (i=0; i<n; i++) {
				//calculem la posició x del nou asteroide, no volem que estigui a prop de la nau
				if (ship.getX() < pWidth/4) {
					x = (Math.random()+1)*pWidth/2;
				} else if (ship.getX() > 3*pWidth/4) {
					x = Math.random()*pWidth/2;
				} else {
					do {
						x = Math.random()*pWidth;
					} while (x > pWidth/4 && x < 3*pWidth/4);
				}
				//calculem la posició y del nou asteroide
				if (ship.getY() < pHeight/4) {
					y = (Math.random()+1)*pHeight/2;
				} else if (ship.getY() > 3*pHeight/4) {
					y = Math.random()*pHeight/2;
				} else {
					do {
						y = Math.random()*pHeight;
					} while (y > pHeight/4 && y < 3*pHeight/4);
				}

				asteroids.addElement(new AsteroidEntity((int)(Math.random()*5+1),(int)x,(int)y,
						Math.random()*10-5,Math.random()*10-5));
			}
		}
	}

	private void gameStart() {
		if (animator == null || !running) {
			animator = new Thread(this);
			animator.start();
		}
	}

	//----CONTROLS--------------------------------
	public void pauseGame() {
		isPaused = true;
	}

	public void resumeGame() {
		isPaused = false;
	}

	public void stopGame() {
		running = false;
	}


	//----RUN------------------------------------------
	@Override
	public void run() {
		// volem que el temps per loop sigui constant en tots els PCs
		long afterTime, beforeTime, timeDiff=0L, sleepTime;
		long overSleepTime = 0L;
		int noDelays = 0;
		long excess = 0L;

		beforeTime = System.nanoTime();

		running = true;
		while (running) {

			gameUpdate(2);
			screenUpdate();

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
				gameUpdate(timeDiff/2000000L);
				skips++;
			}


		}
		finishOff();
	}

	private void finishOff() {
		if (!finishedOff) {
			finishedOff = true;
			restoreScreen();
			System.exit(0);
		}
	}

	private void restoreScreen() {
		Window w = gd.getFullScreenWindow();
		if (w != null)
			w.dispose();
		gd.setFullScreenWindow(null);
	}

	//----GAME-RENDERING-----------------------------
	private void gameRender(Graphics2D g2d) {
		if (waitingToBegin) {
			gameGM.paintBackground(g2d);
			gameGM.paintOnlyAsteroids(g2d);
			gameMM.startScreen(g2d);
			drawStartButtons(g2d);
		}
		else {
			gameGM.paintBackground(g2d);
			gameGM.paintObjects(g2d);	
			// game over?
			if (gameOver) {
				gameOverMessage(g2d);
				drawGameOverButtons(g2d);
			}
			//pintar botons quit i pause
			drawButtons(g2d);
			//pintar score i level
			gameMM.currentScore(score, level, g2d);
		}
	}

	private void drawGameOverButtons(Graphics2D g2d) {
		g2d.setColor(Color.WHITE);
		g2d.setFont(arcadeFont.deriveFont(20.0f));
		FontMetrics metrics = g2d.getFontMetrics();

		if (isOverPlayAgainGameOverButton)
			g2d.setColor(Color.GREEN);
		g2d.drawRect(playAgainGameOverArea.x, playAgainGameOverArea.y, playAgainGameOverArea.width, playAgainGameOverArea.height);
		g2d.drawString("Play again",playAgainGameOverArea.x+playAgainGameOverArea.width/2-metrics.stringWidth("Play Again")/2,
				playAgainGameOverArea.y+playAgainGameOverArea.height*3/4);
		if (isOverPlayAgainGameOverButton)
			g2d.setColor(Color.WHITE);

		if (isOverQuitGameOverButton)
			g2d.setColor(Color.GREEN);
		g2d.drawRect(quitGameOverArea.x, quitGameOverArea.y, quitGameOverArea.width, quitGameOverArea.height);
		g2d.drawString("QUIT",quitGameOverArea.x+quitGameOverArea.width/2-metrics.stringWidth("QUIT")/2,
				quitGameOverArea.y+quitGameOverArea.height*3/4);
		if (isOverQuitGameOverButton)
			g2d.setColor(Color.WHITE);
	}

	private void drawStartButtons(Graphics2D g2d) {
		g2d.setColor(Color.WHITE);
		g2d.setFont(arcadeFont.deriveFont(20.0f));
		FontMetrics metrics = g2d.getFontMetrics();

		if (isOverStartMenuButton)
			g2d.setColor(Color.GREEN);
		g2d.drawRect(startMenuArea.x, startMenuArea.y, startMenuArea.width, startMenuArea.height);
		g2d.drawString("START",startMenuArea.x+startMenuArea.width/2-metrics.stringWidth("START")/2,
				startMenuArea.y+startMenuArea.height*3/4);
		if (isOverStartMenuButton)
			g2d.setColor(Color.WHITE);

		if (isOverQuitMenuButton)
			g2d.setColor(Color.GREEN);
		g2d.drawRect(quitMenuArea.x, quitMenuArea.y, quitMenuArea.width, quitMenuArea.height);
		g2d.drawString("QUIT",quitMenuArea.x+quitMenuArea.width/2-metrics.stringWidth("QUIT")/2,
				quitMenuArea.y+quitMenuArea.height*3/4);
		if (isOverQuitMenuButton)
			g2d.setColor(Color.WHITE);
	}

	private void drawButtons(Graphics2D g2d) {
		g2d.setColor(Color.WHITE);
		g2d.setFont(arcadeFont.deriveFont(11.0f));
		FontMetrics metrics = g2d.getFontMetrics();

		if (isOverPauseInGameButton)
			g2d.setColor(Color.GREEN);
		g2d.drawRect(pauseInGameArea.x, pauseInGameArea.y, pauseInGameArea.width, pauseInGameArea.height);
		if (isPaused)
			g2d.drawString("Paused",pauseInGameArea.x+pauseInGameArea.width/2-metrics.stringWidth("Paused")/2,
					pauseInGameArea.y+pauseInGameArea.height*3/4);
		else 
			g2d.drawString("Pause",pauseInGameArea.x+pauseInGameArea.width/2-metrics.stringWidth("Pause")/2,
					pauseInGameArea.y+pauseInGameArea.height*3/4);
		if (isOverPauseInGameButton)
			g2d.setColor(Color.WHITE);

		if (isOverQuitInGameButton)
			g2d.setColor(Color.GREEN);
		g2d.drawRect(quitInGameArea.x, quitInGameArea.y, quitInGameArea.width, quitInGameArea.height);
		g2d.drawString("Quit", quitInGameArea.x+quitInGameArea.width/2-metrics.stringWidth("Quit")/2,
				quitInGameArea.y+quitInGameArea.height*3/4);
		if (isOverQuitInGameButton)
			g2d.setColor(Color.WHITE);
	}

	private void gameOverMessage(Graphics2D g2d) {
		gameGM.paintExplosion(ship.getX(), ship.getY(), Math.min(deadFramesCounter, 14), g2d);
		deadFramesCounter++;
		gameMM.waitMessage(score, g2d);
	}

	private void screenUpdate() {
		try {
			g2d = (Graphics2D)bs.getDrawGraphics();
			gameRender(g2d);
			g2d.dispose();
			if (!bs.contentsLost())
				bs.show();
			else
				System.err.println("BufferStrategy content lost.");
		} catch (Exception e) {
			e.printStackTrace();
			running = false;
		}
	}


	//----GAME-UPDATE--------------------------------
	private void gameUpdate(long delta) {
		if (waitingToBegin) {
			// do nothing but wait
		}
		else if (!isPaused && !gameOver) {
			calculateMovement(delta);
			teleport();
			detectCollisions();
			eraseDeadEntities();
			moveShip();
			if (asteroids.size()==0)
				levelUp();
			if (keySpace)
				shoot();
		}
	}


	private void levelUp() {
		addAsteroids((int)(Math.random()*10+5));
		level++;
	}

	private void calculateMovement(long delta) {
		int i;
		ship.move(delta,pWidth,pHeight);
		for (i=0; i<asteroids.size(); i++) {
			asteroids.get(i).move(delta,pWidth,pHeight);
		}
		for (i=0; i<shots.size(); i++)
			shots.get(i).move(delta,pWidth, pHeight);
	}

	private void teleport() {
		if (keyX) {
			if (System.currentTimeMillis()-lastTeleportTime > timeBetweenTeleports) {
				lastTeleportTime = System.currentTimeMillis();
				ship.teleport(pWidth, pHeight);
				teleportMessage = false;
			} else {
				lastTeleportTryTime = System.currentTimeMillis();
				if (lastTeleportTryTime-lastTeleportTime > 100)
					teleportMessage = true;
			}
		} else {
			if (System.currentTimeMillis()-lastTeleportTryTime > 1000)
				teleportMessage = false;
		}
	}

	private void detectCollisions() {
		int i,j;
		//iterarem per a  cada asteroide
		for (i=0; i<asteroids.size(); i++) {
			AsteroidEntity a = asteroids.get(i);
			//mirem xocs asteroide-nau
			if (a.getBounds().intersects(
					ship.getX(),ship.getY(),ship.getWidth(),ship.getHeight()))
				shipCollidesWithAsteroid();
			//mirem xocs asteroide-tret
			for (j=0; j<shots.size(); j++) {
				ShotEntity s = shots.get(j);
				if (a.getBounds().intersects(
						s.getX(), s.getY(),s.getWidth(),s.getHeight())
						&& s.status == true) //evita que un tret trenqui 2 asteroides
					shotCollidesWithAsteroid(s,a);
			}
		}
	}

	private void shipCollidesWithAsteroid() {
		ship.status = false;
//		TODO: so d'explosió
		SoundManager.playLargeExplosion();
		gameOver = true;
	}

	private void shotCollidesWithAsteroid(ShotEntity s, AsteroidEntity a) {
		a.status = false;
		s.status = false;
		score+=10;
//		TODO: explosió asteroide trencat
		SoundManager.playMediumExplosion();
		//mirem si creem 2 asteroides més petits
		if(a.explodes()) {
			double theta = Math.toRadians(30);
			double randomModifier = Math.random()*3;
			asteroids.addElement(
					new AsteroidEntity(a.getHp()/2,a.getMiniX(),a.getMiniY(),
							a.getVx()*randomModifier*Math.cos(theta),a.getVy()*randomModifier*Math.sin(theta)));
			asteroids.addElement(
					new AsteroidEntity(a.getHp()/2,a.getMiniX(),a.getMiniY(),
							a.getVx()*randomModifier*Math.cos(-theta),a.getVx()*randomModifier*Math.sin(-theta)));
		}
	}

	private void eraseDeadEntities() {
		int i;
		for (i=0; i<asteroids.size(); i++) {
			AsteroidEntity a = asteroids.get(i);
			if (a.status == false)
				asteroids.removeElement(a);
		}
		for (i=0; i<shots.size(); i++) {
			ShotEntity s = shots.get(i);
			if (s.status == false)
				shots.removeElement(s);
		}
	}

	private void moveShip() {
		if (keyUp && !keyDown)
			ship.increaseSpeedForward();
		else if (!keyUp && keyDown)
			ship.increaseSpeedBackwards();
		if (keyRight && !keyLeft)
			ship.turnRight();
		else if (!keyRight && keyLeft)
			ship.turnLeft();
	}

	private void shoot() {
		if (System.currentTimeMillis()-lastShotTime > timeBetweenShots && ship.status) {
			lastShotTime = System.currentTimeMillis();
			shots.addElement(ship.fireShot());
//			TODO: soroll de tret
			SoundManager.playShot();
		}
	}

	private void restartGame() {
		// netejar objectes de la partida anterior
		asteroids.clear();
		shots.clear();
		deadFramesCounter=0;
		score=0;
		level=1;
		// netejar tecles de la partida anterior
		keyUp=false;
		keyDown=false;
		keyLeft=false;
		keyRight=false;
		keyX=false;
		keySpace=false;
		// inicialitzar variables per una nova partida
		ship = new ShipEntity(pWidth/2, pHeight/2);
		addAsteroids(5);
		// música!
		SoundManager.playBackgroundMusic(0);
		// per acabar: game over és fals
		gameOver = false;
		
	}


	//----KEYBOARD-CONTROLS--------------------------------

	private void readyForTermination() {
		addKeyListener( new KeyAdapter() {
			public void keyPressed (KeyEvent e) {
				int keyCode = e.getKeyCode();
				if ((keyCode == KeyEvent.VK_ESCAPE) ||
						(keyCode == KeyEvent.VK_Q) ||
						((keyCode == KeyEvent.VK_C) && e.isControlDown())) {
					running = false;
				}
				if (keyCode == KeyEvent.VK_P) {
					if (isPaused)
						isPaused = false;
					else
						isPaused = true;
				}
			}
		});
		Runtime.getRuntime().addShutdownHook(new Thread () {
			public void run() {
				finishOff();
			}
		});
	}

	private void testPress(int x, int y) {
		if (isOverPauseInGameButton)
			isPaused = !isPaused;
		else if (isOverQuitInGameButton)
			running = false;
		else if (isOverQuitMenuButton && waitingToBegin)
			running = false;
		else if (isOverStartMenuButton && waitingToBegin)
			waitingToBegin = false;
		else if (isOverPlayAgainGameOverButton && gameOver) {
			// begin again. how?
			restartGame();
		}
		else if (isOverQuitGameOverButton && gameOver)
			running = false;
		else {
			// do nothing
		}

	}
	private void testMove(int x, int y) {
		if (waitingToBegin) {
			isOverStartMenuButton = startMenuArea.contains(x,y);
			isOverQuitMenuButton = quitMenuArea.contains(x,y);
		} else if (running) {
			isOverPauseInGameButton = pauseInGameArea.contains(x,y);
			isOverQuitInGameButton = quitInGameArea.contains(x,y);
			if (gameOver) {
				isOverPlayAgainGameOverButton = playAgainGameOverArea.contains(x,y);
				isOverQuitGameOverButton = quitGameOverArea.contains(x,y);
			}
		}
	}

	public static void main(String[] args) {
		new PimpstenInvasion(DEFAULT_FPS);
	}
}
