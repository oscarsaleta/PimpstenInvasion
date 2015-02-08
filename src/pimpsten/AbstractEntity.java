package pimpsten;

import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;


/**
 * Entitat abstracta. D'aquesta classe abstracta deriven
 * les entitats del joc (asteroides, nau, trets, ovni)
 * @author Oscar Saleta
 */
public abstract class AbstractEntity {

	BufferedImage img;
	
	protected double x,y;
	protected double centerPositionX, centerPositionY;
	protected double vx,vy;
	protected double angle;
	protected int hp;
	protected int width,height;
	public boolean status;

	protected AbstractEntity(int hp, double x, double y) {
		this.hp = hp;
		status = true;
		this.x = x;
		this.y = y;
	}

	/**
	 * Moviment per inèrcia
	 * @param delta interval de temps
	 */
	protected void move(long delta) {
		x+=(delta*vx)/100.;
		y+=(delta*vy)/100.;
	}
	
	/**
	 * Aquesta funció no pinta, però troba el centre de l'entitat
	 * per pintar rotacions més fàcilment
	 */
	protected void paint() {
		centerPositionX = x+width/2;
		centerPositionY = y+height/2;
	}
	
	/**
	 * Trobar els límits de l'entitat (per tractar col·lisions)
	 * @return Una el·lipse (va millor que un rectangle)
	 */
	public Ellipse2D.Double getBounds() {
		return new Ellipse2D.Double(x,y,width,height);
	}
	
	/**
	 * Getter de la coordenada x
	 * @return coordenada x
	 */
	public int getX() {
		return (int)x; 
	}
	
	/**
	 * Getter de la coordenada y
	 * @return coordenada y
	 */
	public int getY() {
		return (int)y;
	}
	
	/**
	 * Getter de l'amplada
	 * @return amplada
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Getter de l'alçada
	 * @return alçada
	 */
	public int getHeight() {
		return height;
	}
	
	/**
	 * Getter de la coordenada x del centre
	 * @return coordenada x del centre
	 */
	public double getCenterPositionX() {
		return centerPositionX;
	}
	
	/**
	 * Getter de la coordenada y del centre
	 * @return coordenada y del centre
	 */
	public double getCenterPositionY() {
		return centerPositionY;
	}
	
}
