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

package org.freshrss.easyrss.network;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.DefaultTagProvider;
import org.htmlcleaner.FastHtmlSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.util.Pair;

import org.freshrss.easyrss.R;
import org.freshrss.easyrss.data.DataMgr;
import org.freshrss.easyrss.data.DataUtils;
import org.freshrss.easyrss.data.Item;
import org.freshrss.easyrss.data.ItemState;
import org.freshrss.easyrss.data.readersetting.SettingImagePrefetching;

// Remember that in this class, the meaning networkConfig is different from other syncers!
public class ItemContentDataSyncer extends AbsDataSyncer {
    private class FetchingHelper {
        final private ExecutorService execService;
        final private List<ItemWrapper> wrappers;
        private int totalItems;
        private int finishedItems;

        public FetchingHelper() {
            this.execService = Executors.newFixedThreadPool(FETCHING_THREAD_COUNT, new ThreadFactory() {
                @Override
                public Thread newThread(final Runnable runnable) {
                    final Thread thread = new Thread(runnable);
                    thread.setPriority(Thread.MIN_PRIORITY);
                    return thread;
                }
            });
            this.wrappers = new ArrayList<ItemWrapper>();
            this.totalItems = 0;
            this.finishedItems = 0;
        }

        public void fetch() {
            final Context context = dataMgr.getContext();
            final ContentResolver resolver = context.getContentResolver();
            final Cursor cur = resolver.query(Item.CONTENT_URI, new String[] { "count(*)" },
                    ItemState._ISCACHED + "=0", null, null);
            if (cur.moveToFirst()) {
                totalItems = cur.getInt(0);
            } else {
                totalItems = 0;
            }
            notifyProgressChanged(context.getString(R.string.TxtSyncingItemContent), finishedItems, totalItems);
            for (int i = 0; i < FETCHING_THREAD_COUNT; i++) {
                execService.execute(new FetchingProcess(this));
            }
            execService.shutdown();
            try {
                execService.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            } catch (final InterruptedException exception) {
                exception.printStackTrace();
            }
            wrappers.clear();
        }

