package tij.changelogs.versioning;

import java.io.File;
import java.util.List;

public class Main {
    static void main(String[] args) {
        CliOptions options = CliParser.parse(args);
        if (options == null) {
            System.exit(1);
        }

        List<File> poms = FileFinder.findPomFiles();

        File rootPom = poms.getFirst();
        poms.removeFirst();

        List<POMParser> pomParsers = poms.stream().map(POMParser::new).toList();

        POMParser rootParser = new POMParser(rootPom);
        String rootVerStr = rootParser.getVersionString();
        Version rootVer = Version.fromString(rootVerStr);

        Version nextVer = rootVer.next(options);
        rootParser.setVersionString(nextVer.toString());
        pomParsers.forEach(parser -> parser.setVersionString(nextVer.toString()));
    }
}
