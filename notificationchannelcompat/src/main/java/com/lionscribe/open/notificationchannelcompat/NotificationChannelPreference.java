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

package com.lionscribe.open.notificationchannelcompat;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.preference.Preference;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.lionscribe.open.notificationchannelcompat.ui.PreferencesChannelsActivity;

public class NotificationChannelPreference extends Preference {
    String _channelId;

    public NotificationChannelPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public NotificationChannelPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public NotificationChannelPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public NotificationChannelPreference(Context context) {
        super(context);
    }

    public static void launchSettings(Context context) {
        launchSettings(context, null);
    }

    public static void launchSettings(Context context, String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            launchSystemNotificationsSettings(context, channelId);
        } else {
            Intent i = new Intent(context, PreferencesChannelsActivity.class);
            if (!TextUtils.isEmpty(channelId))
                i.putExtra(PreferencesChannelsActivity.INTENT_EXTRA_CHANNEL_ID, channelId);
            context.startActivity(i);
			/*
			getActivity().getSupportFragmentManager()
					.beginTransaction()
					.add(R.id.settings_container, new PreferencesChannelsScreen())
					.addToBackStack(null)
					.commit();
			*/
        }

    }

    // Pass channel=null to see all channels
    public static boolean launchSystemNotificationsSettings(Context context, String channelId) {
        Intent intent = new Intent();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (channelId == null) {
                intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
            } else {
                intent.setAction(Settings.ACTION_CHANNEL_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_CHANNEL_ID, channelId);
            }
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
            intent.putExtra("app_package", context.getPackageName());
            intent.putExtra("app_uid", context.getApplicationInfo().uid);
        } else {
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
        }

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NotificationChannelPreference);
        _channelId = a.getString(R.styleable.NotificationChannelPreference_channelId);
        a.recycle();

    }

    @Override
    protected void onClick() {
        super.onClick();
        launchSettings(getContext(), _channelId);
    }
}
