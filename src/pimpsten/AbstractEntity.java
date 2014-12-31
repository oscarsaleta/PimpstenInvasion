package pimpsten;

import java.awt.geom.Ellipse2D;


public abstract class AbstractEntity {

	protected double x,y;
	protected double centerPositionX, centerPositionY;
	protected double vx,vy;
	protected double angle;
	protected int hp;
	protected int width,height;
	public boolean status;

	protected AbstractEntity(int hp) {
		this.hp = hp;
		status = true;
	}

	protected void move(long delta) {
		x+=(delta*vx)/100;
		y+=(delta*vy)/100;
	}
	
	protected void paint() {
		centerPositionX = x+width/2;
		centerPositionY = y+height/2;
	}
	
	public Ellipse2D.Double getBounds() {
		return new Ellipse2D.Double(x,y,width,height);
	}
	
	public int getX() {
		return (int)x; 
	}
	
	public int getY() {
		return (int)y;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
}
