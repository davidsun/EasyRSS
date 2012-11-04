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
 * Main HTML tokenizer.
 * <p>
 * It's task is to parse HTML and produce list of valid tokens: open tag tokens,
 * end tag tokens, contents (text) and comments. As soon as new item is added to
 * token list, cleaner is invoked to clean current list at the end.
 * </p>
 */
abstract public class HtmlTokenizer {
    private final static int WORKING_BUFFER_SIZE = 1024;

    final private BufferedReader reader;
    private char[] working = new char[WORKING_BUFFER_SIZE];
    private transient int pos = 0;
    private transient int len = -1;
    private transient char saved[] = new char[512];
    private transient int savedLen = 0;
    private transient DoctypeToken docType = null;
    private transient TagToken currentTagToken = null;
    private transient List<BaseToken> tokenList = new ArrayList<BaseToken>();
    private boolean asExpected = true;
    private boolean isScriptContext = false;
    private CleanerProperties props;
    private boolean isOmitUnknownTags;
    final private boolean isTreatUnknownTagsAsContent;
    private boolean isOmitDeprecatedTags;
    final private boolean isTreatDeprecatedTagsAsContent;
    final private boolean isNamespacesAware;
    private boolean isOmitComments;
    final private boolean isAllowMultiWordAttributes;
    private boolean isAllowHtmlInsideAttributes;
    private CleanerTransformations transformations;
    private ITagInfoProvider tagInfoProvider;
    private StringBuilder commonStr = new StringBuilder();

    /**
     * Constructor - cretes instance of the parser with specified content.
     * 
     * @param reader
     * @param props
     * @param transformations
     * @param tagInfoProvider
     * 
     * @throws IOException
     */
    public HtmlTokenizer(final Reader reader, final CleanerProperties props,
            final CleanerTransformations transformations, final ITagInfoProvider tagInfoProvider) throws IOException {
        this.reader = new BufferedReader(reader);
        this.props = props;
        this.isOmitUnknownTags = props.isOmitUnknownTags();
        this.isTreatUnknownTagsAsContent = props.isTreatUnknownTagsAsContent();
        this.isOmitDeprecatedTags = props.isOmitDeprecatedTags();
        this.isTreatDeprecatedTagsAsContent = props.isTreatDeprecatedTagsAsContent();
        this.isNamespacesAware = props.isNamespacesAware();
        this.isOmitComments = props.isOmitComments();
        this.isAllowMultiWordAttributes = props.isAllowMultiWordAttributes();
        this.isAllowHtmlInsideAttributes = props.isAllowHtmlInsideAttributes();
        this.transformations = transformations;
        this.tagInfoProvider = tagInfoProvider;
    }

    private boolean addSavedAsContent() {
        if (savedLen > 0) {
            addToken(new ContentNode(saved, savedLen));
            savedLen = 0;
            return true;
        }

        return false;
    }

    private void addToken(final BaseToken token) {
        tokenList.add(token);
        makeTree(tokenList);
    }

    /**
     * Parses a single tag attribute - it is expected to be in one of the forms:
     * name=value name="value" name='value' name
     * 
     * @throws IOException
     */
    private String attributeValue() throws IOException {
        skipWhitespaces();

        if (isCharSimple('<') || isCharSimple('>') || startsWithSimple("/>")) {
            return "";
        }

        boolean isQuoteMode = false;
        boolean isAposMode = false;

        commonStr.delete(0, commonStr.length());
        if (isCharSimple('\'')) {
            isAposMode = true;
            saveCurrentSafe();
            go();
        } else if (isCharSimple('\"')) {
            isQuoteMode = true;
            saveCurrentSafe();
            go();
        }

        while (!isAllRead()
                && (((isAposMode && !isCharEquals('\'') || isQuoteMode && !isCharEquals('\"'))
                        && (isAllowHtmlInsideAttributes || !isCharEquals('>') && !isCharEquals('<')) && (isAllowMultiWordAttributes || !isWhitespaceSafe())) || (!isAposMode
                        && !isQuoteMode && !isWhitespaceSafe() && !isCharEquals('>') && !isCharEquals('<')))) {
            if (isValidXmlCharSafe()) {
                commonStr.append(working[pos]);
                saveCurrentSafe();
            }
            go();
        }

        if (isCharSimple('\'') && isAposMode) {
            saveCurrentSafe();
            go();
        } else if (isCharSimple('\"') && isQuoteMode) {
            saveCurrentSafe();
            go();
        }

        return commonStr.toString();
    }

