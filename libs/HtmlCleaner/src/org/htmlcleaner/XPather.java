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

import java.util.*;

/**
 * <p>
 * Utility for searching cleaned document tree with XPath expressions.
 * </p>
 * Examples of supported axes: <code>
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
 */
public class XPather {

    // array of basic tokens of which XPath expression is made
    final private String tokenArray[];

    /**
     * Constructor - creates XPather instance with specified XPath expression.
     * 
     * @param expression
     */
    public XPather(final String expression) {
        final StringTokenizer tokenizer = new StringTokenizer(expression, "/()[]\"'=<>", true);
        final int tokenCount = tokenizer.countTokens();
        tokenArray = new String[tokenCount];

        int index = 0;

        // this is not real XPath compiler, rather simple way to recognize basic
        // XPaths expressions
        // and interpret them against some TagNode instance.
        while (tokenizer.hasMoreTokens()) {
            tokenArray[index++] = tokenizer.nextToken();
        }
    }

    private Collection evaluateAgainst(final Collection object, int from, final int to, final boolean isRecursive,
            final int position, final int last, final boolean isFilterContext, final Collection filterSource)
            throws XPatherException {
        if (from >= 0 && to < tokenArray.length && from <= to) {
            if (tokenArray[from].trim().length() == 0) {
                return evaluateAgainst(object, from + 1, to, isRecursive, position, last, isFilterContext, filterSource);
            } else if (isToken("(", from)) {
                final int closingBracket = findClosingIndex(from, to);
                if (closingBracket > 0) {
                    final Collection value = evaluateAgainst(object, from + 1, closingBracket - 1, false, position,
                            last, isFilterContext, filterSource);
                    return evaluateAgainst(value, closingBracket + 1, to, false, position, last, isFilterContext,
                            filterSource);
                } else {
                    throwStandardException();
                }
            } else if (isToken("[", from)) {
                final int closingBracket = findClosingIndex(from, to);
                if (closingBracket > 0) {
                    final Collection value = filterByCondition(object, from + 1, closingBracket - 1);
                    return evaluateAgainst(value, closingBracket + 1, to, false, position, last, isFilterContext,
                            filterSource);
                } else {
                    throwStandardException();
                }
            } else if (isToken("\"", from) || isToken("'", from)) {
                // string constant
                final int closingQuote = findClosingIndex(from, to);
                if (closingQuote > from) {
                    final Collection value = singleton(flatten(from + 1, closingQuote - 1));
                    return evaluateAgainst(value, closingQuote + 1, to, false, position, last, isFilterContext,
                            filterSource);
                } else {
                    throwStandardException();
                }
            } else if ((isToken("=", from) || isToken("<", from) || isToken(">", from)) && isFilterContext) {
                // operator inside filter
                final boolean logicValue;
                if (isToken("=", from + 1) && (isToken("<", from) || isToken(">", from))) {
                    final Collection secondObject = evaluateAgainst(filterSource, from + 2, to, false, position, last,
                            isFilterContext, filterSource);
                    logicValue = evaluateLogic(object, secondObject, tokenArray[from] + tokenArray[from + 1]);
                } else {
                    final Collection secondObject = evaluateAgainst(filterSource, from + 1, to, false, position, last,
                            isFilterContext, filterSource);
                    logicValue = evaluateLogic(object, secondObject, tokenArray[from]);
                }
                return singleton(Boolean.valueOf(logicValue));
            } else if (isToken("/", from)) { // children of the node
                final boolean goRecursive = isToken("/", from + 1);
                if (goRecursive) {
                    from++;
                }
                if (from < to) {
                    int toIndex = findClosingIndex(from, to) - 1;
                    if (toIndex <= from) {
                        toIndex = to;
                    }
                    final Collection value = evaluateAgainst(object, from + 1, toIndex, goRecursive, 1, last,
                            isFilterContext, filterSource);
                    return evaluateAgainst(value, toIndex + 1, to, false, 1, last, isFilterContext, filterSource);
                } else {
                    throwStandardException();
                }
            } else if (isFunctionCall(from, to)) {
                final int closingBracketIndex = findClosingIndex(from + 1, to);
                final Collection funcValue = evaluateFunction(object, from, to, position, last, isFilterContext);
                return evaluateAgainst(funcValue, closingBracketIndex + 1, to, false, 1, last, isFilterContext,
                        filterSource);
            } else if (isValidInteger(tokenArray[from])) {
                final Collection value = singleton(Integer.valueOf(tokenArray[from]));
                return evaluateAgainst(value, from + 1, to, false, position, last, isFilterContext, filterSource);
            } else if (isValidDouble(tokenArray[from])) {
                final Collection value = singleton(new Double(tokenArray[from]));
                return evaluateAgainst(value, from + 1, to, false, position, last, isFilterContext, filterSource);
            } else {
                return getElementsByName(object, from, to, isRecursive, isFilterContext);
            }
        } else {
            return object;
        }

        throw new XPatherException();
    }

