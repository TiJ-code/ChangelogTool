package tij.changelogs.xmlModel;

import java.util.List;

public record XmlComponent(String path, List<XmlCategory> categories) {}
