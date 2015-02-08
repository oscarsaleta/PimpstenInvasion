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
import java.util.Random;
import java.util.Vector;

import javax.swing.JFrame;

/**
 * Classe principal del joc. Aquí es duen a terme totes les
 * tasques necessàries perquè el joc funcioni.
 * Aquí hi ha el loop principal del joc, i s'ha implementat
 * un control de FPS per tenir (dins del possible) sempre
 * el mateix nombre de frames per loop.
 * 
 * Es fa el game update (moure objectes, etc), i
 * s'implementen totes les col·lisions i l'eliminació dels
 * objectes que s'han destruit a cada loop.
 * 
 * També es controla el rendering del joc, usant double
 * buffering per evitar "flickering". El joc corre en
 * pantalla completa.
 * 
 * També s'implementen aquí els botons de la UI del joc,
 * tant del menú principal, i el mecanisme del ratolí
 * per saber si s'està passant per sobre d'un botó o si
 * s'ha fet click en un d'ells.
 * 
 * @author Oscar Saleta
 *
 */
@SuppressWarnings("serial")
public class PimpstenInvasion extends JFrame implements Runnable {

	private static final int NUM_BUFFERS = 2;
	private static final int DEFAULT_FPS = 30;
	/**
	 * Màxim nombre de frame updates sense fer sleep abans de fer un yield (
	 */
	private static final int NO_DELAYS_PER_YIELD = 16;
	/**
	 * Nombre màxim de frames no renderitzats
	 */
	private static final int MAX_FRAME_SKIPS = 5;
	/**
	 * Amplada i alçada de la pantalla
	 */
	private int pWidth, pHeight;
	private long period;

	private Thread animator;

	// booleans de control
	private volatile boolean waitingToBegin = true;
	private volatile boolean running = false;
	private volatile boolean gameOver = false;
	private boolean finishedOff = false;
	private volatile boolean isPaused = false;

	// botons pause, mute i quit (ingame)
	private volatile boolean isOverQuitInGameButton = false;
	private Rectangle quitInGameArea;
	private volatile boolean isOverPauseInGameButton = false;
	private Rectangle pauseInGameArea;
	private volatile boolean isOverMuteInGameButton = false;
	private Rectangle muteInGameArea;

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
	GraphicsManager gameGM;

	// objectes del joc
	ShipEntity ship;
	Vector<AsteroidEntity> asteroids = new Vector<AsteroidEntity>(0);
	Vector<ShotEntity> shots = new Vector<ShotEntity>(0);
	Vector<SaucerEntity> saucer = new Vector<SaucerEntity>(0);
	Vector<ShotEntity> saucerShots = new Vector<ShotEntity>(0);

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
	ScoreManager gameSM;
	private int score = 0;
	private int level = 1;
	private boolean scoreSaved = false;

	//variables per missatges en pantalla
	MessageManager gameMM;
	public boolean teleportMessage = false;
	public long lastVisibilitySwapTime = 0;
	public long visibleTime = 1000;
	public long invisibileTime = 500;
	public boolean isMessageVisible = true;
	Font aFont;


	public PimpstenInvasion(long period) {
		this.period = period;
		initFullScreen();

		// carreguem una font alternativa
		try {
			aFont = Font.createFont(Font.TRUETYPE_FONT,this.getClass().getResourceAsStream("resources/fonts/PressStart2P.ttf"));
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(aFont);
			setFont(aFont);
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
		muteInGameArea = new Rectangle(pWidth-100,pHeight-140,75,15);
		startMenuArea = new Rectangle(pWidth/2-100,pHeight/2-50,200,50);
		quitMenuArea = new Rectangle(pWidth/2-100,pHeight/2+75,200,50);
		playAgainGameOverArea = new Rectangle(pWidth/2-125,pHeight/2-50,250,50);
		quitGameOverArea = new Rectangle(pWidth/2-100,pHeight/2+75,200,50);

		// inicialitzar level 1
		restartGame();

		gameGM = new GraphicsManager(pWidth, pHeight, this);
		gameMM = new MessageManager(pWidth, pHeight, this);
		gameSM = new ScoreManager();

		gameStart();
	}

	/**
	 * Aquí s'estableix que el joc correrà en pantalla
	 * completa. Si això no pot ser, pararem l'execució
	 * (no intentarem fer un fallback en finestra)
	 */
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

	/**
	 * Creem una bufferstrategy amb 2 buffers
	 */
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

	/**
	 * Afegeix asteroides (i un saucer, si escau) al nivell
	 * @param n nombre d'asteroides
	 */
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
				asteroids.addElement(new AsteroidEntity((int)(Math.random()*4+1),(int)x,(int)y,
						Math.random()*10-5,Math.random()*10-5));
			}

