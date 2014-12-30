import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class EntitatTret extends EntitatAbstracta {
	
	BufferedImage img;

	EntitatTret (double x, double y, double vx, double vy, double angle) {
		super(1);
		width=height=10;
		this.x = x;
		this.y = y;
		this.vx = vx;
		this.vy = vy;
		this.angle = angle;
		try {
			img = ImageIO.read(new File("piu-0.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	void paint(Graphics2D g2d) {
		super.paint();
		Graphics2D aux = ((Graphics2D) ((Graphics) g2d).create());
		aux.rotate(Math.toRadians(-angle-90),posicioCentreX,posicioCentreY);
		aux.drawImage(img,(int)x,(int)y,width,height,null);
		aux.dispose();
	}

	void move(long delta, int xMax, int yMax) {
		super.move(delta);
		if (x < 0 || x > xMax || y < 0 || y > yMax)
			status = false;
	}
	
}
