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
 * 6/25/2019 Minor changes by Lionscribe as Compat to support older devices
 *
 */

package com.lionscribe.open.notificationchannelcompat.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;

import com.lionscribe.open.notificationchannelcompat.R;

import androidx.appcompat.widget.AppCompatImageView;

/**
 * Extension of ImageView that correctly applies maxWidth and maxHeight.
 */
public class PreferenceImageView extends AppCompatImageView {
    public PreferenceImageView(Context context) {
        super(context);
    }

    public PreferenceImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PreferenceImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            final int maxWidth = getResources().getDimensionPixelSize(R.dimen.preference_image_max_width); //  getMaxWidth() needs sdk 16
            if (maxWidth != Integer.MAX_VALUE
                    && (maxWidth < widthSize || widthMode == MeasureSpec.UNSPECIFIED)) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST);
            }
        }
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            final int maxHeight = getResources().getDimensionPixelSize(R.dimen.preference_image_max_height);// getMaxHeight() needs sdk 16
            if (maxHeight != Integer.MAX_VALUE
                    && (maxHeight < heightSize || heightMode == MeasureSpec.UNSPECIFIED)) {
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}