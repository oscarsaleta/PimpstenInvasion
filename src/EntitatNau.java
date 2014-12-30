import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class EntitatNau extends EntitatAbstracta {
	
	BufferedImage img;
	private double vertexX;
	private double vertexY;
	
	EntitatNau(int x,int y) {
		super(5);
		width=height=50;
		posicioCentreX = x+width/2;
		posicioCentreY = y+height/2;
		this.x = x;
		this.y = y;
		try {
			img = ImageIO.read(new File("nau2.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	void move(long delta, int xMax, int yMax) {
		super.move(delta);
		vx*=0.99;
		vy*=0.99;
		if (x < 0)
			x+=xMax;
		else if (x > xMax)
			x-=xMax;
		if (y < 0)
			y += yMax;
		else if (y > yMax)
			y -= yMax;
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
		aux.rotate(Math.toRadians(-angle),posicioCentreX,posicioCentreY);
		aux.drawImage(img,(int)x,(int)y,width,height,null);
		aux.dispose();
	}

	public EntitatTret fireShot () {
		vertexX = Math.cos(Math.toRadians(angle+90));
		vertexY = Math.sin(Math.toRadians(angle+90));
		return new EntitatTret(posicioCentreX,posicioCentreY-2*vertexY,40*vertexX,-40*vertexY,angle);
	}
	
	public void teleport(int xMax, int yMax) {
			x = Math.random()*xMax;
			y = Math.random()*yMax;
	}
	
	
	public void augmentaVelocitatEndavant() {
		vertexX = Math.cos(Math.toRadians(angle+90));
		vertexY = Math.sin(Math.toRadians(angle+90));
		vx+=0.5*vertexX;
		vy-=0.5*vertexY;
	}
	
	public void augmentaVelocitatEnrera() {
		vertexX = Math.cos(Math.toRadians(angle+90));
		vertexY = Math.sin(Math.toRadians(angle+90));
		vx -= 0.5*vertexX;
    	vy += 0.5*vertexY;
	}
	
	public void giraDreta() {
		angle-=8;
	}
	
	public void giraEsquerra() {
		angle+=8;
	}
}
