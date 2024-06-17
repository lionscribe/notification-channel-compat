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

package com.lionscribe.open.notificationchannelcompat.ui.widgets;

import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;

public class SoundTonePreference extends Preference implements Preference.OnPreferenceChangeListener {
    public static final int REQUEST_CODE_ALERT_RINGTONE = 1371;
    private final Fragment mParent;
    private String mValue;
    private boolean mValueSet;

    public SoundTonePreference(Context context, Fragment parent) {
        super(context);
        super.setPersistent(true);
        mParent = parent;
        updateSummary(getValue());
        setOnPreferenceChangeListener(this);
    }

    @Override
    protected void onClick() {
        super.onClick();
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "Select Sound");
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, Settings.System.DEFAULT_NOTIFICATION_URI);

        String existingValue = getValue();
        if (existingValue != null) {
            if (existingValue.length() == 0) {
                // Select "Silent"
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, (Uri) null);
            } else {
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri.parse(existingValue));
            }
        } else {
            // No ringtone has been selected, set to the default
            intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Settings.System.DEFAULT_NOTIFICATION_URI);
        }
        mParent.startActivityForResult(intent, REQUEST_CODE_ALERT_RINGTONE);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        updateSummary((String) o);
        return true;
    }

    public void handleActivityResult(Intent data) {
        if (data != null) {
            Uri ringtone = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
            setValue(ringtone.toString());
            updateSummary(getValue());
        }
    }

    void updateSummary(String ringtone) {
        setSummary(ringtone == null ? "" : getToneTitle(Uri.parse(ringtone)));
    }

    /**
     * Returns the value of the key.
     *
     * @return The value of the key.
     */
    public String getValue() {
        return mValue;
    }

    /**
     * Sets the value of the key.
     *
     * @param value The value to set for the key.
     */
    public void setValue(String value) {
        // Always persist/notify the first time.
        final boolean changed = !TextUtils.equals(mValue, value);
        if (changed || !mValueSet) {
            mValue = value;
            mValueSet = true;
            persistString(value);
            if (changed) {
                notifyChanged();
            }
        }
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        setValue(getPersistedString((String) defaultValue));
        updateSummary(getValue());
    }

    private String getToneTitle(Uri ringtone) {
        String title = "";
        if (ringtone != null) {
            Ringtone tone = RingtoneManager.getRingtone(getContext(), ringtone);
            if (tone != null) {
                title = tone.getTitle(getContext());
            }
        }
        return title;
    }

}
