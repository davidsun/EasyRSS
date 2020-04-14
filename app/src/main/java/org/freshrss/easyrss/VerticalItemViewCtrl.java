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

import org.freshrss.easyrss.R;
import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.listadapter.ListItemItem;
import org.freshrss.easyrss.view.AbsViewCtrl;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ViewFlipper;

public class VerticalItemViewCtrl extends AbsViewCtrl implements VerticalSingleItemViewListener {
    private static Handler handler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            final VerticalItemViewCtrl viewCtrl = (VerticalItemViewCtrl) msg.obj;
            if (msg.what == MSG_HIDE_ITEM_MENU) {
                if (viewCtrl.itemMenuShowTime <= System.currentTimeMillis() - 2000) {
                    viewCtrl.hideItemMenu();
                } else {
                    handler.sendMessageDelayed(obtainMessage(MSG_HIDE_ITEM_MENU, viewCtrl), 1000);
                }
            }
        }
    };

    final private FeedViewCtrl feedViewCtrl;
    private long itemMenuShowTime;
    private ViewFlipper itemFlipper;
    private View itemMenu;
    private VerticalSingleItemView curSingleItemView;
    private String uid;

    final static private int MSG_HIDE_ITEM_MENU = 0;

    public VerticalItemViewCtrl(final DataMgr dataMgr, final Context context, final String uid,
            final FeedViewCtrl feedViewCtrl) {
        super(dataMgr, R.layout.item, context);

        this.uid = uid;
        this.feedViewCtrl = feedViewCtrl;
        this.itemMenuShowTime = System.currentTimeMillis();
    }

    public void awakenItemMenu() {
        if (itemMenuShowTime == 0) {
            final Animation anim = AnimationUtils.loadAnimation(context, R.anim.item_menu_fade_in);
            anim.setFillAfter(true);
            anim.setFillEnabled(true);
            itemMenu.startAnimation(anim);
            handler.removeMessages(MSG_HIDE_ITEM_MENU);
            handler.sendMessageDelayed(handler.obtainMessage(MSG_HIDE_ITEM_MENU, this), 1000);
        }
        itemMenuShowTime = System.currentTimeMillis();
    }

    public FeedViewCtrl getFeedViewCtrl() {
        return feedViewCtrl;
    }

    public ListItemItem getLastItem(final String uid) {
        return feedViewCtrl.getLastItem(uid);
    }

    public ListItemItem getNextItem(final String uid) {
        return feedViewCtrl.getNextItem(uid);
    }

    public void hideItemMenu() {
        if (itemMenuShowTime > 0) {
            handler.removeMessages(MSG_HIDE_ITEM_MENU);
            itemMenuShowTime = 0;
            final Animation anim = AnimationUtils.loadAnimation(context, R.anim.item_menu_fade_out);
            anim.setFillAfter(true);
            anim.setFillEnabled(true);
            itemMenu.startAnimation(anim);
        }
    }

    private VerticalSingleItemView initView() {
        final VerticalSingleItemView ret = new VerticalSingleItemView(dataMgr, context, uid, itemMenu, this);
        itemFlipper.addView(ret.getView());
        ret.setListener(this);
        return ret;
    }

    @Override
    public void onActivate() {
        // TODO empty method
    }

    @Override
    public void onCreate() {
        itemFlipper = (ViewFlipper) view.findViewById(R.id.ItemFlipper);
        itemMenu = view.findViewById(R.id.Menu);

        curSingleItemView = initView();
        curSingleItemView.loadContent();
    }

    @Override
    public void onDeactivate() {
        // TODO empty method
    }

    @Override
    public void onDestory() {
        curSingleItemView.setListener(null);
    }

    @Override
    public void onImageViewRequired(final String imgPath) {
        if (listener != null) {
            listener.onImageViewRequired(imgPath);
        }
    }

    @Override
    public void showLastItem() {
        final ListItemItem item = getLastItem(this.uid);
        if (item != null) {
            this.uid = item.getId();
            showNextItem(R.anim.item_top_in, R.anim.item_bottom_out);
        }
    }

    @Override
    public void showWebsitePage(final String uid, final boolean isMobilized) {
        if (listener != null) {
            listener.onWebsiteViewSelected(uid, isMobilized);
        }
    }

    @Override
    public void showNextItem() {
        final ListItemItem item = getNextItem(this.uid);
        if (item != null) {
            this.uid = item.getId();
            showNextItem(R.anim.item_bottom_in, R.anim.item_top_out);
        }
    }

    private void showNextItem(final int inAnimation, final int outAnimation) {
        curSingleItemView.setListener(null);
        curSingleItemView.holdItemViewScroll();
        hideItemMenu();

        final VerticalSingleItemView view = initView();
        final Animation anim1 = AnimationUtils.loadAnimation(context, inAnimation);
        final Animation anim2 = AnimationUtils.loadAnimation(context, outAnimation);
        anim1.setInterpolator(new AccelerateDecelerateInterpolator());
        anim2.setInterpolator(new AccelerateDecelerateInterpolator());
        anim2.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationEnd(final Animation animation) {
                curSingleItemView.unload();
                curSingleItemView = view;
                curSingleItemView.setListener(VerticalItemViewCtrl.this);
                curSingleItemView.loadContent();
            }

            @Override
            public void onAnimationRepeat(final Animation animation) {
                // TODO empty method
            }

            @Override
            public void onAnimationStart(final Animation animation) {
                // TODO empty method
            }
        });
        itemFlipper.setInAnimation(anim1);
        itemFlipper.setOutAnimation(anim2);
        itemFlipper.showNext();
        itemFlipper.removeView(curSingleItemView.getView());
    }
}
