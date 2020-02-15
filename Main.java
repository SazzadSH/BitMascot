

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main {
    private static final String REQUEST_URL = "http://rss.cnn.com/rss/edition.rss";
    private static final String SAVE_LOCATION = "d:\\";
    private static final String FILE_NAME = "a.rss";

    public static void main(String[] args) {
        final Fetch dataFetcher = new Fetch();
        final Parse dataParser = new Parse();

        ScheduledExecutorService fetcher = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService parser = Executors.newSingleThreadScheduledExecutor();

        Runnable periodicFetchTask = new Runnable() {
            public void run() {
                dataFetcher.fetcher(REQUEST_URL, SAVE_LOCATION, FILE_NAME);
            }
        };

        Runnable periodicParseTask = new Runnable() {
            public void run() {
                dataParser.parser(SAVE_LOCATION, FILE_NAME);
            }
        };

        fetcher.scheduleAtFixedRate(periodicFetchTask, 0, 15, TimeUnit.SECONDS);
        parser.scheduleAtFixedRate(periodicParseTask, 0, 20, TimeUnit.SECONDS);


    }
}
