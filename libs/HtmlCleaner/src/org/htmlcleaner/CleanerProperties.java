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

/**
 * Properties defining cleaner's behaviour
 */
public class CleanerProperties {
    public static final String BOOL_ATT_SELF = "self";
    public static final String BOOL_ATT_EMPTY = "empty";
    public static final String BOOL_ATT_TRUE = "true";

    private ITagInfoProvider tagInfoProvider = null;
    private boolean advancedXmlEscape = true;
    private boolean transResCharsToNCR = false;
    private boolean useCdataForScriptAndStyle = true;
    private boolean translateSpecialEntities = true;
    private boolean transSpecialEntitiesToNCR = false;
    private boolean recognizeUnicodeChars = true;
    private boolean omitUnknownTags = false;
    private boolean treatUnknownTagsAsContent = false;
    private boolean omitDeprecatedTags = false;
    private boolean treatDeprecatedTagsAsContent = false;
    private boolean omitComments = false;
    private boolean omitXmlDeclaration = false;
    private boolean omitDoctypeDeclaration = true;
    private boolean omitHtmlEnvelope = false;
    private boolean useEmptyElementTags = true;
    private boolean allowMultiWordAttributes = true;
    private boolean allowHtmlInsideAttributes = false;
    private boolean ignoreQuestAndExclam = true;
    private boolean namespacesAware = true;
    private String hyphenReplacementInComment = "=";
    private String booleanAttributeValues = BOOL_ATT_SELF;
    private String pruneTags = null;

    public String getBooleanAttributeValues() {
        return booleanAttributeValues;
    }

    public String getHyphenReplacementInComment() {
        return hyphenReplacementInComment;
    }

    public String getPruneTags() {
        return pruneTags;
    }

    public ITagInfoProvider getTagInfoProvider() {
        return tagInfoProvider;
    }

    public boolean isAdvancedXmlEscape() {
        return advancedXmlEscape;
    }

    public boolean isAllowHtmlInsideAttributes() {
        return allowHtmlInsideAttributes;
    }

    public boolean isAllowMultiWordAttributes() {
        return allowMultiWordAttributes;
    }

    public boolean isIgnoreQuestAndExclam() {
        return ignoreQuestAndExclam;
    }

    public boolean isNamespacesAware() {
        return namespacesAware;
    }

    public boolean isOmitComments() {
        return omitComments;
    }

    public boolean isOmitDeprecatedTags() {
        return omitDeprecatedTags;
    }

    public boolean isOmitDoctypeDeclaration() {
        return omitDoctypeDeclaration;
    }

    public boolean isOmitHtmlEnvelope() {
        return omitHtmlEnvelope;
    }

    public boolean isOmitUnknownTags() {
        return omitUnknownTags;
    }

    public boolean isOmitXmlDeclaration() {
        return omitXmlDeclaration;
    }

    public boolean isRecognizeUnicodeChars() {
        return recognizeUnicodeChars;
    }

    public boolean isTranslateSpecialEntities() {
        return translateSpecialEntities;
    }

    public boolean isTransResCharsToNCR() {
        return transResCharsToNCR;
    }

    public boolean isTransSpecialEntitiesToNCR() {
        return transSpecialEntitiesToNCR;
    }

    public boolean isTreatDeprecatedTagsAsContent() {
        return treatDeprecatedTagsAsContent;
    }

    public boolean isTreatUnknownTagsAsContent() {
        return treatUnknownTagsAsContent;
    }

    public boolean isUseCdataForScriptAndStyle() {
        return useCdataForScriptAndStyle;
    }

    public boolean isUseEmptyElementTags() {
        return useEmptyElementTags;
    }

    public void setAdvancedXmlEscape(final boolean advancedXmlEscape) {
        this.advancedXmlEscape = advancedXmlEscape;
    }

    public void setAllowHtmlInsideAttributes(final boolean allowHtmlInsideAttributes) {
        this.allowHtmlInsideAttributes = allowHtmlInsideAttributes;
    }

    public void setAllowMultiWordAttributes(final boolean allowMultiWordAttributes) {
        this.allowMultiWordAttributes = allowMultiWordAttributes;
    }

    public void setBooleanAttributeValues(final String booleanAttributeValues) {
        if (BOOL_ATT_SELF.equalsIgnoreCase(booleanAttributeValues)
                || BOOL_ATT_EMPTY.equalsIgnoreCase(booleanAttributeValues)
                || BOOL_ATT_TRUE.equalsIgnoreCase(booleanAttributeValues)) {
            this.booleanAttributeValues = booleanAttributeValues.toLowerCase();
        } else {
            this.booleanAttributeValues = BOOL_ATT_SELF;
        }
    }

    public void setHyphenReplacementInComment(final String hyphenReplacementInComment) {
        this.hyphenReplacementInComment = hyphenReplacementInComment;
    }

    public void setIgnoreQuestAndExclam(final boolean ignoreQuestAndExclam) {
        this.ignoreQuestAndExclam = ignoreQuestAndExclam;
    }

    public void setNamespacesAware(final boolean namespacesAware) {
        this.namespacesAware = namespacesAware;
    }

    public void setOmitComments(final boolean omitComments) {
        this.omitComments = omitComments;
    }

    public void setOmitDeprecatedTags(final boolean omitDeprecatedTags) {
        this.omitDeprecatedTags = omitDeprecatedTags;
    }

    public void setOmitDoctypeDeclaration(final boolean omitDoctypeDeclaration) {
        this.omitDoctypeDeclaration = omitDoctypeDeclaration;
    }

    public void setOmitHtmlEnvelope(final boolean omitHtmlEnvelope) {
        this.omitHtmlEnvelope = omitHtmlEnvelope;
    }

    public void setOmitUnknownTags(final boolean omitUnknownTags) {
        this.omitUnknownTags = omitUnknownTags;
    }

    public void setOmitXmlDeclaration(final boolean omitXmlDeclaration) {
        this.omitXmlDeclaration = omitXmlDeclaration;
    }

    public void setPruneTags(final String pruneTags) {
        this.pruneTags = pruneTags;
    }

    public void setRecognizeUnicodeChars(final boolean recognizeUnicodeChars) {
        this.recognizeUnicodeChars = recognizeUnicodeChars;
    }

    public void setTagInfoProvider(final ITagInfoProvider tagInfoProvider) {
        this.tagInfoProvider = tagInfoProvider;
    }

    public void setTranslateSpecialEntities(final boolean translateSpecialEntities) {
        this.translateSpecialEntities = translateSpecialEntities;
    }

    public void setTransResCharsToNCR(final boolean transResCharsToNCR) {
        this.transResCharsToNCR = transResCharsToNCR;
    }

    public void setTransSpecialEntitiesToNCR(final boolean transSpecialEntitiesToNCR) {
        this.transSpecialEntitiesToNCR = transSpecialEntitiesToNCR;
    }

    public void setTreatDeprecatedTagsAsContent(final boolean treatDeprecatedTagsAsContent) {
        this.treatDeprecatedTagsAsContent = treatDeprecatedTagsAsContent;
    }

    public void setTreatUnknownTagsAsContent(final boolean treatUnknownTagsAsContent) {
        this.treatUnknownTagsAsContent = treatUnknownTagsAsContent;
    }

    public void setUseCdataForScriptAndStyle(final boolean useCdataForScriptAndStyle) {
        this.useCdataForScriptAndStyle = useCdataForScriptAndStyle;
    }

    public void setUseEmptyElementTags(final boolean useEmptyElementTags) {
        this.useEmptyElementTags = useEmptyElementTags;
    }
}
