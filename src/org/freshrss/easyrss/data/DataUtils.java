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

package org.freshrss.easyrss.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.FastHtmlSerializer;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;

import org.freshrss.easyrss.R;

import android.content.Context;
import android.content.Intent;
import android.text.ClipboardManager;
import android.text.Html;
import android.widget.Toast;

@SuppressWarnings("deprecation")
final public class DataUtils {
    public final static String DEFAULT_NORMAL_CSS = "<style type='text/css'>"
            + "html{background:#EFEFEF;color:#444;line-height:140%;}"
            + "a{color:#222;font-weight:bold;text-decoration:none;border-bottom:1px #999 dashed;}"
            + "img{max-width:100%;overflow:hidden;height:auto;}" + "</style>";
    public final static String DEFAULT_DARK_CSS = "<style type='text/css'>"
            + "html{background:#101010;color:#BBB;line-height:140%;}"
            + "a{color:#EEE;font-weight:bold;text-decoration:none;border-bottom:1px #777 dashed;}"
            + "img{max-width:100%;overflow:hidden;height:auto;}" + "</style>";
    public final static String DEFAULT_JS = "<script>window.onload=function(){var pics=document.getElementsByTagName('img');for(var i=0;i<pics.length;i++){var pic=pics[i];pic.onclick=function(){alert(this.getAttribute('src'));};}}</script>";

    public static long calcFileSpace(final File dir) {
        if (!dir.exists()) {
            return 0;
        }
        long ret = 0;
        if (dir.isDirectory()) {
            final File[] filies = dir.listFiles();
            for (int i = 0; i < filies.length; i++) {
                final File curFile = filies[i];
                if (curFile.isDirectory()) {
                    ret += calcFileSpace(curFile);
                } else {
                    ret += curFile.length();
                }
            }
        }
        return ret;
    }

    public static void deleteFile(final File dir) {
        if (!dir.exists()) {
            return;
        }
        if (dir.isDirectory()) {
            final File[] filies = dir.listFiles();
            for (int i = 0; i < filies.length; i++) {
                final File curFile = filies[i];
                if (curFile.isDirectory()) {
                    deleteFile(curFile);
                } else {
                    curFile.delete();
                }
            }
            dir.delete();
        }
    }

    public static String getAppFolderPath() {
        return android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "EasyRSS";
    }

    public static boolean isReadUid(final String uid) {
        return uid.endsWith("/state/com.google/read");
    }

    public static boolean isStarredUid(final String uid) {
        return uid.endsWith("/state/com.google/starred");
    }

    public static boolean isSubscriptionUid(final String uid) {
        return uid.startsWith("feed/");
    }

    public static boolean isTagUid(final String uid) {
        return uid.startsWith("user/") && (uid.indexOf("/state/") != -1 || uid.indexOf("/label/") != -1);
    }

    public static boolean isUserTagUid(final String uid) {
        return uid.startsWith("user/") && uid.indexOf("/label/") != -1;
    }

