package uk.co.droidcon.hack.bstf.sounds;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import java.lang.annotation.Retention;

import uk.co.droidcon.hack.bstf.R;

import static java.lang.annotation.RetentionPolicy.SOURCE;

@SuppressWarnings("deprecation")
public class SoundManager {

    private static SoundManager _instance;

    private SoundPool soundPool;

    @Retention(SOURCE)
    @IntDef({PISTOL, RELOAD})
    public @interface Sound {}
    public static final int UNDEFINED = -1;
    public static final int PISTOL = 0;
    public static final int RELOAD = 1;

    private int pistolId;
    private int reloadId;

    private SoundManager(@NonNull final Context context) {
        soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);
        pistolId = soundPool.load(context, R.raw.pistol, 1);
        reloadId = soundPool.load(context, R.raw.reload, 1);
    }

    public static SoundManager getInstance(@NonNull final Context context) {
        if (_instance == null) {
            _instance = new SoundManager(context);
        }
        return _instance;
    }

    public void playSound(@Sound final int soundType) {
        final int soundId = getSoundId(soundType);
        if (soundId != UNDEFINED) {
            soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 0);
        }
    }

    private int getSoundId(@Sound final int soundType) {
        switch (soundType) {
            case PISTOL:
                return pistolId;
            case RELOAD:
                return reloadId;
        }
        return UNDEFINED;
    }
}
