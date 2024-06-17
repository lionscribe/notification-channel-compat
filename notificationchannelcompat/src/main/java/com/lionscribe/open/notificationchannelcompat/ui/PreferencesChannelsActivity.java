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

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;

import com.lionscribe.open.notificationchannelcompat.R;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceFragmentCompat;

public class PreferencesChannelsActivity extends AppCompatActivity {
    public static final String INTENT_EXTRA_CHANNEL_ID = "INTENT_EXTRA_CHANNEL_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        if (getSupportActionBar() != null)
            getSupportActionBar().hide();
        setContentView(R.layout.activity_preferences_channel);
        if (savedInstanceState == null) {
            loadFragment();
        }
    }

    protected void loadFragment() {
        String channelId = getIntent().getStringExtra(INTENT_EXTRA_CHANNEL_ID);

        PreferenceFragmentCompat frag;

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        if (!manager.areNotificationsEnabled())
            frag = new PreferencesChannelsSystemFragment();
        else
            frag = TextUtils.isEmpty(channelId) ? new PreferencesChannelsMainFragment() : PreferencesChannelsSubFragment.newInstance(channelId);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, frag)
                .commit();
    }
}
