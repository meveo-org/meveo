/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */



package com.blackbear.flatworm;

import org.apache.commons.lang.StringUtils;
import java.nio.charset.Charset;
import org.w3c.dom.NamedNodeMap;
import java.util.ArrayList;
import java.util.List;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.xml.sax.InputSource;
import javax.xml.parsers.DocumentBuilderFactory;
import com.blackbear.flatworm.errors.FlatwormConfigurationValueException;
import com.blackbear.flatworm.errors.FlatwormUnsetFieldValueException;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileInputStream;

public class ConfigurationReader
{
    public FileFormat loadConfigurationFile(final String xmlFile) throws FlatwormUnsetFieldValueException, FlatwormConfigurationValueException {
        InputStream in = null;
        try {
            in = this.getClass().getClassLoader().getResourceAsStream(xmlFile);
            if (in == null) {
                in = new FileInputStream(xmlFile);
            }
            return this.loadConfigurationFile(in);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if (in != null) {
                try {
                    in.close();
                }
                catch (IOException ex) {}
            }
        }
        return null;
    }
    
    public FileFormat loadConfigurationFile(final InputStream in) throws FlatwormUnsetFieldValueException, FlatwormConfigurationValueException {
        try {
            final DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
            final DocumentBuilder parser = fact.newDocumentBuilder();
            final Document document = parser.parse(new InputSource(in));
            final NodeList children = document.getChildNodes();
            for (int i = 0; i < children.getLength(); ++i) {
                final Node child = children.item(i);
                if ("file-format".equals(child.getNodeName()) && child.getNodeType() == 1) {
                    return (FileFormat)this.traverse(child);
                }
            }
        }
        catch (SAXException e) {
            e.printStackTrace();
        }
        catch (IOException e2) {
            e2.printStackTrace();
        }
        catch (ParserConfigurationException e3) {
            e3.printStackTrace();
        }
        return null;
    }
    
    private List<Object> getChildNodes(final Node node) throws FlatwormUnsetFieldValueException, FlatwormConfigurationValueException {
        final List<Object> nodes = new ArrayList<Object>();
        final NodeList children = node.getChildNodes();
        if (children != null) {
            for (int i = 0; i < children.getLength(); ++i) {
                final Node child = children.item(i);
                final Object o = this.traverse(child);
                if (o != null) {
                    nodes.add(o);
                }
            }
        }
        return nodes;
    }
    
    private boolean isElementNodeOfType(final String type, final Node node) {
        return type.equals(node.getNodeName()) && node.getNodeType() == 1;
    }
    
    private Node getChildElementNodeOfType(final String type, final Node node) {
        final NodeList children = node.getChildNodes();
        if (children != null) {
            for (int i = 0; i < children.getLength(); ++i) {
                final Node child = children.item(i);
                if (type.equals(child.getNodeName()) && child.getNodeType() == 1) {
                    return child;
                }
            }
        }
        return null;
    }
    
    private List<Node> getChildElementNodesOfType(final String type, final Node node) {
        final List<Node> nodes = new ArrayList<Node>();
        final NodeList children = node.getChildNodes();
        if (children != null) {
            for (int i = 0; i < children.getLength(); ++i) {
                final Node child = children.item(i);
                if (type.equals(child.getNodeName()) && child.getNodeType() == 1) {
                    nodes.add(child);
                }
            }
        }
        return nodes;
    }
    
    private String getChildTextNodeValue(final Node node) {
        final NodeList children = node.getChildNodes();
        if (children != null) {
            for (int i = 0; i < children.getLength(); ++i) {
                final Node child = children.item(i);
                if (child.getNodeType() == 3) {
                    return child.getNodeValue();
                }
            }
        }
        return null;
    }
    
    private boolean hasAttributeValueNamed(final Node node, final String name) {
        return node.getAttributes().getNamedItem(name) != null;
    }
    
    private String getAttributeValueNamed(final Node node, final String name) {
        return this.hasAttributeValueNamed(node, name) ? node.getAttributes().getNamedItem(name).getNodeValue() : null;
    }
    
    private Node getAttributeNamed(final Node node, final String name) {
        final NamedNodeMap map = node.getAttributes();
        return map.getNamedItem(name);
    }
    
