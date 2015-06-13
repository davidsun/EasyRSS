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
import org.freshrss.easyrss.data.DataUtils;
import org.freshrss.easyrss.data.OnSettingUpdatedListener;
import org.freshrss.easyrss.data.OnSubscriptionUpdatedListener;
import org.freshrss.easyrss.data.OnTagUpdatedListener;
import org.freshrss.easyrss.data.Setting;
import org.freshrss.easyrss.data.Subscription;
import org.freshrss.easyrss.data.Tag;
import org.freshrss.easyrss.data.readersetting.SettingFontSize;
import org.freshrss.easyrss.listadapter.AbsListItem;
import org.freshrss.easyrss.listadapter.ListAdapter;
import org.freshrss.easyrss.listadapter.ListItemEmpty;
import org.freshrss.easyrss.listadapter.ListItemSubTag;
import org.freshrss.easyrss.listadapter.ListItemTitle;
import org.freshrss.easyrss.listadapter.OnItemTouchListener;

import android.content.Context;
import android.widget.ListView;

class HomeListWrapper implements OnTagUpdatedListener, OnSubscriptionUpdatedListener, OnSettingUpdatedListener {
    public enum HomeListWrapperType {
        TYPE_ALL, TYPE_UNREAD, TYPE_STARRED,
    }

    final private HomeListWrapperType type;
    final private ListAdapter adapter;
    final private ListView view;
    final private DataMgr dataMgr;
    private int subscriptionCount;
    private int tagCount;

    public HomeListWrapper(final DataMgr dataMgr, final ListView view, final HomeListWrapperType type,
            final int fontSize) {
        this.dataMgr = dataMgr;
        this.view = view;
        this.type = type;
        this.subscriptionCount = 0;
        this.tagCount = 0;

        final Context context = view.getContext();
        this.adapter = new ListAdapter(context, fontSize);

        view.setAdapter(adapter);
        view.setItemsCanFocus(false);
        view.setFocusable(false);
        view.setDivider(null);

        final int unreadCount = dataMgr.getGlobalUnreadCount();
        adapter.updateItem(new ListItemTitle(AbsListItem.ID_TITLE_ALL, context.getString(getFirstTitleResId())));
        adapter.updateItem(new ListItemSubTag("", context.getString(getFirstItemTitleResId()),
                showNumber() ? unreadCount : 0, view.getResources(), getDefaultIconResId()));
        adapter.updateItem(new ListItemTitle(AbsListItem.ID_TITLE_TAGS, context.getString(R.string.TxtTags)));
        adapter.updateItem(new ListItemEmpty(AbsListItem.ID_TAGS_EMPTY, context.getString(R.string.TxtNoMoreTags)));
        adapter.updateItem(new ListItemTitle(AbsListItem.ID_TITLE_SUBSCRIPTIONS, context
                .getString(R.string.TxtSubscriptions)));
        adapter.updateItem(new ListItemEmpty(AbsListItem.ID_SUBSCRIPTIONS_EMPTY, context
                .getString(R.string.TxtNoMoreSubscriptions)));
    }

    private int getDefaultIconResId() {
        final int ret;
        switch (type) {
        case TYPE_ALL:
            ret = R.drawable.icon_all_12;
            break;
        case TYPE_STARRED:
            ret = R.drawable.icon_star_12;
            break;
        case TYPE_UNREAD:
            ret = R.drawable.icon_unread_12;
            break;
        default:
            ret = 0;
            break;
        }
        return ret;
    }

    private int getFirstItemTitleResId() {
        final int ret;
        switch (type) {
        case TYPE_ALL:
            ret = R.string.TxtAllItems;
            break;
        case TYPE_STARRED:
            ret = R.string.TxtAllStarredItems;
            break;
        case TYPE_UNREAD:
            ret = R.string.TxtAllUnreadItems;
            break;
        default:
            ret = 0;
            break;
        }
        return ret;
    }

    private int getFirstTitleResId() {
        final int ret;
        switch (type) {
        case TYPE_ALL:
            ret = R.string.TxtAll;
            break;
        case TYPE_STARRED:
            ret = R.string.TxtStarred;
            break;
        case TYPE_UNREAD:
            ret = R.string.TxtUnread;
            break;
        default:
            ret = 0;
            break;
        }
        return ret;
    }

