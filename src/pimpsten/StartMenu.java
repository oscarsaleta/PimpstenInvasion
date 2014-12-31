package pimpsten;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 * Classe per posar un menú superior a la finestra del joc (controls de volum, restart joc, etc)
 * 
 * @author Oscar Saleta
 *
 */
public class StartMenu implements ActionListener {
	
	Game game;
	JFrame window;
	
	private JMenuItem jmGameRestart;
	private JMenuItem jmGameScoresShow;
	private JMenuItem jmGameScoresClear;
	private JMenuItem jmGameExit;
	private JMenuItem jmMuteMusic;
	private JMenuItem jmMuteAll;
	private JMenuItem jmUnMuteMusic;
	private JMenuItem jmUnMuteAll;
	private JMenuItem jmHelpAbout;
	
	StartMenu(Game game) {
		this.game = game;
		this.window = game.window;

		JMenuBar jmb = new JMenuBar();

		JMenu jmGame = new JMenu("Game");
		jmGameRestart = new JMenuItem("Restart");
		JMenu jmGameScores = new JMenu("Scores");
		jmGameScoresShow = new JMenuItem("Show");
		jmGameScoresClear = new JMenuItem("Clear");
		jmGameScores.add(jmGameScoresShow);
		jmGameScores.add(jmGameScoresClear);
		jmGameExit = new JMenuItem("Exit");
		jmGame.add(jmGameRestart);
		jmGame.add(jmGameScores);
		jmGame.addSeparator();
		jmGame.add(jmGameExit);
		jmb.add(jmGame);

		
		
		JMenu jmSound = new JMenu("Sound");
		JMenu jmMute = new JMenu("Mute");
		jmMuteMusic = new JMenuItem("Music");
		jmMuteAll = new JMenuItem("All sounds");
		jmMute.add(jmMuteMusic);
		jmMute.add(jmMuteAll);
		JMenu jmUnMute = new JMenu("Unmute");
		jmUnMuteMusic = new JMenuItem("Music");
		jmUnMuteAll = new JMenuItem("All sounds");
		jmUnMute.add(jmUnMuteMusic);
		jmUnMute.add(jmUnMuteAll);

		jmSound.add(jmMute);
		jmSound.add(jmUnMute);
		jmb.add(jmSound);

		JMenu jmHelp = new JMenu("Help");
		jmHelpAbout = new JMenuItem("About");
		jmHelp.add(jmHelpAbout);
		jmb.add(jmHelp);

		jmGameRestart.addActionListener(this);
		jmGameScoresShow.addActionListener(this);
		jmGameScoresClear.addActionListener(this);
		jmGameExit.addActionListener(this);
		jmMuteMusic.addActionListener(this);
		jmMuteAll.addActionListener(this);
		jmUnMuteMusic.addActionListener(this);
		jmUnMuteAll.addActionListener(this);
		jmHelpAbout.addActionListener(this);

		window.setJMenuBar(jmb);
	}
	
	public void actionPerformed(ActionEvent ae) {
		Object o = ae.getSource();
		if (o == jmGameRestart) {
			game.waitingForKeyPress = true;
			game.beginGame();
		} else if (o == jmGameScoresShow) {
			game.gameEvent = 2;
		} else if (o == jmGameScoresClear) {
			game.gameSM.eraseScores();
		} else if (o == jmGameExit) {
			System.exit(0);
		} else if (o == jmMuteMusic) {
			SoundManager.muteBackgroundMusic();
		} else if (o == jmMuteAll) {
			SoundManager.muteAllSounds();
		} else if (o == jmUnMuteMusic) {
			SoundManager.unMuteBackgroundMusic();
		} else if (o == jmUnMuteAll) {
			SoundManager.unMuteAllSound();
		} else if (o == jmHelpAbout) {
			String pt1 = "<html><body width='";
            String pt2 =
                "'><h1>Pimpsten Invasion</h1>" +
                "<p>Pimpsten Invasion és un joc Java Swing amb fins acadèmics," +
                " creat com a treball per l'assignatura optativa Programació " +
                " Avançada del grau en Matemàtiques de la UAB." +
                " <br><br>" + 
                " <p>Autor: Oscar Saleta Reig";
            
            int width = 250;
            String s = pt1+width+pt2;
			
			JOptionPane.showMessageDialog(null, s);
		}
	}

}