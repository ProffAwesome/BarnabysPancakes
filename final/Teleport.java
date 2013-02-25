/*****************************************************
 * Stores all the teleports on the map
 *****************************************************/
public class Teleport{
	private String mapTo;
	public int tId;
	public int x, y; 	//Co-ords of this teleporter
	public int sn; 	//Co-ords of matching teleporter
	
	public Teleport(int tId, int x, int y, String mapTo, int sn){
		this.tId = tId;
		this.x = x;
		this.y = y;
		this.mapTo = mapTo;
		this.sn = sn;
	}
	
	/*****************************************************
	 * Initiates teleport sequence
	 * TODO: Initiate this teleport sequence
	 *****************************************************/
	public void startTele(){
		Display.p.bTele = true;
		Display.p.warpTimer = System.currentTimeMillis();
		(new Thread(this.new Warp())).start();
	}
	
	public void stopTele(){	Display.p.bTele = false;	}
	
	/*****************************************************
	 * Teleport sequence.
	 * Actually warps the player to map & corresponding coords
	 *****************************************************/
	public class Warp implements Runnable{
		public Warp(){	}
		public void run(){
			try{
				System.out.println("Teleport in:");
				for (int i = (int)((System.currentTimeMillis() - Display.p.warpTimer)/1000); i < 5; i++){
					System.out.println(5-i);
					if (System.currentTimeMillis() - Display.p.warpTimer > 3)	//They have been in teleporter for 3 seconds
						if (Display.p.bTele == true)	//They haven't been interrupted
							Display.readInMap(mapTo, getClass().getResource(""), true, sn);
						else
							System.out.println("Interrupted");
					Thread.sleep(1000);
				}
			}catch (Exception e){	e.printStackTrace();	}
		}
	}
	
	
}