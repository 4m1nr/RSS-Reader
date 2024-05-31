import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import static java.lang.System.*;


public class RSS {
    private static final int MAX_ITEMS = 5;
    private ArrayList<WebSite> websites;
    File file;
    String fileAddress = "Websites.ser";
    Scanner scanner;
    public RSS() {
        file = new File(fileAddress);
        if (file.exists()){
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                websites = (ArrayList<WebSite>) objectInputStream.readObject();
                objectInputStream.close();
            } catch (Exception e) {
                System.out.println("there was a problem with loading websites");
                System.out.println("websites list will be empty");
                websites = new ArrayList<>();
            }
        }else {
            websites = new ArrayList<>();
        }
        scanner = new Scanner(System.in);
        out.println("Welcome to RSS Reader!");
        showHelp();
        while(true){
            try {action(getInput(scanner.nextLine(), 4));}
            catch (NumberFormatException e) {System.out.println("invalid input");}
            showHelp();
        }
    }

    private void saveWebsites(){
        file = new File(fileAddress);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(websites);
            objectOutputStream.close();
        }catch (IOException e) {
            e.printStackTrace(System.out);
        }
    }

    private void action(int action) {
        switch (action) {
            case 1 -> getUpdates();
            case 2 -> addWebsite();
            case 3 -> deleteWebsite();
            case 4 -> exit(0);
        }
    }
    private void addWebsite(){
        System.out.println("Please enter website URL to add");
        String url = scanner.nextLine();
        try {
            WebSite temp = new WebSite(url);
            if (!websites.contains(temp)){
                websites.add(new WebSite(url));
                System.out.println("Added " + url + " successfully");
        }else
        System.out.println(url + " already exists");
            }catch (Exception e) {
                System.out.println("Something was wrong with adding the URL");
            }
        saveWebsites();
    }
    private void deleteWebsite(){
        System.out.println("Please enter website URL to remove");
        String url = scanner.nextLine();
        try {
            WebSite temp = new WebSite(url);
            if (websites.remove(temp))
                System.out.println("Removed " + url + " Successfully");
            else
                System.out.println("Couldn't find " + url);
        } catch (Exception e) {
            System.out.println("Something was wrong with deleting the URL");
        }
        saveWebsites();
    }
    private void getUpdates() {
        if (websites.size() > 0) {
            System.out.println("[0] all websites");
            for (int i = 0; i < websites.size(); i++)
                System.out.println("[" + (i + 1) + "] " + websites.get(i));
            System.out.println("Enter -1 to return");
        } else{
            System.out.println("Website list is empty");
            return;
        }
        int index;
        try {
            index = getInput(scanner.nextLine(),websites.size()+1);
        }catch (NumberFormatException e){
            out.println("invalid index");
            return;
        }
        if (index == -1)
            return;
        if (index == 0){
            websites.forEach(webSite -> retrieveRssContent(webSite.getRssUrl().toString()));
        }else{
            retrieveRssContent(websites.get(index-1).getRssUrl().toString());
        }
    }
    private int getInput(String input,int max){
        int tempInput;
        tempInput = Integer.parseInt(input);
        if (tempInput > max)
            throw new NumberFormatException();
        else
            return tempInput;
    }


    public static String extractPageTitle(String url) {
        org.jsoup.nodes.Document doc;
        try {
            doc = Jsoup.connect(url).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return doc.select("title").first().text();
    }

    private static void showHelp(){
        System.out.print(
                "Type a valid number for your desired action\n"
                +"[1] Show updates\n"
                +"[2] Add URL\n"
                +"[3] Remove URL\n"
                +"[4] Exit\n"
        );
    }
    private static void retrieveRssContent(String rssUrl){
        try {
            String rssXml = fetchPageSource(rssUrl);
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            StringBuilder xmlStringBuilder = new StringBuilder();
            xmlStringBuilder.append(rssXml);
            ByteArrayInputStream input = new ByteArrayInputStream(
                    xmlStringBuilder.toString().getBytes("UTF-8"));
            org.w3c.dom.Document doc = documentBuilder.parse(input);
            NodeList itemNodes = doc.getElementsByTagName("item");
            for (int i = 0; i < MAX_ITEMS; ++i) {
                Node itemNode = itemNodes.item(i);
                if (itemNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) itemNode;
                    System.out.println("Title: " + element.getElementsByTagName("title").item(0).getTextContent());
                    System.out.println("Link: " + element.getElementsByTagName("link").item(0).getTextContent());
                    System.out.println("Description: " + element.getElementsByTagName("description").item(0).
                            getTextContent());
                    }
                }
            }
        catch (Exception e)
        {
            System.out.println("Error in retrieving RSS content for " + rssUrl + ": " + e.getMessage());
        }
    }
    public static String extractRssUrl(String url) throws IOException{
        Document doc = Jsoup.connect(url).get();
        return doc.select("[type='application/rss+xml']").attr("abs:href");
    }
    private static String fetchPageSource(String urlString) throws Exception {
        URI uri = new URI(urlString);
        URL url = uri.toURL();
        URLConnection urlConnection = url.openConnection();
        urlConnection.setRequestProperty("User-Agent"
                ,   "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML , like Gecko) Chrome/108.0.0.0 Safari/537.36");
        return toString(urlConnection.getInputStream());
    }
    private static String toString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream , "UTF-8"));
        String inputLine;
        StringBuilder stringBuilder = new StringBuilder();
        while ((inputLine = bufferedReader.readLine()) != null) stringBuilder.append(inputLine);
        return stringBuilder.toString();
    }
}
