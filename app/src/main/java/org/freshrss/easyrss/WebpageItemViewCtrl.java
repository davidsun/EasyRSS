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

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.http.protocol.HTTP;
import org.freshrss.easyrss.R;
import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.data.DataUtils;
import org.freshrss.easyrss.data.Item;
import org.freshrss.easyrss.data.Setting;
import org.freshrss.easyrss.data.readersetting.SettingFontSize;
import org.freshrss.easyrss.data.readersetting.SettingTheme;
import org.freshrss.easyrss.view.AbsViewCtrl;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;


public class WebpageItemViewCtrl extends AbsViewCtrl {
    final static private Handler handler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.obj instanceof WebpageItemViewCtrl) {
                final WebpageItemViewCtrl viewCtrl = (WebpageItemViewCtrl) msg.obj;
                switch (msg.what) {
                case MSG_LOADING_FINISHED:
                    if (viewCtrl.showMobilized) {
                        viewCtrl.view.findViewById(R.id.MobilizedProgress).setVisibility(View.GONE);
                        viewCtrl.view.findViewById(R.id.MobilizedContent).setVisibility(View.VISIBLE);
                        viewCtrl.mobilizedView.loadDataWithBaseURL(null, viewCtrl.pageContent, "text/html", "utf-8",
                                null);
                    }
                    break;
                default:
                    break;
                }
            }
        }
    };
    final private static String ITEM_PROJECTION[] = new String[] { Item._UID, Item._TITLE, Item._TIMESTAMP,
            Item._SOURCETITLE, Item._AUTHOR, Item._HREF };
    final private static int MSG_LOADING_FINISHED = 0;

    private static String genFailedToLoadContentPage(final Context context, final int theme) {
        final StringBuilder builder = new StringBuilder();
        builder.append("<div>");
        builder.append(context.getString(R.string.MsgFailedToLoadContent));
        builder.append("</div>");
        builder.append(theme == SettingTheme.THEME_NORMAL ? DataUtils.DEFAULT_NORMAL_CSS : DataUtils.DEFAULT_DARK_CSS);
        return builder.toString();
    }

    private final int fontSize;
    private final Item item;
    private final WebView mobilizedView;
    private final WebView originalView;
    private String pageContent;
    private boolean showMobilized;
    private final int theme;
    private Thread thread;

    @SuppressLint("SetJavaScriptEnabled")
    public WebpageItemViewCtrl(final DataMgr dataMgr, final Context context, final String uid, final boolean isMobilized) {
        super(dataMgr, R.layout.webpage_item, context);

        this.item = dataMgr.getItemByUid(uid, ITEM_PROJECTION);
        this.theme = new SettingTheme(dataMgr).getData();
        this.fontSize = new SettingFontSize(dataMgr).getData();
        // Disable hardware acceleration on Android 3.0-4.1 devices.
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR1
                && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        {
            mobilizedView = (WebView) view.findViewById(R.id.MobilizedContent);
            mobilizedView.setBackgroundColor(context.getResources().getColor(
                    theme == SettingTheme.THEME_NORMAL ? R.color.NormalBackground : R.color.DarkBackground));
            mobilizedView.setFocusable(false);
            final WebSettings settings = mobilizedView.getSettings();
            settings.setDefaultTextEncodingName(HTTP.UTF_8);
            settings.setJavaScriptEnabled(false);
            settings.setDefaultFontSize(fontSize);
        }
        {
            originalView = (WebView) view.findViewById(R.id.OriginalContent);
            originalView.setFocusable(false);
            originalView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
            final WebSettings settings = originalView.getSettings();
            settings.setJavaScriptEnabled(true);
            settings.setSupportZoom(true);
            settings.setBuiltInZoomControls(true);
            originalView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageFinished(final WebView webView, final String url) {
                    view.findViewById(R.id.OriginalProgress).setVisibility(View.GONE);
                }

                @Override
                public void onPageStarted(final WebView webView, final String url, final Bitmap favicon) {
                    if (!showMobilized) {
                        view.findViewById(R.id.OriginalProgress).setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public boolean shouldOverrideUrlLoading(final WebView webView, final String url) {
                    webView.loadUrl(url);
                    return false;
                }
            });
        }

        if (isMobilized) {
            showMobilizedPage();
        } else {
            showOriginalPage();
        }

        view.findViewById(R.id.BtnMobilzedPage).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                showMobilizedPage();
            }
        });
        view.findViewById(R.id.BtnOriginalPage).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                showOriginalPage();
            }
        });
        view.findViewById(R.id.BtnClose).setOnClickListener(new OnClickListener() {
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
        // TODO Auto-generated method stub
    }

    @Override
    public void onDeactivate() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onDestory() {
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
        originalView.stopLoading();
    }

    @SuppressLint("SimpleDateFormat")
    private void showMobilizedPage() {
        showMobilized = true;
        view.findViewById(R.id.Mobilized).setVisibility(View.VISIBLE);
        view.findViewById(R.id.OriginalContent).setVisibility(View.GONE);
        view.findViewById(R.id.BtnMobilzedPage).setEnabled(false);
        view.findViewById(R.id.BtnOriginalPage).setEnabled(true);
        view.findViewById(R.id.OriginalProgress).setVisibility(View.GONE);
        view.findViewById(R.id.MobilizedProgress).setVisibility(View.VISIBLE);
        view.findViewById(R.id.MobilizedContent).setVisibility(View.GONE);
        final TextView title = (TextView) view.findViewById(R.id.ItemTitle);
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize * 4 / 3);
        title.setText(item.getTitle());
        final TextView info = (TextView) view.findViewById(R.id.ItemInfo);
        info.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize * 4 / 5);
        final StringBuilder infoText = new StringBuilder();
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        sdf.setTimeZone(TimeZone.getDefault());
        infoText.append(sdf.format(Utils.timestampToDate(item.getTimestamp())));
        if (item.getAuthor() != null && item.getAuthor().length() > 0) {
            infoText.append(" | By ");
            infoText.append(item.getAuthor());
        }
        if (item.getSourceTitle() != null && item.getSourceTitle().length() > 0) {
            infoText.append(" (");
            infoText.append(item.getSourceTitle());
            infoText.append(")");
        }
        info.setText(infoText);
        if (thread == null) {
            thread = new Thread(new Runnable() {
                @SuppressWarnings("deprecation")
                @Override
                public void run() {
                    try {
                        final StringBuilder urlBuilder = new StringBuilder();
                        urlBuilder.append("http://easyrss.pursuer.me/parser?url=");
                        urlBuilder.append(URLEncoder.encode(item.getHref()));
                        urlBuilder.append("&email=");
                        urlBuilder.append(URLEncoder.encode(dataMgr.getSettingByName(Setting.SETTING_USERNAME)));
                        urlBuilder.append("&version=");
                        urlBuilder.append(context.getString(R.string.Version));
                        final URLConnection connection = new URL(urlBuilder.toString()).openConnection();
                        connection.setConnectTimeout(30 * 1000);
                        connection.setReadTimeout(20 * 1000);
                        final InputStreamReader input = new InputStreamReader(connection.getInputStream());
                        final StringBuilder builder = new StringBuilder();
                        final char buff[] = new char[8192];
                        int len;
                        while ((len = input.read(buff)) != -1) {
                            builder.append(new String(buff, 0, len));
                        }
                        builder.append(theme == SettingTheme.THEME_NORMAL ? DataUtils.DEFAULT_NORMAL_CSS
                                : DataUtils.DEFAULT_DARK_CSS);
                        pageContent = builder.toString();
                        handler.sendMessage(handler.obtainMessage(MSG_LOADING_FINISHED, WebpageItemViewCtrl.this));
                    } catch (final MalformedURLException exception) {
                        exception.printStackTrace();
                        pageContent = genFailedToLoadContentPage(context, theme);
                        handler.sendMessage(handler.obtainMessage(MSG_LOADING_FINISHED, WebpageItemViewCtrl.this));
                    } catch (final IOException exception) {
                        exception.printStackTrace();
                        pageContent = genFailedToLoadContentPage(context, theme);
                        handler.sendMessage(handler.obtainMessage(MSG_LOADING_FINISHED, WebpageItemViewCtrl.this));
                    }
                }
            });
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        } else if (!thread.isAlive()) {
            handler.sendMessage(handler.obtainMessage(MSG_LOADING_FINISHED, WebpageItemViewCtrl.this));
        }
    }

    private void showOriginalPage() {
        showMobilized = false;
        view.findViewById(R.id.Mobilized).setVisibility(View.GONE);
        view.findViewById(R.id.OriginalContent).setVisibility(View.VISIBLE);
        view.findViewById(R.id.BtnMobilzedPage).setEnabled(true);
        view.findViewById(R.id.BtnOriginalPage).setEnabled(false);
        originalView.loadUrl(item.getHref());
    }
}
