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
import java.nio.charset.Charset;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>
 * Common utilities.
 * </p>
 */
final public class Utils {
    final public static int RESERVED_XML_CHARS_SIZE = 128;
    final public static String VAR_START = "${";
    final public static String VAR_END = "}";

    public static final String RESERVED_XML_CHARS[] = new String[RESERVED_XML_CHARS_SIZE];
    public static final char RESERVED_XML_CHARS_LIST[] = { '&', '<', '>', '\"', '\'' };

    static {
        RESERVED_XML_CHARS['&'] = "&amp;";
        RESERVED_XML_CHARS['<'] = "&lt;";
        RESERVED_XML_CHARS['>'] = "&gt;";
        RESERVED_XML_CHARS['\"'] = "&quot;";
        RESERVED_XML_CHARS['\''] = "&apos;";
    }

    /**
     * Trims specified string from left.
     * 
     * @param s
     */
    public static String ltrim(final String s) {
        if (s == null) {
            return null;
        }

        int index = 0;
        final int len = s.length();

        while (index < len && Character.isWhitespace(s.charAt(index))) {
            index++;
        }

        return (index >= len) ? "" : s.substring(index);
    }

    /**
     * Trims specified string from right.
     * 
     * @param s
     */
    public static String rtrim(final String s) {
        if (s == null) {
            return null;
        }

        final int len = s.length();
        int index = len;

        while (index > 0 && Character.isWhitespace(s.charAt(index - 1))) {
            index--;
        }

        return (index <= 0) ? "" : s.substring(0, index);
    }

    public static String getCharsetFromContentTypeString(final String contentType) {
        if (contentType != null) {
            final String pattern = "charset=([a-z\\d\\-]*)";
            final Matcher matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(contentType);
            if (matcher.find()) {
                final String charset = matcher.group(1);
                if (Charset.isSupported(charset)) {
                    return charset;
                }
            }
        }

        return null;
    }

    public static String getCharsetFromContent(final URL url) throws IOException {
        final InputStream stream = url.openStream();
        final byte chunk[] = new byte[2048];
        final int bytesRead = stream.read(chunk);
        if (bytesRead > 0) {
            final String startContent = new String(chunk);
            final String pattern = "\\<meta\\s*http-equiv=[\\\"\\']content-type[\\\"\\']\\s*content\\s*=\\s*[\"']text/html\\s*;\\s*charset=([a-z\\d\\-]*)[\\\"\\'\\>]";
            final Matcher matcher = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE).matcher(startContent);
            if (matcher.find()) {
                final String charset = matcher.group(1);
                if (Charset.isSupported(charset)) {
                    return charset;
                }
            }
        }

