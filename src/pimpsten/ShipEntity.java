package pimpsten;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 * La nau que porta el jugador. Pot girar, accelerar, disparar, teletransportar-se
 * @author Oscar Saleta
 */
public class ShipEntity extends AbstractEntity {
	
	private double vertexX;
	private double vertexY;
	
	ShipEntity(int x,int y) {
		super(5,x,y);
		width=height=50;
		centerPositionX = x+width/2;
		centerPositionY = y+height/2;
		try {
			URL url = this.getClass().getResource("resources/graphics/nau2.png");
			img = ImageIO.read(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Moviment per inèrcia de la nau
	 * @param delta interval de temps
	 * @param xMax amplada de la pantalla
	 * @param yMax alçada de la pantalla
	 */
	public void move(long delta, int xMax, int yMax) {
		super.move(delta);
		vx*=0.99;
		vy*=0.99;
		if ((x%=xMax)<0)
			x+=xMax;
		if ((y%=yMax)<0)
			y+=yMax;
	}
	

	/**
	 * Rutina per pintar la nau amb la rotació pertinent
	 * @param g2d entorn gràfic
	 */
	public void paint(Graphics2D g2d) {
		super.paint();
		Graphics2D aux = ((Graphics2D) ((Graphics) g2d).create());
		aux.rotate(Math.toRadians(-angle),centerPositionX,centerPositionY);
		aux.drawImage(img,(int)x,(int)y,width,height,null);
		aux.dispose();
	}

	/**
	 * Per fer que la nau dispari. El temps entre trets es controla
	 * al joc principal.
	 * @return El tret
	 */
	public ShotEntity fireShot () {
		vertexX = Math.cos(Math.toRadians(angle+90));
		vertexY = Math.sin(Math.toRadians(angle+90));
		return new ShotEntity(centerPositionX,centerPositionY-2*vertexY,40*vertexX,-40*vertexY,angle);
	}
	
	/**
	 * Teleportar la nau (tecla X)
	 * @param xMax amplada de la pantalla
	 * @param yMax alçada de la pantalla
	 */
	public void teleport(int xMax, int yMax) {
			x = Math.random()*xMax;
			y = Math.random()*yMax;
	}
	
	
	/**
	 * Accelera la nau
	 */
	public void increaseSpeedForward() {
		vertexX = Math.cos(Math.toRadians(angle+90));
		vertexY = Math.sin(Math.toRadians(angle+90));
		vx+=0.5*vertexX;
		vy-=0.5*vertexY;
	}
	
	/**
	 * Decelera la nau
	 */
	public void increaseSpeedBackwards() {
		vertexX = Math.cos(Math.toRadians(angle+90));
		vertexY = Math.sin(Math.toRadians(angle+90));
		vx -= 0.5*vertexX;
    	vy += 0.5*vertexY;
	}
	
	/**
	 * Gira a la dreta
	 */
	public void turnRight() {
		angle-=1;
	}
	
	/**
	 * Gira a l'esquerra
	 */
	public void turnLeft() {
		angle+=1;
	}
}
