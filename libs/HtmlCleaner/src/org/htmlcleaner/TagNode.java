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
 * XML node tag - basic node of the cleaned HTML tree. At the same time, it
 * represents start tag token after HTML parsing phase and before cleaning
 * phase. After cleaning process, tree structure remains containing tag nodes
 * (TagNode class), content (text nodes - ContentNode), comments (CommentNode)
 * and optionally doctype node (DoctypeToken).
 * </p>
 */
public class TagNode extends TagToken implements HtmlNode {

    /**
     * Used as base for different node checkers.
     */
    public interface ITagNodeCondition {
        boolean satisfy(TagNode tagNode);
    }

    /**
     * All nodes.
     */
    public class TagAllCondition implements ITagNodeCondition {
        public boolean satisfy(final TagNode tagNode) {
            return true;
        }
    }

    /**
     * Checks if node contains specified attribute.
     */
    public class TagNodeAttExistsCondition implements ITagNodeCondition {
        final private String attName;

        public TagNodeAttExistsCondition(final String attName) {
            this.attName = attName.toLowerCase();
        }

        public boolean satisfy(final TagNode tagNode) {
            return tagNode == null ? false : tagNode.attributes.containsKey(attName);
        }
    }

    /**
     * Checks if node has specified attribute with specified value.
     */
    public class TagNodeAttValueCondition implements ITagNodeCondition {
        final private String attName;
        final private String attValue;
        final private boolean isCaseSensitive;

        public TagNodeAttValueCondition(final String attName, final String attValue, final boolean isCaseSensitive) {
            this.attName = attName;
            this.attValue = attValue;
            this.isCaseSensitive = isCaseSensitive;
        }

        public boolean satisfy(final TagNode tagNode) {
            if (tagNode == null || attName == null || attValue == null) {
                return false;
            } else {
                return isCaseSensitive ? attValue.equals(tagNode.getAttributeByName(attName)) : attValue
                        .equalsIgnoreCase(tagNode.getAttributeByName(attName));
            }
        }
    }

    /**
     * Checks if node has specified name.
     */
    public class TagNodeNameCondition implements ITagNodeCondition {
        final private String name;

        public TagNodeNameCondition(final String name) {
            this.name = name;
        }

        public boolean satisfy(final TagNode tagNode) {
            return tagNode == null ? false : tagNode.name.equalsIgnoreCase(this.name);
        }
    }

    private TagNode parent = null;
    private Map<String, String> attributes = new LinkedHashMap<String, String>();
    private List<Object> children = new ArrayList<Object>();
    private DoctypeToken docType = null;
    private Map<String, String> nsDeclarations = null;
    private List<BaseToken> itemsToMove = null;

    private transient boolean isFormed = false;

    public TagNode(final String name) {
        super(name == null ? null : name.toLowerCase());
    }

    /**
     * @deprecated Use setAttribute instead Adds specified attribute to this tag
     *             or overrides existing one.
     * @param attName
     * @param attValue
     */
    @Deprecated
    public void addAttribute(final String attName, final String attValue) {
        setAttribute(attName, attValue);
    }

    public void addChild(final Object child) {
        if (child == null) {
            return;
        }
        if (child instanceof List<?>) {
            addChildren((List<?>) child);
        } else {
            children.add(child);
            if (child instanceof TagNode) {
                final TagNode childTagNode = (TagNode) child;
                childTagNode.parent = this;
            }
        }
    }

    /**
     * Add all elements from specified list to this node.
     * 
     * @param newChildren
     */
    public void addChildren(final List<?> newChildren) {
        if (newChildren != null) {
            final Iterator<?> it = newChildren.iterator();
            while (it.hasNext()) {
                final Object child = it.next();
                addChild(child);
            }
        }
    }

    public void addItemForMoving(final BaseToken item) {
        if (itemsToMove == null) {
            itemsToMove = new ArrayList<BaseToken>();
        }

        itemsToMove.add(item);
    }

