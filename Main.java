/*
Bit Mascot Java Competency
Author: Sazzad Hossen
Github: github.com/SazzadSH/BitMascot
Email: thesazzadsh@gmail.com
*/

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Main 
{
    private static final String REQ_URL = "http://rss.cnn.com/rss/edition.rss";
    private static final String LOCATION = "d:\\";
    private static final String FILE = "a.rss";

    public static void main(String[] args)
    {
        final Fetch contentFetcher = new Fetch();
        final Parse contentParser = new Parse();

        //Threads
        ScheduledExecutorService fetcher = Executors.newSingleThreadScheduledExecutor();
        ScheduledExecutorService parser = Executors.newSingleThreadScheduledExecutor();

        //Fetches data from RSS Feed
        Runnable fetchTask = new Runnable() 
        {
            public void run() {
                contentFetcher.fetcher(REQ_URL, LOCATION, FILE);
            }
        };

        //Prints JPEG image references in the file & CLI
        Runnable parseTask = new Runnable() 
        {
            public void run() 
            {
                contentParser.parser(LOCATION, FILE);
            }
        };

        //Task execution using threads, after 15 and 20 seconds
        fetcher.scheduleAtFixedRate(fetchTask, 0, 15, TimeUnit.SECONDS);
        parser.scheduleAtFixedRate(parseTask, 0, 20, TimeUnit.SECONDS);
    }
}
