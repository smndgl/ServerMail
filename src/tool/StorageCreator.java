package tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class StorageCreator {
    public static void main(String[] args) {
        String root = System.getProperty("user.dir")+"/src/storage/";
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(root + "account_list.txt")));
            String line = "";
            Boolean res = true;
            while((line = br.readLine()) != null) {
                new File(root+line+"_inbox.json").createNewFile();
                new File(root+line+"_sent.json").createNewFile();
                System.out.println("storage created for "+line);
            }
        }
        catch(IOException e) {
            e.printStackTrace();
        }
    }
}
