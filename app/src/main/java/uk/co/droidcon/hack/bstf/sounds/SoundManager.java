package uk.co.droidcon.hack.bstf.sounds;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.annotation.NonNull;

import java.util.List;

import timber.log.Timber;
import uk.co.droidcon.hack.bstf.R;
import uk.co.droidcon.hack.bstf.models.Weapon;

@SuppressWarnings("deprecation")
public class SoundManager {

    private static SoundManager _instance;

    private SoundPool soundPool;

    private int painSoundId;
    private int deadSoundId;

    private SoundManager(@NonNull final Context context) {
        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);

        final List<Weapon> allWeapons = Weapon.getAll();
        for (Weapon weapon : allWeapons) {
            final int shootId = soundPool.load(context, weapon.getShootSoundResId(), 1);
            final int emptyId = soundPool.load(context, weapon.getEmptySoundResId(), 1);
            final int reloadId = soundPool.load(context, weapon.getReloadSoundResId(), 1);
            weapon.setShootSoundId(shootId);
            weapon.setEmptySoundId(emptyId);
            weapon.setReloadSoundId(reloadId);
        }

        painSoundId = soundPool.load(context, R.raw.pain, 1);
        deadSoundId = soundPool.load(context, R.raw.dead, 1);
    }

    public int getPainSoundId() {
        return painSoundId;
    }

    public int getDeadSoundId() {
        return deadSoundId;
    }

    public static SoundManager getInstance(@NonNull final Context context) {
        if (_instance == null) {
            _instance = new SoundManager(context);
        }
        return _instance;
    }

    public void playSound(final int soundId) {
        if (soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 0) == 0 ) Timber.e("SOUND WITH ID %d didn\'t play", soundId);
    }
}
