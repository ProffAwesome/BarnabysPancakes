import java.io.*;

class Book {
	private String bookName;	//Name of the book (20)
	private String fName;		//Name of the author (20)
	private String lName;		//Name of the author (20)
	private String publisher;	//Publisher name (20)
	private long isbnNum;		//ISBN Number
	private double price;		//Price to buy the book
	private int stock;			//Quantity in stock of that book
	private int recLen = 180;	//Number of bytes in the record
	
	public Book(String bName, String aName1, String aName2, String pName, long iNum, double bPrice, int bStock){
		this.bookName = bName;
		this.fName = aName1;
		this.lName = aName2;
		this.publisher = pName;
		this.isbnNum = iNum;
		this.price = bPrice;
		this.stock = bStock;
	}
	
	public Book(){
		bookName = "";
		fName = "";
		lName = "";
		publisher = "";
		isbnNum = 0;
		price = 0.00;
		stock = 0;
	}
	
	public String getBookName(){ return bookName; }
	public String getfName(){ return fName; }
	public String getlName(){ return lName; }
	public String getPublisher(){ return publisher; }
	public long getIsbnNum(){ return isbnNum; }
	public double getPrice(){ return price; }
	public int getStock(){ return stock; }
	public int getRecLen() { return recLen; }
	
	public void setBookName(String newName){ bookName = newName; }
	public void setfName(String newFName){ fName = newFName; }
	public void setlName(String newLName){ lName = newLName; }
	public void setPublisher(String nPublisher){ publisher = nPublisher; }
	public void setIsbnNum(long nNum){ isbnNum = nNum; }
	public void setPrice(double nPrice){ price = nPrice; }
	public void setStock(int nStock){ stock = nStock; }
	
	public void readRec(BufferedReader in){
		try{
			String temp = in.readLine();
			if (temp != null){
   				this.setBookName(temp);
	   			this.setfName(in.readLine());
   				this.setlName(in.readLine());
   				this.setPublisher(in.readLine());
	    		this.setIsbnNum(Long.parseLong(in.readLine()));
	   			this.setPrice(Double.parseDouble(in.readLine()));
   				this.setStock(Integer.parseInt(in.readLine()));
			}	//end if
    	}catch(Exception e){
    		System.out.println(e);
    	}
	}
	
	public void writeRec(){
		if (this.getIsbnNum() != 0){
    		System.out.println("Book Name: " + this.getBookName());
    		System.out.println("Author: " + this.getlName() + ", " + this.getfName());
    		System.out.println("Publisher: " + this.getPublisher());
    		System.out.println("ISBN Number: " + this.getIsbnNum());
    		System.out.println("Price: " + this.getPrice());
    		System.out.println("--------------------------------------");
		}	//end if
    }
    
    public void readRaf(RandomAccessFile raf, int recordNumber) throws IOException{
    	if (raf.length() >= (recordNumber * recLen)){
    		raf.seek(recordNumber * recLen);
	    	this.bookName = readString(raf);
	    	this.fName = readString(raf);
	    	this.lName = readString(raf);
	    	this.publisher = readString(raf);
	    	this.isbnNum = raf.readLong();
			this.price = raf.readDouble();
			this.stock = raf.readInt();
    	}
    }
    
    public void writeRaf(RandomAccessFile raf, int recordNumber) throws IOException{
    	if (recordNumber != 0)
    		raf.seek(recordNumber * recLen);
    	writeString(raf, this.bookName);
    	writeString(raf, this.fName);
    	writeString(raf, this.lName);
    	writeString(raf, this.publisher);
    	raf.writeLong(this.isbnNum);
    	raf.writeDouble(this.price);
    	raf.writeInt(this.stock);
    }
    
    public void addRaf(RandomAccessFile raf, int dataLen) throws IOException{
    	raf.seek(raf.length());
    	this.setBookName(Keyin.inString("Book name: "));
		this.setfName(Keyin.inString("Author first name: "));
		this.setlName(Keyin.inString("Author last name: "));
		this.setPublisher(Keyin.inString("Publisher name: "));
		this.setIsbnNum(Long.parseLong(Keyin.inString("ISBN Number: ")));
		this.setPrice(Keyin.inDouble("Book price: "));
		this.setStock(Keyin.inInt("# in stock: "));
    	/*writeString(raf, this.bookName);
    	writeString(raf, this.fName);
    	writeString(raf, this.lName);
    	writeString(raf, this.publisher);
    	raf.writeLong(this.isbnNum);
    	raf.writeDouble(this.price);
    	raf.writeInt(this.stock);*/
    	this.writeRaf(raf, 0);
    }
    
    private void writeString(RandomAccessFile raf, String s) throws IOException{
    	int padLen = 0;
    	if (s.length() > 20)
    		padLen = 0;
    	else
    		padLen = 20 - s.length();
    	for (int i = 0; i < s.length(); i++)
    		raf.writeChar(s.charAt(i));
    	if (padLen > 0)
    		for (int j = 0; j < padLen; j++)
    			raf.writeChar(' ');
    }
    private String readString(RandomAccessFile raf) throws IOException{
    	String s = "";
    	for (int i = 0; i < 20; i++)
    		s = s + raf.readChar();
    	return s.trim();
    }
}
