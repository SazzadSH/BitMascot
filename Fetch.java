/*
RSS Content fetching module
*/

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Fetch
{
    private static final String USER_AGENT = "Mozilla/5.0";

    //Method to connect, fetch and write RSS data
    private void fetchData(String requestURL, String location, String file) throws IOException
    {
        //URL connection
        URL url = new URL(requestURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = connection.getResponseCode();

        if (responseCode == HttpURLConnection.HTTP_OK) 
        {
            //RSS Content
            String inputLine;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuffer response = new StringBuffer();

            while ((inputLine = bufferedReader.readLine()) != null)
            {
                response.append(inputLine);
            }

            bufferedReader.close();

            //Writing D:/a.rss
            FileUtil fileWriter = new FileUtil();
            fileWriter.writer(location, file, response.toString());
        }
        else
        {
            System.out.println("Request Failed!");
        }
    }

    //Data fetching initiation
    public void fetcher(String requestURL, String location, String file)
    {
        try
        {
            System.out.println("Fetching RSS content...");
            this.fetchData(requestURL, location, file);
            System.out.println("Fetching done!");
        }
        catch (IOException e)
        {
            System.out.println("Fetching failed!" + e);
        }
    }
}
