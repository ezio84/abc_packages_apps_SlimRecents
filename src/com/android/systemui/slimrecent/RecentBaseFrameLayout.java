/*
 * Copyright (C) 2018 Android Ice Cold Project
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

package com.android.systemui.slimrecent;

import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_BACK;

import android.content.Context;
import android.view.KeyEvent;
import android.widget.FrameLayout;

class RecentBaseFrameLayout extends FrameLayout {

    private RecentController mRecentController;

    public RecentBaseFrameLayout(Context context, RecentController recentController) {
        super(context);
        mRecentController = recentController;
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getKeyCode() == KEYCODE_BACK && event.getAction() == ACTION_DOWN) {
            mRecentController.onExit();
        }
        return super.dispatchKeyEventPreIme(event);
    }
}
