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

import org.freshrss.easyrss.data.UnreadCount;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;

public class UnreadCountJSONParser {
    final private JsonParser parser;
    private OnUnreadCountRetrievedListener listener;

    public UnreadCountJSONParser(final byte[] content) throws JsonParseException, IOException {
        final JsonFactory factory = new JsonFactory();
        this.parser = factory.createParser(content);
    }

    public void setListener(final OnUnreadCountRetrievedListener listener) {
        this.listener = listener;
    }

    private void parse() throws JsonParseException, IOException, IllegalStateException {
        UnreadCount count = new UnreadCount();
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
            case VALUE_NUMBER_INT:
                if (level == 3 && "count".equals(name)) {
                    count.setCount(parser.getIntValue());
                }
            case VALUE_STRING:
                if (level == 3 && "id".equals(name)) {
                    count.setUid(parser.getText());
                } else if (level == 3 && "newestItemTimestampUsec".equals(name)) {
                    count.setNewestItemTime(Long.valueOf(parser.getText()));
                }
            case FIELD_NAME:
                if (level == 1 && "unreadcounts".equals(name)) {
                    found = true;
                }
            default:
            }
            if (level == 2) {
                if (count.getUid() != null && listener != null) {
                    listener.onUnreadCountRetrieved(count);
                }
                count = new UnreadCount();
            }
        }
        parser.close();
        if (!found) {
            throw new IllegalStateException("Invalid JSON input");
        }
    }

    public void parse(final OnUnreadCountRetrievedListener listener) throws JsonParseException, IllegalStateException,
            IOException {
        setListener(listener);
        parse();
    }

    public OnUnreadCountRetrievedListener getListener() {
        return listener;
    }
}
