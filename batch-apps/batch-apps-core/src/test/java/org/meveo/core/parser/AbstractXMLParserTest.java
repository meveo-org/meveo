/*
* (C) Copyright 2009-2013 Manaty SARL (http://manaty.net/) and contributors.
*
* Licensed under the GNU Public Licence, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.gnu.org/licenses/gpl-2.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.meveo.core.parser;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Ignas Lelys
 * @created Dec 21, 2010
 *
 */
public class AbstractXMLParserTest {
    
    @SuppressWarnings("unchecked")
    @Test(groups = {"unit"})
    public void testSetParsingFileAndCloseParser() throws ParserException, IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
        AbstractXMLParser parser = new ParserImpl();
        
        Assert.assertNull(parser.xmlDocument);
        parser.setParsingFile("target/test-classes/files/test.xml");
        Assert.assertNotNull(parser.xmlDocument);
        parser.close();
        Assert.assertNull(parser.xmlDocument);
    }
    
    @SuppressWarnings("unchecked")
    private class ParserImpl extends AbstractXMLParser {
        @Override
        public Object next() throws ParserException {
            return null;
        }
    }

}
