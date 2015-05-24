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

import org.freshrss.easyrss.data.Tag;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;

public class TagJSONParser {
    final private InputStream input;
    private OnTagRetrievedListener listener;

    public TagJSONParser(final InputStream input) {
        this.input = input;
    }

    public InputStream getInput() {
        return input;
    }

    public void setListener(final OnTagRetrievedListener listener) {
        this.listener = listener;
    }

    public void parse() throws JsonParseException, IOException, IllegalStateException {
        final JsonFactory factory = new JsonFactory();
        final JsonParser parser = factory.createParser(input);
        Tag tag = new Tag();
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
                        tag.setUid(parser.getText());
                    } else if ("sortid".equals(name)) {
                        tag.setSortId(parser.getText());
                    }
                }
            case FIELD_NAME:
                if (level == 1 && "tags".equals(name)) {
                    found = true;
                }
            default:
            }
            if (level == 2) {
                if (tag.getUid() != null && listener != null) {
                    listener.onTagRetrieved(tag);
                }
                tag = new Tag();
            }
        }
        parser.close();
        if (!found) {
            throw new IllegalStateException("Invalid JSON input");
        }
    }

    public void parse(final OnTagRetrievedListener listener) throws JsonParseException, IllegalStateException,
            IOException {
        setListener(listener);
        parse();
    }

    public OnTagRetrievedListener getListener() {
        return listener;
    }
}
