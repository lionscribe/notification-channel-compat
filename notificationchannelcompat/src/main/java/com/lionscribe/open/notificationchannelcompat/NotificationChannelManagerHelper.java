/*
 * Copyright (C) 2014 The Android Open Source Project
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

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.util.ArraySet;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public final class NotificationChannelManagerHelper {
    public final static String SHARED_PREFERENCE_NAME = "com.lionscribe.open.notificationchannelcompat_channel_prefs";

    public final static String PREF_KEY_CHANNELS_ALL_ENABLED = "channels_all_enabled";


    public final static String PREF_KEY_CHANNELS_IDS = "channels_ids";

    public final static String PREF_KEY_CHANNEL_NAME = "channel_name_%s";
    public final static String PREF_KEY_CHANNEL_DESCRIPTION = "channel_description_%s";
    public final static String PREF_KEY_CHANNEL_ENABLED = "channel_enabled_%s";
    public final static String PREF_KEY_CHANNEL_IMPORTANCE = "channel_importance_%s";
    public final static String PREF_KEY_CHANNEL_LOCKSCREENVISIBILITY = "channel_lockscreenVisibility_%s";
    public final static String PREF_KEY_CHANNEL_SOUND = "channel_sound_%s";
    public final static String PREF_KEY_CHANNEL_LIGHTS = "channel_lights_%s";
    public final static String PREF_KEY_CHANNEL_LIGHTCOLOR = "channel_lightColor_%s";
    public final static String PREF_KEY_CHANNEL_VIBRATION = "channel_vibration_%s";
    public final static String PREF_KEY_CHANNEL_VIBRATIONENABLED = "channel_vibrationEnabled_%s";
    public final static String PREF_KEY_CHANNEL_GROUP = "channel_group_%s";
    public final static String PREF_KEY_CHANNEL_AUDIOATTRIBUTESCONTENTTYPE = "channel_audioAttributes_ContentType%s";
    public final static String PREF_KEY_CHANNEL_AUDIOATTRIBUTESFLAGS = "channel_audioAttributesFlags_%s";
    public final static String PREF_KEY_CHANNEL_AUDIOATTRIBUTESUSAGE = "channel_audioAttributesUsage_%s";
    public final static String PREF_KEY_CHANNEL_AUDIOSTREAMTYPE = "channel_audioStreamTYpe_%s";

    public final static String PREF_KEY_GROUPS_IDS = "groups_ids";

    public final static String PREF_KEY_GROUP_NAME = "group_name_%s";
    public final static String PREF_KEY_GROUP_DESCRIPTION = "group_description_%s";
    public final static String PREF_KEY_GROUP_ENABLED = "group_enabled_%s";
    final NotificationManager _manager;
    final SharedPreferences _prefs;
    Set<String> _channelIds;
    Set<String> _groupIds;

    public NotificationChannelManagerHelper(Context context, NotificationManager manager) {
        _manager = manager;
        _prefs = context.getSharedPreferences(SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE);
        _channelIds = _prefs.getStringSet(PREF_KEY_CHANNELS_IDS, null);
        _groupIds = _prefs.getStringSet(PREF_KEY_GROUPS_IDS, null);
        if (_channelIds == null)
            _channelIds = new ArraySet<>();
        if (_groupIds == null)
            _groupIds = new ArraySet<>();
    }

    public static String makeKey(String pref, String id) {
        return String.format(pref, id);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static List<NotificationChannelCompat> convertChannelListToCompat(List<NotificationChannel> originals) {
        List<NotificationChannelCompat> channels = new ArrayList<>(originals.size());
        for (NotificationChannel origin : originals)
            channels.add(new NotificationChannelCompat(origin));
        return channels;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static List<NotificationChannel> convertChannelCompatToList(List<NotificationChannelCompat> originals) {
        List<NotificationChannel> channels = new ArrayList<>(originals.size());
        for (NotificationChannelCompat origin : originals)
            channels.add(origin.getOreoVersion());
        return channels;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static List<NotificationChannelGroupCompat> convertGroupListToCompat(List<NotificationChannelGroup> originals) {
        List<NotificationChannelGroupCompat> channels = new ArrayList<>(originals.size());
        for (NotificationChannelGroup origin : originals)
            channels.add(new NotificationChannelGroupCompat(origin));
        return channels;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private static List<NotificationChannelGroup> convertGroupCompatToList(List<NotificationChannelGroupCompat> originals) {
        List<NotificationChannelGroup> channels = new ArrayList<>(originals.size());
        for (NotificationChannelGroupCompat origin : originals)
            channels.add(origin.getOreoVersion());
        return channels;
    }

    public boolean isNotificationsEnabled() {
        return _prefs.getBoolean(PREF_KEY_CHANNELS_ALL_ENABLED, true);
    }

    /**
     * Creates a group container for {@link NotificationChannel} objects.
     * <p>
     * This can be used to rename an existing group.
     * <p>
     * Group information is only used for presentation, not for behavior. Groups are optional
     * for channels, and you can have a mix of channels that belong to groups and channels
     * that do not.
     * </p>
     * <p>
     * For example, if your application supports multiple accounts, and those accounts will
     * have similar channels, you can create a group for each account with account specific
     * labels instead of appending account information to each channel's label.
     * </p>
     *
     * @param group The group to create
     */


    public void createNotificationChannelGroup(@NonNull NotificationChannelGroupCompat group) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _manager.createNotificationChannelGroup(group.getOreoVersion());
            return;
        }
        String groupId = group.getId();
        SharedPreferences.Editor editor = _prefs.edit();
        if (!_groupIds.contains(groupId)) {
            _groupIds.add(groupId);
            editor.putStringSet(PREF_KEY_GROUPS_IDS, _groupIds);
        }
        editor.putString(makeKey(PREF_KEY_GROUP_NAME, groupId), group.getName().toString());
        editor.putString(makeKey(PREF_KEY_GROUP_DESCRIPTION, groupId), group.getDescription());
        if (!_prefs.contains(makeKey(PREF_KEY_GROUP_ENABLED, groupId))) // we don't overwite this pref, even if there from before
            editor.putBoolean(makeKey(PREF_KEY_GROUP_ENABLED, groupId), group.isEnabled());
        editor.apply();
    }

    /**
     * Creates multiple notification channel groups.
     *
     * @param groups The list of groups to create
     */
    public void createNotificationChannelGroups(@NonNull List<NotificationChannelGroupCompat> groups) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _manager.createNotificationChannelGroups(convertGroupCompatToList(groups));
            return;
        }
        for (NotificationChannelGroupCompat g : groups)
            createNotificationChannelGroup(g);
    }

    /**
     * Creates a notification channel that notifications can be posted to.
     * <p>
     * This can also be used to restore a deleted channel and to update an existing channel's
     * name, description, group, and/or importance.
     *
     * <p>The name and description should only be changed if the locale changes
     * or in response to the user renaming this channel. For example, if a user has a channel
     * named 'John Doe' that represents messages from a 'John Doe', and 'John Doe' changes his name
     * to 'John Smith,' the channel can be renamed to match.
     *
     * <p>The importance of an existing channel will only be changed if the new importance is lower
     * than the current value and the user has not altered any settings on this channel.
     *
     * <p>The group an existing channel will only be changed if the channel does not already
     * belong to a group.
     * <p>
     * All other fields are ignored for channels that already exist.
     *
     * @param channel the channel to create.  Note that the created channel may differ from this
     *                value. If the provided channel is malformed, a RemoteException will be
     *                thrown.
     */
    public void createNotificationChannel(@NonNull NotificationChannelCompat channel) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _manager.createNotificationChannel(channel.getOreoVersion());
            return;
        }
        String channelId = channel.getId();
        if (_channelIds.contains(channelId)) {
            if (!channel.getName().equals(prefsGetString(PREF_KEY_CHANNEL_NAME, channelId, "Error"))) {
                _prefs.edit().putString(makeKey(PREF_KEY_CHANNEL_NAME, channelId), channel.getName().toString()).apply();
            }
            if (!channel.getDescription().equals(prefsGetString(PREF_KEY_CHANNEL_DESCRIPTION, channelId, "Error"))) {
                _prefs.edit().putString(makeKey(PREF_KEY_CHANNEL_DESCRIPTION, channelId), channel.getDescription()).apply();
            }
            // We do not change anything else once set
            return;
        }

        SharedPreferences.Editor editor = _prefs.edit();
        _channelIds.add(channelId);
        editor.putStringSet(PREF_KEY_CHANNELS_IDS, _channelIds);
        editor.putString(makeKey(PREF_KEY_CHANNEL_NAME, channelId), channel.getName().toString());
        editor.putBoolean(makeKey(PREF_KEY_CHANNEL_ENABLED, channelId), channel.isEnabled());
        editor.putString(makeKey(PREF_KEY_CHANNEL_IMPORTANCE, channelId), Integer.toString(channel.getImportance())); // we need it as string, as it's set as value in dropdown
        editor.putString(makeKey(PREF_KEY_CHANNEL_DESCRIPTION, channelId), channel.getDescription());
        editor.putString(makeKey(PREF_KEY_CHANNEL_GROUP, channelId), channel.getGroup());
        editor.putInt(makeKey(PREF_KEY_CHANNEL_LOCKSCREENVISIBILITY, channelId), channel.getLockscreenVisibility());
        editor.putBoolean(makeKey(PREF_KEY_CHANNEL_LIGHTS, channelId), channel.shouldShowLights());
        editor.putInt(makeKey(PREF_KEY_CHANNEL_LIGHTCOLOR, channelId), channel.getLightColor());
        editor.putString(makeKey(PREF_KEY_CHANNEL_SOUND, channelId), channel.getSound() == null ? null : channel.getSound().toString());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && _prefs.contains(makeKey(PREF_KEY_CHANNEL_AUDIOATTRIBUTESCONTENTTYPE, channelId))) {
            editor.putInt(makeKey(PREF_KEY_CHANNEL_AUDIOATTRIBUTESCONTENTTYPE, channelId), channel.getAudioAttributes().getContentType());
            editor.putInt(makeKey(PREF_KEY_CHANNEL_AUDIOATTRIBUTESFLAGS, channelId), channel.getAudioAttributes().getFlags());
            editor.putInt(makeKey(PREF_KEY_CHANNEL_AUDIOATTRIBUTESUSAGE, channelId), channel.getAudioAttributes().getUsage());
        }
        editor.putInt(makeKey(PREF_KEY_CHANNEL_AUDIOSTREAMTYPE, channelId), channel.getAudioStreamType());
        editor.putBoolean(makeKey(PREF_KEY_CHANNEL_VIBRATIONENABLED, channelId), channel.shouldVibrate());

        long[] vibrationPattern = channel.getVibrationPattern();
        String vibrationPatternStr = null;
        if (vibrationPattern != null) {
            StringBuilder vibrationPatternBldr = new StringBuilder(vibrationPattern.length * 4); // Guessing size
            for (long val : vibrationPattern) {
                vibrationPatternBldr.append(Long.toString(val)); // we are not using 16 byte hex for long, as comma separated will be smaller, due to small values
                vibrationPatternBldr.append(',');
            }
            vibrationPatternStr = vibrationPatternBldr.toString();
        }
        editor.putString(makeKey(PREF_KEY_CHANNEL_VIBRATION, channelId), vibrationPatternStr);

        editor.apply();
    }

    /**
     * Creates multiple notification channels that different notifications can be posted to. See
     * {#createNotificationChannel(NotificationChannel)}.
     *
     * @param channels the list of channels to attempt to create.
     */
    public void createNotificationChannels(@NonNull List<NotificationChannelCompat> channels) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _manager.createNotificationChannels(convertChannelCompatToList(channels));
            return;
        }
        for (NotificationChannelCompat c : channels)
            createNotificationChannel(c);

    }

    /**
     * Returns the notification channel settings for a given channel id.
     * <p>
     * The channel must belong to your package, or it will not be returned.
     */
    public NotificationChannelCompat getNotificationChannel(String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new NotificationChannelCompat(_manager.getNotificationChannel(channelId));

        }
        if (!_channelIds.contains(channelId))
            return null;

        int importance = Integer.parseInt(prefsGetString(PREF_KEY_CHANNEL_IMPORTANCE, channelId, Integer.toString(NotificationManagerCompat.IMPORTANCE_DEFAULT))); // as Pref is a string, we have to jump through loop to get is as int
        NotificationChannelCompat channel = new NotificationChannelCompat(channelId, prefsGetString(PREF_KEY_CHANNEL_NAME, channelId, "Error"), importance);
        channel.setDescription(prefsGetString(PREF_KEY_CHANNEL_DESCRIPTION, channelId, "None"));
        channel.setEnabled(prefsGetBoolean(PREF_KEY_CHANNEL_ENABLED, channelId, channel.isEnabled()));
        channel.setGroup(prefsGetString(PREF_KEY_CHANNEL_GROUP, channelId, null));
        channel.setLockscreenVisibility(prefsGetInt(PREF_KEY_CHANNEL_LOCKSCREENVISIBILITY, channelId, channel.getLockscreenVisibility()));
        channel.enableLights(prefsGetBoolean(PREF_KEY_CHANNEL_LIGHTS, channelId, channel.shouldShowLights()));
        channel.setLightColor(prefsGetInt(PREF_KEY_CHANNEL_LIGHTCOLOR, channelId, channel.getLightColor()));

        String soundUriStr = prefsGetString(PREF_KEY_CHANNEL_SOUND, channelId, null);
        Uri soundUri = TextUtils.isEmpty(soundUriStr) ? null : Uri.parse(soundUriStr);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && _prefs.contains(makeKey(PREF_KEY_CHANNEL_AUDIOATTRIBUTESCONTENTTYPE, channelId))) {
            AudioAttributes.Builder audioBuilder = new AudioAttributes.Builder();
            audioBuilder.setContentType(prefsGetInt(PREF_KEY_CHANNEL_AUDIOATTRIBUTESCONTENTTYPE, channelId, 0));
            audioBuilder.setFlags(prefsGetInt(PREF_KEY_CHANNEL_AUDIOATTRIBUTESFLAGS, channelId, 0));
            audioBuilder.setUsage(prefsGetInt(PREF_KEY_CHANNEL_AUDIOATTRIBUTESUSAGE, channelId, 0));

            channel.setSound(soundUri, audioBuilder.build());
        }
        channel.setSound(soundUri, prefsGetInt(PREF_KEY_CHANNEL_AUDIOSTREAMTYPE, channelId, channel.getAudioStreamType())); // support pre-lollypop

        channel.enableVibration(prefsGetBoolean(PREF_KEY_CHANNEL_VIBRATIONENABLED, channelId, channel.shouldVibrate()));
        String vibrationPatternStr = prefsGetString(PREF_KEY_CHANNEL_VIBRATION, channelId, null);
        if (vibrationPatternStr != null) {
            String[] vibrationPatternStrArray = vibrationPatternStr.split(",");
            int length = vibrationPatternStrArray.length;
            long[] vibrationPattern = new long[length];
            for (int i = 0; i < length; ++i) {
                vibrationPattern[i] = Long.parseLong(vibrationPatternStrArray[i]);
            }
            channel.setVibrationPattern(vibrationPattern);
        }

        return channel;
    }

    /**
     * Returns all notification channels belonging to the calling package.
     */
    public List<NotificationChannelCompat> getNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return convertChannelListToCompat(_manager.getNotificationChannels());
        }
        ArrayList<NotificationChannelCompat> list = new ArrayList<>(_channelIds.size());
        for (String channelId : _channelIds) {
            list.add(getNotificationChannel(channelId));
        }
        return list;
    }

    /**
     * Deletes the given notification channel.
     *
     * <p>If you {#createNotificationChannel(NotificationChannel) create} a new channel with
     * this same id, the deleted channel will be un-deleted with all of the same settings it
     * had before it was deleted.
     */
    public void deleteNotificationChannel(String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _manager.deleteNotificationChannel(channelId);
            return;
        }
        if (_channelIds.contains(channelId)) {
            _channelIds.remove(channelId);
            _prefs.edit().putStringSet(PREF_KEY_CHANNELS_IDS, _channelIds).apply();
            // We don't delete settings, as those will be reused if recreated
        }
    }

    /**
     * Returns the notification channel group settings for a given channel group id.
     * <p>
     * The channel group must belong to your package, or null will be returned.
     */
    public NotificationChannelGroupCompat getNotificationChannelGroup(String channelGroupId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return new NotificationChannelGroupCompat(_manager.getNotificationChannelGroup(channelGroupId));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            for (NotificationChannelGroup group : _manager.getNotificationChannelGroups()) {
                if (group.getId().equals(channelGroupId)) {
                    return new NotificationChannelGroupCompat(group);
                }
            }
            return null; // not found
        }

        if (!_groupIds.contains(channelGroupId))
            return null;

        NotificationChannelGroupCompat group = new NotificationChannelGroupCompat(channelGroupId, prefsGetString(PREF_KEY_GROUP_NAME, channelGroupId, "Error"));
        group.setEnabled(prefsGetBoolean(PREF_KEY_GROUP_ENABLED, channelGroupId, true));
        return group;
    }

    /**
     * Returns all notification channel groups belonging to the calling app.
     */
    public List<NotificationChannelGroupCompat> getNotificationChannelGroups() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return convertGroupListToCompat(_manager.getNotificationChannelGroups());

        }
        ArrayList<NotificationChannelGroupCompat> list = new ArrayList<>(_groupIds.size());
        for (String groupId : _groupIds) {
            list.add(getNotificationChannelGroup(groupId));
        }
        return list;
    }

    /**
     * Deletes the given notification channel group, and all notification channels that
     * belong to it.
     */
    public void deleteNotificationChannelGroup(String groupId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _manager.deleteNotificationChannelGroup(groupId);
            return;
        }
        if (_groupIds.contains(groupId)) {
            SharedPreferences.Editor editor = _prefs.edit();
            // first delete all channels that have this id
            for (String channelId : _channelIds) {
                if (_prefs.getString(makeKey(PREF_KEY_CHANNEL_GROUP, channelId), "").equals(groupId)) {
                    _channelIds.remove(channelId);
                }
            }
            _groupIds.remove(groupId);
            editor.putStringSet(PREF_KEY_GROUPS_IDS, _groupIds);
            editor.putStringSet(PREF_KEY_CHANNELS_IDS, _channelIds);
            editor.apply();
            // We don't delete any settings, as those will be reused if recreated
        }
    }

    // helper classes
    private String prefsGetString(String prefs, String id, String defaultValue) {
        return _prefs.getString(makeKey(prefs, id), defaultValue);
    }

    private boolean prefsGetBoolean(String prefs, String id, boolean defaultValue) {
        return _prefs.getBoolean(makeKey(prefs, id), defaultValue);
    }

    private int prefsGetInt(String prefs, String id, int defaultValue) {
        return _prefs.getInt(makeKey(prefs, id), defaultValue);
    }
}
