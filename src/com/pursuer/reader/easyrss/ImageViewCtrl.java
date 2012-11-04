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

package com.pursuer.reader.easyrss;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.Toast;
import android.widget.ZoomControls;

import com.pursuer.reader.easyrss.data.DataMgr;
import com.pursuer.reader.easyrss.view.AbsViewCtrl;
import com.pursuer.reader.easyrss.view.OnScaleChangedListener;
import com.pursuer.reader.easyrss.view.TouchImageView;

public class ImageViewCtrl extends AbsViewCtrl implements ItemListWrapperListener {
    final private static int MSG_IMG_FAILED = 0;
    final private static int MSG_IMG_READY = 1;

    final private String imgPath;
    final private Handler handler;

    @SuppressLint("HandlerLeak")
    public ImageViewCtrl(final DataMgr dataMgr, final Context context, final String imgPath) {
        super(dataMgr, R.layout.image, context);

        this.imgPath = imgPath;
        this.handler = new Handler() {
            @Override
            public void handleMessage(final Message msg) {
                switch (msg.what) {
                case MSG_IMG_FAILED:
                    view.findViewById(R.id.Loading).setVisibility(View.GONE);
                    view.findViewById(R.id.Image).setVisibility(View.GONE);
                    view.findViewById(R.id.ZoomControl).setVisibility(View.GONE);
                    view.findViewById(R.id.Failed).setVisibility(View.VISIBLE);
                    break;
                case MSG_IMG_READY:
                    final TouchImageView imageView = (TouchImageView) view.findViewById(R.id.Image);
                    final FrameLayout zoomWrap = (FrameLayout) view.findViewById(R.id.ZoomControl);
                    final ZoomControls zoomControls = new ZoomControls(context);
                    final View btnSave = view.findViewById(R.id.BtnSave);
                    final Bitmap image = (Bitmap) msg.obj;
                    btnSave.setVisibility(View.VISIBLE);
                    btnSave.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(final View view) {
                            try {
                                final String path = android.os.Environment.getExternalStorageDirectory()
                                        .getAbsolutePath()
                                        + File.separator
                                        + "EasyRSS-"
                                        + Integer.toHexString(imgPath.hashCode()) + ".jpeg";
                                final FileOutputStream output = new FileOutputStream(path);
                                Toast.makeText(context,
                                        context.getString(R.string.MsgSavingImageTo) + " " + path + " ...",
                                        Toast.LENGTH_LONG).show();
                                image.compress(Bitmap.CompressFormat.JPEG, 100, output);
                            } catch (final Exception exception) {
                                exception.printStackTrace();
                            }
                        }
                    });
                    view.findViewById(R.id.Loading).setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    zoomWrap.setVisibility(View.VISIBLE);
                    view.findViewById(R.id.Failed).setVisibility(View.GONE);
                    imageView.setAutoFillScreen(true);
                    imageView.setImageBitmap(image);
                    imageView.setOnScaleChangedListener(new OnScaleChangedListener() {
                        @Override
                        public void onScaleChanged(final TouchImageView view, final float scale) {
                            zoomControls.setIsZoomInEnabled(view.getScale() < view.getMaxScale());
                            zoomControls.setIsZoomOutEnabled(view.getScale() > view.getMinScale());
                        }
                    });
                    zoomWrap.addView(zoomControls);
                    zoomControls.setIsZoomInEnabled(imageView.getScale() < imageView.getMaxScale());
                    zoomControls.setIsZoomOutEnabled(imageView.getScale() > imageView.getMinScale());
                    zoomControls.setOnZoomInClickListener(new OnClickListener() {
                        @Override
                        public void onClick(final View view) {
                            imageView.setScale(imageView.getScale() * 1.5f);
                        }
                    });
                    zoomControls.setOnZoomOutClickListener(new OnClickListener() {
                        @Override
                        public void onClick(final View view) {
                            imageView.setScale(imageView.getScale() / 1.5f);
                        }
                    });
                    break;
                default:
                    break;
                }
            }
        };
    }

    @Override
    public void onNeedMoreItems() {
        // TODO Auto-generated method stub
    }

    @Override
    public void onActivate() {
        view.findViewById(R.id.BtnClose).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (listener != null) {
                    listener.onBackNeeded();
                }
            }
        });
        final Thread thread = new Thread() {
            @Override
            public void run() {
                if (imgPath.endsWith(".erss")) {
                    final Bitmap bitmap = BitmapFactory.decodeFile(imgPath);
                    if (bitmap != null) {
                        final Message message = handler.obtainMessage(MSG_IMG_READY, bitmap);
                        handler.sendMessage(message);
                    } else {
                        handler.sendEmptyMessage(MSG_IMG_FAILED);
                    }
                } else {
                    try {
                        final URLConnection connection = new URL(imgPath).openConnection();
                        connection.setConnectTimeout(30 * 1000);
                        connection.setReadTimeout(20 * 1000);
                        final InputStream inputStream = connection.getInputStream();
                        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        final byte[] buff = new byte[8192];
                        int len;
                        while ((len = inputStream.read(buff)) != -1) {
                            outputStream.write(buff, 0, len);
                        }
                        outputStream.flush();
                        final byte[] imageBytes = outputStream.toByteArray();
                        final Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                        if (bitmap != null) {
                            final Message message = handler.obtainMessage(MSG_IMG_READY, bitmap);
                            handler.sendMessage(message);
                        } else {
                            handler.sendEmptyMessage(MSG_IMG_FAILED);
                        }
                    } catch (final MalformedURLException exception) {
                        exception.printStackTrace();
                        handler.sendEmptyMessage(MSG_IMG_FAILED);
                    } catch (final IOException exception) {
                        exception.printStackTrace();
                        handler.sendEmptyMessage(MSG_IMG_FAILED);
                    }
                }
            }
        };
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
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
        // TODO Auto-generated method stub
    }
}
