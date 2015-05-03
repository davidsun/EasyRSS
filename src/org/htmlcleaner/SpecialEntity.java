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

import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * This class contains map with special entities used in HTML and their
 * unicodes.
 * </p>
 */
final public class SpecialEntity {

    private static Map<String, SpecialEntity> entities = new HashMap<String, SpecialEntity>();

    private static int maxEntityLength = 0;

    static {
        addEntity("nbsp", 160);
        addEntity("iexcl", 161);
        addEntity("cent", 162);
        addEntity("pound", 163);
        addEntity("curren", 164);
        addEntity("yen", 165);
        addEntity("brvbar", 166);
        addEntity("sect", 167);
        addEntity("uml", 168);
        addEntity("copy", 169);
        addEntity("ordf", 170);
        addEntity("laquo", 171);
        addEntity("not", 172);
        addEntity("shy", 173);
        addEntity("reg", 174);
        addEntity("macr", 175);
        addEntity("deg", 176);
        addEntity("plusmn", 177);
        addEntity("sup2", 178);
        addEntity("sup3", 179);
        addEntity("acute", 180);
        addEntity("micro", 181);
        addEntity("para", 182);
        addEntity("middot", 183);
        addEntity("cedil", 184);
        addEntity("sup1", 185);
        addEntity("ordm", 186);
        addEntity("raquo", 187);
        addEntity("frac14", 188);
        addEntity("frac12", 189);
        addEntity("frac34", 190);
        addEntity("iquest", 191);
        addEntity("Agrave", 192);
        addEntity("Aacute", 193);
        addEntity("Acirc", 194);
        addEntity("Atilde", 195);
        addEntity("Auml", 196);
        addEntity("Aring", 197);
        addEntity("AElig", 198);
        addEntity("Ccedil", 199);
        addEntity("Egrave", 200);
        addEntity("Eacute", 201);
        addEntity("Ecirc", 202);
        addEntity("Euml", 203);
        addEntity("Igrave", 204);
        addEntity("Iacute", 205);
        addEntity("Icirc", 206);
        addEntity("Iuml", 207);
        addEntity("ETH", 208);
        addEntity("Ntilde", 209);
        addEntity("Ograve", 210);
        addEntity("Oacute", 211);
        addEntity("Ocirc", 212);
        addEntity("Otilde", 213);
        addEntity("Ouml", 214);
        addEntity("times", 215);
        addEntity("Oslash", 216);
        addEntity("Ugrave", 217);
        addEntity("Uacute", 218);
        addEntity("Ucirc", 219);
        addEntity("Uuml", 220);
        addEntity("Yacute", 221);
        addEntity("THORN", 222);
        addEntity("szlig", 223);
        addEntity("agrave", 224);
        addEntity("aacute", 225);
        addEntity("acirc", 226);
        addEntity("atilde", 227);
        addEntity("auml", 228);
        addEntity("aring", 229);
        addEntity("aelig", 230);
        addEntity("ccedil", 231);
        addEntity("egrave", 232);
        addEntity("eacute", 233);
        addEntity("ecirc", 234);
        addEntity("euml", 235);
        addEntity("igrave", 236);
        addEntity("iacute", 237);
        addEntity("icirc", 238);
        addEntity("iuml", 239);
        addEntity("eth", 240);
        addEntity("ntilde", 241);
        addEntity("ograve", 242);
        addEntity("oacute", 243);
        addEntity("ocirc", 244);
        addEntity("otilde", 245);
        addEntity("ouml", 246);
        addEntity("divide", 247);
        addEntity("oslash", 248);
        addEntity("ugrave", 249);
        addEntity("uacute", 250);
        addEntity("ucirc", 251);
        addEntity("uuml", 252);
        addEntity("yacute", 253);
        addEntity("thorn", 254);
        addEntity("yuml", 255);
        addEntity("OElig", 338);
        addEntity("oelig", 339);
        addEntity("Scaron", 352);
        addEntity("scaron", 353);
        addEntity("Yuml", 376);
        addEntity("fnof", 402);
        addEntity("circ", 710);
        addEntity("tilde", 732);

        // Greek letters
        addEntity("Alpha", 913);
        addEntity("Beta", 914);
        addEntity("Gamma", 915);
        addEntity("Delta", 916);
        addEntity("Epsilon", 917);
        addEntity("Zeta", 918);
        addEntity("Eta", 919);
        addEntity("Theta", 920);
        addEntity("Iota", 921);
        addEntity("Kappa", 922);
        addEntity("Lambda", 923);
        addEntity("Mu", 924);
        addEntity("Nu", 925);
        addEntity("Xi", 926);
        addEntity("Omicron", 927);
        addEntity("Pi", 928);
        addEntity("Rho", 929);
        addEntity("Sigma", 931);
        addEntity("Tau", 932);
        addEntity("Upsilon", 933);
        addEntity("Phi", 934);
        addEntity("Chi", 935);
        addEntity("Psi", 936);
        addEntity("Omega", 937);
        addEntity("alpha", 945);
        addEntity("beta", 946);
        addEntity("gamma", 947);
        addEntity("delta", 948);
        addEntity("epsilon", 949);
        addEntity("zeta", 950);
        addEntity("eta", 951);
        addEntity("theta", 952);
        addEntity("iota", 953);
        addEntity("kappa", 954);
        addEntity("lambda", 955);
        addEntity("mu", 956);
        addEntity("nu", 957);
        addEntity("xi", 958);
        addEntity("omicron", 959);
        addEntity("pi", 960);
        addEntity("rho", 961);
        addEntity("sigmaf", 962);
        addEntity("sigma", 963);
        addEntity("tau", 964);
        addEntity("upsilon", 965);
        addEntity("phi", 966);
        addEntity("chi", 967);
        addEntity("psi", 968);
        addEntity("omega", 969);
        addEntity("thetasym", 977);
        addEntity("upsih", 978);
        addEntity("piv", 982);

        addEntity("ensp", 8194);
        addEntity("emsp", 8195);
        addEntity("thinsp", 8201);
        addEntity("zwnj", 8204);
        addEntity("zwj", 8205);
        addEntity("lrm", 8206);
        addEntity("rlm", 8207);
        addEntity("ndash", 8211);
        addEntity("mdash", 8212);
        addEntity("lsquo", 8216);
        addEntity("rsquo", 8217);
        addEntity("sbquo", 8218);
        addEntity("ldquo", 8220);
        addEntity("rdquo", 8221);
        addEntity("bdquo", 8222);
        addEntity("dagger", 8224);
        addEntity("Dagger", 8225);
        addEntity("bull", 8226);

        addEntity("hellip", 8230);
        addEntity("permil", 8240);
        addEntity("prime", 8242);
        addEntity("Prime", 8243);
        addEntity("lsaquo", 8249);
        addEntity("rsaquo", 8250);
        addEntity("oline", 8254);
        addEntity("frasl", 8260);
        addEntity("euro", 8364);
        addEntity("image", 8465);
        addEntity("weierp", 8472);
        addEntity("real", 8476);
        addEntity("trade", 8482);
        addEntity("alefsym", 8501);
        addEntity("larr", 8592);
        addEntity("uarr", 8593);
        addEntity("rarr", 8594);
        addEntity("darr", 8595);
        addEntity("harr", 8596);
        addEntity("crarr", 8629);
        addEntity("lArr", 8656);
        addEntity("uArr", 8657);
        addEntity("rArr", 8658);
        addEntity("dArr", 8659);
        addEntity("hArr", 8660);

        // math symbols
        addEntity("forall", 8704);
        addEntity("part", 8706);
        addEntity("exist", 8707);
        addEntity("empty", 8709);
        addEntity("nabla", 8711);
        addEntity("isin", 8712);
        addEntity("notin", 8713);
        addEntity("ni", 8715);
        addEntity("prod", 8719);
        addEntity("sum", 8721);
        addEntity("minus", 8722);
        addEntity("lowast", 8727);
        addEntity("radic", 8730);
        addEntity("prop", 8733);
        addEntity("infin", 8734);
        addEntity("ang", 8736);
        addEntity("and", 8743);
        addEntity("or", 8744);
        addEntity("cap", 8745);
        addEntity("cup", 8746);
        addEntity("int", 8747);
        addEntity("there4", 8756);
        addEntity("sim", 8764);
        addEntity("cong", 8773);
        addEntity("asymp", 8776);
        addEntity("ne", 8800);
        addEntity("equiv", 8801);
        addEntity("le", 8804);
        addEntity("ge", 8805);
        addEntity("sub", 8834);
        addEntity("sup", 8835);
        addEntity("nsub", 8836);
        addEntity("sube", 8838);
        addEntity("supe", 8839);
        addEntity("oplus", 8853);
        addEntity("otimes", 8855);
        addEntity("perp", 8869);
        addEntity("sdot", 8901);
        addEntity("lceil", 8968);
        addEntity("rceil", 8969);
        addEntity("lfloor", 8970);
        addEntity("rfloor", 8971);
        addEntity("lang", 9001);
        addEntity("rang", 9002);
        addEntity("loz", 9674);
        addEntity("spades", 9824);
        addEntity("clubs", 9827);
        addEntity("hearts", 9829);
        addEntity("diams", 9830);
    }

