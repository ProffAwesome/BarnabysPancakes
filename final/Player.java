public class Player {
	double x, y;
	int health, mana, cash, exp;
	
	boolean bTele; 	//Current teleporter id
	int nTele;	//Number of teleporters on map
	long warpTimer;
	
	boolean dead = false;
	
	Weapon q, l, r; //assigned inventory
	Weapon[] inv = new Weapon[32];
	
	public Player() { }
	
	public Player (boolean a) { //for dev/new player defaults
		health = 100;
		mana = 100;
		cash = 0;
		exp = 0;
		q = new Weapon(0);
		l = new Weapon(10);
		dead = false;
		r = new Weapon(15);
		for (int i = 0; i < inv.length; i++)
			inv[i] = new Weapon();
	}
	
	/*****************************************************
	* Resets all the players stats back to defaults
	* TODO: Reset stats back to last checkpoint
	*****************************************************/
	public final void resetStats(){
		health = 100;
		mana = 100;
		cash = 0;
		exp = 0;
		q = new Weapon(0);
		l = new Weapon(10);
		dead = false;
		r = new Weapon(15);
		for (int i = 0; i < inv.length; i++)
			inv[i] = new Weapon();
	}
	
	public final void checkTele(){
		for (int i = 0; i < nTele; i++){
			;
		}
	}

	
	public final void addWeap(Weapon temp) {
		boolean added = false;
		for (int i = 0; i < inv.length; i++) {
			if (inv[i].wid == -1 && !added) {
				inv[i] = temp;
				added = true;
			}
		}
		if (!added)
			System.out.println("No space in inventory.");
	}
	
	public final void takeHealth(int damage){
		if (health > damage)
			health -= damage;
		else {
			health = 0;
			dead = true;
		}
	}
}