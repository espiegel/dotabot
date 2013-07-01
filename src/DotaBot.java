import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StringReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.jibble.pircbot.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.RateLimitStatus;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class DotaBot extends PircBot implements IStatusListener
{
	public static final long IRC_POST_INTERVAL = 3000;
    public static final int API_LIMIT_WARN_THRESHOLD = 45;
    
	private String SAVEPATH = "data.ser";
	private String CHANNEL = "#dota2.il";
	//private String CHANNEL2 = "#dota2.israel";
	private String MYNAME = "Rooftrellen"; // Changed from JinzakkBatrider
	private String hero = "Tree";
	
	private final int SPAWN = 0;
	private final int DEATH = 1;	
	private final int BOTDEATH = 2;
	private final int CHAT = 3;
	
	private final int PLAYERSPERLINE = 10;
	
	List<String> spawn = new ArrayList<String>();
	List<String> chat = new ArrayList<String>();
	List<String> death = new ArrayList<String>();
	List<String> botdeath = new ArrayList<String>();
	
	List<Game> games = new ArrayList<Game>();
	List<Player> players = new ArrayList<Player>();
	
	
	public DotaBot()
	{
		setName(MYNAME);
		setVersion("0.1 alpha - http://www.dotaisrael.co.il");
		setLogin(MYNAME);
		loadAll();
		fillResponses();
		
		
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    long factor = (long) Math.pow(10, places);
	    value = value * factor;
	    long tmp = Math.round(value);
	    return (double) tmp / factor;
	}
	
	// Twitter Functionality
	@Override
	public void onReceiveStatuses(ResponseList<Status> list)
	{
		Collections.reverse(list);
		RateLimitStatus limit = list.getRateLimitStatus();
		if (limit.getRemainingHits() < API_LIMIT_WARN_THRESHOLD)
		{
			String message = new StringBuilder()
					.append("WARN: API limit is ")
					.append(limit.getRemainingHits())
					.append(" remains. Reset time is ")
					.append(DateFormat.getTimeInstance(DateFormat.SHORT)
							//.format(limit.getResetTime())).append(" .")
							.format(limit.getResetTimeInSeconds())).append(" .")
					.toString();
			System.err.println(message);
			for (String ch : getChannels())
			{
				sendMessage(ch, message);
				TwitterUtils.sleep(IRC_POST_INTERVAL);
			}
		}

		for (Status status : list)
		{
			String message = new StringBuilder().append("\u001FTwitter:\u000F ")
					.append("\u0002").append(status.getUser().getScreenName()).append("\u000F - ")
					.append(status.getText().replace('\n', ' ')).toString();
			System.err.println(status.getCreatedAt() + " | " + message);
			for (String ch : getChannels())
			{
				output(ch, message);
				TwitterUtils.sleep(IRC_POST_INTERVAL);
			}
		}
	}
	
	@Override
	public void onCaughtTwitterException(TwitterException e)
	{
		BufferedReader reader = new BufferedReader(new StringReader(
				e.getMessage()));
		String message = "";
		try {
			message = new StringBuilder().append(reader.readLine()).toString();
			reader.close();
		} catch (IOException e1) { }
		System.err.println(message);
		for (String ch : getChannels())
		{
			output(ch, "Twitter API error.");
			TwitterUtils.sleep(IRC_POST_INTERVAL);
		}
	}
	
	// 0 = spawn. 1 = death. 2 = botdeath. 3 = chat.
	public String getRandom(int num)
	{
		Random random = new Random();	
		int index;
		
		if(num == SPAWN)
		{
			index = random.nextInt(spawn.size());		
			return spawn.get(index);
		}
		if(num == DEATH)
		{
			index = random.nextInt(death.size());		
			return death.get(index);
		}
		if(num == BOTDEATH)
		{
			index = random.nextInt(botdeath.size());
			return botdeath.get(index);
		}
		
		// default is CHAT
		index = random.nextInt(chat.size());		
		return chat.get(index);	
	}
	
	private void fillResponses()
	{
		for (int i = 0; i <= 3; i++)
		{
			try {
				// Open the file that is the first
				// command line parameter
				String filePath = hero;
				switch(i)
				{
					case SPAWN: filePath += "Spawn.txt"; break;
					case DEATH: filePath += "Death.txt"; break;
					case BOTDEATH: filePath += "BotDeath.txt"; break;
					case CHAT: filePath += "Chat.txt"; break;
					default: filePath += "Chat.txt"; break;
				}
				FileInputStream fstream = new FileInputStream(filePath);
				// Get the object of DataInputStream
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				String strLine;
				// Read File Line By Line
				while ((strLine = br.readLine()) != null) {
					switch(i)
					{
						case SPAWN: spawn.add(strLine); break;
						case DEATH: death.add(strLine); break;
						case BOTDEATH: botdeath.add(strLine); break;
						case CHAT: chat.add(strLine); break;
						default: chat.add(strLine); break;
					}
				}
				// Close the input stream
				in.close();
			} catch (Exception e) {// Catch exception if any
				System.err.println("Error: " + e.getMessage());
			}
		}
		
		if(hero.equals("Storm"))
		{
			spawn.add("I am Storm Spirit!");
			spawn.add("Storm Spirit has arrived!");
			spawn.add("Ha ha! I am here!");
			spawn.add("Feel the wind in your hair!");
			spawn.add("Stormy weather...");
			spawn.add("The calm before the storm...");
			spawn.add("Storm Spirit!");
			spawn.add("Ah, that first breath is the sweetest.");
			spawn.add("Yes! Again!");
			spawn.add("Well blow me down...");
			spawn.add("Breath of fresh air!");
			spawn.add("Well, I've caught my breath...back to it!");
			spawn.add("I needed a break anyway.");
			spawn.add("In the eye of the storm...");
			
			chat.add("Coming through!");
			chat.add("I'm in a hurry-cane!");
			chat.add("Whoa, what'd I miss?");
			chat.add("Where's the party?");
			chat.add("Lookin for me?");
			chat.add("Let the fun begin!");
			chat.add("I'm over here!");
			chat.add("Over here now!");
			chat.add("Here I am!");
			chat.add("Touche");
			chat.add("Zap!");
			chat.add("Puddin' pop!");
			chat.add("Ooh, who's that handsome devil?");
			chat.add("Blow them away!");
			chat.add("Stormy weather time!");
			chat.add("Get set for heavy weather!");
			chat.add("Storm warning!");
			chat.add("Storm Force Five!");
			chat.add("I love this part!");
			chat.add("Storm's a-comin!");
			chat.add("That's the spirit!");
			chat.add("I'll spirit you away!");
			chat.add("The spirit of the storm increases!");
			
			death.add("You had your warning!");
			death.add("You blew it!");
			death.add("Told you a storm was coming!");
			death.add("Blown away!");
			death.add("Blow the man down!");
			death.add("No whining now!");
			death.add("There's no safe harbor from this storm, Kunkka!");
			death.add("Ha ha! You should have put to port sooner!");
			death.add("Admiral, but not admirable. Hm hm hm hm.");
			death.add("Call off your little storms, Razor. They're an embarrassment.");
			death.add("You can come out of that tin can any time.");
			death.add("If you think to harness nature's power, you've got another think coming.");
			death.add("The biggest tree in the forest must bow to the smallest storm.");
			death.add("Oh, a windfall!");
			death.add("When you strip away his leaves, Nature's Prophet is just plain gnarly.");
			death.add("Ha ha! Blew you off course, did I?");
			death.add("Your whirlybird is at the mercy of the wind, and I'm not feeling very merciful today.");
			death.add("You needed an altitude adjustment, hm hm.");
			death.add("You shouldn't have spit into the wind, Viper.");
			death.add("Ha ha ha ha. Your maidenly charms were wasted on me.");
			death.add("Venomancer, I was never a fan.");
			death.add("Antimage, you're no fun at all.");
			death.add("Not a fan, Antimage?");
			death.add("That grimoire of his got terrible reviews.");
			death.add("Mm, Spirit, Apparition�they're nothing alike.");
			death.add("Pity, Puck. You lightened the air");
			
		    botdeath.add("No!");
			botdeath.add("Can't breathe!");
			botdeath.add("I blew it...");
		    botdeath.add("Lights out...");
			botdeath.add("My spirit...sinks...");
			botdeath.add("Ex...hausted...");
			botdeath.add("I'll fight to the last...breath...");
			botdeath.add("Spirit...away...");
			botdeath.add("Spirit...away...");
		}
		
		if(hero.equals("Tidehunter")) // Default is tide
		{
			spawn.add("I hunt again.");
			spawn.add("The tide is rolling out.");
			spawn.add("Time and Tidehunter wait for no man.");
			spawn.add("Time to make waves.");
			spawn.add("The hunt begins.");
			spawn.add("I rise again from the deeps.");
			spawn.add("In with the tide.");
			spawn.add("The red tide comes.");
			spawn.add("My skin breathes again.");
			spawn.add("Twas but a shallow sleep.");
			spawn.add("Fresh is good�unless we're talking water.");
			spawn.add("Got to keep moving, or I suffocate.");
			spawn.add("Look what the tide washed in.");
			 
			chat.add("Leave the depths to the treacherous Meranths, I can find all I need in the shallows.");
			chat.add("By Maelrawn the Tentacular, the Lurker in the Whirlpool, may my enemies be sucked down in spirals!");
			chat.add("Who needs a big brain when you've got teeth like mine?");
			chat.add("I'm hungry enough to eat a plankton.");
			chat.add("I could eat a diatom.");
			chat.add("I could eat a krill.");
			chat.add("I could eat an anchovy.");
			chat.add("I could eat a sea cucumber.");
			chat.add("I could eat a seagrape.");
			chat.add("I could eat a seahorse.");
			chat.add("I could eat a nautilus.");
			chat.add("I could eat an eel.");
			chat.add("I could eat a jellyfish.");
			chat.add("I could eat a mullet.");
			chat.add("I'm so hungry I could eat a mackerel.");
			chat.add("I could eat a giant isopod.");
			chat.add("I could eat a tubeworm.");
			chat.add("I could eat a porpoise.");
			chat.add("I could eat a manatee.");
			chat.add("I could eat an oarfish.");
			chat.add("Ha ha! I could eat a walrus.");
			chat.add("I'm hungry enough to eat a manta ray.");
			chat.add("I could eat a shark.");
			chat.add("I could eat a whale shark.");
			chat.add("I could eat a blue whale.");
			chat.add("I could eat a colossal squid.");
			chat.add("I could eat a plesiosaur.");
			chat.add("Ha ha ha! Women and children and Kunkka first.");
			chat.add("Ravaged!");
			chat.add("You look ravaged.");
			chat.add("Oh, the ravages of time.");
			chat.add("You've got guts.");
			chat.add("Think of it as caviar.");
			chat.add("Amphibious assault!");
			chat.add("You swim against the tide.");
			chat.add("You can't hide from the tide.");
			
			death.add("The admirable Admiral dies like a dog!");
			death.add("Die in slime, Admiral Mouthbreather!");
			death.add("Where's your fleet now, Kunkka?");
			death.add("Die in slime, Kunkka!");
			death.add("The admirable Kunkka dies like a dog!");
			death.add("I always pitied you, Morphling, for having to carry Kunkka's ships.");
			death.add("Alas poor Morphling, so full of tasty fish.");
			death.add("Ah, sweet Siren, if only you'd steered Kunkka onto the rocks.");
			death.add("You should put your fire to work burning up Kunkka's ships.");
			death.add("Why fire your arrows at me when Kunkka's sails need burning?");
			death.add("Why fight me, Slardar? Together we could have brought Kunkka to the bottom.");
			death.add("You could have given Kunkka some pointers in how to die nobly.");
			death.add("You were a commanding presence, which is more than I can say for Kunkka.");
			death.add("Puck, you taste as bad as Kunkka's boot.");
			death.add("Take a bath, Axe. You smell worse than Kunkka!");
			death.add("Your metal jacket sank you faster than one of Kunkka's ships.");
			death.add("You went down like Kunkka's ship.");
			death.add("Next time, Weaver, let Kunkka give you lessons in scuttling.");
			death.add("He abandoned this plane faster than Kunkka abandoned his fleet.");
			death.add("Did you think I'd blunder? You must have mistaken me for Kunkka.");
			
			botdeath.add("I'll have my vengeance yet, Kunkka!");
			botdeath.add("Kunkkaaaaaaa!");
			botdeath.add("An anchor around my neck!");
			botdeath.add("My body, cast upon the tides.");
			botdeath.add("I sleep with the fishes.");
			botdeath.add("To the bottom I go!");
		}
	}
	
	protected void onDisconnect()
	{
		saveAll();
		output(CHANNEL, getRandom(BOTDEATH));	
		//output(CHANNEL2, getRandom(BOTDEATH));
	}
	
	protected void onJoin(String channel,
            String sender,
            String login,
            String hostname)
	{
		output(channel, sender+" joins us. "+getRandom(SPAWN));
		sendNotice(sender, "Welcome "+sender+"! I am "+MYNAME+". Type !help to see how I can help you.");
	}
		
	protected void onPart(String channel, String sender, String login, String hostname)
	{
		if(login.equals(MYNAME))
			output(channel, getRandom(BOTDEATH));
		else
			output(channel, getRandom(DEATH));
	}
	
	// Parses all outgoing output from the bot
	public void output(String channel, String msg)
	{
		sendMessage(channel, Colors.RED+"["+Colors.BLUE+msg+Colors.RED+"]");
	}
	
	// Returns Player object from the Player list
	private Player getPlayer(String channel, String name)
	{
		Iterator<Player> ite = players.iterator();
		
		while(ite.hasNext())
		{
			Player p = ite.next();
			if(p.getName().equals(name))
				return p;			
		}
		
		return null;
	}
	
	private boolean checkOp(String channel, String name)
	{
		boolean op = false;
		User[] users = getUsers(channel);
		
		for(int i=0;i<users.length;i++)
		{
			if(users[i].getNick().equals(name))
			{		
				if(users[i].isOp())
					op = true;
			}
		}
		
		return op;
	}
	
	private boolean userInChannel(String channel, String name)
	{
		User[] users = getUsers(channel);
		
		for(int i=0;i<users.length;i++)
			if(users[i].getNick().equals(name))
				return true;
		
		return false;
	}
	
	private Player forceRemovePlayer(String channel, String name)
	{		
		Player p = getPlayer(channel, name);
		if(p == null)
			return null;
		
		leaveGame(channel, name);
		players.remove(p);
		output(channel, "Removing player "+name+" from the database...");
		return p;
	}
	
	private Player forceCreatePlayer(String channel, String name)
	{		
		output(channel, "Adding new player "+name+" to database...");
		Player newPlayer = new Player(name);
			
		players.add(newPlayer);
		return newPlayer;
	}
	
	private Player createPlayer(String channel, String name)
	{		
		if(!userInChannel(channel, name))
		{
			output(channel, "Player "+name+" not found in the channel.");
			return null;
		}
				
		output(channel, "Adding new player "+name+" to database...");
		Player newPlayer = new Player(name);
			
		players.add(newPlayer);
		return newPlayer;
	}
	
	private Game getGame(String channel, String name)
	{
		Iterator<Game> ite = games.iterator();
		
		while(ite.hasNext())
		{
			Game g = ite.next();
			if(g.getName().equals(name))
				return g;			
		}
		
		return null;
	}
	
	private void saveAll()
	{
		DotaBotMain.saveAll();
		
		try {
			FileOutputStream saveFile = new FileOutputStream(SAVEPATH);		
			ObjectOutputStream save = new ObjectOutputStream(saveFile);
			
			int size = players.size();
			save.writeObject(size);
			
			Iterator<Player> i = players.iterator();
			while(i.hasNext()) //save.writeObject(players);
			{
				Player p = i.next();
				save.writeObject(p);
			}
			
			save.close();
			saveFile.close();
			//output(channel, getRandom(CHAT));
		}
		catch(Exception exc) {
			exc.printStackTrace();
			output(CHANNEL, "Error saving data...");
			//output(CHANNEL2, "Error saving data...");
		}
	}
	
	private void loadAll()
	{
		try {
			FileInputStream saveFile = new FileInputStream(SAVEPATH);			
			ObjectInputStream save = new ObjectInputStream(saveFile);
			
			//players = (ArrayList<Player>) save.readObject();
			int size = (Integer) save.readObject();
			for(int i=0;i<size;i++)
			{
				Player p = (Player) save.readObject();
				players.add(p);
			}
			
			save.close();	
			saveFile.close();
			output(CHANNEL, "Loaded data successfully...");
			//output(CHANNEL2, "Loaded data successfully...");
		}
		catch(Exception exc) {
			output(CHANNEL, "Error loading data...");
			//output(CHANNEL2, "Error loading data...");
		}
	}
	
	// Returns Player object and creates one if necessary
	private Player getP(String channel, String sender)
	{
		Player p = getPlayer(channel, sender);
		if(p == null)
			p = createPlayer(channel, sender);
		
		return p;
	}
	
	public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason)
	{		
		/*
		Player p = getPlayer(CHANNEL, sourceNick);
		if(p == null)
			return;
		
		Game g = p.getOwned();
		if(g != null)
			g.abortGame();
		
		g = p.getGame();
		if(g != null)
			g.removePlayer(p);
		
		p.setGame(null);
		p.setSide(null);
		*/
		leaveGame(CHANNEL, sourceNick);
		saveAll();
		if(sourceLogin.equals(MYNAME)) {
			output(CHANNEL, getRandom(BOTDEATH));
			//output(CHANNEL2, getRandom(BOTDEATH));
		}
		else {
			if(userInChannel(CHANNEL, sourceLogin))
				output(CHANNEL, getRandom(DEATH));
			//if(userInChannel(CHANNEL2, sourceLogin))
			//	output(CHANNEL2, getRandom(DEATH));
		}
	}
	
	/*
	private void printStats(String channel, Player p)
	{
		output(channel, p.getName()+"'s Wins: "+p.getWins()+", Losses: "+p.getLosses()+", W/L Ratio: "+p.getRatio()+".");
		if(p.getOwned() != null)
			output(channel, "Opened the game: "+p.getOwned().getName());
		if(p.getGame() != null)
			output(channel, "Is currently in the game: "+p.getGame().getName()+". "+((p.getSide() != null)?("Side: "+p.getSide()):""));
	}
	*/
	
	private void printStatsTarget(String channel, Player p, String target)
	{
		sendNotice(target, p.getName()+"'s Wins: "+p.getWins()+", Losses: "+p.getLosses()+", W/L Ratio: "+p.getRatio()+".");
		if(p.getOwned() != null)
			sendNotice(target, "Opened the game: "+p.getOwned().getName());
		if(p.getGame() != null)
			sendNotice(target, "Is currently in the game: "+p.getGame().getName()+". "+((p.getSide() != null)?("Side: "+p.getSide()):""));
	}
	
	private void printPlayers(String channel, String target)
	{
		String message = "\u001F\u0002Players: \u000F ";;
		Collections.sort(players, new Comparator<Player>() {

			@Override
			public int compare(Player a, Player b) {
				// TODO Auto-generated method stub
				return a.getName().compareToIgnoreCase(b.getName());
			}
			
		});
	
		int i=1;
		for(Player p : players)
		{
			message = message + p.getName() + ", ";
			i++;
			
			if(i >= PLAYERSPERLINE)
			{
				message = message.substring(0, message.length()-1);
				sendNotice(target, message);
				message = "";
				i = 1;
			}
		}
		
		if(message.length() > 0)
			message = message.substring(0, message.length()-2);
		
		sendNotice(target, message);
	}
	
	private void printTopTen(String channel, String target)
	{
		Collections.sort(players, new Comparator<Player>() {

			@Override
			public int compare(Player a, Player b) {
				// TODO Auto-generated method stub
				if(a.getWins() > b.getWins())
					return -1;
				else if(a.getWins() < b.getWins())
					return 1;
				else if(a.getRatio() > b.getRatio())
					return -1;
				else if(a.getRatio() < b.getRatio())
					return 1;
				return 0;
			}
		
		});
		
		String message = "\u001F\u0002Top 10:\u000F ";
		int i = 1;
		for(Player p: players)
		{
			if(i > 10)
				break;
			
			message = message + i+") \u0002"+p.getName()+"\u000F W: "+p.getWins()+" L: "+p.getLosses()+" WR: "+round(p.getRatio()*100,2)+"% ";				
			i++;
		}		
		
		sendNotice(target, message);		
	}
	
	private void leaveGame(String channel, String sender)
	{
		Player p = getPlayer(channel, sender);
		if(p == null) return;
		
		if(p.getGame() == null)
			return;
		
		Game g = p.getGame();
		if(g.getOwner().equals(p))
		{
			output(channel, p.getName()+" aborted his game, "+g.getName());
			abortGame(channel, g);
			return;
		}
		
		g.removePlayer(p);
		p.setGame(null);
		p.setSide(null);
	}
	
	private void abortGame(String channel, Game g)
	{
		g.abortGame();
		games.remove(g);
		return;
	}
	
	public void onMessage(String channel, String sender, String login,
			String hostname, String  message)
	{
		if( (int)(Math.random() * (500)) == 0)
			saveAll();
		
		if(message.equalsIgnoreCase("!time"))
		{
			String time = new java.util.Date().toString();
			output(channel, sender + ": The time is now " + time);
			return;
		}
		
		if(message.equalsIgnoreCase("!chat"))
		{
			output(channel, getRandom(CHAT));
		}
				
		if(message.startsWith("!report "))
		{
			Player p = getP(channel, sender);
			if(p == null)
				return;
			
			Game g = p.getGame();
			if(g == null)
				return;
			
			if(g.getStatus() != 1)
				return;
			
			if(!g.getOwner().equals(p))
				return;
				
			String[] split = message.split(" ");
			if(split.length != 2)
				return;
			
			String side = split[1];
			side = side.toLowerCase();
			if(!(side.equals("dire") || side.equals("radiant")))
				return;
			
			output(channel, "The "+side+" has won in game "+g.getName()+"!");
			for(Player q : g.getDire())
			{
				if(side.equals("dire"))
					q.incWins();
				else
					q.incLosses();
			}
			for(Player q : g.getRadiant())
			{
				if(side.equals("radiant"))
					q.incWins();
				else
					q.incLosses();
			}
			
			g.abortGame();
		}
		
		if(message.startsWith("!side"))
		{
			Player p = getP(channel, sender);
			if(p == null)
				return;
				
			if(p.getGame() == null)
				return;
			
			String[] split = message.split(" ");
			if(split.length != 2)
				return;
			
			String side = split[1];
			side = side.toLowerCase();
			if(!(side.equals("dire") || side.equals("radiant") || side.equals("random")))
				return;
			
			if(side.equals("random"))
			{
				double rand = Math.random();
				
				if(rand <= 0.5)
					side = "dire";
				else
					side = "radiant";
			}
			
			if( (side.equals("dire") && p.getGame().getDire().size() == 5) ||
				(side.equals("radiant") && p.getGame().getRadiant().size() == 5))
			{
				output(channel, p.getName()+", there are already 5 players on the "+side+" in game "+p.getGame().getName());
				return;
			}
			
			p.setSide(side);
			if(side.equals("dire") && !p.getGame().getDire().contains(p))
				p.getGame().addDire(p);
			if(side.equals("radiant") && !p.getGame().getRadiant().contains(p))
				p.getGame().addRadiant(p);
			
			output(channel, p.getName()+" has joined the "+side+" in game "+p.getGame().getName());
		}
		
		if(message.equals("!allusers"))
		{		
			printPlayers(channel, sender);
			return;
		}
		
		if(message.equals("!top10"))
		{		
			printTopTen(channel, sender);
			return;
		}
		
		if(message.equals("!stats"))
		{
			Player p = getP(channel, sender);
			if(p == null) return;
			
			printStatsTarget(channel, p, sender);
			return;
		}
		
		if(message.startsWith("!stats "))
		{
			String name = message.substring(7);
			Player p = getPlayer(channel, name);			
			if(p == null)
			{
				sendNotice(sender, "Player "+name+" doesn't exist in the database!");
				return;
			}
			printStatsTarget(channel, p, sender);
			return;
		}
		
		if(message.startsWith("!addwin "))
		{
			if(!checkOp(channel, sender))
				return;
			
			String[] split = message.split(" ");
			if(split.length != 2)
				return;
			
			String name = split[1];
			Player p = getPlayer(channel, name);			
			if(p == null)
			{
				sendNotice(sender, "Player "+name+" doesn't exist in the database!");
				return;
			}

			p.incWins();			
			printStatsTarget(channel, p, sender);
			return;
		}
		
		if(message.startsWith("!addloss "))
		{
			if(!checkOp(channel, sender))
				return;
			
			String[] split = message.split(" ");
			if(split.length != 2)
				return;
			
			String name = split[1];
			Player p = getPlayer(channel, name);			
			if(p == null)
			{
				sendNotice(sender, "Player "+name+" doesn't exist in the database!");
				return;
			}

			p.incLosses();		
			printStatsTarget(channel, p, sender);
			return;
		}
		
		if(message.startsWith("!addplayer "))
		{
			if(!checkOp(channel, sender))
				return;
			
			String[] split = message.split(" ");
			if(split.length != 2)
				return;
			
			String name = split[1];
			
			forceCreatePlayer(channel, name);
			output(channel, sender+" has added "+name+" to the player database.");
			return;
		}
		
		if(message.startsWith("!removeplayer "))
		{
			if(!checkOp(channel, sender))
				return;
			
			String[] split = message.split(" ");
			if(split.length != 2)
				return;
			
			String name = split[1];
			
			forceRemovePlayer(channel, name);
			output(channel, sender+" has removed "+name+" from the player database.");
			return;
		}
		
		if(message.equals("!save"))
		{
			saveAll();
		}
			
		if(message.equals("!die"))
		{
			if(!checkOp(channel,sender))
				return;
			
			for(Game g : games)
				g.abortGame();
			
			saveAll();			
			output(channel, getRandom(BOTDEATH));
			try {
			Thread.sleep(2000);
			}
			catch(Exception ex)
			{
				output(channel, "I'm not sleepy!");
				ex.printStackTrace();
			}
			
			this.disconnect();
			System.exit(0);
		}
		
		if(message.startsWith("!setwins "))
		{
			if(!checkOp(channel,sender))
				return;
			
			String[] split = message.split(" ");
			if(split.length != 3)
				return;
			
			String name = split[1];
			int wins = Integer.parseInt(split[2]);

			Player p = getP(channel, name);
			if(p == null) return;
			
			p.setWins(wins);
			output(channel, p.getName()+"'s wins are now set to "+wins+".");
			return;
		}
		
		if(message.startsWith("!setlosses "))
		{
			if(!checkOp(channel,sender))
				return;
			
			String[] split = message.split(" ");
			if(split.length != 3)
				return;
			
			String name = split[1];
			int losses = Integer.parseInt(split[2]);

			Player p = getP(channel, name);
			if(p == null) return;
			
			p.setLosses(losses);
			output(channel, p.getName()+"'s losses are now set to "+losses+".");
			return;
		}
		
		if(message.startsWith("!new "))
		{
			String gameName;
			gameName = message.substring(5);
			
			Game g = getGame(channel, gameName);
					
			Player p = getP(channel, sender);
			if(p == null) return;
			
			if(g != null)
			{
				output(channel, "A game with the name '"+gameName+"' is already open!");
				return;
			}
			if(p.getOwned() != null)
			{
				output(channel, p.getName()+", I can't open a game for you. You already opened "+p.getOwned().getName());
				return;
			}
			if(p.getGame() != null)
			{
				output(channel, p.getName()+", I can't open a game for you. You are already in the game "+p.getGame().getName());
				return;
			}
			else
			{				
				g = new Game(gameName, p);
				games.add(g);
				output(channel, "A new game has been opened: "+gameName+" (0/10)");
			}			
		}
		
		if(message.equals("!list"))
		{
			if(games.size() == 0)
			{
				output(channel, "No games are currently open.");
				return;
			}
			
			output(channel, "List of current open games: ");		
			Iterator<Game> ite = games.iterator();
			
			while(ite.hasNext())
			{
				Game g = ite.next();
				output(channel, g.getName()+" ("+g.getNumPlayers()+"/10) : Status - "+ ((g.getStatus()==0)?"Not Started":"Started"));		
			}
		}
		
		if(message.startsWith("!list "))
		{
			Game g = getGame(channel, message.substring(6));
			
			if(g == null)
			{
				output(channel, "No such game open!");
				return;
			}
			
			output(channel, "("+g.getNumPlayers()+"/10) players in game: "+g.getName());
			output(channel, g.listPlayers());
		}
		
		if(message.startsWith("!join "))
		{
			String gameName = message.substring(6);
			Game g = getGame(channel, gameName);
			
			if(g == null)
			{
				output(channel, "No such game open!");
				return;
			}
			
			if(g.getStatus() == 1)
			{
				output(channel, "That game has already started!");
				return;
			}
			
			Player p = getP(channel, sender);
			if(p == null) return;
			
			if(p.getGame() != null)
			{
				output(channel, sender+", you are already in a game! ("+p.getGame().getName()+")");
				return;
			}
			
			g.addPlayer(p);
			p.setGame(g);
			p.setSide(null);
			output(channel, sender+" has joined game "+g.getName()+". Game now has ("+g.getNumPlayers()+"/10) players.");
			output(channel, sender+", choose your side: !side [radiant/dire/random]");
			if(g.getNumPlayers() == 10)
				output(channel, g.getName()+" has started!");
		}
		
		if(message.equals("!leave"))
		{
			leaveGame(channel, sender);
		}
		
		if(message.equals("!showtopics"))
		{
			if(!checkOp(channel,sender))
				return;
			
			List<LatestTopic> list = DotaBotMain.list;
			
			int i=1;
			for(LatestTopic t : list)
			{
				System.out.println(i+") "+t.toString());
				i++;
			}
		}
		
		if(message.startsWith("!abort "))
		{
			String[] split = message.split(" ");
			if(split.length != 2)
				return;
			
			String name = split[1];
			Game g = null;
			
			for(Game x : games)
			{
				if(x.getName().equals(name))
				{
					g = x;
					break;
				}
			}
			
			if(g == null)
				return;
			
			if(g != getP(channel, sender).getOwned() || !checkOp(channel,sender))
				return;
			
			output(channel, g.getName()+" aborted.");
			abortGame(channel, g);			
		}
		
		if(message.startsWith("!tweet "))
		{
			if(!checkOp(channel, sender))
				return;
			
			String[] split = message.split(" ");
			if(split.length < 2)
				return;
			
			String tweet = message.substring(split[0].length()+1);
			
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true)
			  .setOAuthConsumerKey("AxuEvoY52lmRpgox76PkkA")
			  .setOAuthConsumerSecret("LNTe27w3sKWLavxc4Qcp81snYTetbcElgWzbunecE")
			  .setOAuthAccessToken("894204871-p7s5Ufqf8Tu8nF6HkpUl6oMP2WcA3UL62HiRzkWk")
			  .setOAuthAccessTokenSecret("vEbUGKROBOxkbl9iVHmOmX24yIQTy8PFP156HhMc");
			TwitterFactory tf = new TwitterFactory(cb.build());
			Twitter twitter = tf.getInstance();

			try {
				twitter.updateStatus(tweet);
			} catch (TwitterException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
				
		}
		
		if(message.startsWith("!dotabuff "))
		{
			String[] split = message.split(" ");
			if(split.length != 2)
				return;
			
			String name = split[1];
			
			String url = "http://dotabuff.com/players/"+name;
			
			try {
			Document doc = Jsoup.connect(url).get();
			String text = doc.body().html();
			System.out.println(text);
			
			String playername = doc.select("div.content-header-title").text();
			int won = Integer.parseInt(doc.select("span.won").text());
			int lost = Integer.parseInt(doc.select("span.lost").text());
	
			double winrate = (double)won / ((double)(won + lost));
			winrate = round(winrate*100, 2);
			
			output(channel, "Calculating stats from dotabuff.com ...");
			output(channel, playername+": Won="+won+", Lost="+lost+", Winrate="+winrate+"%");
			}
			catch (Exception e) {
				e.printStackTrace();
				output(channel, "Error connecting to dotabuff.");
			}
		}
		
		if(message.equals("!help"))
		{
			sendNotice(sender, "Available commands are: ");
			sendNotice(sender, "!dotabuff [player number], !chat, !allusers, !top10, !stats [player], !list [game], !new [game], !join [game], !leave, !side [radiant/dire/random], !report [radiant/dire]");
			
			if(checkOp(channel, sender))
			{
				sendNotice(sender, "Operator commands:");
				sendNotice(sender, "!die, !showtopics, !addwin [player], !addloss [player], !setwins [player] [wins], !setlosses [player] [losses], !abort [game], !addplayer [player], !removeplayer [player]");
			}
		}
	}
}