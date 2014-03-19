import java.io.*;
import java.util.*;
class FileOp
{
public String readFile(String pathname) throws IOException {

    File file = new File(pathname);
    StringBuilder fileContents = new StringBuilder((int)file.length());
    Scanner scanner = new Scanner(file);
    String lineSeparator = System.getProperty("line.separator");

    try {
        while(scanner.hasNextLine()) {        
            fileContents.append(scanner.nextLine() + lineSeparator);
        }
        return fileContents.toString();
    } finally {
        scanner.close();
    }
}
public static void Save(String pathname, String textToSave)  throws IOException{
	File file = new File(pathname);
    file.delete();
    try {
        BufferedWriter out = new BufferedWriter(new FileWriter(file));
        out.write(textToSave);
        out.close();
    } catch (IOException e) {
    }
}
}