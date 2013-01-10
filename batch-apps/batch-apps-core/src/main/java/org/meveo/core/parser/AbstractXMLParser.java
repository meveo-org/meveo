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

import java.io.File;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.meveo.commons.exceptions.ConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Abstract parser for xml files. It parses and stores
 * {@link org.w3c.dom.Document} object with xml file information.
 * 
 * @author Ignas Lelys
 * @created Dec 21, 2010
 * 
 */
public abstract class AbstractXMLParser<T> extends AbstractParser<T> {

    private static final Logger logger = Logger.getLogger(AbstractXMLParser.class);

    protected Document xmlDocument;

    /**
     * @see org.meveo.core.parser.Parser#setParsingFile(java.lang.String)
     */
    @Override
    public void setParsingFile(String fileName) throws ParserException {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            xmlDocument = db.parse(new File(fileName));
            xmlDocument.getDocumentElement().normalize(); // TODO check this out
        } catch (Exception e) {
            logger.error("Error parsing xml file", e);
            throw new ParserException();
        }
    }

    /**
     * @see org.meveo.core.parser.Parser#close()
     */
    @Override
    public void close() {
        // TODO check if closing is needed
        xmlDocument = null;
    }
    
    /**
     * Gets first element from list of node.
     * 
     * @param nodeList {@link NodeList}
     * @return first element if exists, null otherwise
     */
    protected Element getFirstElement(NodeList nodeList) {
        if (nodeList != null && nodeList.getLength() > 0) {
            Node node = nodeList.item(0);
            return (Element) node;
        } else {
            return null;
        }
    }
    
    /**
     * Converts node to full xml string (with all sub elements etc).
     * 
     * @param node Node to convert to string.
     * 
     * @return XML string
     * @throws TransformerException 
     */
    protected String getNodeXmlString(Node node) {
        try {
            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transformer = transFactory.newTransformer();
            StringWriter buffer = new StringWriter();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(new DOMSource(node),
                  new StreamResult(buffer));
            return buffer.toString();
        } catch (Exception e) {
            logger.error("Error converting xml node to its string representation", e);
            throw new ConfigurationException();
        }
    }

    /**
     * @see org.meveo.core.parser.Parser#next()
     */
    @Override
    public abstract T next() throws ParserException;
}
