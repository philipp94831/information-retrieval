
package de.hpi.ir.searchengine;

/**
 *
 * @author: Konstantina.Lazarid
 * @dataset: US patent grants : ipg files from http://www.google.com/googlebooks/uspto-patents-grants-text.html
 * @course: Information Retrieval and Web Search, Hasso-Plattner Institut, 2015
 *
 */
 
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;

/*
* use the function 'ArrayList <String> getGoogleRanking(String query)' to get the gold rankings from google.
* this function returns the patent titles that google printed for this query.
* the result set will be at most 100 US utility patents. You should only use the topK of them.
*/

public class WebFile {

    private java.util.Map<String, java.util.List<String>> responseHeader = null;
    private java.net.URL responseURL = null;
    private int responseCode = -1;
    private String MIMEtype = null;
    private String charset = null;
    private Object content = null;

    private boolean caching = false;
    private SearchEngine MyEngine;
    
    public void setEngine(SearchEngine CurrentEngine){
        MyEngine = CurrentEngine;
    }
    
    /**
     * Open a web file.
     *
     * @param urlString
     * @throws java.io.IOException
     * @throws java.net.UnknownServiceException
     * @throws java.net.SocketTimeoutException
     */
    public void openWebFile(String urlString)
            throws java.io.IOException, java.net.UnknownServiceException, java.net.SocketTimeoutException {
        // Open a URL connection.
        final java.net.URL url = new java.net.URL(urlString);
        final java.net.URLConnection uconn = url.openConnection();
        if (!(uconn instanceof java.net.HttpURLConnection)) {
            throw new java.lang.IllegalArgumentException(
                    "URL protocol must be HTTP.");
        }
        final java.net.HttpURLConnection conn
                = (java.net.HttpURLConnection) uconn;

        // Set up a request.
        conn.setConnectTimeout(10000);    // 10 sec
        conn.setReadTimeout(10000);       // 10 sec
        conn.setInstanceFollowRedirects(true);
        conn.setRequestProperty("User-agent", "ich");

        // Send the request.
        conn.connect();

        // Get the response.
        responseHeader = conn.getHeaderFields();
        responseCode = conn.getResponseCode();
        responseURL = conn.getURL();
        final int length = conn.getContentLength();
        final String type = conn.getContentType();
        if (type != null) {
            final String[] parts = type.split(";");
            MIMEtype = parts[0].trim();
            for (int i = 1; i < parts.length && charset == null; i++) {
                final String t = parts[i].trim();
                final int index = t.toLowerCase().indexOf("charset=");
                if (index != -1) {
                    charset = t.substring(index + 8);
                }
            }
        }

        // Get the content.
        final java.io.InputStream stream = conn.getErrorStream();
        if (stream != null) {
            content = readStream(length, stream);
        } else if ((content = conn.getContent()) != null
                && content instanceof java.io.InputStream) {
            content = readStream(length, (java.io.InputStream) content);
        }
        conn.disconnect();
    }

    /**
     * Read stream bytes and transcode.
     */
    @SuppressWarnings("empty-statement")
    private Object readStream(int length, java.io.InputStream stream)
            throws java.io.IOException {
        final int buflen = Math.max(1024, Math.max(length, stream.available()));
        byte[] buf = new byte[buflen];;
        byte[] bytes = null;

        for (int nRead = stream.read(buf); nRead != -1; nRead = stream.read(buf)) {
            if (bytes == null) {
                bytes = buf;
                buf = new byte[buflen];
                continue;
            }
            final byte[] newBytes = new byte[bytes.length + nRead];
            System.arraycopy(bytes, 0, newBytes, 0, bytes.length);
            System.arraycopy(buf, 0, newBytes, bytes.length, nRead);
            bytes = newBytes;
        }

        if (charset == null) {
            return new String(bytes);
        }
        try {
            return new String(bytes, charset);
        } catch (java.io.UnsupportedEncodingException e) {
        }
        return bytes;
    }

    /**
     * Get the content.
     *
     * @return
     */
    public Object getContent() {
        return content;
    }

    /**
     * Get the response code.
     *
     * @return
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * Get the response header.
     *
     * @return
     */
    public java.util.Map<String, java.util.List<String>> getHeaderFields() {
        return responseHeader;
    }

    /**
     * Get the URL of the received page.
     *
     * @return
     */
    public java.net.URL getURL() {
        return responseURL;
    }

