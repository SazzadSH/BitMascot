import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parse {
    private static String regularExpression = "(http(s?):/)(/[^/]+)+" + "\\.(?:jpg)";

    private void parseData(String location, String fileName) throws IOException {
        FileUtil fileUtil = new FileUtil();
        String responseData = fileUtil.reader(location, fileName);
        Pattern pattern = Pattern.compile(regularExpression);
        Matcher matcher = pattern.matcher(responseData);

        String content = "";
        while (matcher.find()) {
            content += matcher.group() + " \n";
        }
        fileUtil.writer(location, fileName, content);
    }

    public void parser(String location, String fileName) {
        try {
            System.out.println("Starting Data Parsing...");
            long startTime = System.nanoTime();
            this.parseData(location, fileName);
            long endTime = System.nanoTime();
            System.out.println("Data Parsing Completed");
            long duration = (endTime - startTime) / 1000000;
            System.out.println("Took " + duration + " milliseconds to execute the Parse!!!");
        } catch (IOException e) {
            System.out.println("Something Went Wrong While Parsing Data!! " + e);
        }
    }
}
