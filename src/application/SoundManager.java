package application;

import java.io.ByteArrayInputStream;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class SoundManager {
	private static boolean muted = false;
	private static Clip bgmClip = null;
	private static float volume = -6.0f; // in dB

	public static boolean isMuted() {
		return muted;
	}

	public static void setMuted(boolean m) {
		muted = m;
		if (muted) {
			stopBGM();
		} else {
			playBGM();
		}
	}

	public static void toggleMute() {
		setMuted(!muted);
	}

	private static Clip createClip(byte[] pcmData, float sampleRate) {
		try {
			AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
			AudioInputStream ais = new AudioInputStream(
					new ByteArrayInputStream(pcmData), format, pcmData.length / 2);
			Clip clip = AudioSystem.getClip();
			clip.open(ais);
			if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
				FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
				gain.setValue(volume);
			}
			return clip;
		} catch (Throwable t) {
			return null;
		}
	}

	private static void playGenerated(byte[] pcmData) {
		if (muted) return;
		try {
			Clip c = createClip(pcmData, 44100.0f);
			if (c != null) {
				c.addLineListener(e -> {
					if (e.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
						c.close();
					}
				});
				c.start();
			}
		} catch (Throwable ignored) {
		}
	}

	// 1. UI Button Click
	public static void playClick() {
		if (muted) return;
		int sampleRate = 44100;
		int samples = (int) (sampleRate * 0.05); // 50ms
		byte[] data = new byte[samples * 2];
		for (int i = 0; i < samples; i++) {
			double t = (double) i / sampleRate;
			double freq = 700.0 - (t * 4000.0);
			double env = Math.exp(-t * 60.0);
			short val = (short) (Math.sin(2 * Math.PI * freq * t) * 12000 * env);
			data[i * 2] = (byte) (val & 0xFF);
			data[i * 2 + 1] = (byte) ((val >> 8) & 0xFF);
		}
		playGenerated(data);
	}

	// 2. Candy Selected Pop
	public static void playSelect() {
		if (muted) return;
		int sampleRate = 44100;
		int samples = (int) (sampleRate * 0.08); // 80ms
		byte[] data = new byte[samples * 2];
		for (int i = 0; i < samples; i++) {
			double t = (double) i / sampleRate;
			double freq = 550.0 + (t * 4500.0); // pitch goes up
			double env = Math.exp(-t * 40.0);
			short val = (short) (Math.sin(2 * Math.PI * freq * t) * 14000 * env);
			data[i * 2] = (byte) (val & 0xFF);
			data[i * 2 + 1] = (byte) ((val >> 8) & 0xFF);
		}
		playGenerated(data);
	}

	// 3. Candy Swap Swoosh
	public static void playSwap() {
		if (muted) return;
		int sampleRate = 44100;
		int samples = (int) (sampleRate * 0.12); // 120ms
		byte[] data = new byte[samples * 2];
		for (int i = 0; i < samples; i++) {
			double t = (double) i / sampleRate;
			double freq = 320.0 + (Math.sin(t * Math.PI / 0.12) * 280.0);
			double env = Math.sin(t * Math.PI / 0.12);
			short val = (short) (Math.sin(2 * Math.PI * freq * t) * 15000 * env);
			data[i * 2] = (byte) (val & 0xFF);
			data[i * 2 + 1] = (byte) ((val >> 8) & 0xFF);
		}
		playGenerated(data);
	}

	// 4. Candy Match / Crush Sparkle Chime (Sweet C-Major Chord)
	public static void playMatch() {
		if (muted) return;
		int sampleRate = 44100;
		int samples = (int) (sampleRate * 0.38); // 380ms
		byte[] data = new byte[samples * 2];
		double[] freqs = { 523.25, 659.25, 783.99, 1046.50 }; // C5, E5, G5, C6
		for (int i = 0; i < samples; i++) {
			double t = (double) i / sampleRate;
			double env = Math.exp(-t * 9.0);
			double sampleVal = 0.0;
			for (int f = 0; f < freqs.length; f++) {
				double delay = f * 0.035;
				if (t >= delay) {
					double localT = t - delay;
					sampleVal += Math.sin(2 * Math.PI * freqs[f] * localT) * Math.exp(-localT * 10.0);
				}
			}
			short val = (short) (sampleVal * 7000 * env);
			data[i * 2] = (byte) (val & 0xFF);
			data[i * 2 + 1] = (byte) ((val >> 8) & 0xFF);
		}
		playGenerated(data);
	}

	// 5. Multi-match Combo Chime
	public static void playCombo() {
		if (muted) return;
		int sampleRate = 44100;
		int samples = (int) (sampleRate * 0.45);
		byte[] data = new byte[samples * 2];
		double[] freqs = { 659.25, 830.61, 987.77, 1318.51 }; // E5, G#5, B5, E6
		for (int i = 0; i < samples; i++) {
			double t = (double) i / sampleRate;
			double sampleVal = 0.0;
			for (int f = 0; f < freqs.length; f++) {
				double delay = f * 0.04;
				if (t >= delay) {
					double localT = t - delay;
					sampleVal += Math.sin(2 * Math.PI * freqs[f] * localT) * Math.exp(-localT * 8.0);
				}
			}
			short val = (short) (sampleVal * 6500);
			data[i * 2] = (byte) (val & 0xFF);
			data[i * 2 + 1] = (byte) ((val >> 8) & 0xFF);
		}
		playGenerated(data);
	}

	// 6. Win Fanfare
	public static void playWin() {
		if (muted) return;
		int sampleRate = 44100;
		int samples = (int) (sampleRate * 1.2);
		byte[] data = new byte[samples * 2];
		double[] melody = { 523.25, 659.25, 783.99, 1046.50, 1318.51 };
		double noteDur = 0.18;
		for (int i = 0; i < samples; i++) {
			double t = (double) i / sampleRate;
			int noteIdx = Math.min((int) (t / noteDur), melody.length - 1);
			double noteT = t - (noteIdx * noteDur);
			double freq = melody[noteIdx];
			double env = Math.exp(-noteT * 5.0);
			if (noteIdx == melody.length - 1) {
				env = Math.exp(-noteT * 2.5); // sustain last note
			}
			short val = (short) ((Math.sin(2 * Math.PI * freq * t)
					+ 0.3 * Math.sin(4 * Math.PI * freq * t)) * 11000 * env);
			data[i * 2] = (byte) (val & 0xFF);
			data[i * 2 + 1] = (byte) ((val >> 8) & 0xFF);
		}
		playGenerated(data);
	}

	// 7. Game Over Melancholy Chime
	public static void playGameOver() {
		if (muted) return;
		int sampleRate = 44100;
		int samples = (int) (sampleRate * 1.1);
		byte[] data = new byte[samples * 2];
		double[] melody = { 783.99, 659.25, 523.25, 392.00 }; // G5 -> E5 -> C5 -> G4
		double noteDur = 0.25;
		for (int i = 0; i < samples; i++) {
			double t = (double) i / sampleRate;
			int noteIdx = Math.min((int) (t / noteDur), melody.length - 1);
			double noteT = t - (noteIdx * noteDur);
			double freq = melody[noteIdx];
			double env = Math.exp(-noteT * 4.0);
			short val = (short) (Math.sin(2 * Math.PI * freq * t) * 10000 * env);
			data[i * 2] = (byte) (val & 0xFF);
			data[i * 2 + 1] = (byte) ((val >> 8) & 0xFF);
		}
		playGenerated(data);
	}

	// 8. Cheerful Looping BGM (Sweet Marimba Lofi Melody)
	public static synchronized void playBGM() {
		if (muted) return;
		if (bgmClip != null && bgmClip.isRunning()) return;

		try {
			int sampleRate = 22050; // lighter sample rate for background loop
			// 4-measure loop at 120 bpm (8 seconds)
			int totalSec = 8;
			int samples = sampleRate * totalSec;
			byte[] data = new byte[samples * 2];

			// Upbeat cheerful kalimba / bell notes
			double[] notes = {
				523.25, 659.25, 783.99, 880.00, // C5, E5, G5, A5
				783.99, 659.25, 587.33, 523.25, // G5, E5, D5, C5
				587.33, 659.25, 783.99, 659.25, // D5, E5, G5, E5
				523.25, 440.00, 523.25, 587.33  // C5, A4, C5, D5
			};
			double noteLen = (double) totalSec / notes.length; // 0.5s per note

			for (int i = 0; i < samples; i++) {
				double t = (double) i / sampleRate;
				int noteIdx = (int) (t / noteLen) % notes.length;
				double noteT = t % noteLen;
				double freq = notes[noteIdx];
				double env = Math.exp(-noteT * 7.0);

				// gentle harmonic bell
				double wave = Math.sin(2 * Math.PI * freq * t)
						+ 0.35 * Math.sin(4 * Math.PI * freq * t)
						+ 0.15 * Math.sin(6 * Math.PI * freq * t);
				short val = (short) (wave * 3500 * env);
				data[i * 2] = (byte) (val & 0xFF);
				data[i * 2 + 1] = (byte) ((val >> 8) & 0xFF);
			}

			AudioFormat format = new AudioFormat(sampleRate, 16, 1, true, false);
			AudioInputStream ais = new AudioInputStream(
					new ByteArrayInputStream(data), format, data.length / 2);
			bgmClip = AudioSystem.getClip();
			bgmClip.open(ais);
			if (bgmClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
				FloatControl gain = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);
				gain.setValue(-12.0f); // background music volume soft & pleasant
			}
			bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
		} catch (Throwable t) {
			// BGM failure is non-fatal
		}
	}

	public static synchronized void stopBGM() {
		try {
			if (bgmClip != null) {
				bgmClip.stop();
				bgmClip.close();
				bgmClip = null;
			}
		} catch (Throwable ignored) {
		}
	}
}
