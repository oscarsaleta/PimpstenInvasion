package pimpsten;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * Classe principal del joc. S'encarrega de:
 * - part visual (finestra del joc, pintar components)
 * - motor del joc (evolució temporal dels components)
 * 
 * @author Oscar Saleta
 *
 */
@SuppressWarnings("serial")
public class Game extends Canvas {

	//	public static Game sharedInstance;

	//variables de finestra i doublebuffering
	JFrame window;
	JPanel panel;

	private Dimension windowSize = new Dimension(800,600);
	public BufferStrategy bs;

	//variables que controlen el flux del joc
	public int gameEvent = 1;
	public boolean waitingForKeyPress = true;
	private int victoryCounter = 0;

	//variables per missatges en pantalla
	public MessageManager gameMM;
	public boolean teleportMessage = false;
	public long lastVisibilitySwapTime = 0;
	public long visibleTime = 1000;
	public long invisibileTime = 500;
	public boolean isMessageVisible = true;
	Font arcadeFont;

	//variables per gràfics
	private int deadFramesCount;
	public GraphicsManager gameGM;

	//variables per puntuació
	private int score = 0;
	private int highScore;
	public ScoreManager gameSM;

	//variables temporals pels trets i teleports
	private long lastShotTime = 0;
	private long timeBetweenShots = 300;
	private long lastTeleportTime = 0;
	private long lastTeleportTryTime = 0;
	private long timeBetweenTeleports = 3000;

	//variables per manejar l'input per teclat (podrem premer 2 tecles alhora)
	public boolean keyLeft = false;
	public boolean keyRight = false;
	public boolean keyUp = false;
	public boolean keyDown = false;
	public boolean keyX = false;
	public boolean keySpace = false;

	//objectes del nostre joc (nau, asteroides i trets)
	ShipEntity ship;
	Vector<AsteroidEntity> asteroids = new Vector<AsteroidEntity>(0);
	Vector<ShotEntity> shots = new Vector<ShotEntity>(0);

	public static void main(String[] args) {
		Game game = new Game();
		game.gameLoop();
	}

	Game() {
		//tindrem la finestra dins la mateixa classe del joc
		window = new JFrame("Pimpsten Invasion (nightly)");
		//les imatges es pinten al panel
		panel = (JPanel) window.getContentPane();
		panel.setPreferredSize(windowSize);
		panel.setBackground(Color.BLACK);
		panel.setLayout(null);

		setBounds(0,0,windowSize.width,windowSize.height);
		panel.add(this);

		//swing pot fer doublebuffering aixi que no volem que awt repinti el joc
		setIgnoreRepaint(true);

		//fem que la finestra tingui la mida que volem i la mostrem
		window.pack();
		window.setResizable(false);
		window.setVisible(true);

		//si tanquem la finestra java no matarà el joc perquè hi haurà altres objectes en
		//memòria, així que el forcem a fer-ho
		window.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		new StartMenu(this);

		//carreguem la font del joc des de la carpeta de recursos
		try {
			arcadeFont = Font.createFont(Font.TRUETYPE_FONT,this.getClass().getResourceAsStream("resources/fonts/PressStart2P.ttf"));
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(arcadeFont);
			panel.setFont(arcadeFont);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FontFormatException e) {
			e.printStackTrace();
		}

		//aquest key listener es una classe que maneja l'input per teclat
		addKeyListener(new KeyboardEventManager(this));
		requestFocus();

		//doublebuffering pel nostre canvas!
		createBufferStrategy(2);
		bs = getBufferStrategy();

		//creo el gestor de missatges i el gestor gràfic
		gameMM = new MessageManager(windowSize.width, windowSize.height,this);
		gameGM = new GraphicsManager(windowSize.width,windowSize.height,this);
		gameSM = new ScoreManager("resources/scores/scores.txt",this);

		initializeVariables();

		SoundManager.playBackgroundMusic();
	}

	public void beginGame() {
		//netejar objectes de la partida anterior
		asteroids.clear();
		shots.clear();
		deadFramesCount=0;
		score=0;
		victoryCounter=0;
		//netejar tecles de la partida anterior
		keyUp=false;
		keyDown=false;
		keyLeft=false;
		keyRight=false;
		keyX=false;
		keySpace=false;
		//iniciar objectes per la nova partida
		initializeVariables();
	}

	private void initializeVariables() {
		//creem la nau
		ship = new ShipEntity(windowSize.width/2, windowSize.height/2);
		//creem els asteroides (per començar només 5)
		addAsteroids(5);
		//mirem quina és la puntuació màxima abans de començar un nou joc
		highScore = gameSM.getHighScore();
	}