    /**
     * Adds namespace declaration to the node
     * 
     * @param nsPrefix
     *            Namespace prefix
     * @param nsURI
     *            Namespace URI
     */
    public void addNamespaceDeclaration(final String nsPrefix, final String nsURI) {
        if (nsDeclarations == null) {
            nsDeclarations = new TreeMap<String, String>();
        }
        nsDeclarations.put(nsPrefix, nsURI);
    }

    /**
     * Collect all prefixes in namespace declarations up the path to the
     * document root from the specified node
     * 
     * @param prefixes
     *            Set of prefixes to be collected
     */
    public void collectNamespacePrefixesOnPath(final Set<String> prefixes) {
        final Map<String, String> nsDeclarations = getNamespaceDeclarations();
        if (nsDeclarations != null) {
            for (String prefix : nsDeclarations.keySet()) {
                prefixes.add(prefix);
            }
        }
        if (parent != null) {
            parent.collectNamespacePrefixesOnPath(prefixes);
        }
    }

    /**
     * Evaluates XPath expression on give node. <br>
     * <em>
     *  This is not fully supported XPath parser and evaluator.
     *  Examples below show supported elements:
     * </em> <code>
     * <ul>
     *      <li>//div//a</li>
     *      <li>//div//a[@id][@class]</li>
     *      <li>/body/*[1]/@type</li>
     *      <li>//div[3]//a[@id][@href='r/n4']</li>
     *      <li>//div[last() >= 4]//./div[position() = last()])[position() > 22]//li[2]//a</li>
     *      <li>//div[2]/@*[2]</li>
     *      <li>data(//div//a[@id][@class])</li>
     *      <li>//p/last()</li>
     *      <li>//body//div[3][@class]//span[12.2<position()]/@id</li>
     *      <li>data(//a['v' < @id])</li>
     * </ul>
     * </code>
     * 
     * @param xPathExpression
     * @return
     * @throws XPatherException
     */
    public Object[] evaluateXPath(final String xPathExpression) throws XPatherException {
        return new XPather(xPathExpression).evaluateAgainstNode(this);
    }

    /**
     * Finds first element in the tree that satisfy specified condition.
     * 
     * @param condition
     * @param isRecursive
     * @return First TagNode found, or null if no such elements.
     */
    private TagNode findElement(final ITagNodeCondition condition, final boolean isRecursive) {
        if (condition == null) {
            return null;
        }

        for (int i = 0; i < children.size(); i++) {
            final Object item = children.get(i);
            if (item instanceof TagNode) {
                final TagNode currNode = (TagNode) item;
                if (condition.satisfy(currNode)) {
                    return currNode;
                } else if (isRecursive) {
                    final TagNode inner = currNode.findElement(condition, isRecursive);
                    if (inner != null) {
                        return inner;
                    }
                }
            }
        }

        return null;
    }

    public TagNode findElementByAttValue(final String attName, final String attValue, final boolean isRecursive,
            final boolean isCaseSensitive) {
        return findElement(new TagNodeAttValueCondition(attName, attValue, isCaseSensitive), isRecursive);
    }

    public TagNode findElementByName(final String findName, final boolean isRecursive) {
        return findElement(new TagNodeNameCondition(findName), isRecursive);
    }

    public TagNode findElementHavingAttribute(final String attName, final boolean isRecursive) {
        return findElement(new TagNodeAttExistsCondition(attName), isRecursive);
    }

    public TagNode[] getAllElements(final boolean isRecursive) {
        return getElements(new TagAllCondition(), isRecursive);
    }

    public List getAllElementsList(final boolean isRecursive) {
        return getElementList(new TagAllCondition(), isRecursive);
    }

    /**
     * @param attName
     * @return Value of the specified attribute, or null if it this tag doesn't
     *         contain it.
     */
    public String getAttributeByName(final String attName) {
        return attName != null ? attributes.get(attName.toLowerCase()) : null;
    }

