
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class Fetch {
    private static final String USER_AGENT = "Mozilla/5.0";


    private void fetchData(String requestURL, String location, String fileName) throws IOException {
        URL url = new URL(requestURL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", USER_AGENT);
        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = bufferedReader.readLine()) != null) {
                response.append(inputLine);
            }
            bufferedReader.close();

            FileUtil fileWriter = new FileUtil();
            fileWriter.writer(location, fileName, response.toString());
        } else {
            System.out.println("GET request not worked");
        }
    }

    public void fetcher(String requestURL, String location, String fileName) {
        try {
            System.out.println("Starting Data Fetching...");
            long startTime = System.nanoTime();
            this.fetchData(requestURL, location, fileName);
            long endTime = System.nanoTime();
            System.out.println("Data Fetching Completed");
            long duration = (endTime - startTime) / 1000000;
            System.out.println("Took " + duration + " milliseconds to execute the Fetch!!!");
        } catch (IOException e) {
            System.out.println("Something Went Wrong While Fetching Data!! " + e);
        }
    }
}