	private void addAsteroids(int quantity) {
		int i;
		double x,y;
		quantity+=victoryCounter;
		if (asteroids.size() == 0) {
			for (i=0; i<quantity; i++) {
				//calculem la posició x del nou asteroide
				//no volem que estigui a prop de la nau
				if (ship.getX() < windowSize.width/4) {
					x = (Math.random()+1)*windowSize.width/2;
				} else if (ship.getX() > 3*windowSize.width/4) {
					x = Math.random()*windowSize.width/2;
				} else {
					do {
						x = Math.random()*windowSize.width;
					} while (x > windowSize.width/4 && x < 3*windowSize.width/4);
				}
				//calculem la posició y del nou asteroide
				if (ship.getY() < windowSize.height/4) {
					y = (Math.random()+1)*windowSize.height/2;
				} else if (ship.getY() > 3*windowSize.height/4) {
					y = Math.random()*windowSize.height/2;
				} else {
					do {
						y = Math.random()*windowSize.height;
					} while (y > windowSize.height/4 && y < 3*windowSize.height/4);
				}


				asteroids.addElement(new AsteroidEntity((int)(Math.random()*5+1),(int)x,(int)y,
						Math.random()*10-5,Math.random()*10-5));
			}
		}
	}

	private void shoot() {
		if (System.currentTimeMillis()-lastShotTime > timeBetweenShots && ship.status) {
			lastShotTime = System.currentTimeMillis();
			shots.addElement(ship.fireShot());
			SoundManager.playShot();
		}
	}

	private void teleport() {
		if (keyX) {
			if (System.currentTimeMillis()-lastTeleportTime > timeBetweenTeleports) {
				lastTeleportTime = System.currentTimeMillis();
				ship.teleport(windowSize.width, windowSize.height);
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

	private void calculateMovement(long delta) {
		int i;
		ship.move(delta,windowSize.width,windowSize.height);
		for (i=0; i<asteroids.size(); i++)
			asteroids.get(i).move(delta,windowSize.width,windowSize.height);
		for (i=0; i<shots.size(); i++)
			shots.get(i).move(delta,windowSize.width, windowSize.height);
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
		SoundManager.playLargeExplosion();
		waitingForKeyPress = true;
		if (score > highScore) {
			gameSM.addNewHighScore(score);
		}
	}

	private void shotCollidesWithAsteroid(ShotEntity s, AsteroidEntity a) {
		a.status = false;
		s.status = false;
		score+=10;
		SoundManager.playMediumExplosion();
		//mirem si creem 2 asteroides més petits
		if(a.explodes()) {
			double randomModifier = Math.random()*3;
			asteroids.addElement(
					new AsteroidEntity(a.getHp()/2,a.getMiniX(),a.getMiniY(),
							a.getVy()*randomModifier,-a.getVx()*randomModifier));
			asteroids.addElement(
					new AsteroidEntity(a.getHp()/2,a.getMiniX(),a.getMiniY(),
							-a.getVy()*randomModifier,a.getVx()*randomModifier));
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


	private void gameLoop() {
		//iniciar el temporitzador
		long lastLoopTime = System.currentTimeMillis();
		//això en realitat correrà sempre perquè jocEnMarxa sempre és true
		while (gameEvent == 1) {
			//interval de temps transcorregut entre 2 loops
			long delta = System.currentTimeMillis() - lastLoopTime;
			//refrescar el temporitzador
			lastLoopTime = System.currentTimeMillis();
			//gràfics on dibuixem per fer doublebuffering amb bs.show()
			Graphics2D g = (Graphics2D) bs.getDrawGraphics();

			//pintem el fons (negre)
			gameGM.paintBackground(g);

			//si no estem en pausa, calculem moviments i mirem si s'ha de teleportar la nau
			if (!waitingForKeyPress) {
				calculateMovement(delta);
				teleport();
				detectCollisions();
				eraseDeadEntities();
				gameGM.paintObjects(g);
				gameMM.currentScore(score, g);
				addAsteroids((int)(Math.random()*10+5));
			} else {
				if (ship.status==false) {
					gameGM.paintObjects(g);
					gameGM.paintExplosion(ship.getX(), ship.getY(), Math.min(deadFramesCount, 14), g);
					deadFramesCount++;
					gameMM.waitMessage(score, g);
				} else {
					gameMM.startScreen(g);
				}

			}

			g.dispose();
			bs.show();

			//accions del jugador
			moveShip();
			if(keySpace)
				shoot();

			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		//coses a fer quan gamerunning sigui false....
		if (gameEvent == 2) {
			while (true) {
				Graphics2D g = (Graphics2D) bs.getDrawGraphics();
				gameGM.paintHighScores(g);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

		}
	}


}
