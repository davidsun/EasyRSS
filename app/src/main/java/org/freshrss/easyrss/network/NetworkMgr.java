/*******************************************************************************
 * Copyright (c) 2012 Pursuer (http://pursuer.me).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Pursuer - initial API and implementation
 ******************************************************************************/

package org.freshrss.easyrss.network;

import java.util.ConcurrentModificationException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import org.freshrss.easyrss.R;
import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.data.readersetting.SettingImageFetching;
import org.freshrss.easyrss.data.readersetting.SettingImmediateStateSyncing;
import org.freshrss.easyrss.data.readersetting.SettingSyncMethod;
import org.freshrss.easyrss.network.AbsDataSyncer.DataSyncerException;

final public class NetworkMgr implements DataSyncerListener {
    private class ItemContentSyncThread extends Thread {
        private int networkConfig;

        public synchronized int getNetworkConfig() {
            return networkConfig;
        }

        public synchronized void notifySyncStarted() {
            notifyAll();
        }

        @Override
        public void run() {
            final DataMgr dataMgr = DataMgr.getInstance();
            while (true) {
                try {
                    waitForSync();
                } catch (final InterruptedException exception) {
                    exception.printStackTrace();
                }
                final ItemContentDataSyncer syncer = ItemContentDataSyncer.getInstance(dataMgr, getNetworkConfig());
                NetworkMgr.this.notifySyncStarted(syncer);
                syncer.setListener(NetworkMgr.this);
                boolean succeeded;
                try {
                    syncer.sync();
                    succeeded = true;
                } catch (final DataSyncerException exception) {
                    exception.printStackTrace();
                    succeeded = false;
                }
                syncer.setListener(null);
                notifySyncFinished(syncer, succeeded);
            }
        }

        public synchronized void setNetworkConfig(final int networkConfig) {
            this.networkConfig = networkConfig;
        }

