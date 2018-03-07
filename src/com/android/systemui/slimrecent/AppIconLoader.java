/*
 * Copyright (C) 2014-2017 SlimRoms Project
 * Author: Lars Greiss - email: kufikugel@googlemail.com
 * Copyright (C) 2017 ABC rom
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.systemui.slimrecent;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Process;

import com.android.systemui.R;
import com.android.systemui.slimrecent.ImageHelper;
import com.android.systemui.slimrecent.icons.IconsHandler;

import java.lang.ref.WeakReference;

/**
 * This class handles async app icon load for the requested apps
 * and put them when sucessfull into the LRU cache.
 *
 * Compared to the task screenshots this class is laid out due
 * that the #link:CacheController can request an app icon as well
 * eg if the app was updated and may changed the icon.
 */
public class AppIconLoader {

    /**
     * Singleton.
     */
    private static AppIconLoader sInstance;

    private Context mContext;

    public interface IconCallback {
        void onDrawableLoaded(Drawable drawable);
    }

    /**
     * Get the instance.
     */
    public static AppIconLoader getInstance(Context context) {
        if (sInstance != null) {
            return sInstance;
        } else {
            return sInstance = new AppIconLoader(context);
        }
    }

    /**
     * Constructor.
     */
    private AppIconLoader(Context context) {
        mContext = context;
    }

    /**
     * Load the app icon via async task.
     *
     * @params packageName
     * @params imageView
     */
    protected void loadAppIcon(ActivityInfo info, String identifier,
            IconCallback callback, float scaleFactor, int iconSizeId) {
        final BitmapDownloaderTask task =
                new BitmapDownloaderTask(callback, mContext, scaleFactor, iconSizeId, identifier);
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, info);
    }

    /**
     * AsyncTask loader for the app icon.
     */
    private static class BitmapDownloaderTask
            extends AsyncTask<ActivityInfo, Void, Drawable> {

        private Drawable mAppIcon;

        private IconCallback mCallback;
        private final WeakReference<Context> rContext;

        private float mScaleFactor;
        private int mIconSizeId;

        private String mLRUCacheKey;

        public BitmapDownloaderTask(IconCallback callback,
                Context context, float scaleFactor, int iconSizeId, String identifier) {
            mCallback = callback;
            rContext = new WeakReference<Context>(context);
            mScaleFactor = scaleFactor;
            mIconSizeId = iconSizeId;
            mLRUCacheKey = identifier;
        }

        @Override
        protected Drawable doInBackground(ActivityInfo... params) {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND + 1);
            if (isCancelled() || rContext == null) {
                return null;
            }
            // Load and return bitmap
            return getAppIcon(params[0], rContext.get(), mScaleFactor, mIconSizeId);
        }

        @Override
        protected void onPostExecute(Drawable bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }

            final Context context;
            if (rContext != null) {
                context = rContext.get();
            } else {
                context = null;
            }
            // Assign image to the view if the view was passed through.
            // #link:loadAppIcon
            if (mCallback != null) {
                mCallback.onDrawableLoaded(bitmap);
            }
            if (bitmap != null && context != null
                    && bitmap instanceof BitmapDrawable) {
                // Put our bitmap intu LRU cache for later use.
                CacheController.getInstance(context, null)
                        .addBitmapToMemoryCache(
                        mLRUCacheKey, (BitmapDrawable)bitmap);
            }
        }
    }

    /**
     * Loads the actual app icon.
     */
    private static Drawable getAppIcon(ActivityInfo info,
            Context context, float scaleFactor, int iconSizeId) {
        if (context == null) {
            return null;
        }
        return IconsHandler.getInstance(context).getIconFromHandler(context, info, scaleFactor, iconSizeId);

    }
}
