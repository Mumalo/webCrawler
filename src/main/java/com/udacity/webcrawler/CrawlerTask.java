package com.udacity.webcrawler;

import com.udacity.webcrawler.parser.PageParser;
import com.udacity.webcrawler.parser.PageParserFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

public class CrawlerTask extends RecursiveAction {

    private final String url;
    private final Instant deadline;
    private final int maxDepth;
    private final Set<String> visitedUrls;
    private final List<Pattern> ignoredUrls;
    private final Clock clock;
    private final PageParserFactory parserFactory;
    private final Map<String, Integer> wordCounts;

    private CrawlerTask(
            String url,
            Instant deadline,
            int maxDepth,
            Set<String> visitedUrls,
            List<Pattern> ignoredUrls,
            Clock clock,
            PageParserFactory parserFactory,
            Map<String, Integer> wordCounts) {
        this.url = url;
        this.deadline = deadline;
        this.maxDepth = maxDepth;
        this.visitedUrls = visitedUrls;
        this.ignoredUrls = ignoredUrls;
        this.clock = clock;
        this.parserFactory = parserFactory;
        this.wordCounts = wordCounts;
    }

    public static class Builder {
        private String url;
        private Instant deadline;
        private List<Pattern> ignoredUrls;
        private Clock clock;
        private Set<String> visitedUrls;
        private PageParserFactory parserFactory;
        private int maxDepth;
        private Map<String, Integer> wordCounts;

        public Builder setUrl(String url){
            this.url = url;
            return this;
        }

        public Builder setDeadline(Instant deadline){
            this.deadline = deadline;
            return this;
        }

        public Builder setClock(Clock clock){
            this.clock = clock;
            return this;
        }

        public Builder setMaxDepth(int maxDepth){
            this.maxDepth = maxDepth;
            return this;
        }

        public Builder setIgnoredUrls(List<Pattern> ignoredUrls){
            this.ignoredUrls = ignoredUrls;
            return this;
        }

        public Builder setVisitedUrls(Set<String> visitedUrls){
            this.visitedUrls = visitedUrls;
            return this;
        }

        public Builder setParserFactory(PageParserFactory parserFactory){
            this.parserFactory = parserFactory;
            return this;
        }

        public Builder setWordCounts(Map<String, Integer> wordCounts){
            this.wordCounts = wordCounts;
            return this;
        }

        CrawlerTask build(){
            return new CrawlerTask(
                    url,
                    deadline,
                    maxDepth,
                    visitedUrls,
                    ignoredUrls,
                    clock,
                    parserFactory,
                    wordCounts
            );
        }
    }

    private boolean stopCrawl(){
        if (maxDepth == 0 || clock.instant().isAfter(deadline)){
            return true;
        }

        if (visitedUrls.contains(url)){
            return true;
        }

        return ignoredUrls
                .stream()
                .anyMatch(pattern -> pattern.matcher(url).matches());
    }

    @Override
    protected void compute() {
        if (stopCrawl()) return;

        visitedUrls.add(url);

        PageParser.Result result = parserFactory.get(url).parse();

        result.getWordCounts().forEach((key, value) -> wordCounts.compute(key, (k, v) -> (v == null) ? value : value + v));

        List<CrawlerTask> tasks = getTasks(result.getLinks(), maxDepth-1);
        invokeAll(tasks);
    }

    public static CrawlerTask newInstance(
            String url,
            Instant deadline,
            int maxDepth,
            Set<String> visitedUrls,
            List<Pattern> ignoredUrls,
            Clock clock,
            PageParserFactory parserFactory,
            Map<String, Integer> wordCounts
    ){
        Builder builder = new Builder();
        builder
                .setUrl(url)
                .setDeadline(deadline)
                .setMaxDepth(maxDepth)
                .setVisitedUrls(visitedUrls)
                .setIgnoredUrls(ignoredUrls)
                .setClock(clock)
                .setParserFactory(parserFactory)
                .setWordCounts(wordCounts);
        return builder.build();
    }

    private List<CrawlerTask> getTasks(List<String> links, int maxDepth){
        List<CrawlerTask> taskList = new ArrayList<>();

        for (String link : links){
            CrawlerTask currentTask = CrawlerTask.newInstance(
                    link,
                    deadline,
                    maxDepth,
                    visitedUrls,
                    ignoredUrls,
                    clock,
                    parserFactory,
                    wordCounts
            );
            taskList.add(currentTask);
        }

        return taskList;
    }
}
