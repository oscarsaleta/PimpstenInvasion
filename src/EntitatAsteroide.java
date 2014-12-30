import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class EntitatAsteroide extends EntitatAbstracta {

	BufferedImage img;
	boolean hasMiniAsteroids;
	private double direccioRotacio;
	
	EntitatAsteroide(int hp, int x, int y, double vx, double vy) {
		super(hp);
		width = hp*40;
		height = hp*40;
		this.x = x;
		this.y = y;
		this.vx = vx;
		this.vy = vy;
		direccioRotacio = Math.random()*2-1;
		if (hp > 1)
			hasMiniAsteroids = true;
		try {
			img = ImageIO.read(new File("asteroid.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void paint(Graphics2D g2d) {
		super.paint();
		Graphics2D aux = ((Graphics2D) ((Graphics) g2d).create());
		aux.rotate(Math.toRadians(-angle),posicioCentreX,posicioCentreY);
		aux.drawImage(img, (int)x, (int)y, width, height, null);
		aux.dispose();
	}
	
	void move(long delta, int xMax, int yMax) {
		super.move(delta);
		angle+=direccioRotacio*3;
		if (x > xMax)
			x-=xMax;
		else if (x < 0)
			x+=xMax;
		if (y > yMax)
			y-=yMax;
		else if (y < 0)
			y+=yMax;
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
