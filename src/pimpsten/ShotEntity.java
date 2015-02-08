package pimpsten;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;


/**
 * Aquesta entitat és un tret. Pot ser disparat per la
 * nau o per l'ovni. Té una mida petita, velocitat
 * elevada i no usa la pantalla com un torus, sinó que
 * quan surt de la pantalla és eliminat.
 * @author Oscar Saleta
 */
public class ShotEntity extends AbstractEntity {
	
	ShotEntity (double x, double y, double vx, double vy, double angle) {
		super(1,x,y);
		width=height=10;
		this.vx = vx;
		this.vy = vy;
		this.angle = angle;
		try {
			URL url = this.getClass().getResource("resources/graphics/piu-0.png");
			img = ImageIO.read(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Dibuixar el tret, rotat en la direcció adient
	 * @param g2d gràfics
	 */
	void paint(Graphics2D g2d) {
		super.paint();
		Graphics2D aux = ((Graphics2D) ((Graphics) g2d).create());
		aux.rotate(Math.toRadians(-angle-90),centerPositionX,centerPositionY);
		aux.drawImage(img,(int)x,(int)y,width,height,null);
		aux.dispose();
	}

	/**
	 * Moviment per inèrcia, si surt de la pantalla
	 * s'elimina.
	 * @param delta interval de temps
	 * @param xMax amplada de la pantalla
	 * @param yMax alçada de la pantalla
	 */
	void move(long delta, int xMax, int yMax) {
		super.move(delta);
		if (x < 0 || x > xMax || y < 0 || y > yMax)
			status = false;
	}
	
}
