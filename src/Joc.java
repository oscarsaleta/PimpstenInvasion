import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Vector;

import javax.imageio.ImageIO;
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
public class Joc extends Canvas {

	//variables de finestra i doublebuffering
	private Dimension midaFinestra = new Dimension(800,600);
	private BufferStrategy bs;

	//variables que controlen el flux del joc
	private boolean jocEnMarxa = true;
	public boolean esperantTecla = true;
	private int nombreVictories = 0;
	private int puntuacio = 0;

	//variables per missatges en pantalla
	private boolean missatgeTeleport = false;
	private int comptadorFramesMort;

	//variables temporals pels trets i teleports
	private long instantUltimTret = 0;
	private long intervalEntreTrets = 150;
	private long instantUltimTeleport = 0;
	private long instantUltimIntentTeleport = 0;
	private long intervalEntreTeleports = 3000;

	//variables per manejar l'input per teclat (podrem premer 2 tecles alhora)
	public boolean teclaEsquerra = false;
	public boolean teclaDreta = false;
	public boolean teclaAdalt = false;
	public boolean teclaAbaix = false;
	public boolean teclaX = false;
	public boolean teclaEspai = false;

	//objectes del nostre joc (nau, asteroides i trets)
	EntitatNau nau;
	Vector<EntitatAsteroide> asteroides = new Vector<EntitatAsteroide>(0);
	Vector<EntitatTret> trets = new Vector<EntitatTret>(0);

	public static void main(String[] args) {
		Joc joc = new Joc();
		joc.bucleJoc();
	}

	Joc() {
		//tindrem la finestra dins la mateixa classe del joc
		JFrame finestra = new JFrame("Pimpsten Invasion (Swing version early alpha)");
		//les imatges es pinten al panel
		JPanel panel = (JPanel) finestra.getContentPane();
		panel.setPreferredSize(midaFinestra);
		panel.setBackground(Color.BLACK);
		panel.setLayout(null);

		setBounds(0,0,midaFinestra.width,midaFinestra.height);
		panel.add(this);

		//swing pot fer doublebuffering aixi que no volem que awt repinti el joc
		setIgnoreRepaint(true);

		//fem que la finestra tingui la mida que volem i la mostrem
		finestra.pack();
		finestra.setResizable(false);
		finestra.setVisible(true);

		//si tanquem la finestra java no matarà el joc perquè hi haurà altres objectes en
		//memòria, així que el forcem a fer-ho
		finestra.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		//aquest key listener es una classe interna que maneja l'input per teclat
		addKeyListener(new GestorEventsTeclat(this));
		requestFocus();

		//doublebuffering pel nostre canvas!
		createBufferStrategy(2);
		bs = getBufferStrategy();

		iniciarObjectes();
	}

	public void iniciarPartida() {
		//netejar objectes de la partida anterior
		asteroides.clear();
		trets.clear();
		comptadorFramesMort=0;
		puntuacio=0;
		nombreVictories=0;
		//netejar tecles de la partida anterior
		teclaAdalt=false;
		teclaAbaix=false;
		teclaEsquerra=false;
		teclaDreta=false;
		teclaX=false;
		teclaEspai=false;
		//iniciar objectes per la nova partida
		iniciarObjectes();
	}

	private void iniciarObjectes() {
		//creem la nau
		nau = new EntitatNau(midaFinestra.width/2, midaFinestra.height/2);
		//creem els asteroides (per començar només 5)
		afegeixAsteroides(5);

	}

	private void afegeixAsteroides(int quantitat) {
		int i;
		double x,y;
		quantitat+=nombreVictories;
		if (asteroides.size() == 0) {
			for (i=0; i<quantitat; i++) {
				//calculem la posici� x del nou asteroide
				//no volem que estigui a prop de la nau
				if (nau.getX() < midaFinestra.width/4) {
					x = (Math.random()+1)*midaFinestra.width/2;
				} else if (nau.getX() > 3*midaFinestra.width/4) {
					x = Math.random()*midaFinestra.width/2;
				} else {
					do {
						x = Math.random()*midaFinestra.width;
					} while (x > midaFinestra.width/4 && x < 3*midaFinestra.width/4);
				}
				//calculem la posici� y del nou asteroide
				if (nau.getY() < midaFinestra.height/4) {
					y = (Math.random()+1)*midaFinestra.height/2;
				} else if (nau.getY() > 3*midaFinestra.height/4) {
					y = Math.random()*midaFinestra.height/2;
				} else {
					do {
						y = Math.random()*midaFinestra.height;
					} while (y > midaFinestra.height/4 && y < 3*midaFinestra.height/4);
				}

				
				asteroides.addElement(new EntitatAsteroide((int)(Math.random()*5+1),(int)x,(int)y,
						Math.random()*10-5,Math.random()*10-5));
			}
		}
	}

	private void dispara() {
		if (System.currentTimeMillis()-instantUltimTret > intervalEntreTrets) {
			instantUltimTret = System.currentTimeMillis();
			trets.addElement(nau.fireShot());
		}
	}

