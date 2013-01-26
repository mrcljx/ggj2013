package org.ggj2013;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;

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
	public Map<Integer, Boolean> soundLoaded = new HashMap<Integer, Boolean>();
	public boolean loaded = false;

	public SoundManager(Context appContext) {
		sndPool = new SoundPool(16, AudioManager.STREAM_MUSIC, 100);
		sndPool.setOnLoadCompleteListener(new OnLoadCompleteListener() {
			@Override
			public void onLoadComplete(SoundPool soundPool, int sampleId,
					int status) {
				soundLoaded.put(sampleId, (status == 0 ? true : false));

				if (soundLoaded.size() == sounds.size()) {
					loaded = true;
				}
			}
		});
		pContext = appContext;
	}

	public int load(int sound_id) {
		return sndPool.load(pContext, sound_id, 1);
	}

	public void play(final String ident, final String soundKey,
			float[] balance, final float masterVolume, final int loops) {

		Log.d("SoundManager", "Start playing sound: " + ident);

		int streamId = sndPool.play(sounds.get(soundKey), masterVolume
				* balance[0], masterVolume * balance[1], 1, loops, rate);

		if (streamId == 0) {
			throw new RuntimeException("Failed to play sound!");
		}

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