package ch.hatbe;

import java.io.IOException;
import java.nio.file.Path;

public class Main {
    static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Usage: java -jar app.jar <rootPath> <outputFile>");
            return;
        }

        String rootPath = args[0];
        Path outputFile = Path.of(args[1]);

        try {
            ImageFinder finder = new ImageFinder(rootPath);

            finder.writeReport(outputFile);
        } catch (RuntimeException | IOException e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}