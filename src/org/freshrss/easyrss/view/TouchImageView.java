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
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

public class TouchImageView extends ImageView {
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(final ScaleGestureDetector detector) {
            setScale(detector.getScaleFactor() * savedScale, detector.getFocusX(), detector.getFocusY());
            return true;
        }

        @Override
        public boolean onScaleBegin(final ScaleGestureDetector detector) {
            mode = STATE_ZOOM;
            return true;
        }
    }

    private static final int CLICK_DISTANCE = 10;
    // We can be in one of these 3 states
    private static final int STATE_NONE = 0;
    private static final int STATE_DRAG = 1;
    private static final int STATE_ZOOM = 2;

    final private Matrix matrix = new Matrix();
    private OnScaleChangedListener onScaleChangedListener;
    private int mode = STATE_NONE;
    private PointF last = new PointF();
    private PointF start = new PointF();
    private float minScale = 1.0f;
    private float maxScale = 2.0f;
    private float savedScale = 1.0f;
    private float width, height;
    private float bmWidth, bmHeight;
    private boolean autoFillScreen;
    final private ScaleGestureDetector mScaleDetector;

    public TouchImageView(final Context context) {
        super(context);

        this.mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        init();
    }

    public TouchImageView(final Context context, final AttributeSet attrs) {
        super(context, attrs);

        this.mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        init();
    }

    public float getMaxScale() {
        return maxScale;
    }

    public float getMinScale() {
        return minScale;
    }

    public OnScaleChangedListener getOnScaleChangedListener() {
        return onScaleChangedListener;
    }

    public float getScale() {
        return savedScale;
    }

    private void init() {
        autoFillScreen = false;
        setClickable(true);
        matrix.setTranslate(1f, 1f);
        setImageMatrix(matrix);
        setScaleType(ScaleType.MATRIX);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                mScaleDetector.onTouchEvent(event);

                final float m[] = new float[9];
                matrix.getValues(m);
                final float x = m[Matrix.MTRANS_X];
                final float y = m[Matrix.MTRANS_Y];
                final PointF curr = new PointF(event.getX(), event.getY());
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    last.set(event.getX(), event.getY());
                    start.set(last);
                    mode = STATE_DRAG;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == STATE_DRAG) {
                        float deltaX = curr.x - last.x;
                        float deltaY = curr.y - last.y;
                        final float scaleWidth = Math.round(bmWidth * savedScale);
                        final float scaleHeight = Math.round(bmHeight * savedScale);
                        if (scaleWidth < width) {
                            deltaX = 0;
                        } else if (x + deltaX > 0) {
                            deltaX = -x;
                        } else if (x + deltaX < -scaleWidth + width) {
                            deltaX = -(x + scaleWidth - width);
                        }
                        if (scaleHeight < height) {
                            deltaY = 0;
                        } else if (y + deltaY > 0) {
                            deltaY = -y;
                        } else if (y + deltaY < -scaleHeight + height) {
                            deltaY = -(y + scaleHeight - height);
                        }
                        matrix.postTranslate(deltaX, deltaY);
                        last.set(curr.x, curr.y);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mode = STATE_NONE;
                    int xDiff = (int) Math.abs(curr.x - start.x);
                    int yDiff = (int) Math.abs(curr.y - start.y);
                    if (xDiff < CLICK_DISTANCE && yDiff < CLICK_DISTANCE) {
                        performClick();
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mode = STATE_NONE;
                    break;
                }
                setImageMatrix(matrix);
                invalidate();
                return true;
            }
        });
    }

    public boolean isAutoFillScreen() {
        return autoFillScreen;
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = MeasureSpec.getSize(widthMeasureSpec);
        height = MeasureSpec.getSize(heightMeasureSpec);

        if (autoFillScreen) {
            float scale = 1.0f;
            scale = Math.min(scale, width / bmWidth);
            scale = Math.min(scale, height / bmHeight);
            if (scale < minScale) {
                minScale = scale;
            }
            savedScale = scale;
        }
        final float scaledWidth = Math.round(bmWidth * savedScale);
        final float scaledHeight = Math.round(bmHeight * savedScale);
        matrix.setScale(savedScale, savedScale);
        matrix.postTranslate((width - scaledWidth) / 2.0f, (height - scaledHeight) / 2.0f);
        setImageMatrix(matrix);
    }

    public void setAutoFillScreen(final boolean autoFillScreen) {
        this.autoFillScreen = autoFillScreen;
    }

    @Override
    public void setImageBitmap(final Bitmap bitmap) {
        super.setImageBitmap(bitmap);
        if (bitmap != null) {
            bmWidth = bitmap.getWidth();
            bmHeight = bitmap.getHeight();
        }
    }

    public void setMaxScale(final float maxScale) {
        this.maxScale = maxScale;
    }

    public void setMaxZoom(final float x) {
        maxScale = x;
    }

    public void setMinScale(final float minScale) {
        this.minScale = minScale;
    }

    public void setOnScaleChangedListener(final OnScaleChangedListener onScaleChangedListener) {
        this.onScaleChangedListener = onScaleChangedListener;
    }

    public void setScale(final float scale) {
        setScale(scale, width / 2.0f, height / 2.0f);
    }

    public void setScale(float scale, final float posX, final float posY) {
        if (scale > maxScale) {
            scale = maxScale;
        } else if (scale < minScale) {
            scale = minScale;
        }
        matrix.postScale(scale / savedScale, scale / savedScale, posX, posY);
        savedScale = scale;
        final float m[] = new float[9];
        matrix.getValues(m);
        final float x = m[Matrix.MTRANS_X];
        final float y = m[Matrix.MTRANS_Y];
        final float scaledWidth = Math.round(bmWidth * savedScale);
        final float scaledHeight = Math.round(bmHeight * savedScale);
        final float offX, offY;
        if (scaledWidth < width) {
            offX = (width - scaledWidth) / 2.0f - x;
        } else if (x > 0) {
            offX = -x;
        } else if (x < -scaledWidth + width) {
            offX = -scaledWidth + width - x;
        } else {
            offX = 0f;
        }
        if (scaledHeight < height) {
            offY = (height - scaledHeight) / 2.0f - y;
        } else if (y > 0) {
            offY = -y;
        } else if (y < -scaledHeight + height) {
            offY = -scaledHeight + height - y;
        } else {
            offY = 0f;
        }
        matrix.postTranslate(offX, offY);
        setImageMatrix(matrix);
        invalidate();
        if (onScaleChangedListener != null) {
            onScaleChangedListener.onScaleChanged(this, savedScale);
        }
    }
}
