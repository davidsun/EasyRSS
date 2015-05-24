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

package org.freshrss.easyrss.account;

import java.io.IOException;

import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.data.Setting;
import org.freshrss.easyrss.network.NetworkListener;
import org.freshrss.easyrss.network.NetworkMgr;
import org.freshrss.easyrss.network.url.AbsURL;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ReaderAccountMgr implements NetworkListener {
    private class CallbackAuthToken implements AccountManagerCallback<Bundle> {
        final private boolean showIntent;

        public CallbackAuthToken(final boolean showIntent) {
            this.showIntent = showIntent;
        }

        public void run(final AccountManagerFuture<Bundle> result) {
            try {
                final Bundle bundle = result.getResult();
                final Intent intent = (Intent) bundle.get(AccountManager.KEY_INTENT);
                if (intent != null) {
                    /*
                     * Remember: This returns onAuthUpdateFinished(true), then
                     * it may still be canceled by user!
                     */
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    if (showIntent) {
                        context.startActivity(intent);
                    }
                } else {
                    final DataMgr dataMgr = DataMgr.getInstance();
                    dataMgr.updateSetting(new Setting(Setting.SETTING_AUTH, bundle
                            .getString(AccountManager.KEY_AUTHTOKEN)));
                }
                notifyOnAuthUpdateFinished(true);
            } catch (final OperationCanceledException exception) {
                notifyOnAuthUpdateFinished(false);
                exception.printStackTrace();
            } catch (final AuthenticatorException exception) {
                notifyOnAuthUpdateFinished(false);
                exception.printStackTrace();
            } catch (final IOException exception) {
                notifyOnAuthUpdateFinished(false);
                exception.printStackTrace();
            }
        }
    };

    public static final String ACCOUNT_TYPE = "com.google";
    public static final String ACCOUNT_AUTH_TYPE = "reader";

    private static ReaderAccountMgr instance;

    public synchronized static ReaderAccountMgr getInstance() {
        return instance;
    }

    public synchronized static void init(final Context context) {
        if (instance == null) {
            instance = new ReaderAccountMgr(context);
        }
    }

    final private AccountManager accMgr;
    final private Context context;
    private ReaderAccountMgrListener listener;

    private ReaderAccountMgr(final Context context) {
        this.accMgr = AccountManager.get(context);
        this.context = context;
    }

    public synchronized String blockingGetAuth() {
        if (!getAuth()) {
            return DataMgr.getInstance().getSettingByName(Setting.SETTING_AUTH);
        }
        try {
            wait();
        } catch (final InterruptedException exception) {
            exception.printStackTrace();
        }
        return DataMgr.getInstance().getSettingByName(Setting.SETTING_AUTH);
    }

    public void clearLogin() {
        final DataMgr dataMgr = DataMgr.getInstance();
        dataMgr.removeSettingByName(Setting.SETTING_AUTH);
        dataMgr.removeSettingByName(Setting.SETTING_IS_CLIENT_LOGIN);
        dataMgr.removeSettingByName(Setting.SETTING_USERNAME);
        dataMgr.removeSettingByName(Setting.SETTING_PASSWORD);
    }

    /*
     * @return whether syncing is started
     */
    public boolean getAuth() {
        if (isAuthValid()) {
            return false;
        }
        final DataMgr dataMgr = DataMgr.getInstance();
        final String user = dataMgr.getSettingByName(Setting.SETTING_USERNAME);
        if (user == null) {
            return false;
        }
        if (isClientLogin()) {
            final String pass = dataMgr.getSettingByName(Setting.SETTING_PASSWORD);
            if (pass == null) {
                return false;
            }
            return tryClientLogin(user, pass);
        } else {
            return tryNonClicentLogin(user);
        }
    }

    public ReaderAccountMgrListener getListener() {
        return listener;
    }

    public boolean hasAccount() {
        final DataMgr dataMgr = DataMgr.getInstance();
        final String user = dataMgr.getSettingByName(Setting.SETTING_USERNAME);
        final String cLogin = dataMgr.getSettingByName(Setting.SETTING_IS_CLIENT_LOGIN);
        if (user == null || cLogin == null) {
            return false;
        }
        if (Boolean.valueOf(cLogin)) {
            final String pass = dataMgr.getSettingByName(Setting.SETTING_PASSWORD);
            return (pass != null);
        } else {
            final Account[] accounts = accMgr.getAccountsByType(ACCOUNT_TYPE);
            for (int i = 0; i < accounts.length; i++) {
                if (accounts[i].name.equals(user)) {
                    return true;
                }
            }
            return false;
        }
    }

    public void invalidateAuth() {
        if (!isClientLogin()) {
            accMgr.invalidateAuthToken(ACCOUNT_TYPE, DataMgr.getInstance().getSettingByName(Setting.SETTING_AUTH));
        }
        DataMgr.getInstance().removeSettingByName(Setting.SETTING_AUTH);
    }

    public boolean isAuthValid() {
        final String auth = DataMgr.getInstance().getSettingByName(Setting.SETTING_AUTH);
        return (auth != null);
    }

    public boolean isClientLogin() {
        final String cLogin = DataMgr.getInstance().getSettingByName(Setting.SETTING_IS_CLIENT_LOGIN);
        return (cLogin != null && Boolean.valueOf(cLogin));
    }

    private synchronized void notifyOnAuthUpdateFinished(final boolean succeeded) {
        notifyAll();
        if (listener != null) {
            listener.onAuthUpdateFinished(succeeded);
        }
    }

    @Override
    public void onDataSyncerProgressChanged(final String text, final int progress, final int maxProgress) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onLogin(final boolean succeeded) {
        NetworkMgr.getInstance().removeListener(this);
        notifyOnAuthUpdateFinished(succeeded);
    }

    @Override
    public void onSyncFinished(final String syncerType, final boolean succeeded) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onSyncStarted(final String syncerType) {
        // TODO Auto-generated method stub
    }

    public void setClientLogin(final String serverUrl, final String user, final String pass) {
        final DataMgr dataMgr = DataMgr.getInstance();
        dataMgr.updateSetting(new Setting(Setting.SETTING_SERVER_URL, serverUrl));
        AbsURL.setServerUrl(serverUrl);
        dataMgr.updateSetting(new Setting(Setting.SETTING_USERNAME, user));
        dataMgr.updateSetting(new Setting(Setting.SETTING_PASSWORD, pass));
        dataMgr.updateSetting(new Setting(Setting.SETTING_IS_CLIENT_LOGIN, String.valueOf(true)));
    }

    public void setListener(final ReaderAccountMgrListener listener) {
        this.listener = listener;
    }

    public void setNonClientLogin(final String user) {
        final DataMgr dataMgr = DataMgr.getInstance();
        dataMgr.updateSetting(new Setting(Setting.SETTING_USERNAME, user));
        dataMgr.updateSetting(new Setting(Setting.SETTING_IS_CLIENT_LOGIN, String.valueOf(false)));
    }

    public boolean tryClientLogin(final String user, final String pass) {
        final NetworkMgr networkMgr = NetworkMgr.getInstance();
        networkMgr.addListener(this);
        networkMgr.login(user, pass);
        return true;
    }

    @SuppressWarnings("deprecation")
    public void refreshAuthState(final String user) {
        final Account[] accounts = accMgr.getAccountsByType(ACCOUNT_TYPE);
        for (int i = 0; i < accounts.length; i++) {
            if (accounts[i].name.equals(user)) {
                accMgr.getAuthToken(accounts[i], ACCOUNT_AUTH_TYPE, false, new CallbackAuthToken(false), null);
                break;
            }
        }
    }

    @SuppressWarnings("deprecation")
    public boolean tryNonClicentLogin(final String user) {
        final Account[] accounts = accMgr.getAccountsByType(ACCOUNT_TYPE);
        for (int i = 0; i < accounts.length; i++) {
            if (accounts[i].name.equals(user)) {
                accMgr.getAuthToken(accounts[i], ACCOUNT_AUTH_TYPE, false, new CallbackAuthToken(true), null);
                return true;
            }
        }
        return false;
    }
}
