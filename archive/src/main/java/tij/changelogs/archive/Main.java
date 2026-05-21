package tij.changelogs.archive;

import tij.changelogs.config.ConfigConstants;

import java.io.File;
import java.nio.file.Files;

public class Main {

    static void main() {
        try {
            File dir = ConfigConstants.CUMULATED_DIR;
            File[] files = dir.listFiles();

            if (files == null || files.length == 0)
                throw new IllegalStateException("No cumulated files");

            File xml = null, md = null;

            for (File f : files) {
                if (f.getName().endsWith(".xml")) xml = f;
                if (f.getName().endsWith(".md")) md = f;
            }

            if (xml == null || md == null)
                throw new IllegalStateException("Missing xml or md");

            String base = strip(xml.getName());

            File outDir = new File(ConfigConstants.ARCHIVE_DIR, base);
            if (!outDir.exists()) outDir.mkdirs();

            Files.move(xml.toPath(), new File(outDir, xml.getName()).toPath());
            Files.move(md.toPath(), new File(outDir, md.getName()).toPath());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String strip(String n) {
        int i = n.lastIndexOf('.');
        return i == -1 ? n : n.substring(0, i);
    }
}