    private void comment() throws IOException {
        go(4);
        while (!isAllRead() && !startsWithSimple("-->")) {
            if (isValidXmlCharSafe()) {
                saveCurrentSafe();
            }
            go();
        }

        if (startsWithSimple("-->")) {
            go(3);
        }

        if (savedLen > 0) {
            if (!isOmitComments) {
                final String hyphenRepl = props.getHyphenReplacementInComment();
                String comment = new String(saved, 0, savedLen).replaceAll("--", hyphenRepl + hyphenRepl);

                if (comment.length() > 0 && comment.charAt(0) == '-') {
                    comment = hyphenRepl + comment.substring(1);
                }
                final int len = comment.length();
                if (len > 0 && comment.charAt(len - 1) == '-') {
                    comment = comment.substring(0, len - 1) + hyphenRepl;
                }

                addToken(new CommentNode(comment));
            }
            savedLen = 0;
        }
    }

    private boolean content() throws IOException {
        while (!isAllRead()) {
            if (isValidXmlCharSafe()) {
                saveCurrentSafe();
            }
            go();

            if (isCharSimple('<')) {
                break;
            }
        }

        return addSavedAsContent();
    }

    public abstract TagNode createTagNode(String name);

    private void doctype() throws IOException {
        go(9);

        skipWhitespaces();
        final String part1 = identifier();
        skipWhitespaces();
        final String part2 = identifier();
        skipWhitespaces();
        final String part3 = attributeValue();
        skipWhitespaces();
        final String part4 = attributeValue();

        ignoreUntil('<');

        docType = new DoctypeToken(part1, part2, part3, part4);
    }

    /**
     * @return Current character to be read, but first it must be checked if it
     *         exists. This method is made for performance reasons to be used
     *         instead of isChar(...).
     */
    @SuppressWarnings("unused")
    private char getCurrentChar() {
        return working[pos];
    }

    public DoctypeToken getDocType() {
        return docType;
    }

    public List<BaseToken> getTokenList() {
        return this.tokenList;
    }

    private void go() throws IOException {
        pos++;
        readIfNeeded(0);
    }

    private void go(final int step) throws IOException {
        pos += step;
        readIfNeeded(step - 1);
    }

    /**
     * Parses an identifier from the current position.
     * 
     * @throws IOException
     */
    private String identifier() throws IOException {
        asExpected = true;

        if (!isIdentifierStartChar()) {
            asExpected = false;
            return null;
        }

        commonStr.delete(0, commonStr.length());

        while (!isAllRead() && isIdentifierChar()) {
            saveCurrentSafe();
            commonStr.append(working[pos]);
            go();
        }

        // strip invalid characters from the end
        while (commonStr.length() > 0 && Utils.isIdentifierHelperChar(commonStr.charAt(commonStr.length() - 1))) {
            commonStr.deleteCharAt(commonStr.length() - 1);
        }

        if (commonStr.length() == 0) {
            return null;
        }

        String id = commonStr.toString();

        final int columnIndex = id.indexOf(':');
        if (columnIndex >= 0) {
            final String prefix = id.substring(0, columnIndex);
            String suffix = id.substring(columnIndex + 1);
            final int nextColumnIndex = suffix.indexOf(':');
            if (nextColumnIndex >= 0) {
                suffix = suffix.substring(0, nextColumnIndex);
            }
            id = isNamespacesAware ? (prefix + ":" + suffix) : suffix;
        }

        return id;
    }

    private void ignoreUntil(final char ch) throws IOException {
        while (!isAllRead()) {
            go();
            if (isChar(ch)) {
                break;
            }
        }
    }

    /**
     * Checks if end of the content is reached.
     */
    private boolean isAllRead() {
        return len >= 0 && pos >= len;
    }

    /**
     * Checks if character at current runtime position is equal to specified
     * char.
     * 
     * @param ch
     * @return true is equal, false otherwise.
     */
    private boolean isChar(final char ch) {
        return isChar(pos, ch);
    }

