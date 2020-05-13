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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.freshrss.easyrss.R;
import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.view.AbsViewCtrl;
import org.freshrss.easyrss.view.OnScaleChangedListener;
import org.freshrss.easyrss.view.TouchImageView;

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


public class ImageViewCtrl extends AbsViewCtrl implements ItemListWrapperListener {
    static final private Handler handler = new Handler() {
        @Override
        public void handleMessage(final Message msg) {
            if (msg.obj instanceof ImageViewCtrl) {
                final ImageViewCtrl viewCtrl = (ImageViewCtrl) msg.obj;
                switch (msg.what) {
                case MSG_IMG_FAILED:
                    viewCtrl.view.findViewById(R.id.Loading).setVisibility(View.GONE);
                    viewCtrl.view.findViewById(R.id.Image).setVisibility(View.GONE);
                    viewCtrl.view.findViewById(R.id.ZoomControl).setVisibility(View.GONE);
                    viewCtrl.view.findViewById(R.id.Failed).setVisibility(View.VISIBLE);
                    break;
                case MSG_IMG_READY:
                    final TouchImageView imageView = (TouchImageView) viewCtrl.view.findViewById(R.id.Image);
                    final FrameLayout zoomWrap = (FrameLayout) viewCtrl.view.findViewById(R.id.ZoomControl);
                    final ZoomControls zoomControls = new ZoomControls(viewCtrl.context);
                    final View btnSave = viewCtrl.view.findViewById(R.id.BtnSave);
                    final Bitmap image = (Bitmap) viewCtrl.bitmap;
                    btnSave.setVisibility(View.VISIBLE);
                    btnSave.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(final View view) {
                            try {
                                final String path = android.os.Environment.getExternalStorageDirectory()
                                        .getAbsolutePath()
                                        + File.separator
                                        + "EasyRSS-"
                                        + Integer.toHexString(viewCtrl.imgPath.hashCode()) + ".jpeg";
                                final FileOutputStream output = new FileOutputStream(path);
                                Toast.makeText(viewCtrl.context,
                                        viewCtrl.context.getString(R.string.MsgSavingImageTo) + " " + path + " ...",
                                        Toast.LENGTH_LONG).show();
                                image.compress(Bitmap.CompressFormat.JPEG, 100, output);
                            } catch (final Exception exception) {
                                exception.printStackTrace();
                            }
                        }
                    });
                    viewCtrl.view.findViewById(R.id.Loading).setVisibility(View.GONE);
                    imageView.setVisibility(View.VISIBLE);
                    zoomWrap.setVisibility(View.VISIBLE);
                    viewCtrl.view.findViewById(R.id.Failed).setVisibility(View.GONE);
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
        }
    };
    static final private int MSG_IMG_FAILED = 0;
    static final private int MSG_IMG_READY = 1;

    final private String imgPath;
    private Bitmap bitmap;

    public ImageViewCtrl(final DataMgr dataMgr, final Context context, final String imgPath) {
        super(dataMgr, R.layout.image, context);

        this.imgPath = imgPath;
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
                        ImageViewCtrl.this.bitmap = bitmap;
                        handler.sendMessage(handler.obtainMessage(MSG_IMG_READY, ImageViewCtrl.this));
                    } else {
                        handler.sendMessage(handler.obtainMessage(MSG_IMG_FAILED, ImageViewCtrl.this));
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
                            ImageViewCtrl.this.bitmap = bitmap;
                            handler.sendMessage(handler.obtainMessage(MSG_IMG_READY, ImageViewCtrl.this));
                        } else {
                            handler.sendMessage(handler.obtainMessage(MSG_IMG_FAILED, ImageViewCtrl.this));
                        }
                    } catch (final MalformedURLException exception) {
                        exception.printStackTrace();
                        handler.sendMessage(handler.obtainMessage(MSG_IMG_FAILED, ImageViewCtrl.this));
                    } catch (final IOException exception) {
                        exception.printStackTrace();
                        handler.sendMessage(handler.obtainMessage(MSG_IMG_FAILED, ImageViewCtrl.this));
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

    @Override
    public void onNeedMoreItems() {
        // TODO Auto-generated method stub
    }
}