    private Object traverse(final Node node) throws FlatwormUnsetFieldValueException, FlatwormConfigurationValueException {
        final int type = node.getNodeType();
        if (type == 1) {
            final String nodeName = node.getNodeName();
            if (nodeName.equals("file-format")) {
                final FileFormat f = new FileFormat();
                String encoding = Charset.defaultCharset().name();
                if (this.hasAttributeValueNamed(node, "encoding")) {
                    encoding = this.getAttributeValueNamed(node, "encoding");
                }
                f.setEncoding(encoding);
                final List<Object> children = this.getChildNodes(node);
                for (int i = 0; i < children.size(); ++i) {
                    if (children.get(i).getClass().equals(Converter.class)) {
                        f.addConverter((Converter) children.get(i));
                    }
                    if (children.get(i).getClass().equals(Record.class)) {
                        f.addRecord((Record) children.get(i));
                    }
                }
                return f;
            }
            if (nodeName.equals("converter")) {
                final Converter c = new Converter();
                c.setConverterClass(this.getAttributeValueNamed(node, "class"));
                c.setMethod(this.getAttributeValueNamed(node, "method"));
                c.setReturnType(this.getAttributeValueNamed(node, "return-type"));
                c.setName(this.getAttributeValueNamed(node, "name"));
                return c;
            }
            if (nodeName.equals("record")) {
                final Record r = new Record();
                r.setName(this.getAttributeValueNamed(node, "name"));
                final Node identChild = this.getChildElementNodeOfType("record-ident", node);
                if (identChild != null) {
                    final Node fieldChild = this.getChildElementNodeOfType("field-ident", identChild);
                    final Node lengthChild = this.getChildElementNodeOfType("length-ident", identChild);
                    if (lengthChild != null) {
                        r.setLengthIdentMin(Integer.parseInt(this.getAttributeValueNamed(lengthChild, "minlength")));
                        r.setLengthIdentMax(Integer.parseInt(this.getAttributeValueNamed(lengthChild, "maxlength")));
                        r.setIdentTypeFlag('L');
                    }
                    else if (fieldChild != null) {
                        r.setFieldIdentStart(Integer.parseInt(this.getAttributeValueNamed(fieldChild, "field-start")));
                        r.setFieldIdentLength(Integer.parseInt(this.getAttributeValueNamed(fieldChild, "field-length")));
                        final List<Node> matchNodes = this.getChildElementNodesOfType("match-string", fieldChild);
                        for (int j = 0; j < matchNodes.size(); ++j) {
                            r.addFieldIdentMatchString(this.getChildTextNodeValue(matchNodes.get(j)));
                        }
                        r.setIdentTypeFlag('F');
                    }
                }
                final Node recordChild = this.getChildElementNodeOfType("record-definition", node);
                r.setRecordDefinition((RecordDefinition)this.traverse(recordChild));
                return r;
            }
            if (nodeName.equals("record-definition")) {
                final RecordDefinition rd = new RecordDefinition();
                final List<Object> children2 = this.getChildNodes(node);
                for (int k = 0; k < children2.size(); ++k) {
                    final Object o = children2.get(k);
                    if (o.getClass().equals(Bean.class)) {
                        rd.addBeanUsed((Bean)o);
                    }
                    if (o.getClass().equals(Line.class)) {
                        rd.addLine((Line)o);
                    }
                }
                return rd;
            }
            if (nodeName.equals("bean")) {
                final Bean b = new Bean();
                b.setBeanName(this.getAttributeValueNamed(node, "name"));
                b.setBeanClass(this.getAttributeValueNamed(node, "class"));
                try {
                    b.setBeanObjectClass(Class.forName(b.getBeanClass()));
                }
                catch (ClassNotFoundException e) {
                    throw new FlatwormConfigurationValueException("Unable to load class " + b.getBeanClass());
                }
                return b;
            }
            if (nodeName.equals("line")) {
                final Line li = new Line();
                final Node delimit = this.getAttributeNamed(node, "delimit");
                final Node quote = this.getAttributeNamed(node, "quote");
                if (delimit != null) {
                    li.setDelimeter(this.getAttributeValueNamed(node, "delimit"));
                }
                if (quote != null) {
                    li.setQuoteChar(this.getAttributeValueNamed(node, "quote"));
                }
                final List<Object> v = this.getChildNodes(node);
                for (int l = 0; l < v.size(); ++l) {
                    final Object o2 = v.get(l);
                    if (o2 instanceof LineElement) {
                        li.addElement((LineElement)o2);
                    }
                }
                return li;
            }
            if (nodeName.equals("segment-element")) {
                final SegmentElement segment = new SegmentElement();
                segment.setCardinalityMode(CardinalityMode.LOOSE);
                segment.setName(this.getAttributeValueNamed(node, "name"));
                segment.setMinCount(Integer.parseInt(this.getAttributeValueNamed(node, "minCount")));
                segment.setMaxCount(Integer.parseInt(this.getAttributeValueNamed(node, "maxCount")));
                segment.setBeanRef(this.getAttributeValueNamed(node, "beanref"));
                segment.setParentBeanRef(this.getAttributeValueNamed(node, "parent-beanref"));
                segment.setAddMethod(this.getAttributeValueNamed(node, "addMethod"));
                final String segmentMode = this.getAttributeValueNamed(node, "cardinality-mode");
                if (!StringUtils.isBlank(segmentMode)) {
                    if (segmentMode.toLowerCase().startsWith("strict")) {
                        segment.setCardinalityMode(CardinalityMode.STRICT);
                    }
                    else if (segmentMode.toLowerCase().startsWith("restrict")) {
                        segment.setCardinalityMode(CardinalityMode.RESTRICTED);
                    }
                }
                final Node fieldChild = this.getChildElementNodeOfType("field-ident", node);
                if (fieldChild != null) {
                    segment.setFieldIdentStart(Integer.parseInt(this.getAttributeValueNamed(fieldChild, "field-start")));
                    segment.setFieldIdentLength(Integer.parseInt(this.getAttributeValueNamed(fieldChild, "field-length")));
                    final List<Node> matchNodes2 = this.getChildElementNodesOfType("match-string", fieldChild);
                    for (int m = 0; m < matchNodes2.size(); ++m) {
                        segment.addFieldIdentMatchString(this.getChildTextNodeValue(matchNodes2.get(m)));
                    }
                }
                this.validateSegmentConfiguration(segment);
                final List<Object> v = this.getChildNodes(node);
                for (int l = 0; l < v.size(); ++l) {
                    final Object o2 = v.get(l);
                    if (o2 instanceof LineElement) {
                        segment.addElement((LineElement)o2);
                    }
                }
                return segment;
            }
            if (nodeName.equals("record-element")) {
                final RecordElement re = new RecordElement();
                final Node start = this.getAttributeNamed(node, "start");
                final Node end = this.getAttributeNamed(node, "end");
                final Node length = this.getAttributeNamed(node, "length");
                final Node beanref = this.getAttributeNamed(node, "beanref");
                final Node beanType = this.getAttributeNamed(node, "type");
                if (end == null && length == null) {
                    final FlatwormConfigurationValueException err = new FlatwormConfigurationValueException("Must set either the 'end' or 'length' properties");
                    throw err;
                }
                if (end != null && length != null) {
                    final FlatwormConfigurationValueException err = new FlatwormConfigurationValueException("Can't specify both the 'end' or 'length' properties");
                    throw err;
                }
                if (start != null) {
                    re.setFieldStart(Integer.parseInt(start.getNodeValue()));
                }
                if (end != null) {
                    re.setFieldEnd(Integer.parseInt(end.getNodeValue()));
                }
                if (length != null) {
                    re.setFieldLength(Integer.parseInt(length.getNodeValue()));
                }
                if (beanref != null) {
                    re.setBeanRef(beanref.getNodeValue());
                }
                if (beanType != null) {
                    re.setType(beanType.getNodeValue());
                }
                final List<Node> children3 = this.getChildElementNodesOfType("conversion-option", node);
                for (int i2 = 0; i2 < children3.size(); ++i2) {
                    final Node o3 = children3.get(i2);
                    final String name = this.getAttributeValueNamed(o3, "name");
                    final String value = this.getAttributeValueNamed(o3, "value");
                    final ConversionOption co = new ConversionOption(name, value);
                    re.addConversionOption(name, co);
                }
                return re;
            }
        }
        return null;
    }
    
    private void validateSegmentConfiguration(final SegmentElement segment) throws FlatwormConfigurationValueException {
        final StringBuilder errors = new StringBuilder();
        if (StringUtils.isBlank(segment.getBeanRef())) {
            if (!StringUtils.isBlank(segment.getName())) {
                segment.setBeanRef(segment.getName());
            }
            else {
                errors.append("Must specify the beanref to be used, or a segment name that matches a bean name.\n");
            }
        }
        if (StringUtils.isBlank(segment.getParentBeanRef())) {
            errors.append("Must specify the beanref for the parent onject.");
        }
        if (StringUtils.isBlank(segment.getAddMethod()) && errors.length() == 0) {
            segment.setAddMethod("add" + StringUtils.capitalize(StringUtils.isBlank(segment.getName()) ? segment.getBeanRef() : segment.getName()));
        }
        if (segment.getFieldIdentMatchStrings().size() == 0) {
            errors.append("Must specify the segment identifier.\n");
        }
        if (errors.length() > 0) {
            throw new FlatwormConfigurationValueException(errors.toString());
        }
    }
}
