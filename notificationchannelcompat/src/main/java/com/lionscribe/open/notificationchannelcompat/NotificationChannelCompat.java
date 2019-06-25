/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 6/25/2019 Rewritten by Lionscribe as Compat to support older devices
 *
 */
package com.lionscribe.open.notificationchannelcompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.CheckResult;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.util.Arrays;

/**
 * A representation of settings that apply to a collection of similarly themed notifications.
 */
public final class NotificationChannelCompat implements Parcelable {

    public static final Creator<NotificationChannelCompat> CREATOR = new Creator<NotificationChannelCompat>() {
        @Override
        public NotificationChannelCompat createFromParcel(Parcel in) {
            return new NotificationChannelCompat(in);
        }

        @Override
        public NotificationChannelCompat[] newArray(int size) {
            return new NotificationChannelCompat[size];
        }
    };
    static final String TAG = "ChannelsCompat";
    /**
     * The maximum length for text fields in a NotificationChannelCompat. Fields will be truncated at this
     * limit.
     */
    private static final int MAX_TEXT_LENGTH = 500;

    private static final int DEFAULT_LIGHT_COLOR = 0;
    private static final int DEFAULT_VISIBILITY = NotificationCompat.VISIBILITY_PRIVATE;
    private static final int DEFAULT_IMPORTANCE = NotificationManagerCompat.IMPORTANCE_UNSPECIFIED;

    private final String mId;
    private NotificationChannel _oreoNotificationChannel;
    private String mName;
    private String mDesc;
    private boolean mChannelEnabled = true;
    private int mImportance = DEFAULT_IMPORTANCE;
    private int mLockscreenVisibility = DEFAULT_VISIBILITY;
    private Uri mSound = Settings.System.DEFAULT_NOTIFICATION_URI;
    private boolean mLights;
    private int mLightColor = DEFAULT_LIGHT_COLOR;
    private long[] mVibration;
    // Bitwise representation of fields that have been changed by the user, preventing the app from
    // making changes to these fields.
    private boolean mVibrationEnabled;
    private String mGroup;
    private AudioAttributes mAudioAttributes = null;  // Notification.AUDIO_ATTRIBUTES_DEFAULT;
    private int mAudioStreamType = NotificationCompat.STREAM_DEFAULT;

    /**
     * Creates a notification channel.
     *
     * @param id         The id of the channel. Must be unique per package. The value may be truncated if
     *                   it is too long.
     * @param name       The user visible name of the channel. You can rename this channel when the system
     *                   locale changes by listening for the {@link Intent#ACTION_LOCALE_CHANGED}
     *                   broadcast. The recommended maximum length is 40 characters; the value may be
     *                   truncated if it is too long.
     * @param importance The importance of the channel. This controls how interruptive notifications
     *                   posted to this channel are.
     */
    public NotificationChannelCompat(String id, CharSequence name, int importance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _oreoNotificationChannel = new NotificationChannel(id, name, importance);
            this.mId = null; // just to make compiler happy
        } else {
            this.mId = getTrimmedString(id);
            this.mName = name != null ? getTrimmedString(name.toString()) : null;
            this.mImportance = importance;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected NotificationChannelCompat(NotificationChannel original) {
        _oreoNotificationChannel = original;
        this.mId = null; // just to make compiler happy
    }

    protected NotificationChannelCompat(Parcel in) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _oreoNotificationChannel = in.readParcelable(NotificationChannel.class.getClassLoader());
            this.mId = null; // just to make compiler happy
            return;
        }

