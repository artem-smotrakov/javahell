package com.gypsyengineer.java.xml.dtd.xxe;

import com.gypsyengineer.http.SimpleHttpServer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This test shows how SchemaFactory may be used in an unsafe way:
 * 1. For demo purposes, the test starts a local HTTP server which plays a role of an internal server
 *    which should not be available for an adversary.
 * 2. Then, the test prepares an XML document to be validates.
 * 3. Next, the test prepares a malicious XSD document which contains an external entity
 *    which points to the HTTP server.
 * 4. Finally, the test calls the unsafeValidate() method which validates the document
 *    without turning on the secure XML processing mode.
 * 5. During validation, the validator parses the malicious entity
 *    which results to accessing the HTTP server.
 */
public class SchemaFactoryXXEWithURL {

    private static void unsafeValidate(Document doc, String xsd) throws SAXException, IOException {
        String language = "http://www.w3.org/2001/XMLSchema";
        SchemaFactory factory = SchemaFactory.newInstance(language);

        /*
         * Enabling XMLConstants.FEATURE_SECURE_PROCESSING would fix the issue:
         *
         *      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING ,true);
         */
        Schema schema = factory.newSchema(new StreamSource(new ByteArrayInputStream(
            xsd.getBytes(StandardCharsets.UTF_8))));

        Validator validator = schema.newValidator();
        validator.validate(new DOMSource(doc));
    }

    /*
     * XML and XSD documents were taken from https://www.w3schools.com/xml/schema_example.asp
     */
    public static void main(String... args) throws Exception {
        SimpleHttpServer server = new SimpleHttpServer("test");
        try {
            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "\n"
                + "<shiporder orderid=\"889923\"\n"
                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
                + "xsi:noNamespaceSchemaLocation=\"shiporder.xsd\">\n"
                + "  <orderperson>John Smith</orderperson>\n"
                + "  <shipto>\n"
                + "    <name>Ola Nordmann</name>\n"
                + "    <address>Langgt 23</address>\n"
                + "    <city>4000 Stavanger</city>\n"
                + "    <country>Norway</country>\n"
                + "  </shipto>\n"
                + "  <item>\n"
                + "    <title>Empire Burlesque</title>\n"
                + "    <note>Special Edition</note>\n"
                + "    <quantity>1</quantity>\n"
                + "    <price>10.90</price>\n"
                + "  </item>\n"
                + "  <item>\n"
                + "    <title>Hide your heart</title>\n"
                + "    <quantity>1</quantity>\n"
                + "    <price>9.90</price>\n"
                + "  </item>\n"
                + "</shiporder>";

            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document doc = documentBuilder.parse(new InputSource(new ByteArrayInputStream(
                xml.getBytes(StandardCharsets.UTF_8))));

            String xsd = String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
                    + " <!DOCTYPE foo [  \n"
                    + "   <!ELEMENT foo ANY >\n"
                    + "   <!ENTITY xxe SYSTEM \"%s\" >\n"
                    + "]>\n"
                    + "<foo>&xxe;</foo>"
                    + "<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\">\n"
                    + "\n"
                    + "<xs:element name=\"shiporder\">\n"
                    + "  <xs:complexType>\n"
                    + "    <xs:sequence>\n"
                    + "      <xs:element name=\"orderperson\" type=\"xs:string\"/>\n"
                    + "      <xs:element name=\"shipto\">\n"
                    + "        <xs:complexType>\n"
                    + "          <xs:sequence>\n"
                    + "            <xs:element name=\"name\" type=\"xs:string\"/>\n"
                    + "            <xs:element name=\"address\" type=\"xs:string\"/>\n"
                    + "            <xs:element name=\"city\" type=\"xs:string\"/>\n"
                    + "            <xs:element name=\"country\" type=\"xs:string\"/>\n"
                    + "          </xs:sequence>\n"
                    + "        </xs:complexType>\n"
                    + "      </xs:element>\n"
                    + "      <xs:element name=\"item\" maxOccurs=\"unbounded\">\n"
                    + "        <xs:complexType>\n"
                    + "          <xs:sequence>\n"
                    + "            <xs:element name=\"title\" type=\"xs:string\"/>\n"
                    + "            <xs:element name=\"note\" type=\"xs:string\" minOccurs=\"0\"/>\n"
                    + "            <xs:element name=\"quantity\" type=\"xs:positiveInteger\"/>\n"
                    + "            <xs:element name=\"price\" type=\"xs:decimal\"/>\n"
                    + "          </xs:sequence>\n"
                    + "        </xs:complexType>\n"
                    + "      </xs:element>\n"
                    + "    </xs:sequence>\n"
                    + "    <xs:attribute name=\"orderid\" type=\"xs:string\" use=\"required\"/>\n"
                    + "  </xs:complexType>\n"
                    + "</xs:element>\n"
                    + "\n"
                    + "</xs:schema>", server.url());

            if (server.accepted()) {
                throw new IllegalArgumentException(
                    "The server should not have been reached here! Something is wrong with the test!");
            }

            unsafeValidate(doc, xsd);
        } catch (Exception e) {
            System.out.println("Received an exception: ");
        } finally {
            server.close();
        }

        if (server.accepted()) {
            throw new RuntimeException("Oops! The server has been reached!");
        }
    }

}