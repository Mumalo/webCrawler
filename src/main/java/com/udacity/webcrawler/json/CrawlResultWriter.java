package com.udacity.webcrawler.json;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Utility class to write a {@link CrawlResult} to file.
 */
public final class CrawlResultWriter {
  private final CrawlResult result;

  /**
   * Creates a new {@link CrawlResultWriter} that will write the given {@link CrawlResult}.
   */
  public CrawlResultWriter(CrawlResult result) {
    this.result = Objects.requireNonNull(result);
  }

  public void writeToFile(String name) throws IOException {
    if (name.isEmpty()){
      Writer outputWriter = new BufferedWriter(new OutputStreamWriter(System.out));
      write(outputWriter);
    } else {
      Path path = Paths.get(name);
      write(path);
    }
  }

  /**
   * Formats the {@link CrawlResult} as JSON and writes it to the given {@link Path}.
   *
   * <p>If a file already exists at the path, the existing file should not be deleted; new data
   * should be appended to it.
   *
   * @param path the file path where the crawl result data should be written.
   */
  public void write(Path path) throws IOException {
    // This is here to get rid of the unused variable warning.
    Objects.requireNonNull(path);
    ObjectMapper mapper = new ObjectMapper();
    if (Files.notExists(path)){
      Files.createFile(path);
    }
    BufferedWriter writer = Files.newBufferedWriter(path);

    mapper.disable(Feature.AUTO_CLOSE_SOURCE);
    mapper.writeValue(writer, result);
    // TODO: Fill in this method.
  }

  /**
   * Formats the {@link CrawlResult} as JSON and writes it to the given {@link Writer}.
   *
   * @param writer the destination where the crawl result data should be written.
   */
  public void write(Writer writer) throws IOException {
    // This is here to get rid of the unused variable warning.
    Objects.requireNonNull(writer);
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.disable(Feature.AUTO_CLOSE_SOURCE);
    objectMapper.writeValue(writer, result);
    // TODO: Fill in this method.
  }
}
