/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.systemui.statusbar.oswin.data;

import android.app.Dialog;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;

public abstract class LoaderManager {

    public interface LoaderCallbacks<D> {

        public Loader<D> onCreateLoader(int id, Bundle args);

        public void onLoadFinished(Loader<D> loader, D data);

        public void onLoaderReset(Loader<D> loader);
    }
    
    public abstract <D> Loader<D> initLoader(int id, Bundle args,
            LoaderManager.LoaderCallbacks<D> callback);

    public abstract <D> Loader<D> restartLoader(int id, Bundle args,
            LoaderManager.LoaderCallbacks<D> callback);

    public abstract void destroyLoader(int id);

    public abstract <D> Loader<D> getLoader(int id);

    public abstract void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args);

    public static void enableDebugLogging(boolean enabled) {
        LoaderManagerImpl.DEBUG = enabled;
    }
}