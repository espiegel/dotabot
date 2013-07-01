import static com.rosaloves.bitlyj.Bitly.as;
import static com.rosaloves.bitlyj.Bitly.shorten;

import com.rosaloves.bitlyj.Url;


public class LatestTopic implements java.io.Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6283228514094530475L;
	private String author, date, url, name;

	public LatestTopic() { author = date = url = name = null; }
	
	public LatestTopic(String author, String date, String url, String name)
	{
		this.author = author;
		this.date = date;
		this.url = url;
		this.name = name;
	}
	
	public String toString()
	{
		return "Author: "+author+", Date: "+date+", URL: "+url+", Name: "+name;
	}
	
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getFixUrl() {
		
		if(url.isEmpty())
			return "";
		
		String fixurl = "http://www.dotaisrael.co.il"+url.substring(1);
		Url nurl = as("zaknafein", "R_57db038e061b13e952fec891910f82d5").call(shorten(fixurl));			
		return nurl.getShortUrl();
	}
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
}
