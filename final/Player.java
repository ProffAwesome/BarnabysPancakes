public class Player {
	double x, y;
	int ammo, health, mana, cash, exp;
	int q, l, r; //assigned inventory
	int[] inv = new int[32]; //inventory items - 8x4
	public Player () { }
	public Player (boolean a) { //for dev/new player defaults
		ammo = 30;
		health = 100;
		mana = 100;
		cash = 0;
		exp = 0;
		q = 0;
		l = 10;
		r = 15;
	}
	
	public void takeHealth(int damage){
		if (health > damage)
			health -= damage;
		else
			health = 0;
			//Death sequence
	}
}