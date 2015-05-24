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

import org.freshrss.easyrss.data.DataUtils;
import org.freshrss.easyrss.data.Item;

import android.text.Html;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

public class ItemJSONParser {
    final private JsonParser parser;
    private OnItemRetrievedListener listener;

    public ItemJSONParser(final byte[] input) throws JsonParseException, IOException {
        final JsonFactory factory = new JsonFactory();
        this.parser = factory.createParser(input);
    }

    public ItemJSONParser(final InputStream input) throws JsonParseException, IOException {
        final JsonFactory factory = new JsonFactory();
        this.parser = factory.createParser(input);
    }

    public OnItemRetrievedListener getListener() {
        return listener;
    }

    public void parse() throws JsonParseException, IOException {
        Item item = new Item();
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
                if (level == 1 && "continuation".equals(name)) {
                    if (listener != null) {
                        listener.onListContinuationRetrieved(parser.getText());
                    }
                } else if (level == 3) {
                    if ("id".equals(name)) {
                        final String text = parser.getText();
                        item.setUid(text.substring(text.lastIndexOf('/') + 1));
                    } else if ("title".equals(name)) {
                        item.setTitle(Html.fromHtml(parser.getText()).toString());
                    } else if ("timestampUsec".equals(name)) {
                        item.setTimestamp(Long.valueOf(parser.getText()));
                    } else if ("author".equals(name)) {
                        item.setAuthor(Html.fromHtml(parser.getText()).toString());
                    }
                } else if (level == 4) {
                    if ("content".equals(name)) {
                        item.setContent(parser.getText());
                    } else if ("streamId".equals(name)) {
                        item.setSourceUri(parser.getText());
                    } else if ("title".equals(name)) {
                        item.setSourceTitle(Html.fromHtml(parser.getText()).toString());
                    }
                } else if (level == 5 && "href".equals(name)) {
                    item.setHref(parser.getText());
                }
                break;
            case FIELD_NAME:
                if (level == 1 && "items".equals(name)) {
                    found = true;
                } else if (level == 3 && "categories".equals(name)) {
                    parser.nextToken();
                    if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
                        while (parser.nextToken() != JsonToken.END_ARRAY) {
                            if (parser.getCurrentToken() == JsonToken.VALUE_STRING) {
                                final String category = parser.getText();
                                if (DataUtils.isReadUid(category)) {
                                    item.getState().setRead(true);
                                } else if (DataUtils.isStarredUid(category)) {
                                    item.getState().setStarred(true);
                                } else if (DataUtils.isTagUid(category)) {
                                    item.addTag(category);
                                }
                            }
                        }
                    }
                } else if (level == 3 && "enclosure".equals(name)) {
                    parser.nextToken();
                    if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
                        while (parser.nextToken() != JsonToken.END_ARRAY) {
                        }
                    }
                }
            default:
                break;
            }
            if (level == 2) {
                if (item.getUid() != null && listener != null) {
                    listener.onItemRetrieved(item);
                }
                item = new Item();
            }
        }
        parser.close();
        if (!found) {
            throw new IllegalStateException("Invalid JSON input");
        }
    }

    public void parse(final OnItemRetrievedListener listener) throws JsonParseException, IOException {
        setListener(listener);
        parse();
    }

    public void setListener(final OnItemRetrievedListener listener) {
        this.listener = listener;
    }
}