    /**
     * Add new entity to the set.
     * 
     * @param entityName
     *            Entity name, for example "pound"
     * @param intCode
     *            Unicode of the entity, for example 163
     * 
     * @throws org.htmlcleaner.HtmlCleanerException
     */
    public static void addEntity(final String entityName, final int intCode) throws HtmlCleanerException {
        if (entities.containsKey(entityName)) {
            throw new HtmlCleanerException("Entity \"" + entityName + "\" is already defined!");
        }
        entities.put(entityName, new SpecialEntity(entityName, intCode));
        final int entityNameLen = entityName.length();
        if (entityNameLen > maxEntityLength) {
            maxEntityLength = entityNameLen;
        }
    }

    public static SpecialEntity getEntity(final String key) {
        return entities.get(key);
    }

    public static int getMaxEntityLength() {
        return maxEntityLength;
    }

    final private String key;
    final private int intCode;

    private SpecialEntity(final String key, final int intCode) {
        this.key = key;
        this.intCode = intCode;
    }

    public char getCharacter() {
        return (char) intCode;
    }

    /**
     * @return Numeric Character Reference in decimal format
     */
    public String getDecimalNCR() {
        return "&#" + intCode + ";";
    }

    /**
     * @return Escaped value of the entity
     */
    public String getEscapedValue() {
        return "&" + key + ";";
    }

    /**
     * @return Numeric Character Reference in hex format
     */
    public String getHexNCR() {
        return "&#x" + Integer.toHexString(intCode) + ";";
    }

    public int getIntCode() {
        return intCode;
    }

    public String getKey() {
        return key;
    }

}