    /**
     * Checks if character at specified position is equal to specified char.
     * 
     * @param position
     * @param ch
     * @return true is equals, false otherwise.
     */
    private boolean isChar(final int position, final char ch) {
        if (len >= 0 && position >= len) {
            return false;
        }

        return Character.toLowerCase(ch) == Character.toLowerCase(working[position]);
    }

    private boolean isCharEquals(final char ch) {
        return working[pos] == ch;
    }

    private boolean isCharSimple(final char ch) {
        return (len < 0 || pos < len) && (ch == working[pos]);
    }

    /**
     * Checks if character at current runtime position can be identifier part.
     * 
     * @return true is may be identifier part, false otherwise.
     */
    private boolean isIdentifierChar() {
        if (len >= 0 && pos >= len) {
            return false;
        }

        final char ch = working[pos];
        return Character.isUnicodeIdentifierStart(ch) || Character.isDigit(ch) || Utils.isIdentifierHelperChar(ch);
    }

    /**
     * Checks if character at current runtime position can be identifier start.
     * 
     * @return true is may be identifier start, false otherwise.
     */
    private boolean isIdentifierStartChar() {
        return isIdentifierStartChar(pos);
    }

    /**
     * Checks if character at specified position can be identifier start.
     * 
     * @param position
     * @return true is may be identifier start, false otherwise.
     */
    private boolean isIdentifierStartChar(final int position) {
        if (len >= 0 && position >= len) {
            return false;
        }

        final char ch = working[position];
        return Character.isUnicodeIdentifierStart(ch) || ch == '_';
    }

    /**
     * Checks if specified tag name is one of the reserved tags: HTML, HEAD or
     * BODY
     * 
     * @param tagName
     * @return
     */
    private boolean isReservedTag(String tagName) {
        tagName = tagName.toLowerCase();
        return "html".equals(tagName) || "head".equals(tagName) || "body".equals(tagName);
    }

    private boolean isValidXmlChar() {
        return isAllRead() || Utils.isValidXmlChar(working[pos]);
    }

    private boolean isValidXmlCharSafe() {
        return Utils.isValidXmlChar(working[pos]);
    }

    /**
     * Checks if character at current runtime position is whitespace.
     * 
     * @return true is whitespace, false otherwise.
     */
    @SuppressWarnings("unused")
    private boolean isWhitespace() {
        return isWhitespace(pos);
    }

    /**
     * Checks if character at specified position is whitespace.
     * 
     * @param position
     * @return true is whitespace, false otherwise.
     */
    private boolean isWhitespace(final int position) {
        if (len >= 0 && position >= len) {
            return false;
        }

        return Character.isWhitespace(working[position]);
    }

    private boolean isWhitespaceSafe() {
        return Character.isWhitespace(working[pos]);
    }

    public abstract void makeTree(List<BaseToken> tokenList);

    private void readIfNeeded(final int neededChars) throws IOException {
        if (len == -1 && pos + neededChars >= WORKING_BUFFER_SIZE) {
            final int numToCopy = WORKING_BUFFER_SIZE - pos;
            System.arraycopy(working, pos, working, 0, numToCopy);
            pos = 0;

            int expected = WORKING_BUFFER_SIZE - numToCopy;
            int size = 0;
            int charsRead;
            int offset = numToCopy;
            do {
                charsRead = reader.read(working, offset, expected);
                if (charsRead >= 0) {
                    size += charsRead;
                    offset += charsRead;
                    expected -= charsRead;
                }
            } while (charsRead >= 0 && expected > 0);

            if (expected > 0) {
                len = size + numToCopy;
            }

            // convert invalid XML characters to spaces
            for (int i = 0; i < (len >= 0 ? len : WORKING_BUFFER_SIZE); i++) {
                final int ch = working[i];
                if (ch >= 1 && ch <= 32 && ch != 10 && ch != 13) {
                    working[i] = ' ';
                }
            }
        }
    }

    /**
     * Saves specified character to the temporary buffer.
     * 
     * @param ch
     */
    private void save(final char ch) {
        if (savedLen >= saved.length) {
            final char newSaved[] = new char[saved.length + 512];
            System.arraycopy(saved, 0, newSaved, 0, saved.length);
            saved = newSaved;
        }
        saved[savedLen++] = ch;
    }