    private boolean hideZeroItems() {
        return type == HomeListWrapperType.TYPE_UNREAD;
    }

    public void setAdapterListener(final OnItemTouchListener itemTouchListener) {
        adapter.setListener(itemTouchListener);
    }

    @Override
    public void onSettingUpdated(final String name) {
        if (name.equals(Setting.SETTING_FONT_SIZE)) {
            final int fontSize = new SettingFontSize(dataMgr).getData();
            adapter.setFontSize(fontSize);
        } else if (name.equals(Setting.SETTING_GLOBAL_ITEM_UNREAD_COUNT)) {
            updateGlobalUnreadCount(dataMgr.getGlobalUnreadCount());
        }
    }

    private boolean showNumber() {
        return type != HomeListWrapperType.TYPE_STARRED;
    }

    private void updateGlobalUnreadCount(final int unreadCount) {
        adapter.updateItem(new ListItemSubTag("", view.getContext().getString(getFirstItemTitleResId()),
                showNumber() ? unreadCount : 0, view.getResources(), getDefaultIconResId()));
    }

    @Override
    public void onSubscriptionUpdated(final Subscription sub) {
        int loc = adapter.getCount();
        boolean erased = false, updated = false;
        if (hideZeroItems() && sub.getUnreadCount() <= 0) {
            final Integer itemLoc = adapter.getItemLocationById(sub.getUid());
            if (itemLoc != null) {
                adapter.removeItem(itemLoc);
                subscriptionCount--;
                erased = true;
            }
            loc--;
        } else {
            if (sub.getIcon() == null) {
                if (!adapter.updateItem(
                        new ListItemSubTag(sub.getUid(), sub.getTitle(), showNumber() ? sub.getUnreadCount() : 0, view
                                .getResources(), R.drawable.icon_subscription_16), loc)) {
                    subscriptionCount++;
                }
            } else {
                if (!adapter.updateItem(
                        new ListItemSubTag(sub.getUid(), sub.getTitle(), showNumber() ? sub.getUnreadCount() : 0, sub
                                .getIcon()), loc)) {
                    subscriptionCount++;
                }
            }
            updated = true;
        }
        if (erased && subscriptionCount == 0) {
            adapter.updateItem(
                    new ListItemEmpty(AbsListItem.ID_SUBSCRIPTIONS_EMPTY, view.getContext().getString(
                            R.string.TxtNoMoreSubscriptions)), loc);
        } else if (updated) {
            final Integer locEnd = adapter.getItemLocationById(AbsListItem.ID_SUBSCRIPTIONS_EMPTY);
            if (locEnd != null) {
                adapter.removeItem(locEnd);
            }
        }
    }

    @Override
    public void onTagUpdated(final Tag tag) {
        if (DataUtils.isUserTagUid(tag.getUid())) {
            int loc = adapter.getItemLocationById(AbsListItem.ID_TITLE_SUBSCRIPTIONS);
            boolean erased = false, updated = false;
            if (hideZeroItems() && tag.getUnreadCount() <= 0) {
                final Integer itemLoc = adapter.getItemLocationById(tag.getUid());
                if (itemLoc != null) {
                    adapter.removeItem(itemLoc);
                    tagCount--;
                    erased = true;
                }
                loc--;
            } else {
                if (!adapter.updateItem(
                        new ListItemSubTag(tag.getUid(), tag.getTitle(), showNumber() ? tag.getUnreadCount() : 0, view
                                .getResources(), R.drawable.icon_rss_16), loc)) {
                    tagCount++;
                }
                updated = true;
            }
            if (erased && tagCount == 0) {
                adapter.updateItem(
                        new ListItemEmpty(AbsListItem.ID_TAGS_EMPTY, view.getContext()
                                .getString(R.string.TxtNoMoreTags)), loc);
            } else if (updated) {
                final Integer locEnd = adapter.getItemLocationById(AbsListItem.ID_TAGS_EMPTY);
                if (locEnd != null) {
                    adapter.removeItem(locEnd);
                }
            }
        }
    }
}
