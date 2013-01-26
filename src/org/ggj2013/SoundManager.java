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

	public static final int LOOPS_0 = 0;
	public static final int LOOPS_INFINITE = -1;

	public Map<String, Integer> sounds = new HashMap<String, Integer>();
	public Map<String, Integer> streams = new HashMap<String, Integer>();

	public SoundManager(Context appContext) {
		sndPool = new SoundPool(16, AudioManager.STREAM_MUSIC, 100);
		pContext = appContext;
	}

	public int load(int sound_id) {
		return sndPool.load(pContext, sound_id, 1);
	}

	public void play(String ident, String soundKey, float[] balance,
			float masterVolume, int loops) {
		int streamId = sndPool.play(sounds.get(soundKey), masterVolume
				* balance[0], masterVolume * balance[1], 1, loops, rate);

		streams.put(ident, streamId);
	}

	public void loadSoundPack(SoundPack soundPack) {
		for (Map.Entry<String, Integer> sound : soundPack.getAllSounds()
				.entrySet()) {

			sounds.put(sound.getKey(), this.load(sound.getValue()));
		}
	}

	public void changeVolume(String streamIdent, float[] balance,
			float masterVolume) {

		sndPool.setVolume(streams.get(streamIdent), masterVolume * balance[0],
				masterVolume * balance[1]);
	}

	// Sample calls
	/*
	 * soundManager.changeVolume("CatBackground",
	 * SoundManager.BALANCE_FULL_LEFT, 0.1f);
	 * 
	 * soundManager.play("CatBackground", SoundPackStandard.CAT_MEOW,
	 * SoundManager.BALANCE_FULL_LEFT, SoundManager.VOLUME_100,
	 * SoundManager.LOOPS_0);
	 */

}