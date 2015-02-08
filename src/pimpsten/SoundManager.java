package pimpsten;

import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;


/**
 * Classe que s'encarrega de la gestió dels sons del joc.
 * @author Oscar Saleta
 * TODO: poder carregar els sons des del fitxer jar.
 */
public class SoundManager {

	private final static SoundManager single = new SoundManager();
	private final static Clip BACKGROUND = loadAudioFile("resources/sound/atari.wav",-20f);
	private final static Clip SHOT = loadAudioFile("resources/sound/fire.wav",0f);
	private final static Clip LARGE_EXPLOSION = loadAudioFile("resources/sound/bangLarge.wav", -10f);
	private final static Clip SAUCER_MUSIC = loadAudioFile("resources/sound/saucer.wav",-20f);
	
	private static boolean mediumExplosionBoolean = true;
	/**
	 * Per saber si els sons estàn deshabilitats
	 */
	public static volatile boolean IS_MUTED = false;


	/**
	 * Música per quan apareix un ovni
	 */
	public static void playSaucerMusic() {
		play(SAUCER_MUSIC);
	}
	
	/**
	 * So d'un tret
	 */
	public static void playShot() {
		play(SHOT);
	}

	
	/**
	 * So d'explosió d'un asteroide
	 */
	public static void playMediumExplosion() {
		// Ho fem així perquè si declarem una explosió mitjana estàtica
		// i intentem reproduir-ne dues molt seguides es talla el so
		if (mediumExplosionBoolean) {
			Clip mediumExplosion = loadAudioFile("resources/sound/bangMedium.wav",-10f);
			play(mediumExplosion);
		}
	}

	/**
	 * So d'explosió d'un ovni o de la nau
	 */
	public static void playLargeExplosion() {
		play(LARGE_EXPLOSION);
	}

	/**
	 * Música de fons
	 */
	public static void playBackgroundMusic() {
		BACKGROUND.setFramePosition(0);
		loop(BACKGROUND);
	}

	/**
	 * Mutejar tots els sons del joc
	 */
	public static void muteAllSounds() {
		IS_MUTED = true;
		mute(BACKGROUND, true);
		mute(SHOT, true);
		mediumExplosionBoolean = false;
		mute(LARGE_EXPLOSION, true);
		mute(SAUCER_MUSIC, true);
	}

	/**
	 * Des-mutejar tots els sons
	 */
	public static void unMuteAllSound() {
		IS_MUTED = false;
		mute(BACKGROUND, false);
		mute(SHOT, false);
		mediumExplosionBoolean = true;
		mute(LARGE_EXPLOSION, false);
		mute(SAUCER_MUSIC, false);
	}

	private static void mute(Clip clip, boolean bool) {
		BooleanControl mute = (BooleanControl) clip.getControl(BooleanControl.Type.MUTE);
		mute.setValue(bool);
	}


	private static Clip loadAudioFile(String s, float gain) {
		Clip clip = null;
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(single.getClass().getResourceAsStream(s));
			DataLine.Info info = new DataLine.Info(Clip.class, ais.getFormat());
			clip = (Clip)AudioSystem.getLine(info);
			clip.open(ais);
			FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
			volume.setValue(gain);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
		return clip;
	}

	private static void play(Clip clip)  {
		clip.setFramePosition(0);
		clip.loop(0);
		clip.start();
	}

	private static void loop(Clip clip) {
		clip.loop(Clip.LOOP_CONTINUOUSLY);
	}

}
