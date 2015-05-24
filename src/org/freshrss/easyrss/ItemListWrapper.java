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

import org.freshrss.easyrss.data.Item;
import org.freshrss.easyrss.data.OnItemUpdatedListener;
import org.freshrss.easyrss.listadapter.AbsListItem;
import org.freshrss.easyrss.listadapter.ListAdapter;
import org.freshrss.easyrss.listadapter.ListItemEndDisabled;
import org.freshrss.easyrss.listadapter.ListItemEndEnabled;
import org.freshrss.easyrss.listadapter.ListItemEndLoading;
import org.freshrss.easyrss.listadapter.ListItemItem;
import org.freshrss.easyrss.listadapter.ListItemTitle;
import org.freshrss.easyrss.listadapter.OnItemTouchListener;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;


public class ItemListWrapper implements OnItemUpdatedListener {
    final private ListAdapter adapter;
    private ItemListWrapperListener listener;

    public ItemListWrapper(final ListView view, final int fontSize) {
        this.adapter = new ListAdapter(view.getContext(), fontSize);
        view.setAdapter(adapter);
        view.setItemsCanFocus(false);
        view.setFocusable(false);
        view.setDivider(null);
        view.setOnScrollListener(new OnScrollListener() {
            @Override
            public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount,
                    final int totalItemCount) {
                if (firstVisibleItem + visibleItemCount + 10 >= totalItemCount && listener != null) {
                    listener.onNeedMoreItems();
                }
            }

            @Override
            public void onScrollStateChanged(final AbsListView view, final int scrollState) {
                // TODO Auto-generated method stub
            }
        });
    }

    public ListAdapter getAdapter() {
        return adapter;
    }

    public ItemListWrapperListener getListener() {
        return listener;
    }

    @Override
    public void onItemUpdated(final Item item) {
        if (adapter.hasItem(item.getUid())) {
            updateItem(item);
        }
    }

    public void removeItemEnd() {
        final Integer loc = adapter.getItemLocationById(AbsListItem.ID_END);
        if (loc != null) {
            adapter.removeItem(loc);
        }
    }

    public void setAdapterListener(final OnItemTouchListener onItemTouchListener) {
        adapter.setListener(onItemTouchListener);
    }

    public void setListener(final ItemListWrapperListener listener) {
        this.listener = listener;
    }

    public void updateItem(final Item item) {
        adapter.updateItem(new ListItemItem(item.getUid(), item.getTitle(), item.getSourceTitle(), item.getState()
                .isRead(), item.getState().isStarred(), item.getTimestamp()));
    }

    public void updateItemEndDisabled() {
        adapter.updateItem(new ListItemEndDisabled(AbsListItem.ID_END));
    }

    public void updateItemEndEnabled() {
        adapter.updateItem(new ListItemEndEnabled(AbsListItem.ID_END));
    }

    public void updateItemEndLoading() {
        adapter.updateItem(new ListItemEndLoading(AbsListItem.ID_END));
    }

    public void updateTitle(final String id, final String title) {
        adapter.updateItem(new ListItemTitle(id, title));
    }
}
