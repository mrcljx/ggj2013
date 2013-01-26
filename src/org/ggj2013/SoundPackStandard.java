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

	public static final String HEARTBEAT_01 = "HEARTBEAT_01";
	public static final String HEARTBEAT_02 = "HEARTBEAT_02";
	public static final String HEARTBEAT_03 = "HEARTBEAT_03";
	public static final String HEARTBEAT_04 = "HEARTBEAT_04";
	public static final String HEARTBEAT_05 = "HEARTBEAT_05";

	@Override
	public Map<String, Integer> getAllSounds() {
		Map<String, Integer> sounds = new HashMap<String, Integer>();

		sounds.put(SoundPackStandard.HEARTBEAT_01, R.raw.heartbeat_01);
		sounds.put(SoundPackStandard.HEARTBEAT_02, R.raw.heartbeat_02);
		sounds.put(SoundPackStandard.HEARTBEAT_03, R.raw.heartbeat_03);
		sounds.put(SoundPackStandard.HEARTBEAT_04, R.raw.heartbeat_04);
		sounds.put(SoundPackStandard.HEARTBEAT_05, R.raw.heartbeat_05);

		sounds.put(SoundPackStandard.BREATH, R.raw.breath);
		sounds.put(SoundPackStandard.ENEMY_BIG, R.raw.enemy_big);
		sounds.put(SoundPackStandard.ENEMY_MEDIUM, R.raw.enemy_medium);
		sounds.put(SoundPackStandard.ENEMY_SMALL, R.raw.enemy_small);

		sounds.put(SoundPackStandard.GOAL, R.raw.ziel_neu_02);

		return sounds;
	}
}
