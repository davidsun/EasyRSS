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

import org.freshrss.easyrss.R;

import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class ListItemItem extends AbsListItem {
    private String title;
    private String subscriptionTitle;
    private boolean isRead;
    private boolean isStarred;
    private long timestamp;

    public ListItemItem(final String id, final String title, final String subscriptionTitle, final boolean isRead,
            final boolean isStarred, final long timestamp) {
        super(id);

        this.title = title;
        this.subscriptionTitle = subscriptionTitle;
        this.isRead = isRead;
        this.isStarred = isStarred;
        this.timestamp = timestamp;
    }

    public String getSubscriptionTitle() {
        return subscriptionTitle;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public View inflate(View view, final LayoutInflater inflater, final int fontSize) {
        if (view == null || view.getId() != R.id.ListItemItem) {
            view = inflater.inflate(R.layout.list_item_item, null);
        }
        final ImageView imgState = (ImageView) view.findViewById(R.id.ItemState);
        final TextView txtTitle = (TextView) view.findViewById(R.id.Title);
        final TextView txtSubTitle = (TextView) view.findViewById(R.id.SubscriptionTitle);
        txtTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize);
        txtTitle.setText(title);
        txtSubTitle.setTextSize(TypedValue.COMPLEX_UNIT_DIP, fontSize * 4 / 5);
        txtSubTitle.setText(subscriptionTitle);
        if (isRead) {
            imgState.setImageResource(R.drawable.read_sign);
            txtTitle.setTypeface(null, Typeface.NORMAL);
        } else {
            imgState.setImageResource(R.drawable.unread_sign);
            txtTitle.setTypeface(null, Typeface.BOLD);
        }
        return view;
    }

    public boolean isRead() {
        return isRead;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public void setRead(final boolean isRead) {
        this.isRead = isRead;
    }

    public void setStarred(final boolean isStarred) {
        this.isStarred = isStarred;
    }

    public void setSubscriptionTitle(final String subscriptionTitle) {
        this.subscriptionTitle = subscriptionTitle;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTitle(final String title) {
        this.title = title;
    }
}
