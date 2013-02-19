/*****************************************************
 * Stores all the teleports on the map
 *****************************************************/
public class Teleport{
	private String mapTo;
	public int tId;
	public int x, y; 	//Co-ords of this teleporter
	public int nx, ny; 	//Co-ords of matching teleporter
	
	public Teleport(int tId, int x, int y, String mapTo, int nx, int ny){
		this.tId = tId;
		this.x = x;
		this.y = y;
		this.mapTo = mapTo;
		this.nx = nx;
		this.ny = ny;
	}
	
	/*****************************************************
	 * Initiates teleport sequence
	 * TODO: Initiate this teleport sequence
	 *****************************************************/
	public void startTele(){
		Player.bTele = true;
		Player.warpTimer = System.currentTimeMillis();
		(new Thread(this.new Warp())).start();
	}
	
	public void stopTele(){	Player.bTele = false;	}
	
	/*****************************************************
	 * Teleport sequence.
	 * Actually warps the player to map & corresponding coords
	 *****************************************************/
	public class Warp implements Runnable{
		public Warp(){	}
		public void run(){
			try{
				System.out.println("Teleport in:");
				for (int i = (int)((System.currentTimeMillis() - Player.warpTimer)/1000); i < 5; i++){
					System.out.println(5-i);
					if (System.currentTimeMillis() - Player.warpTimer > 3)	//They have been in teleporter for 3 seconds
						if (Player.bTele == true)	//They haven't been interrupted
							Display.readInMap(mapTo);
						else
							System.out.println("Interrupted");
					Thread.sleep(1000);
				}
			}catch (Exception e){	e.printStackTrace();	}
		}
	}
	
	
}