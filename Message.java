

import java.io.Serializable;

public class Message implements Serializable{
    private String type;
    private int[] vectorClock;
    private int toid;
    private int fromid;
    private int snapcount;
    
    public Message(String type, int[] vectorClock, int toid,int fromid,int snapcount){
    	this.type = type;
    	this.vectorClock = vectorClock;
    	this.toid = toid;
    	this.fromid = fromid;
    	this.snapcount = snapcount;
    }
    
    public String getType(){
    	return type;
    }
    
    public int[] getClock(){
    	return vectorClock;
    }
    
    public int gettoid(){
    	return toid;
    }

    public int getfromid(){
    	return fromid;
    }
    
    public int getsnapcount(){
    	return snapcount;
    }
}
