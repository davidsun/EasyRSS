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

package org.freshrss.easyrss.view;

import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.network.NetworkListener;

import android.content.Context;
import android.view.LayoutInflater;


public abstract class AbsViewCtrl implements NetworkListener {
    final protected Context context;
    final protected DataMgr dataMgr;
    final protected ICachedView view;
    final protected int resId;
    protected ViewCtrlListener listener;

    public AbsViewCtrl(final DataMgr dataMgr, final int resId, final Context context) {
        this.resId = resId;
        this.context = context;
        this.dataMgr = dataMgr;

        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.view = (ICachedView) inflater.inflate(resId, null);
    }

    public ViewCtrlListener getListener() {
        return listener;
    }

    public int getResId() {
        return resId;
    }

    public ICachedView getView() {
        return view;
    }

    protected void handleOnDataSyncerProgressChanged(final String text, final int progress, final int maxProgress) {
        // TODO Empty method
    }

    protected void handleOnLogin(final boolean succeeded) {
        // TODO Empty method
    }

    protected void handleOnSyncFinished(final String syncerType, final boolean succeeded) {
        // TODO Empty method
    }

    protected void handleOnSyncStarted(final String syncerType) {
        // TODO Empty method
    }

    public abstract void onActivate();

    public abstract void onCreate();

    @Override
    public void onDataSyncerProgressChanged(final String text, final int progress, final int maxProgress) {
        handleOnDataSyncerProgressChanged(text, progress, maxProgress);
    }

    public abstract void onDeactivate();

    public abstract void onDestory();

    @Override
    public void onLogin(final boolean succeeded) {
        handleOnLogin(succeeded);
    }

    @Override
    public void onSyncFinished(final String syncerType, final boolean succeeded) {
        handleOnSyncFinished(syncerType, succeeded);
    }

    @Override
    public void onSyncStarted(final String syncerType) {
        handleOnSyncStarted(syncerType);
    }

    public void setListener(final ViewCtrlListener listener) {
        this.listener = listener;
    }
}
