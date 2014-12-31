package pimpsten;

import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JOptionPane;


/**
 * Classe per manejar les puntuacions màximes del jocs. S'usa un fitxer de suport
 * 
 * @author Oscar Saleta
 *
 * TODO: ordenar puntuacions, afegir puntuacions
 */
public class ScoreManager {

	public Game game;
	
	private FileInputStream fis;
	private ObjectInputStream ois;
	private boolean scoreFileIsEmpty;
	private FileOutputStream fos;
	private ObjectOutputStream oos;
	private int highScore;
	private String path;
	private TreeMap<Integer,String> tm;
	private TreeMap<Integer,String> tm2 = new TreeMap<Integer,String>(
			new Comparator<Integer>() {
				@Override
				public int compare(Integer o1, Integer o2) {
					return -o1.compareTo(o2);
				}
			});

	private String playerNames;
	private String playerScores;

	ScoreManager(String path, Game game) {
		this.path = path;
		this.game = game;
		//		openReader();
		//		readTreeMap();
		//		closeReader();
	}

	private void openReader() {
		try {
			fis = new FileInputStream(this.getClass().getResource(path).getPath());
			ois = new ObjectInputStream(fis);
			scoreFileIsEmpty = false;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (EOFException e) {
			scoreFileIsEmpty = true;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void closeReader() {
		try {
			ois.close();
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void openWriter() {
		try {
			fos = new FileOutputStream(this.getClass().getResource(path).getPath());
			oos = new ObjectOutputStream(fos);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void closeWriter() {
		try {
			oos.close();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readTreeMap() {
		openReader();
		if (scoreFileIsEmpty)
			tm = new TreeMap<Integer,String>();
		else {
			try {
				tm = (TreeMap<Integer,String>)ois.readObject();
			} catch (IOException e) {
				tm = new TreeMap<Integer,String>();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			closeReader();
		}
	}

	private void rewriteTreeMap() {
		openWriter();
		try {
			oos.writeObject(tm);
		} catch (IOException e) {
			e.printStackTrace();
		}
		closeWriter();
	}


	public void addNewHighScore(int score) {
		String s = "<html><body width='250'><h1>New Highscore: "+score+" </h1>" +
				"<p>Insert player's name." +
				" <br>";

		String player = (String) JOptionPane.showInputDialog(null,s,"New highscore!",
				JOptionPane.INFORMATION_MESSAGE);

		tm.put(score, player);
		rewriteTreeMap();

	}

	public int getHighScore() {
		readTreeMap();
		if (tm.size() == 0)
			return 0;
		highScore = tm.lastKey();
		return highScore;
	}

	public String getPlayerNames() {
		playerNames = "";
		
		tm.clear();
		readTreeMap();
		tm2.clear();
		tm2.putAll(tm);
		
		for(Map.Entry<Integer, String> entry : tm2.entrySet()) {
			playerNames += entry.getKey()+System.lineSeparator();
		}
		
		return playerNames;
	}
	
	public String getPlayerScores() {
		playerScores = "";
		
		tm.clear();
		readTreeMap();
		tm2.clear();
		tm2.putAll(tm);

		for(Map.Entry<Integer, String> entry : tm2.entrySet()) {
			playerScores += entry.getValue()+System.lineSeparator();
		}
		
		return playerScores;
	}
	
	
	
	
//	public void showScores() {
//		tm.clear();
//		openReader();
//		readTreeMap();
//		closeReader();
//
//		tm2.clear();
//		tm2.putAll(tm);
//
//		String s ="<html><body width='250'><h1>Highscores:</h1>";
//
//		for (Map.Entry<Integer, String> entry : tm2.entrySet()) {
//			s+="<b1>"+entry.getKey()+"</b1> <p1>"+entry.getValue()+"</p1>"+System.lineSeparator();
//		}
//
//		JOptionPane.showMessageDialog(null, s, "Highscores", JOptionPane.INFORMATION_MESSAGE);
//	}

	public void eraseScores() {
		tm.clear();
		rewriteTreeMap();
	}

}
