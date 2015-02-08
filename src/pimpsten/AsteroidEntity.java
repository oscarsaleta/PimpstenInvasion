package pimpsten;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;


/**
 * Aquesta classe és un asteroide. La seva mida varia i,
 * quant més gran sigui, més trets calen per destruir-lo.
 * A més, si és prou gran, en trencar-lo es convertirà
 * en dos asteroides més petits.
 * La velocitat és variable, i van rotant a mida que es
 * mouen.
 * @author Oscar Saleta
 */
public class AsteroidEntity extends AbstractEntity {

	private boolean hasMiniAsteroids;
	private double rotationDirection;
	
	AsteroidEntity(int hp, double x, double y, double vx, double vy) {
		super(hp,x,y);
		width = hp*40;
		height = hp*40;
		this.vx = vx;
		this.vy = vy;
		rotationDirection = (Math.random()*2-1)/2.;
		if (hp > 1)
			hasMiniAsteroids = true;
		try {
			URL url = this.getClass().getResource("resources/graphics/asteroid.png");
			img = ImageIO.read(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Pintar l'asteroide amb la rotació pertinent
	 * @param g2d gràfics
	 */
	public void paint(Graphics2D g2d) {
		super.paint();
		Graphics2D aux = ((Graphics2D) ((Graphics) g2d).create());
		aux.rotate(Math.toRadians(-angle),centerPositionX,centerPositionY);
		aux.drawImage(img, (int)x, (int)y, width, height, null);
		aux.dispose();
	}
	
	/**
	 * Moure l'asteroide per inèrcia, la pantalla actua
	 * com un torus.
	 * @param delta interval de moviment
	 * @param xMax amplada de la pantalla
	 * @param yMax alçada de la pantalla
	 */
	public void move(long delta, int xMax, int yMax) {
		super.move(delta);
		angle+=rotationDirection;
		if (centerPositionX < 0 || centerPositionX > xMax) {
			if ((x%=xMax) < 0)
				x+=xMax+width/2;
		}
		if (centerPositionY > yMax || centerPositionY < 0) {
			if ((y%=yMax) < 0)
				y+=yMax;
		}
	}

	/**
	 * Ens informa de si s'han de crear dos mini-asteroides
	 * quan es trenqui aquest.
	 * @return true si es trencarà en asteroides més petits,
	 * false altrament
	 */
	public boolean explodes() {
		status = false;
		if (hasMiniAsteroids) {
			return true;
		}
		return false;
	}

	/**
	 * Getter per la vida de l'asteroide
	 * @return punts de vida (1 vol dir que d'un tret desapareix,
	 * si és major que 1 llavors sortiran miniasteroides amb
	 * un punt menys de vida que el seu)
	 */
	public int getHp() {
		return hp;
	}
	
	/**
	 * Getter per la component x de la velocitat
	 * @return component x de la velocitat
	 */
	public double getVx() {
		return vx;
	}
	
	/**
	 * Getter per la component y de la velocitat
	 * @return component y de la velocitat
	 */
	public double getVy() {
		return vy;
	}
	
	/**
	 * Calcula la posició x dels miniasteroides que han
	 * de sortir d'aquest
	 * @return coordenada x del miniasteroide
	 */
	public int getMiniX() {
		return (int)(x+width/2-10*hp);
	}

	/**
	 * Calcula la posició y dels miniasteroides que han
	 * de sortir d'aquest
	 * @return coordenada y del miniasteroide
	 */
	public int getMiniY() {
		return (int)(y+height/2-10*hp);
	}
}
