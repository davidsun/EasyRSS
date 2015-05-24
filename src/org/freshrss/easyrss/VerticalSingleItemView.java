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
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.apache.http.protocol.HTTP;

import org.freshrss.easyrss.R;
import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.data.DataUtils;
import org.freshrss.easyrss.data.Item;
import org.freshrss.easyrss.data.ItemState;
import org.freshrss.easyrss.data.readersetting.SettingBrowserChoice;
import org.freshrss.easyrss.data.readersetting.SettingFontSize;
import org.freshrss.easyrss.data.readersetting.SettingImageFetching;
import org.freshrss.easyrss.data.readersetting.SettingTheme;
import org.freshrss.easyrss.listadapter.ListItemItem;
import org.freshrss.easyrss.network.NetworkMgr;
import org.freshrss.easyrss.network.NetworkUtils;
import org.freshrss.easyrss.view.OnScrollChangedListener;
import org.freshrss.easyrss.view.OverScrollView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class VerticalSingleItemView implements OnScrollChangedListener, OnTouchListener {
    final private static String ITEM_PROJECTION[] = new String[] { Item._UID, Item._TITLE, ItemState._ISCACHED,
            ItemState._ISREAD, ItemState._ISSTARRED, Item._TIMESTAMP, Item._SOURCETITLE, Item._AUTHOR, Item._HREF };

    final private DataMgr dataMgr;
    final private Context context;
    final private View view;
    final private View lastItemView;
    final private View nextItemView;
    final private View menu;
    final private Item item;
    final private String lastUid;
    final private String nextUid;
    final private WebView webView;
    final private OverScrollView itemScrollView;
    final private VerticalItemViewCtrl itemViewCtrl;
    final private int theme;
    final private int fontSize;
    private VerticalSingleItemViewListener listener;
    private boolean showTop;
    private boolean showBottom;
    private long imageClickTime;

    @SuppressLint({ "NewApi", "SimpleDateFormat" })
    public VerticalSingleItemView(final DataMgr dataMgr, final Context context, final String uid, final View menu,
            final VerticalItemViewCtrl itemViewCtrl) {
        this.dataMgr = dataMgr;
        this.context = context;
        this.item = dataMgr.getItemByUid(uid, ITEM_PROJECTION);
        this.fontSize = new SettingFontSize(dataMgr).getData();
        this.theme = new SettingTheme(dataMgr).getData();
        this.showTop = false;
        this.showBottom = false;
        this.menu = menu;
        this.itemViewCtrl = itemViewCtrl;
        this.imageClickTime = 0;

        final LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.view = inflater.inflate(R.layout.single_item_view, null);
        // Disable hardware acceleration on Android 3.0-4.1 devices.
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR1
                && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            view.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }

        lastItemView = view.findViewById(R.id.LastItemView);
        nextItemView = view.findViewById(R.id.NextItemView);
        itemScrollView = (OverScrollView) view.findViewById(R.id.ItemScrollView);
        itemScrollView.setTopScrollView(lastItemView);
        itemScrollView.setBottomScrollView(nextItemView);
        itemScrollView.setOnScrollChangeListener(this);
        itemScrollView.setOnTouchListener(this);

        final View btnShowOriginal = view.findViewById(R.id.BtnShowOriginal);
        btnShowOriginal.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                boolean ret = false;
                final TextView txt = (TextView) view.findViewById(R.id.BtnText);
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    txt.setTextColor(0xFF787878);
                    ret = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    ret = true;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    txt.setTextColor(0xBB787878);
                    ret = false;
                    break;
                case MotionEvent.ACTION_UP:
                    txt.setTextColor(0xBB787878);
                    final SettingBrowserChoice setting = new SettingBrowserChoice(dataMgr);
                    if (setting.getData() == SettingBrowserChoice.BROWSER_CHOICE_EXTERNAL) {
                        final Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.parse(item.getHref()));
                        context.startActivity(intent);
                    } else if (VerticalSingleItemView.this.listener != null) {
                        VerticalSingleItemView.this.listener.showWebsitePage(item.getUid(), false);
                    }
                    break;
                default:
                }
                return ret;
            }
        });

        final View btnShowMobilized = view.findViewById(R.id.BtnShowMobilized);
        btnShowMobilized.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                boolean ret = false;
                final TextView txt = (TextView) view.findViewById(R.id.BtnText);
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    txt.setTextColor(0xFF787878);
                    ret = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    ret = true;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    txt.setTextColor(0xBB787878);
                    ret = false;
                    break;
                case MotionEvent.ACTION_UP:
                    txt.setTextColor(0xBB787878);
                    if (VerticalSingleItemView.this.listener != null) {
                        VerticalSingleItemView.this.listener.showWebsitePage(item.getUid(), true);
                    }
                    ret = true;
                    break;
                default:
                }
                return ret;
            }
        });

        {
            final ListItemItem lastItem = itemViewCtrl.getLastItem(item.getUid());
            final TextView txt = (TextView) lastItemView.findViewById(R.id.LastItemTitle);
            txt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
            if (lastItem == null) {
                lastUid = null;
                menu.findViewById(R.id.BtnPrevious).setEnabled(false);
                final ImageView img = (ImageView) lastItemView.findViewById(R.id.LastItemArrow);
                img.setImageResource(R.drawable.no_more_circle);
                txt.setText(R.string.TxtNoPreviousItem);
            } else {
                lastUid = lastItem.getId();
                final View btnLast = menu.findViewById(R.id.BtnPrevious);
                btnLast.setEnabled(true);
                btnLast.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        if (listener != null) {
                            listener.showLastItem();
                        }
                    }
                });
                txt.setText(lastItem.getTitle());
            }
        }

        {
            final ListItemItem nextItem = itemViewCtrl.getNextItem(item.getUid());
            final TextView txt = (TextView) nextItemView.findViewById(R.id.NextItemTitle);
            txt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
            if (nextItem == null) {
                nextUid = null;
                menu.findViewById(R.id.BtnNext).setEnabled(false);
                final ImageView img = (ImageView) nextItemView.findViewById(R.id.NextItemArrow);
                img.setImageResource(R.drawable.no_more_circle);
                txt.setText(R.string.TxtNoNextItem);
            } else {
                nextUid = nextItem.getId();
                final View btnNext = menu.findViewById(R.id.BtnNext);
                btnNext.setEnabled(true);
                btnNext.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        if (listener != null) {
                            listener.showNextItem();
                        }
                    }
                });
                txt.setText(nextItem.getTitle());
            }
        }

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

        webView = (WebView) view.findViewById(R.id.webView);
        webView.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
        webView.setBackgroundColor(context.getResources().getColor(
                theme == SettingTheme.THEME_NORMAL ? R.color.NormalBackground : R.color.DarkBackground));
        webView.setFocusable(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(final WebView view, final String url) {
                if (VerticalSingleItemView.this.imageClickTime > System.currentTimeMillis() - 1000) {
                    return true;
                }
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setData(Uri.parse(url));
                context.startActivity(intent);
                return true;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(final WebView view, final String url, final String message, final JsResult result) {
                if (VerticalSingleItemView.this.listener != null) {
                    if (message.endsWith(".erss")) {
                        VerticalSingleItemView.this.listener.onImageViewRequired(item.getStoragePath() + "/" + message);
                    } else {
                        VerticalSingleItemView.this.listener.onImageViewRequired(message);
                    }
                }
                VerticalSingleItemView.this.imageClickTime = System.currentTimeMillis();
                result.cancel();
                return true;
            }
        });

        updateButtonStar();
        updateButtonSharing();
        updateButtonOpenLink();

        if (!item.getState().isRead()) {
            dataMgr.markItemAsReadWithTransactionByUid(uid);
            NetworkMgr.getInstance().startImmediateItemStateSyncing();
        }
    }

    public Item getItem() {
        return item;
    }

    public VerticalSingleItemViewListener getListener() {
        return listener;
    }

    public View getView() {
        return view;
    }

    public void holdItemViewScroll() {
        itemScrollView.setScrollHold(true);
        itemScrollView.setOnScrollChangeListener(null);
        itemScrollView.setOnTouchListener(null);
        webView.stopLoading();
        webView.setWebViewClient(null);
        webView.setWebChromeClient(null);
        menu.findViewById(R.id.BtnAddStar).setOnClickListener(null);
        menu.findViewById(R.id.BtnSharing).setOnClickListener(null);
        menu.findViewById(R.id.BtnOpenLink).setOnClickListener(null);
    }

    @SuppressLint("SetJavaScriptEnabled")
    public void loadContent() {
        final WebSettings settings = webView.getSettings();
        settings.setDefaultTextEncodingName(HTTP.UTF_8);
        settings.setJavaScriptEnabled(true);
        settings.setDefaultFontSize(fontSize);
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        //settings.setRenderPriority(RenderPriority.LOW);

        final StringBuffer content = new StringBuffer();
        if (item.getState().isCached()) {
            settings.setBlockNetworkImage(true);
            content.append(DataUtils.readFromFile(new File(item.getFullContentStoragePath())));
        } else {
            final SettingImageFetching sImgFetch = new SettingImageFetching(dataMgr);
            if (NetworkUtils.checkImageFetchingNetworkStatus(context, sImgFetch.getData())) {
                settings.setBlockNetworkImage(false);
                content.append(DataUtils.readFromFile(new File(item.getOriginalContentStoragePath())));
            } else {
                settings.setBlockNetworkImage(true);
                content.append(DataUtils.readFromFile(new File(item.getStrippedContentStoragePath())));
            }
        }
        content.append(DataUtils.DEFAULT_JS);
        content.append(theme == SettingTheme.THEME_NORMAL ? DataUtils.DEFAULT_NORMAL_CSS : DataUtils.DEFAULT_DARK_CSS);
        webView.loadDataWithBaseURL("file://" + item.getStoragePath() + "/", content.toString(), null, "utf-8", null);
    }

    @Override
    public void onScrollChanged(final View view, final int x, final int y, final int oldx, final int oldy) {
        if (y + view.getHeight() > nextItemView.getTop() + 5) {
            itemViewCtrl.hideItemMenu();
        } else {
            itemViewCtrl.awakenItemMenu();
        }
        if (lastUid != null) {
            final boolean shouldShowTop = (y < lastItemView.getTop() + 50);
            if (!showTop && shouldShowTop) {
                final Animation anim = AnimationUtils.loadAnimation(context, R.anim.up_arrow_anim_rev);
                final View img = lastItemView.findViewById(R.id.LastItemArrow);
                anim.setFillEnabled(true);
                anim.setFillAfter(true);
                img.startAnimation(anim);
                showTop = true;
            } else if (showTop && !shouldShowTop) {
                final Animation anim = AnimationUtils.loadAnimation(context, R.anim.up_arrow_anim);
                final View img = lastItemView.findViewById(R.id.LastItemArrow);
                anim.setFillEnabled(true);
                anim.setFillAfter(true);
                img.startAnimation(anim);
                showTop = false;
            }
        }
        if (nextUid != null) {
            final boolean shouldShowBottom = (y + view.getHeight() > nextItemView.getBottom() - 50);
            if (!showBottom && shouldShowBottom) {
                final Animation anim = AnimationUtils.loadAnimation(context, R.anim.down_arrow_anim_rev);
                final View img = nextItemView.findViewById(R.id.NextItemArrow);
                anim.setFillEnabled(true);
                anim.setFillAfter(true);
                img.startAnimation(anim);
                showBottom = true;
            } else if (showBottom && !shouldShowBottom) {
                final Animation anim = AnimationUtils.loadAnimation(context, R.anim.down_arrow_anim);
                final View img = nextItemView.findViewById(R.id.NextItemArrow);
                anim.setFillEnabled(true);
                anim.setFillAfter(true);
                img.startAnimation(anim);
                showBottom = false;
            }
        }
    }

    @Override
    public boolean onTouch(final View view, final MotionEvent event) {
        final int action = event.getAction();
        if (action == MotionEvent.ACTION_CANCEL) {
            showTop = false;
            showBottom = false;
        } else if (action == MotionEvent.ACTION_UP) {
            if (showTop) {
                if (listener != null && lastUid != null) {
                    listener.showLastItem();
                }
            } else if (showBottom) {
                if (listener != null && nextUid != null) {
                    listener.showNextItem();
                }
            }
            showTop = false;
            showBottom = false;
        }
        return false;
    }

    public void setListener(final VerticalSingleItemViewListener listener) {
        this.listener = listener;
    }

    public void unload() {
        webView.freeMemory();
    }

    private void updateButtonOpenLink() {
        final View btnOpenLink = menu.findViewById(R.id.BtnOpenLink);
        btnOpenLink.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (listener != null) {
                    final SettingBrowserChoice setting = new SettingBrowserChoice(dataMgr);
                    switch (setting.getData()) {
                    case SettingBrowserChoice.BROWSER_CHOICE_UNKNOWN:
                        final LayoutInflater inflater = (LayoutInflater) context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context,
                                android.R.style.Theme_DeviceDefault_Dialog));
                        final View popupView = inflater.inflate(R.layout.browser_choice_popup, null);
                        final CheckBox checkBox = (CheckBox) popupView.findViewById(R.id.CheckBoxDontShowAgain);
                        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(final CompoundButton buttonView, final boolean isChecked) {
                                popupView.findViewById(R.id.Hint).setVisibility(View.VISIBLE);
                            }
                        });
                        final String items[] = new String[3];
                        items[0] = context.getString(R.string.TxtBuiltInMobilized);
                        items[1] = context.getString(R.string.TxtBuiltInOriginal);
                        items[2] = context.getString(R.string.TxtExternalOriginal);
                        builder.setTitle(R.string.TxtChooseBrowser);
                        builder.setView(popupView);
                        builder.setItems(items, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, final int which) {
                                switch (which) {
                                case 0:
                                    if (listener != null) {
                                        listener.showWebsitePage(item.getUid(), true);
                                    }
                                    if (checkBox.isChecked()) {
                                        setting.setData(dataMgr, SettingBrowserChoice.BROWSER_CHOICE_MOBILIZED);
                                    }
                                    dialog.dismiss();
                                    break;
                                case 1:
                                    if (listener != null) {
                                        listener.showWebsitePage(item.getUid(), false);
                                    }
                                    if (checkBox.isChecked()) {
                                        setting.setData(dataMgr, SettingBrowserChoice.BROWSER_CHOICE_ORIGINAL);
                                    }
                                    dialog.dismiss();
                                    break;
                                case 2:
                                    final Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    intent.setData(Uri.parse(item.getHref()));
                                    context.startActivity(intent);
                                    if (checkBox.isChecked()) {
                                        setting.setData(dataMgr, SettingBrowserChoice.BROWSER_CHOICE_EXTERNAL);
                                    }
                                    dialog.dismiss();
                                    break;
                                default:
                                    break;
                                }
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
                    case SettingBrowserChoice.BROWSER_CHOICE_EXTERNAL:
                        final Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.parse(item.getHref()));
                        context.startActivity(intent);
                        break;
                    case SettingBrowserChoice.BROWSER_CHOICE_MOBILIZED:
                        if (listener != null) {
                            listener.showWebsitePage(item.getUid(), true);
                        }
                        break;
                    case SettingBrowserChoice.BROWSER_CHOICE_ORIGINAL:
                        if (listener != null) {
                            listener.showWebsitePage(item.getUid(), false);
                        }
                        break;
                    default:
                        break;
                    }
                }
            }
        });
    }

    private void updateButtonSharing() {
        final View btnSharing = menu.findViewById(R.id.BtnSharing);
        btnSharing.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(context,
                        android.R.style.Theme_DeviceDefault_Dialog));
                final String[] popup = new String[] { context.getString(R.string.TxtSendTo),
                        context.getString(R.string.TxtSendItemTextTo),
                        context.getString(R.string.TxtSendItemContentTo),
                        context.getString(R.string.TxtCopyToClipboard) };
                builder.setItems(popup, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialogInterface, final int id) {
                        switch (id) {
                        case 0:
                            DataUtils.sendTo(context, item);
                            break;
                        case 1:
                            DataUtils.sendContentTo(context, item);
                            break;
                        case 2:
                            DataUtils.sendHtmlContentTo(context, item);
                            break;
                        case 3:
                            DataUtils.copyToClipboard(context, item);
                        default:
                        }
                    }
                });
                builder.show();
            }
        });
    }

    private void updateButtonStar() {
        final View btnStar = menu.findViewById(R.id.BtnAddStar);
        if (item.getState().isStarred()) {
            btnStar.setBackgroundResource(R.drawable.menu_item_remove_star_xml);
        } else {
            btnStar.setBackgroundResource(R.drawable.menu_item_add_star_xml);
        }
        btnStar.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                final boolean starred = !item.getState().isStarred();
                dataMgr.markItemAsStarredWithTransactionByUid(item.getUid(), starred);
                item.getState().setStarred(starred);
                Toast.makeText(context, starred ? R.string.MsgStarred : R.string.MsgUnstarred, Toast.LENGTH_LONG)
                        .show();
                updateButtonStar();
                NetworkMgr.getInstance().startImmediateItemStateSyncing();
            }
        });
    }
}
