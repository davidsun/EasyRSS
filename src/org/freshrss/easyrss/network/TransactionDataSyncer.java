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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;

import org.freshrss.easyrss.R;
import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.data.Transaction;

public class TransactionDataSyncer extends AbsDataSyncer {
    private class SyncingThread implements Runnable {
        private Exception exception;

        public Exception getException() {
            return exception;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    final Transaction trans = getNextTransaction();
                    if (trans == null) {
                        return;
                    }
                    String tag = "";
                    boolean isAdd = false;
                    switch (trans.getType()) {
                    case Transaction.TYPE_SET_READ:
                        tag = "user/-/state/com.google/read";
                        isAdd = true;
                        break;
                    case Transaction.TYPE_REMOVE_READ:
                        tag = "user/-/state/com.google/read";
                        isAdd = false;
                        break;
                    case Transaction.TYPE_SET_STARRED:
                        tag = "user/-/state/com.google/starred";
                        isAdd = true;
                        break;
                    case Transaction.TYPE_REMOVE_STARRED:
                        tag = "user/-/state/com.google/starred";
                        isAdd = false;
                        break;
                    default:
                    }
                    if (tag.length() > 0) {
                        final ItemTagDataSyncer syncer = new ItemTagDataSyncer(dataMgr, networkConfig, trans.getUid(),
                                tag, isAdd);
                        syncer.sync();
                    }
                    dataMgr.removeTransactionById(trans.getId());
                } catch (final DataSyncerException exception) {
                    this.exception = exception;
                    return;
                }
            }
        }
    }

    private final static int SYNCING_THREAD_COUNT = 5;

    final private List<Transaction> transactions;
    private int progress;

    private static TransactionDataSyncer instance;

    public static synchronized TransactionDataSyncer getInstance(final DataMgr dataMgr, final int networkConfig) {
        if (instance == null) {
            instance = new TransactionDataSyncer(dataMgr, networkConfig);
        }
        return instance;
    }

    private TransactionDataSyncer(final DataMgr dataMgr, final int networkConfig) {
        super(dataMgr, networkConfig);
        this.transactions = new ArrayList<Transaction>();
    }

    @Override
    protected void finishSyncing() {
        // TODO nothing needed
    }

    private Transaction getNextTransaction() throws DataSyncerException {
        synchronized (transactions) {
            final Context context = dataMgr.getContext();
            notifyProgressChanged(context.getString(R.string.TxtSyncingItemStatus), progress, transactions.size());
            if (progress < transactions.size()) {
                final TokenDataSyncer tSyncer = new TokenDataSyncer(dataMgr, networkConfig);
                tSyncer.sync();
                final Transaction ret = transactions.get(progress);
                progress++;
                return ret;
            } else {
                return null;
            }
        }
    }

    @Override
    public void startSyncing() throws DataSyncerException {
        syncTransactions();
    }

    private void syncTransactions() throws DataSyncerException {
        final Context context = dataMgr.getContext();
        final ContentResolver resolver = context.getContentResolver();
        while (true) {
            if (!NetworkUtils.checkSyncingNetworkStatus(context, networkConfig)) {
                return;
            }
            progress = 0;
            transactions.clear();
            final Cursor cur = resolver.query(Transaction.CONTENT_URI, null, null, null, Transaction._ID + " LIMIT 50");
            for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                transactions.add(Transaction.fromCursor(cur));
            }
            cur.close();
            if (!transactions.isEmpty()) {
                final int tCount = Math.min(SYNCING_THREAD_COUNT, transactions.size());
                final List<SyncingThread> syncingThreads = new ArrayList<TransactionDataSyncer.SyncingThread>(tCount);
                final ExecutorService execService = Executors.newFixedThreadPool(tCount, new ThreadFactory() {
                    @Override
                    public Thread newThread(final Runnable runnable) {
                        final Thread thread = new Thread(runnable);
                        thread.setPriority(Thread.MIN_PRIORITY);
                        return thread;
                    }
                });
                for (int i = 0; i < tCount; i++) {
                    final SyncingThread thread = new SyncingThread();
                    syncingThreads.add(thread);
                    execService.execute(thread);
                }
                execService.shutdown();
                try {
                    execService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
                    for (final SyncingThread thread : syncingThreads) {
                        if (thread.getException() != null) {
                            throw new DataSyncerException(thread.getException());
                        }
                    }
                } catch (final InterruptedException exception) {
                    exception.printStackTrace();
                }
            } else {
                break;
            }
        }
    }
}
