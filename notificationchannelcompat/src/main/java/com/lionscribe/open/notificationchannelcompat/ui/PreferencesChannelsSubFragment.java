/*
 * Copyright (C) 2019 Lionscribe Software LLC
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
 */

package com.lionscribe.open.notificationchannelcompat.ui;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.view.ContextThemeWrapper;
import android.text.TextUtils;

import com.lionscribe.open.notificationchannelcompat.NotificationChannelCompat;
import com.lionscribe.open.notificationchannelcompat.NotificationChannelManagerHelper;
import com.lionscribe.open.notificationchannelcompat.R;
import com.lionscribe.open.notificationchannelcompat.ui.widgets.SoundTonePreference;

public class PreferencesChannelsSubFragment extends PreferenceFragmentCompat {
    static final String FRAGMENT_TAG = "ChannelsSubFragment";
    NotificationChannelCompat _channel;

    SoundTonePreference _soundTonePreference;
    PreferenceCategory _preferenceDetailsCategory;

    public static PreferencesChannelsSubFragment newInstance(String channelId) {
        PreferencesChannelsSubFragment myFragment = new PreferencesChannelsSubFragment();

        Bundle args = new Bundle();
        args.putString("channelId", channelId);
        myFragment.setArguments(args);
        return myFragment;
    }


    @Override
    public void onCreatePreferences(Bundle savedInstance, String rootPreferenceKey) {
        Context activityContext = getActivity();

        final String channelId = getArguments().getString("channelId", null);
        if (TextUtils.isEmpty(channelId))
            return;

        NotificationChannelManagerHelper notificationManagerHelper = new NotificationChannelManagerHelper(getContext(), (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE));
        _channel = notificationManagerHelper.getNotificationChannel(channelId);
        if (_channel == null)
            return;

        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(NotificationChannelManagerHelper.SHARED_PREFERENCE_NAME);
        PreferenceScreen preferenceScreen = preferenceManager.createPreferenceScreen(activityContext);
        setPreferenceScreen(preferenceScreen);

        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(activityContext, R.style.PreferenceThemeOverlay);

        // We instance each Preference using our ContextThemeWrapper object
        PreferenceCategory preferenceCategory = new PreferenceCategory(contextThemeWrapper);
        preferenceCategory.setTitle(_channel.getName());

        SwitchPreference showNotificationsCategory = new SwitchPreference(contextThemeWrapper);
        showNotificationsCategory.setTitle(R.string.app_notifications_switch_label);
        showNotificationsCategory.setKey(NotificationChannelManagerHelper.makeKey(NotificationChannelManagerHelper.PREF_KEY_CHANNEL_ENABLED, channelId));
        showNotificationsCategory.setDefaultValue(true);


        _preferenceDetailsCategory = new PreferenceCategory(contextThemeWrapper);

        ListPreference behaviourPreference;
        behaviourPreference = new ListPreference(contextThemeWrapper);
        behaviourPreference.setKey(NotificationChannelManagerHelper.makeKey(NotificationChannelManagerHelper.PREF_KEY_CHANNEL_IMPORTANCE, channelId));
        behaviourPreference.setTitle(R.string.notification_importance_title);
        behaviourPreference.setDialogTitle(R.string.notification_importance_title);
        behaviourPreference.setSummary("%1$s");
        behaviourPreference.setEntries(R.array.notification_importance_entries_array);
        behaviourPreference.setEntryValues(R.array.notification_importance_values_array);
        behaviourPreference.setValueIndex(1);
        behaviourPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                ListPreference listPreference = (ListPreference) preference;
                // We have to set it manually, rather than allowing OS with returning true, for then getEntry will still have old value
                listPreference.setValue(newValue.toString());
                preference.setSummary(listPreference.getEntry());
                return false;
            }
        });

        _soundTonePreference = new SoundTonePreference(contextThemeWrapper, this);
        _soundTonePreference.setDefaultValue(Settings.System.DEFAULT_NOTIFICATION_URI.toString());
        _soundTonePreference.setKey(NotificationChannelManagerHelper.makeKey(NotificationChannelManagerHelper.PREF_KEY_CHANNEL_SOUND, channelId));
        _soundTonePreference.setTitle(R.string.notification_channel_sound_title);


        String vs = Context.VIBRATOR_SERVICE;
        Vibrator mVibrator = (Vibrator) getActivity().getSystemService(vs);
        boolean isVibrator = mVibrator.hasVibrator();

        SwitchPreference vibrateSwitch = null;
        if (isVibrator) {
            vibrateSwitch = new SwitchPreference(contextThemeWrapper);
            vibrateSwitch.setTitle(R.string.notification_vibrate_title);
            vibrateSwitch.setKey(NotificationChannelManagerHelper.makeKey(NotificationChannelManagerHelper.PREF_KEY_CHANNEL_VIBRATIONENABLED, channelId));
            vibrateSwitch.setDefaultValue(true);
        }

        SwitchPreference lightsSwitch = null;
        if (canPulseLight()) {
            lightsSwitch = new SwitchPreference(contextThemeWrapper);
            lightsSwitch.setTitle(R.string.notification_show_lights_title);
            // lightsSwitch.setSummary("May not be supported on this device");
            lightsSwitch.setKey(NotificationChannelManagerHelper.makeKey(NotificationChannelManagerHelper.PREF_KEY_CHANNEL_LIGHTS, channelId));
            lightsSwitch.setDefaultValue(true);
        }

        getPreferenceScreen().addPreference(preferenceCategory);
        preferenceCategory.addPreference(showNotificationsCategory);

        getPreferenceScreen().addPreference(_preferenceDetailsCategory);
        _preferenceDetailsCategory.addPreference(behaviourPreference);
        _preferenceDetailsCategory.addPreference(_soundTonePreference);
        if (vibrateSwitch != null)
            _preferenceDetailsCategory.addPreference(vibrateSwitch);
        if (lightsSwitch != null)
            _preferenceDetailsCategory.addPreference(lightsSwitch);

        // Must be called after preferences are added to screen
        _preferenceDetailsCategory.setDependency(NotificationChannelManagerHelper.makeKey(NotificationChannelManagerHelper.PREF_KEY_CHANNEL_ENABLED, channelId));

        PreferenceCategory infoCategory = new PreferenceCategory(contextThemeWrapper);
        infoCategory.setEnabled(false);
        Preference info = new Preference(contextThemeWrapper);
        info.setTitle(_channel.getDescription());
        info.setIcon(R.drawable.ic_info_disabled);
        info.setEnabled(false);

        getPreferenceScreen().addPreference(infoCategory);
        infoCategory.addPreference(info);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SoundTonePreference.REQUEST_CODE_ALERT_RINGTONE) {
            _soundTonePreference.handleActivityResult(data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    boolean canPulseLight() {
        boolean bCanPulse = false;
        int id = Resources.getSystem().getIdentifier("config_intrusiveNotificationLed", "bool", "android");
        if (id > 0) {
            Resources res = Resources.getSystem();
            try {
                bCanPulse = res.getBoolean(id);
            } catch (Resources.NotFoundException e) {
            }
        }
        if (bCanPulse) {
            try {
                final String NOTIFICATION_LIGHT_PULSE = "notification_light_pulse"; // val of Settings.System.NOTIFICATION_LIGHT_PULSE
                bCanPulse = Settings.System.getInt(getContext().getContentResolver(), NOTIFICATION_LIGHT_PULSE, 0) == 1;
            } catch (Exception e) {
                bCanPulse = false;
            }
        }
        return bCanPulse;
    }
}