    /**
     * Get the MIME type.
     *
     * @return
     */
    public String getMIMEType() {
        return MIMEtype;
    }

    // returns the patent titles from google patent search for a given query string
    // the titles returned will be at most 100
    ArrayList <String> getGoogleRanking(String query) {

        ArrayList <String> ranking = new ArrayList <>();
        int safeNumber = 100;  // to get enough US utility patents and exclude others
        try {
            // pose the query
            String queryTerms = query.replaceAll(" ", "+");
            String Queryurl = "https://www.google.com/search?hl=en&q=" + queryTerms + "&tbm=pts&num=" + safeNumber; // only English patents
            String page = "";
            openWebFile(Queryurl);
            page = (String) getContent();
            // get all patent urls returned from google (only the utility ones)
            LinkedHashMap <String, String> patents = new LinkedHashMap <>(); // key: patent url, value: patent title
            patternUrlCheck(page, patents);
            // get the titles
            for (Map.Entry <String, String> patent : patents.entrySet()) {
                String patentTitle = patent.getValue();
                String patentUrl = patent.getKey();
                // if short title
                if (patentTitle.contains("...")) {
                    openWebFile(patentUrl);
                    String patentText = (String) getContent();
                    // find the original title
                    String completeTitle = "";
                    completeTitle = patternTitleCheck(patentText);
                 //   System.out.print("completeTitle\t" + completeTitle + "\n");
                    ranking.add(completeTitle);
                } 
                else {
                 //   System.out.print("patentTitle\t" + patentTitle + "\n");
                    ranking.add(patentTitle);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(WebFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(caching) storeResults(ranking, query);
        return ranking;
    }
    
    public void patternUrlCheck(String page, LinkedHashMap <String, String> urls){
        
        Pattern pattern = Pattern.compile("<div><h3 class=\"r\">(.*?)</a></h3></div>");
        Matcher matcher = pattern.matcher(page);
        String textMatched = "";
        boolean utility = false;
        while (matcher.find()) {
            textMatched = matcher.group(1);
            Element link = Jsoup.parse(textMatched).select("a").first();
            String href = link.attr("href");
            String patentTitle = link.text();
            // only US utility patents
            utility = patternUtilityCheck(href);
            if (utility) urls.put(href, patentTitle);
        }
     //   System.out.print("\n" + urls.size() + " US utility patents found" + "\n");
    }
    
    public boolean patternUtilityCheck (String currentUrl){
        
        Pattern pattern = Pattern.compile("https://www.google.de/patents/US(.*?)?dq=");
        Matcher matcher = pattern.matcher(currentUrl);
        String patentSuffix = "";
        String regex = "\\d+"; // only digits
        boolean utility = false;
        while (matcher.find()) {
            // this is a US patent -> check if it is a utility patent
            patentSuffix = matcher.group(1).replaceAll("[^\\d.]", "");
            if(patentSuffix.matches(regex)) {
                utility = true;
                break;
            }
        }
        return utility;
    }
    
    public String patternTitleCheck (String patentText){
        Pattern pattern = Pattern.compile("<meta name=\"DC.title\" content=\"(.*?)\">");
        Matcher matcher = pattern.matcher(patentText);
        String completeTitle = "";
        while (matcher.find()) {
            completeTitle = matcher.group(1); // redirect to the patent's page and get the complete title
            break;
        }
        return completeTitle;
    }
 
    public void enableCaching(){
        caching = true;
    }
    
    @SuppressWarnings("ConvertToTryWithResources")
    public void storeResults(ArrayList <String> ranking, String query){
        
        try {
            ArrayList <String> excludedSymbols = new ArrayList <> ();
            excludedSymbols.add("\\"); excludedSymbols.add("/"); excludedSymbols.add(":"); excludedSymbols.add("*");
            excludedSymbols.add("?"); excludedSymbols.add("\""); excludedSymbols.add("<"); excludedSymbols.add(">"); excludedSymbols.add("|");
            for(String symbol: excludedSymbols){
                query.replaceAll(symbol, "");
            }
            PrintWriter writer = new PrintWriter(MyEngine.getResultsDirectory() + query.replaceAll(" ", "_") + ".txt", "UTF-8");
            for(String title: ranking){
                writer.write(title + "\n");
            }
            writer.close();
        } catch (FileNotFoundException | UnsupportedEncodingException ex) {
            Logger.getLogger(WebFile.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }
}