    /**
     * Saves character at current runtime position to the temporary buffer.
     */
    private void saveCurrent() {
        if (!isAllRead()) {
            save(working[pos]);
        }
    }

    /**
     * Saves specified number of characters at current runtime position to the
     * temporary buffer.
     * 
     * @throws IOException
     */
    private void saveCurrent(int size) throws IOException {
        readIfNeeded(size);
        int pos = this.pos;
        while (!isAllRead() && (size > 0)) {
            save(working[pos]);
            pos++;
            size--;
        }
    }

    private void saveCurrentSafe() {
        save(working[pos]);
    }

    /**
     * Skips whitespaces at current position and moves foreward until
     * non-whitespace character is found or the end of content is reached.
     * 
     * @throws IOException
     */
    private void skipWhitespaces() throws IOException {
        while (!isAllRead() && isWhitespaceSafe()) {
            saveCurrentSafe();
            go();
        }
    }

    /**
     * Starts parsing HTML.
     * 
     * @throws IOException
     */
    public void start() throws IOException {
        // initialize runtime values
        currentTagToken = null;
        tokenList.clear();
        asExpected = true;
        isScriptContext = false;

        boolean isLateForDoctype = false;

        this.pos = WORKING_BUFFER_SIZE;
        readIfNeeded(0);

        boolean isScriptEmpty = true;

        while (!isAllRead()) {
            // resets all the runtime values
            savedLen = 0;
            currentTagToken = null;
            asExpected = true;

            // this is enough for making decision
            readIfNeeded(10);

            if (isScriptContext) {
                if (startsWith("</script") && (isWhitespace(pos + 8) || isChar(pos + 8, '>'))) {
                    tagEnd();
                } else if (isScriptEmpty && startsWithSimple("<!--")) {
                    comment();
                } else {
                    final boolean isTokenAdded = content();
                    if (isScriptEmpty && isTokenAdded) {
                        final BaseToken lastToken = tokenList.get(tokenList.size() - 1);
                        if (lastToken != null) {
                            final String lastTokenAsString = lastToken.toString();
                            if (lastTokenAsString != null && lastTokenAsString.trim().length() > 0) {
                                isScriptEmpty = false;
                            }
                        }
                    }
                }
                if (!isScriptContext) {
                    isScriptEmpty = true;
                }
            } else {
                if (startsWith("<!doctype")) {
                    if (!isLateForDoctype) {
                        doctype();
                        isLateForDoctype = true;
                    } else {
                        ignoreUntil('<');
                    }
                } else if (startsWithSimple("</") && isIdentifierStartChar(pos + 2)) {
                    isLateForDoctype = true;
                    tagEnd();
                } else if (startsWithSimple("<!--")) {
                    comment();
                } else if (startsWithSimple("<") && isIdentifierStartChar(pos + 1)) {
                    isLateForDoctype = true;
                    tagStart();
                } else if (props.isIgnoreQuestAndExclam() && (startsWithSimple("<!") || startsWithSimple("<?"))) {
                    ignoreUntil('>');
                    if (isCharSimple('>')) {
                        go();
                    }
                } else {
                    content();
                }
            }
        }

        reader.close();
    }

    /**
     * Checks if content starts with specified value at the current position.
     * 
     * @param value
     * @return true if starts with specified value, false otherwise.
     * @throws IOException
     */
    private boolean startsWith(final String value) throws IOException {
        final int valueLen = value.length();
        readIfNeeded(valueLen);
        if (len >= 0 && pos + valueLen > len) {
            return false;
        }

        for (int i = 0; i < valueLen; i++) {
            final char ch1 = Character.toLowerCase(value.charAt(i));
            final char ch2 = Character.toLowerCase(working[pos + i]);
            if (ch1 != ch2) {
                return false;
            }
        }

        return true;
    }

    private boolean startsWithSimple(final String value) throws IOException {
        final int valueLen = value.length();
        readIfNeeded(valueLen);
        if (len >= 0 && pos + valueLen > len) {
            return false;
        }

        for (int i = 0; i < valueLen; i++) {
            if (value.charAt(i) != working[pos + i]) {
                return false;
            }
        }

        return true;
    }

