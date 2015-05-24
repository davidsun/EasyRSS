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

import java.io.File;

import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.data.DataUtils;
import org.freshrss.easyrss.data.OnSettingUpdatedListener;
import org.freshrss.easyrss.data.Setting;
import org.freshrss.easyrss.data.readersetting.SettingBrowserChoice;
import org.freshrss.easyrss.data.readersetting.SettingDescendingItemsOrdering;
import org.freshrss.easyrss.data.readersetting.SettingFontSize;
import org.freshrss.easyrss.data.readersetting.SettingHttpsConnection;
import org.freshrss.easyrss.data.readersetting.SettingImageFetching;
import org.freshrss.easyrss.data.readersetting.SettingImagePrefetching;
import org.freshrss.easyrss.data.readersetting.SettingImmediateStateSyncing;
import org.freshrss.easyrss.data.readersetting.SettingMarkAllAsReadConfirmation;
import org.freshrss.easyrss.data.readersetting.SettingMaxItems;
import org.freshrss.easyrss.data.readersetting.SettingNotificationOn;
import org.freshrss.easyrss.data.readersetting.SettingSyncInterval;
import org.freshrss.easyrss.data.readersetting.SettingSyncMethod;
import org.freshrss.easyrss.data.readersetting.SettingTheme;
import org.freshrss.easyrss.data.readersetting.SettingVolumeKeySwitching;
import org.freshrss.easyrss.view.AbsViewCtrl;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Message;
import android.view.ContextThemeWrapper;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsViewCtrl extends AbsViewCtrl implements OnSettingUpdatedListener {

    public SettingsViewCtrl(final DataMgr dataMgr, final Context context) {
        super(dataMgr, R.layout.settings, context);

        showSettingSync();
        showSettingMaxItems();
        showSettingImageFetching();
        showSettingImagePrefetching();
        showSettingImmediateStateSyncing();
        showSettingSyncInterval();
        showSettingHttpsConnection();
        showSettingFontSize();
        showSettingTheme();
        showSettingVolumnKeySwitching();
        showSettingDecendingItemsOrdering();
        showSettingNotificationOn();
        showSettingMarkAllAsReadConfirmation();
        showSettingAboutEasyRSS();
        showSettingAboutAuthor();
        showSettingBrowserChoice();

        final View btnCal = view.findViewById(R.id.BtnCalculation);
        btnCal.setOnClickListener(new OnClickListener() {
            @SuppressLint("HandlerLeak")
            @Override
            public void onClick(final View view) {
                final ProgressDialog dialog = ProgressDialog.show(context, context.getString(R.string.TxtWorking),
                        context.getString(R.string.TxtCalculating));
                final Handler handler = new Handler() {
                    @Override
                    public void handleMessage(final Message msg) {
                        final TextView txtSpace = (TextView) SettingsViewCtrl.this.view.findViewById(R.id.TxtSpace);
                        final float mbSize = Math.round((float) msg.what * 100.0f / 1024.0f / 1024.0f) / 100.0f;
                        final File file = new File(DataUtils.getAppFolderPath());
                        final int fileCount = file.exists() ? file.listFiles().length : 0;
                        txtSpace.setText(fileCount + " " + context.getString(R.string.TxtEntries) + ", " + mbSize
                                + " MB");
                        dialog.dismiss();
                    }
                };
                final Thread thread = new Thread() {
                    @Override
                    public void run() {
                        final long size = DataUtils.calcFileSpace(new File(DataUtils.getAppFolderPath()));
                        handler.sendEmptyMessage((int) size);
                    }
                };
                thread.setPriority(Thread.MIN_PRIORITY);
                thread.start();
            }
        });

        final View btnClose = this.view.findViewById(R.id.BtnClose);
        btnClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (listener != null) {
                    listener.onBackNeeded();
                }
            }
        });
    }

    @Override
    public void onActivate() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onCreate() {
        dataMgr.addOnSettingUpdatedListener(this);
    }

    @Override
    public void onDeactivate() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onDestory() {
        dataMgr.removeOnSettingUpdatedListener(this);
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onSettingUpdated(final String name) {
        if (name.equals(Setting.SETTING_SYNC_METHOD)) {
            showSettingSync();
        } else if (name.equals(Setting.SETTING_IMAGE_FETCHING)) {
            showSettingImageFetching();
        } else if (name.equals(Setting.SETTING_MAX_ITEMS)) {
            showSettingMaxItems();
            final SettingMaxItems sMaxItems = new SettingMaxItems(dataMgr);
            final ProgressDialog dialog = ProgressDialog.show(context, context.getString(R.string.TxtWorking),
                    context.getString(R.string.TxtRemovingOutdatedItems));
            final Handler handler = new Handler() {
                @Override
                public void handleMessage(final Message msg) {
                    if (msg.what == 0) {
                        dialog.dismiss();
                    }
                }
            };
            final Thread thread = new Thread() {
                @Override
                public void run() {
                    dataMgr.removeOutdatedItemsWithLimit(sMaxItems.getData());
                    handler.sendEmptyMessage(0);
                }
            };
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        } else if (name.equals(Setting.SETTING_FONT_SIZE)) {
            showSettingFontSize();
        }
    }

    private void showSettingAboutAuthor() {
        final View settingAbout = view.findViewById(R.id.SettingAboutAuthor);
        settingAbout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.setPressed(true);
                    break;
                case MotionEvent.ACTION_UP:
                    view.setPressed(false);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context,
                            android.R.style.Theme_DeviceDefault_Dialog));
                    builder.setMessage(R.string.TxtAboutAuthorIntro);
                    builder.setNegativeButton(context.getString(R.string.TxtCancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.show();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    view.setPressed(false);
                    break;
                default:
                }
                return true;
            }
        });
    }

    private void showSettingAboutEasyRSS() {
        try {
            final PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            ((TextView) view.findViewById(R.id.TxtSettingVersionNumber)).setText(context
                    .getText(R.string.TxtCurrentVersion)
                    + ": "
                    + context.getString(R.string.Version)
                    + " ("
                    + info.versionCode + ")");
        } catch (final NameNotFoundException exception) {
            exception.printStackTrace();
        }

        final View settingAbout = view.findViewById(R.id.SettingAboutEasyRSS);
        settingAbout.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.setPressed(true);
                    break;
                case MotionEvent.ACTION_UP:
                    view.setPressed(false);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context,
                            android.R.style.Theme_DeviceDefault_Dialog));
                    builder.setMessage(R.string.TxtAboutEasyRSSIntro);
                    builder.setNegativeButton(context.getString(R.string.TxtCancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.show();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    view.setPressed(false);
                    break;
                default:
                }
                return true;
            }
        });
    }

    private void showSettingBrowserChoice() {
        final SettingBrowserChoice setting = new SettingBrowserChoice(dataMgr);
        final LinearLayout settingBrowserChoice = (LinearLayout) view.findViewById(R.id.SettingBrowserChoice);
        if (settingBrowserChoice != null) {
            settingBrowserChoice.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(final View view, final MotionEvent event) {
                    switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        view.setPressed(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        view.setPressed(false);
                        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context,
                                android.R.style.Theme_DeviceDefault_Dialog));
                        final String items[] = new String[4];
                        items[0] = context.getString(R.string.TxtChooseEverytime);
                        items[1] = context.getString(R.string.TxtBuiltInMobilized);
                        items[2] = context.getString(R.string.TxtBuiltInOriginal);
                        items[3] = context.getString(R.string.TxtExternalOriginal);
                        builder.setTitle(R.string.TxtSettingBrowserChoice);
                        builder.setSingleChoiceItems(items, setting.getData(), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                setting.setData(dataMgr, which);
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton(context.getString(R.string.TxtCancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int which) {
                                        dialog.dismiss();
                                    }
                                });
                        builder.show();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        view.setPressed(false);
                        break;
                    default:
                    }
                    return true;
                }
            });
        }
    }

    private void showSettingDecendingItemsOrdering() {
        final ImageView img = (ImageView) view.findViewById(R.id.SwitchDecendingItemsOrdering);
        final SettingDescendingItemsOrdering sOrdering = new SettingDescendingItemsOrdering(dataMgr);
        img.setImageResource(sOrdering.getData() ? R.drawable.switch_on : R.drawable.switch_off);
        img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                sOrdering.setData(dataMgr, !sOrdering.getData());
                dataMgr.updateSetting(sOrdering.toSetting());
                showSettingDecendingItemsOrdering();
            }
        });
    }

    private void showSettingSyncInterval() {
        final TextView txt = (TextView) view.findViewById(R.id.TxtSyncInterval);
        final SettingSyncInterval setting = new SettingSyncInterval(dataMgr);
        txt.setText(setting.toSeconds() / 3600 + "H");

        final View settingFontSize = view.findViewById(R.id.SettingSyncInterval);
        settingFontSize.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.setPressed(true);
                    break;
                case MotionEvent.ACTION_UP:
                    view.setPressed(false);
                    final String[] sItems = context.getResources().getStringArray(R.array.SettingSyncInterval);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context,
                            android.R.style.Theme_DeviceDefault_Dialog));
                    builder.setTitle(context.getString(R.string.TxtSettingSyncInterval));
                    builder.setSingleChoiceItems(sItems, setting.getData(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int id) {
                            setting.setData(dataMgr, id);
                            showSettingSyncInterval();
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(context.getString(R.string.TxtCancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.show();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    view.setPressed(false);
                    break;
                default:
                }
                return true;
            }
        });
    }

    private void showSettingFontSize() {
        final TextView txt = (TextView) view.findViewById(R.id.TxtFontSize);
        final SettingFontSize setting = new SettingFontSize(dataMgr);
        txt.setText(setting.getData().toString());

        final View settingFontSize = view.findViewById(R.id.SettingFontSize);
        settingFontSize.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.setPressed(true);
                    break;
                case MotionEvent.ACTION_UP:
                    view.setPressed(false);
                    final String[] sItems = context.getResources().getStringArray(R.array.SettingFontSize);
                    int selId;
                    for (selId = 0; selId < sItems.length && !setting.getData().equals(Integer.valueOf(sItems[selId])); selId++) {
                    }
                    final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context,
                            android.R.style.Theme_DeviceDefault_Dialog));
                    builder.setTitle(context.getString(R.string.TxtSettingFontSize));
                    builder.setSingleChoiceItems(sItems, selId, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int id) {
                            setting.setData(dataMgr,
                                    Integer.valueOf(context.getResources().getStringArray(R.array.SettingFontSize)[id]));
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(context.getString(R.string.TxtCancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.show();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    view.setPressed(false);
                    break;
                default:
                }
                return true;
            }
        });
    }

    private void showSettingHttpsConnection() {
        final ImageView img = (ImageView) view.findViewById(R.id.SwitchHttpsConnection);
        final SettingHttpsConnection sHttps = new SettingHttpsConnection(dataMgr);
        img.setImageResource(sHttps.getData() ? R.drawable.switch_on : R.drawable.switch_off);
        img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                sHttps.setData(dataMgr, !sHttps.getData());
                dataMgr.updateSetting(sHttps.toSetting());
                showSettingHttpsConnection();
            }
        });
    }

    private void showSettingImageFetching() {
        final ImageView img = (ImageView) view.findViewById(R.id.SettingImageFetchingStatus);
        final SettingImageFetching sFetch = new SettingImageFetching(dataMgr);
        switch (sFetch.getData()) {
        case SettingImageFetching.FETCH_METHOD_DISABLED:
            img.setImageResource(R.drawable.setting_disabled);
            break;
        case SettingImageFetching.FETCH_METHOD_NETWORK:
            img.setImageResource(R.drawable.setting_network);
            break;
        case SettingImageFetching.FETCH_METHOD_WIFI:
            img.setImageResource(R.drawable.setting_wifi);
            break;
        default:
        }

        final LinearLayout settingSync = (LinearLayout) view.findViewById(R.id.SettingImageFetching);
        if (settingSync != null) {
            settingSync.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(final View view, final MotionEvent event) {
                    switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        view.setPressed(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        view.setPressed(false);
                        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context,
                                android.R.style.Theme_DeviceDefault_Dialog));
                        builder.setTitle(context.getString(R.string.TxtSettingImageFetching));
                        builder.setSingleChoiceItems(context.getResources()
                                .getStringArray(R.array.SettingImageFetching), sFetch.getData(),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dialog, final int id) {
                                        sFetch.setData(dataMgr, id);
                                        dialog.dismiss();
                                    }
                                });
                        builder.setNegativeButton(context.getString(R.string.TxtCancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int which) {
                                        dialog.dismiss();
                                    }
                                });
                        builder.show();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        view.setPressed(false);
                        break;
                    default:
                    }
                    return true;
                }
            });
        }
    }

    private void showSettingImagePrefetching() {
        final ImageView img = (ImageView) view.findViewById(R.id.SwitchImageFetching);
        if (img == null) {
            return;
        }
        final SettingImagePrefetching sImgDown = new SettingImagePrefetching(dataMgr);
        img.setImageResource(sImgDown.getData() ? R.drawable.switch_on : R.drawable.switch_off);
        img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                sImgDown.setData(dataMgr, !sImgDown.getData());
                dataMgr.updateSetting(sImgDown.toSetting());
                showSettingImagePrefetching();
            }
        });
    }

    private void showSettingImmediateStateSyncing() {
        final ImageView img = (ImageView) view.findViewById(R.id.SwitchImmediateStateSyncing);
        if (img == null) {
            return;
        }
        final SettingImmediateStateSyncing sStateSyncing = new SettingImmediateStateSyncing(dataMgr);
        img.setImageResource(sStateSyncing.getData() ? R.drawable.switch_on : R.drawable.switch_off);
        img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                sStateSyncing.setData(dataMgr, !sStateSyncing.getData());
                dataMgr.updateSetting(sStateSyncing.toSetting());
                showSettingImmediateStateSyncing();
            }
        });
    }

    private void showSettingMarkAllAsReadConfirmation() {
        final ImageView img = (ImageView) view.findViewById(R.id.SwitchMarkAllAsReadConfirmation);
        final SettingMarkAllAsReadConfirmation sMark = new SettingMarkAllAsReadConfirmation(dataMgr);
        img.setImageResource(sMark.getData() ? R.drawable.switch_on : R.drawable.switch_off);
        img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                sMark.setData(dataMgr, !sMark.getData());
                dataMgr.updateSetting(sMark.toSetting());
                showSettingMarkAllAsReadConfirmation();
            }
        });
    }

    private void showSettingMaxItems() {
        final TextView txt = (TextView) view.findViewById(R.id.SettingMaxItemsCount);
        final SettingMaxItems sMaxItems = new SettingMaxItems(dataMgr);
        txt.setText(sMaxItems.getData().toString());

        final LinearLayout settingMaxItems = (LinearLayout) view.findViewById(R.id.SettingMaxItems);
        if (settingMaxItems != null) {
            settingMaxItems.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(final View view, final MotionEvent event) {
                    switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        view.setPressed(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        view.setPressed(false);
                        final String[] sItems = context.getResources().getStringArray(R.array.SettingMaxItems);
                        int selId;
                        for (selId = 0; selId < sItems.length
                                && !sMaxItems.getData().equals(Integer.valueOf(sItems[selId])); selId++) {
                        }
                        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context,
                                android.R.style.Theme_DeviceDefault_Dialog));
                        builder.setTitle(context.getString(R.string.TxtSettingMaxNoOfItems));
                        builder.setSingleChoiceItems(sItems, selId, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int id) {
                                sMaxItems.setData(dataMgr, Integer.valueOf(context.getResources().getStringArray(
                                        R.array.SettingMaxItems)[id]));
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton(context.getString(R.string.TxtCancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int which) {
                                        dialog.dismiss();
                                    }
                                });
                        builder.show();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        view.setPressed(false);
                        break;
                    default:
                    }
                    return true;
                }
            });
        }
    }

    private void showSettingNotificationOn() {
        final ImageView img = (ImageView) view.findViewById(R.id.SwitchNotificationOn);
        final SettingNotificationOn sNotification = new SettingNotificationOn(dataMgr);
        img.setImageResource(sNotification.getData() ? R.drawable.switch_on : R.drawable.switch_off);
        img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                sNotification.setData(dataMgr, !sNotification.getData());
                dataMgr.updateSetting(sNotification.toSetting());
                showSettingNotificationOn();
            }
        });
    }

    private void showSettingSync() {
        final ImageView img = (ImageView) view.findViewById(R.id.SettingSyncStatus);
        final SettingSyncMethod sSync = new SettingSyncMethod(dataMgr);
        switch (sSync.getData()) {
        case SettingSyncMethod.SYNC_METHOD_MANUAL:
            img.setImageResource(R.drawable.setting_manual);
            break;
        case SettingSyncMethod.SYNC_METHOD_NETWORK:
            img.setImageResource(R.drawable.setting_network);
            break;
        case SettingSyncMethod.SYNC_METHOD_WIFI:
            img.setImageResource(R.drawable.setting_wifi);
            break;
        default:
        }

        final LinearLayout settingSync = (LinearLayout) view.findViewById(R.id.SettingItemSync);
        if (settingSync != null) {
            settingSync.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(final View view, final MotionEvent event) {
                    switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        view.setPressed(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        view.setPressed(false);
                        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context,
                                android.R.style.Theme_DeviceDefault_Dialog));
                        builder.setTitle(context.getString(R.string.TxtSettingAutoSync));
                        builder.setSingleChoiceItems(context.getResources().getStringArray(R.array.SettingItemSync),
                                sSync.getData(), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(final DialogInterface dialog, final int id) {
                                        sSync.setData(dataMgr, id);
                                        dialog.dismiss();
                                    }
                                });
                        builder.setNegativeButton(context.getString(R.string.TxtCancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(final DialogInterface dialog, final int which) {
                                        dialog.dismiss();
                                    }
                                });
                        builder.show();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        view.setPressed(false);
                        break;
                    default:
                    }
                    return true;
                }
            });
        }
    }

    public void showSettingTheme() {
        final TextView txt = (TextView) view.findViewById(R.id.TxtTheme);
        final SettingTheme setting = new SettingTheme(dataMgr);
        if (setting.getData() == SettingTheme.THEME_NORMAL) {
            txt.setText(R.string.TxtThemeNormal);
        } else {
            txt.setText(R.string.TxtThemeDark);
        }
        final View settingTheme = view.findViewById(R.id.SettingTheme);
        settingTheme.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.setPressed(true);
                    break;
                case MotionEvent.ACTION_UP:
                    view.setPressed(false);
                    final String[] sItems = context.getResources().getStringArray(R.array.SettingTheme);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context,
                            android.R.style.Theme_DeviceDefault_Dialog));
                    builder.setTitle(context.getString(R.string.TxtSettingTheme));
                    builder.setSingleChoiceItems(sItems, setting.getData(), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int id) {
                            setting.setData(dataMgr, id);
                            if (listener != null) {
                                listener.onReloadRequired(true);
                            }
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton(context.getString(R.string.TxtCancel),
                            new DialogInterface.OnClickListener() {
                                public void onClick(final DialogInterface dialog, final int which) {
                                    dialog.dismiss();
                                }
                            });
                    builder.show();
                    break;
                case MotionEvent.ACTION_CANCEL:
                    view.setPressed(false);
                    break;
                default:
                }
                return true;
            }
        });
    }

    private void showSettingVolumnKeySwitching() {
        final ImageView img = (ImageView) view.findViewById(R.id.SwitchVolumnKeySwitching);
        final SettingVolumeKeySwitching sOrdering = new SettingVolumeKeySwitching(dataMgr);
        img.setImageResource(sOrdering.getData() ? R.drawable.switch_on : R.drawable.switch_off);
        img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                sOrdering.setData(dataMgr, !sOrdering.getData());
                dataMgr.updateSetting(sOrdering.toSetting());
                showSettingVolumnKeySwitching();
            }
        });
    }
}
