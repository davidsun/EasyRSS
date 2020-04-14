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

import java.util.ArrayList;
import java.util.List;

import org.freshrss.easyrss.ImageViewCtrl;
import org.freshrss.easyrss.R;
import org.freshrss.easyrss.SettingsViewCtrl;

import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ViewFlipper;

public class ViewManager {
    final private Activity activity;
    final private List<AbsViewCtrl> viewCtrls;
    final private ViewFlipper flipper;

    public ViewManager(final Activity activity) {
        this.viewCtrls = new ArrayList<AbsViewCtrl>();
        this.activity = activity;
        this.flipper = (ViewFlipper) activity.findViewById(R.id.GlobalFlipper);
    }

    public void clearViews() {
        for (int i = 0; i < viewCtrls.size(); i++) {
            viewCtrls.get(i).onDestory();
        }
        flipper.removeAllViews();
        viewCtrls.clear();
    }

    public int getLastViewResId() {
        return viewCtrls.get(viewCtrls.size() - 1).getResId();
    }

    public AbsViewCtrl getTopView() {
        return viewCtrls.isEmpty() ? null : viewCtrls.get(viewCtrls.size() - 1);
    }

    public int getViewCount() {
        return viewCtrls.size();
    }

    public void popView(final Animation inAnimation, final Animation outAnimation) {
        if (viewCtrls.isEmpty()) {
            return;
        }
        flipper.setInAnimation(inAnimation);

        final AbsViewCtrl lastView = viewCtrls.get(viewCtrls.size() - 1);
        flipper.setOutAnimation(outAnimation);
        if (outAnimation == null) {
            flipper.showPrevious();
            lastView.onDeactivate();
            lastView.onDestory();
        } else {
            outAnimation.setAnimationListener(new AnimationListener() {
                @Override
                public void onAnimationEnd(final Animation animation) {
                    if (!viewCtrls.isEmpty()) {
                        viewCtrls.get(viewCtrls.size() - 1).getView().disableCache();
                    }
                    lastView.onDestory();
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
            flipper.showPrevious();
            lastView.onDeactivate();
        }
        viewCtrls.remove(lastView);
        flipper.removeView((View) lastView.getView());
        if (!viewCtrls.isEmpty()) {
            viewCtrls.get(viewCtrls.size() - 1).onActivate();
        }
    }

    public void popView(final int inAnimation, final int outAnimation) {
        final Animation inAnim = (inAnimation == -1) ? (null) : (AnimationUtils.loadAnimation(activity, inAnimation));
        final Animation outAnim = (outAnimation == -1) ? (null)
                : (AnimationUtils.loadAnimation(activity, outAnimation));
        popView(inAnim, outAnim);
    }

    public void pushView(final AbsViewCtrl view) {
        pushView(view, -1, -1);
    }

    public void setStaticAnimation(final Animation inAnimation, final Animation outAnimation) {
        final int size = viewCtrls.size();
        viewCtrls.get(size - 2).getView().setAnimation(inAnimation);
        viewCtrls.get(size - 1).getView().setAnimation(outAnimation);
        flipper.invalidate();
    }

    public void restoreTopView() {
        final AbsViewCtrl topView = viewCtrls.get(viewCtrls.size() - 1);
        if (!(topView instanceof SettingsViewCtrl) && !(topView instanceof ImageViewCtrl)) {
            topView.getView().disableCache();
        }
    }

    public void pushView(final AbsViewCtrl view, final int inAnimation, final int outAnimation) {
        if (!viewCtrls.isEmpty()) {
            final AbsViewCtrl topView = viewCtrls.get(viewCtrls.size() - 1);
            if (!(topView instanceof SettingsViewCtrl) && !(topView instanceof ImageViewCtrl)) {
                topView.getView().enableCache();
            }
            topView.onDeactivate();
        }
        flipper.addView((View) view.getView());
        if (inAnimation == -1) {
            flipper.setInAnimation(null);
        } else {
            flipper.setInAnimation(AnimationUtils.loadAnimation(activity, inAnimation));
        }
        if (outAnimation == -1) {
            flipper.setOutAnimation(null);
        } else {
            flipper.setOutAnimation(AnimationUtils.loadAnimation(activity, outAnimation));
        }
        flipper.showNext();
        viewCtrls.add(view);
        view.onCreate();
        view.onActivate();
    }
}
