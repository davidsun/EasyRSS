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

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class OverScrollView extends FrameLayout {
    final static private int DRAGGING_MODE_IDLE = 0;
    final static private int DRAGGING_MODE_HORIZONTAL = 1;
    final static private int DRAGGING_MODE_VERTICAL = 2;

    private OnScrollChangedListener onScrollChangedListener;
    private OverScroller scroller;
    private int topScrollMargin;
    private int bottomScrollMargin;
    private int draggingMode;
    private int touchSlop;
    private int minVelocity;
    private int maxVelocity;
    private float lastMotionX;
    private float lastMotionY;
    private boolean isScrollHold;
    private View topScrollView;
    private View bottomScrollView;
    VelocityTracker velocityTracker;

    public OverScrollView(final Context context) {
        super(context);
        init();
    }

    public OverScrollView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OverScrollView(final Context context, final AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void adjustScrollY() {
        if (isScrollHold || draggingMode == DRAGGING_MODE_VERTICAL) {
            return;
        }
        final int y = getScrollY();
        if (y < getMinScrollY()) {
            scrollTo(0, getMinScrollY());
        } else if (y > getMaxScrollY()) {
            scrollTo(0, getMaxScrollY());
        }
    }

    @Override
    public void computeScroll() {
        if (!isScrollHold && scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    protected int computeVerticalScrollOffset() {
        return Math.max(super.computeVerticalScrollOffset() - topScrollMargin, 0);
    }

    @Override
    protected int computeVerticalScrollRange() {
        return getChildAt(0).getMeasuredHeight() - topScrollMargin - bottomScrollMargin;
    }

    private void fling(final int velocityY) {
        scroller.fling(getScrollX(), getScrollY(), 0, velocityY, 0, 0, getMinScrollY(), getMaxScrollY(), 0,
                Math.min(topScrollMargin, bottomScrollMargin) - 1);
        invalidate();
    }

    public int getBottomScrollMargin() {
        return bottomScrollMargin;
    }

    public View getBottomScrollView() {
        return bottomScrollView;
    }

    private int getMaxScrollY() {
        return getChildAt(0).getMeasuredHeight() - bottomScrollMargin - getHeight();
    }

    private int getMinScrollY() {
        return topScrollMargin;
    }

    public OnScrollChangedListener getOnScrollChangeListener() {
        return onScrollChangedListener;
    }

    public int getTopScrollMargin() {
        return topScrollMargin;
    }

    public View getTopScrollView() {
        return topScrollView;
    }

    private void init() {
        this.draggingMode = DRAGGING_MODE_IDLE;
        this.topScrollMargin = 0;
        this.bottomScrollMargin = 0;
        this.lastMotionY = 0;
        this.scroller = new OverScroller(getContext());
        this.isScrollHold = false;

        setFocusable(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        this.touchSlop = configuration.getScaledTouchSlop();
        this.minVelocity = configuration.getScaledMinimumFlingVelocity();
        this.maxVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    public boolean isScrollHold() {
        return isScrollHold;
    }

    @Override
    protected void measureChild(final View child, final int parentWidthMeasureSpec, final int parentHeightMeasureSpec) {
        final ViewGroup.LayoutParams lp = child.getLayoutParams();
        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, 0, lp.width);
        final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    protected void measureChildWithMargins(final View child, final int parentWidthMeasureSpec, final int widthUsed,
            final int parentHeightMeasureSpec, final int heightUsed) {
        final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, lp.leftMargin + lp.rightMargin
                + widthUsed, lp.width);
        final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(lp.topMargin + lp.bottomMargin,
                MeasureSpec.UNSPECIFIED);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent event) {
        final int action = event.getAction();

        final float x = event.getX();
        final float y = event.getY();
        switch (action) {
        case MotionEvent.ACTION_MOVE:
            if (draggingMode == DRAGGING_MODE_IDLE) {
                final int xDiff = (int) Math.abs(x - lastMotionX);
                final int yDiff = (int) Math.abs(y - lastMotionY);
                if (yDiff > touchSlop) {
                    draggingMode = DRAGGING_MODE_VERTICAL;
                } else if (xDiff > touchSlop) {
                    draggingMode = DRAGGING_MODE_HORIZONTAL;
                }
            }

            if (draggingMode == DRAGGING_MODE_VERTICAL) {
                lastMotionX = x;
                lastMotionY = y;
            }

            break;
        case MotionEvent.ACTION_DOWN:
            if (!scroller.isFinished()) {
                draggingMode = DRAGGING_MODE_IDLE;
                scroller.abortAnimation();
            }
            lastMotionX = x;
            lastMotionY = y;
            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            draggingMode = DRAGGING_MODE_IDLE;
            if (scroller.springBack(0, getScrollY(), 0, 0, getMinScrollY(), getMaxScrollY())) {
                invalidate();
            }
            break;
        default:
        }
        return draggingMode == DRAGGING_MODE_VERTICAL;
    }

    @Override
    protected void onLayout(final boolean changed, final int left, final int top, final int right, final int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        adjustScrollY();
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.UNSPECIFIED) {
            return;
        }

        if (topScrollView != null) {
            setTopScrollMargin(topScrollView.getMeasuredHeight());
        }
        if (bottomScrollView != null) {
            setBottomScrollMargin(bottomScrollView.getMeasuredHeight());
        }

        if (getChildCount() > 0) {
            final View child = getChildAt(0);
            final int height = getMeasuredHeight() + topScrollMargin + bottomScrollMargin;
            if (child.getMeasuredHeight() < height) {
                final FrameLayout.LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, 0, lp.width);
                final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }

    @Override
    protected void onScrollChanged(final int x, final int y, final int oldx, final int oldy) {
        super.onScrollChanged(x, y, oldx, oldy);
        if (onScrollChangedListener != null) {
            onScrollChangedListener.onScrollChanged(this, x, y, oldx, oldy);
        }
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();

        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(event);

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            if (!scroller.isFinished()) {
                scroller.abortAnimation();
                draggingMode = DRAGGING_MODE_IDLE;
            }
            draggingMode = DRAGGING_MODE_VERTICAL;
            lastMotionX = x;
            lastMotionY = y;
            break;
        case MotionEvent.ACTION_MOVE:
            final int xDiff = (int) Math.abs(x - lastMotionX);
            final int yDiff = (int) Math.abs(y - lastMotionY);
            if (draggingMode == DRAGGING_MODE_IDLE) {
                if (yDiff > touchSlop) {
                    draggingMode = DRAGGING_MODE_VERTICAL;
                } else if (xDiff > touchSlop) {
                    draggingMode = DRAGGING_MODE_HORIZONTAL;
                }
            }

            if (draggingMode == DRAGGING_MODE_VERTICAL) {
                final int scrollY = getScrollY();
                int deltaY = (int) (lastMotionY - y);
                deltaY = Math.max(deltaY, -scrollY);
                deltaY = Math.min(deltaY, getChildAt(0).getHeight() - getHeight() - scrollY);
                if (deltaY != 0) {
                    scrollBy(0, deltaY);
                }
                lastMotionX = x;
                lastMotionY = y;
            }
            break;
        case MotionEvent.ACTION_UP:
            if (draggingMode == DRAGGING_MODE_VERTICAL) {
                velocityTracker.computeCurrentVelocity(1000, maxVelocity);
                final int initialVelocity = (int) velocityTracker.getYVelocity();

                if (getChildCount() > 0) {
                    if ((Math.abs(initialVelocity) > minVelocity)) {
                        fling(-initialVelocity);
                    } else {
                        if (scroller.springBack(0, getScrollY(), 0, 0, getMinScrollY(), getMaxScrollY())) {
                            invalidate();
                        }
                    }
                }
            }
            draggingMode = DRAGGING_MODE_IDLE;
            break;
        case MotionEvent.ACTION_CANCEL:
            if (draggingMode == DRAGGING_MODE_VERTICAL
                    && scroller.springBack(0, getScrollY(), 0, 0, getMinScrollY(), getMaxScrollY())) {
                invalidate();
            }
            draggingMode = DRAGGING_MODE_IDLE;
            break;
        default:
        }
        return true;
    }

    private void setBottomScrollMargin(final int bottomScrollMargin) {
        this.bottomScrollMargin = bottomScrollMargin;
        adjustScrollY();
    }

    public void setBottomScrollView(final View bottomScrollView) {
        this.bottomScrollView = bottomScrollView;
        if (bottomScrollView != null) {
            setBottomScrollMargin(bottomScrollView.getMeasuredHeight());
        }
    }

    public void setOnScrollChangeListener(final OnScrollChangedListener onScrollChangeListener) {
        this.onScrollChangedListener = onScrollChangeListener;
    }

    public void setScrollHold(final boolean isScrollHold) {
        this.isScrollHold = isScrollHold;
        scroller.forceFinished(isScrollHold);
    }

    private void setTopScrollMargin(final int topScrollMargin) {
        this.topScrollMargin = topScrollMargin;
        adjustScrollY();
    }

    public void setTopScrollView(final View topScrollView) {
        this.topScrollView = topScrollView;
        if (topScrollView != null) {
            setTopScrollMargin(topScrollView.getMeasuredHeight());
        }
    }
}
