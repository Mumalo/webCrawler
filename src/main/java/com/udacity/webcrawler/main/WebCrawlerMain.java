package com.udacity.webcrawler.main;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import com.udacity.webcrawler.WebCrawler;
import com.udacity.webcrawler.WebCrawlerModule;
import com.udacity.webcrawler.json.ConfigurationLoader;
import com.udacity.webcrawler.json.CrawlResult;
import com.udacity.webcrawler.json.CrawlResultWriter;
import com.udacity.webcrawler.json.CrawlerConfiguration;
import com.udacity.webcrawler.profiler.Profiler;
import com.udacity.webcrawler.profiler.ProfilerModule;
import org.jsoup.internal.StringUtil;

import javax.inject.Inject;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

public final class WebCrawlerMain {

  private final CrawlerConfiguration config;

  private WebCrawlerMain(CrawlerConfiguration config) {
    this.config = Objects.requireNonNull(config);
  }

  @Inject
  private WebCrawler crawler;

  @Inject
  private Profiler profiler;

  private void run() throws Exception {
    Guice.createInjector(new WebCrawlerModule(config), new ProfilerModule()).injectMembers(this);

    WebCrawler crawlerProxy = profiler.wrap(WebCrawler.class, crawler);
    CrawlResult result = crawlerProxy.crawl(config.getStartPages());
    CrawlResultWriter resultWriter = new CrawlResultWriter(result);
    String resultPath = config.getResultPath();
    String profileDataPath = config.getProfileOutputPath();

    if (!resultPath.isEmpty() || !profileDataPath.isEmpty()){
      if (!resultPath.isEmpty()){
        Path path = Paths.get(resultPath);
        resultWriter.write(path);
      }

      if (!profileDataPath.isEmpty()){
        System.out.printf("Writing performance info in file: %s%n", profileDataPath);
        Path path = Paths.get(profileDataPath);
        profiler.writeData(path);
      }
    }

    if (resultPath.isEmpty() || profileDataPath.isEmpty()){
      Writer outputWriter = new BufferedWriter(new OutputStreamWriter(System.out));
      if (resultPath.isEmpty()){
        resultWriter.write(outputWriter);
      }

      if (profileDataPath.isEmpty()){
        outputWriter = new BufferedWriter(new OutputStreamWriter(System.out));
        System.out.println("Writing performance into console\n");
        profiler.writeData(outputWriter);
        outputWriter.flush();
      }
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 1) {
      System.out.println("Usage: WebCrawlerMain [starting-url]");
      return;
    }

    CrawlerConfiguration config = new ConfigurationLoader(Path.of(args[0])).load();
    new WebCrawlerMain(config).run();
  }
}
