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

/*  Copyright (c) 2006-2007, Vladimir Nikic
 All rights reserved.

 Redistribution and use of this software in source and binary forms,
 with or without modification, are permitted provided that the following
 conditions are met:

 * Redistributions of source code must retain the above
 copyright notice, this list of conditions and the
 following disclaimer.

 * Redistributions in binary form must reproduce the above
 copyright notice, this list of conditions and the
 following disclaimer in the documentation and/or other
 materials provided with the distribution.

 * The name of HtmlCleaner may not be used to endorse or promote
 products derived from this software without specific prior
 written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 POSSIBILITY OF SUCH DAMAGE.

 You can contact Vladimir Nikic by sending e-mail to
 nikic_vladimir@yahoo.com. Please include the word "HtmlCleaner" in the
 subject line.
 */

package org.htmlcleaner;

import java.io.*;
import java.util.*;

/**
 * <p>
 * Pretty HTML serializer - creates resulting HTML with indenting lines.
 * </p>
 */
public class PrettyHtmlSerializer extends HtmlSerializer {
    private static final String DEFAULT_INDENTATION_STRING = "\t";

    private String indentString = DEFAULT_INDENTATION_STRING;
    final private List<String> indents = new ArrayList<String>();

    public PrettyHtmlSerializer(final CleanerProperties props) {
        this(props, DEFAULT_INDENTATION_STRING);
    }

    public PrettyHtmlSerializer(final CleanerProperties props, final String indentString) {
        super(props);
        this.indentString = indentString;
    }

    protected void serialize(final TagNode tagNode, final Writer writer) throws IOException {
        serializePrettyHtml(tagNode, writer, 0, false, true);
    }

    /**
     * @param level
     * @return Appropriate indentation for the specified depth.
     */
    private synchronized String getIndent(final int level) {
        final int size = indents.size();
        if (size <= level) {
            String prevIndent = size == 0 ? null : indents.get(size - 1);
            for (int i = size; i <= level; i++) {
                final String currIndent = prevIndent == null ? "" : prevIndent + indentString;
                indents.add(currIndent);
                prevIndent = currIndent;
            }
        }

        return indents.get(level);
    }

    private String getIndentedText(final String content, final int level) {
        final String indent = getIndent(level);
        final StringBuilder result = new StringBuilder(content.length());
        final StringTokenizer tokenizer = new StringTokenizer(content, "\n\r");

        while (tokenizer.hasMoreTokens()) {
            final String line = tokenizer.nextToken().trim();
            if (!"".equals(line)) {
                result.append(indent).append(line).append('\n');
            }
        }

        return result.toString();
    }

    private String getSingleLineOfChildren(final List children) {
        final StringBuilder result = new StringBuilder();
        final Iterator childrenIt = children.iterator();
        boolean isFirst = true;

        while (childrenIt.hasNext()) {
            final Object child = childrenIt.next();

            if (!(child instanceof ContentNode)) {
                return null;
            } else {
                String content = child.toString();

                // if first item trims it from left
                if (isFirst) {
                    content = Utils.ltrim(content);
                }

                // if last item trims it from right
                if (!childrenIt.hasNext()) {
                    content = Utils.rtrim(content);
                }

                if (content.indexOf('\n') >= 0 || content.indexOf('\r') >= 0) {
                    return null;
                }
                result.append(content);
            }

            isFirst = false;
        }

        return result.toString();
    }

    protected void serializePrettyHtml(final TagNode tagNode, final Writer writer, final int level,
            final boolean isPreserveWhitespaces, final boolean isLastNewLine) throws IOException {
        final List tagChildren = tagNode.getChildren();
        final String tagName = tagNode.getName();
        final boolean isHeadlessNode = Utils.isEmptyString(tagName);
        final String indent = isHeadlessNode ? "" : getIndent(level);

        if (!isPreserveWhitespaces) {
            if (!isLastNewLine) {
                writer.write("\n");
            }
            writer.write(indent);
        }
        serializeOpenTag(tagNode, writer, true);

        final boolean preserveWhitespaces = isPreserveWhitespaces || "pre".equalsIgnoreCase(tagName);

        boolean lastWasNewLine = false;

        if (!isMinimizedTagSyntax(tagNode)) {
            final String singleLine = getSingleLineOfChildren(tagChildren);
            final boolean dontEscape = dontEscape(tagNode);
            if (!preserveWhitespaces && singleLine != null) {
                writer.write(!dontEscape(tagNode) ? escapeText(singleLine) : singleLine);
            } else {
                final Iterator childIterator = tagChildren.iterator();
                while (childIterator.hasNext()) {
                    final Object child = childIterator.next();
                    if (child instanceof TagNode) {
                        serializePrettyHtml((TagNode) child, writer, isHeadlessNode ? level : level + 1,
                                preserveWhitespaces, lastWasNewLine);
                        lastWasNewLine = false;
                    } else if (child instanceof ContentNode) {
                        final String content = dontEscape ? child.toString() : escapeText(child.toString());
                        if (content.length() > 0) {
                            if (dontEscape || preserveWhitespaces) {
                                writer.write(content);
                            } else if (Character.isWhitespace(content.charAt(0))) {
                                if (!lastWasNewLine) {
                                    writer.write("\n");
                                    lastWasNewLine = false;
                                }
                                if (content.trim().length() > 0) {
                                    writer.write(getIndentedText(Utils.rtrim(content), isHeadlessNode ? level
                                            : level + 1));
                                } else {
                                    lastWasNewLine = true;
                                }
                            } else {
                                if (content.trim().length() > 0) {
                                    writer.write(Utils.rtrim(content));
                                }
                                if (!childIterator.hasNext()) {
                                    writer.write("\n");
                                    lastWasNewLine = true;
                                }
                            }
                        }
                    } else if (child instanceof CommentNode) {
                        if (!lastWasNewLine && !preserveWhitespaces) {
                            writer.write("\n");
                            lastWasNewLine = false;
                        }
                        final CommentNode commentNode = (CommentNode) child;
                        final String content = commentNode.getCommentedContent();
                        writer.write(dontEscape ? content
                                : getIndentedText(content, isHeadlessNode ? level : level + 1));
                    }
                }
            }

            if (singleLine == null && !preserveWhitespaces) {
                if (!lastWasNewLine) {
                    writer.write("\n");
                }
                writer.write(indent);
            }

            serializeEndTag(tagNode, writer, false);
        }
    }

}
