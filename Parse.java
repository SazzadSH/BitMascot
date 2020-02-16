/*
JPEG References printing module
*/

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parse 
{
    private static String regEx = "(http(s?):/)(/[^/]+)+" + "\\.(?:jpg)";

    //Method to parse JPEG references from RSS
    private void parseData(String location, String file) throws IOException
    {
        FileUtil filePrint = new FileUtil();
        String responseData = filePrint.reader(location, file);
        Pattern pattern = Pattern.compile(regEx);
        Matcher matcher = pattern.matcher(responseData);

        String content = "";

        while (matcher.find())
        {
            content += matcher.group() + " \n";
        }

        //Printing JPEG references in file & CLI
        filePrint.writer(location, file, content);
        System.out.println(content);
    }

    //Parsing JPEG References
    public void parser(String location, String file)
    {
        long startTime, endTime, duration;
        try
        {
            System.out.println("Parsing JPEG Reference...");
            startTime = System.nanoTime();
            this.parseData(location, file);

            endTime = System.nanoTime();
            System.out.println("Parsing done!");

            duration = (endTime - startTime) / 1000000;
            //System.out.println("Took " + duration + " milliseconds to execute the Parse!!!");
        }
        catch (IOException e)
        {
            System.out.println("Failed parsing!" + e);
        }
    }
}
