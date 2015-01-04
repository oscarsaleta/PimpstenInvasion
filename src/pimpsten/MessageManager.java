package pimpsten;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

/**
 * Classe que gestiona els missatges que imprimim en pantalla
 * 
 * @author Oscar Saleta
 *
 */
public class MessageManager {

	private int maxX;
	private int maxY;
	private Game game;

//	private final Font titleFont = new Font("Press Start 2P", Font.BOLD, 43);
//	private final Font scoreFont = new Font("Press Start 2P", Font.PLAIN, 20);
//	private final Font messageFont = new Font("Press Start 2P", Font.PLAIN, 11);

	MessageManager (int maxX, int maxY, Game game) {
		this.maxX = maxX;
		this.maxY = maxY;
		this.game = game;
	}

	private void blinkingMessage(String m, int positionY, Graphics2D aux) {
		final Font messageFont = new Font(aux.getFont().getFontName(),Font.PLAIN,11);
		if (game.isMessageVisible) {
			//si el missatge anterior era visible
			if (System.currentTimeMillis() - game.lastVisibilitySwapTime < game.visibleTime) {
				//si encara hem d'imprimir un missatge visible
				aux.setColor(Color.WHITE);
				aux.setFont(messageFont);
				aux.drawString(m, (maxX-aux.getFontMetrics().stringWidth(m))/2, positionY);
			} else {
				//hem de deixar de mostrar el missatge
				game.isMessageVisible = false;
				game.lastVisibilitySwapTime = System.currentTimeMillis();
			}
		} else {
			//el missatge anterior no era visible
			if (System.currentTimeMillis() - game.lastVisibilitySwapTime > game.invisibileTime) {
				//hem de fer el missatge visible
				aux.setColor(Color.WHITE);
				aux.setFont(messageFont);
				aux.drawString(m, (maxX-aux.getFontMetrics().stringWidth(m))/2, positionY);
				game.isMessageVisible = true;
				game.lastVisibilitySwapTime = System.currentTimeMillis();
			}
		}
	}

	public void startScreen(Graphics2D g) {
		Graphics2D aux = ((Graphics2D) ((Graphics) g).create());
		
		final Font titleFont = new Font(aux.getFont().getFontName(),Font.BOLD,43);
		final Font messageFont = new Font(aux.getFont().getFontName(),Font.PLAIN,11);
		
		game.gameGM.paintOnlyAsteroids(aux);
		//pintem el missatge que parpalleja
		String m = "Press a key to start";
		blinkingMessage(m, maxY/2+100, aux);
		
		//pintem els altres missatges
		aux.setColor(Color.WHITE);

		String title1 = "PIMPSTEN";
		String title2 = "INVASION";
		aux.setFont(titleFont);
		aux.drawString(title1, (maxX-aux.getFontMetrics().stringWidth(title1))/2, maxY/2-150);
		aux.drawString(title2, (maxX-aux.getFontMetrics().stringWidth(title2))/2, maxY/2-75);
		
		String subtitle = "v1.00-stable";
		aux.setFont(messageFont);
		aux.drawString(subtitle, (maxX-aux.getFontMetrics().stringWidth(subtitle))/2, maxY/2);

		aux.dispose();
	}

	public void noTeleport(Graphics2D g) {
		Graphics2D aux = ((Graphics2D) ((Graphics) g).create());
		
		final Font messageFont = new Font(aux.getFont().getFontName(),Font.PLAIN,11);
		
		String m = "Teleport on cooldown";
		aux.setFont(messageFont);
		aux.setColor(Color.WHITE);
		aux.drawString(m,(maxX-aux.getFontMetrics().stringWidth(m))/2,maxY-100);
	}

	public void waitMessage(int score, Graphics2D g) {
		Graphics2D aux = ((Graphics2D) ((Graphics) g).create());
		
		final Font titleFont = new Font(aux.getFont().getFontName(),Font.BOLD,43);
		final Font messageFont = new Font(aux.getFont().getFontName(),Font.PLAIN,11);
		
		aux.setColor(Color.WHITE);
		
		String m1 = "GAME OVER";
		aux.setFont(titleFont);
		aux.drawString(m1,(maxX-aux.getFontMetrics().stringWidth(m1))/2,200);
		
		String m2 = "Final score: "+score;
		aux.setFont(messageFont);
		aux.drawString(m2,(maxX-aux.getFontMetrics().stringWidth(m2))/2, 300);
		
		String m3 = "Press a key to restart";
		blinkingMessage(m3, 350, aux);
		aux.dispose();
	}

	public void currentScore(int score, Graphics2D g) {
		Graphics2D aux = ((Graphics2D) ((Graphics) g).create());
		
		final Font scoreFont = new Font(aux.getFont().getFontName(), Font.PLAIN, 20);
		
		String s = "Score: "+score;
		aux.setColor(Color.WHITE);
		aux.setFont(scoreFont);
		aux.drawString(s, 30, 30);
	}
	
}
