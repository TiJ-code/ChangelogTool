package tij.changelogs.xmlModel;

import java.util.List;

public record XmlComponent(String topic, String path, List<XmlCategory> categories) {
    public XmlComponent(String path, List<XmlCategory> categories) {
        this(null, path, categories);
    }
}
