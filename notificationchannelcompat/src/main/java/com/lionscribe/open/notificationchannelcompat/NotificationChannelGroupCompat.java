/*
 * Copyright (C) 2017 The Android Open Source Project
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
import android.content.Intent;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * A grouping of related notification channels. e.g., channels that all belong to a single account.
 */
public final class NotificationChannelGroupCompat implements Parcelable {
    public static final Creator<NotificationChannelGroupCompat> CREATOR =
            new Creator<NotificationChannelGroupCompat>() {
                @Override
                public NotificationChannelGroupCompat createFromParcel(Parcel in) {
                    return new NotificationChannelGroupCompat(in);
                }

                @Override
                public NotificationChannelGroupCompat[] newArray(int size) {
                    return new NotificationChannelGroupCompat[size];
                }
            };
    /**
     * The maximum length for text fields in a NotificationChannelGroup. Fields will be truncated at
     * this limit.
     */
    private static final int MAX_TEXT_LENGTH = 1000;
    private final String mId;
    private CharSequence mName;
    private String mDescription;
    private boolean mEnabled;
    private List<NotificationChannelCompat> mChannels = new ArrayList<>();
    private NotificationChannelGroup _notificationChannelGroup;

    /**
     * Creates a notification channel group.
     *
     * @param id   The id of the group. Must be unique per package.  the value may be truncated if
     *             it is too long.
     * @param name The user visible name of the group. You can rename this group when the system
     *             locale changes by listening for the {@link Intent#ACTION_LOCALE_CHANGED}
     *             broadcast. <p>The recommended maximum length is 40 characters; the value may be
     *             truncated if it is too long.
     */
    public NotificationChannelGroupCompat(String id, CharSequence name) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _notificationChannelGroup = new NotificationChannelGroup(id, name);
            mId = id; // make compiler happy
            return;
        }
        this.mId = getTrimmedString(id);
        this.mName = name != null ? getTrimmedString(name.toString()) : null;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    protected NotificationChannelGroupCompat(NotificationChannelGroup original) {
        _notificationChannelGroup = original;
        this.mId = _notificationChannelGroup.getId(); // just to make compiler happy
    }

    protected NotificationChannelGroupCompat(Parcel in) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _notificationChannelGroup = in.readParcelable(NotificationChannelCompat.class.getClassLoader());
            mId = getId(); // make compiler happy
            return;
        }
        if (in.readByte() != 0) {
            mId = in.readString();
        } else {
            mId = null;
        }
        mName = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        if (in.readByte() != 0) {
            mDescription = in.readString();
        } else {
            mDescription = null;
        }
        in.readTypedList(mChannels, NotificationChannelCompat.CREATOR);
        mEnabled = in.readByte() != 0;
    }

    private String getTrimmedString(String input) {
        if (input != null && input.length() > MAX_TEXT_LENGTH) {
            return input.substring(0, MAX_TEXT_LENGTH);
        }
        return input;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            _notificationChannelGroup.writeToParcel(dest, flags);
            return;
        }
        if (mId != null) {
            dest.writeByte((byte) 1);
            dest.writeString(mId);
        } else {
            dest.writeByte((byte) 0);
        }
        TextUtils.writeToParcel(mName, dest, flags);
        if (mDescription != null) {
            dest.writeByte((byte) 1);
            dest.writeString(mDescription);
        } else {
            dest.writeByte((byte) 0);
        }
        dest.writeTypedList(mChannels);
        dest.writeByte((byte) (mEnabled ? -1 : 0));
    }

    /**
     * Returns the id of this group.
     */
    public String getId() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _notificationChannelGroup.getId();
        }
        return mId;
    }

    /**
     * Returns the user visible name of this group.
     */
    public CharSequence getName() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _notificationChannelGroup.getName();
        }
        return mName;
    }

    /**
     * Returns the user visible description of this group.
     */
    public String getDescription() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return _notificationChannelGroup.getDescription();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Not supported
            return null;
        }
        return mDescription;
    }

    /**
     * Sets the user visible description of this group.
     *
     * <p>The recommended maximum length is 300 characters; the value may be truncated if it is too
     * long.
     */
    public void setDescription(String description) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            _notificationChannelGroup.setDescription(description);
            return;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // not supported
            return;
        }
        mDescription = getTrimmedString(description);
    }

    /**
     * Returns the list of channels that belong to this group
     */
    public List<NotificationChannelCompat> getChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            List<NotificationChannel> originals = _notificationChannelGroup.getChannels();
            List<NotificationChannelCompat> channels = new ArrayList<>(originals.size());
            for (NotificationChannel origin : originals)
                channels.add(new NotificationChannelCompat(origin));
            return channels;
        }
        return mChannels;
    }

    private void setChannels(List<NotificationChannelCompat> channels) {
        mChannels = channels;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public List<NotificationChannel> getChannelsOreo() {
        return _notificationChannelGroup.getChannels();
    }

    /**
     * Returns whether or not notifications posted to {@link NotificationChannel channels} belonging
     * to this group are blocked. This value is independent of
     * {NotificationManager#areNotificationsEnabled()} and
     * {@link NotificationChannel#getImportance()}.
     */
    public boolean isBlocked() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return _notificationChannelGroup.isBlocked();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return false; // not an option
        }
        return !mEnabled;
    }

    /**
     * Returns whether or not notifications posted to {@link NotificationChannel channels} belonging
     * to this group are enabled. This value is independent of
     * {NotificationManager#areNotificationsEnabled()} and
     * {@link NotificationChannel#getImportance()}.
     */
    public boolean isEnabled() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return !_notificationChannelGroup.isBlocked();
        }
        return mEnabled;
    }

    protected void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    @Override
    public int describeContents() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _notificationChannelGroup.describeContents();
        }
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationChannelGroupCompat that = (NotificationChannelGroupCompat) o;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _notificationChannelGroup.equals(that._notificationChannelGroup);
        }
        if (isBlocked() != that.isBlocked()) return false;
        if (getId() != null ? !getId().equals(that.getId()) : that.getId() != null) return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) {
            return false;
        }
        if (getDescription() != null ? !getDescription().equals(that.getDescription())
                : that.getDescription() != null) {
            return false;
        }
        return getChannels() != null ? getChannels().equals(that.getChannels())
                : that.getChannels() == null;
    }

    @Override
    public int hashCode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _notificationChannelGroup.hashCode();
        }
        int result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + (isBlocked() ? 1 : 0);
        result = 31 * result + (getChannels() != null ? getChannels().hashCode() : 0);
        return result;
    }

    @Override
    public NotificationChannelGroupCompat clone() {
        NotificationChannelGroupCompat cloned = new NotificationChannelGroupCompat(getId(), getName());
        cloned.setDescription(getDescription());
        cloned.setEnabled(isEnabled());
        cloned.setChannels(getChannels());
        return cloned;
    }

    @Override
    public String toString() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return _notificationChannelGroup.toString();
        }
        return "NotificationChannelGroup{"
                + "mId='" + mId + '\''
                + ", mName=" + mName
                + ", mDescription=" + (!TextUtils.isEmpty(mDescription) ? "hasDescription " : "")
                + ", mBlocked=" + !mEnabled
                + ", mChannels=" + mChannels
                + '}';
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    NotificationChannelGroup getOreoVersion() {
        return _notificationChannelGroup;
    }
}