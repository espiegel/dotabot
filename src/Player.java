
public class Player implements java.io.Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3394309638611729808L;
	
	private String _name;
	private int _wins;
	private int _losses;
	private double _ratio;
	private Game _currentGame;
	private Game _ownedGame;
	private String _side;
	
	public Player()
	{
		_name = "Default";
		_wins = 0;
		_losses = 0;
		_currentGame = null;
		_ownedGame = null;
	}
	
	public Player(String name)
	{
		this();
		_name = name;
	}	
	
	public void setGame(Game g) { _currentGame = g; }
	public Game getGame() { return _currentGame; }
	
	public void setOwned(Game g) { _ownedGame = g; }
	public Game getOwned() { return _ownedGame; }
	
	public void setName(String name) { _name = name; }
	public String getName() { return _name; }
	
	public void setSide(String side) { _side = side; }
	public String getSide() { return _side; }
	
	public void setWins(int wins) { _wins = wins; updateRatio(); }
	public int getWins() { return _wins; }
	
	public void setLosses(int losses) { _losses = losses; updateRatio(); }
	public int getLosses() { return _losses; }
	
	public int incWins() { _wins++; updateRatio(); return _wins; }
	public int incLosses() { _losses++; updateRatio(); return _losses; }
	
	public double getRatio() { return _ratio; }
	public void updateRatio()
	{
		if(_losses == 0)
			_ratio = 1.0;
		else
			_ratio = (double)((double)(_wins) / (double)(_wins + _losses));
	}
}
