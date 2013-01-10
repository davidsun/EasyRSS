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

package com.pursuer.reader.easyrss;

import com.pursuer.reader.easyrss.R;
import com.pursuer.reader.easyrss.account.ReaderAccountMgr;
import com.pursuer.reader.easyrss.account.ReaderAccountMgrListener;
import com.pursuer.reader.easyrss.data.DataMgr;
import com.pursuer.reader.easyrss.network.NetworkMgr;
import com.pursuer.reader.easyrss.view.AbsViewCtrl;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

public class LoginViewCtrl extends AbsViewCtrl implements ReaderAccountMgrListener {
    final private static int MSG_LOGIN_SUCCEEDED = 0;
    final private static int MSG_LOGIN_FAILED = 1;
    final private static int MSG_GAIN_AUTH_SUCCEEDED = 2;
    final private static int MSG_GAIN_AUTH_FAILED = 3;

    final private Handler handler;
    private ProgressDialog authPendingDialog;
    private String user;
    private String pass;

    @SuppressLint("HandlerLeak")
    public LoginViewCtrl(final DataMgr dataMgr, final Context context) {
        super(dataMgr, R.layout.login, context);

        this.handler = new Handler() {
            @Override
            public void handleMessage(final Message msg) {
                switch (msg.what) {
                case MSG_LOGIN_SUCCEEDED:
                    ReaderAccountMgr.getInstance().setClientLogin(user, pass);
                    Toast.makeText(context, R.string.MsgLoginSucceeded, Toast.LENGTH_LONG).show();
                    if (listener != null) {
                        listener.onLogin(true);
                    }
                    break;
                case MSG_LOGIN_FAILED:
                    Toast.makeText(context, R.string.MsgLoginFailed, Toast.LENGTH_LONG).show();
                    final View btnLogin = view.findViewById(R.id.BtnLogin);
                    btnLogin.setEnabled(true);
                    if (listener != null) {
                        listener.onLogin(false);
                    }
                    break;
                case MSG_GAIN_AUTH_SUCCEEDED:
                    Toast.makeText(context, R.string.MsgAuthenticationSucceeded, Toast.LENGTH_LONG).show();
                    ReaderAccountMgr.getInstance().setNonClientLogin(user);
                    if (authPendingDialog != null) {
                        authPendingDialog.dismiss();
                    }
                    if (listener != null) {
                        listener.onLogin(true);
                    }
                    break;
                case MSG_GAIN_AUTH_FAILED:
                    Toast.makeText(context, R.string.MsgAuthenticationFailed, Toast.LENGTH_LONG).show();
                    if (authPendingDialog != null) {
                        authPendingDialog.dismiss();
                    }
                    if (listener != null) {
                        listener.onLogin(false);
                    }
                    break;
                default:
                }
            }
        };
    }

    @Override
    public void onActivate() {
        // TODO empty method
    }

    @Override
    public void onAuthUpdateFinished(boolean succeeded) {
        if (pass != null) {
            if (succeeded) {
                handler.sendEmptyMessage(MSG_LOGIN_SUCCEEDED);
            } else {
                handler.sendEmptyMessage(MSG_LOGIN_FAILED);
            }
        }
    }

    @Override
    public void onCreate() {
        NetworkMgr.getInstance().addListener(this);
        ReaderAccountMgr.getInstance().setListener(this);

        final View btnLogin = view.findViewById(R.id.BtnLogin);
        btnLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                view.setEnabled(false);
                user = ((EditText) LoginViewCtrl.this.view.findViewById(R.id.TxtUsername)).getText().toString();
                pass = ((EditText) LoginViewCtrl.this.view.findViewById(R.id.TxtPassword)).getText().toString();
                ReaderAccountMgr.getInstance().tryClientLogin(user, pass);
                Toast.makeText(context, R.string.MsgLogging, Toast.LENGTH_LONG).show();
            }
        });

        final View btnShowAccounts = view.findViewById(R.id.BtnShowAccounts);
        final AccountManager accMgr = AccountManager.get(context);
        final Account[] accounts = accMgr.getAccountsByType(ReaderAccountMgr.ACCOUNT_TYPE);
        if (accounts.length > 0) {
            btnShowAccounts.setEnabled(true);
            btnShowAccounts.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(final View view) {
                    final CharSequence[] items = new String[accounts.length];
                    for (int i = 0; i < accounts.length; i++) {
                        items[i] = accounts[i].name;
                    }
                    final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context,
                            android.R.style.Theme_DeviceDefault_Dialog));
                    builder.setTitle(R.string.TxtChooseYourAccount);
                    builder.setNegativeButton(context.getString(R.string.TxtCancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            authPendingDialog = ProgressDialog.show(new ContextThemeWrapper(context,
                                    android.R.style.Theme_DeviceDefault_Dialog),
                                    context.getString(R.string.TxtWorking), context
                                            .getString(R.string.TxtWaitingForAuthentication));
                            user = items[id].toString();
                            pass = null;
                            ReaderAccountMgr.getInstance().tryNonClicentLogin(user);

                            final Thread thread = new Thread() {
                                final private static int WAIT_LIMIT = 60;

                                @Override
                                public void run() {
                                    int times = 0;
                                    final ReaderAccountMgr accMgr = ReaderAccountMgr.getInstance();
                                    while (times < WAIT_LIMIT) {
                                        try {
                                            sleep(500);
                                        } catch (final InterruptedException exception) {
                                            exception.printStackTrace();
                                        }
                                        accMgr.refreshAuthState(user);
                                        if (accMgr.isAuthValid()) {
                                            break;
                                        }
                                        times++;
                                    }
                                    if (times < WAIT_LIMIT) {
                                        handler.sendEmptyMessage(MSG_GAIN_AUTH_SUCCEEDED);
                                    } else {
                                        handler.sendEmptyMessage(MSG_GAIN_AUTH_FAILED);
                                    }
                                }
                            };
                            thread.setPriority(Thread.MIN_PRIORITY);
                            thread.start();
                        }
                    });
                    builder.show();
                }
            });
        } else {
            btnShowAccounts.setEnabled(false);
        }
    }

    @Override
    public void onDeactivate() {
        // TODO empty method
    }

    @Override
    public void onDestory() {
        NetworkMgr.getInstance().removeListener(this);
        ReaderAccountMgr.getInstance().setListener(null);
    }
}
