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
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Main HtmlCleaner class.
 * 
 * <p>
 * It represents public interface to the user. It's task is to call tokenizer
 * with specified source HTML, traverse list of produced token list and create
 * internal object model. It also offers a set of methods to write resulting XML
 * to string, file or any output stream.
 * </p>
 * <p>
 * Typical usage is the following:
 * </p>
 * 
 * <xmp> // create an instance of HtmlCleaner HtmlCleaner cleaner = new
 * HtmlCleaner();
 * 
 * // take default cleaner properties CleanerProperties props =
 * cleaner.getProperties();
 * 
 * // customize cleaner's behaviour with property setters props.setXXX(...);
 * 
 * // Clean HTML taken from simple string, file, URL, input stream, // input
 * source or reader. Result is root node of created // tree-like structure.
 * Single cleaner instance may be safely used // multiple times. TagNode node =
 * cleaner.clean(...);
 * 
 * // optionally find parts of the DOM or modify some nodes TagNode[] myNodes =
 * node.getElementsByXXX(...); // and/or Object[] myNodes =
 * node.evaluateXPath(xPathExpression); // and/or aNode.removeFromTree(); //
 * and/or aNode.addAttribute(attName, attValue); // and/or
 * aNode.removeAttribute(attName, attValue); // and/or
 * cleaner.setInnerHtml(aNode, htmlContent); // and/or do some other tree
 * manipulation/traversal
 * 
 * // serialize a node to a file, output stream, DOM, JDom... new
 * XXXSerializer(props).writeXmlXXX(aNode, ...); myJDom = new
 * JDomSerializer(props, true).createJDom(aNode); myDom = new
 * DomSerializer(props, true).createDOM(aNode); </xmp>
 */
public class HtmlCleaner {

    private class CleanTimeValues {
        private OpenTags openTags;
        private boolean headOpened = false;
        private boolean bodyOpened = false;
        private Set headTags = new LinkedHashSet();
        private Set allTags = new TreeSet();

        private TagNode htmlNode;
        private TagNode bodyNode;
        private TagNode headNode;
        private TagNode rootNode;

        private Set<String> pruneTagSet = new HashSet<String>();
        private Set<TagNode> pruneNodeSet = new HashSet<TagNode>();
    }

    /**
     * Class that contains information and mathods for managing list of open,
     * but unhandled tags.
     */
    private class OpenTags {
        private List<TagPos> list = new ArrayList<TagPos>();
        private TagPos last = null;
        private Set<String> set = new HashSet<String>();

        private void addTag(String tagName, int position) {
            last = new TagPos(position, tagName);
            list.add(last);
            set.add(tagName);
        }

        private TagPos findFirstTagPos() {
            return list.isEmpty() ? null : list.get(0);
        }

        private TagPos findTag(String tagName) {
            if (tagName != null) {
                final ListIterator<TagPos> it = list.listIterator(list.size());
                String fatalTag = null;
                final TagInfo fatalInfo = tagInfoProvider.getTagInfo(tagName);
                if (fatalInfo != null) {
                    fatalTag = fatalInfo.getFatalTag();
                }

                while (it.hasPrevious()) {
                    final TagPos currTagPos = it.previous();
                    if (tagName.equals(currTagPos.name)) {
                        return currTagPos;
                    } else if (fatalTag != null && fatalTag.equals(currTagPos.name)) {
                        // do not search past a fatal tag for this tag
                        return null;
                    }
                }
            }

            return null;
        }

        private TagPos findTagToPlaceRubbish() {
            TagPos result = null, prev = null;

            if (!isEmpty()) {
                final ListIterator<TagPos> it = list.listIterator(list.size());
                while (it.hasPrevious()) {
                    result = it.previous();
                    if (result.info == null || result.info.allowsAnything()) {
                        if (prev != null) {
                            return prev;
                        }
                    }
                    prev = result;
                }
            }

            return result;
        }

        private TagPos getLastTagPos() {
            return last;
        }

        private boolean isEmpty() {
            return list.isEmpty();
        }

        private void removeTag(final String tagName) {
            final ListIterator<TagPos> it = list.listIterator(list.size());
            while (it.hasPrevious()) {
                final TagPos currTagPos = it.previous();
                if (tagName.equals(currTagPos.name)) {
                    it.remove();
                    break;
                }
            }

            last = list.isEmpty() ? null : list.get(list.size() - 1);
        }