        if (in.readByte() != 0) {
            mId = in.readString();
        } else {
            mId = null;
        }
        if (in.readByte() != 0) {
            mName = in.readString();
        } else {
            mName = null;
        }
        if (in.readByte() != 0) {
            mDesc = in.readString();
        } else {
            mDesc = null;
        }
        mChannelEnabled = in.readByte() != 0;
        mImportance = in.readInt();
        mLockscreenVisibility = in.readInt();
        if (in.readByte() != 0) {
            mSound = Uri.CREATOR.createFromParcel(in);
        } else {
            mSound = null;
        }
        mLights = in.readByte() != 0;
        mVibration = in.createLongArray();
        mVibrationEnabled = in.readByte() != 0;
        if (in.readByte() != 0) {
            mGroup = in.readString();
        } else {
            mGroup = null;
        }
        boolean readAudioAttributes = in.readInt() > 0;
        if (readAudioAttributes && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // unnecessary check for compiler, as in Pre-Lollipop readAudioAttributes will always be false
            mAudioAttributes = AudioAttributes.CREATOR.createFromParcel(in);
        } else {
            mAudioAttributes = null;
        }
        mAudioStreamType = in.readInt();
        mLightColor = in.readInt();
    }


    // Modifiable by apps post channel creation

    @CheckResult
    static public boolean applyChannel(Context context, Notification notif, String channelId) {
        if (TextUtils.isEmpty(channelId)) {
            Log.w(TAG, "Cannot create notification without channel set!");
            return false;
        }

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            if (!channelId.equals(notif.getChannelId())) {
                try {
                    Field f1 = notif.getClass().getSuperclass().getDeclaredField("mChannelId");
                    f1.setAccessible(true);
                    f1.set(notif, channelId);
                } catch (Exception e) {
                    //caller should already have set channel, but we just use reflection to do again, so if it fails it will be users issue
                    Log.w(TAG, "Must set Notification channel correctly before call to applyChannel");
                    return false;
                }
            }
            return true;
        }


        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannelManagerHelper notificationManagerHelper = new NotificationChannelManagerHelper(context, notificationManager);
        if (!notificationManagerHelper.isNotificationsEnabled()) {
            Log.d(TAG, "Notifications are disabled. Showing no notification!");
            return false;
        }

        NotificationChannelCompat channel = notificationManagerHelper.getNotificationChannel(channelId);
        if (channel == null) {
            Log.w(TAG, "Cannot create notification with unknown channel \"" + channelId + "\"!");
            return false;
        }

        if (!channel.isEnabled() || channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
            Log.d(TAG, "Channel is disabled. Showing no notification!");
            return false;
        }

        if (channel.getGroup() != null) {
            NotificationChannelGroupCompat group = notificationManagerHelper.getNotificationChannelGroup(channel.getGroup());
            if (group != null && group.isBlocked()) {
                Log.d(TAG, "Group is disabled. Showing no notification!");
                return false;
            }
        }

        notif.defaults = 0;  // clear all default flags

        notif.sound = (channel.getImportance() >= NotificationManagerCompat.IMPORTANCE_DEFAULT ? channel.getSound() : null);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && channel.getAudioAttributes() != null) {
            notif.audioAttributes = channel.getAudioAttributes();
        } else {
            notif.audioStreamType = channel.getAudioStreamType();
        }

        boolean permissionGrantedVibrate = ContextCompat.checkSelfPermission(context, "android.permission.VIBRATE") == PackageManager.PERMISSION_GRANTED;
        if (channel.shouldVibrate()) {
            if (channel.getVibrationPattern() != null && permissionGrantedVibrate) // when no permission we use DEFAULT VIBRATE which on some OS does not need permission
                notif.vibrate = channel.getVibrationPattern();
            else if (permissionGrantedVibrate || Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)  // As of 4.2.1 permission is not needed for DEFAULT_VIBRATE. See https://android.googlesource.com/platform/frameworks/base/+/cc2e849
                notif.defaults += Notification.DEFAULT_VIBRATE;
            else
                notif.vibrate = null;
        } else if (Build.VERSION.SDK_INT >= 21 && channel.getImportance() == NotificationManagerCompat.IMPORTANCE_HIGH && notif.sound == null) // then headsup won't be shown without vibrate, as there is no sound
        {
            if (permissionGrantedVibrate)
                notif.vibrate = new long[0];   // we put an empty vibrate, that fools system in cases that PRIORITY_MAX should show HeadsUp even without sound
            else
                notif.defaults += Notification.DEFAULT_VIBRATE; // too bad, vibrate!
        } else {
            notif.vibrate = null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notif.priority = (channel.getImportance() - NotificationManagerCompat.IMPORTANCE_DEFAULT); // NotificationManager.PRIORITY_DEFAULT = 3, while Notification.PRIORITY_DEFAULT = 0, so we have to deduct
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notif.visibility = channel.getLockscreenVisibility();
        }

        if (channel.shouldShowLights()) {
            notif.ledARGB = channel.getLightColor();
            if (notif.ledARGB == DEFAULT_LIGHT_COLOR) {
                notif.defaults += Notification.DEFAULT_LIGHTS;
            } else {
                try {
                    Resources resources = context.getResources();
                    Resources systemResources = Resources.getSystem();
                    // if (notif.ledARGB == DEFAULT_LIGHT_COLOR)
                    //      notif.ledARGB = ContextCompat.getColor(context, systemResources.getIdentifier("config_defaultNotificationColor", "color", "android"));
                    notif.ledOnMS = resources.getInteger(systemResources.getIdentifier("config_defaultNotificationLedOn", "integer", "android"));
                    notif.ledOffMS = resources.getInteger(systemResources.getIdentifier("config_defaultNotificationLedOff", "integer", "android"));
                } catch (Exception e) {
                    notif.ledOnMS = 500;
                    notif.ledOffMS = 2000;
                }
            }
            notif.flags = notif.flags | Notification.FLAG_SHOW_LIGHTS;
        } else {
            notif.flags = notif.flags & ~Notification.FLAG_SHOW_LIGHTS;
        }
        return true;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _oreoNotificationChannel.writeToParcel(dest, flags);
            return;
        }

        if (mId != null) {
            dest.writeByte((byte) 1);
            dest.writeString(mId);
        } else {
            dest.writeByte((byte) 0);
        }
        if (mName != null) {
            dest.writeByte((byte) 1);
            dest.writeString(mName);
        } else {
            dest.writeByte((byte) 0);
        }
        if (mDesc != null) {
            dest.writeByte((byte) 1);
            dest.writeString(mDesc);
        } else {
            dest.writeByte((byte) 0);
        }
        dest.writeByte(mChannelEnabled ? (byte) 1 : (byte) 0);
        dest.writeInt(mImportance);
        dest.writeInt(mLockscreenVisibility);
        if (mSound != null) {
            dest.writeByte((byte) 1);
            mSound.writeToParcel(dest, 0);
        } else {
            dest.writeByte((byte) 0);
        }
        dest.writeByte(mLights ? (byte) 1 : (byte) 0);
        dest.writeLongArray(mVibration);
        dest.writeByte(mVibrationEnabled ? (byte) 1 : (byte) 0);
        if (mGroup != null) {
            dest.writeByte((byte) 1);
            dest.writeString(mGroup);
        } else {
            dest.writeByte((byte) 0);
        }
        if (mAudioAttributes != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // unnecessary check for compiler, as in Pre-Lollipop mAudioAttributes will always be null
            dest.writeInt(1);
            mAudioAttributes.writeToParcel(dest, 0);
        } else {
            dest.writeInt(0);
        }
        dest.writeInt(mAudioStreamType);
        dest.writeInt(mLightColor);
    }

    private String getTrimmedString(String input) {
        if (input != null && input.length() > MAX_TEXT_LENGTH) {
            return input.substring(0, MAX_TEXT_LENGTH);
        }
        return input;
    }

    // Modifiable by apps on channel creation.

    /**
     * Sets whether notifications posted to this channel can appear as application icon badges
     * in a Launcher.
     * <p>
     * Only modifiable before the channel is submitted to
     * {NotificationManager#createNotificationChannelCompat(NotificationChannelCompat)}.
     *
     * @param showBadge true if badges should be allowed to be shown.
     */

    public void setShowBadge(boolean showBadge) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _oreoNotificationChannel.setShowBadge(showBadge);
        }
        // not supported pre-28
    }

    /**
     * Sets the sound that should be played for notifications posted to this channel.
     * Notification channels with an {@link #getImportance() importance} of at
     * least {@link NotificationManager#IMPORTANCE_DEFAULT} should have a sound.
     * <p>
     * Only modifiable before the channel is submitted to
     * {NotificationManager#createNotificationChannelCompat(NotificationChannelCompat)}.
     */
    public void setSound(Uri sound, int streamType) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _oreoNotificationChannel.setSound(sound, null);
            return;
        }
        this.mSound = sound;
        this.mAudioStreamType = streamType;
        // do not override mAudioAttributes as it may be set for later sdk
    }

    /**
     * Sets the sound that should be played for notifications posted to this channel and its
     * audio attributes. Notification channels with an {@link #getImportance() importance} of at
     * least {@link NotificationManager#IMPORTANCE_DEFAULT} should have a sound.
     * <p>
     * Only modifiable before the channel is submitted to
     * {NotificationManager#createNotificationChannelCompat(NotificationChannelCompat)}.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void setSound(Uri sound, AudioAttributes audioAttributes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _oreoNotificationChannel.setSound(sound, audioAttributes);
            return;
        }
        this.mSound = sound;
        this.mAudioAttributes = audioAttributes;
        // do not override mAudioStreamType as it may be set for earlier sdk
    }

    /**
     * Sets whether notifications posted to this channel should display notification lights,
     * on devices that support that feature.
     * <p>
     * Only modifiable before the channel is submitted to
     * {NotificationManager#createNotificationChannelCompat(NotificationChannelCompat)}.
     */
    public void enableLights(boolean lights) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _oreoNotificationChannel.enableLights(lights);
            return;
        }
        this.mLights = lights;
    }

    /**
     * Sets whether notification posted to this channel should vibrate. The vibration pattern can
     * be set with {@link #setVibrationPattern(long[])}.
     * <p>
     * Only modifiable before the channel is submitted to
     * {NotificationManager#createNotificationChannelCompat(NotificationChannelCompat)}.
     */
    public void enableVibration(boolean vibration) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _oreoNotificationChannel.enableVibration(vibration);
            return;
        }
        this.mVibrationEnabled = vibration;
    }

    /**
     * Sets whether or not notifications posted to this channel can interrupt the user in
     * {@link android.app.NotificationManager.Policy#INTERRUPTION_FILTER_PRIORITY} mode.
     * <p>
     * Only modifiable by the system and notification ranker.
     */
    public void setBypassDnd(boolean bypassDnd) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _oreoNotificationChannel.setBypassDnd(bypassDnd);
        }
    }

    /**
     * Returns the id of this channel.
     */
    public String getId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _oreoNotificationChannel.getId();
        }
        return mId;
    }

    /**
     * Returns the user visible name of this channel.
     */
    public CharSequence getName() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _oreoNotificationChannel.getName();
        }
        return mName;
    }

    /**
     * Sets the user visible name of this channel.
     *
     * <p>The recommended maximum length is 40 characters; the value may be truncated if it is too
     * long.
     */
    public void setName(CharSequence name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _oreoNotificationChannel.setName(name);
            return;
        }
        mName = name != null ? getTrimmedString(name.toString()) : null;
    }

    /**
     * Returns the user visible description of this channel.
     */
    public String getDescription() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _oreoNotificationChannel.getDescription();
        }
        return mDesc;
    }

    /**
     * Sets the user visible description of this channel.
     *
     * <p>The recommended maximum length is 300 characters; the value may be truncated if it is too
     * long.
     */
    public void setDescription(String description) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _oreoNotificationChannel.setDescription(description);
            return;
        }
        mDesc = getTrimmedString(description);
    }

    // Modifiable by a notification ranker.

    /**
     * Returns the user specified importance e.g. {@link NotificationManager#IMPORTANCE_LOW} for
     * notifications posted to this channel. Note: This value might be
     * {@link NotificationManager#IMPORTANCE_NONE}, but notifications posted to this channel will
     * not be shown to the user if the parent  or app is blocked.
     */
    public int getImportance() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _oreoNotificationChannel.getImportance();
        }
        return mImportance;
    }

    /**
     * Sets the level of interruption of this notification channel.
     * <p>
     * Only modifiable before the channel is submitted to
     * {NotificationManager#createNotificationChannelCompat(NotificationChannelCompat)}.
     *
     * @param importance the amount the user should be interrupted by
     *                   notifications from this channel.
     */
    public void setImportance(int importance) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _oreoNotificationChannel.setImportance(importance);
            return;
        }
        this.mImportance = importance;
    }

    /**
     * Whether or not notifications posted to this channel can bypass the Do Not Disturb
     * {@link NotificationManager#INTERRUPTION_FILTER_PRIORITY} mode.
     */
    public boolean canBypassDnd() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _oreoNotificationChannel.canBypassDnd();
        }
        return false;
    }

    /**
     * Returns the notification sound for this channel.
     */
    public Uri getSound() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _oreoNotificationChannel.getSound();
        }
        return mSound;
    }

    /**
     * Sets the sound that should be played for notifications posted to this channel.
     * Notification channels with an {@link #getImportance() importance} of at
     * least {@link NotificationManager#IMPORTANCE_DEFAULT} should have a sound.
     * <p>
     * Only modifiable before the channel is submitted to
     * {NotificationManager#createNotificationChannelCompat(NotificationChannelCompat)}.
     */
    public void setSound(Uri sound) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _oreoNotificationChannel.setSound(sound, null);
            return;
        }
        this.mSound = sound;
        this.mAudioAttributes = null;
        this.mAudioStreamType = NotificationCompat.STREAM_DEFAULT;
    }

    /**
     * Returns the audio strea for sound played by notifications posted to this channel on per-Lollypop devices.
     */
    public int getAudioStreamType() {
        return mAudioStreamType;
    }

    /**
     * Returns the audio attributes for sound played by notifications posted to this channel.
     */
    public AudioAttributes getAudioAttributes() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _oreoNotificationChannel.getAudioAttributes();
        }
        return mAudioAttributes;
    }

    /**
     * Returns whether notifications to this channel are shown
     */
    public boolean isEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _oreoNotificationChannel.getImportance() == NotificationManager.IMPORTANCE_NONE;
        }
        return mChannelEnabled;
    }

    protected void setEnabled(boolean enabled) {
        mChannelEnabled = enabled;
    }

    /**
     * Returns whether notifications posted to this channel trigger notification lights.
     */
    public boolean shouldShowLights() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _oreoNotificationChannel.shouldShowLights();
        }
        return mLights;
    }

    /**
     * Returns the notification light color for notifications posted to this channel. Irrelevant
     * unless {@link #shouldShowLights()}.
     */
    public int getLightColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _oreoNotificationChannel.getLightColor();
        }
        return mLightColor;
    }

    /**
     * Sets the notification light color for notifications posted to this channel, if lights are
     * {@link #enableLights(boolean) enabled} on this channel and the device supports that feature.
     * <p>
     * Only modifiable before the channel is submitted to
     * {NotificationManager#createNotificationChannelCompat(NotificationChannelCompat)}.
     */
    public void setLightColor(int argb) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _oreoNotificationChannel.setLightColor(argb);
            return;
        }
        this.mLightColor = argb;
    }

    /**
     * Returns whether notifications posted to this channel always vibrate.
     */
    public boolean shouldVibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _oreoNotificationChannel.shouldVibrate();
        }
        return mVibrationEnabled;
    }

    /**
     * Returns the vibration pattern for notifications posted to this channel. Will be ignored if
     * vibration is not enabled ({@link #shouldVibrate()}.
     */
    public long[] getVibrationPattern() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _oreoNotificationChannel.getVibrationPattern();
        }
        return mVibration;
    }

    /**
     * Sets the vibration pattern for notifications posted to this channel. If the provided
     * pattern is valid (non-null, non-empty), will {@link #enableVibration(boolean)} enable
     * vibration} as well. Otherwise, vibration will be disabled.
     * <p>
     * Only modifiable before the channel is submitted to
     * {NotificationManager#createNotificationChannelCompat(NotificationChannelCompat)}.
     */
    public void setVibrationPattern(long[] vibrationPattern) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _oreoNotificationChannel.setVibrationPattern(vibrationPattern);
            return;
        }
        this.mVibrationEnabled = vibrationPattern != null && vibrationPattern.length > 0;
        this.mVibration = vibrationPattern;
    }

    /**
     * Returns whether or not notifications posted to this channel are shown on the lockscreen in
     * full or redacted form.
     */
    public int getLockscreenVisibility() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _oreoNotificationChannel.getLockscreenVisibility();
        }
        return mLockscreenVisibility;
    }

    /**
     * Sets whether notifications posted to this channel appear on the lockscreen or not, and if so,
     * whether they appear in a redacted form. See e.g. {@link Notification#VISIBILITY_SECRET}.
     * <p>
     * Only modifiable by the system and notification ranker.
     */
    public void setLockscreenVisibility(int lockscreenVisibility) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _oreoNotificationChannel.setLockscreenVisibility(lockscreenVisibility);
            return;
        }
        this.mLockscreenVisibility = lockscreenVisibility;
    }

    /**
     * Returns whether notifications posted to this channel can appear as badges in a Launcher
     * application.
     * <p>
     * Note that badging may be disabled for other reasons.
     */
    public boolean canShowBadge() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _oreoNotificationChannel.canShowBadge();
        }
        return false; // not supported
    }

    /**
     * Returns what group this channel belongs to.
     * <p>
     * This is used only for visually grouping channels in the UI.
     */
    public String getGroup() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _oreoNotificationChannel.getGroup();
        }
        return mGroup;
    }

    /**
     * Sets what group this channel belongs to.
     * <p>
     * Group information is only used for presentation, not for behavior.
     * <p>
     * Only modifiable before the channel is submitted to
     * { NotificationManagerCompat#createNotificationChannel(NotificationChannelCompat)}, unless the
     * channel is not currently part of a group.
     *
     * @param groupId the id of a group created by
     *                {NotificationManager#createNotificationChannelCompatGroup(NotificationChannelCompatGroup)}.
     */
    public void setGroup(String groupId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _oreoNotificationChannel.setGroup(groupId);
            return;
        }
        this.mGroup = groupId;
    }

    @Override
    public int describeContents() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _oreoNotificationChannel.describeContents();
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationChannelCompat that = (NotificationChannelCompat) o;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _oreoNotificationChannel.equals(that._oreoNotificationChannel);
        }

        if (getImportance() != that.getImportance()) return false;
        if (getLockscreenVisibility() != that.getLockscreenVisibility()) return false;
        if (mLights != that.mLights) return false;
        if (getLightColor() != that.getLightColor()) return false;
        if (mVibrationEnabled != that.mVibrationEnabled) return false;
        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) {
            return false;
        }
        if (getDescription() != null ? !getDescription().equals(that.getDescription())
                : that.getDescription() != null) {
            return false;
        }
        if (getSound() != null ? !getSound().equals(that.getSound()) : that.getSound() != null) {
            return false;
        }
        if (!Arrays.equals(mVibration, that.mVibration)) return false;
        if (getGroup() != null ? !getGroup().equals(that.getGroup()) : that.getGroup() != null) {
            return false;
        }
        return getAudioAttributes() != null ? getAudioAttributes().equals(that.getAudioAttributes())
                : that.getAudioAttributes() == null;
    }

    @Override
    public int hashCode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _oreoNotificationChannel.hashCode();
        }
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + getImportance();
        result = 31 * result + getLockscreenVisibility();
        result = 31 * result + (getSound() != null ? getSound().hashCode() : 0);
        result = 31 * result + (mLights ? 1 : 0);
        result = 31 * result + getLightColor();
        result = 31 * result + Arrays.hashCode(mVibration);
        result = 31 * result + (mVibrationEnabled ? 1 : 0);
        result = 31 * result + (getGroup() != null ? getGroup().hashCode() : 0);
        result = 31 * result + (getAudioAttributes() != null ? getAudioAttributes().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _oreoNotificationChannel.toString();
        }
        return "NotificationChannelCompat{"
                + "mId='" + mId + '\''
                + ", mName=" + mName
                + ", mDescription=" + (!TextUtils.isEmpty(mDesc) ? "hasDescription " : "")
                + ", mImportance=" + mImportance
                + ", mLockscreenVisibility=" + mLockscreenVisibility
                + ", mSound=" + mSound
                + ", mLights=" + mLights
                + ", mLightColor=" + mLightColor
                + ", mVibration=" + Arrays.toString(mVibration)
                + ", mVibrationEnabled=" + mVibrationEnabled
                + ", mGroup='" + mGroup + '\''
                + ", mAudioAttributes=" + mAudioAttributes
                + ", mAudioStreamType=" + mAudioStreamType
                + '}';
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    NotificationChannel getOreoVersion() {
        return _oreoNotificationChannel;
    }

}