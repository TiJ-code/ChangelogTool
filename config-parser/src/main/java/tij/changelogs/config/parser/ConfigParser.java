package tij.changelogs.config.parser;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import tij.changelogs.config.Config;
import tij.changelogs.config.model.ConfigFileVersion;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public final class ConfigParser {

    private ConfigParser() {}

    public static Config parse(Path path) {
        try {
            byte[] data = loadConfig(path);

            ConfigFileVersion version =
                    parseVersionWithoutDtd(new ByteArrayInputStream(data));

            return parseWithDtd(
                    new ByteArrayInputStream(data),
                    version
            );

        } catch (Exception e) {
            throw new RuntimeException("Failed to parse config", e);
        }
    }

    private static byte[] loadConfig(Path path) throws IOException {

        String normalized = path.toString().replace("\\", "/");

        try (InputStream resource =
                ConfigParser.class.getClassLoader()
                        .getResourceAsStream(normalized)) {

            if (resource != null) {
                return resource.readAllBytes();
            }

            return Files.readAllBytes(path);
        }
    }

    private static Config parseWithDtd(
            InputStream is,
            ConfigFileVersion fv
    ) {

        try {
            var factory = DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(false);
            factory.setIgnoringComments(true);
            factory.setValidating(true);

            var builder = factory.newDocumentBuilder();

            builder.setEntityResolver(createResolver());

            Document doc = builder.parse(is);

            return fv.parseFunction.apply(doc);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Error when parsing configuration",
                    e
            );
        }
    }

    private static ConfigFileVersion parseVersionWithoutDtd(
            InputStream is
    ) {

        try {
            var factory = DocumentBuilderFactory.newInstance();

            factory.setNamespaceAware(false);
            factory.setIgnoringComments(true);
            factory.setValidating(false);

            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/validation/dynamic", false);

            var builder = factory.newDocumentBuilder();

            Document doc = builder.parse(is);

            var el = doc.getDocumentElement();

            return ConfigFileVersion.from(
                    el.getAttribute("version")
            );

        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not parse config version",
                    e
            );
        }
    }

    private static EntityResolver createResolver() {

        return (_, systemId) -> {

            if (systemId == null) {
                return null;
            }

            String fileName = systemId;

            if (systemId.contains("/")) {
                fileName = systemId.substring(systemId.lastIndexOf('/') + 1);
            }

            String resourcePath = "/dtd/" + fileName;

            InputStream resource =
                    ConfigParser.class.getResourceAsStream(resourcePath);

            if (resource != null) {
                return new InputSource(resource);
            }

            throw new FileNotFoundException(
                    "DTD not found in resources: " + resourcePath
            );
        };
    }
}