    /**
     * @return Map instance containing all attribute name/value pairs.
     */
    public Map<String, String> getAttributes() {
        return attributes;
    }

    /**
     * @param child
     *            Child to find index of
     * @return Index of the specified child node inside this node's children, -1
     *         if node is not the child
     */
    public int getChildIndex(final HtmlNode child) {
        int index = 0;
        for (final Object curr : children) {
            if (curr == child) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * @return List of children objects. During the cleanup process there could
     *         be different kind of childern inside, however after clean there
     *         should be only TagNode instances.
     */
    public List<Object> getChildren() {
        return children;
    }

    public List<TagNode> getChildTagList() {
        final List<TagNode> childTagList = new ArrayList<TagNode>();
        for (int i = 0; i < children.size(); i++) {
            final Object item = children.get(i);
            if (item instanceof TagNode) {
                childTagList.add((TagNode) item);
            }
        }

        return childTagList;
    }

    /**
     * @return An array of child TagNode instances.
     */
    public TagNode[] getChildTags() {
        final List<TagNode> childTagList = getChildTagList();
        final TagNode childrenArray[] = new TagNode[childTagList.size()];
        for (int i = 0; i < childTagList.size(); i++) {
            childrenArray[i] = childTagList.get(i);
        }

        return childrenArray;
    }

    public DoctypeToken getDocType() {
        return docType;
    }

    /**
     * Get all elements in the tree that satisfy specified condition.
     * 
     * @param condition
     * @param isRecursive
     * @return List of TagNode instances with specified name.
     */
    private List getElementList(final ITagNodeCondition condition, final boolean isRecursive) {
        final List result = new LinkedList();
        if (condition == null) {
            return result;
        }

        for (int i = 0; i < children.size(); i++) {
            final Object item = children.get(i);
            if (item instanceof TagNode) {
                final TagNode currNode = (TagNode) item;
                if (condition.satisfy(currNode)) {
                    result.add(currNode);
                }
                if (isRecursive) {
                    final List innerList = currNode.getElementList(condition, isRecursive);
                    if (innerList != null && !innerList.isEmpty()) {
                        result.addAll(innerList);
                    }
                }
            }
        }

        return result;
    }

    public List getElementListByAttValue(final String attName, final String attValue, final boolean isRecursive,
            final boolean isCaseSensitive) {
        return getElementList(new TagNodeAttValueCondition(attName, attValue, isCaseSensitive), isRecursive);
    }

    public List getElementListByName(final String findName, final boolean isRecursive) {
        return getElementList(new TagNodeNameCondition(findName), isRecursive);
    }

    public List getElementListHavingAttribute(final String attName, final boolean isRecursive) {
        return getElementList(new TagNodeAttExistsCondition(attName), isRecursive);
    }

    /**
     * @param condition
     * @param isRecursive
     * @return The array of all subelemets that satisfy specified condition.
     */
    private TagNode[] getElements(final ITagNodeCondition condition, final boolean isRecursive) {
        final List list = getElementList(condition, isRecursive);
        final TagNode array[] = new TagNode[list == null ? 0 : list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = (TagNode) list.get(i);
        }

        return array;
    }

    public TagNode[] getElementsByAttValue(final String attName, final String attValue, final boolean isRecursive,
            final boolean isCaseSensitive) {
        return getElements(new TagNodeAttValueCondition(attName, attValue, isCaseSensitive), isRecursive);
    }

    public TagNode[] getElementsByName(final String findName, final boolean isRecursive) {
        return getElements(new TagNodeNameCondition(findName), isRecursive);
    }

    public TagNode[] getElementsHavingAttribute(final String attName, final boolean isRecursive) {
        return getElements(new TagNodeAttExistsCondition(attName), isRecursive);
    }

    public List<BaseToken> getItemsToMove() {
        return itemsToMove;
    }

    /**
     * @return Map of namespace declarations for this node
     */
    public Map<String, String> getNamespaceDeclarations() {
        return nsDeclarations;
    }

    public String getNamespaceURIOnPath(final String nsPrefix) {
        if (nsDeclarations != null) {
            for (final Map.Entry<String, String> nsEntry : nsDeclarations.entrySet()) {
                final String currName = nsEntry.getKey();
                if (currName.equals(nsPrefix) || ("".equals(currName) && nsPrefix == null)) {
                    return nsEntry.getValue();
                }
            }
        }
        if (parent != null) {
            return parent.getNamespaceURIOnPath(nsPrefix);
        }

        return null;
    }

    /**
     * @return Parent of this node, or null if this is the root node.
     */
    public TagNode getParent() {
        return parent;
    }

    /**
     * @return Text content of this node and it's subelements.
     */
    public StringBuffer getText() {
        final StringBuffer text = new StringBuffer();
        for (int i = 0; i < children.size(); i++) {
            final Object item = children.get(i);
            if (item instanceof ContentNode) {
                text.append(item.toString());
            } else if (item instanceof TagNode) {
                final StringBuffer subtext = ((TagNode) item).getText();
                text.append(subtext);
            }
        }

        return text;
    }

    /**
     * Checks existance of specified attribute.
     * 
     * @param attName
     */
    public boolean hasAttribute(final String attName) {
        return attName != null ? attributes.containsKey(attName.toLowerCase()) : false;
    }

    /**
     * @return Whether this node has child elements or not.
     */
    public boolean hasChildren() {
        return !children.isEmpty();
    }

    /**
     * Inserts specified node at specified position in array of children
     * 
     * @param index
     * @param childToAdd
     */
    public void insertChild(final int index, final HtmlNode childToAdd) {
        children.add(index, childToAdd);
    }

    /**
     * Inserts specified node in the list of children after specified child
     * 
     * @param node
     *            Child after which to insert new node
     * @param nodeToInsert
     *            Node to be inserted at specified position
     */
    public void insertChildAfter(final HtmlNode node, final HtmlNode nodeToInsert) {
        final int index = getChildIndex(node);
        if (index >= 0) {
            insertChild(index + 1, nodeToInsert);
        }
    }

    /**
     * Inserts specified node in the list of children before specified child
     * 
     * @param node
     *            Child before which to insert new node
     * @param nodeToInsert
     *            Node to be inserted at specified position
     */
    public void insertChildBefore(final HtmlNode node, final HtmlNode nodeToInsert) {
        final int index = getChildIndex(node);
        if (index >= 0) {
            insertChild(index, nodeToInsert);
        }
    }

    public boolean isFormed() {
        return isFormed;
    }

    public TagNode makeCopy() {
        final TagNode copy = new TagNode(name);
        copy.attributes.putAll(attributes);
        return copy;
    }

    /**
     * Removes all children (subelements and text content).
     */
    public void removeAllChildren() {
        this.children.clear();
    }

    /**
     * Removes specified attribute from this tag.
     * 
     * @param attName
     */
    public void removeAttribute(final String attName) {
        if (attName != null && !"".equals(attName.trim())) {
            attributes.remove(attName.toLowerCase());
        }
    }

    /**
     * Remove specified child element from this node.
     * 
     * @param child
     * @return True if child object existed in the children list.
     */
    public boolean removeChild(final Object child) {
        return this.children.remove(child);
    }

    /**
     * Remove this node from the tree.
     * 
     * @return True if element is removed (if it is not root node).
     */
    public boolean removeFromTree() {
        if (parent != null) {
            final boolean existed = parent.removeChild(this);
            parent = null;
            return existed;
        }
        return false;
    }

    /**
     * Replaces specified child node with specified replacement node.
     * 
     * @param childToReplace
     *            Child node to be replaced
     * @param replacement
     *            Replacement node
     */
    public void replaceChild(final HtmlNode childToReplace, final HtmlNode replacement) {
        if (replacement == null) {
            return;
        }
        final ListIterator it = children.listIterator();
        while (it.hasNext()) {
            final Object curr = it.next();
            if (curr == childToReplace) {
                it.set(replacement);
                break;
            }
        }
    }

    public void serialize(final Serializer serializer, final Writer writer) throws IOException {
        serializer.serialize(this, writer);
    }

    /**
     * Adding new attribute ir overriding existing one.
     * 
     * @param attName
     * @param attValue
     */
    public void setAttribute(String attName, final String attValue) {
        if (attName != null && !"".equals(attName.trim())) {
            attName = attName.toLowerCase();
            if ("xmlns".equals(attName)) {
                addNamespaceDeclaration("", attValue);
            } else if (attName.startsWith("xmlns:")) {
                addNamespaceDeclaration(attName.substring(6), attValue);
            } else {
                attributes.put(attName, attValue == null ? "" : attValue);
            }
        }
    }

    public void setChildren(final List<Object> children) {
        this.children = children;
    }

    public void setDocType(final DoctypeToken docType) {
        this.docType = docType;
    }

    public void setFormed() {
        setFormed(true);
    }

    public void setFormed(final boolean isFormed) {
        this.isFormed = isFormed;
    }

    public void setItemsToMove(final List<BaseToken> itemsToMove) {
        this.itemsToMove = itemsToMove;
    }

    /**
     * Changes name of the tag
     * 
     * @param name
     * @return True if new name is valid, false otherwise
     */
    public boolean setName(final String name) {
        if (Utils.isValidXmlIdentifier(name)) {
            this.name = name;
            return true;
        }

        return false;
    }

    public void transformAttributes(final TagTransformation tagTrans) {
        final boolean isPreserveSourceAtts = tagTrans.isPreserveSourceAttributes();
        final boolean hasAttTransforms = tagTrans.hasAttributeTransformations();
        if (hasAttTransforms || !isPreserveSourceAtts) {
            final Map<String, String> newAttributes = isPreserveSourceAtts ? new LinkedHashMap<String, String>(
                    attributes) : new LinkedHashMap<String, String>();
            if (hasAttTransforms) {
                final Map map = tagTrans.getAttributeTransformations();
                final Iterator iterator = map.entrySet().iterator();
                while (iterator.hasNext()) {
                    final Map.Entry entry = (Map.Entry) iterator.next();
                    final String attName = (String) entry.getKey();
                    final String template = (String) entry.getValue();
                    if (template == null) {
                        newAttributes.remove(attName);
                    } else {
                        final String attValue = Utils.evaluateTemplate(template, attributes);
                        newAttributes.put(attName, attValue);
                    }
                }
            }
            this.attributes = newAttributes;
        }
    }

    /**
     * Traverses the tree and performs visitor's action on each node. It stops
     * when it finishes all the tree or when visitor returns false.
     * 
     * @param visitor
     *            TagNodeVisitor implementation
     */
    public void traverse(final TagNodeVisitor visitor) {
        traverseInternally(visitor);
    }

    private boolean traverseInternally(final TagNodeVisitor visitor) {
        if (visitor != null) {
            final boolean hasParent = parent != null;
            boolean toContinue = visitor.visit(parent, this);

            if (!toContinue) {
                return false; // if visitor stops traversal
            } else if (hasParent && parent == null) {
                return true;
                // if this node is pruned from the tree during the visit, then
                // don't go deeper
            }
            for (final Object child : children.toArray()) {
                // make an array to avoid ConcurrentModificationException when
                // some node is cut
                if (child instanceof TagNode) {
                    toContinue = ((TagNode) child).traverseInternally(visitor);
                } else if (child instanceof ContentNode) {
                    toContinue = visitor.visit(this, (ContentNode) child);
                } else if (child instanceof CommentNode) {
                    toContinue = visitor.visit(this, (CommentNode) child);
                }
                if (!toContinue) {
                    return false;
                }
            }
        }
        return true;
    }

}
