

import java.io.*;

public class FileUtil {
    public void writer(String location, String fileName, String content) throws IOException {
        File file = new File(location, fileName);

        if (file.createNewFile()) {
            System.out.println("FileUtil created: " + file.getName());
        }

        BufferedWriter writer = new BufferedWriter(new java.io.FileWriter(file));
        writer.write(content);
        writer.close();
    }

    public String reader(String location, String fileName) throws IOException {
        File file = new File(location, fileName);

        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));

        String response = new String();
        for (String line; (line = bufferedReader.readLine()) != null; response += line);

       bufferedReader.close();
        return response;
    }
}
