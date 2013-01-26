package org.ggj2013;

import java.util.HashMap;
import java.util.Map;

public class SoundPackStandard extends SoundPack {
	public static final String BEAT = "BEAT";
	public static final String BREATH = "BREATH";

	public static final String GOAL = "GOAL";

	public static final String ENEMY_BIG = "ENEMY_BIG";
	public static final String ENEMY_MEDIUM = "ENEMY_MEDIUM";
	public static final String ENEMY_SMALL = "ENEMY_SMALL";

	@Override
	public Map<String, Integer> getAllSounds() {
		Map<String, Integer> sounds = new HashMap<String, Integer>();

		sounds.put(SoundPackStandard.BEAT, R.raw.beat);
		sounds.put(SoundPackStandard.BREATH, R.raw.breath);
		sounds.put(SoundPackStandard.ENEMY_BIG, R.raw.enemy_big);
		sounds.put(SoundPackStandard.ENEMY_MEDIUM, R.raw.enemy_medium);
		sounds.put(SoundPackStandard.ENEMY_SMALL, R.raw.enemy_small);

		sounds.put(SoundPackStandard.GOAL, R.raw.ziel_neu_02);

		return sounds;
	}
}
