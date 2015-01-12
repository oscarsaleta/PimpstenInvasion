package pimpsten;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;


public class AsteroidEntity extends AbstractEntity {

	BufferedImage img;
	boolean hasMiniAsteroids;
	private double rotationDirection;
	
	AsteroidEntity(int hp, double x, double y, double vx, double vy) {
		super(hp,x,y);
		width = hp*40;
		height = hp*40;
		this.vx = vx;
		this.vy = vy;
		rotationDirection = Math.random()*2-1;
		if (hp > 1)
			hasMiniAsteroids = true;
		try {
			URL url = this.getClass().getResource("resources/graphics/asteroid.png");
			img = ImageIO.read(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void paint(Graphics2D g2d) {
		super.paint();
		Graphics2D aux = ((Graphics2D) ((Graphics) g2d).create());
		aux.rotate(Math.toRadians(-angle),centerPositionX,centerPositionY);
		aux.drawImage(img, (int)x, (int)y, width, height, null);
		aux.dispose();
	}
	
	void move(long delta, int xMax, int yMax) {
		super.move(delta);
		angle+=rotationDirection;
		if (centerPositionX < 0 || centerPositionX > xMax) {
			if ((x%=xMax) < 0)
				x+=xMax;
		}
			
		if (centerPositionY > yMax || centerPositionY < 0) {
			if ((y%=yMax) < 0)
				y+=yMax;
		}
//		if (centerPositionX > xMax)
//			x-=xMax;
//		else if (centerPositionX < 0)
//			x+=xMax;
//		if (centerPositionY > yMax)
//			y-=yMax;
//		else if (centerPositionY < 0)
//			y+=yMax;
	}

	public boolean explodes() {
		status = false;
		if (hasMiniAsteroids) {
			return true;
		}
		return false;
	}

	public int getHp() {
		return hp;
	}
	
	public double getVx() {
		return vx;
	}
	
	public double getVy() {
		return vy;
	}
	
	public int getMiniX() {
		return (int)(x+width/2-10*hp);
	}
	
	public int getMiniY() {
		return (int)(y+height/2-10*hp);
	}
}
