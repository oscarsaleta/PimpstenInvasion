package pimpsten;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;


public class GraphicsManager {

	private int maxX, maxY;
	private GameOld game;

	GraphicsManager(int maxX, int maxY, GameOld game) {
		this.maxX = maxX;
		this.maxY = maxY;
		this.game = game;
	}

	public void paintBackground(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, maxX, maxY);
	}
	
	public void paintOnlyAsteroids(Graphics2D g) {
		for (int i=0; i<game.asteroids.size(); i++)
			game.asteroids.get(i).paint(g);
	}
	
	public void paintObjects(Graphics2D g) {
		int i;
		if (game.ship.status == true)
			game.ship.paint(g);
		for (i=0; i<game.asteroids.size(); i++)
			game.asteroids.get(i).paint(g);
		for (i=0; i<game.shots.size(); i++)
			game.shots.get(i).paint(g);
		if (game.teleportMessage)
			game.gameMM.noTeleport(g);
	}
	
	public void paintExplosion(int x, int y, int scale, Graphics2D g) {
		try {
			URL url = this.getClass().getResource("resources/graphics/explosion.png");
			BufferedImage img = ImageIO.read(url);
			g.drawImage(img, x-scale*5, y-scale*5, scale*10, scale*10, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
