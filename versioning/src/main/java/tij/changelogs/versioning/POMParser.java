package tij.changelogs.versioning;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class POMParser {
    private final File pomFile;

    private Element rootElement;
    private boolean parsed;

    public POMParser(File pomFile) {
        this.pomFile = pomFile;
        this.parsed = false;
    }

    public void parse() {
        try {
            var builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(pomFile);
            this.rootElement = doc.getDocumentElement();
            parsed = true;
        } catch (Exception e) {
            parsed = false;
        }
    }

    public void setVersionString(String newVersion) {
        try {
            String fileStr = Files.readString(pomFile.toPath());

            String currentVersionString = getVersionString();
            System.out.println(currentVersionString);

            fileStr = fileStr.replace(currentVersionString, newVersion);
            Files.writeString(pomFile.toPath(), fileStr);
        } catch (Exception e) {
            System.err.println("Failed to update " + pomFile.getPath());
            e.printStackTrace();
        }
    }

    public String getVersionString() {
        ensureParsed();

        List<Element> directVersionChildren = directChildrenByTag(rootElement, "version");
        if (!directVersionChildren.isEmpty())
            return directVersionChildren.getFirst().getTextContent().trim();

        var parentNodes = rootElement.getElementsByTagName("parent");
        for (int i = 0; i < parentNodes.getLength(); i++) {
            var parentEl = (Element) parentNodes.item(i);

            List<Element> parent_directVersionChildren = directChildrenByTag(parentEl, "version");
            if (!parent_directVersionChildren.isEmpty())
                return parent_directVersionChildren.getFirst().getTextContent().trim();
        }

        return null;
    }

    private static List<Element> directChildrenByTag(Element parent, String tag) {
        List<Element> result = new ArrayList<>();

        var children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            var node = children.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE)
                continue;

            if (!node.getNodeName().equals(tag))
                continue;

            result.add((Element) node);
        }

        return result;
    }

    private void ensureParsed() {
        if (!parsed)
            parse();
    }
}
