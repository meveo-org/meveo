package org.meveo.model.scripts;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;

public class FunctionUtils {

    private final static DocumentBuilderFactory BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    private final static XPath XPATH = XPathFactory.newInstance().newXPath();
    private static final String FUNCTION_CODE_XPATH = "//org.meveo.jmeter.threadgroup.model.MeveoThreadGroup[@testname='Meveo - Function Test']//stringProp[@name='functionCode']";
    private static final String CODE_XPATH = "//org.meveo.jmeter.sampler.model.MeveoSampler[@testname='Execute function']//stringProp[@name='code']";
    private static final String TESTPLAN_XPTAH = "//TestPlan";

    private static XPathExpression fnCodeXpath;
    private static XPathExpression codeXpath;
    private static XPathExpression testplanXpath;

    private static DocumentBuilder documentBuilder;

    static {
        BUILDER_FACTORY.setValidating(false);
        try {
            documentBuilder = BUILDER_FACTORY.newDocumentBuilder();

            fnCodeXpath = XPATH.compile(FUNCTION_CODE_XPATH);
            codeXpath = XPATH.compile(CODE_XPATH);
            testplanXpath = XPATH.compile(TESTPLAN_XPTAH);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static String asString(Node node) {
        try {
            DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
            LSSerializer writer = impl.createLSSerializer();
            writer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            LSOutput output = impl.createLSOutput();
            output.setByteStream(result);
            writer.write(node, output);
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Mainly used in JUnit test class
     *
     * @param fileContent test suite content
     * @param code        code of the function
     * @return the name of the property that is not set correctly if any, null otherwise
     */
    public static String checkTestSuite(String fileContent, String code) {
        try {
            try(StringReader stringReader = new StringReader(fileContent)) {
                InputSource source = new InputSource(stringReader);
                Document document = documentBuilder.parse(source);

                Node functionCodeNode = (Node) fnCodeXpath.evaluate(document, XPathConstants.NODE);
                if(!functionCodeNode.getFirstChild().getNodeValue().equals(code)) {
                    return "functionCode";
                }

                Node codeNode = (Node) codeXpath.evaluate(document, XPathConstants.NODE);
                if(!codeNode.getFirstChild().getNodeValue().equals(code)) {
                    return "code";
                }

                Node testplanNode = (Node) testplanXpath.evaluate(document, XPathConstants.NODE);
                if(!testplanNode.getAttributes().getNamedItem("testname").getNodeValue().equals(code)) {
                    return "testPlan";
                }

                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String replaceWithCorrectCode(String jmeterTestFileContent, String code) {

        try {
            try(StringReader stringReader = new StringReader(jmeterTestFileContent)) {
                InputSource source = new InputSource(stringReader);
                Document document = documentBuilder.parse(source);

                Node functionCodeNode = (Node) fnCodeXpath.evaluate(document, XPathConstants.NODE);
                functionCodeNode.getFirstChild().setNodeValue(code);

                Node codeNode = (Node) codeXpath.evaluate(document, XPathConstants.NODE);
                codeNode.getFirstChild().setNodeValue(code);

                Node testplanNode = (Node) testplanXpath.evaluate(document, XPathConstants.NODE);
                testplanNode.getAttributes().getNamedItem("testname").setNodeValue(code);

                return asString(document);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }


}
