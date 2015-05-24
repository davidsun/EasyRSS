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

package org.freshrss.easyrss;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.Toast;

import org.freshrss.easyrss.R;
import org.freshrss.easyrss.account.ReaderAccountMgr;
import org.freshrss.easyrss.account.ReaderAccountMgrListener;
import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.network.NetworkMgr;
import org.freshrss.easyrss.network.url.AbsURL;
import org.freshrss.easyrss.view.AbsViewCtrl;

public class LoginViewCtrl extends AbsViewCtrl implements ReaderAccountMgrListener {
    final private static int MSG_LOGIN_SUCCEEDED = 0;
    final private static int MSG_LOGIN_FAILED = 1;
    final private static int MSG_GAIN_AUTH_SUCCEEDED = 2;
    final private static int MSG_GAIN_AUTH_FAILED = 3;

    final private Handler handler;
    private ProgressDialog authPendingDialog;
    private String serverUrl;
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
                    ReaderAccountMgr.getInstance().setClientLogin(serverUrl, user, pass);
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
                serverUrl = ((EditText) LoginViewCtrl.this.view.findViewById(R.id.TxtServerUrl)).getText().toString();
                AbsURL.setServerUrl(serverUrl);
                user = ((EditText) LoginViewCtrl.this.view.findViewById(R.id.TxtUsername)).getText().toString();
                pass = ((EditText) LoginViewCtrl.this.view.findViewById(R.id.TxtPassword)).getText().toString();
                ReaderAccountMgr.getInstance().tryClientLogin(user, pass);
                Toast.makeText(context, R.string.MsgLogging, Toast.LENGTH_LONG).show();
            }
        });
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
