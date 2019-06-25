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
import android.os.Bundle;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.view.ContextThemeWrapper;

import com.lionscribe.open.notificationchannelcompat.NotificationChannelCompat;
import com.lionscribe.open.notificationchannelcompat.NotificationChannelGroupCompat;
import com.lionscribe.open.notificationchannelcompat.NotificationChannelManagerHelper;
import com.lionscribe.open.notificationchannelcompat.R;
import com.lionscribe.open.notificationchannelcompat.ui.widgets.MasterCheckBoxPreference;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PreferencesChannelsMainFragment extends PreferenceFragmentCompat {

    MasterCheckBoxPreference _openedChannelPref;

    @Override
    public void onCreatePreferences(Bundle savedInstance, String rootPreferenceKey) {
        Context activityContext = getActivity();

        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(NotificationChannelManagerHelper.SHARED_PREFERENCE_NAME);
        PreferenceScreen preferenceScreen = preferenceManager.createPreferenceScreen(activityContext);
        setPreferenceScreen(preferenceScreen);

        ContextThemeWrapper contextThemeWrapper = new ContextThemeWrapper(activityContext, R.style.PreferenceThemeOverlay);

        // We instance each Preference using our ContextThemeWrapper object
        PreferenceCategory preferenceCategory = new PreferenceCategory(contextThemeWrapper);
        getPreferenceScreen().addPreference(preferenceCategory);

        SwitchPreference showNotificationsAll = new SwitchPreference(contextThemeWrapper);
        showNotificationsAll.setTitle(R.string.app_notifications_switch_label);
        showNotificationsAll.setKey(NotificationChannelManagerHelper.PREF_KEY_CHANNELS_ALL_ENABLED);
        showNotificationsAll.setDefaultValue(true);

        preferenceCategory.addPreference(showNotificationsAll);

        NotificationChannelManagerHelper notificationManagerHelper = new NotificationChannelManagerHelper(getContext(), (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE));

        List<NotificationChannelGroupCompat> groups = notificationManagerHelper.getNotificationChannelGroups();
        Collections.sort(groups, new Comparator<NotificationChannelGroupCompat>() {
            public int compare(NotificationChannelGroupCompat obj1, NotificationChannelGroupCompat obj2) {
                return obj1.getName().toString().compareToIgnoreCase(obj2.getName().toString()); // To compare string values
            }
        });

        List<NotificationChannelCompat> channels = notificationManagerHelper.getNotificationChannels();
        Collections.sort(channels, new Comparator<NotificationChannelCompat>() {
            public int compare(NotificationChannelCompat obj1, NotificationChannelCompat obj2) {
                return obj1.getName().toString().compareToIgnoreCase(obj2.getName().toString()); // To compare string values
            }
        });

        for (int g = 0; g <= groups.size(); ++g) {
            NotificationChannelGroupCompat group = g < groups.size() ? groups.get(g) : null;
            PreferenceCategory preferenceGroupCategory = null;
            for (NotificationChannelCompat channel : channels) {
                if (group == null) {
                    if (channel.getGroup() != null)
                        continue;
                } else if (!group.getId().equals(channel.getGroup())) {
                    continue;
                }
                // There is a matching channel, so create preference category for it
                if (preferenceGroupCategory == null) {
                    preferenceGroupCategory = new PreferenceCategory(contextThemeWrapper);
                    preferenceGroupCategory.setTitle(group == null ? getString(R.string.notification_channels_other) : group.getName());
                    getPreferenceScreen().addPreference(preferenceGroupCategory);
                    preferenceGroupCategory.setDependency(NotificationChannelManagerHelper.PREF_KEY_CHANNELS_ALL_ENABLED);

                    if (group != null) {
                        SwitchPreference showNotificationsGroup = new SwitchPreference(contextThemeWrapper);
                        showNotificationsGroup.setTitle(R.string.app_notifications_switch_label);
                        showNotificationsGroup.setKey(NotificationChannelManagerHelper.makeKey(NotificationChannelManagerHelper.PREF_KEY_GROUP_ENABLED, group.getId()));
                        showNotificationsGroup.setDefaultValue(true);
                        preferenceGroupCategory.addPreference(showNotificationsGroup);
                    }
                }

                final String channelId = channel.getId();
                MasterCheckBoxPreference channelPref = new MasterCheckBoxPreference(contextThemeWrapper);
                channelPref.setTitle(channel.getName());
                channelPref.setKey(NotificationChannelManagerHelper.makeKey(NotificationChannelManagerHelper.PREF_KEY_CHANNEL_ENABLED, channelId));
                channelPref.setDefaultValue(true);

                channelPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        getActivity().getSupportFragmentManager()
                                .beginTransaction()
                                //.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim. slide_out_right, android.R.anim.slide_in_left, android.R.anim.slide_out_right)
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .replace(R.id.settings_container, PreferencesChannelsSubFragment.newInstance(channelId), PreferencesChannelsSubFragment.FRAGMENT_TAG)
                                .addToBackStack(null)
                                .commit();
                        _openedChannelPref = (MasterCheckBoxPreference) preference;
                        return true;
                    }
                });
                preferenceGroupCategory.addPreference(channelPref);
                if (group != null)
                    channelPref.setDependency(NotificationChannelManagerHelper.makeKey(NotificationChannelManagerHelper.PREF_KEY_GROUP_ENABLED, group.getId()));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (_openedChannelPref != null) {
            _openedChannelPref.setChecked(getPreferenceManager().getSharedPreferences().getBoolean(_openedChannelPref.getKey(), true));
            _openedChannelPref = null;
        }
    }
}



