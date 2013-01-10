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

import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.config.task.TestConfig;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Ignas Lelys
 * @created Dec 21, 2010
 *
 */
public class AbstractTextParserTest {
    
    @SuppressWarnings("unchecked")
    @Test(groups = {"unit"})
    public void testSetParsingFileAndCloseParser() throws ParserException, IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
        AbstractTextParser parser = new ParserImpl();
        ReflectionUtils.setPrivateField(AbstractTextParser.class, parser, "config", new TestConfig());
        Assert.assertNull(parser.fileName);
        Assert.assertNull(parser.channel);
        Assert.assertNull(parser.scanner);
        parser.setParsingFile("target/test-classes/files/test.csv");
        Assert.assertEquals(parser.fileName, "target/test-classes/files/test.csv");
        Assert.assertNotNull(parser.channel);
        Assert.assertNotNull(parser.scanner);
        parser.close();
        Assert.assertFalse(parser.channel.isOpen());
    }
    
    @SuppressWarnings("unchecked")
    private class ParserImpl extends AbstractTextParser {
        @Override
        public Object next() throws ParserException {
            return null;
        }
    }

}
