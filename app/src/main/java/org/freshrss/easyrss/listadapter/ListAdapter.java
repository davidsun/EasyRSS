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

package org.freshrss.easyrss.listadapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnTouchListener;
import android.widget.BaseAdapter;

public class ListAdapter extends BaseAdapter {
    final private OnTouchListener onTouchListener;
    final private LayoutInflater inflater;
    final private List<AbsListItem> items;
    final private Map<String, Integer> mItems;
    private OnItemTouchListener listener;
    private int fontSize;

    public ListAdapter(final Context context, final int fontSize) {
        super();

        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.items = new ArrayList<AbsListItem>();
        this.mItems = new HashMap<String, Integer>();
        this.fontSize = fontSize;
        this.onTouchListener = new OnTouchListener() {
            @Override
            public boolean onTouch(final View view, final MotionEvent event) {
                switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.setPressed(true);
                    break;
                case MotionEvent.ACTION_UP:
                    view.setPressed(false);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    view.setPressed(false);
                    break;
                default:
                }
                if (listener != null) {
                    listener.onItemTouched(ListAdapter.this, getItem((Integer) view.getTag()), event);
                }
                return true;
            }
        };
    }

    public void clear() {
        items.clear();
        mItems.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    public int getFontSize() {
        return fontSize;
    }

    @Override
    public AbsListItem getItem(final int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(final int position) {
        // TODO Auto-generated method stub
        return 0;
    }

    public Integer getItemLocationById(final String id) {
        return mItems.get(id);
    }

    public OnItemTouchListener getListener() {
        return listener;
    }

    @Override
    public View getView(final int position, View view, final ViewGroup parent) {
        final AbsListItem item = items.get(position);
        view = item.inflate(view, inflater, fontSize);
        view.setTag(position);
        view.setOnTouchListener(onTouchListener);
        return view;
    }

    public boolean hasItem(final String id) {
        return mItems.containsKey(id);
    }

    public void removeItem(final int position) {
        final AbsListItem ali = items.get(position);
        items.remove(ali);
        mItems.remove(ali.getId());
        for (int i = position; i < items.size(); i++) {
            mItems.put(items.get(i).getId(), i);
        }
    }

    public void setFontSize(final int fontSize) {
        this.fontSize = fontSize;
    }

    public void setListener(final OnItemTouchListener listener) {
        this.listener = listener;
    }

    /*
     * Return: whether item exists.
     */
    public boolean updateItem(final AbsListItem item) {
        return updateItem(item, items.size());
    }

    /*
     * Return: whether item exists.
     */
    public boolean updateItem(final AbsListItem item, final int suggestedLoc) {
        final Integer loc = mItems.get(item.getId());
        if (loc == null) {
            items.add(suggestedLoc, item);
            for (int i = suggestedLoc; i < items.size(); i++) {
                mItems.put(items.get(i).getId(), i);
            }
        } else {
            items.set(loc, item);
        }
        notifyDataSetChanged();
        return (loc != null);
    }
}