	private void teleporta() {
		if (teclaX) {
			if (System.currentTimeMillis()-instantUltimTeleport > intervalEntreTeleports) {
				instantUltimTeleport = System.currentTimeMillis();
				nau.teleport(midaFinestra.width, midaFinestra.height);
				missatgeTeleport = false;
			} else {
				instantUltimIntentTeleport = System.currentTimeMillis();
				if (instantUltimIntentTeleport-instantUltimTeleport > 100)
					missatgeTeleport = true;
			}
		} else {
			if (System.currentTimeMillis()-instantUltimIntentTeleport > 1000)
				missatgeTeleport = false;
		}
	}

	private void calculaMoviment(long delta) {
		nau.move(delta,midaFinestra.width,midaFinestra.height);
		for (EntitatAsteroide a : asteroides)
			a.move(delta,midaFinestra.width,midaFinestra.height);
		for (EntitatTret s : trets)
			s.move(delta,midaFinestra.width, midaFinestra.height);
	}

	private void detectaColisions() {
		int i,j;
		//iterarem per a  cada asteroide
		for (i=0; i<asteroides.size(); i++) {
			EntitatAsteroide a = asteroides.get(i);
			//mirem xocs asteroide-nau
			if (a.getBounds().intersects(
					nau.getX(),nau.getY(),nau.getWidth(),nau.getHeight())) {
				nau.status = false;
				esperantTecla = true;
			}
			//mirem xocs asteroide-tret
			for (j=0; j<trets.size(); j++) {
				EntitatTret s = trets.get(j);
				if (a.getBounds().intersects(
						s.getX(), s.getY(),s.getWidth(),s.getHeight())
						&& s.status == true) { //evita que un tret trenqui 2 asteroides
					a.status = false;
					s.status = false;
					puntuacio+=10;
					//mirem si creem 2 asteroides més petits
					if(a.explodes()) {
						double randomModifier = Math.random()*3;
						asteroides.addElement(
								new EntitatAsteroide(a.getHp()/2,a.getMiniX(),a.getMiniY(),
										a.getVy()*randomModifier,-a.getVx()*randomModifier));
						asteroides.addElement(
								new EntitatAsteroide(a.getHp()/2,a.getMiniX(),a.getMiniY(),
										-a.getVy()*randomModifier,a.getVx()*randomModifier));
					}
				}
			}
		}
	}

	private void pintaObjectes(Graphics2D g) {
		int i;
		nau.paint(g);
		for (i=0; i<asteroides.size(); i++)
			asteroides.get(i).paint(g);
		for (i=0; i<trets.size(); i++)
			trets.get(i).paint(g);
		if (missatgeTeleport)
			PintorMissatges.noTeleport(midaFinestra.width,midaFinestra.height,g);
	}

	private void esborraBasura() {
		int i;
		for (i=0; i<asteroides.size(); i++) {
			EntitatAsteroide a = asteroides.get(i);
			if (a.status == false)
				asteroides.removeElement(a);
		}
		for (i=0; i<trets.size(); i++) {
			EntitatTret s = trets.get(i);
			if (s.status == false)
				trets.removeElement(s);
		}
	}

	private void mouNau() {
		if (teclaAdalt && !teclaAbaix)
			nau.augmentaVelocitatEndavant();
		else if (!teclaAdalt && teclaAbaix)
			nau.augmentaVelocitatEnrera();
		if (teclaDreta && !teclaEsquerra)
			nau.giraDreta();
		else if (!teclaDreta && teclaEsquerra)
			nau.giraEsquerra();
	}


	private void bucleJoc() {
		//iniciar el temporitzador
		long tempsUltimLoop = System.currentTimeMillis();
		//això en realitat correrà sempre perquè jocEnMarxa sempre és true
		while (jocEnMarxa) {
			//interval de temps transcorregut entre 2 loops
			long delta = System.currentTimeMillis() - tempsUltimLoop;
			//refrescar el temporitzador
			tempsUltimLoop = System.currentTimeMillis();
			//gràfics on dibuixem per fer doublebuffering amb bs.show()
			Graphics2D g = (Graphics2D) bs.getDrawGraphics();

			//pintem el fons (negre)
			pintaFons(g);

			//si no estem en pausa, calculem moviments i mirem si s'ha de teleportar la nau
			if (!esperantTecla) {
				calculaMoviment(delta);
				teleporta();
				detectaColisions();
				esborraBasura();
				pintaObjectes(g);
				PintorMissatges.puntuacioActual(puntuacio, midaFinestra.width,
						midaFinestra.height, g);
				afegeixAsteroides((int)(Math.random()*10+5));
			} else {
				if (nau.status==false) {
					pintaObjectes(g);
					pintaExplosio(nau.getX(), nau.getY(), Math.min(comptadorFramesMort, 14), g);
					comptadorFramesMort++;
				}
				PintorMissatges.espera(nau.status,puntuacio,midaFinestra.width,midaFinestra.height,g);
			}

			g.dispose();
			bs.show();

			//accions del jugador
			mouNau();
			if(teclaEspai)
				dispara();

			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void pintaFons(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, midaFinestra.width, midaFinestra.height);
	}

	private void pintaExplosio(int x, int y, int scale, Graphics2D g) {
		try {
			BufferedImage img = ImageIO.read(new File("explosion.png"));
			g.drawImage(img, x-scale*5, y-scale*5, scale*10, scale*10, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
