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
 * Class contains information about single HTML tag.<br/>
 * It also contains rules for tag balancing. For each tag, list of dependant
 * tags may be defined. There are several kinds of dependancies used to reorder
 * tags:
 * <ul>
 * <li>
 * fatal tags - required outer tag - the tag will be ignored during parsing
 * (will be skipped) if this fatal tag is missing. For example, most web
 * browsers ignore elements TD, TR, TBODY if they are not in the context of
 * TABLE tag.</li>
 * <li>
 * required enclosing tags - if there is no such, it is implicitely created. For
 * example if TD is out of TR - open TR is created before.</li>
 * <li>
 * forbidden tags - it is not allowed to occure inside - for example FORM cannot
 * be inside other FORM and it will be ignored during cleanup.</li>
 * <li>
 * allowed children tags - for example TR allowes TD and TH. If there are some
 * dependant allowed tags defined then cleaner ignores other tags, treating them
 * as unallowed, unless they are in some other relationship with this tag.</li>
 * <li>
 * higher level tags - for example for TR higher tags are THEAD, TBODY, TFOOT.</li>
 * <li>
 * tags that must be closed and copied - for example, in
 * <code>&lt;a href="#"&gt;&lt;div&gt;....</code> tag A must be closed before
 * DIV but copied again inside DIV.</li>
 * <li>
 * tags that must be closed before closing this tag and copied again after - for
 * example, in <code>&lt;i&gt;&lt;b&gt;at&lt;/i&gt; first&lt;/b&gt; text </code>
 * tag B must be closed before closing I, but it must be copied again after
 * resulting finally in sequence:
 * <code>&lt;i&gt;&lt;b&gt;at&lt;/b&gt;&lt;/i&gt;&lt;b&gt; first&lt;/b&gt; text </code>
 * .</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Tag TR for instance (table row) may define the following dependancies:
 * <ul>
 * <li>fatal tag is <code>table</code></li>
 * <li>required enclosing tag is <code>tbody</code></li>
 * <li>allowed children tags are <code>td,th</code></li>
 * <li>higher level tags are <code>thead,tfoot</code></li>
 * <li>tags that muste be closed before are
 * <code>tr,td,th,caption,colgroup</code></li>
 * </ul>
 * meaning the following: <br>
 * <ul>
 * <li><code>tr</code> must be in context of <code>table</code>, otherwise it
 * will be ignored,</li>
 * <li><code>tr</code> may can be directly inside <code>tbody</code>,
 * <code>tfoot</code> and <code>thead</code>, otherwise <code>tbody</code> will
 * be implicitely created in front of it.</li>
 * <li><code>tr</code> can contain <code>td</code> and <code>th</code>, all
 * other tags and content will be pushed out of current limiting context, in the
 * case of html tables, in front of enclosing <code>table</code> tag.</li>
 * <li>if previous open tag is one of <code>tr</code>, <code>caption</code> or
 * <code>colgroup</code>, it will be implicitely closed.</li>
 * </ul>
 * </p>
 */
public class TagInfo {

    protected static final int HEAD_AND_BODY = 0;
    protected static final int HEAD = 1;
    protected static final int BODY = 2;

    protected static final int CONTENT_ALL = 0;
    protected static final int CONTENT_NONE = 1;
    protected static final int CONTENT_TEXT = 2;

    private String name;
    final private int contentType;
    private Set mustCloseTags = new HashSet();
    private Set higherTags = new HashSet();
    private Set childTags = new HashSet();
    private Set permittedTags = new HashSet();
    private Set copyTags = new HashSet();
    private Set continueAfterTags = new HashSet();
    private int belongsTo = BODY;
    private String requiredParent = null;
    private String fatalTag = null;
    private boolean deprecated = false;
    private boolean unique = false;
    private boolean ignorePermitted = false;

    public TagInfo(final String name, final int contentType, final int belongsTo, final boolean depricated,
            final boolean unique, final boolean ignorePermitted) {
        this.name = name;
        this.contentType = contentType;
        this.belongsTo = belongsTo;
        this.deprecated = depricated;
        this.unique = unique;
        this.ignorePermitted = ignorePermitted;
    }

    public boolean allowsAnything() {
        return CONTENT_ALL == contentType && childTags.isEmpty();
    }

    public boolean allowsBody() {
        return CONTENT_NONE != contentType;
    }

    public boolean allowsItem(final BaseToken token) {
        if (contentType != CONTENT_NONE && token instanceof TagToken) {
            final TagToken tagToken = (TagToken) token;
            final String tagName = tagToken.getName();
            if ("script".equals(tagName)) {
                return true;
            }
        }

        if (CONTENT_ALL == contentType) {
            if (!childTags.isEmpty()) {
                return token instanceof TagToken ? childTags.contains(((TagToken) token).getName()) : false;
            } else if (!permittedTags.isEmpty()) {
                return token instanceof TagToken ? !permittedTags.contains(((TagToken) token).getName()) : true;
            }
            return true;
        } else if (CONTENT_TEXT == contentType) {
            return !(token instanceof TagToken);
        }

        return false;
    }

    public void defineAllowedChildrenTags(final String commaSeparatedListOfTags) {
        final StringTokenizer tokenizer = new StringTokenizer(commaSeparatedListOfTags.toLowerCase(), ",");
        while (tokenizer.hasMoreTokens()) {
            final String currTag = tokenizer.nextToken();
            this.childTags.add(currTag);
        }
    }

