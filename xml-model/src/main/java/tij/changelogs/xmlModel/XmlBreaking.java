package tij.changelogs.xmlModel;

import java.util.List;

public record XmlBreaking(String severity, List<XmlEntry> entries) {}
