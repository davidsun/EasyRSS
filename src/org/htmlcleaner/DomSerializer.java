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

import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * DOM serializer - creates xml DOM.
 * </p>
 */
public class DomSerializer {
    protected CleanerProperties props;
    protected boolean escapeXml = true;

    public DomSerializer(final CleanerProperties props, final boolean escapeXml) {
        this.props = props;
        this.escapeXml = escapeXml;
    }

    public DomSerializer(final CleanerProperties props) {
        this(props, true);
    }

    public Document createDOM(final TagNode rootNode) throws ParserConfigurationException {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        final Document document = factory.newDocumentBuilder().newDocument();
        final Element rootElement = createElement(rootNode, document);
        document.appendChild(rootElement);
        setAttributes(rootNode, rootElement);
        createSubnodes(document, rootElement, rootNode.getChildren());
        return document;
    }

    private Element createElement(final TagNode node, final Document document) {
        String name = node.getName();
        final boolean nsAware = props.isNamespacesAware();
        final String prefix = Utils.getXmlNSPrefix(name);
        final Map<String, String> nsDeclarations = node.getNamespaceDeclarations();
        String nsURI = null;
        if (prefix != null) {
            if (nsAware) {
                if (nsDeclarations != null) {
                    nsURI = nsDeclarations.get(prefix);
                }
                if (nsURI == null) {
                    nsURI = node.getNamespaceURIOnPath(prefix);
                }
                if (nsURI == null) {
                    nsURI = prefix;
                }
            } else {
                name = Utils.getXmlName(name);
            }
        } else {
            if (nsAware) {
                if (nsDeclarations != null) {
                    nsURI = nsDeclarations.get("");
                }
                if (nsURI == null) {
                    nsURI = node.getNamespaceURIOnPath(prefix);
                }
            }
        }

        if (nsAware && nsURI != null) {
            return document.createElementNS(nsURI, name);
        } else {
            return document.createElement(name);
        }
    }

    private void setAttributes(final TagNode node, final Element element) {
        for (final Map.Entry<String, String> entry : node.getAttributes().entrySet()) {
            final String attrName = entry.getKey();
            String attrValue = entry.getValue();
            if (escapeXml) {
                attrValue = Utils.escapeXml(attrValue, props, true);
            }

            final String attPrefix = Utils.getXmlNSPrefix(attrName);
            if (attPrefix != null) {
                if (props.isNamespacesAware()) {
                    String nsURI = node.getNamespaceURIOnPath(attPrefix);
                    if (nsURI == null) {
                        nsURI = attPrefix;
                    }
                    element.setAttributeNS(nsURI, attrName, attrValue);
                } else {
                    element.setAttribute(Utils.getXmlName(attrName), attrValue);
                }
            } else {
                element.setAttribute(attrName, attrValue);
            }
        }
    }

    private void createSubnodes(final Document document, final Element element, final List<Object> tagChildren) {
        if (tagChildren != null) {
            final Iterator<Object> it = tagChildren.iterator();
            while (it.hasNext()) {
                final Object item = it.next();
                if (item instanceof CommentNode) {
                    final CommentNode commentNode = (CommentNode) item;
                    final Comment comment = document.createComment(commentNode.getContent().toString());
                    element.appendChild(comment);
                } else if (item instanceof ContentNode) {
                    final String nodeName = element.getNodeName();
                    String content = item.toString();
                    final boolean specialCase = props.isUseCdataForScriptAndStyle()
                            && ("script".equalsIgnoreCase(nodeName) || "style".equalsIgnoreCase(nodeName));
                    if (escapeXml && !specialCase) {
                        content = Utils.escapeXml(content, props, true);
                    }
                    element.appendChild(specialCase ? document.createCDATASection(content) : document
                            .createTextNode(content));
                } else if (item instanceof TagNode) {
                    final TagNode subTagNode = (TagNode) item;
                    final Element subelement = createElement(subTagNode, document);

                    setAttributes(subTagNode, subelement);

                    // recursively create subnodes
                    createSubnodes(document, subelement, subTagNode.getChildren());

                    element.appendChild(subelement);
                } else if (item instanceof List) {
                    final List sublist = (List) item;
                    createSubnodes(document, element, sublist);
                }
            }
        }
    }

}
