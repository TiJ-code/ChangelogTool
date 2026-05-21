package tij.changelogs.xmlModel;

import java.util.List;

public record XmlCategory(String name, List<XmlBreaking> breakingLevels, List<XmlEntry> topLevelEntries) {}
