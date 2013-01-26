package org.ggj2013;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundManager {
	private final Context pContext;
	private final SoundPool sndPool;
	private final float rate = 1.0f;

	public static final float[] BALANCE_FULL_RIGHT = new float[] { 0.0f, 1.0f };
	public static final float[] BALANCE_HALF_RIGHT = new float[] { 0.5f, 1.0f };
	public static final float[] BALANCE_FULL_LEFT = new float[] { 1.0f, 0.0f };
	public static final float[] BALANCE_HALF_LEFT = new float[] { 0.5f, 1.0f };
	public static final float[] BALANCE_CENTER = new float[] { 1.0f, 1.0f };

	public static final float VOLUME_100 = 1f;
	public static final float VOLUME_50 = 0.5f;

	public Map<String, Integer> sounds = new HashMap<String, Integer>();

	public SoundManager(Context appContext) {
		sndPool = new SoundPool(16, AudioManager.STREAM_MUSIC, 100);
		pContext = appContext;
	}

	public int load(int sound_id) {
		return sndPool.load(pContext, sound_id, 1);
	}

	public void play(String soundKey, float[] balance, float masterVolume) {
		sndPool.play(sounds.get(soundKey), masterVolume * balance[0],
				masterVolume * balance[1], 1, 0, rate);
	}

	public void loadSoundPack(SoundPack soundPack) {
		for (Map.Entry<String, Integer> sound : soundPack.getAllSounds()
				.entrySet()) {

			sounds.put(sound.getKey(), this.load(sound.getValue()));
		}
	}
}