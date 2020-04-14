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
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.LinearLayout;

public final class HorizontalSwipeView extends LinearLayout {
    private static final int SNAP_VELOCITY_DIP_PER_SECOND = 600;
    private static final int VELOCITY_UNIT_PIXELS_PER_SECOND = 1000;

    private int mMaximumVelocity;
    private int mDensityAdjustedSnapVelocity;
    private int mTouchSlop;
    private float mLastMotionX;
    private float mLastMotionY;
    private boolean isDragging;
    private boolean isRightSwipeValid;
    private boolean isLeftSwipeValid;
    private VelocityTracker mVelocityTracker;
    private HorizontalSwipeViewListener listener;

    public HorizontalSwipeView(final Context context) {
        super(context);
        init();
    }

    public HorizontalSwipeView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public HorizontalSwipeViewListener getListener() {
        return listener;
    }

    private void init() {
        this.isDragging = false;

        final DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(
                displayMetrics);
        mDensityAdjustedSnapVelocity = (int) (displayMetrics.density * SNAP_VELOCITY_DIP_PER_SECOND);

        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    }

    public boolean isLeftSwipeValid() {
        return isLeftSwipeValid;
    }

    public boolean isRightSwipeValid() {
        return isRightSwipeValid;
    }

    @Override
    public boolean onInterceptTouchEvent(final MotionEvent event) {
        if (!isLeftSwipeValid && !isRightSwipeValid) {
            return false;
        }
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

                if (xDiff > mTouchSlop) {
                    isDragging = (xDiff >= yDiff)
                            && ((isLeftSwipeValid && x < mLastMotionX) || (isRightSwipeValid && x > mLastMotionX));
                } else if (yDiff > mTouchSlop) {
                    isDragging = false;
                }
            }
            break;
        case MotionEvent.ACTION_CANCEL:
        case MotionEvent.ACTION_UP:
            if (isDragging) {
                if (listener != null) {
                    listener.cancelSwipe();
                }
                isDragging = false;
            }
            break;
        default:
            break;
        }
        return isDragging;
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if (!isLeftSwipeValid && !isRightSwipeValid) {
            return false;
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }

        mVelocityTracker.addMovement(event);

        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();

        switch (action) {
        case MotionEvent.ACTION_DOWN:
            mLastMotionX = x;
            mLastMotionY = y;
            isDragging = true;
            break;
        case MotionEvent.ACTION_MOVE:
            if (!isDragging) {
                final int xDiff = (int) Math.abs(x - mLastMotionX);
                final int yDiff = (int) Math.abs(y - mLastMotionY);

                if (xDiff > mTouchSlop) {
                    isDragging = (xDiff >= yDiff)
                            && ((isLeftSwipeValid && x < mLastMotionX) || (isRightSwipeValid && x > mLastMotionX));
                } else if (yDiff > mTouchSlop) {
                    isDragging = false;
                }
            }

            if (isDragging) {
                final int deltaX = (int) (mLastMotionX - x);
                if (listener != null) {
                    listener.swipeTo(deltaX);
                }

                mLastMotionX = x;
                mLastMotionY = y;
            }
            break;
        case MotionEvent.ACTION_UP:
            if (isDragging) {
                mVelocityTracker.computeCurrentVelocity(VELOCITY_UNIT_PIXELS_PER_SECOND, mMaximumVelocity);
                final int velocityX = (int) mVelocityTracker.getXVelocity();
                if (listener != null) {
                    if (velocityX > mDensityAdjustedSnapVelocity) {
                        listener.swipeRight();
                    } else if (velocityX < -mDensityAdjustedSnapVelocity) {
                        listener.swipeLeft();
                    } else {
                        listener.cancelSwipe();
                    }
                }

                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
            }
            isDragging = false;
            break;
        case MotionEvent.ACTION_CANCEL:
            if (isDragging) {
                if (listener != null) {
                    listener.cancelSwipe();
                }
                isDragging = false;
            }
            break;
        default:
            break;
        }
        return true;
    }

    public void setLeftSwipeValid(final boolean isLeftSwipeValid) {
        this.isLeftSwipeValid = isLeftSwipeValid;
    }

    public void setListener(final HorizontalSwipeViewListener listener) {
        this.listener = listener;
    }

    public void setRightSwipeValid(final boolean isRightSwipeValid) {
        this.isRightSwipeValid = isRightSwipeValid;
    }
}
