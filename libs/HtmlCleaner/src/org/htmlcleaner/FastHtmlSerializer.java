/*******************************************************************************
 * Copyright 2011 Zheng Sun
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package org.htmlcleaner;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class FastHtmlSerializer extends HtmlSerializer {
    public FastHtmlSerializer(final CleanerProperties props) {
        super(props);
    }

    protected void serialize(final TagNode tagNode, final Writer writer) throws IOException {
        final Stack<TagNode> tagStack = new Stack<TagNode>();
        final Stack<List<Object>> childStack = new Stack<List<Object>>();
        serializeOpenTag(tagNode, writer, false);
        if (!isMinimizedTagSyntax(tagNode)) {
            tagStack.push(tagNode);
            childStack.push(new ArrayList<Object>(tagNode.getChildren()));
            while (!tagStack.isEmpty()) {
                final TagNode tag = tagStack.peek();
                final List<Object> children = childStack.peek();
                if (children.isEmpty()) {
                    tagStack.pop();
                    childStack.pop();
                    if (!isMinimizedTagSyntax(tag)) {
                        serializeEndTag(tag, writer, false);
                    }
                } else {
                    final Object item = children.get(0);
                    children.remove(0);
                    if (item instanceof ContentNode) {
                        final String content = item.toString();
                        writer.write(dontEscape(tag) ? content : escapeText(content));
                    } else if (item instanceof TagNode) {
                        final TagNode currentTag = (TagNode) item;
                        serializeOpenTag(currentTag, writer, false);
                        tagStack.push(currentTag);
                        childStack.push(new ArrayList<Object>(currentTag.getChildren()));
                    } else if (item instanceof BaseToken) {
                        ((BaseToken) item).serialize(this, writer);
                    }
                }
            }
        }
    }
}
