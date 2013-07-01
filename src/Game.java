import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class Game implements java.io.Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8341127026294682060L;
	private String _name; // Game name
	private int _gameStatus; // 0-not started. 1-started. 2-finished.
	private List<Player> _players = new ArrayList<Player>(); // List of players inside the game
	private List<Player> _dire = new ArrayList<Player>(); // List of dire players
	private List<Player> _radiant = new ArrayList<Player>(); // List of radiant players
	private Player _owner; // The player that owns the game
	
	public Game()
	{
		_name = "Default";
		_gameStatus = 0;
	}
	
	public Game(String name, Player owner)
	{
		this();
		_name = name;
		_owner = owner;
		
		owner.setOwned(this);
	}
	
	public List<Player> getPlayers() { return _players; }
	public List<Player> getRadiant() { return _radiant; }
	public List<Player> getDire() { return _dire; }
	
	public String listPlayers()
	{
		String list = "";
		Iterator<Player> i = _radiant.iterator();
		Iterator<Player> j = _dire.iterator();
		Iterator<Player> k = _players.iterator();
		
		if(_players.size() == 0)
			return "No Players in this game";
		
		while(k.hasNext())
		{
			Player p = k.next();
			if(p.getSide() == null)
				list = list + p.getName() + ", ";
		}
		
		list = list + "Radiant: ";
		while(i.hasNext())
		{
			Player p = i.next();
			list = list + p.getName() + ", ";
		}
		
		list = list + "Dire: ";
		while(j.hasNext())
		{
			Player p = j.next();
			list = list + p.getName() + ",";
		}
		
		String newlist = list.substring(0, list.length() - 1);
		
		return newlist;
	}
	
	public void addRadiant(Player p)
	{
		_radiant.add(p);
		_dire.remove(p);
	}
	
	public void addDire(Player p)
	{
		_dire.add(p);
		_radiant.remove(p);
	}
	
	public void addPlayer(Player p)
	{
		_players.add(p);
		
		if(_players.size() == 10)
			setStatus(1);
	}
	
	public void removePlayer(Player p)
	{
		
		if(p.getSide().equals("radiant"))
			_radiant.remove(p);
		if(p.getSide().equals("dire"))
			_dire.remove(p);
		
		_players.remove(p);
	
		p.setSide(null);
		p.setGame(null);
	}
	
	public void abortGame()
	{
		for(Player p : _players)
		{
			p.setSide(null);
			p.setGame(null);
			//removePlayer(p);
		}
		
		if(_owner != null)
			_owner.setOwned(null);
	}
	
	public int getNumPlayers() { return _players.size(); }
	
	private void setStatus(int num) { _gameStatus = num; }
	public int getStatus() { return _gameStatus; }
	
	public void setName(String name) { _name = name; }
	public String getName() { return _name; }
	
	public Player getOwner() { return _owner; }
}
