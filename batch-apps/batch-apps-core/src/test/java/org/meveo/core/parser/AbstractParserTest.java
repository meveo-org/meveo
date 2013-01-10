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

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Abstract parser tests.
 * 
 * @author Ignas Lelys
 * @created Aug 4, 2010
 *
 */
public class AbstractParserTest {
    
    public static final String DATE_FORMAT_STRING = "yyMMddHHmmss";
    
    @SuppressWarnings("unchecked")
    @Test(groups = {"unit"})
    public void testGetFieldValue() {
        AbstractParser parser = new ParserImpl();
        Assert.assertNull(parser.getFieldValue(""));
        Assert.assertEquals(parser.getFieldValue("", "default"), "default");
        Assert.assertEquals(parser.getFieldValue("test", "default"), "test");
        Assert.assertEquals(parser.getFieldValue("test"), "test");
    }
    
    @SuppressWarnings("unchecked")
    @Test(groups = {"unit"})
    public void testGetDateFieldValue() {
        AbstractParser parser = new ParserImpl();
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT_STRING);
        Calendar date = Calendar.getInstance();
        date.set(2010, Calendar.JANUARY, 1, 0, 0, 0);
        date.set(Calendar.MILLISECOND, 0);
        Assert.assertEquals(parser.getDateFieldValue("100101000000", format, DATE_FORMAT_STRING), date.getTime());
    }
    
    @SuppressWarnings("unchecked")
    @Test(groups = {"unit"})
    public void testGetLongFieldValue() {
        AbstractParser parser = new ParserImpl();
        Assert.assertEquals(parser.getLongFieldValue("1"), (Long)1L);
        Assert.assertNull(parser.getLongFieldValue(""));
    }
    
    @SuppressWarnings("unchecked")
    @Test(groups = {"unit"})
    public void testGetBooleanFieldValue() {
        AbstractParser parser = new ParserImpl();
        Assert.assertFalse(parser.getBooleanFieldValue("0"));
        Assert.assertTrue(parser.getBooleanFieldValue("1"));
        Assert.assertNull(parser.getBooleanFieldValue(""));
    }
    
    @SuppressWarnings("unchecked")
    private class ParserImpl extends AbstractParser {
        
        public Object next() throws ParserException {
            return null;
        }
        
        public void close() {
        }
        
        public void setParsingFile(String fileName) throws ParserException {
        }
    }
}
