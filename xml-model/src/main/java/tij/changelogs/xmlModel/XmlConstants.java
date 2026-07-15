package tij.changelogs.xmlModel;

public final class XmlConstants {
    public static final String TAG_CHANGELOG = "changelog";
    public static final String TAG_PATCH = "patch";
    public static final String TAG_TOPIC = "topic";
    public static final String TAG_COMPONENT = "component";
    public static final String TAG_CATEGORY = "category";
    public static final String TAG_BREAKING = "breaking";
    public static final String TAG_ENTRY = "entry";

    public static final String ATTRIBUTE_PATCH_VERSION = "version";
    public static final String ATTRIBUTE_COMPONENT_REF = "ref";
    public static final String ATTRIBUTE_TOPIC_NAME = "name";
    public static final String ATTRIBUTE_CATEGORY_NAME = "name";
    public static final String ATTRIBUTE_BREAKING_SEVERITY = "severity";

    private XmlConstants() {}
}
