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
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.JTextField;

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
	private static final int PWIDTH = 1200;
	/**
	 * Alçada de la finestra
	 */
	private static final int PHEIGHT = 700;
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

	// booleans de control
	private boolean running = false;
	private boolean gameOver = false;
	private boolean isPaused = false;

	// variables gràfiques
	private Graphics2D g2d;
	private Image img = null;
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

	//	BufferedImage explosion
	//	
	//	try {
	//		URL url = this.getClass().getResource("resources/graphics/explosion.png");
	//		BufferedImage img = ImageIO.read(url);
	//		g.drawImage(img, x-scale*5, y-scale*5, scale*10, scale*10, null);
	//	} catch (IOException e) {
	//		e.printStackTrace();
	//	}

	public GamePanel(Game game, long period) {
		this.game = game;
		this.period = period;
		setBackground(Color.BLACK);
		setPreferredSize(new Dimension(PWIDTH,PHEIGHT));

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

		setFocusable(true);
		requestFocus();
		readyForTermination();
		addKeyListener(new KeyboardEventManager(this));

		//		TODO: create game components
		ship = new ShipEntity(PWIDTH/2, PHEIGHT/2);
		addAsteroids(5);

		gameGM = new GraphicsManager(PWIDTH, PHEIGHT, this);
		gameMM = new MessageManager(PWIDTH, PHEIGHT, this);
	}

	private void addAsteroids(int n) {
		int i;
		double x,y;
		if (asteroids.size() == 0) {
			for (i=0; i<n; i++) {
				//calculem la posició x del nou asteroide, no volem que estigui a prop de la nau
				if (ship.getX() < PWIDTH/4) {
					x = (Math.random()+1)*PWIDTH/2;
				} else if (ship.getX() > 3*PWIDTH/4) {
					x = Math.random()*PWIDTH/2;
				} else {
					do {
						x = Math.random()*PWIDTH;
					} while (x > PWIDTH/4 && x < 3*PWIDTH/4);
				}
				//calculem la posició y del nou asteroide
				if (ship.getY() < PHEIGHT/4) {
					y = (Math.random()+1)*PHEIGHT/2;
				} else if (ship.getY() > 3*PHEIGHT/4) {
					y = Math.random()*PHEIGHT/2;
				} else {
					do {
						y = Math.random()*PHEIGHT;
					} while (y > PHEIGHT/4 && y < 3*PHEIGHT/4);
				}

				asteroids.addElement(new AsteroidEntity((int)(Math.random()*5+1),(int)x,(int)y,
						Math.random()*10-5,Math.random()*10-5));
			}
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
		while (!running) {
			
		}
		
		while (running) {

			gameUpdate(2);
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
				gameUpdate(timeDiff/2000000L);
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


	//----GAME-UPDATE--------------------------------
	private void gameUpdate(long delta) {
		if (!isPaused && !gameOver) {
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
		game.setLevelBoxNumber(++level);
		
	}

	private void calculateMovement(long delta) {
		int i;
		ship.move(delta,PWIDTH,PHEIGHT);
		for (i=0; i<asteroids.size(); i++) {
			asteroids.get(i).move(delta,PWIDTH,PHEIGHT);
		}
		for (i=0; i<shots.size(); i++)
			shots.get(i).move(delta,PWIDTH, PHEIGHT);
	}

	private void teleport() {
		if (keyX) {
			if (System.currentTimeMillis()-lastTeleportTime > timeBetweenTeleports) {
				lastTeleportTime = System.currentTimeMillis();
				ship.teleport(PWIDTH, PHEIGHT);
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
						&& s.status == true) {//evita que un tret trenqui 2 asteroides
					shotCollidesWithAsteroid(s,a);
					score+=10;
					game.setScoreBoxNumber(score);
				}
			}
		}
	}

	private void shipCollidesWithAsteroid() {
		ship.status = false;
		SoundManager.playLargeExplosion();
		gameOver = true;
	}

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
			SoundManager.playShot();
		}
	}


	//----GAME-RENDERING-----------------------------
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

		gameGM.paintBackground(g2d);
		//		g2d.setColor(Color.BLACK);
		//		g2d.fillRect(0, 0, PWIDTH, PHEIGHT);

		//		TODO: draw elements

		gameGM.paintObjects(g2d);		
		//		int i;
		//		if (ship.status == true)
		//			ship.paint(g2d);
		//		for (i=0; i<asteroids.size(); i++)
		//			asteroids.get(i).paint(g2d);
		//		for (i=0; i<shots.size(); i++)
		//			shots.get(i).paint(g2d);		
		//		if (teleportMessage) {
		//			final Font messageFont = arcadeFont.deriveFont(13.0f);
		//			final FontMetrics messageMetrics = getFontMetrics(messageFont);
		//			String m = "Teleport on cooldown";
		//			g2d.setFont(messageFont);
		//			g2d.setColor(Color.WHITE);
		//			g2d.drawString(m,(PWIDTH-messageMetrics.stringWidth(m))/2,PHEIGHT-100);
		//		}
		if (gameOver)
			gameOverMessage(g2d);
	}

	private void gameOverMessage(Graphics2D g2d) {
		gameGM.paintExplosion(ship.getX(), ship.getY(), Math.min(deadFramesCounter, 14), g2d);
		deadFramesCounter++;
		gameMM.waitMessage(score, g2d);

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
	}

}
