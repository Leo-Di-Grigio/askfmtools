package main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

class ProfileParser {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64)";
    
    private static final String CLASS_VIEW_MORE = "viewMore";
    private static final String CLASS_DATA_URL = "data-url";
    private static final String CLASS_ITEM_ANSWER = "streamItem-answer";
    private static final String CLASS_CONTENT_QUESTION = "streamItemContent-question";
    private static final String CLASS_CONTENT_ANSWER = "streamItemContent-answer";
    private static final String CLASS_CONTENT_LINK = "streamItemsAge";
    
    private static final String URL_ASK = "https://ask.fm/";
    private static final String URL_ASK_MORE_REQUEST = "https://ask.fm";

    public static HttpClient client;
    
    public ProfileParser() {
        client = HttpClientBuilder.create().build();
    }
    
    public ArrayList<Answer> parse(String userName){
        if(client != null){
            ArrayList<Answer> result = new ArrayList<Answer>();

            Document parsedData = Jsoup.parse(sendGet(client, URL_ASK + userName));
            
            ArrayList<Answer> data = processRequest(parsedData);
            if(data != null){
                result.addAll(data);
                data = null;
            }
            
            while(isNextRequest(parsedData)){         
                parsedData = Jsoup.parse(sendGet(client, processNextRequest(parsedData)));
                
                data = processRequest(parsedData);
                if(data != null){
                    result.addAll(data);
                    data = null;
                }
            }
            
            return result;
        }
        else{
            return null;
        }
    }

    public boolean checkUser(String username) {
        return true;
    }
    
    private String sendGet(HttpClient client, String url) {        
        HttpGet request = new HttpGet(url);
        request.addHeader("Host", "ask.fm");
        request.addHeader("Accept","*/*");
        request.addHeader("X-Requested-With","XMLHttpRequest");
        request.addHeader("Accept-Encoding","gzip, deflate, sdch");
        request.addHeader("Accept-Language","ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4");
        request.addHeader("User-Agent", USER_AGENT);
        request.addHeader("Connection", "keep-alive");

        try {
            HttpResponse response = client.execute(request);
            
            System.out.println("\nSending 'GET' request to URL : " + url);
            System.out.println("Response Code: " + response.getStatusLine().getStatusCode());
    
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            StringBuffer result = new StringBuffer();
            String line = null;
            
            while ((line = rd.readLine()) != null) {
                result.append(line);
                result.append("\n");
            }
            
            response.getEntity().getContent().close();
            return result.toString();
        }
        catch (ClientProtocolException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
    
    private boolean isNextRequest(Document parsedData) {
        Iterator<Element> iterator = parsedData.getElementsByClass(CLASS_VIEW_MORE).iterator();
        
        if(iterator.hasNext()){
            return true;
        }
        else{
            return false;
        }
    }

    private String processNextRequest(Document parsedData){
        Iterator<Element> iterator = parsedData.getElementsByClass(CLASS_VIEW_MORE).iterator();
        
        if(iterator.hasNext()){
            String result = URL_ASK_MORE_REQUEST + (iterator.next().attr(CLASS_DATA_URL).toString().replace("&amp;", "&"));
            System.out.println(result);
            return result;
        }
        else {
            return null;
        }
    }
    
    private ArrayList<Answer> processRequest(Document parsedData) {
        ArrayList<Answer> result = new ArrayList<Answer>();

        Iterator<Element> iterator = parsedData.getElementsByClass(CLASS_ITEM_ANSWER).iterator();
        while(iterator.hasNext()){
            Element element = iterator.next();
            
            Iterator<Element> iteratorQuestion = element.getElementsByClass(CLASS_CONTENT_QUESTION).iterator();
            Iterator<Element> iteratorAnswer = element.getElementsByClass(CLASS_CONTENT_ANSWER).iterator();
            Iterator<Element> iteratorMeta = element.getElementsByClass(CLASS_CONTENT_LINK).iterator();

            String textQuestion = "";
            String textAnswer = "";
            String textHref = "";
            
            while(iteratorQuestion.hasNext()){            
                textQuestion += iteratorQuestion.next().text() + "\r\n";
            }
            while(iteratorAnswer.hasNext()){
                textAnswer += iteratorAnswer.next().text() + "\r\n";
            }
            while(iteratorMeta.hasNext()){
                textHref = iteratorMeta.next().attr("href").toString();
            }
            
            result.add(new Answer(textQuestion, textAnswer, textHref));
        }
        
        if(result.isEmpty()){
            return null;
        }
        else{
            return result;
        }
    }
}