        return null;
    }

    public static boolean isHexadecimalDigit(final char ch) {
        return Character.isDigit(ch) || ch == 'A' || ch == 'a' || ch == 'B' || ch == 'b' || ch == 'C' || ch == 'c'
                || ch == 'D' || ch == 'd' || ch == 'E' || ch == 'e' || ch == 'F' || ch == 'f';
    }

    public static boolean isValidXmlChar(final char ch) {
        return ((ch >= 0x20) && (ch <= 0xD7FF)) || (ch == 0x9) || (ch == 0xA) || (ch == 0xD)
                || ((ch >= 0xE000) && (ch <= 0xFFFD)) || ((ch >= 0x10000) && (ch <= 0x10FFFF));
    }

    public static boolean isReservedXmlChar(final char ch) {
        return (ch < RESERVED_XML_CHARS_SIZE && RESERVED_XML_CHARS[ch] != null);
    }

    public static boolean isValidInt(final String s, final int radix) {
        try {
            Integer.parseInt(s, radix);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Escapes XML string.
     * 
     * @param s
     *            String to be escaped
     * @param props
     *            Cleaner properties gover affect escaping behaviour
     * @param isDomCreation
     *            Tells if escaped content will be part of the DOM
     */
    public static String escapeXml(final String s, final CleanerProperties props, final boolean isDomCreation) {
        final boolean advanced = props.isAdvancedXmlEscape();
        final boolean recognizeUnicodeChars = props.isRecognizeUnicodeChars();
        final boolean translateSpecialEntities = props.isTranslateSpecialEntities();

        if (s != null) {
            final int len = s.length();
            final StringBuilder result = new StringBuilder(len);

            for (int i = 0; i < len; i++) {
                final char ch = s.charAt(i);

                if (ch == '&') {
                    if ((advanced || recognizeUnicodeChars) && (i < len - 2) && (s.charAt(i + 1) == '#')) {
                        final boolean isHex = Character.toLowerCase(s.charAt(i + 2)) == 'x';
                        int charIndex = i + (isHex ? 3 : 2);
                        final int radix = isHex ? 16 : 10;
                        String unicode = "";
                        while (charIndex < len) {
                            final char currCh = s.charAt(charIndex);
                            if (currCh == ';') {
                                break;
                            } else if (isValidInt(unicode + currCh, radix)) {
                                unicode += currCh;
                                charIndex++;
                            } else {
                                charIndex--;
                                break;
                            }
                        }

                        if (isValidInt(unicode, radix)) {
                            final char unicodeChar = (char) Integer.parseInt(unicode, radix);
                            if (!isValidXmlChar(unicodeChar)) {
                                i = charIndex;
                            } else if (!isReservedXmlChar(unicodeChar)) {
                                result.append(recognizeUnicodeChars ? String.valueOf(unicodeChar) : "&#" + unicode
                                        + ";");
                                i = charIndex;
                            } else {
                                i = charIndex;
                                result.append("&#" + (isHex ? "x" : "") + unicode + ";");
                            }
                        } else {
                            result.append("&amp;");
                        }
                    } else {
                        if (translateSpecialEntities) {
                            // get minimal following sequence required to
                            // recognize some special entitiy
                            final String seq = s.substring(i,
                                    i + Math.min(SpecialEntity.getMaxEntityLength() + 2, len - i));
                            final int semiIndex = seq.indexOf(';');
                            if (semiIndex > 0) {
                                final String entityKey = seq.substring(1, semiIndex);
                                final SpecialEntity entity = SpecialEntity.getEntity(entityKey);
                                if (entity != null) {
                                    result.append(props.isTransSpecialEntitiesToNCR() ? entity.getDecimalNCR() : entity
                                            .getCharacter());
                                    i += entityKey.length() + 1;
                                    continue;
                                }
                            }
                        }

                        if (advanced) {
                            final String sub = s.substring(i);
                            boolean isReservedSeq = false;
                            for (int j = 0; j < RESERVED_XML_CHARS_LIST.length; j++) {
                                final char currentChar = RESERVED_XML_CHARS_LIST[j];
                                final String seq = RESERVED_XML_CHARS[currentChar];
                                if (sub.startsWith(seq)) {
                                    result.append(isDomCreation ? currentChar : (props.isTransResCharsToNCR() ? "&#"
                                            + (int) currentChar + ";" : seq));
                                    i += seq.length() - 1;
                                    isReservedSeq = true;
                                    break;
                                }
                            }
                            if (!isReservedSeq) {
                                result.append(isDomCreation ? "&" : (props.isTransResCharsToNCR() ? "&#" + (int) '&'
                                        + ";" : RESERVED_XML_CHARS['&']));
                            }
                            continue;
                        }

                        result.append("&amp;");
                    }
                } else if (isReservedXmlChar(ch)) {
                    result.append(props.isTransResCharsToNCR() ? "&#" + (int) ch + ";" : (isDomCreation ? ch
                            : RESERVED_XML_CHARS[ch]));
                } else {
                    result.append(ch);
                }
            }

            return result.toString();
        }

        return null;
    }

    /**
     * Checks whether specified object's string representation is empty string
     * (containing of only whitespaces).
     * 
     * @param object
     *            Object whose string representation is checked
     * @return true, if empty string, false otherwise
     */
    public static boolean isWhitespaceString(final Object object) {
        if (object != null) {
            final String s = object.toString();
            return s != null && "".equals(s.trim());
        }
        return false;
    }

    /**
     * Checks if specified character can be part of xml identifier (tag name of
     * attribute name) and is not standard identifier character.
     * 
     * @param ch
     *            Character to be checked
     * @return True if it can be part of xml identifier
     */
    public static boolean isIdentifierHelperChar(final char ch) {
        return ':' == ch || '.' == ch || '-' == ch || '_' == ch;
    }

    /**
     * Chacks whether specified string can be valid tag name or attribute name
     * in xml.
     * 
     * @param s
     *            String to be checked
     * @return True if string is valid xml identifier, false otherwise
     */
    public static boolean isValidXmlIdentifier(final String s) {
        if (s != null) {
            final int len = s.length();
            if (len == 0) {
                return false;
            }
            for (int i = 0; i < len; i++) {
                final char ch = s.charAt(i);
                if ((i == 0 && !Character.isUnicodeIdentifierStart(ch) && ch != '_')
                        || (!Character.isUnicodeIdentifierStart(ch) && !Character.isDigit(ch) && !Utils
                                .isIdentifierHelperChar(ch))) {
                    return false;
                }
            }
            return true;
        }

        return false;
    }

    /**
     * @param o
     * @return True if specified string is null of contains only whitespace
     *         characters
     */
    public static boolean isEmptyString(final Object o) {
        return o == null || "".equals(o.toString().trim());
    }

    /**
     * Evaluates string template for specified map of variables. Template string
     * can contain dynamic parts in the form of ${VARNAME}. Each such part is
     * replaced with value of the variable if such exists in the map, or with
     * empty string otherwise.
     * 
     * @param template
     *            Template string
     * @param variables
     *            Map of variables (can be null)
     * @return Evaluated string
     */
    public static String evaluateTemplate(final String template, final Map variables) {
        if (template == null) {
            return template;
        }

        final StringBuilder result = new StringBuilder();

        int startIndex = template.indexOf(VAR_START);
        int endIndex = -1;

        while (startIndex >= 0 && startIndex < template.length()) {
            result.append(template.substring(endIndex + 1, startIndex));
            endIndex = template.indexOf(VAR_END, startIndex);

            if (endIndex > startIndex) {
                final String varName = template.substring(startIndex + VAR_START.length(), endIndex);
                final Object resultObj = variables != null ? variables.get(varName.toLowerCase()) : "";
                result.append(resultObj == null ? "" : resultObj.toString());
            }

            startIndex = template.indexOf(VAR_START, Math.max(endIndex + VAR_END.length(), startIndex + 1));
        }

        result.append(template.substring(endIndex + 1));

        return result.toString();
    }

    public static String[] tokenize(final String s, final String delimiters) {
        if (s == null) {
            return new String[] {};
        }

        final StringTokenizer tokenizer = new StringTokenizer(s, delimiters);
        final String result[] = new String[tokenizer.countTokens()];
        int index = 0;
        while (tokenizer.hasMoreTokens()) {
            result[index++] = tokenizer.nextToken();
        }

        return result;
    }

    public static void updateTagTransformations(final CleanerTransformations transformations, final String key,
            final String value) {
        final int index = key.indexOf('.');

        // new tag transformation case (tagname[=destname[,preserveatts]])
        if (index <= 0) {
            String destTag = null;
            boolean preserveSourceAtts = true;
            if (value != null) {
                final String[] tokens = tokenize(value, ",;");
                if (tokens.length > 0) {
                    destTag = tokens[0];
                }
                if (tokens.length > 1) {
                    preserveSourceAtts = "true".equalsIgnoreCase(tokens[1]) || "yes".equalsIgnoreCase(tokens[1])
                            || "1".equals(tokens[1]);
                }
            }
            final TagTransformation newTagTrans = new TagTransformation(key, destTag, preserveSourceAtts);
            transformations.addTransformation(newTagTrans);
        } else { // attribute transformation description
            final String[] parts = tokenize(key, ".");
            final String tagName = parts[0];
            final TagTransformation trans = transformations.getTransformation(tagName);
            if (trans != null) {
                trans.addAttributeTransformation(parts[1], value);
            }
        }
    }

    /**
     * Checks if specified link is full URL.
     * 
     * @param link
     * @return True, if full URl, false otherwise.
     */
    public static boolean isFullUrl(String link) {
        if (link == null) {
            return false;
        }
        link = link.trim().toLowerCase();
        return link.startsWith("http://") || link.startsWith("https://") || link.startsWith("file://");
    }

    /**
     * Calculates full URL for specified page URL and link which could be full,
     * absolute or relative like there can be found in A or IMG tags.
     */
    public static String fullUrl(String pageUrl, final String link) {
        if (isFullUrl(link)) {
            return link;
        } else if (link != null && link.charAt(0) == '?') {
            final int qindex = pageUrl.indexOf('?');
            final int len = pageUrl.length();
            if (qindex < 0) {
                return pageUrl + link;
            } else if (qindex == len - 1) {
                return pageUrl.substring(0, len - 1) + link;
            } else {
                return pageUrl + "&" + link.substring(1);
            }
        }

        final boolean isLinkAbsolute = (link.charAt(0) == '/');

        if (!isFullUrl(pageUrl)) {
            pageUrl = "http://" + pageUrl;
        }

        final int slashIndex = isLinkAbsolute ? pageUrl.indexOf('/', 8) : pageUrl.lastIndexOf('/');
        if (slashIndex <= 8) {
            pageUrl += "/";
        } else {
            pageUrl = pageUrl.substring(0, slashIndex + 1);
        }

        return isLinkAbsolute ? pageUrl + link.substring(1) : pageUrl + link;
    }

    /**
     * @param name
     * @return For xml element name or attribute name returns prefix (part
     *         before :) or null if there is no prefix
     */
    public static String getXmlNSPrefix(final String name) {
        final int colIndex = name.indexOf(':');
        if (colIndex > 0) {
            return name.substring(0, colIndex);
        }

        return null;
    }

    /**
     * @param name
     * @return For xml element name or attribute name returns name after prefix
     *         (part after :)
     */
    public static String getXmlName(final String name) {
        final int colIndex = name.indexOf(':');
        if (colIndex > 0 && colIndex < name.length() - 1) {
            return name.substring(colIndex + 1);
        }

        return name;
    }

}
