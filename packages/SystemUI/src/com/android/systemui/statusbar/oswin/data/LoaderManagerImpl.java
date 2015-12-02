package com.android.systemui.statusbar.oswin.data;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;

import android.app.Dialog;
import android.content.Loader;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

public class LoaderManagerImpl extends LoaderManager {
    static final String TAG = "LoaderManager";
    static boolean DEBUG = false;

    final SparseArray<LoaderInfo> mLoaders = new SparseArray<LoaderInfo>(0);

    final SparseArray<LoaderInfo> mInactiveLoaders = new SparseArray<LoaderInfo>(0);

    final String mWho;

    Dialog mActivity;
    boolean mStarted;
    boolean mRetaining;
    boolean mRetainingStarted;
    
    boolean mCreatingLoader;

    final class LoaderInfo implements Loader.OnLoadCompleteListener<Object>,
            Loader.OnLoadCanceledListener<Object> {
        final int mId;
        final Bundle mArgs;
        LoaderManager.LoaderCallbacks<Object> mCallbacks;
        Loader<Object> mLoader;
        boolean mHaveData;
        boolean mDeliveredData;
        Object mData;
        boolean mStarted;
        boolean mRetaining;
        boolean mRetainingStarted;
        boolean mReportNextStart;
        boolean mDestroyed;
        boolean mListenerRegistered;

        LoaderInfo mPendingLoader;
        
        public LoaderInfo(int id, Bundle args, LoaderManager.LoaderCallbacks<Object> callbacks) {
            mId = id;
            mArgs = args;
            mCallbacks = callbacks;
        }
        
        void start() {
            if (mRetaining && mRetainingStarted) {
                // Our owner is started, but we were being retained from a
                // previous instance in the started state...  so there is really
                // nothing to do here, since the loaders are still started.
                mStarted = true;
                return;
            }

            if (mStarted) {
                // If loader already started, don't restart.
                return;
            }

            mStarted = true;
            
            if (DEBUG) Log.v(TAG, "  Starting: " + this);
            if (mLoader == null && mCallbacks != null) {
               mLoader = mCallbacks.onCreateLoader(mId, mArgs);
            }
            if (mLoader != null) {
                if (mLoader.getClass().isMemberClass()
                        && !Modifier.isStatic(mLoader.getClass().getModifiers())) {
                    throw new IllegalArgumentException(
                            "Object returned from onCreateLoader must not be a non-static inner member class: "
                            + mLoader);
                }
                if (!mListenerRegistered) {
                    mLoader.registerListener(mId, this);
                    mLoader.registerOnLoadCanceledListener(this);
                    mListenerRegistered = true;
                }
                mLoader.startLoading();
            }
        }
        
        void retain() {
            if (DEBUG) Log.v(TAG, "  Retaining: " + this);
            mRetaining = true;
            mRetainingStarted = mStarted;
            mStarted = false;
            mCallbacks = null;
        }
        
        void finishRetain() {
            if (mRetaining) {
                if (DEBUG) Log.v(TAG, "  Finished Retaining: " + this);
                mRetaining = false;
                if (mStarted != mRetainingStarted) {
                    if (!mStarted) {
                        // This loader was retained in a started state, but
                        // at the end of retaining everything our owner is
                        // no longer started...  so make it stop.
                        stop();
                    }
                }
            }

            if (mStarted && mHaveData && !mReportNextStart) {
                // This loader has retained its data, either completely across
                // a configuration change or just whatever the last data set
                // was after being restarted from a stop, and now at the point of
                // finishing the retain we find we remain started, have
                // our data, and the owner has a new callback...  so
                // let's deliver the data now.
                callOnLoadFinished(mLoader, mData);
            }
        }
        
        void reportStart() {
            if (mStarted) {
                if (mReportNextStart) {
                    mReportNextStart = false;
                    if (mHaveData) {
                        callOnLoadFinished(mLoader, mData);
                    }
                }
            }
        }

        void stop() {
            if (DEBUG) Log.v(TAG, "  Stopping: " + this);
            mStarted = false;
            if (!mRetaining) {
                if (mLoader != null && mListenerRegistered) {
                    // Let the loader know we're done with it
                    mListenerRegistered = false;
                    mLoader.unregisterListener(this);
                    mLoader.unregisterOnLoadCanceledListener(this);
                    mLoader.stopLoading();
                }
            }
        }

        void cancel() {
            if (DEBUG) Log.v(TAG, "  Canceling: " + this);
            if (mStarted && mLoader != null && mListenerRegistered) {
                if (!mLoader.cancelLoad()) {
                    onLoadCanceled(mLoader);
                }
            }
        }

        void destroy() {
            if (DEBUG) Log.v(TAG, "  Destroying: " + this);
            mDestroyed = true;
            boolean needReset = mDeliveredData;
            mDeliveredData = false;
            if (mCallbacks != null && mLoader != null && mHaveData && needReset) {
                if (DEBUG) Log.v(TAG, "  Reseting: " + this);
                try {
                    mCallbacks.onLoaderReset(mLoader);
                } finally {

                }
            }
            mCallbacks = null;
            mData = null;
            mHaveData = false;
            if (mLoader != null) {
                if (mListenerRegistered) {
                    mListenerRegistered = false;
                    mLoader.unregisterListener(this);
                    mLoader.unregisterOnLoadCanceledListener(this);
                }
                mLoader.reset();
            }
            if (mPendingLoader != null) {
                mPendingLoader.destroy();
            }
        }

        @Override
        public void onLoadCanceled(Loader<Object> loader) {
            if (DEBUG) Log.v(TAG, "onLoadCanceled: " + this);

            if (mDestroyed) {
                if (DEBUG) Log.v(TAG, "  Ignoring load canceled -- destroyed");
                return;
            }

            if (mLoaders.get(mId) != this) {
                // This cancellation message is not coming from the current active loader.
                // We don't care about it.
                if (DEBUG) Log.v(TAG, "  Ignoring load canceled -- not active");
                return;
            }

            LoaderInfo pending = mPendingLoader;
            if (pending != null) {
                // There is a new request pending and we were just
                // waiting for the old one to cancel or complete before starting
                // it.  So now it is time, switch over to the new loader.
                if (DEBUG) Log.v(TAG, "  Switching to pending loader: " + pending);
                mPendingLoader = null;
                mLoaders.put(mId, null);
                destroy();
                installLoader(pending);
            }
        }

        @Override
        public void onLoadComplete(Loader<Object> loader, Object data) {
            if (DEBUG) Log.v(TAG, "onLoadComplete: " + this);
            
            if (mDestroyed) {
                if (DEBUG) Log.v(TAG, "  Ignoring load complete -- destroyed");
                return;
            }

            if (mLoaders.get(mId) != this) {
                // This data is not coming from the current active loader.
                // We don't care about it.
                if (DEBUG) Log.v(TAG, "  Ignoring load complete -- not active");
                return;
            }
            
            LoaderInfo pending = mPendingLoader;
            if (pending != null) {
                // There is a new request pending and we were just
                // waiting for the old one to complete before starting
                // it.  So now it is time, switch over to the new loader.
                if (DEBUG) Log.v(TAG, "  Switching to pending loader: " + pending);
                mPendingLoader = null;
                mLoaders.put(mId, null);
                destroy();
                installLoader(pending);
                return;
            }
            
            // Notify of the new data so the app can switch out the old data before
            // we try to destroy it.
            if (mData != data || !mHaveData) {
                mData = data;
                mHaveData = true;
                if (mStarted) {
                    callOnLoadFinished(loader, data);
                }
            }

            //if (DEBUG) Log.v(TAG, "  onLoadFinished returned: " + this);

            // We have now given the application the new loader with its
            // loaded data, so it should have stopped using the previous
            // loader.  If there is a previous loader on the inactive list,
            // clean it up.
            LoaderInfo info = mInactiveLoaders.get(mId);
            if (info != null && info != this) {
                info.mDeliveredData = false;
                info.destroy();
                mInactiveLoaders.remove(mId);
            }
        }

        void callOnLoadFinished(Loader<Object> loader, Object data) {
            if (mCallbacks != null) {
                 try {
                    if (DEBUG) Log.v(TAG, "  onLoadFinished in " + loader + ": " + loader.dataToString(data));
                    mCallbacks.onLoadFinished(loader, data);
                } finally {

                }
                mDeliveredData = true;
            }
        }
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(64);
            sb.append("LoaderInfo{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(" #");
            sb.append(mId);
            sb.append(" : ");
            sb.append("}}");
            return sb.toString();
        }

        public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            writer.print(prefix); writer.print("mId="); writer.print(mId);
                    writer.print(" mArgs="); writer.println(mArgs);
            writer.print(prefix); writer.print("mCallbacks="); writer.println(mCallbacks);
            writer.print(prefix); writer.print("mLoader="); writer.println(mLoader);
            if (mLoader != null) {
                mLoader.dump(prefix + "  ", fd, writer, args);
            }
            if (mHaveData || mDeliveredData) {
                writer.print(prefix); writer.print("mHaveData="); writer.print(mHaveData);
                        writer.print("  mDeliveredData="); writer.println(mDeliveredData);
                writer.print(prefix); writer.print("mData="); writer.println(mData);
            }
            writer.print(prefix); writer.print("mStarted="); writer.print(mStarted);
                    writer.print(" mReportNextStart="); writer.print(mReportNextStart);
                    writer.print(" mDestroyed="); writer.println(mDestroyed);
            writer.print(prefix); writer.print("mRetaining="); writer.print(mRetaining);
                    writer.print(" mRetainingStarted="); writer.print(mRetainingStarted);
                    writer.print(" mListenerRegistered="); writer.println(mListenerRegistered);
            if (mPendingLoader != null) {
                writer.print(prefix); writer.println("Pending Loader ");
                        writer.print(mPendingLoader); writer.println(":");
                mPendingLoader.dump(prefix + "  ", fd, writer, args);
            }
        }
    }
    
    public LoaderManagerImpl(String who, Dialog activity, boolean started) {
        mWho = who;
        mActivity = activity;
        mStarted = started;
    }
    
    void updateActivity(Dialog activity) {
        mActivity = activity;
    }
    
    private LoaderInfo createLoader(int id, Bundle args,
            LoaderManager.LoaderCallbacks<Object> callback) {
        LoaderInfo info = new LoaderInfo(id, args,  (LoaderManager.LoaderCallbacks<Object>)callback);
        Loader<Object> loader = callback.onCreateLoader(id, args);
        info.mLoader = (Loader<Object>)loader;
        return info;
    }
    
    private LoaderInfo createAndInstallLoader(int id, Bundle args,
            LoaderManager.LoaderCallbacks<Object> callback) {
        try {
            mCreatingLoader = true;
            LoaderInfo info = createLoader(id, args, callback);
            installLoader(info);
            return info;
        } finally {
            mCreatingLoader = false;
        }
    }
    
    void installLoader(LoaderInfo info) {
        mLoaders.put(info.mId, info);
        if (mStarted) {
            // The activity will start all existing loaders in it's onStart(),
            // so only start them here if we're past that point of the activitiy's
            // life cycle
            info.start();
        }
    }
    
    @SuppressWarnings("unchecked")
    public <D> Loader<D> initLoader(int id, Bundle args, LoaderManager.LoaderCallbacks<D> callback) {
        if (mCreatingLoader) {
            throw new IllegalStateException("Called while creating a loader");
        }
        
        LoaderInfo info = mLoaders.get(id);
        
        if (DEBUG) Log.v(TAG, "initLoader in " + this + ": args=" + args);

        if (info == null) {
            // Loader doesn't already exist; create.
            info = createAndInstallLoader(id, args,  (LoaderManager.LoaderCallbacks<Object>)callback);
            if (DEBUG) Log.v(TAG, "  Created new loader " + info);
        } else {
            if (DEBUG) Log.v(TAG, "  Re-using existing loader " + info);
            info.mCallbacks = (LoaderManager.LoaderCallbacks<Object>)callback;
        }
        
        if (info.mHaveData && mStarted) {
            // If the loader has already generated its data, report it now.
            info.callOnLoadFinished(info.mLoader, info.mData);
        }
        
        return (Loader<D>)info.mLoader;
    }
    
    @SuppressWarnings("unchecked")
    public <D> Loader<D> restartLoader(int id, Bundle args, LoaderManager.LoaderCallbacks<D> callback) {
        if (mCreatingLoader) {
            throw new IllegalStateException("Called while creating a loader");
        }
        
        LoaderInfo info = mLoaders.get(id);
        if (DEBUG) Log.v(TAG, "restartLoader in " + this + ": args=" + args);
        if (info != null) {
            LoaderInfo inactive = mInactiveLoaders.get(id);
            if (inactive != null) {
                if (info.mHaveData) {
                    // This loader now has data...  we are probably being
                    // called from within onLoadComplete, where we haven't
                    // yet destroyed the last inactive loader.  So just do
                    // that now.
                    if (DEBUG) Log.v(TAG, "  Removing last inactive loader: " + info);
                    inactive.mDeliveredData = false;
                    inactive.destroy();
                    info.mLoader.abandon();
                    mInactiveLoaders.put(id, info);
                } else {
                    // We already have an inactive loader for this ID that we are
                    // waiting for!  What to do, what to do...
                    if (!info.mStarted) {
                        // The current Loader has not been started...  we thus
                        // have no reason to keep it around, so bam, slam,
                        // thank-you-ma'am.
                        if (DEBUG) Log.v(TAG, "  Current loader is stopped; replacing");
                        mLoaders.put(id, null);
                        info.destroy();
                    } else {
                        // Now we have three active loaders... we'll queue
                        // up this request to be processed once one of the other loaders
                        // finishes or is canceled.
                        if (DEBUG) Log.v(TAG, "  Current loader is running; attempting to cancel");
                        info.cancel();
                        if (info.mPendingLoader != null) {
                            if (DEBUG) Log.v(TAG, "  Removing pending loader: " + info.mPendingLoader);
                            info.mPendingLoader.destroy();
                            info.mPendingLoader = null;
                        }
                        if (DEBUG) Log.v(TAG, "  Enqueuing as new pending loader");
                        info.mPendingLoader = createLoader(id, args, 
                                (LoaderManager.LoaderCallbacks<Object>)callback);
                        return (Loader<D>)info.mPendingLoader.mLoader;
                    }
                }
            } else {
                // Keep track of the previous instance of this loader so we can destroy
                // it when the new one completes.
                if (DEBUG) Log.v(TAG, "  Making last loader inactive: " + info);
                info.mLoader.abandon();
                mInactiveLoaders.put(id, info);
            }
        }
        
        info = createAndInstallLoader(id, args,  (LoaderManager.LoaderCallbacks<Object>)callback);
        return (Loader<D>)info.mLoader;
    }
    
    /**
     * Rip down, tear apart, shred to pieces a current Loader ID.  After returning
     * from this function, any Loader objects associated with this ID are
     * destroyed.  Any data associated with them is destroyed.  You better not
     * be using it when you do this.
     * @param id Identifier of the Loader to be destroyed.
     */
    public void destroyLoader(int id) {
        if (mCreatingLoader) {
            throw new IllegalStateException("Called while creating a loader");
        }
        
        if (DEBUG) Log.v(TAG, "destroyLoader in " + this + " of " + id);
        int idx = mLoaders.indexOfKey(id);
        if (idx >= 0) {
            LoaderInfo info = mLoaders.valueAt(idx);
            mLoaders.removeAt(idx);
            info.destroy();
        }
        idx = mInactiveLoaders.indexOfKey(id);
        if (idx >= 0) {
            LoaderInfo info = mInactiveLoaders.valueAt(idx);
            mInactiveLoaders.removeAt(idx);
            info.destroy();
        }
    }

    /**
     * Return the most recent Loader object associated with the
     * given ID.
     */
    @SuppressWarnings("unchecked")
    public <D> Loader<D> getLoader(int id) {
        if (mCreatingLoader) {
            throw new IllegalStateException("Called while creating a loader");
        }
        
        LoaderInfo loaderInfo = mLoaders.get(id);
        if (loaderInfo != null) {
            if (loaderInfo.mPendingLoader != null) {
                return (Loader<D>)loaderInfo.mPendingLoader.mLoader;
            }
            return (Loader<D>)loaderInfo.mLoader;
        }
        return null;
    }
 
    void doStart() {
        if (DEBUG) Log.v(TAG, "Starting in " + this);
        if (mStarted) {
            RuntimeException e = new RuntimeException("here");
            e.fillInStackTrace();
            Log.w(TAG, "Called doStart when already started: " + this, e);
            return;
        }
        
        mStarted = true;

        // Call out to sub classes so they can start their loaders
        // Let the existing loaders know that we want to be notified when a load is complete
        for (int i = mLoaders.size()-1; i >= 0; i--) {
            mLoaders.valueAt(i).start();
        }
    }
    
    void doStop() {
        if (DEBUG) Log.v(TAG, "Stopping in " + this);
        if (!mStarted) {
            RuntimeException e = new RuntimeException("here");
            e.fillInStackTrace();
            Log.w(TAG, "Called doStop when not started: " + this, e);
            return;
        }

        for (int i = mLoaders.size()-1; i >= 0; i--) {
            mLoaders.valueAt(i).stop();
        }
        mStarted = false;
    }
    
    void doRetain() {
        if (DEBUG) Log.v(TAG, "Retaining in " + this);
        if (!mStarted) {
            RuntimeException e = new RuntimeException("here");
            e.fillInStackTrace();
            Log.w(TAG, "Called doRetain when not started: " + this, e);
            return;
        }

        mRetaining = true;
        mStarted = false;
        for (int i = mLoaders.size()-1; i >= 0; i--) {
            mLoaders.valueAt(i).retain();
        }
    }
    
    void finishRetain() {
        if (mRetaining) {
            if (DEBUG) Log.v(TAG, "Finished Retaining in " + this);

            mRetaining = false;
            for (int i = mLoaders.size()-1; i >= 0; i--) {
                mLoaders.valueAt(i).finishRetain();
            }
        }
    }
    
    void doReportNextStart() {
        for (int i = mLoaders.size()-1; i >= 0; i--) {
            mLoaders.valueAt(i).mReportNextStart = true;
        }
    }

    void doReportStart() {
        for (int i = mLoaders.size()-1; i >= 0; i--) {
            mLoaders.valueAt(i).reportStart();
        }
    }

    void doDestroy() {
        if (!mRetaining) {
            if (DEBUG) Log.v(TAG, "Destroying Active in " + this);
            for (int i = mLoaders.size()-1; i >= 0; i--) {
                mLoaders.valueAt(i).destroy();
            }
            mLoaders.clear();
        }
        
        if (DEBUG) Log.v(TAG, "Destroying Inactive in " + this);
        for (int i = mInactiveLoaders.size()-1; i >= 0; i--) {
            mInactiveLoaders.valueAt(i).destroy();
        }
        mInactiveLoaders.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("LoaderManager{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" in ");
        sb.append("}}");
        return sb.toString();
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        if (mLoaders.size() > 0) {
            writer.print(prefix); writer.println("Active Loaders:");
            String innerPrefix = prefix + "    ";
            for (int i=0; i < mLoaders.size(); i++) {
                LoaderInfo li = mLoaders.valueAt(i);
                writer.print(prefix); writer.print("  #"); writer.print(mLoaders.keyAt(i));
                        writer.print(": "); writer.println(li.toString());
                li.dump(innerPrefix, fd, writer, args);
            }
        }
        if (mInactiveLoaders.size() > 0) {
            writer.print(prefix); writer.println("Inactive Loaders:");
            String innerPrefix = prefix + "    ";
            for (int i=0; i < mInactiveLoaders.size(); i++) {
                LoaderInfo li = mInactiveLoaders.valueAt(i);
                writer.print(prefix); writer.print("  #"); writer.print(mInactiveLoaders.keyAt(i));
                        writer.print(": "); writer.println(li.toString());
                li.dump(innerPrefix, fd, writer, args);
            }
        }
    }

    public boolean hasRunningLoaders() {
        boolean loadersRunning = false;
        final int count = mLoaders.size();
        for (int i = 0; i < count; i++) {
            final LoaderInfo li = mLoaders.valueAt(i);
            loadersRunning |= li.mStarted && !li.mDeliveredData;
        }
        return loadersRunning;
    }
}