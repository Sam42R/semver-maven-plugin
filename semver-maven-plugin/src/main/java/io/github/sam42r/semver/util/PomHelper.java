package io.github.sam42r.semver.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Stack;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PomHelper {

    public static void changeVersion(Path pom, String version) {
        changeVersionInternal(pom, version, "/project/version");
    }

    public static void changeParentVersion(Path pom, String version) {
        changeVersionInternal(pom, version, "/project/parent/version");
    }

    private static void changeVersionInternal(Path pom, String version, String path) {
        final var parserFactory = SAXParserFactory.newInstance();
        final var projectVersionLocator = new ProjectVersionLocator(path);

        try (var inputStream = Files.newInputStream(pom)) {
            var parser = parserFactory.newSAXParser();
            parser.parse(inputStream, projectVersionLocator);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (ParserConfigurationException | SAXException e) {
            throw new IllegalArgumentException(e);
        }

        var projectVersionLocation = projectVersionLocator.getProjectVersionLocation();
        log.debug("Version start={} end={}", projectVersionLocation.from(), projectVersionLocation.to());

        try (var outputStream = new ByteArrayOutputStream(); var writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            var lines = Files.readAllLines(pom);
            // note: since we need the line number to locate project version we use traditionally for loop here
            for (var lineNumber = 0; lineNumber < lines.size(); lineNumber++) {
                var line = lines.get(lineNumber);

                var lineToWrite = lineNumber == projectVersionLocation.line() ?
                        line.substring(0, projectVersionLocation.from()) + version + line.substring(projectVersionLocation.to()) :
                        line;

                writer.write(lineToWrite);
                writer.newLine();
            }
            writer.flush();

            Files.write(pom, outputStream.toByteArray(), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Project version location with zero-based values.
     *
     * @author Sam42R
     */
    private record ProjectVersionLocation(int line, int from, int to) {
    }

    /**
     * SAX {@link DefaultHandler} to find project version location in <i>pom.xml</i>.
     *
     * @author Sam42R
     */
    @SuppressWarnings("java:S1149")
    private static class ProjectVersionLocator extends DefaultHandler {
        private static final String VERSION_ELEM_NAME = "version";

        private final Stack<String> stack = new Stack<>();
        private final String path;
        private Locator locator;

        private Point versionStart;
        private Point versionEnd;

        public ProjectVersionLocator(String path) {
            this.path = path.toLowerCase();
        }

        @Override
        public void setDocumentLocator(Locator locator) {
            this.locator = locator;
        }

        @Override
        public void startDocument() throws SAXException {
            stack.clear();
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            stack.push(qName);

            if (VERSION_ELEM_NAME.equalsIgnoreCase(qName) && path.equalsIgnoreCase(getCurrentPath())) {
                versionStart = new Point(locator.getColumnNumber() - 1, locator.getLineNumber() - 1);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (VERSION_ELEM_NAME.equalsIgnoreCase(qName) && path.equalsIgnoreCase(getCurrentPath())) {
                versionEnd = new Point(locator.getColumnNumber() - "</%s>".formatted(VERSION_ELEM_NAME).length() - 1, locator.getLineNumber() - 1);
            }

            stack.pop();
        }

        private String getCurrentPath() {
            return "/" + String.join("/", stack);
        }

        public ProjectVersionLocation getProjectVersionLocation() {
            return new ProjectVersionLocation((int) versionStart.getY(), (int) versionStart.getX(), (int) versionEnd.getX());
        }
    }
}
