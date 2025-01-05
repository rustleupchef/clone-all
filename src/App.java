import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class App {
    public static void main(String[] args) throws IOException, InterruptedException, FileNotFoundException, ParseException{
        JSONObject config = (JSONObject) new JSONParser().parse(new FileReader("config.json"));

        Process gitGetUser = new ProcessBuilder("git", "config", "--get", "user.name").start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(gitGetUser.getInputStream()));
        String username = reader.readLine();
        gitGetUser.waitFor();
        reader.close();

        // if the conf.json url is not null change the url to that
        if (!((String)config.get("username")).equals("")) username = (String) config.get("username");

        HashSet<String> ignored = new HashSet<String>();
        JSONArray repos = (JSONArray) config.get("ignored");
        for (Object repo : repos) {
            ignored.add((String) repo);
        }
        for (int i = 1; true; i++) {
            boolean validPage = false;
            Document doc = Jsoup.connect(String.format("https://github.com/%s?page=%d&tab=repositories", username, i)).get();
            Elements links = doc.select("a[href]");  
            for (Element link : links) {
                if (link.attr("itemprop").equals("name codeRepository")) {
                    String name = link.attr("href");
                    if (ignored.contains(name.split("/")[2])) continue; 
                    String url = String.format("https://github.com%s", name);
                    System.out.println(url);
                    Process gitClone = new ProcessBuilder("git", "clone", url).start();
                    gitClone.waitFor();
                    validPage = true;
                }
            }
            if (!validPage) break;
        }
    }
}
