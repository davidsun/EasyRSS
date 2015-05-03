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

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Describes how specified tag is transformed to another one, or is ignored
 * during parsing
 */
public class TagTransformation {

    private String sourceTag;
    private String destTag;
    private boolean preserveSourceAttributes;
    private Map attributeTransformations;

    /**
     * Creates new tag transformation from source tag to target tag specifying
     * whether source tag attributes are preserved.
     * 
     * @param sourceTag
     *            Name of the tag to be transformed.
     * @param destTag
     *            Name of tag to which source tag is to be transformed.
     * @param preserveSourceAttributes
     *            Tells whether source tag attributes are preserved in
     *            transformation.
     */
    public TagTransformation(final String sourceTag, final String destTag, final boolean preserveSourceAttributes) {
        this.sourceTag = sourceTag.toLowerCase();
        if (destTag == null) {
            this.destTag = null;
        } else {
            this.destTag = Utils.isValidXmlIdentifier(destTag) ? destTag.toLowerCase() : sourceTag;
        }
        this.preserveSourceAttributes = preserveSourceAttributes;
    }

    /**
     * Creates new tag transformation from source tag to target tag preserving
     * all source tag attributes.
     * 
     * @param sourceTag
     *            Name of the tag to be transformed.
     * @param destTag
     *            Name of tag to which source tag is to be transformed.
     */
    public TagTransformation(final String sourceTag, final String destTag) {
        this(sourceTag, destTag, true);
    }

    /**
     * Creates new tag transformation in which specified tag will be skipped
     * (ignored) during parsing process.
     * 
     * @param sourceTag
     */
    public TagTransformation(final String sourceTag) {
        this(sourceTag, null);
    }

    /**
     * Adds new attribute transformation to this tag transformation. It tells
     * how destination attribute will look like. Small templating mechanism is
     * used to describe attribute value: all names between ${ and } inside the
     * template are evaluated against source tag attributes. That way one can
     * make attribute values consist of mix of source tag attributes.
     * 
     * @param targetAttName
     *            Name of the destination attribute
     * @param transformationDesc
     *            Template describing attribute value.
     */
    public void addAttributeTransformation(final String targetAttName, final String transformationDesc) {
        if (attributeTransformations == null) {
            attributeTransformations = new LinkedHashMap();
        }
        attributeTransformations.put(targetAttName.toLowerCase(), transformationDesc);
    }

    /**
     * Adds new attribute transformation in which destination attrbute will not
     * exists (simply removes it from list of attributes).
     * 
     * @param targetAttName
     */
    public void addAttributeTransformation(final String targetAttName) {
        addAttributeTransformation(targetAttName, null);
    }

    boolean hasAttributeTransformations() {
        return attributeTransformations != null;
    }

    String getSourceTag() {
        return sourceTag;
    }

    String getDestTag() {
        return destTag;
    }

    boolean isPreserveSourceAttributes() {
        return preserveSourceAttributes;
    }

    Map getAttributeTransformations() {
        return attributeTransformations;
    }

}