        /**
         * Checks if any of tags specified in the set are already open.
         * 
         * @param tags
         */
        private boolean someAlreadyOpen(final Set tags) {
            final Iterator<TagPos> it = list.iterator();
            while (it.hasNext()) {
                final TagPos curr = it.next();
                if (tags.contains(curr.name)) {
                    return true;
                }
            }
            return false;
        }

        private boolean tagEncountered(final String tagName) {
            return set.contains(tagName);
        }

        private boolean tagExists(final String tagName) {
            return findTag(tagName) != null;
        }
    }

    /**
     * Contains information about single open tag
     */
    private class TagPos {
        private int position;
        private String name;
        private TagInfo info;

        TagPos(final int position, final String name) {
            this.position = position;
            this.name = name;
            this.info = tagInfoProvider.getTagInfo(name);
        }
    }

    public static final String DEFAULT_CHARSET = System.getProperty("file.encoding");
    private CleanerProperties properties;
    private ITagInfoProvider tagInfoProvider;
    private CleanerTransformations transformations = null;

    /**
     * Constructor - creates cleaner instance with default tag info provider and
     * default properties.
     */
    public HtmlCleaner() {
        this(null, null);
    }

    /**
     * Constructor - creates the instance with default tag info provider and
     * specified properties
     * 
     * @param properties
     *            Properties used during parsing and serializing
     */
    public HtmlCleaner(final CleanerProperties properties) {
        this(null, properties);
    }

    /**
     * Constructor - creates the instance with specified tag info provider and
     * default properties
     * 
     * @param tagInfoProvider
     *            Provider for tag filtering and balancing
     */
    public HtmlCleaner(final ITagInfoProvider tagInfoProvider) {
        this(tagInfoProvider, null);
    }

    /**
     * Constructor - creates the instance with specified tag info provider and
     * specified properties
     * 
     * @param tagInfoProvider
     *            Provider for tag filtering and balancing
     * @param properties
     *            Properties used during parsing and serializing
     */
    public HtmlCleaner(final ITagInfoProvider tagInfoProvider, final CleanerProperties properties) {
        this.tagInfoProvider = (tagInfoProvider == null) ? DefaultTagProvider.getInstance() : tagInfoProvider;
        this.properties = properties == null ? new CleanerProperties() : properties;
        this.properties.setTagInfoProvider(this.tagInfoProvider);
    }

