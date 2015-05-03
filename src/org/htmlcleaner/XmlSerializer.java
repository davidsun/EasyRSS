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
 * Abstract XML serializer - contains common logic for descendants.
 * </p>
 */
public abstract class XmlSerializer extends Serializer {

    protected XmlSerializer(final CleanerProperties props) {
        super(props);
    }

    /**
     * @deprecated Use writeToStream() instead.
     */
    @Deprecated
    public void writeXmlToStream(final TagNode tagNode, final OutputStream out, final String charset)
            throws IOException {
        super.writeToStream(tagNode, out, charset);
    }

    /**
     * @deprecated Use writeToStream() instead.
     */
    @Deprecated
    public void writeXmlToStream(final TagNode tagNode, final OutputStream out) throws IOException {
        super.writeToStream(tagNode, out);
    }

    /**
     * @deprecated Use writeToFile() instead.
     */
    @Deprecated
    public void writeXmlToFile(final TagNode tagNode, final String fileName, final String charset) throws IOException {
        super.writeToFile(tagNode, fileName, charset);
    }

    /**
     * @deprecated Use writeToFile() instead.
     */
    @Deprecated
    public void writeXmlToFile(final TagNode tagNode, final String fileName) throws IOException {
        super.writeToFile(tagNode, fileName);
    }

    /**
     * @deprecated Use getAsString() instead.
     */
    @Deprecated
    public String getXmlAsString(final TagNode tagNode, final String charset) throws IOException {
        return super.getAsString(tagNode, charset);
    }

    /**
     * @deprecated Use getAsString() instead.
     */
    @Deprecated
    public String getXmlAsString(final TagNode tagNode) throws IOException {
        return super.getAsString(tagNode);
    }

    /**
     * @deprecated Use write() instead.
     */
    @Deprecated
    public void writeXml(final TagNode tagNode, final Writer writer, final String charset) throws IOException {
        super.write(tagNode, writer, charset);
    }

    protected String escapeXml(final String xmlContent) {
        return Utils.escapeXml(xmlContent, props, false);
    }

    protected boolean dontEscape(final TagNode tagNode) {
        return props.isUseCdataForScriptAndStyle() && isScriptOrStyle(tagNode);
    }

    protected boolean isMinimizedTagSyntax(final TagNode tagNode) {
        final TagInfo tagInfo = props.getTagInfoProvider().getTagInfo(tagNode.getName());
        return tagNode.getChildren().size() == 0
                && (props.isUseEmptyElementTags() || (tagInfo != null && tagInfo.isEmptyTag()));
    }

    protected void serializeOpenTag(final TagNode tagNode, final Writer writer, final boolean newLine)
            throws IOException {
        String tagName = tagNode.getName();

        if (Utils.isEmptyString(tagName)) {
            return;
        }

        final boolean nsAware = props.isNamespacesAware();

        Set<String> definedNSPrefixes = null;
        Set<String> additionalNSDeclNeeded = null;

        final String tagPrefix = Utils.getXmlNSPrefix(tagName);
        if (tagPrefix != null) {
            if (nsAware) {
                definedNSPrefixes = new HashSet<String>();
                tagNode.collectNamespacePrefixesOnPath(definedNSPrefixes);
                if (!definedNSPrefixes.contains(tagPrefix)) {
                    additionalNSDeclNeeded = new TreeSet<String>();
                    additionalNSDeclNeeded.add(tagPrefix);
                }
            } else {
                tagName = Utils.getXmlName(tagName);
            }
        }

        writer.write("<" + tagName);

        // write attributes
        for (Map.Entry<String, String> entry : tagNode.getAttributes().entrySet()) {
            String attName = entry.getKey();
            final String attPrefix = Utils.getXmlNSPrefix(attName);
            if (attPrefix != null) {
                if (nsAware) {
                    // collect used namespace prefixes in attributes in order to
                    // explicitly define
                    // ns declaration if needed; otherwise it would be
                    // ill-formed xml
                    if (definedNSPrefixes == null) {
                        definedNSPrefixes = new HashSet<String>();
                        tagNode.collectNamespacePrefixesOnPath(definedNSPrefixes);
                    }
                    if (!definedNSPrefixes.contains(attPrefix)) {
                        if (additionalNSDeclNeeded == null) {
                            additionalNSDeclNeeded = new TreeSet<String>();
                        }
                        additionalNSDeclNeeded.add(attPrefix);
                    }
                } else {
                    attName = Utils.getXmlName(attName);
                }
            }
            writer.write(" " + attName + "=\"" + escapeXml(entry.getValue()) + "\"");
        }

        // write namespace declarations
        if (nsAware) {
            final Map<String, String> nsDeclarations = tagNode.getNamespaceDeclarations();
            if (nsDeclarations != null) {
                for (Map.Entry<String, String> entry : nsDeclarations.entrySet()) {
                    final String prefix = entry.getKey();
                    String att = "xmlns";
                    if (prefix.length() > 0) {
                        att += ":" + prefix;
                    }
                    writer.write(" " + att + "=\"" + escapeXml(entry.getValue()) + "\"");
                }
            }
        }

        // write additional namespace declarations needed for this tag in order
        // xml to be well-formed
        if (additionalNSDeclNeeded != null) {
            for (String prefix : additionalNSDeclNeeded) {
                writer.write(" xmlns:" + prefix + "=\"" + prefix + "\"");
            }
        }

        if (isMinimizedTagSyntax(tagNode)) {
            writer.write(" />");
            if (newLine) {
                writer.write("\n");
            }
        } else if (dontEscape(tagNode)) {
            writer.write("><![CDATA[");
        } else {
            writer.write(">");
        }
    }

    protected void serializeEndTag(final TagNode tagNode, final Writer writer, final boolean newLine)
            throws IOException {
        String tagName = tagNode.getName();

        if (Utils.isEmptyString(tagName)) {
            return;
        }

        if (dontEscape(tagNode)) {
            writer.write("]]>");
        }

        if (Utils.getXmlNSPrefix(tagName) != null && !props.isNamespacesAware()) {
            tagName = Utils.getXmlName(tagName);
        }
        writer.write("</" + tagName + ">");

        if (newLine) {
            writer.write("\n");
        }
    }

}
