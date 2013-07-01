import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jibble.pircbot.Colors;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class DotaBotMain
{
	public static List<LatestTopic> list = new ArrayList<LatestTopic>();
	private static String SAVEPATH = "topics.ser";
	private static String CHANNEL = "#dota2.il";
	private static long MESSAGE_DELAY = 5000;
	
	public static void main(String[] args) throws Exception
	{
		loadAll();
		
		String source = "EidanSp/dota";
		boolean findTopics = true;
		long curTime = System.currentTimeMillis();
		long refreshRate = 300000; // 300k ms is 5 minutes
		DotaBot bot = new DotaBot();
			
		connect(bot);
		
		//TwitterClient client = new TwitterClient(new TwitterFactory().getInstance(), bot);
		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true)
		  .setOAuthConsumerKey("GDUOSgzLQ3DR7SaBTLTHQ")
		  .setOAuthConsumerSecret("pFN939TmUWIqglBq0HVOZH1D41F1FkjZw3RGT11zaGE")
		  .setOAuthAccessToken("370878274-qnoOtbmKbgtWfMzjYuNGB7rmvAicOxXFEPiZvWc5")
		  .setOAuthAccessTokenSecret("m7xJ8Zw2IafsWSFuSLMo3JWg0wCL8oq59LlOAteq8");
		TwitterFactory tf = new TwitterFactory(cb.build());
		Twitter twitter = tf.getInstance();
		
		TwitterClient client = new TwitterClient(twitter, bot);
		
		while(true)
		{					
			if(findTopics)
			{
				try {
				getTopics(bot); }
				catch(Exception e)
				{
					bot.output(CHANNEL, "Couldn't open website");
					System.out.println("Couldn't open website");
					e.printStackTrace();
				}
				finally {
				findTopics = false;			
				}
			}
			
			if(Math.abs(System.currentTimeMillis() - curTime) > 10000)
				findTopics = true;
			

			client.getTimeline(source);
			TwitterUtils.sleep(refreshRate);
			
			if(!bot.isConnected()) {
				System.out.println("Bot isn't connected... attemping to reconnect");
				connect(bot);
			}
			boolean found = false;
			String[] channels = bot.getChannels();
			for(String channel : channels) {
				System.out.println(channel);
				if(channel.equalsIgnoreCase(CHANNEL)) {
					found = true;
					break;
				}
			}
			
			if(!found) {
				bot.joinChannel(CHANNEL);
			}
		}		
	}

	private static void connect(DotaBot bot) throws IOException, IrcException,
			NickAlreadyInUseException, UnsupportedEncodingException {
		if(bot == null)
			return;
		
		bot.setVerbose(true);
		bot.connect("se.quakenet.org");
		bot.joinChannel(CHANNEL);
		bot.setMessageDelay(1000);
		bot.setEncoding("UTF8");
	}
	
	public static void getTopics(DotaBot bot) throws Exception
	{				
		boolean found;
		System.out.println("getting topics...");
		String url = "http://www.dotaisrael.co.il/newtopic.php";
		
		// Default timeout for connect is 3 seconds
		Connection con = Jsoup.connect(url);
		con.timeout(10000); // give it 10 seconds
		Document doc = con.get();
		
		String text = doc.body().text();
		
		String split[] = text.split("---");
		
		int i=0;
		String author = "";
		String date = "";
		String turl = "";
		String name = "";
		for(String s : split)
		{			
			if(i % 4 == 0)
				author = s;
			else if(i % 4 == 1)
				date = s;
			else if(i % 4 == 2)
			{
				turl = s;
			}
			else
			{
				name = s;
				LatestTopic topic = new LatestTopic(author, date, turl, name);
				
				found = false;
				
				for(LatestTopic t : list)
				{
					String curname = t.getName();
					String cururl = t.getUrl();
					
					if(turl.indexOf("sid=") == -1 || cururl.indexOf("sid=") == -1)
					{
						if(curname.equals(name) && t.getAuthor().equals(author) && t.getDate().equals(date))
							found = true;
					}
					else
					{
						String turl1 = turl.substring(0,turl.indexOf("sid=")-1) + turl.substring(turl.indexOf("#"));
						String turl2 = cururl.substring(0,cururl.indexOf("sid=")-1) + cururl.substring(cururl.indexOf("#"));
					
						if(turl1.equals(turl2))
							found = true;
					}
				}
				
				if(!found)
				{
					list.add(topic);
					System.out.println("New Topic! "+topic);
					
					bot.output(CHANNEL, "New message was posted by: "+Colors.BOLD+topic.getAuthor()+Colors.NORMAL+", "+topic.getFixUrl());
					Thread.sleep(MESSAGE_DELAY );
				}
			}
			
			i++;
		}
		  
	}
	
	public static void saveAll()
	{
		try {
			FileOutputStream saveFile = new FileOutputStream(SAVEPATH);		
			ObjectOutputStream save = new ObjectOutputStream(saveFile);
			
			int size = list.size();
			save.writeObject(size);
			
			Iterator<LatestTopic> i = list.iterator();
			while(i.hasNext())
			{
				LatestTopic t = i.next();
				save.writeObject(t);
			}
			
			save.close();
			saveFile.close();
		}
		catch(Exception exc) {
			exc.printStackTrace();
		}
	}
	
	public static void loadAll()
	{
		try {
			FileInputStream saveFile = new FileInputStream(SAVEPATH);			
			ObjectInputStream save = new ObjectInputStream(saveFile);
			
			int size = (Integer) save.readObject();
			for(int i=0;i<size;i++)
			{
				LatestTopic t = (LatestTopic) save.readObject();
				list.add(t);
			}
			
			save.close();	
			saveFile.close();
			System.out.println("Loaded data successfully...");
		}
		catch(Exception exc) {
			exc.printStackTrace();
		}
	}
}