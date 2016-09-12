

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


public class serverThread implements Runnable {
	
	private int id;
	public static ArrayList<String> neighbour;
	private ServerSocket soc_server;
    private String[] all_nodes;
    private String[] neighborlist;
    private int port;
    private static String configpath;
    
    public serverThread(ServerSocket soc_server,int id,String[] all_nodes, String[] neighborlist,String configpath){
		this.soc_server = soc_server;
		this.id = id;
		this.all_nodes = all_nodes;
		this.neighborlist = neighborlist;
		this.configpath = configpath;
	}

	@Override
	public void run() {
		while (true) {
            try {
            	//System.out.println("new server "+this.id );
				runserver(soc_server.accept());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
	
	private void runserver(Socket s){
		try{
			ObjectInputStream input;
		input = new ObjectInputStream(s.getInputStream());
		while(true){
		Object obj=null;
		try {
			obj = input.readObject();
			//Message msg = (Message) obj;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (obj instanceof Message) {
			Message msg = (Message) obj;
			if(msg.getType().equals("appMsg")){
				System.out.println(Node.hostname+ " appMsg received from " +msg.getfromid());
				if((Node.state).equals("active")){
					compareClk(msg.getClock());
					writercv(msg);
				}
				else if((Node.state).equals("passive")){
					compareClk(msg.getClock());
					writercv(msg);
					Node.state = "active";
				}
				else if((Node.state).equals("finish")){
					compareClk(msg.getClock());
					writercv(msg);
				}
				
			}
			else if((msg.getType()).equals("Marker")){
				if(Node.getIdentifier() !=0){
				if(Node.getSnapcount() < msg.getsnapcount()){
					System.out.println(Node.hostname+ " marker received from " +msg.getfromid());
					//if(Node.getIdentifier() !=0){
					Node.setSnapcount();
					writesnap(msg);
					snapshot s1 = new snapshot(neighborlist,Node.snapshotDelay,all_nodes);
					Thread t = new Thread(s1);
					t.start();
					if((Node.state).equals("finish") || (Node.state).equals("passive")){
						Message msg1= new Message("finish",Node.getClock(),0,Node.getIdentifier(),Node.getSnapcount());
						String info[] = all_nodes[0].split(" ");
						int idnode = Integer.parseInt(info[0]);
				        String hostnode = info[1];
				        int portnode = Integer.parseInt(info[2]);
						clientThread c = new clientThread(idnode,hostnode,portnode);
						c.connect(hostnode, portnode, msg1);
					}
				}
			}
			}
			else if((msg.getType()).equals("finish")){
				if(Node.getIdentifier() == 0){
					if(Node.getSnapcount() == msg.getsnapcount()){
						Node.finish[msg.getfromid()] = 1;
						int[] msgclk = msg.getClock();
						for(int i=0;i<Node.number_of_nodes;i++){
							Node.terminate[msg.getfromid()][i]= msgclk[i];
						}
					}
					int flag =0;
					for(int i =0;i<Node.finish.length;i++){
						if(Node.finish[i] != 1){
							flag =1;
							break;
						}
					}
					if(flag == 0){
						Node.terminated =true;
						System.out.println("Application terminated - RUN CLEANUP SCRIPT TO KILL ALL BACKGROUND THREADS");
						String x= "Application terminated at Global state: ";
						writesnap(x);
					}
				}
			}
		}
		}
		}
		catch(Exception e){
			//System.out.println("EOF of client message- server restart");
			
			try {
				s.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			//e.printStackTrace();
		}
		

}

private void compareClk(int[] msgclock) {
	int[] currclock = Node.getClock();
	int[] newvclock = new int[Node.number_of_nodes];
	for(int i =0;i<Node.number_of_nodes;i++){
		if(i!= Node.getIdentifier()){
			newvclock[i]= Math.max(msgclock[i],currclock[i]);
		}
	}
	newvclock[Node.getIdentifier()]= currclock[Node.getIdentifier()]+1;
	Node.setClock(newvclock);
	
}

public static void writesnap(String x){
	Writer writer;
	try {
		FileOutputStream FoutStream = new FileOutputStream(
				"terminated_globalState_root-"+".txt", true);
		try {
			writer = new BufferedWriter(
					new OutputStreamWriter(FoutStream, "UTF-8"));
				
			writer.append(x);
			writer.append("\n");
			for(int i=0;i<Node.number_of_nodes;i++){
				writer.append("node "+i+":");
				for(int j=0;j<Node.number_of_nodes;j++){
					writer.append(Integer.toString(Node.terminate[i][j]));
					writer.append(" ");
				}
				writer.append("\n");
			}

			writer.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			FoutStream.close();
		}
	} catch (Exception e) {
	}
	
}
public static void writesnap(Message msg){
	Writer writer;
	try {
		FileOutputStream FoutStream = new FileOutputStream(
				configpath + "-"+ Node.getIdentifier() +".txt", true);
		try {
			writer = new BufferedWriter(
					new OutputStreamWriter(FoutStream, "UTF-8"));
				
			writer.append(Node.getClockString());
			writer.append("\n");

			writer.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			FoutStream.close();
		}
	} catch (Exception e) {
	}
	
}
	
private void writercv(Message msg) {
	Writer writer;
	try {
		FileOutputStream FoutStream = new FileOutputStream(
				"receivefile-" + Node.hostname + ".txt", true);
		try {
			writer = new BufferedWriter(
					new OutputStreamWriter(FoutStream, "UTF-8"));
				
			writer.append(msg.getType()+" message Hostname: " + Node.hostname + " received from: " + msg.getfromid()+ " at vclock: "+Node.getClockString());
			writer.append("\n");

			writer.close();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		} finally {
			FoutStream.close();
		}
	} catch (Exception e) {
	}
	
}

}
