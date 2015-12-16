package de.hpi.ir.yahoogle;

import java.io.IOException;
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
* use the function 'ArrayList <String> getGoogleRanking(String query)' to get the gold rankings from google for a given query and compute NDCG later on
* this function returns the patent titles that google returned for a given query
* the result set will be at most 100 US utility patents (int safeNumber)
*/
public class WebFile {

	private String charset = null;
	private Object content = null;
	private String MIMEtype = null;
	private int responseCode = -1;
	private java.util.Map<String, java.util.List<String>> responseHeader = null;
	private java.net.URL responseURL = null;

	/**
	 * Get the content.
	 *
	 * @return
	 */
	public Object getContent() {
		return content;
	}

	// returns the patent titles from google patent search engine for a given
	// query string
	// the titles returned will be at most 100
	public ArrayList<String> getGoogleRanking(String query) {
		ArrayList<String> ranking = new ArrayList<>();
		int safeNumber = 100; // to get enough US utility patents and exclude
								// others
		try {
			// issue the query
			String queryTerms = query.replaceAll(" ", "+");
			String queryUrl = "https://www.google.com/search?hl=en&q="
					+ queryTerms + "&tbm=pts&num=" + safeNumber; // only English
																	// patents
			String page = "";
			openWebFile(queryUrl);
			page = (String) getContent();
			// get all patent urls returned from google (only the utility ones)
			LinkedHashMap<String, String> patents = new LinkedHashMap<>(); // key:
																			// patent
																			// url,
																			// value:
																			// patent
																			// title
			patternUrlCheck(page, patents); // if utility, store it
			// get the titles
			for (Map.Entry<String, String> patent : patents.entrySet()) {
				String patentTitle = patent.getValue();
				String patentUrl = patent.getKey();
				// if short title
				if (patentTitle.contains("...")) {
					openWebFile(patentUrl);
					String patentText = (String) getContent();
					// find the original title
					String completeTitle = "";
					completeTitle = patternTitleCheck(patentText);
					ranking.add(completeTitle);
				}
				// if whole title
				else {
					ranking.add(patentTitle);
				}
			}
		} catch (IOException ex) {
			Logger.getLogger(WebFile.class.getName()).log(Level.SEVERE, null,
					ex);
		}
		return ranking;
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
	 * Get the MIME type.
	 *
	 * @return
	 */
	public String getMIMEType() {
		return MIMEtype;
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
	 * Get the URL of the received page.
	 *
	 * @return
	 */
	public java.net.URL getURL() {
		return responseURL;
	}

	/**
	 * Open a web file.
	 *
	 * @param urlString
	 * @throws java.io.IOException
	 * @throws java.net.UnknownServiceException
	 * @throws java.net.SocketTimeoutException
	 */
	public void openWebFile(String urlString) throws java.io.IOException, java.net.UnknownServiceException, java.net.SocketTimeoutException {
		// Open a URL connection.
		final java.net.URL url = new java.net.URL(urlString);
		final java.net.URLConnection uconn = url.openConnection();
		if (!(uconn instanceof java.net.HttpURLConnection)) {
			throw new java.lang.IllegalArgumentException(
					"URL protocol must be HTTP.");
		}
		final java.net.HttpURLConnection conn = (java.net.HttpURLConnection) uconn;
		// Set up a request.
		conn.setConnectTimeout(10000); // 10 sec
		conn.setReadTimeout(10000); // 10 sec
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

	public String patternTitleCheck(String patentText) {
		Pattern pattern = Pattern
				.compile("<meta name=\"DC.title\" content=\"(.*?)\">");
		Matcher matcher = pattern.matcher(patentText);
		String completeTitle = "";
		if (matcher.find()) {
			completeTitle = matcher.group(1); // redirect to the patent's page
												// and get the complete title
		}
		return completeTitle;
	}

	public void patternUrlCheck(String page, LinkedHashMap<String, String> urls) {
		Pattern pattern = Pattern
				.compile("<div><h3 class=\"r\">(.*?)</a></h3></div>");
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
			if (utility)
				urls.put(href, patentTitle);
		}
	}

	public boolean patternUtilityCheck(String currentUrl) {
		Pattern pattern = Pattern
				.compile("https://www.google.de/patents/US(.*?)?dq=");
		Matcher matcher = pattern.matcher(currentUrl);
		String patentSuffix = "";
		String regex = "\\d+"; // only digits
		boolean utility = false;
		while (matcher.find()) {
			// this is a US patent -> check if it is a utility patent too
			patentSuffix = matcher.group(1).replaceAll("[^\\d.]", "");
			if (patentSuffix.matches(regex)) {
				utility = true;
				break;
			}
		}
		return utility;
	}

	/**
	 * Read stream bytes and transcode.
	 */
	private Object readStream(int length, java.io.InputStream stream) throws java.io.IOException {
		final int buflen = Math.max(1024, Math.max(length, stream.available()));
		byte[] buf = new byte[buflen];
		;
		byte[] bytes = null;
		for (int nRead = stream.read(buf); nRead != -1; nRead = stream
				.read(buf)) {
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
}