    /**
     * Add attributes from specified map to the specified tag. If some attribute
     * already exist it is preserved.
     * 
     * @param tag
     * @param attributes
     */
    private void addAttributesToTag(final TagNode tag, final Map attributes) {
        if (attributes != null) {
            final Map tagAttributes = tag.getAttributes();
            final Iterator it = attributes.entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry currEntry = (Map.Entry) it.next();
                final String attName = (String) currEntry.getKey();
                if (!tagAttributes.containsKey(attName)) {
                    final String attValue = (String) currEntry.getValue();
                    tag.setAttribute(attName, attValue);
                }
            }
        }
    }

    /**
     * Checks if specified tag with specified info is candidate for moving to
     * head section.
     * 
     * @param tagInfo
     * @param tagNode
     */
    private void addPossibleHeadCandidate(final TagInfo tagInfo, final TagNode tagNode,
            final CleanTimeValues cleanTimeValues) {
        if (tagInfo != null && tagNode != null) {
            if (tagInfo.isHeadTag()
                    || (tagInfo.isHeadAndBodyTag() && cleanTimeValues.headOpened && !cleanTimeValues.bodyOpened)) {
                cleanTimeValues.headTags.add(tagNode);
            }
        }
    }

    /**
     * Assigns root node to internal variable. Root node of the result depends
     * on parameter "omitHtmlEnvelope". If it is set, then first child of the
     * body will be root node, or html will be root node otherwise.
     */
    private void calculateRootNode(final CleanTimeValues cleanTimeValues) {
        cleanTimeValues.rootNode = cleanTimeValues.htmlNode;

        if (properties.isOmitHtmlEnvelope()) {
            final List bodyChildren = cleanTimeValues.bodyNode.getChildren();
            if (bodyChildren != null) {
                for (Object child : bodyChildren) {
                    // if found child that is tag itself, then return it
                    if (child instanceof TagNode) {
                        cleanTimeValues.rootNode = (TagNode) child;
                        break;
                    }
                }
            }
        }
    }

    public TagNode clean(final File file) throws IOException {
        return clean(file, DEFAULT_CHARSET);
    }

    public TagNode clean(final File file, final String charset) throws IOException {
        final FileInputStream in = new FileInputStream(file);
        final Reader reader = new InputStreamReader(in, charset);
        return clean(reader);
    }

    public TagNode clean(final InputStream in) throws IOException {
        return clean(in, DEFAULT_CHARSET);
    }

    public TagNode clean(final InputStream in, final String charset) throws IOException {
        return clean(new InputStreamReader(in, charset));
    }

    public TagNode clean(final Reader reader) throws IOException {
        return clean(reader, new CleanTimeValues());
    }

    /**
     * Basic version of the cleaning call.
     * 
     * @param reader
     * @return An instance of TagNode object which is the root of the XML tree.
     * @throws IOException
     */
    public TagNode clean(final Reader reader, final CleanTimeValues cleanTimeValues) throws IOException {
        cleanTimeValues.openTags = new OpenTags();
        cleanTimeValues.headOpened = false;
        cleanTimeValues.bodyOpened = false;
        cleanTimeValues.headTags.clear();
        cleanTimeValues.allTags.clear();
        setPruneTags(properties.getPruneTags(), cleanTimeValues);

        cleanTimeValues.htmlNode = createTagNode("html", cleanTimeValues);
        cleanTimeValues.bodyNode = createTagNode("body", cleanTimeValues);
        cleanTimeValues.headNode = createTagNode("head", cleanTimeValues);
        cleanTimeValues.rootNode = null;
        cleanTimeValues.htmlNode.addChild(cleanTimeValues.headNode);
        cleanTimeValues.htmlNode.addChild(cleanTimeValues.bodyNode);

        final HtmlTokenizer htmlTokenizer = new HtmlTokenizer(reader, properties, transformations, tagInfoProvider) {
            @Override
            public TagNode createTagNode(final String name) {
                return HtmlCleaner.this.createTagNode(name, cleanTimeValues);
            }

            @Override
            public void makeTree(final List<BaseToken> tokenList) {
                HtmlCleaner.this.makeTree(tokenList, tokenList.listIterator(tokenList.size() - 1), cleanTimeValues);
            }
        };

        htmlTokenizer.start();

        final List<BaseToken> nodeList = htmlTokenizer.getTokenList();
        closeAll(nodeList, cleanTimeValues);
        createDocumentNodes(nodeList, cleanTimeValues);

        calculateRootNode(cleanTimeValues);

        // if there are some nodes to prune from tree
        if (cleanTimeValues.pruneNodeSet != null && !cleanTimeValues.pruneNodeSet.isEmpty()) {
            final Iterator iterator = cleanTimeValues.pruneNodeSet.iterator();
            while (iterator.hasNext()) {
                final TagNode tagNode = (TagNode) iterator.next();
                final TagNode parent = tagNode.getParent();
                if (parent != null) {
                    parent.removeChild(tagNode);
                }
            }
        }

        cleanTimeValues.rootNode.setDocType(htmlTokenizer.getDocType());

        return cleanTimeValues.rootNode;
    }

    public TagNode clean(final String htmlContent) {
        try {
            return clean(new StringReader(htmlContent));
        } catch (final IOException e) {
            // should never happen because reading from StringReader
            throw new HtmlCleanerException(e);
        }
    }

    /**
     * Creates instance from the content downloaded from specified URL. HTML
     * encoding is resolved following the attempts in the sequence: 1. reading
     * Content-Type response header, 2. Analyzing META tags at the beginning of
     * the html, 3. Using platform's default charset.
     * 
     * @param url
     * @return
     * @throws IOException
     */
    public TagNode clean(final URL url) throws IOException {
        return clean(url, null);
    }

    public TagNode clean(final URL url, String charset) throws IOException {
        final URLConnection urlConnection = url.openConnection();
        if (charset == null) {
            charset = Utils.getCharsetFromContentTypeString(urlConnection.getHeaderField("Content-Type"));
        }
        if (charset == null) {
            charset = Utils.getCharsetFromContent(url);
        }
        if (charset == null) {
            charset = DEFAULT_CHARSET;
        }
        return clean(url.openStream(), charset);
    }

    /**
     * Close all unclosed tags if there are any.
     */
    private void closeAll(final List<BaseToken> nodeList, final CleanTimeValues cleanTimeValues) {
        final TagPos firstTagPos = cleanTimeValues.openTags.findFirstTagPos();
        if (firstTagPos != null) {
            closeSnippet(nodeList, firstTagPos, null, cleanTimeValues);
        }
    }

    private List closeSnippet(final List nodeList, final TagPos tagPos, final Object toNode,
            final CleanTimeValues cleanTimeValues) {
        final List closed = new ArrayList();
        final ListIterator it = nodeList.listIterator(tagPos.position);

        TagNode tagNode = null;
        Object item = it.next();
        boolean isListEnd = false;

        while ((toNode == null && !isListEnd) || (toNode != null && item != toNode)) {
            if (isStartToken(item)) {
                final TagNode startTagToken = (TagNode) item;
                closed.add(startTagToken);
                final List<BaseToken> itemsToMove = startTagToken.getItemsToMove();
                if (itemsToMove != null) {
                    final OpenTags prevOpenTags = cleanTimeValues.openTags;
                    cleanTimeValues.openTags = new OpenTags();
                    makeTree(itemsToMove, itemsToMove.listIterator(0), cleanTimeValues);
                    closeAll(itemsToMove, cleanTimeValues);
                    startTagToken.setItemsToMove(null);
                    cleanTimeValues.openTags = prevOpenTags;
                }

                final TagNode newTagNode = createTagNode(startTagToken);
                final TagInfo tag = tagInfoProvider.getTagInfo(newTagNode.getName());
                addPossibleHeadCandidate(tag, newTagNode, cleanTimeValues);
                if (tagNode != null) {
                    tagNode.addChildren(itemsToMove);
                    tagNode.addChild(newTagNode);
                    it.set(null);
                } else {
                    if (itemsToMove != null) {
                        itemsToMove.add(newTagNode);
                        it.set(itemsToMove);
                    } else {
                        it.set(newTagNode);
                    }
                }

                cleanTimeValues.openTags.removeTag(newTagNode.getName());
                tagNode = newTagNode;
            } else {
                if (tagNode != null) {
                    it.set(null);
                    if (item != null) {
                        tagNode.addChild(item);
                    }
                }
            }

            if (it.hasNext()) {
                item = it.next();
            } else {
                isListEnd = true;
            }
        }

        return closed;
    }

    private void createDocumentNodes(final List listNodes, final CleanTimeValues cleanTimeValues) {
        final Iterator it = listNodes.iterator();
        while (it.hasNext()) {
            final Object child = it.next();

            if (child == null) {
                continue;
            }

            boolean toAdd = true;

            if (child instanceof TagNode) {
                final TagNode node = (TagNode) child;
                final TagInfo tag = tagInfoProvider.getTagInfo(node.getName());
                addPossibleHeadCandidate(tag, node, cleanTimeValues);
            } else {
                if (child instanceof ContentNode) {
                    toAdd = !"".equals(child.toString());
                }
            }

            if (toAdd) {
                cleanTimeValues.bodyNode.addChild(child);
            }
        }

        // move all viable head candidates to head section of the tree
        final Iterator headIterator = cleanTimeValues.headTags.iterator();
        while (headIterator.hasNext()) {
            final TagNode headCandidateNode = (TagNode) headIterator.next();

            // check if this node is already inside a candidate for moving to
            // head
            TagNode parent = headCandidateNode.getParent();
            boolean toMove = true;
            while (parent != null) {
                if (cleanTimeValues.headTags.contains(parent)) {
                    toMove = false;
                    break;
                }
                parent = parent.getParent();
            }

            if (toMove) {
                headCandidateNode.removeFromTree();
                cleanTimeValues.headNode.addChild(headCandidateNode);
            }
        }
    }

    private TagNode createTagNode(final String name, final CleanTimeValues cleanTimeValues) {
        final TagNode node = new TagNode(name);
        if (cleanTimeValues.pruneTagSet != null && name != null
                && cleanTimeValues.pruneTagSet.contains(name.toLowerCase())) {
            cleanTimeValues.pruneNodeSet.add(node);
        }
        return node;
    }

    private TagNode createTagNode(final TagNode startTagToken) {
        startTagToken.setFormed();
        return startTagToken;
    }

    /**
     * For the specified node, returns it's content as string.
     * 
     * @param node
     */
    public String getInnerHtml(final TagNode node) {
        if (node != null) {
            try {
                final String content = new SimpleXmlSerializer(properties).getAsString(node);
                int index1 = content.indexOf("<" + node.getName());
                index1 = content.indexOf('>', index1 + 1);
                final int index2 = content.lastIndexOf('<');
                return index1 >= 0 && index1 <= index2 ? content.substring(index1 + 1, index2) : null;
            } catch (final IOException e) {
                throw new HtmlCleanerException(e);
            }
        } else {
            throw new HtmlCleanerException("Cannot return inner html of the null node!");
        }
    }

    public CleanerProperties getProperties() {
        return properties;
    }

    /**
     * @return ITagInfoProvider instance for this HtmlCleaner
     */
    public ITagInfoProvider getTagInfoProvider() {
        return tagInfoProvider;
    }

    /**
     * @return Transormations defined for this instance of cleaner
     */
    public CleanerTransformations getTransformations() {
        return transformations;
    }

    private boolean isAllowedInLastOpenTag(final BaseToken token, final CleanTimeValues cleanTimeValues) {
        final TagPos last = cleanTimeValues.openTags.getLastTagPos();
        if (last != null) {
            if (last.info != null) {
                return last.info.allowsItem(token);
            }
        }

        return true;
    }

    /**
     * Checks if open fatal tag is missing if there is a fatal tag for the
     * specified tag.
     * 
     * @param tag
     */
    private boolean isFatalTagSatisfied(final TagInfo tag, final CleanTimeValues cleanTimeValues) {
        if (tag != null) {
            final String fatalTagName = tag.getFatalTag();
            return fatalTagName == null ? true : cleanTimeValues.openTags.tagExists(fatalTagName);
        }

        return true;
    }

    private boolean isStartToken(final Object o) {
        return (o instanceof TagNode) && !((TagNode) o).isFormed();
    }

    private TagNode makeTagNodeCopy(final TagNode tagNode, final CleanTimeValues cleanTimeValues) {
        final TagNode copy = tagNode.makeCopy();
        if (cleanTimeValues.pruneTagSet != null && cleanTimeValues.pruneTagSet.contains(tagNode.getName())) {
            cleanTimeValues.pruneNodeSet.add(copy);
        }
        return copy;
    }

    public void makeTree(final List<BaseToken> nodeList, final ListIterator<BaseToken> nodeIterator,
            final CleanTimeValues cleanTimeValues) {
        // process while not reach the end of the list
        while (nodeIterator.hasNext()) {
            final BaseToken token = nodeIterator.next();

            if (token instanceof EndTagToken) {
                final EndTagToken endTagToken = (EndTagToken) token;
                final String tagName = endTagToken.getName();
                final TagInfo tag = tagInfoProvider.getTagInfo(tagName);

                if ((tag == null && properties.isOmitUnknownTags())
                        || (tag != null && tag.isDeprecated() && properties.isOmitDeprecatedTags())) {
                    nodeIterator.set(null);
                } else if (tag != null && !tag.allowsBody()) {
                    nodeIterator.set(null);
                } else {
                    final TagPos matchingPosition = cleanTimeValues.openTags.findTag(tagName);

                    if (matchingPosition != null) {
                        final List closed = closeSnippet(nodeList, matchingPosition, endTagToken, cleanTimeValues);
                        nodeIterator.set(null);
                        for (int i = closed.size() - 1; i >= 1; i--) {
                            final TagNode closedTag = (TagNode) closed.get(i);
                            if (tag != null && tag.isContinueAfter(closedTag.getName())) {
                                nodeIterator.add(makeTagNodeCopy(closedTag, cleanTimeValues));
                                nodeIterator.previous();
                            }
                        }
                    } else if (!isAllowedInLastOpenTag(token, cleanTimeValues)) {
                        saveToLastOpenTag(nodeList, token, cleanTimeValues);
                        nodeIterator.set(null);
                    }
                }
            } else if (isStartToken(token)) {
                final TagNode startTagToken = (TagNode) token;
                final String tagName = startTagToken.getName();
                final TagInfo tag = tagInfoProvider.getTagInfo(tagName);

                final TagPos lastTagPos = cleanTimeValues.openTags.isEmpty() ? null : cleanTimeValues.openTags
                        .getLastTagPos();
                final TagInfo lastTagInfo = lastTagPos == null ? null : tagInfoProvider.getTagInfo(lastTagPos.name);

                // add tag to set of all tags
                cleanTimeValues.allTags.add(tagName);

                // HTML open tag
                if ("html".equals(tagName)) {
                    addAttributesToTag(cleanTimeValues.htmlNode, startTagToken.getAttributes());
                    nodeIterator.set(null);
                    // BODY open tag
                } else if ("body".equals(tagName)) {
                    cleanTimeValues.bodyOpened = true;
                    addAttributesToTag(cleanTimeValues.bodyNode, startTagToken.getAttributes());
                    nodeIterator.set(null);
                    // HEAD open tag
                } else if ("head".equals(tagName)) {
                    cleanTimeValues.headOpened = true;
                    addAttributesToTag(cleanTimeValues.headNode, startTagToken.getAttributes());
                    nodeIterator.set(null);
                    // unknown HTML tag and unknown tags are not allowed
                } else if ((tag == null && properties.isOmitUnknownTags())
                        || (tag != null && tag.isDeprecated() && properties.isOmitDeprecatedTags())) {
                    nodeIterator.set(null);
                    // if current tag is unknown, unknown tags are allowed and
                    // last open tag doesn't allow any other tags in its body
                } else if (tag == null && lastTagInfo != null && !lastTagInfo.allowsAnything()) {
                    saveToLastOpenTag(nodeList, token, cleanTimeValues);
                    nodeIterator.set(null);
                } else if (tag != null && tag.hasPermittedTags()
                        && cleanTimeValues.openTags.someAlreadyOpen(tag.getPermittedTags())) {
                    nodeIterator.set(null);
                    // if tag that must be unique, ignore this occurence
                } else if (tag != null && tag.isUnique() && cleanTimeValues.openTags.tagEncountered(tagName)) {
                    nodeIterator.set(null);
                    // if there is no required outer tag without that this open
                    // tag is ignored
                } else if (!isFatalTagSatisfied(tag, cleanTimeValues)) {
                    nodeIterator.set(null);
                    // if there is no required parent tag - it must be added
                    // before this open tag
                } else if (mustAddRequiredParent(tag, cleanTimeValues)) {
                    final String requiredParent = tag.getRequiredParent();
                    final TagNode requiredParentStartToken = createTagNode(requiredParent, cleanTimeValues);
                    nodeIterator.previous();
                    nodeIterator.add(requiredParentStartToken);
                    nodeIterator.previous();
                    // if last open tag has lower presidence then this, it must
                    // be closed
                } else if (tag != null && lastTagPos != null && tag.isMustCloseTag(lastTagInfo)) {
                    final List closed = closeSnippet(nodeList, lastTagPos, startTagToken, cleanTimeValues);
                    final int closedCount = closed.size();

                    // it is needed to copy some tags again in front of current,
                    // if there are any
                    if (tag.hasCopyTags() && closedCount > 0) {
                        // first iterates over list from the back and collects
                        // all start tokens
                        // in sequence that must be copied
                        final ListIterator closedIt = closed.listIterator(closedCount);
                        final List toBeCopied = new ArrayList();
                        while (closedIt.hasPrevious()) {
                            final TagNode currStartToken = (TagNode) closedIt.previous();
                            if (tag.isCopy(currStartToken.getName())) {
                                toBeCopied.add(0, currStartToken);
                            } else {
                                break;
                            }
                        }

                        if (toBeCopied.size() > 0) {
                            final Iterator copyIt = toBeCopied.iterator();
                            while (copyIt.hasNext()) {
                                final TagNode currStartToken = (TagNode) copyIt.next();
                                nodeIterator.add(makeTagNodeCopy(currStartToken, cleanTimeValues));
                            }

                            // back to the previous place, before adding new
                            // start tokens
                            for (int i = 0; i < toBeCopied.size(); i++) {
                                nodeIterator.previous();
                            }
                        }
                    }

                    nodeIterator.previous();
                    // if this open tag is not allowed inside last open tag,
                    // then it must be moved to the place where it can be
                } else if (!isAllowedInLastOpenTag(token, cleanTimeValues)) {
                    saveToLastOpenTag(nodeList, token, cleanTimeValues);
                    nodeIterator.set(null);
                    // if it is known HTML tag but doesn't allow body, it is
                    // immediately closed
                } else if (tag != null && !tag.allowsBody()) {
                    final TagNode newTagNode = createTagNode(startTagToken);
                    addPossibleHeadCandidate(tag, newTagNode, cleanTimeValues);
                    nodeIterator.set(newTagNode);
                    // default case - just remember this open tag and go further
                } else {
                    cleanTimeValues.openTags.addTag(tagName, nodeIterator.previousIndex());
                }
            } else {
                if (!isAllowedInLastOpenTag(token, cleanTimeValues)) {
                    saveToLastOpenTag(nodeList, token, cleanTimeValues);
                    nodeIterator.set(null);
                }
            }
        }
    }

    /**
     * Check if specified tag requires parent tag, but that parent tag is
     * missing in the appropriate context.
     * 
     * @param tag
     */
    private boolean mustAddRequiredParent(final TagInfo tag, final CleanTimeValues cleanTimeValues) {
        if (tag != null) {
            final String requiredParent = tag.getRequiredParent();
            if (requiredParent != null) {
                final String fatalTag = tag.getFatalTag();
                int fatalTagPositon = -1;
                if (fatalTag != null) {
                    final TagPos tagPos = cleanTimeValues.openTags.findTag(fatalTag);
                    if (tagPos != null) {
                        fatalTagPositon = tagPos.position;
                    }
                }

                // iterates through the list of open tags from the end and check
                // if there is some higher
                final ListIterator<TagPos> it = cleanTimeValues.openTags.list
                        .listIterator(cleanTimeValues.openTags.list.size());
                while (it.hasPrevious()) {
                    final TagPos currTagPos = it.previous();
                    if (tag.isHigher(currTagPos.name)) {
                        return currTagPos.position <= fatalTagPositon;
                    }
                }

                return true;
            }
        }

        return false;
    }

    private void saveToLastOpenTag(final List nodeList, final BaseToken tokenToAdd,
            final CleanTimeValues cleanTimeValues) {
        final TagPos last = cleanTimeValues.openTags.getLastTagPos();
        if (last != null && last.info != null && last.info.isIgnorePermitted()) {
            return;
        }

        final TagPos rubbishPos = cleanTimeValues.openTags.findTagToPlaceRubbish();
        if (rubbishPos != null) {
            final TagNode startTagToken = (TagNode) nodeList.get(rubbishPos.position);
            startTagToken.addItemForMoving(tokenToAdd);
        }
    }

    /**
     * For the specified tag node, defines it's html content. This causes
     * cleaner to reclean given html portion and insert it inside the node
     * instead of previous content.
     * 
     * @param node
     * @param content
     */
    public void setInnerHtml(final TagNode node, final String content) {
        if (node != null) {
            final String nodeName = node.getName();
            final StringBuilder html = new StringBuilder();
            html.append("<" + nodeName + " marker=''>");
            html.append(content);
            html.append("</" + nodeName + ">");
            TagNode parent = node.getParent();
            while (parent != null) {
                final String parentName = parent.getName();
                html.insert(0, "<" + parentName + ">");
                html.append("</" + parentName + ">");
                parent = parent.getParent();
            }

            final TagNode rootNode = clean(html.toString());
            final TagNode cleanedNode = rootNode.findElementHavingAttribute("marker", true);
            if (cleanedNode != null) {
                node.setChildren(cleanedNode.getChildren());
            }
        }
    }

    private void setPruneTags(final String pruneTags, final CleanTimeValues cleanTimeValues) {
        cleanTimeValues.pruneTagSet.clear();
        cleanTimeValues.pruneNodeSet.clear();
        if (pruneTags != null) {
            final StringTokenizer tokenizer = new StringTokenizer(pruneTags, ",");
            while (tokenizer.hasMoreTokens()) {
                cleanTimeValues.pruneTagSet.add(tokenizer.nextToken().trim().toLowerCase());
            }
        }
    }

    /**
     * Sets tranformations for this cleaner instance.
     * 
     * @param transformations
     */
    public void setTransformations(final CleanerTransformations transformations) {
        this.transformations = transformations;
    }

}