    /**
     * Main public method for this class - a way to execute XPath expression
     * against specified TagNode instance.
     * 
     * @param node
     */
    public Object[] evaluateAgainstNode(final TagNode node) throws XPatherException {
        if (node == null) {
            throw new XPatherException("Cannot evaluate XPath expression against null value!");
        }

        final Collection collectionResult = evaluateAgainst(singleton(node), 0, tokenArray.length - 1, false, 1, 0,
                false, null);
        final Object[] array = new Object[collectionResult.size()];

        final Iterator iterator = collectionResult.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            array[index++] = iterator.next();
        }

        return array;
    }

    /**
     * Evaluates specified function. Currently, following XPath functions are
     * supported: last, position, text, count, data
     * 
     * @param source
     * @param from
     * @param to
     * @param position
     * @param last
     * @return Collection as the result of evaluation.
     */
    private Collection evaluateFunction(final Collection source, final int from, final int to, final int position,
            final int last, final boolean isFilterContext) throws XPatherException {
        final String name = tokenArray[from].trim();
        final ArrayList result = new ArrayList();

        final int size = source.size();
        final Iterator iterator = source.iterator();
        int index = 0;
        while (iterator.hasNext()) {
            final Object curr = iterator.next();
            index++;
            if ("last".equals(name)) {
                result.add(Integer.valueOf(isFilterContext ? last : size));
            } else if ("position".equals(name)) {
                result.add(Integer.valueOf(isFilterContext ? position : index));
            } else if ("text".equals(name)) {
                if (curr instanceof TagNode) {
                    result.add(((TagNode) curr).getText());
                } else if (curr instanceof String) {
                    result.add(curr.toString());
                }
            } else if ("count".equals(name)) {
                final Collection argumentEvaluated = evaluateAgainst(source, from + 2, to - 1, false, position, 0,
                        isFilterContext, null);
                result.add(Integer.valueOf(argumentEvaluated.size()));
            } else if ("data".equals(name)) {
                final Collection argumentEvaluated = evaluateAgainst(source, from + 2, to - 1, false, position, 0,
                        isFilterContext, null);
                final Iterator it = argumentEvaluated.iterator();
                while (it.hasNext()) {
                    final Object elem = it.next();
                    if (elem instanceof TagNode) {
                        result.add(((TagNode) elem).getText());
                    } else if (elem instanceof String) {
                        result.add(elem.toString());
                    }
                }
            } else {
                throw new XPatherException("Unknown function " + name + "!");
            }
        }

        return result;
    }

    /**
     * Evaluates logic operation on two collections.
     * 
     * @param first
     * @param second
     * @param logicOperator
     * @return Result of logic operation
     */
    private boolean evaluateLogic(final Collection first, final Collection second, final String logicOperator) {
        if (first == null || first.isEmpty() || second == null || second.isEmpty()) {
            return false;
        }
        final Object elem1 = first.iterator().next();
        final Object elem2 = second.iterator().next();
        if (elem1 instanceof Number && elem2 instanceof Number) {
            final double d1 = ((Number) elem1).doubleValue();
            final double d2 = ((Number) elem2).doubleValue();
            if ("=".equals(logicOperator)) {
                return d1 == d2;
            } else if ("<".equals(logicOperator)) {
                return d1 < d2;
            } else if (">".equals(logicOperator)) {
                return d1 > d2;
            } else if ("<=".equals(logicOperator)) {
                return d1 <= d2;
            } else if (">=".equals(logicOperator)) {
                return d1 >= d2;
            }
        } else {
            final String s1 = toText(elem1);
            final String s2 = toText(elem2);
            final int result = s1.compareTo(s2);
            if ("=".equals(logicOperator)) {
                return result == 0;
            } else if ("<".equals(logicOperator)) {
                return result < 0;
            } else if (">".equals(logicOperator)) {
                return result > 0;
            } else if ("<=".equals(logicOperator)) {
                return result <= 0;
            } else if (">=".equals(logicOperator)) {
                return result >= 0;
            }
        }

        return false;
    }

    /**
     * Filter nodes satisfying the condition
     * 
     * @param source
     * @param from
     * @param to
     */
    private final Collection filterByCondition(final Collection source, final int from, final int to)
            throws XPatherException {
        final ArrayList result = new ArrayList();
        final Iterator iterator = source.iterator();
        int index = 0;
        final int size = source.size();
        while (iterator.hasNext()) {
            final Object curr = iterator.next();
            index++;

            final ArrayList logicValueList = new ArrayList(evaluateAgainst(singleton(curr), from, to, false, index,
                    size, true, singleton(curr)));
            if (logicValueList.size() >= 1) {
                final Object first = logicValueList.get(0);
                if (first instanceof Boolean) {
                    if (((Boolean) first).booleanValue()) {
                        result.add(curr);
                    }
                } else if (first instanceof Integer) {
                    if (((Integer) first).intValue() == index) {
                        result.add(curr);
                    }
                } else {
                    result.add(curr);
                }
            }
        }
        return result;
    }

    /**
     * @param from
     * @param to
     * @return matching closing index in the token array for the current token,
     *         or -1 if there is no closing token within expected bounds.
     */
    private int findClosingIndex(final int from, final int to) {
        if (from < to) {
            final String currToken = tokenArray[from];

            if ("\"".equals(currToken)) {
                for (int i = from + 1; i <= to; i++) {
                    if ("\"".equals(tokenArray[i])) {
                        return i;
                    }
                }
            } else if ("'".equals(currToken)) {
                for (int i = from + 1; i <= to; i++) {
                    if ("'".equals(tokenArray[i])) {
                        return i;
                    }
                }
            } else if ("(".equals(currToken) || "[".equals(currToken) || "/".equals(currToken)) {
                boolean isQuoteClosed = true;
                boolean isAposClosed = true;
                int brackets = "(".equals(currToken) ? 1 : 0;
                int angleBrackets = "[".equals(currToken) ? 1 : 0;
                int slashes = "/".equals(currToken) ? 1 : 0;
                for (int i = from + 1; i <= to; i++) {
                    if ("\"".equals(tokenArray[i])) {
                        isQuoteClosed = !isQuoteClosed;
                    } else if ("'".equals(tokenArray[i])) {
                        isAposClosed = !isAposClosed;
                    } else if ("(".equals(tokenArray[i]) && isQuoteClosed && isAposClosed) {
                        brackets++;
                    } else if (")".equals(tokenArray[i]) && isQuoteClosed && isAposClosed) {
                        brackets--;
                    } else if ("[".equals(tokenArray[i]) && isQuoteClosed && isAposClosed) {
                        angleBrackets++;
                    } else if ("]".equals(tokenArray[i]) && isQuoteClosed && isAposClosed) {
                        angleBrackets--;
                    } else if ("/".equals(tokenArray[i]) && isQuoteClosed && isAposClosed && brackets == 0
                            && angleBrackets == 0) {
                        slashes--;
                    }

                    if (isQuoteClosed && isAposClosed && brackets == 0 && angleBrackets == 0 && slashes == 0) {
                        return i;
                    }
                }
            }

        }

        return -1;
    }

    private String flatten(final int from, final int to) {
        if (from <= to) {
            final StringBuffer result = new StringBuffer();
            for (int i = from; i <= to; i++) {
                result.append(tokenArray[i]);
            }

            return result.toString();
        }

        return "";
    }

    /**
     * For the given source collection and specified name, returns collection of
     * subnodes or attribute values.
     * 
     * @param source
     * @param from
     * @param to
     * @param isRecursive
     * @return Colection of TagNode instances or collection of String instances.
     */
    private Collection getElementsByName(final Collection source, final int from, final int to,
            final boolean isRecursive, final boolean isFilterContext) throws XPatherException {
        String name = tokenArray[from].trim();

        if (isAtt(name)) {
            name = name.substring(1);
            final Collection result = new ArrayList();
            Collection nodes;
            if (isRecursive) {
                nodes = new LinkedHashSet();
                final Iterator iterator = source.iterator();
                while (iterator.hasNext()) {
                    final Object next = iterator.next();
                    if (next instanceof TagNode) {
                        final TagNode node = (TagNode) next;
                        nodes.addAll(node.getAllElementsList(true));
                    }
                }
            } else {
                nodes = source;
            }

            final Iterator iterator = nodes.iterator();
            while (iterator.hasNext()) {
                final Object next = iterator.next();
                if (next instanceof TagNode) {
                    final TagNode node = (TagNode) next;
                    if ("*".equals(name)) {
                        result.addAll(evaluateAgainst(node.getAttributes().values(), from + 1, to, false, 1, 1,
                                isFilterContext, null));
                    } else {
                        final String attValue = node.getAttributeByName(name);
                        if (attValue != null) {
                            result.addAll(evaluateAgainst(singleton(attValue), from + 1, to, false, 1, 1,
                                    isFilterContext, null));
                        }
                    }
                } else {
                    throwStandardException();
                }
            }
            return result;
        } else {
            final Collection result = new LinkedHashSet();
            final Iterator iterator = source.iterator();
            int index = 0;
            while (iterator.hasNext()) {
                final Object next = iterator.next();
                if (next instanceof TagNode) {
                    final TagNode node = (TagNode) next;
                    index++;
                    final boolean isSelf = ".".equals(name);
                    final boolean isParent = "..".equals(name);
                    final boolean isAll = "*".equals(name);

                    final Collection subnodes;
                    if (isSelf) {
                        subnodes = singleton(node);
                    } else if (isParent) {
                        final TagNode parent = node.getParent();
                        subnodes = parent != null ? singleton(parent) : new ArrayList();
                    } else {
                        subnodes = isAll ? node.getChildTagList() : node.getElementListByName(name, false);
                    }

                    final LinkedHashSet nodeSet = new LinkedHashSet(subnodes);
                    final Collection refinedSubnodes = evaluateAgainst(nodeSet, from + 1, to, false, index, nodeSet
                            .size(), isFilterContext, null);

                    if (isRecursive) {
                        final List childTags = node.getChildTagList();
                        if (isSelf || isParent || isAll) {
                            result.addAll(refinedSubnodes);
                        }
                        final Iterator childIterator = childTags.iterator();
                        while (childIterator.hasNext()) {
                            final TagNode childTag = (TagNode) childIterator.next();
                            final Collection childrenByName = getElementsByName(singleton(childTag), from, to,
                                    isRecursive, isFilterContext);
                            if (!isSelf && !isParent && !isAll && refinedSubnodes.contains(childTag)) {
                                result.add(childTag);
                            }
                            result.addAll(childrenByName);
                        }
                    } else {
                        result.addAll(refinedSubnodes);
                    }
                } else {
                    throwStandardException();
                }
            }
            return result;
        }
    }

    /**
     * Checks if token is attribute (starts with @)
     * 
     * @param token
     */
    private boolean isAtt(final String token) {
        return token != null && token.length() > 1 && token.charAt(0) == '@';
    }

    /**
     * Checks if tokens in specified range represents valid function call.
     * 
     * @param from
     * @param to
     * @return True if it is valid function call, false otherwise.
     */
    private boolean isFunctionCall(final int from, final int to) {
        if (!isIdentifier(tokenArray[from]) && !isToken("(", from + 1)) {
            return false;
        }

        return findClosingIndex(from + 1, to) > from + 1;
    }

    /**
     * Checks if given string is valid identifier.
     * 
     * @param str
     */
    private boolean isIdentifier(String str) {
        if (str == null) {
            return false;
        }

        str = str.trim();
        if (str.length() > 0) {
            if (!Character.isLetter(str.charAt(0))) {
                return false;
            }
            for (int i = 1; i < str.length(); i++) {
                final char ch = str.charAt(i);
                if (ch != '_' && ch != '-' && !Character.isLetterOrDigit(ch)) {
                    return false;
                }
            }
        }

        return false;
    }

    private boolean isToken(final String token, final int index) {
        final int len = tokenArray.length;
        return index >= 0 && index < len && tokenArray[index].trim().equals(token.trim());
    }

    private boolean isValidDouble(final String s) {
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidInteger(final String s) {
        try {
            Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Creates one-element collection for the specified object.
     * 
     * @param element
     */
    private Collection<Object> singleton(final Object element) {
        final ArrayList<Object> result = new ArrayList();
        result.add(element);
        return result;
    }

    private void throwStandardException() throws XPatherException {
        throw new XPatherException();
    }

    private String toText(final Object o) {
        if (o == null) {
            return "";
        }
        if (o instanceof TagNode) {
            return ((TagNode) o).getText().toString();
        } else {
            return o.toString();
        }
    }

}
