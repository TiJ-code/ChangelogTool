package tij.changelogs.markdown;

import org.w3c.dom.Node;
import tij.changelogs.config.Config;
import tij.changelogs.config.ConfigConstants;
import tij.changelogs.config.ConfigSystem;
import tij.changelogs.markdown.parser.ChangelogParser;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class Main {
    static void main(String[] args) {
        Path configFilePath = CliParser.parse(args);
        Config config = ConfigSystem.load(configFilePath);

        File dir = ConfigConstants.CUMULATED_DIR;

        File[] filesArray = dir.listFiles((_, name) ->
                name.toLowerCase().endsWith(".xml")
        );

        if (filesArray == null) {
            throw new IllegalStateException(
                    "Path direcotry does not exist: " + dir.getAbsolutePath()
            );
        }

        List<File> files = Arrays.stream(filesArray)
                .map(File::getAbsoluteFile)
                .toList();

        if (files.isEmpty()) {
            throw new IllegalStateException(
                    "No cumulated changelog xml exists"
            );
        }

        File cumulatedFile = files.getFirst();

        String version = getVersionFromPom();

        cumulatedFile = renameFileToReleaseVersion(cumulatedFile, version);

        var list = ChangelogParser.parse(cumulatedFile, config);
        System.out.println(list);
    }

    private static File renameFileToReleaseVersion(File file, String version) {
        if (file.getName().contains(version))
            return file;

        Path original = file.toPath();
        Path sibling = original.resolveSibling(version + "_" + file.getName());
        file.renameTo(sibling.toAbsolutePath().toFile());
        return sibling.toAbsolutePath().toFile();
    }

    private static String getVersionFromPom() {
        try {
            var factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(true);
            factory.setValidating(false);
            factory.setNamespaceAware(false);

            var builder = factory.newDocumentBuilder();

            File root = new File(".").getCanonicalFile();

            var doc = builder.parse(new File(root, "pom.xml"));

            var children = doc.getDocumentElement().getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                var node = children.item(i);

                if (node.getNodeType() != Node.ELEMENT_NODE)
                    continue;

                if (node.getNodeName().equals("version"))
                    return node.getTextContent();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
