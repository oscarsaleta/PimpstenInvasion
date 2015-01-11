package pimpsten;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Classe que gestiona input per teclat del joc
 * 
 * @author Oscar Saleta
 *
 */
public class KeyboardEventManagerOld extends KeyAdapter {
	private int pressCount=1;

	GameOld game;
	
	KeyboardEventManagerOld(GameOld joc) {
		this.game = joc;
	}
	
	public void keyPressed(KeyEvent e) {
		if (game.waitingForKeyPress)
			return;

		if (e.getKeyCode() == KeyEvent.VK_UP)
			game.keyUp = true;
		if (e.getKeyCode() == KeyEvent.VK_DOWN)
			game.keyDown = true;
		if (e.getKeyCode() == KeyEvent.VK_LEFT)
			game.keyLeft = true;
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			game.keyRight = true;
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
			game.keySpace = true;
		if (e.getKeyCode() == KeyEvent.VK_X)
			game.keyX = true;
	}

	public void keyReleased(KeyEvent e) {
		if (game.waitingForKeyPress)
			return;

		if (e.getKeyCode() == KeyEvent.VK_UP)
			game.keyUp = false;
		if (e.getKeyCode() == KeyEvent.VK_DOWN)
			game.keyDown = false;
		if (e.getKeyCode() == KeyEvent.VK_LEFT)
			game.keyLeft = false;
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			game.keyRight = false;
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
			game.keySpace = false;
		if (e.getKeyCode() == KeyEvent.VK_X)
			game.keyX = false;
	}

	public void keyTyped(KeyEvent e) {
		if (game.waitingForKeyPress) {
			if (pressCount == 1) {
				game.waitingForKeyPress = false;
				game.beginGame();
				pressCount=0;
			} else {
				pressCount++;
			}
		}
		if (e.getKeyChar() == 27)
			System.exit(0);
	}
}
