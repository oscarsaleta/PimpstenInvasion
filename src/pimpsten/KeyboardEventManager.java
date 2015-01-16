package pimpsten;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Classe que gestiona input per teclat del joc
 * 
 * @author Oscar Saleta
 *
 */
public class KeyboardEventManager extends KeyAdapter {
	private int pressCount=1;

	PimpstenInvasion gp;
	
	KeyboardEventManager(PimpstenInvasion gp) {
		this.gp = gp;
	}
	
	public void keyPressed(KeyEvent e) {
//		if (gp.waitingForKeyPress)
//			return;

		if (e.getKeyCode() == KeyEvent.VK_UP)
			gp.keyUp = true;
		if (e.getKeyCode() == KeyEvent.VK_DOWN)
			gp.keyDown = true;
		if (e.getKeyCode() == KeyEvent.VK_LEFT)
			gp.keyLeft = true;
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			gp.keyRight = true;
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
			gp.keySpace = true;
		if (e.getKeyCode() == KeyEvent.VK_X)
			gp.keyX = true;
	}

	public void keyReleased(KeyEvent e) {
//		if (gp.waitingForKeyPress)
//			return;

		if (e.getKeyCode() == KeyEvent.VK_UP)
			gp.keyUp = false;
		if (e.getKeyCode() == KeyEvent.VK_DOWN)
			gp.keyDown = false;
		if (e.getKeyCode() == KeyEvent.VK_LEFT)
			gp.keyLeft = false;
		if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			gp.keyRight = false;
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
			gp.keySpace = false;
		if (e.getKeyCode() == KeyEvent.VK_X)
			gp.keyX = false;
	}

	public void keyTyped(KeyEvent e) {
//		if (gp.waitingForKeyPress) {
//			if (pressCount == 1) {
//				gp.waitingForKeyPress = false;
//				gp.beginGame();
//				pressCount=0;
//			} else {
//				pressCount++;
//			}
//		}
//		if (e.getKeyChar() == 27)
//			System.exit(0);
	}
}