    /**
     * Parses list tag attributes from the current position.
     * 
     * @throws IOException
     */
    private void tagAttributes() throws IOException {
        while (!isAllRead() && asExpected && !isCharSimple('>') && !startsWithSimple("/>")) {
            skipWhitespaces();
            final String attName = identifier();

            if (!asExpected) {
                if (!isCharSimple('<') && !isCharSimple('>') && !startsWithSimple("/>")) {
                    if (isValidXmlChar()) {
                        saveCurrent();
                    }
                    go();
                }

                if (!isCharSimple('<')) {
                    asExpected = true;
                }

                continue;
            }

            String attValue;

            skipWhitespaces();
            if (isCharSimple('=')) {
                saveCurrentSafe();
                go();
                attValue = attributeValue();
            } else if (CleanerProperties.BOOL_ATT_EMPTY.equals(props.getBooleanAttributeValues())) {
                attValue = "";
            } else if (CleanerProperties.BOOL_ATT_TRUE.equals(props.getBooleanAttributeValues())) {
                attValue = "true";
            } else {
                attValue = attName;
            }

            if (asExpected) {
                currentTagToken.setAttribute(attName, attValue);
            }
        }
    }

/**
         * Parses end of the tag.
         * It expects that current position is at the "<" after which
         * "/" and the tag's name follows.
         * @throws IOException
         */
    private void tagEnd() throws IOException {
        saveCurrent(2);
        go(2);

        if (isAllRead()) {
            return;
        }

        String tagName = identifier();
        if (transformations != null && transformations.hasTransformationForTag(tagName)) {
            final TagTransformation tagTransformation = transformations.getTransformation(tagName);
            if (tagTransformation != null) {
                tagName = tagTransformation.getDestTag();
            }
        }

        if (tagName != null) {
            final TagInfo tagInfo = tagInfoProvider.getTagInfo(tagName);
            if ((tagInfo == null && !isOmitUnknownTags && isTreatUnknownTagsAsContent && !isReservedTag(tagName))
                    || (tagInfo != null && tagInfo.isDeprecated() && !isOmitDeprecatedTags && isTreatDeprecatedTagsAsContent)) {
                content();
                return;
            }
        }

        currentTagToken = new EndTagToken(tagName);

        if (asExpected) {
            skipWhitespaces();
            tagAttributes();

            if (tagName != null) {
                addToken(currentTagToken);
            }

            if (isCharSimple('>')) {
                go();
            }

            if ("script".equalsIgnoreCase(tagName)) {
                isScriptContext = false;
            }

            currentTagToken = null;
        } else {
            addSavedAsContent();
        }
    }

/**
         * Parses start of the tag.
         * It expects that current position is at the "<" after which
         * the tag's name follows.
         * @throws IOException
         */
    private void tagStart() throws IOException {
        saveCurrent();
        go();

        if (isAllRead()) {
            return;
        }

        String tagName = identifier();

        TagTransformation tagTransformation = null;
        if (transformations != null && transformations.hasTransformationForTag(tagName)) {
            tagTransformation = transformations.getTransformation(tagName);
            if (tagTransformation != null) {
                tagName = tagTransformation.getDestTag();
            }
        }

        if (tagName != null) {
            final TagInfo tagInfo = tagInfoProvider.getTagInfo(tagName);
            if ((tagInfo == null && !isOmitUnknownTags && isTreatUnknownTagsAsContent && !isReservedTag(tagName))
                    || (tagInfo != null && tagInfo.isDeprecated() && !isOmitDeprecatedTags && isTreatDeprecatedTagsAsContent)) {
                content();
                return;
            }
        }

        final TagNode tagNode = createTagNode(tagName);
        currentTagToken = tagNode;

        if (asExpected) {
            skipWhitespaces();
            tagAttributes();

            if (tagName != null) {
                if (tagTransformation != null) {
                    tagNode.transformAttributes(tagTransformation);
                }
                addToken(currentTagToken);
            }

            if (isCharSimple('>')) {
                go();
                if ("script".equalsIgnoreCase(tagName)) {
                    isScriptContext = true;
                }
            } else if (startsWithSimple("/>")) {
                go(2);
                if ("script".equalsIgnoreCase(tagName)) {
                    addToken(new EndTagToken(tagName));
                }
            }

            currentTagToken = null;
        } else {
            addSavedAsContent();
        }
    }

}
