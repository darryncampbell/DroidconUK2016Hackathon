package uk.co.droidcon.hack.bstf.models;

import android.support.annotation.DrawableRes;
import android.support.annotation.RawRes;

import java.util.List;

import rx.Observable;
import uk.co.droidcon.hack.bstf.R;

public enum Weapon {
    GLOCK(R.drawable.glock, R.drawable.ammo, "Glock", R.raw.pistol, R.raw.emptypop, R.raw.reload2),
    LASER(R.drawable.laser, R.drawable.laser_ammo, "Laser", R.raw.laser, R.raw.emptypop, R.raw.laser_reload);

    Weapon(@DrawableRes int imageId, @DrawableRes int ammoImageId, String name, @RawRes int shootSoundResId, @RawRes int emptySoundResId, @RawRes int reloadSoundResId) {
        this.imageId = imageId;
        this.ammoImageId = ammoImageId;
        this.name = name;
        this.shootSoundResId = shootSoundResId;
        this.emptySoundResId = emptySoundResId;
        this.reloadSoundResId = reloadSoundResId;
    }

    @DrawableRes private int imageId;
    @DrawableRes private int ammoImageId;
    private String name;
    @RawRes private int shootSoundResId;
    @RawRes private int emptySoundResId;
    @RawRes private int reloadSoundResId;

    // SoundPool ids
    private int shootSoundId;
    private int emptySoundId;
    private int reloadSoundId;

    @DrawableRes
    public int getImageId() {
        return imageId;
    }

    @DrawableRes
    public int getAmmoImageId() {
        return ammoImageId;
    }

    public String getName() {
        return name;
    }

    @RawRes
    public int getShootSoundResId() {
        return shootSoundResId;
    }

    @RawRes
    public int getEmptySoundResId() {
        return emptySoundResId;
    }

    @RawRes
    public int getReloadSoundResId() {
        return reloadSoundResId;
    }

    public int getShootSoundId() {
        return shootSoundId;
    }

    public int getEmptySoundId() {
        return emptySoundId;
    }

    public int getReloadSoundId() {
        return reloadSoundId;
    }

    public void setShootSoundId(int shootSoundId) {
        this.shootSoundId = shootSoundId;
    }

    public void setEmptySoundId(int emptySoundId) {
        this.emptySoundId = emptySoundId;
    }

    public void setReloadSoundId(int reloadSoundId) {
        this.reloadSoundId = reloadSoundId;
    }

    public static List<Weapon> getAll() {
        return Observable.from(Weapon.values()).toList().toBlocking().single();
    }
}
