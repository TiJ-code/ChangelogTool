package tij.changelogs.versioning;

import tij.changelogs.versioning.cli.CliArguments;
import tij.changelogs.versioning.cli.CliCommand;
import tij.changelogs.versioning.cli.CliParser;

import java.io.File;
import java.util.List;

public class Main {
    static void main(String[] args) {
        CliArguments arguments = CliParser.parse(args);
        if (arguments == null) {
            System.exit(1);
        }

        List<File> poms = FileFinder.findPomFiles();

        File rootPom = poms.getFirst();
        poms.removeFirst();

        List<POMParser> pomParsers = poms.stream().map(POMParser::new).toList();

        POMParser rootParser = new POMParser(rootPom);
        String rootVerStr = rootParser.getVersionString();
        Version current = Version.fromString(rootVerStr);

        if (CliCommand.STRING.equals(arguments.cmd())) {
            System.out.println(current.displayString());
            System.exit(0);
        }

        Version nextVer = switch (arguments.cmd()) {
            case RELEASE -> current.release();
            case STAGE -> current.stage(arguments.stage());
            case SUFFIX -> current.snapshot();
            default -> throw new IllegalStateException("Unexpected value: " + arguments.cmd());
        };
        rootParser.setVersionString(nextVer.toString());
        pomParsers.forEach(parser -> parser.setVersionString(nextVer.toString()));
    }
}
