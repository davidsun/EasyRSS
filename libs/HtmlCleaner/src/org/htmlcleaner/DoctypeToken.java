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

import java.io.IOException;
import java.io.Writer;

/**
 * <p>
 * HTML doctype token.
 * </p>
 */
public class DoctypeToken implements BaseToken {
    private static String clean(String s) {
        if (s != null) {
            s = s.replace('>', ' ');
            s = s.replace('<', ' ');
            s = s.replace('&', ' ');
            s = s.replace('\'', ' ');
            s = s.replace('\"', ' ');
        }

        return s;
    }

    final private String part1;
    final private String part2;
    final private String part3;
    final private String part4;

    public DoctypeToken(final String part1, final String part2, final String part3, final String part4) {
        this.part1 = part1 != null ? part1.toUpperCase() : part1;
        this.part2 = part2 != null ? part2.toUpperCase() : part2;
        this.part3 = clean(part3);
        this.part4 = clean(part4);
    }

    public String getContent() {
        String result = "<!DOCTYPE " + part1 + " ";
        result += part2 + " \"" + part3 + "\"";
        if (part4 != null && !"".equals(part4)) {
            result += " \"" + part4 + "\"";
        }
        result += ">";
        return result;
    }

    public String getName() {
        return "";
    }

    public String getPart1() {
        return part1;
    }

    public String getPart2() {
        return part2;
    }

    public String getPart3() {
        return part3;
    }

    public String getPart4() {
        return part4;
    }

    public boolean isValid() {
        if (part1 == null || "".equals(part1)) {
            return false;
        }

        if (!"public".equalsIgnoreCase(part2) && !"system".equalsIgnoreCase(part2)) {
            return false;
        }

        if ("system".equalsIgnoreCase(part2) && part4 != null && !"".equals(part4)) {
            return false;
        }

        if ("public".equalsIgnoreCase(part2) && (part4 == null || "".equals(part4))) {
            return false;
        }

        return true;
    }

    public void serialize(final Serializer serializer, final Writer writer) throws IOException {
        writer.write(getContent() + "\n");
    }

    public String toString() {
        return getContent();
    }
}
