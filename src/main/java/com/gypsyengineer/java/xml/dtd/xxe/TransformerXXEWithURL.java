package com.gypsyengineer.java.xml.dtd.xxe;

import com.gypsyengineer.http.SimpleHttpServer;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This test shows how Transformer may be used in an unsafe way:
 *
 * 1. For demo purposes, the test starts a local HTTP server which plays a role of an internal server
 *    which should not be available for an adversary.
 * 2. Then, the test prepares an XML document which contains an external entity
 *    which points to the HTTP server.
 * 3. Finally, the test calls the unsafeTransform() method which transforms the document.
 *    without turning on the secure XML processing mode.
 * 4. During transformation, the transformer parses the malicious entity
 *    which results to accessing the HTTP server.
 */
public class TransformerXXEWithURL {

    private static String unsafeTransform(String xml) throws TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();

        /*
         * Enabling XMLConstants.FEATURE_SECURE_PROCESSING would fix the issue:
         *
         *      factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING ,true);
         */
        Transformer transformer = factory.newTransformer();
        StringWriter buff = new StringWriter();
        transformer.transform(new StreamSource(new StringReader(xml)), new StreamResult(buff));

        return buff.toString();
    }

    public static void main(String... args) throws Exception {
        SimpleHttpServer server = new SimpleHttpServer("test");
        try {
            String xml = String.format(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                    + "<!DOCTYPE bar [\n"
                    + "     <!ENTITY foo SYSTEM \"%s\" >\n"
                    + "]>\n"
                    + "<xxe>&foo;</xxe>",
                server.url());

            unsafeTransform(xml);
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