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

public class Html5TagProvider extends DefaultTagProvider {
    private static Html5TagProvider instance;
    private static final long serialVersionUID = 1L;

    /**
     * @return Singleton instance of this class.
     */
    public static synchronized Html5TagProvider getInstance() {
        if (instance == null) {
            instance = new Html5TagProvider();
        }
        return instance;
    }

    protected Html5TagProvider() {
        super();

        TagInfo tagInfo;

        tagInfo = new TagInfo("time", TagInfo.CONTENT_TEXT, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("time", tagInfo);

        tagInfo = new TagInfo("article", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("article", tagInfo);

        tagInfo = new TagInfo("section", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("section", tagInfo);

        tagInfo = new TagInfo("header", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("header", tagInfo);

        tagInfo = new TagInfo("footer", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("footer", tagInfo);

        tagInfo = new TagInfo("aside", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeCopyInsideTags("a,bdo,strong,em,q,b,i,u,tt,sub,sup,big,small,strike,s,font");
        tagInfo.defineCloseBeforeTags("p,address,label,abbr,acronym,dfn,kbd,samp,var,cite,code,param,xml");
        this.put("aside", tagInfo);

        tagInfo = new TagInfo("video", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeTags("object");
        this.put("video", tagInfo);

        tagInfo = new TagInfo("audio", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeTags("object");
        this.put("audio", tagInfo);

        tagInfo = new TagInfo("source", TagInfo.CONTENT_ALL, TagInfo.BODY, false, false, false);
        tagInfo.defineCloseBeforeTags("source");
        this.put("source", tagInfo);
    }
}
