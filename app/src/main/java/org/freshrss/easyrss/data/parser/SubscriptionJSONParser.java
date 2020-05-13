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

package org.freshrss.easyrss.data.parser;

import java.io.IOException;
import java.io.InputStream;

import org.freshrss.easyrss.data.Subscription;

import android.text.Html;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;

public class SubscriptionJSONParser {
    final private InputStream input;
    private OnSubscriptionRetrievedListener listener;

    public SubscriptionJSONParser(final InputStream input) {
        this.input = input;
    }

    public InputStream getInput() {
        return input;
    }

    public void setListener(final OnSubscriptionRetrievedListener listener) {
        this.listener = listener;
    }

    private void parse() throws JsonParseException, IOException, IllegalStateException {
        final JsonFactory factory = new JsonFactory();
        final JsonParser parser = factory.createParser(input);
        Subscription sub = new Subscription();
        int level = 0;
        boolean found = false;
        while (parser.nextToken() != null) {
            final String name = parser.getCurrentName();
            switch (parser.getCurrentToken()) {
            case START_OBJECT:
            case START_ARRAY:
                level++;
                break;
            case END_OBJECT:
            case END_ARRAY:
                level--;
                break;
            case VALUE_STRING:
                if (level == 3) {
                    if ("id".equals(name)) {
                        sub.setUid(parser.getText());
                    } else if ("htmlUrl".equals(name)) {
                        sub.setUrl(parser.getText());
                    } else if ("title".equals(name)) {
                        sub.setTitle(Html.fromHtml(parser.getText()).toString());
                    } else if ("sortid".equals(name)) {
                        sub.setSortId(parser.getText());
                    } else if ("firstitemmsec".equals(name)) {
                        sub.setFirstItemMsec(Long.valueOf(parser.getText()));
                    }
                } else if (level == 5 && "id".equals(name)) {
                    sub.addTag(parser.getText());
                }
                break;
            case FIELD_NAME:
                if (level == 1 && "subscriptions".equals(name)) {
                    found = true;
                }
                break;
            default:
            }
            if (level == 2) {
                if (sub.getUid() != null && listener != null) {
                    listener.onSubscriptionRetrieved(sub);
                }
                sub = new Subscription();
            }
        }
        parser.close();
        if (!found) {
            throw new IllegalStateException("Invalid JSON input");
        }
    }

    public void parse(final OnSubscriptionRetrievedListener listener) throws JsonParseException, IllegalStateException,
            IOException {
        setListener(listener);
        parse();
    }

    public OnSubscriptionRetrievedListener getListener() {
        return listener;
    }
}