			/* Cada 3 nivells volem ficar un saucer */
			if (level>1) {
				Random random = new Random();
				x = random.nextBoolean() ? 0 : pWidth;
				y = random.nextBoolean() ? 0 : pHeight;
				saucer.addElement(new SaucerEntity(x, y, 0, 0));
				SoundManager.playSaucerMusic();
			}
		}
	}

	private void gameStart() {
		if (animator == null || !running) {
			animator = new Thread(this);
			animator.start();
		}
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
			// la delta de gameUpdate() és una reminiscència de com funcionava abans el programa, ara no caldria
			gameUpdate(1);
			screenUpdate();
			// mirem quant temps hem tardat en fer l'update
			afterTime = System.nanoTime();
			timeDiff = afterTime - beforeTime;
			/* si cada loop ha de durar period, podem usar el temps que sobra per
			 * dormir (si n'hi ha) */
			sleepTime = (period - timeDiff) - overSleepTime;
			// si podem dormir, dormim
			if (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime/1000000L); // passar el temps a milisegons
				} catch (InterruptedException e) {
				}
				/* overSleepTime és la diferència entre el que realment hem dormit i el
				 *  que volíem dormir */
				overSleepTime = (System.nanoTime() - afterTime) - sleepTime;
			}
			/* si no podem dormir, ho deixarem estar màxim 16 cops abans de deixar lloc
			 * a altres threads (amb yield) */
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
			/* es surt de la pantalla completa per tornar el control
			 * a les altres aplicacions */
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
	/**
	 * Cridem les funcions necessàries del GraphicsManager
	 * per pintar-ho tot a cada loop
	 * @param g2d gràfics
	 */
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

	/**
	 * Pintar els botons de la pantalla GameOver (si el
	 * ratolí està a sobre d'un botó, aquest es pinta
	 * verd)
	 * @param g2d gràfics
	 */
	private void drawGameOverButtons(Graphics2D g2d) {
		g2d.setColor(Color.WHITE);
		g2d.setFont(aFont.deriveFont(20.0f));
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

	/**
	 * Pintar els botons del menú principal
	 * @param g2d gràfics
	 */
	private void drawStartButtons(Graphics2D g2d) {
		g2d.setColor(Color.WHITE);
		g2d.setFont(aFont.deriveFont(20.0f));
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

	/**
	 * Pintar els botons que apareixen ingame
	 * @param g2d gràfics
	 */
	private void drawButtons(Graphics2D g2d) {
		g2d.setColor(Color.WHITE);
		g2d.setFont(aFont.deriveFont(11.0f));
		FontMetrics metrics = g2d.getFontMetrics();

		if (!gameOver) {

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

			if (isOverMuteInGameButton)
				g2d.setColor(Color.GREEN);
			g2d.drawRect(muteInGameArea.x, muteInGameArea.y, muteInGameArea.width, muteInGameArea.height);
			if (SoundManager.IS_MUTED)
				g2d.drawString("Muted",muteInGameArea.x+muteInGameArea.width/2-metrics.stringWidth("Muted")/2,
						muteInGameArea.y+muteInGameArea.height*3/4);
			else 
				g2d.drawString("Mute",muteInGameArea.x+muteInGameArea.width/2-metrics.stringWidth("Mute")/2,
						muteInGameArea.y+muteInGameArea.height*3/4);
			if (isOverMuteInGameButton)
				g2d.setColor(Color.WHITE);

		}
	}

	/**
	 * Quan perd el jugador, es pinta l'explosió, es mostra
	 * la pantalla de game over i es demana el nom si hi ha
	 * hagut puntuació màxima
	 * @param g2d gràfics
	 */
	private void gameOverMessage(Graphics2D g2d) {
		gameGM.paintExplosion(ship.getX(), ship.getY(), Math.min(deadFramesCounter, 14), g2d);
		deadFramesCounter++;
		gameMM.gameOverMessage(score, g2d);

		if (deadFramesCounter >= 14 && score > gameSM.getTopScore() && !scoreSaved) {
			gameSM.writeScore(score);
			scoreSaved = true;
		}
	}

	/**
	 * Aquí es mostra a la pantalla el contigut del buffer
	 */
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
	/**
	 * Actualització de l'estat del joc
	 * @param delta interval de temps
	 */
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
			saucerShoot();
			if (asteroids.size()==0)
				levelUp();
			if (keySpace)
				shoot();
		}
	}

	/**
	 * Augmentar un nivell (i afegir asteroides/ovni)
	 */
	private void levelUp() {
		level++;
		addAsteroids((int)(Math.random()*10+5));
	}

	/**
	 * Calcular el moviment dels objectes en un
	 * determinat interval de temps
	 * @param delta interval de temps
	 */
	private void calculateMovement(long delta) {
		int i;
		ship.move(delta,pWidth,pHeight);
		for (i=0; i<asteroids.size(); i++)
			asteroids.get(i).move(delta,pWidth,pHeight);
		for (i=0; i<shots.size(); i++)
			shots.get(i).move(delta,pWidth, pHeight);
		for (i=0; i<saucer.size(); i++)
			saucer.get(i).move(delta,pWidth,pHeight);
		for (i=0; i<saucerShots.size(); i++)
			saucerShots.get(i).move(delta,pWidth,pHeight);
	}

	/**
	 * Intentar teletransportar la nau si s'ha premut la X,
	 * si no es pot es mostra el missatge de "no teleport".
	 */
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

	/**
	 * Aquí es detecten totes les col·lisions:
	 * asteroide-nau, asteroide-tret, ovni-nau,
	 * ovni-tret, nau-tret(ovni)
	 */
	private void detectCollisions() {
		int i,j;
		//iterarem per a  cada asteroide
		for (i=0; i<asteroids.size(); i++) {
			AsteroidEntity a = asteroids.get(i);
			//mirem xocs asteroide-nau
			if (a.getBounds().intersects(
					ship.getX(),ship.getY(),ship.getWidth(),ship.getHeight()))
				shipCollidesWithAsteroid();
			//mirem xocs asteroide-tret(nau)			
			for (j=0; j<shots.size(); j++) {
				ShotEntity s = shots.get(j);
				if (a.getBounds().intersects(
						s.getX(), s.getY(),s.getWidth(),s.getHeight())
						&& s.status == true) //evita que un tret trenqui 2 asteroides
					shotCollidesWithAsteroid(s,a);
			}
		}
		//iterarem pel saucer
		for (i=0; i<saucer.size(); i++) {
			SaucerEntity sau = saucer.get(i);
			if (sau.getBounds().intersects(
					ship.getX(),ship.getY(),ship.getWidth(),ship.getHeight()))
				shipCollidesWithSaucer(sau);
			// mirem xocs tret-saucer
			for (j=0; j<shots.size(); j++) {
				ShotEntity s = shots.get(j);
				if (sau.getBounds().intersects(
						s.getX(), s.getY(),s.getWidth(),s.getHeight())
						&& s.status == true)
					shotCollidesWithSaucer(s,sau);
			}
		}
		// iterarem pels trets del saucer
		for (i=0; i<saucerShots.size(); i++) {
			ShotEntity s = saucerShots.get(i);
			// mirem xocs tret(saucer)-nau
			if (ship.getBounds().intersects(
					s.getX(),s.getY(),s.getWidth(),s.getHeight()))
				shipCollidesWithSaucerShot(s);
		}
	}

	/**
	 * Funció que regeix la col·lisió d'un tret(nau) amb un ovni
	 * @param s tret (nau)
	 * @param sau ovni
	 */
	private void shotCollidesWithSaucer(ShotEntity s, SaucerEntity sau) {
		s.status = false;
		sau.status = false;
		score+=50;
		SoundManager.playLargeExplosion();
	}

	/**
	 * La nau xoca contra un asteroide. Game over.
	 */
	private void shipCollidesWithAsteroid() {
		ship.status = false;
		SoundManager.playLargeExplosion();
		gameOver = true;
	}

	/**
	 * La nau xoca contra un ovni. Es fa el mateix que
	 * amb un asteroide però l'ovni també es trenca 
	 * @param sau
	 */
	private void shipCollidesWithSaucer(SaucerEntity sau) {
		sau.status = false;
		shipCollidesWithAsteroid();
	}

	/**
	 * L'ovni ha disparat a la nau! Game Over
	 * @param s tret (ovni)
	 */
	private void shipCollidesWithSaucerShot(ShotEntity s) {
		s.status = false;
		shipCollidesWithAsteroid();
	}

	/**
	 * Es trenca un asteroide d'un tret de la nau
	 * @param s tret
	 * @param a asteroide
	 */
	private void shotCollidesWithAsteroid(ShotEntity s, AsteroidEntity a) {
		a.status = false;
		s.status = false;
		score+=10;
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

	/**
	 * S'esborren les entitats que tinguin status==false
	 * del seu vector corresponent (i.e. les que han xocat,
	 * o trets que s'han sortit de la pantalla)
	 */
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
		for (i=0; i<saucer.size(); i++) {
			SaucerEntity s = saucer.get(i);
			if (s.status == false)
				saucer.removeElement(s);
		}
		for (i=0; i<saucerShots.size(); i++) {
			ShotEntity s = saucerShots.get(i);
			if (s.status == false)
				saucerShots.removeElement(s);
		}
	}

	/**
	 * Events del teclat fan moure la nau
	 */
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

	/**
	 * Quan es prem l'espai, s'afegeix un tret
	 * al vector de trets de la nau.
	 */
	private void shoot() {
		if (System.currentTimeMillis()-lastShotTime > timeBetweenShots && ship.status) {
			lastShotTime = System.currentTimeMillis();
			shots.addElement(ship.fireShot());
			SoundManager.playShot();
		}
	}

	/**
	 * L'ovni dispara de forma aleatòria cada
	 * mig segon.
	 */
	private void saucerShoot() {
		for (int i=0; i<saucer.size(); i++) {
			SaucerEntity sau = saucer.get(i);
			ShotEntity s = sau.fireShot();
			if (s != null) {
				saucerShots.add(s);
				SoundManager.playShot();
			}
		}

	}

	/**
	 * Reiniciar la partida (tots els vectors es buiden,
	 * els booleans, la puntuació i el nivell es
	 * reinicien, la nau es recol·loca al mig de la
	 * pantalla, i es generen nous asteroides)
	 */
	private void restartGame() {
		// netejar objectes de la partida anterior
		asteroids.clear();
		shots.clear();
		saucer.clear();
		saucerShots.clear();
		deadFramesCounter=0;
		score=0;
		scoreSaved = false;
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
		SoundManager.playBackgroundMusic();
		// per acabar: game over és fals
		gameOver = false;

	}


	//----KEYBOARD-CONTROLS--------------------------------

	/**
	 * Aquesta funció afegeix un listener de teclat
	 * per tancar el programa si es prem la Q, Ctrl+C
	 * o ESC, i també fa un shutdown hook per assegurar
	 * que tots els processos del joc es tanquen abans
	 * de perdre'n el control (e.g. perquè no segueixi
	 * sonant la música quan hem tancat el joc)
	 */
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
		/* Per estar realment llestos per acabar el programa, necessitem estar
		 * segurs de que quan el tanquem es tanquin tots els threads oberts.
		 * No volem que simplement es deixi de veure la imatge en fullscreen pero
		 * el joc segueixi ocupant memòria.
		 * Per això usem un shutdown hook, que és un thread que s'executarà quan
		 * intentem matar el programa amb System.exit.
		 * Aquest en particular crida la funció finishOff(), on hem fet que s'alliberi
		 * el control de la pantalla i es surti del procés del joc.
		 */
		Runtime.getRuntime().addShutdownHook(new Thread () {
			public void run() {
				finishOff();
			}
		});
	}

	/**
	 * Click de ratolí a les coordenades (x,y).
	 * Ens serveix per controlar els clicks als
	 * botons de la UI.
	 * @param x coordenada x del cursor
	 * @param y coordenada y del cursor
	 */
	private void testPress(int x, int y) {
		if (isOverPauseInGameButton)
			isPaused = !isPaused;
		else if (isOverQuitInGameButton)
			running = false;
		else if (isOverMuteInGameButton) {
			if (SoundManager.IS_MUTED)
				SoundManager.unMuteAllSound();
			else
				SoundManager.muteAllSounds();
		}
		else if (isOverQuitMenuButton && waitingToBegin)
			running = false;
		else if (isOverStartMenuButton && waitingToBegin)
			waitingToBegin = false;
		else if (isOverPlayAgainGameOverButton && gameOver) {
			// begin again.
			restartGame();
		}
		else if (isOverQuitGameOverButton && gameOver)
			running = false;
		else {
			// do nothing
		}

	}
	/**
	 * Simplement mira on està el ratolí (sense el
	 * click) per veure si hem de canviar el color
	 * dels botons.
	 * @param x coordenada x del ratolí
	 * @param y coordenada y del ratolí
	 */
	private void testMove(int x, int y) {
		if (waitingToBegin) {
			isOverStartMenuButton = startMenuArea.contains(x,y);
			isOverQuitMenuButton = quitMenuArea.contains(x,y);
		} else if (gameOver) {
			isOverPlayAgainGameOverButton = playAgainGameOverArea.contains(x,y);
			isOverQuitGameOverButton = quitGameOverArea.contains(x,y);
		} else if (running) {
			isOverPauseInGameButton = pauseInGameArea.contains(x,y);
			isOverQuitInGameButton = quitInGameArea.contains(x,y);
			isOverMuteInGameButton = muteInGameArea.contains(x,y);
		}
	}

	/**
	 * Main, cridem al constructor del joc i aquest
	 * ho farà tot
	 * @param args no hi ha args
	 */
	public static void main(String[] args) {
		new PimpstenInvasion(DEFAULT_FPS);
	}
}