    public static String readFromFile(final File file) {
        final StringBuilder ret = new StringBuilder();
        try {
            final BufferedReader input = new BufferedReader(new FileReader(file), 4096);
            int len;
            final char buff[] = new char[4096];
            while ((len = input.read(buff, 0, 4096)) != -1) {
                ret.append(buff, 0, len);
            }
            input.close();
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
        return ret.toString();
    }

    public static void sendContentTo(final Context context, final Item item) {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, Html.fromHtml(item.getTitle()).toString());
        final HtmlCleaner cleaner = new HtmlCleaner();
        try {
            final TagNode[] bodyNode = cleaner.clean(new File(item.getOriginalContentStoragePath())).getElementsByName(
                    "body", true);
            if (bodyNode.length > 0) {
                intent.putExtra(Intent.EXTRA_TEXT, Html.fromHtml(item.getTitle()) + "\n\n" + item.getHref() + "\n\n"
                        + bodyNode[0].getText() + "\n(" + context.getString(R.string.TxtViaEasyRSS) + ")");
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.TxtSendTo)));
            }
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void sendHtmlContentTo(final Context context, final Item item) {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/html");
        intent.putExtra(Intent.EXTRA_SUBJECT, Html.fromHtml(item.getTitle()).toString());
        try {
            final HtmlCleaner cleaner = new HtmlCleaner();
            final TagNode[] bodyNode = cleaner.clean(new File(item.getOriginalContentStoragePath())).getElementsByName(
                    "body", true);
            if (bodyNode.length > 0) {
                intent.putExtra(
                        Intent.EXTRA_TEXT,
                        Html.fromHtml("<p><strong>"
                                + item.getTitle()
                                + "</strong></p><p>Published on <a href='"
                                + item.getHref()
                                + "'>"
                                + item.getSourceTitle()
                                + "</a></p><p>"
                                + cleaner.getInnerHtml(bodyNode[0])
                                + "</p><p>("
                                + context.getString(R.string.TxtViaEasyRSS)
                                + " | <a href='https://market.android.com/details?id=org.freshrss.easyrss'>Make it simple & elegant!</a>)</p>"));
                context.startActivity(Intent.createChooser(intent, context.getString(R.string.TxtSendTo)));
            }
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void copyToClipboard(final Context context, final Item item) {
        final String text = Html.fromHtml(item.getTitle()) + ", " + item.getHref() + " ("
                + context.getString(R.string.TxtViaEasyRSS) + ")";
        final ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setText(text);
        Toast.makeText(context, R.string.MsgCopiedToClipboard, Toast.LENGTH_LONG).show();
    }

    public static void sendTo(final Context context, final Item item) {
        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, Html.fromHtml(item.getTitle()).toString());
        intent.putExtra(
                Intent.EXTRA_TEXT,
                Html.fromHtml(item.getTitle()) + ", " + item.getHref() + " ("
                        + context.getString(R.string.TxtViaEasyRSS) + ")");
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.TxtSendTo)));
    }

    public static void streamTransfer(final InputStream in, final OutputStream out) {
        final byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (final IOException exception) {
            exception.printStackTrace();
        }
    }

    public static void writeItemToFile(final Item item) throws IOException {
        final File fdir = new File(item.getStoragePath());
        fdir.mkdirs();
        final String content = item.getContent();
        final HtmlCleaner cleaner = new HtmlCleaner();
        final TagNode node = cleaner.clean((content == null) ? "" : content);
        final List<TagNode> imgList = new ArrayList<TagNode>();
        final Queue<TagNode> nodes = new LinkedList<TagNode>();
        nodes.add(node);
        while (!nodes.isEmpty()) {
            final TagNode tag = nodes.poll();
            final String tagName = tag.getName().toLowerCase();
            if ("frame".equals(tagName) || "iframe".equals(tagName) || "script".equals(tagName)) {
                tag.removeFromTree();
            } else if ("img".equals(tagName)) {
                tag.removeAttribute("style");
                final String src = tag.getAttributeByName("src");
                if (src != null && (src.startsWith("http://") || src.startsWith("https://"))) {
                    tag.removeAttribute("width");
                    tag.removeAttribute("height");
                    imgList.add(tag);
                } else {
                    tag.removeFromTree();
                }
            } else if (tag.hasChildren()) {
                nodes.addAll(tag.getChildTagList());
            }
        }
        final CleanerProperties prop = cleaner.getProperties();
        prop.setOmitXmlDeclaration(false);
        final FastHtmlSerializer serializer = new FastHtmlSerializer(prop);
        {
            final OutputStream out = new FileOutputStream(new File(item.getOriginalContentStoragePath()));
            serializer.writeToStream(node, out);
            out.close();
        }
        for (final TagNode tag : imgList) {
            tag.removeFromTree();
        }
        {
            final OutputStream out = new FileOutputStream(new File(item.getStrippedContentStoragePath()));
            serializer.writeToStream(node, out);
            out.close();
        }
    }

    private DataUtils() {
    }
}
