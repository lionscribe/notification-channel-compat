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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.view.ContextThemeWrapper;

import com.lionscribe.open.notificationchannelcompat.NotificationChannelManagerHelper;
import com.lionscribe.open.notificationchannelcompat.NotificationChannelPreference;
import com.lionscribe.open.notificationchannelcompat.R;

public class PreferencesChannelsSystemFragment extends PreferenceFragmentCompat {

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

        Preference systemPref = new Preference(contextThemeWrapper);
        systemPref.setTitle(R.string.notification_importance_blocked);
        systemPref.setSummary(R.string.write_settings);
        systemPref.setLayoutResource(R.layout.preference_goto_target);
        systemPref.setIcon(R.drawable.ic_error_outline);
        systemPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                NotificationChannelPreference.launchSystemNotificationsSettings(getContext(), null);
                return true;
            }
        });
        preferenceCategory.addPreference(systemPref);
    }

    @Override
    public void onResume() {
        super.onResume();
        NotificationManagerCompat manager = NotificationManagerCompat.from(getContext());
        if (manager.areNotificationsEnabled()) // user must have enabled it, so go to main screen
        {
            ((PreferencesChannelsActivity) getActivity()).loadFragment(); // will now load main or sub fragment based on origina intent
        }
    }
}



