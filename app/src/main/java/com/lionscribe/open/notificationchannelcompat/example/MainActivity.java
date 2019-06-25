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

package com.lionscribe.open.notificationchannelcompat.example;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.lionscribe.open.notificationchannelcompat.NotificationChannelCompat;
import com.lionscribe.open.notificationchannelcompat.NotificationChannelGroupCompat;
import com.lionscribe.open.notificationchannelcompat.NotificationChannelManagerHelper;
import com.lionscribe.open.notificationchannelcompat.NotificationChannelPreference;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    final static String[] CHANNEL_IDS = {"channel_one_home", "channel_two_home", "channel_three", "channel_one_work", "channel_two_work"};
    final static String[] CHANNEL_NAMES = {"Channel One", "Channel Two", "Channel Three", "Channel One", "Channel Two"};
    final static String[] CHANNEL_DESCRIPTIONS = {"Channel One Home", "Channel Two Home", "Channel Three", "Channel One Work", "Channel Two Work"};
    final static int[] CHANNEL_GROUPS = {0, 0, -1, 1, 1}; // Channel Three is in no group

    final static String[] GROUP_IDS = {"group_home", "group_work"};
    final static String[] GROUP_NAMES = {"Home", "Work"};
    final static String[] GROUP_DESCRIPTIONS = {"Group Home", "Group Work"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initChannels();
        Spinner spinner = findViewById(R.id.channels_spinner);
        ArrayAdapter adapter = new ArrayAdapter<CharSequence>(this, android.R.layout.simple_spinner_item, CHANNEL_DESCRIPTIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @Override
    public void onClick(View view) {
        int viewId = view.getId();
        if (viewId == R.id.notif_button) {
            Spinner s = findViewById(R.id.channels_spinner);
            int channelId = s.getSelectedItemPosition();
            if (channelId >= 0 && channelId < CHANNEL_IDS.length)
                doNotify(channelId);
        } else if (viewId == R.id.settings_button) {
            Intent i = new Intent(this, SettingsActivity.class);
            startActivity(i);
        } else if (viewId == R.id.settingsdirect_button) {
            NotificationChannelPreference.launchSettings(this);
        } else if (viewId == R.id.channeldirect_button) {
            Spinner s = findViewById(R.id.channels_spinner);
            int channelId = s.getSelectedItemPosition();
            if (channelId >= 0 && channelId < CHANNEL_IDS.length)
                NotificationChannelPreference.launchSettings(this, CHANNEL_IDS[channelId]);
        }

    }

    private void initChannels() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannelManagerHelper notificationManagerHelper = new NotificationChannelManagerHelper(this, notificationManager);

        for (int i = 0; i < GROUP_IDS.length; ++i) {
            NotificationChannelGroupCompat group = new NotificationChannelGroupCompat(GROUP_IDS[i], GROUP_NAMES[i]);
            group.setDescription(GROUP_DESCRIPTIONS[i]);
            notificationManagerHelper.createNotificationChannelGroup(group);
        }

        for (int i = 0; i < CHANNEL_IDS.length; ++i) {
            CharSequence name = CHANNEL_NAMES[i];
            String description = CHANNEL_DESCRIPTIONS[i];
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannelCompat channel = new NotificationChannelCompat(CHANNEL_IDS[i], name, importance);
            channel.setDescription(description);
            if (CHANNEL_GROUPS[i] >= 0)
                channel.setGroup(GROUP_IDS[CHANNEL_GROUPS[i]]);
            // Register the channel with the system; you can't change the importance or other notification behaviors after this
            notificationManagerHelper.createNotificationChannel(channel);
        }

    }

    private void doNotify(int index) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_IDS[index])
                .setSmallIcon(R.drawable.ic_notifications_active_black_24dp)
                .setContentTitle(CHANNEL_NAMES[index])
                .setContentText(CHANNEL_DESCRIPTIONS[index])
                .build();

        if (NotificationChannelCompat.applyChannel(this, notification, CHANNEL_IDS[index])) // apply channel prefs, and chack return value, telling us if we should show notification
        {
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            notificationManager.notify(index, notification);
        }
    }

}