        public ItemWrapper getNextItemWrapper() {
            synchronized (wrappers) {
                final Context context = dataMgr.getContext();
                if (!NetworkUtils.checkImageFetchingNetworkStatus(context, networkConfig)) {
                    return null;
                }
                while (!wrappers.isEmpty() && wrappers.get(0).isFinished()) {
                    finishedItems++;
                    notifyProgressChanged(context.getString(R.string.TxtSyncingItemContent), finishedItems, totalItems);
                    wrappers.remove(0);
                }
                if (!wrappers.isEmpty()) {
                    return wrappers.get(0);
                }
                final ContentResolver resolver = context.getContentResolver();
                final Cursor cur;
                cur = resolver.query(Uri.withAppendedPath(Item.CONTENT_URI, "limit/10"), ITEM_PROJECTION,
                        ItemState._ISCACHED + "=0", null, null);
                for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
                    wrappers.add(new ItemWrapper(Item.fromCursor(cur)));
                }
                cur.close();
                return wrappers.isEmpty() ? null : wrappers.get(0);
            }
        }
    }

    private class FetchingProcess implements Runnable {
        final private FetchingHelper helper;

        public FetchingProcess(final FetchingHelper helper) {
            super();

            this.helper = helper;
        }

        @Override
        public void run() {
            while (true) {
                final ItemWrapper wrapper = helper.getNextItemWrapper();
                if (wrapper == null) {
                    break;
                }
                final Pair<Integer, String> psi = wrapper.downloadNextImage();
                if (psi != null) {
                    final int picId = psi.first;
                    final String src = psi.second;
                    final String sDStateString = Environment.getExternalStorageState();
                    if (!sDStateString.equals(android.os.Environment.MEDIA_MOUNTED)) {
                        wrapper.onFinishImageFetching(picId, DOWNLOADING_STATUS_FILE_ERROR);
                        break;
                    } else {
                        final File file = new File(wrapper.getItem().getImageStoragePath(picId));
                        if (file.isDirectory()) {
                            DataUtils.deleteFile(file);
                        }
                        try {
                            if (!file.exists()) {
                                final URLConnection connection = new URL(src).openConnection();
                                connection.setConnectTimeout(30 * 1000);
                                connection.setReadTimeout(20 * 1000);
                                final InputStream input = connection.getInputStream();
                                final OutputStream out = new FileOutputStream(file);
                                final byte buff[] = new byte[CONTENT_IO_BUFFER_SIZE];
                                int len;
                                while ((len = input.read(buff)) != -1) {
                                    out.write(buff, 0, len);
                                }
                                out.close();
                                try {
                                    input.close();
                                } catch (final IOException exception) {
                                    exception.printStackTrace();
                                }
                            }
                            wrapper.onFinishImageFetching(picId, DOWNLOADING_STATUS_SUCCEEDED);
                        } catch (final Exception exception) {
                            wrapper.onFinishImageFetching(picId, DOWNLOADING_STATUS_NETWORK_ERROR);
                            exception.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    private class ItemWrapper {
        final private Item item;
        final private List<TagNode> imgList;
        final private TagNode root;
        private boolean hasFileError;
        private int downloadedImageCount;

        public ItemWrapper(final Item item) {
            this.item = item;
            this.imgList = new ArrayList<TagNode>();
            this.root = new HtmlCleaner().clean(DataUtils.readFromFile(new File(item.getOriginalContentStoragePath())));
            final Queue<TagNode> nodes = new LinkedList<TagNode>();
            nodes.add(root);
            while (!nodes.isEmpty()) {
                final TagNode tag = nodes.poll();
                final String tagName = tag.getName();
                if ("img".equals(tagName)) {
                    final String src = tag.getAttributeByName("src");
                    if (src != null && (src.startsWith("http://") || src.startsWith("https://"))) {
                        imgList.add(tag);
                    } else {
                        tag.removeFromTree();
                    }
                } else if (tag.hasChildren()) {
                    nodes.addAll(tag.getChildTagList());
                }
            }
            this.hasFileError = false;
            this.downloadedImageCount = 0;

            if (isSucceeded()) {
                markAsCached();
            }
        }

        public synchronized Pair<Integer, String> downloadNextImage() {
            if (downloadedImageCount < imgList.size()) {
                final TagNode ret = imgList.get(downloadedImageCount);
                downloadedImageCount++;
                return Pair.create(downloadedImageCount, ret.getAttributeByName("src"));
            } else {
                return null;
            }
        }

        public Item getItem() {
            return item;
        }

        public boolean isFinished() {
            return (downloadedImageCount >= imgList.size());
        }

        public boolean isSucceeded() {
            return (isFinished() && !hasFileError);
        }

        private void markAsCached() {
            final CleanerProperties prop = new CleanerProperties();
            prop.setTagInfoProvider(DefaultTagProvider.getInstance());
            prop.setOmitXmlDeclaration(false);
            final FastHtmlSerializer serializer = new FastHtmlSerializer(prop);
            try {
                final OutputStream out = new BufferedOutputStream(new FileOutputStream(new File(
                        item.getFullContentStoragePath())), 8192);
                serializer.writeToStream(root, out);
                out.close();
                final ContentResolver resolver = dataMgr.getContext().getContentResolver();
                final ContentValues values = new ContentValues(1);
                values.put(ItemState._ISCACHED, true);
                resolver.update(Item.CONTENT_URI, values, Item._UID + "=?", new String[] { item.getUid() });
            } catch (final IOException exception) {
                exception.printStackTrace();
            }
        }

        public synchronized void onFinishImageFetching(final int id, final int status) {
            final TagNode ele = imgList.get(id - 1);
            if (status == DOWNLOADING_STATUS_FILE_ERROR) {
                hasFileError = true;
                downloadedImageCount = imgList.size();
                return;
            }
            ele.setAttribute("src", id + ".erss");
            if (status != DOWNLOADING_STATUS_SUCCEEDED) {
                final InputStream input = dataMgr.getContext().getResources().openRawResource(R.raw.no_such_picture);
                try {
                    final FileOutputStream output = new FileOutputStream(item.getImageStoragePath(id));
                    DataUtils.streamTransfer(input, output);
                } catch (final FileNotFoundException exception) {
                    exception.printStackTrace();
                }
            }
            if (isSucceeded()) {
                markAsCached();
            }
        }
    }

    private final static String[] ITEM_PROJECTION = { Item._UID };
    final private static int DOWNLOADING_STATUS_SUCCEEDED = 0;
    final private static int DOWNLOADING_STATUS_NETWORK_ERROR = 1;
    final private static int DOWNLOADING_STATUS_FILE_ERROR = 2;
    final private static int FETCHING_THREAD_COUNT = 5;

    private static ItemContentDataSyncer instance;

    private static synchronized void clearInstance() {
        instance = null;
    }

    public static synchronized ItemContentDataSyncer getInstance(final DataMgr dataMgr, final int networkConfig) {
        if (instance == null) {
            instance = new ItemContentDataSyncer(dataMgr, networkConfig);
        }
        return instance;
    }

    private ItemContentDataSyncer(final DataMgr dataMgr, final int networkConfig) {
        super(dataMgr, networkConfig);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else {
            return (obj instanceof ItemContentDataSyncer);
        }
    }

    @Override
    protected void finishSyncing() {
        clearInstance();
    }

    @Override
    public void startSyncing() throws DataSyncerException {
        final SettingImagePrefetching sImgPrefetch = new SettingImagePrefetching(dataMgr);
        if (sImgPrefetch.getData()) {
            final FetchingHelper helper = new FetchingHelper();
            helper.fetch();
        }
    }
}
