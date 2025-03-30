package io.github.sam42r.semver.analyzer.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Configuration {

    private static final String CLASSPATH_RESOURCE = "classpath:";

    private String release;

    private List<AnalyzedCommit> items;

    public static Configuration read(String resource) {
        var objectMapper = new ObjectMapper(new YAMLFactory());
        try (var inputStream = resource.startsWith(CLASSPATH_RESOURCE) ?
                Configuration.class.getResourceAsStream(resource.replace(CLASSPATH_RESOURCE, "")) :
                new FileInputStream(resource)
        ) {
            return objectMapper.readValue(inputStream, Configuration.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
