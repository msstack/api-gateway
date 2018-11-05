package gateway.util;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class ReadAPIdoc {

    private ArrayList<String> urls = new ArrayList<>();
    private ArrayList<String[]> processedUrls = new ArrayList<>();
    private static ReadAPIdoc readAPIdoc = null;

    private ReadAPIdoc(){
    }

    public static ReadAPIdoc getInstance(){
        if(readAPIdoc==null) {
            readAPIdoc = new ReadAPIdoc();
        }
        return readAPIdoc;
    }

    public ArrayList<String[]> getEndPoints(String APIdocPath){
        readURLs(APIdocPath);
        return processedUrls;
    }

    private void readURLs(String fileName){
        try {
            BufferedReader bf = new BufferedReader(new FileReader(fileName));
            bf.lines().forEach(urls::add);
            processUrls();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void processUrls(){
        if(urls.isEmpty()){
            return;
        }else {
            urls.stream().forEach(s->{
                processedUrls.add(s.split(" "));
            });
        }
    }
}
