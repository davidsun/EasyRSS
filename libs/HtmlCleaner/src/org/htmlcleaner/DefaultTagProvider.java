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

/**
 * This class is automatically created from ConfigFileTagProvider which reads
 * default XML configuration file with tag descriptions. It is used as default
 * tag info provider. Class is created for performance purposes - parsing XML
 * file requires some processing time.
 */
public class DefaultTagProvider extends HashMap<String, TagInfo> implements ITagInfoProvider {
    private static DefaultTagProvider instance;
    private static final long serialVersionUID = 1L;

    /**
     * @return Singleton instance of this class.
     */
    public static synchronized DefaultTagProvider getInstance() {
        if (instance == null) {
            instance = new DefaultTagProvider();
        }
        return instance;
    }

    protected DefaultTagProvider() {
        super();

        TagInfo tagInfo;

        tagInfo = new TagInfo("div", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("div", tagInfo);

        tagInfo = new TagInfo("span", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        this.put("span", tagInfo);

        tagInfo = new TagInfo("meta", TagInfo.CONTENT_NONE, TagInfo.HEAD, false, false, false);
        this.put("meta", tagInfo);

        tagInfo = new TagInfo("link", TagInfo.CONTENT_NONE, TagInfo.HEAD, false, false, false);
        this.put("link", tagInfo);

        tagInfo = new TagInfo("title", TagInfo.CONTENT_TEXT, TagInfo.HEAD, false, true, false);
        this.put("title", tagInfo);

        tagInfo = new TagInfo("style", TagInfo.CONTENT_TEXT, TagInfo.HEAD, false, false, false);
        this.put("style", tagInfo);

        tagInfo = new TagInfo("bgsound", TagInfo.CONTENT_NONE, TagInfo.HEAD, false, false, false);
        this.put("bgsound", tagInfo);

        tagInfo = new TagInfo("h1", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("h1,h2,h3,h4,h5,h6,p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("h1", tagInfo);

        tagInfo = new TagInfo("h2", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("h1,h2,h3,h4,h5,h6,p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("h2", tagInfo);

        tagInfo = new TagInfo("h3", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("h1,h2,h3,h4,h5,h6,p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("h3", tagInfo);

        tagInfo = new TagInfo("h4", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("h1,h2,h3,h4,h5,h6,p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("h4", tagInfo);

        tagInfo = new TagInfo("h5", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("h1,h2,h3,h4,h5,h6,p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("h5", tagInfo);

        tagInfo = new TagInfo("h6", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("h1,h2,h3,h4,h5,h6,p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("h6", tagInfo);

        tagInfo = new TagInfo("p", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("p", tagInfo);

        tagInfo = new TagInfo("strong", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        this.put("strong", tagInfo);

        tagInfo = new TagInfo("em", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        this.put("em", tagInfo);

        tagInfo = new TagInfo("abbr", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        this.put("abbr", tagInfo);

        tagInfo = new TagInfo("acronym", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        this.put("acronym", tagInfo);

        tagInfo = new TagInfo("address", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("address", tagInfo);

        tagInfo = new TagInfo("bdo", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        this.put("bdo", tagInfo);

        tagInfo = new TagInfo("blockquote", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("blockquote", tagInfo);

        tagInfo = new TagInfo("cite", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        this.put("cite", tagInfo);

        tagInfo = new TagInfo("q", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        this.put("q", tagInfo);

        tagInfo = new TagInfo("code", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        this.put("code", tagInfo);

        tagInfo = new TagInfo("ins", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        this.put("ins", tagInfo);

        tagInfo = new TagInfo("del", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        this.put("del", tagInfo);

        tagInfo = new TagInfo("dfn", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        this.put("dfn", tagInfo);

        tagInfo = new TagInfo("kbd", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        this.put("kbd", tagInfo);

        tagInfo = new TagInfo("pre", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("pre", tagInfo);

        tagInfo = new TagInfo("samp", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        this.put("samp", tagInfo);

        tagInfo = new TagInfo("listing", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("listing", tagInfo);

        tagInfo = new TagInfo("var", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        this.put("var", tagInfo);

        tagInfo = new TagInfo("br", TagInfo.CONTENT_NONE, TagInfo.BODY, false, false, false);
        this.put("br", tagInfo);

        tagInfo = new TagInfo("wbr", TagInfo.CONTENT_NONE, TagInfo.BODY, false, false, false);
        this.put("wbr", tagInfo);

        tagInfo = new TagInfo("nobr", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeTags("nobr");
        this.put("nobr", tagInfo);

        tagInfo = new TagInfo("xmp", TagInfo.CONTENT_TEXT, TagInfo.BODY, false, false, false);
        this.put("xmp", tagInfo);

        tagInfo = new TagInfo("a", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeTags("a");
        this.put("a", tagInfo);

        tagInfo = new TagInfo("base", TagInfo.CONTENT_NONE, TagInfo.HEAD, false, false, false);
        this.put("base", tagInfo);

        tagInfo = new TagInfo("img", TagInfo.CONTENT_NONE, TagInfo.BODY, false, false, false);
        this.put("img", tagInfo);

        tagInfo = new TagInfo("area", TagInfo.CONTENT_NONE, TagInfo.BODY, false, false, false);
        tagInfo.defineFatalTags("map");
        tagInfo.defineCloseBeforeTags("area");
        this.put("area", tagInfo);

        tagInfo = new TagInfo("map", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeTags("map");
        this.put("map", tagInfo);

        tagInfo = new TagInfo("object", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        this.put("object", tagInfo);

        tagInfo = new TagInfo("param", TagInfo.CONTENT_NONE, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("param", tagInfo);

        tagInfo = new TagInfo("applet", TagInfo.CONTENT_ALL, TagInfo.BODY, true, false, false);
        this.put("applet", tagInfo);

        tagInfo = new TagInfo("xml", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        this.put("xml", tagInfo);

        tagInfo = new TagInfo("ul", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("ul", tagInfo);

        tagInfo = new TagInfo("ol", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("ol", tagInfo);

        tagInfo = new TagInfo("li", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("li,p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("li", tagInfo);

        tagInfo = new TagInfo("dl", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("dl", tagInfo);

        tagInfo = new TagInfo("dt", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeTags("dt,dd");
        this.put("dt", tagInfo);

        tagInfo = new TagInfo("dd", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeTags("dt,dd");
        this.put("dd", tagInfo);

        tagInfo = new TagInfo("menu", TagInfo.CONTENT_ALL, TagInfo.BODY, true, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("menu", tagInfo);

        tagInfo = new TagInfo("dir", TagInfo.CONTENT_ALL, TagInfo.BODY, true, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("dir", tagInfo);

        tagInfo = new TagInfo("table", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineAllowedChildrenTags("tr,tbody,thead,tfoot,colgroup,col,form,caption,tr");
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("tr,thead,tbody,tfoot,caption,colgroup,table,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param");
        this.put("table", tagInfo);

        tagInfo = new TagInfo("tr", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineFatalTags("table");
        tagInfo.defineRequiredEnclosingTags("tbody");
        tagInfo.defineAllowedChildrenTags("td,th");
        tagInfo.defineHigherLevelTags("thead,tfoot");
        tagInfo.defineCloseBeforeTags("tr,td,th,caption,colgroup");
        this.put("tr", tagInfo);

        tagInfo = new TagInfo("td", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineFatalTags("table");
        tagInfo.defineRequiredEnclosingTags("tr");
        tagInfo.defineCloseBeforeTags("td,th,caption,colgroup");
        this.put("td", tagInfo);

        tagInfo = new TagInfo("th", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineFatalTags("table");
        tagInfo.defineRequiredEnclosingTags("tr");
        tagInfo.defineCloseBeforeTags("td,th,caption,colgroup");
        this.put("th", tagInfo);

        tagInfo = new TagInfo("tbody", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineFatalTags("table");
        tagInfo.defineAllowedChildrenTags("tr,form");
        tagInfo.defineCloseBeforeTags("td,th,tr,tbody,thead,tfoot,caption,colgroup");
        this.put("tbody", tagInfo);

        tagInfo = new TagInfo("thead", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineFatalTags("table");
        tagInfo.defineAllowedChildrenTags("tr,form");
        tagInfo.defineCloseBeforeTags("td,th,tr,tbody,thead,tfoot,caption,colgroup");
        this.put("thead", tagInfo);

        tagInfo = new TagInfo("tfoot", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineFatalTags("table");
        tagInfo.defineAllowedChildrenTags("tr,form");
        tagInfo.defineCloseBeforeTags("td,th,tr,tbody,thead,tfoot,caption,colgroup");
        this.put("tfoot", tagInfo);

        tagInfo = new TagInfo("col", TagInfo.CONTENT_NONE, TagInfo.BODY, false, false, false);
        tagInfo.defineFatalTags("table");
        this.put("col", tagInfo);

        tagInfo = new TagInfo("colgroup", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineFatalTags("table");
        tagInfo.defineAllowedChildrenTags("col");
        tagInfo.defineCloseBeforeTags("td,th,tr,tbody,thead,tfoot,caption,colgroup");
        this.put("colgroup", tagInfo);

        tagInfo = new TagInfo("caption", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineFatalTags("table");
        tagInfo.defineCloseBeforeTags("td,th,tr,tbody,thead,tfoot,caption,colgroup");
        this.put("caption", tagInfo);

        tagInfo = new TagInfo("form", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, true);
        tagInfo.defineForbiddenTags("form");
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("option,optgroup,textarea,select,fieldset,p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("form", tagInfo);

        tagInfo = new TagInfo("input", TagInfo.CONTENT_NONE, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeTags("select,optgroup,option");
        this.put("input", tagInfo);

        tagInfo = new TagInfo("textarea", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeTags("select,optgroup,option");
        this.put("textarea", tagInfo);

        tagInfo = new TagInfo("select", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, true);
        tagInfo.defineAllowedChildrenTags("option,optgroup");
        tagInfo.defineCloseBeforeTags("option,optgroup,select");
        this.put("select", tagInfo);

        tagInfo = new TagInfo("option", TagInfo.CONTENT_TEXT, TagInfo.BODY, false, false, true);
        tagInfo.defineFatalTags("select");
        tagInfo.defineCloseBeforeTags("option");
        this.put("option", tagInfo);

        tagInfo = new TagInfo("optgroup", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, true);
        tagInfo.defineFatalTags("select");
        tagInfo.defineAllowedChildrenTags("option");
        tagInfo.defineCloseBeforeTags("optgroup");
        this.put("optgroup", tagInfo);

        tagInfo = new TagInfo("button", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeTags("select,optgroup,option");
        this.put("button", tagInfo);

        tagInfo = new TagInfo("label", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        this.put("label", tagInfo);

        tagInfo = new TagInfo("fieldset", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("fieldset", tagInfo);

        tagInfo = new TagInfo("legend", TagInfo.CONTENT_TEXT, TagInfo.BODY, false, false, false);
        tagInfo.defineRequiredEnclosingTags("fieldset");
        tagInfo.defineCloseBeforeTags("legend");
        this.put("legend", tagInfo);

        tagInfo = new TagInfo("isindex", TagInfo.CONTENT_NONE, TagInfo.BODY, true, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("isindex", tagInfo);

        tagInfo = new TagInfo("script", TagInfo.CONTENT_ALL, TagInfo.HEAD_AND_BODY, false, false, false);
        this.put("script", tagInfo);

        tagInfo = new TagInfo("noscript", TagInfo.CONTENT_ALL, TagInfo.HEAD_AND_BODY, false, false, false);
        this.put("noscript", tagInfo);

        tagInfo = new TagInfo("b", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseInsideCopyAfterTags("u,i,tt,sub,sup,big,small,strike,blink,s");
        this.put("b", tagInfo);

        tagInfo = new TagInfo("i", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseInsideCopyAfterTags("b,u,tt,sub,sup,big,small,strike,blink,s");
        this.put("i", tagInfo);

        tagInfo = new TagInfo("u", TagInfo.CONTENT_ALL, TagInfo.BODY, true, false, false);
        tagInfo.defineCloseInsideCopyAfterTags("b,i,tt,sub,sup,big,small,strike,blink,s");
        this.put("u", tagInfo);

        tagInfo = new TagInfo("tt", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseInsideCopyAfterTags("b,u,i,sub,sup,big,small,strike,blink,s");
        this.put("tt", tagInfo);

        tagInfo = new TagInfo("sub", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseInsideCopyAfterTags("b,u,i,tt,sup,big,small,strike,blink,s");
        this.put("sub", tagInfo);

        tagInfo = new TagInfo("sup", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseInsideCopyAfterTags("b,u,i,tt,sub,big,small,strike,blink,s");
        this.put("sup", tagInfo);

        tagInfo = new TagInfo("big", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseInsideCopyAfterTags("b,u,i,tt,sub,sup,small,strike,blink,s");
        this.put("big", tagInfo);

        tagInfo = new TagInfo("small", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseInsideCopyAfterTags("b,u,i,tt,sub,sup,big,strike,blink,s");
        this.put("small", tagInfo);

        tagInfo = new TagInfo("strike", TagInfo.CONTENT_ALL, TagInfo.BODY, true, false, false);
        tagInfo.defineCloseInsideCopyAfterTags("b,u,i,tt,sub,sup,big,small,blink,s");
        this.put("strike", tagInfo);

        tagInfo = new TagInfo("blink", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseInsideCopyAfterTags("b,u,i,tt,sub,sup,big,small,strike,s");
        this.put("blink", tagInfo);

        tagInfo = new TagInfo("marquee", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("marquee", tagInfo);

        tagInfo = new TagInfo("s", TagInfo.CONTENT_ALL, TagInfo.BODY, true, false, false);
        tagInfo.defineCloseInsideCopyAfterTags("b,u,i,tt,sub,sup,big,small,strike,blink");
        this.put("s", tagInfo);

        tagInfo = new TagInfo("hr", TagInfo.CONTENT_NONE, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("hr", tagInfo);

        tagInfo = new TagInfo("font", TagInfo.CONTENT_ALL, TagInfo.BODY, true, false, false);
        this.put("font", tagInfo);

        tagInfo = new TagInfo("basefont", TagInfo.CONTENT_NONE, TagInfo.BODY, true, false, false);
        this.put("basefont", tagInfo);

        tagInfo = new TagInfo("center", TagInfo.CONTENT_ALL, TagInfo.BODY, true, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("center", tagInfo);

        tagInfo = new TagInfo("comment", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        this.put("comment", tagInfo);

        tagInfo = new TagInfo("server", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        this.put("server", tagInfo);

        tagInfo = new TagInfo("iframe", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        this.put("iframe", tagInfo);

        tagInfo = new TagInfo("embed", TagInfo.CONTENT_NONE, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("embed", tagInfo);
    }

    /**
     * Sets new tag info.
     * 
     * @param tagInfo
     *            tag info to be added to the provider.
     */
    public void addTagInfo(TagInfo tagInfo) {
        if (tagInfo != null) {
            put(tagInfo.getName().toLowerCase(), tagInfo);
        }
    }

    public TagInfo getTagInfo(String tagName) {
        return get(tagName);
    }

    /**
     * Removes tag info with specified name.
     * 
     * @param tagName
     *            Name of the tag to be removed from the tag provider.
     */
    public void removeTagInfo(String tagName) {
        if (tagName != null) {
            remove(tagName.toLowerCase());
        }
    }
}
