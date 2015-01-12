package pimpsten;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class Game extends JFrame implements WindowListener {

	private static int DEFAULT_FPS = 30;

	private GamePanel gp;
	private JTextField scoreBox;
	private JTextField levelBox;

	private Font arcadeFont;
	private Font boxFont;

	public Game(long period) {
		super("The new Pimpsten Invasion");
		// carreguem una font alternativa
		try {
			arcadeFont = Font.createFont(Font.TRUETYPE_FONT,this.getClass().getResourceAsStream("resources/fonts/PressStart2P.ttf"));
			boxFont = arcadeFont.deriveFont(20.0f);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (FontFormatException e) {
			e.printStackTrace();
		}
		
		makeGUI(period);



		addWindowListener(this);
		pack();
		setResizable(false);
		setVisible(true);
	}

	private void makeGUI(long period) {
		Container c = getContentPane();
		
		gp = new GamePanel(this,period);
		
		c.add(gp,"Center");

		JPanel ctrls = new JPanel();
		ctrls.setLayout(new BoxLayout(ctrls, BoxLayout.X_AXIS));
		ctrls.setBackground(Color.DARK_GRAY);
		scoreBox = new JTextField("Score: 0");
		scoreBox.setEditable(false);
		scoreBox.setFont(boxFont);
		scoreBox.setOpaque(false);
		scoreBox.setForeground(Color.WHITE);
		scoreBox.setBorder(BorderFactory.createEmptyBorder());
		ctrls.add(scoreBox);

		JPanel filler = new JPanel();
		Dimension d = new Dimension(350,0);
		filler.setSize(d);
		filler.setPreferredSize(d);
		filler.setMaximumSize(d);
		filler.setMinimumSize(d);
		filler.setBorder(BorderFactory.createEmptyBorder());
		filler.setOpaque(false);
		ctrls.add(filler);
		
		levelBox = new JTextField("Level: 1");
		levelBox.setEditable(false);
		levelBox.setFont(boxFont);
		levelBox.setOpaque(false);
		levelBox.setForeground(Color.WHITE);
		levelBox.setBorder(BorderFactory.createEmptyBorder());
		levelBox.setAlignmentX(Component.RIGHT_ALIGNMENT);
		ctrls.add(levelBox);

		c.add(ctrls,"South");
	}

	public void setLevelBoxNumber(int level) {
		levelBox.setText("Level: "+level);
	}

	public void setScoreBoxNumber(int score) {
		scoreBox.setText("Score: "+score);
	}

	//----MAIN------------------------

	public static void main(String[] args) {
		long period = (long)(1000.0/DEFAULT_FPS);
		new Game(period);
	}

	//----WINDOWLISTENER--------------

	@Override
	public void windowActivated(WindowEvent arg0) {
		gp.resumeGame();		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		gp.stopGame();		
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		gp.pauseGame();		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		gp.resumeGame();		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		gp.pauseGame();
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
	}
}
