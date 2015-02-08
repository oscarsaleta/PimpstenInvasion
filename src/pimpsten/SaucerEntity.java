package pimpsten;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 * Aquesta classe implementa un ovni, que entrarà en joc
 * cada cop que es destrueixin tots els asteroides en pantalla.
 * Aquest ovni es mou de forma aleatòria, canviant de moviment
 * cada 3 segons, i dispara en direccions aleatòries. No cal
 * destruir-lo per superar el nivell, però si es fa hi haurà 2
 * ovnis al nivell següent.
 * 
 * @author Oscar Saleta
 */
public class SaucerEntity extends AbstractEntity {

	private long lastChangeMove = 0;
	private long timeChangeMove = 3000;
	private long shotInterval = 500;
	private long lastShotTime = 500;
	
	private double vertexX;
	private double vertexY;

	SaucerEntity(double x, double y, double vx, double vy) {
		super(1,x,y);
		width = height = 50;
		this.vx = vx;
		this.vy = vy;
		centerPositionX = x+width/2;
		centerPositionY = y+height/2;
		try {
			URL url = this.getClass().getResource("resources/graphics/saucer2.png");
			img = ImageIO.read(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Moure el saucer (usa el mateix mecanisme que un asteroide, però
	 * crida a changeMoveDirection() per anar canviant la direcció
	 * del moviment)
	 * @param delta interval temporal de moviment
	 * @param xMax amplada de la pantalla
	 * @param yMax alçada de la pantalla
	 */
	public void move(long delta, int xMax, int yMax) {
		super.move(delta);
		if (centerPositionX < 0 || centerPositionX > xMax) {
			if ((x%=xMax) < 0)
				x+=xMax+width/2;
		}
		if (centerPositionY > yMax || centerPositionY < 0) {
			if ((y%=yMax) < 0)
				y+=yMax;
		}
		changeMoveDirection();
	}

	private void changeMoveDirection() {
		if (System.currentTimeMillis() - lastChangeMove > timeChangeMove) {
			vx = Math.random()*10-5;
			vy = Math.random()*10-5;
			lastChangeMove = System.currentTimeMillis();
		}
	}

	/**
	 * Disparem un tret en direcció aleatòria.
	 * @return Un tret provinent del ovni
	 */
	public ShotEntity fireShot() {
		if (System.currentTimeMillis() - lastShotTime > shotInterval) {
			angle = Math.random()*360;
			vertexX = Math.cos(Math.toRadians(angle));
			vertexY = Math.sin(Math.toRadians(angle));
			lastShotTime = System.currentTimeMillis();
			return new ShotEntity(centerPositionX,centerPositionY-2*vertexY,40*vertexX,-40*vertexY,angle);
		}
		return null;
	}

	/**
	 * Pintar l'ovni (aquest no rota)
	 * @param g2d gràfics
	 */
	public void paint(Graphics2D g2d) {
		super.paint();
		Graphics2D aux = ((Graphics2D) ((Graphics) g2d).create());
		aux.drawImage(img,(int)x,(int)y,width,height,null);
		aux.dispose();
	}




}
