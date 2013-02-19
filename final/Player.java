public class Player {
	public static double x, y;
	public static int health, mana, cash, exp;
	
	public static boolean bTele; 	//Current teleporter id
	public static int nTele;	//Number of teleporters on map
	public static long warpTimer;
	
	public static boolean dead = false;
	
	public static Weapon q, l, r; //assigned inventory
	public static Weapon[] inv = new Weapon[32];
	
	public Player() { }
	
	public Player (boolean a) { //for dev/new player defaults
		health = 100;
		mana = 100;
		cash = 0;
		exp = 0;
		q = new Weapon(0);
		l = new Weapon(10);
		dead = false;
		if (!Display.demo)
			r = new Weapon(15);
		else
			r = new Weapon();
		for (int i = 0; i < inv.length; i++)
			inv[i] = new Weapon();
	}
	
	/*****************************************************
	 * Resets all the players stats back to defaults
	 * TODO: Reset stats back to last checkpoint
	 *****************************************************/
	public static final void resetStats(){
		health = 100;
		mana = 100;
		cash = 0;
		exp = 0;
		q = new Weapon(0);
		l = new Weapon(10);
		dead = false;
		if (!Display.demo)
			r = new Weapon(15);
		else
			r = new Weapon();
		for (int i = 0; i < inv.length; i++)
			inv[i] = new Weapon();
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
	
	public void takeHealth(int damage){
		if (health > damage)
			health -= damage;
		else {
			health = 0;
			dead = true;
		}
	}
	
	public static void checkTele(){
		for (int i = 0; i < nTele; i++){
			
		}
	}
	
	
}