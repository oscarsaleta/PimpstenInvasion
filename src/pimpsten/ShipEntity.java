package pimpsten;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

/**
 * La nau que porta el jugador. Pot girar, accelerar, disparar, teletransportar-se
 * 
 * @author Oscar Saleta
 *
 */
public class ShipEntity extends AbstractEntity {
	
	BufferedImage img;
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
	
	void move(long delta, int xMax, int yMax) {
		super.move(delta);
		vx*=0.99;
		vy*=0.99;
		if ((x%=xMax)<0)
			x+=xMax;
		if ((y%=yMax)<0)
			y+=yMax;
		
//		if (x < 0)
//			x+=xMax;
//		else if (x > xMax)
//			x-=xMax;
//		if (y < 0)
//			y += yMax;
//		else if (y > yMax)
//			y -= yMax;
	}
	
	void moveEvent(KeyEvent e, int xMax, int yMax) {
		int keyCode = e.getKeyCode();
		vertexX = Math.cos(Math.toRadians(angle+90));
		vertexY = Math.sin(Math.toRadians(angle+90));
	    switch( keyCode ) { 
	        case KeyEvent.VK_UP: //accelerate
	        	vx += 0.5*vertexX;
	        	vy -= 0.4*vertexY;
	            break;
	            
	        case KeyEvent.VK_DOWN: //brake
	        	vx -= 0.5*vertexX;
	        	vy += 0.4*vertexY;
	            break;
	            
	        case KeyEvent.VK_LEFT:
	        	angle+=8;
	        	
	            break;
	            
	        case KeyEvent.VK_RIGHT :
	        	angle-=8;
	        	
	            break;
	    }
	}

	void paint(Graphics2D g2d) {
		super.paint();
		Graphics2D aux = ((Graphics2D) ((Graphics) g2d).create());
		aux.rotate(Math.toRadians(-angle),centerPositionX,centerPositionY);
		aux.drawImage(img,(int)x,(int)y,width,height,null);
		aux.dispose();
	}

	public ShotEntity fireShot () {
		vertexX = Math.cos(Math.toRadians(angle+90));
		vertexY = Math.sin(Math.toRadians(angle+90));
		return new ShotEntity(centerPositionX,centerPositionY-2*vertexY,40*vertexX,-40*vertexY,angle);
	}
	
	public void teleport(int xMax, int yMax) {
			x = Math.random()*xMax;
			y = Math.random()*yMax;
	}
	
	
	public void increaseSpeedForward() {
		vertexX = Math.cos(Math.toRadians(angle+90));
		vertexY = Math.sin(Math.toRadians(angle+90));
		vx+=0.5*vertexX;
		vy-=0.5*vertexY;
	}
	
	public void increaseSpeedBackwards() {
		vertexX = Math.cos(Math.toRadians(angle+90));
		vertexY = Math.sin(Math.toRadians(angle+90));
		vx -= 0.5*vertexX;
    	vy += 0.5*vertexY;
	}
	
	public void turnRight() {
		angle-=1;
	}
	
	public void turnLeft() {
		angle+=1;
	}
}
