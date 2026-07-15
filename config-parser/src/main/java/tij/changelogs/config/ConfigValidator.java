package tij.changelogs.config;

import org.w3c.dom.Document;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

public class ConfigValidator {
    public static void validate(Document doc, String xsdPath) throws Exception {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Schema schema = factory.newSchema(ConfigValidator.class.getResource(xsdPath));
        Validator validator = schema.newValidator();

        validator.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException e) { System.out.println("Warning: " + e.getMessage()); }
            @Override
            public void error(SAXParseException e) throws SAXParseException { throw e; }
            @Override
            public void fatalError(SAXParseException e) throws SAXParseException { throw e; }
        });

        validator.validate(new DOMSource(doc));
    }
}