        public synchronized void waitForSync() throws InterruptedException {
            wait();
        }
    }
    private class SyncThread extends Thread {
        @Override
        public void run() {
            while (true) {
                final AbsDataSyncer syncer;
                synchronized (syncers) {
                    while (syncers.isEmpty()) {
                        try {
                            syncers.wait();
                        } catch (final InterruptedException exception) {
                            exception.printStackTrace();
                        }
                    }
                    syncer = syncers.remove();
                }
                notifySyncStarted(syncer);
                syncer.setListener(NetworkMgr.this);
                boolean succeeded;
                try {
                    syncer.sync();
                    succeeded = true;
                } catch (final Exception exception) {
                    notifyOnDataSyncerProgressChanged(
                            context.getString(R.string.TxtSyncFailed) + ": " + exception.getMessage() + ".", -1, -1);
                    succeeded = false;
                }
                syncer.setPending(false);
                syncer.setListener(null);
                notifySyncFinished(syncer, succeeded);
            }
        }
    }
    final static private Handler handler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            final Bundle bundle = msg.getData();
            if (instance != null) {
                switch (msg.what) {
                case MSG_ON_DATA_SYNCER_PROGRESS_CHANGED: {
                    final String text = bundle.getString(KEY_TEXT);
                    final int progress = bundle.getInt(KEY_PROGRESS);
                    final int maxProgress = bundle.getInt(KEY_MAX_PROGRESS);
                    try {
                        for (final NetworkListener listener : instance.listeners) {
                            listener.onDataSyncerProgressChanged(text, progress, maxProgress);
                        }
                    } catch (final ConcurrentModificationException exception) {
                        exception.printStackTrace();
                    }
                }
                    break;
                case MSG_ON_LOGIN: {
                    final boolean succeeded = bundle.getBoolean(KEY_SUCCEEDED);
                    try {
                        for (final NetworkListener listener : instance.listeners) {
                            listener.onLogin(succeeded);
                        }
                    } catch (final ConcurrentModificationException exception) {
                        exception.printStackTrace();
                    }
                }
                    break;
                case MSG_SYNC_FINISHED: {
                    final String syncerType = bundle.getString(KEY_SYNCER_TYPE);
                    final boolean succeeded = bundle.getBoolean(KEY_SUCCEEDED);
                    try {
                        for (final NetworkListener listener : instance.listeners) {
                            listener.onSyncFinished(syncerType, succeeded);
                        }
                    } catch (final ConcurrentModificationException exception) {
                        exception.printStackTrace();
                    }
                }
                    break;
                case MSG_SYNC_STARTED: {
                    final String syncerType = bundle.getString(KEY_SYNCER_TYPE);
                    try {
                        for (final NetworkListener listener : instance.listeners) {
                            listener.onSyncStarted(syncerType);
                        }
                    } catch (final ConcurrentModificationException exception) {
                        exception.printStackTrace();
                    }
                }
                    break;
                default:
                    break;
                }
            }
        }
    };
    private static NetworkMgr instance = null;
    final static private String KEY_MAX_PROGRESS = "maxProgress";
    final static private String KEY_PROGRESS = "progress";
    final static private String KEY_SUCCEEDED = "succeeded";
    final static private String KEY_SYNCER_TYPE = "syncerType";
    final static private String KEY_TEXT = "text";

    final static private int MSG_ON_DATA_SYNCER_PROGRESS_CHANGED = 0;;
    final static private int MSG_ON_LOGIN = 1;
    final static private int MSG_SYNC_FINISHED = 2;
    final static private int MSG_SYNC_STARTED = 3;

    public static NetworkMgr getInstance() {
        return instance;
    }

    public static synchronized void init(final Context context) {
        if (instance == null) {
            instance = new NetworkMgr(context);
        }
    }
    final private Context context;
    final private ItemContentSyncThread itemContentSyncThread;
    final private List<NetworkListener> listeners;
    private Thread loginThread;
    final private Queue<AbsDataSyncer> syncers;
    final private SyncThread syncThread;

    private NetworkMgr(final Context context) {
        this.context = context;
        this.syncers = new LinkedList<AbsDataSyncer>();
        this.listeners = new LinkedList<NetworkListener>();
        this.syncThread = new SyncThread();
        this.itemContentSyncThread = new ItemContentSyncThread();
        
        syncThread.setPriority(Thread.MIN_PRIORITY);
        syncThread.start();
        itemContentSyncThread.setPriority(Thread.MIN_PRIORITY);
        itemContentSyncThread.start();
    }

    /*
     * This method need to be called in MAIN thread.
     */
    public void addListener(final NetworkListener listener) {
        listeners.add(listener);
    }

    public void login(final String user, final String pass) {
        if (loginThread != null && loginThread.isAlive()) {
            return;
        }
        loginThread = new Thread() {
            @Override
            public void run() {
                final LoginDataSyncer syncer = new LoginDataSyncer(DataMgr.getInstance(), user, pass);
                try {
                    syncer.sync();
                    notifyOnLogin(true);
                } catch (final DataSyncerException exception) {
                    notifyOnLogin(false);
                    exception.printStackTrace();
                }
            }
        };
        loginThread.start();
    }

    private void notifyOnDataSyncerProgressChanged(final String text, final int progress, final int maxProgress) {
        final Message msg = handler.obtainMessage(MSG_ON_DATA_SYNCER_PROGRESS_CHANGED);
        final Bundle bundle = new Bundle();
        bundle.putString(KEY_TEXT, text);
        bundle.putInt(KEY_PROGRESS, progress);
        bundle.putInt(KEY_MAX_PROGRESS, maxProgress);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    private void notifyOnLogin(final boolean succeeded) {
        final Message msg = handler.obtainMessage(MSG_ON_LOGIN);
        final Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_SUCCEEDED, succeeded);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    private void notifySyncFinished(final AbsDataSyncer syncer, final boolean succeeded) {
        final Message msg = handler.obtainMessage(MSG_SYNC_FINISHED);
        final Bundle bundle = new Bundle();
        bundle.putString(KEY_SYNCER_TYPE, syncer.getClass().getName());
        bundle.putBoolean(KEY_SUCCEEDED, succeeded);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    private void notifySyncStarted(final AbsDataSyncer syncer) {
        final Message msg = handler.obtainMessage(MSG_SYNC_STARTED);
        final Bundle bundle = new Bundle();
        bundle.putString(KEY_SYNCER_TYPE, syncer.getClass().getName());
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    @Override
    public void onProgressChanged(final String text, final int progress, final int maxProgress) {
        notifyOnDataSyncerProgressChanged(text, progress, maxProgress);
    }

    /*
     * This method need to be called in MAIN thread.
     */
    public void removeListener(final NetworkListener listener) {
        listeners.remove(listener);
    }

    public void startImmediateItemStateSyncing() {
        final SettingImmediateStateSyncing sStateSyncing = new SettingImmediateStateSyncing(DataMgr.getInstance());
        if (sStateSyncing.getData()) {
            startSync(TransactionDataSyncer.getInstance(DataMgr.getInstance(), SettingSyncMethod.SYNC_METHOD_MANUAL));
        }
    }

    public void startSync(final AbsDataSyncer syncer) {
        if (!syncer.setEnterPending()) {
            return;
        }
        synchronized (syncers) {
            syncers.add(syncer);
            syncers.notify();
        }
    }

    public void startSyncItemContent() {
        final SettingImageFetching sImageFetch = new SettingImageFetching(DataMgr.getInstance());
        itemContentSyncThread.setNetworkConfig(sImageFetch.getData());
        itemContentSyncThread.notifySyncStarted();
    }
}