    public void defineCloseBeforeCopyInsideTags(final String commaSeparatedListOfTags) {
        final StringTokenizer tokenizer = new StringTokenizer(commaSeparatedListOfTags.toLowerCase(), ",");
        while (tokenizer.hasMoreTokens()) {
            final String currTag = tokenizer.nextToken();
            this.copyTags.add(currTag);
            this.mustCloseTags.add(currTag);
        }
    }

    public void defineCloseBeforeTags(final String commaSeparatedListOfTags) {
        final StringTokenizer tokenizer = new StringTokenizer(commaSeparatedListOfTags.toLowerCase(), ",");
        while (tokenizer.hasMoreTokens()) {
            final String currTag = tokenizer.nextToken();
            this.mustCloseTags.add(currTag);
        }
    }

    public void defineCloseInsideCopyAfterTags(final String commaSeparatedListOfTags) {
        final StringTokenizer tokenizer = new StringTokenizer(commaSeparatedListOfTags.toLowerCase(), ",");
        while (tokenizer.hasMoreTokens()) {
            final String currTag = tokenizer.nextToken();
            this.continueAfterTags.add(currTag);
        }
    }

    public void defineFatalTags(final String commaSeparatedListOfTags) {
        final StringTokenizer tokenizer = new StringTokenizer(commaSeparatedListOfTags.toLowerCase(), ",");
        while (tokenizer.hasMoreTokens()) {
            final String currTag = tokenizer.nextToken();
            this.fatalTag = currTag;
            this.higherTags.add(currTag);
        }
    }

    // getters and setters

    public void defineForbiddenTags(final String commaSeparatedListOfTags) {
        final StringTokenizer tokenizer = new StringTokenizer(commaSeparatedListOfTags.toLowerCase(), ",");
        while (tokenizer.hasMoreTokens()) {
            final String currTag = tokenizer.nextToken();
            this.permittedTags.add(currTag);
        }
    }

    public void defineHigherLevelTags(final String commaSeparatedListOfTags) {
        final StringTokenizer tokenizer = new StringTokenizer(commaSeparatedListOfTags.toLowerCase(), ",");
        while (tokenizer.hasMoreTokens()) {
            final String currTag = tokenizer.nextToken();
            this.higherTags.add(currTag);
        }
    }

    public void defineRequiredEnclosingTags(final String commaSeparatedListOfTags) {
        final StringTokenizer tokenizer = new StringTokenizer(commaSeparatedListOfTags.toLowerCase(), ",");
        while (tokenizer.hasMoreTokens()) {
            final String currTag = tokenizer.nextToken();
            this.requiredParent = currTag;
            this.higherTags.add(currTag);
        }
    }

    public int getBelongsTo() {
        return belongsTo;
    }

    public Set getChildTags() {
        return childTags;
    }

    public int getContentType() {
        return contentType;
    }

    public Set getContinueAfterTags() {
        return continueAfterTags;
    }

    public Set getCopyTags() {
        return copyTags;
    }

    public String getFatalTag() {
        return fatalTag;
    }

    public Set getHigherTags() {
        return higherTags;
    }

    public Set getMustCloseTags() {
        return mustCloseTags;
    }

    public String getName() {
        return name;
    }

    public Set getPermittedTags() {
        return permittedTags;
    }

    public String getRequiredParent() {
        return requiredParent;
    }

    public boolean hasCopyTags() {
        return !copyTags.isEmpty();
    }

    public boolean hasPermittedTags() {
        return !permittedTags.isEmpty();
    }

    public boolean isContinueAfter(final String tagName) {
        return continueAfterTags.contains(tagName);
    }

    public boolean isCopy(final String tagName) {
        return copyTags.contains(tagName);
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public boolean isEmptyTag() {
        return CONTENT_NONE == contentType;
    }

    public boolean isHeadAndBodyTag() {
        return belongsTo == HEAD || belongsTo == HEAD_AND_BODY;
    }

    public boolean isHeadTag() {
        return belongsTo == HEAD;
    }

    public boolean isHigher(final String tagName) {
        return higherTags.contains(tagName);
    }

    public boolean isIgnorePermitted() {
        return ignorePermitted;
    }

    public boolean isMustCloseTag(final TagInfo tagInfo) {
        if (tagInfo != null) {
            return mustCloseTags.contains(tagInfo.getName()) || tagInfo.contentType == CONTENT_TEXT;
        }

        return false;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setBelongsTo(final int belongsTo) {
        this.belongsTo = belongsTo;
    }

    public void setChildTags(final Set childTags) {
        this.childTags = childTags;
    }

    // other functionality

    public void setContinueAfterTags(final Set continueAfterTags) {
        this.continueAfterTags = continueAfterTags;
    }

    public void setCopyTags(final Set copyTags) {
        this.copyTags = copyTags;
    }

    public void setDeprecated(final boolean deprecated) {
        this.deprecated = deprecated;
    }

    public void setFatalTag(final String fatalTag) {
        this.fatalTag = fatalTag;
    }

    public void setHigherTags(final Set higherTags) {
        this.higherTags = higherTags;
    }

    public void setIgnorePermitted(final boolean ignorePermitted) {
        this.ignorePermitted = ignorePermitted;
    }

    public void setMustCloseTags(final Set mustCloseTags) {
        this.mustCloseTags = mustCloseTags;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setPermittedTags(final Set permittedTags) {
        this.permittedTags = permittedTags;
    }

    public void setRequiredParent(final String requiredParent) {
        this.requiredParent = requiredParent;
    }

    public void setUnique(final boolean unique) {
        this.unique = unique;
    }

}
