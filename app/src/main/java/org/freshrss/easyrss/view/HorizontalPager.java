/**
 * Modified by Sun Zheng https://github.com/davidsun/horizontalpager
 * based on modifications by Yoni Samlan https://github.com/ysamlan/horizontalpager
 * based on RealViewSwitcher, whose license is:
 *
 * Copyright (C) 2010 Marc Reichelt
 *
 * Work derived from Workspace.java of the Launcher application
 *  see http://android.git.kernel.org/?p=platform/packages/apps/Launcher.git
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.freshrss.easyrss.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Scroller;

public final class HorizontalPager extends ViewGroup {
    private static final int ANIMATION_SCREEN_SET_DURATION_MILLIS = 500;
    private static final int FRACTION_OF_SCREEN_WIDTH_FOR_SWIPE = 15;
    private static final int INVALID_SCREEN = -1;
    private static final int SNAP_VELOCITY_DIP_PER_SECOND = 600;
    private static final int VELOCITY_UNIT_PIXELS_PER_SECOND = 1000;

    private int mCurrentScreen;
    private int mDensityAdjustedSnapVelocity;
    private boolean mFirstLayout = true;
    private float mLastMotionX;
    private float mLastMotionY;
    private int mMaximumVelocity;
    private int mNextScreen = INVALID_SCREEN;
    private Scroller scroller;
    private int mTouchSlop;
    private boolean isDragging;
    private VelocityTracker mVelocityTracker;
    private HorizontalPagerListener listener;
    private int mLastSeenLayoutWidth = -1;

    public HorizontalPager(final Context context) {
        super(context);
        init();
    }

    public HorizontalPager(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @Override
    public void computeScroll() {
        if (scroller.computeScrollOffset()) {
            scrollTo(scroller.getCurrX(), scroller.getCurrY());
            postInvalidate();
        } else if (mNextScreen != INVALID_SCREEN) {
            mCurrentScreen = Math.max(0, Math.min(mNextScreen, getChildCount() - 1));
            if (listener != null) {
                listener.onScreenSwitch(mCurrentScreen);
            }
            mNextScreen = INVALID_SCREEN;
        }
    }

    public int getCurrentScreen() {
        return mCurrentScreen;
    }

    public HorizontalPagerListener getListener() {
        return listener;
    }

    private void init() {
        this.scroller = new Scroller(getContext());
        this.isDragging = false;

        final DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(
                displayMetrics);
        mDensityAdjustedSnapVelocity = (int) (displayMetrics.density * SNAP_VELOCITY_DIP_PER_SECOND);

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent event) {
        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            mLastMotionX = x;
            mLastMotionY = y;
            break;
        case MotionEvent.ACTION_MOVE:
            if (!isDragging) {
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                final int yDiff = (int) Math.abs(y - mLastMotionY);

                if (xDiff > mTouchSlop && yDiff > mTouchSlop) {
                    isDragging = (xDiff >= yDiff);
                } else if (xDiff > mTouchSlop) {
                    isDragging = true;
                } else if (yDiff > mTouchSlop) {
                    isDragging = false;
                }
            }
            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            isDragging = false;
            break;
        default:
            break;
        }
        if (isDragging) {
            mLastMotionX = x;
            mLastMotionY = y;
        }
        return isDragging;
    }

    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final int b) {
        int childLeft = 0;
        final int count = getChildCount();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != View.GONE) {
                final int childWidth = child.getMeasuredWidth();
                child.layout(childLeft, 0, childLeft + childWidth, child.getMeasuredHeight());
                childLeft += childWidth;
            }
        }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int width = MeasureSpec.getSize(widthMeasureSpec);
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("HorizontalPager can only be used in EXACTLY mode.");
        }

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode != MeasureSpec.EXACTLY) {
            throw new IllegalStateException("HorizontalPager can only be used in EXACTLY mode.");
        }

        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            getChildAt(i).measure(widthMeasureSpec, heightMeasureSpec);
        }

        if (mFirstLayout) {
            scrollTo(mCurrentScreen * width, 0);
            if (listener != null) {
                listener.onScreenSwitch(mCurrentScreen);
            }
            mFirstLayout = false;
        } else if (width != mLastSeenLayoutWidth) {
            final Display display = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay();
            @SuppressWarnings("deprecation")
            final int displayWidth = display.getWidth();
            mNextScreen = Math.max(0, Math.min(getCurrentScreen(), getChildCount() - 1));
            final int newX = mNextScreen * displayWidth;
            final int delta = newX - getScrollX();
            scroller.startScroll(getScrollX(), 0, delta, 0, 0);
        }

        mLastSeenLayoutWidth = width;
    }

    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {
        if (listener != null) {
            listener.onScrollChanged(l, t, oldl, oldt);
        }
        super.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }

        mVelocityTracker.addMovement(event);

        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            if (!scroller.isFinished()) {
                scroller.abortAnimation();
            }

            mLastMotionX = x;
            mLastMotionY = y;
            isDragging = true;
            break;
        case MotionEvent.ACTION_MOVE:
            final int xDiff = (int) Math.abs(x - mLastMotionX);
            if (xDiff > mTouchSlop) {
                isDragging = true;
            }

            if (isDragging) {
                final int deltaX = (int) (mLastMotionX - x);
                final int scrollX = getScrollX();

                if (deltaX < 0) {
                    if (scrollX > 0) {
                        scrollBy(Math.max(-scrollX, deltaX), 0);
                    }
                } else if (deltaX > 0) {
                    final int availableToScroll = getChildAt(getChildCount() - 1).getRight() - scrollX - getWidth();
                    if (availableToScroll > 0) {
                        scrollBy(Math.min(availableToScroll, deltaX), 0);
                    }
                }

                mLastMotionX = x;
                mLastMotionY = y;
            }
            break;
        case MotionEvent.ACTION_UP:
            if (isDragging) {
                final VelocityTracker velocityTracker = mVelocityTracker;
                velocityTracker.computeCurrentVelocity(VELOCITY_UNIT_PIXELS_PER_SECOND, mMaximumVelocity);
                final int velocityX = (int) velocityTracker.getXVelocity();

                if (velocityX > mDensityAdjustedSnapVelocity && mCurrentScreen > 0) {
                    snapToScreen(mCurrentScreen - 1);
                } else if (velocityX < -mDensityAdjustedSnapVelocity && mCurrentScreen < getChildCount() - 1) {
                    snapToScreen(mCurrentScreen + 1);
                } else {
                    snapToDestination();
                }

                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
            }
            isDragging = false;
            break;
        case MotionEvent.ACTION_CANCEL:
            isDragging = false;
            break;
        default:
            break;
        }

        return true;
    }

    public void setCurrentScreen(final int currentScreen, final boolean animate) {
        mCurrentScreen = Math.max(0, Math.min(currentScreen, getChildCount() - 1));
        if (animate) {
            snapToScreen(currentScreen, ANIMATION_SCREEN_SET_DURATION_MILLIS);
        } else {
            scrollTo(mCurrentScreen * getWidth(), 0);
        }
        invalidate();
    }

    public void setListener(final HorizontalPagerListener listener) {
        this.listener = listener;
    }

    private void snapToDestination() {
        final int screenWidth = getWidth();
        final int scrollX = getScrollX();
        final int deltaX = scrollX - (screenWidth * mCurrentScreen);
        int whichScreen = mCurrentScreen;

        if ((deltaX < 0) && mCurrentScreen != 0 && ((screenWidth / FRACTION_OF_SCREEN_WIDTH_FOR_SWIPE) < -deltaX)) {
            whichScreen--;
        } else if ((deltaX > 0) && (mCurrentScreen + 1 != getChildCount())
                && ((screenWidth / FRACTION_OF_SCREEN_WIDTH_FOR_SWIPE) < deltaX)) {
            whichScreen++;
        }

        snapToScreen(whichScreen);
    }

    private void snapToScreen(final int whichScreen) {
        snapToScreen(whichScreen, -1);
    }

    private void snapToScreen(final int whichScreen, final int duration) {
        mNextScreen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
        final int newX = mNextScreen * getWidth();
        final int delta = newX - getScrollX();

        if (duration < 0) {
            scroller.startScroll(getScrollX(), 0, delta, 0,
                    (int) (Math.abs(delta) / (float) getWidth() * ANIMATION_SCREEN_SET_DURATION_MILLIS));
        } else {
            scroller.startScroll(getScrollX(), 0, delta, 0, duration);
        }

        invalidate();
    }
}
