package uk.co.droidcon.hack.bstf.sounds;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import java.lang.annotation.Retention;

import timber.log.Timber;
import uk.co.droidcon.hack.bstf.R;

import static java.lang.annotation.RetentionPolicy.SOURCE;

@SuppressWarnings("deprecation")
public class SoundManager {

    private static SoundManager _instance;

    private SoundPool soundPool;

    @Retention(SOURCE)
    @IntDef({PISTOL, RELOAD, LASER, PAIN, DEATH, EMPTY_POP})
    public @interface Sound {}
    private static final int UNDEFINED = -1;
    public static final int PISTOL = 0;
    public static final int RELOAD = 1;
    public static final int LASER =2;
    public static final int PAIN = 3;
    public static final int DEATH = 4;
    public static final int EMPTY_POP = 5;

    private int pistolId;
    private int reloadId;
    private int laserId;
    private int painId;
    private int deathId;
    private int emptyPopId;

    private SoundManager(@NonNull final Context context) {
        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 0);
        pistolId = soundPool.load(context, R.raw.pistol, 1);
        reloadId = soundPool.load(context, R.raw.reload2, 1);
        laserId = soundPool.load(context, R.raw.laser, 1);
        painId = soundPool.load(context, R.raw.pain, 1);
        deathId = soundPool.load(context, R.raw.dead, 1);
        emptyPopId = soundPool.load(context, R.raw.emptypop, 1);
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
            if (soundPool.play(soundId, 1.0f, 1.0f, 1, 0, 0) == 0 ) Timber.e("SOUND WITH ID %d didn\'t play", soundId);
        }
    }

    private int getSoundId(@Sound final int soundType) {
        switch (soundType) {
            case PISTOL:
                return pistolId;
            case RELOAD:
                return reloadId;
            case DEATH:
                return deathId;
            case LASER:
                return laserId;
            case PAIN:
                return painId;
            case EMPTY_POP:
                return emptyPopId;
        }
        return UNDEFINED;
    }
}
