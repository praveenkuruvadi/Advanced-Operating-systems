

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Scanner;

public class Node implements Runnable {
	
    private String type;
    private String configPath;
    private static int identifier;

    private int maxPerActive;
    private int minPerActive;
    private int maxNumber;
    private int minSendDelay;
    public static int snapshotDelay;
    public static int number_of_nodes;
    private String[] all_nodes;
    public static String hostname;
    private int port;
    private Queue<String> neighbors = new LinkedList<>();
    private String[] neighborlist;
    public static int number_neighbors;
    
    private static volatile int[] vClock;
    public static volatile boolean terminated;
    public static volatile int[] finish;
    public static volatile int[][] terminate;
    public volatile static String state;
    private volatile static int snapcount=0;
    public static volatile int msgcnt =0;
	private ServerSocket server_socket = null;
    
    public Node(String type,int id, String configPath){
        this.type = type;
        this.identifier = id;
        this.configPath = configPath;
    }
    public synchronized static void setSnapcount(){
    	snapcount++;
    }
    public static int getIdentifier(){
    	return identifier;
    }
	public static int[] getClock() {
		return vClock;
	}
	
	public static String getClockString(){
		int[] currClock = getClock();
		StringBuilder builder = new StringBuilder();
		for (int i : currClock) {
		  builder.append(i);
		  builder.append(" ");
		}
		String text = builder.toString();
		return text;
	}

	public synchronized static void setClock(int[] newvclock) {
		vClock = newvclock;
	}
    
    private void fileparser(String path, int id) throws Exception {
        Scanner scan_path = new Scanner(new File(path));
        identifier = id;
        String nextLine = scan_path.nextLine().trim();
        while (nextLine.equals("") || nextLine.charAt(0) == '#') {
            nextLine = scan_path.nextLine().trim();
        }
        if (nextLine.contains("#")) {
            number_of_nodes = Integer.parseInt(nextLine.split("#")[0].trim().split("\\s+")[0]);
        } else {
            number_of_nodes = Integer.parseInt(nextLine.trim().split("\\s+")[0]);
			minPerActive = Integer.parseInt(nextLine.trim().split("\\s+")[1]);
			maxPerActive = Integer.parseInt(nextLine.trim().split("\\s+")[2]);
			minSendDelay = Integer.parseInt(nextLine.trim().split("\\s+")[3]);
			snapshotDelay = Integer.parseInt(nextLine.trim().split("\\s+")[4]);
			maxNumber = Integer.parseInt(nextLine.trim().split("\\s+")[5]);
        }
        all_nodes = new String[number_of_nodes];
        nextLine = scan_path.nextLine().trim();
        while (nextLine.equals("") || nextLine.charAt(0) == '#') {
            nextLine = scan_path.nextLine().trim();
        }
        for (int i = 0; i < number_of_nodes; i++) {
            if (nextLine.contains("#")) {
                all_nodes[i] = nextLine.split("#")[0].trim();
            } else {
                all_nodes[i] = nextLine;
            }
            if (identifier == i) {
                String[] info = all_nodes[i].split(" ");
                hostname = info[1];
                port = Integer.parseInt(info[2]);
            }
            nextLine = scan_path.nextLine();
        }
        int count =0;
        while (scan_path.hasNextLine()) {
            nextLine = scan_path.nextLine();
            while (nextLine.equals("") || nextLine.charAt(0) == '#') {
                nextLine = scan_path.nextLine();
            }

            String nextNeighbor;
            if (nextLine.contains("#")) {
                nextNeighbor = nextLine.split("#")[0].trim();
            } else {
                //nextLine = scan_path.nextLine();
                    nextNeighbor = nextLine;
                    //System.out.println(nextNeighbor);
            }
            //identifier = identifier+1;
            if (count == identifier) {
            	this.neighbors.add(nextNeighbor);
				neighborlist = neighbors.peek().split(" ");
				number_neighbors = neighborlist.length;
            }
            count++;
        }
    }
    
    public void display() throws Exception{
        System.out.println("number of nodes: "+number_of_nodes);
        System.out.println("Host name of machine: "+hostname);
        System.out.println("Server side running on port: "+port);
        System.out.println("Maxnumber: " + maxNumber);
        System.out.println("Minperactive: "+ minPerActive+ "max: "+ maxPerActive);
        System.out.println("minDelay: "+ minSendDelay+"snapshot: "+snapshotDelay);
        System.out.println("All nodes in system: ");
        for (int i= 0;i< number_of_nodes;i++)
            System.out.println(all_nodes[i]);
        System.out.println("Number of neighbors: "+neighborlist.length);
        System.out.println("neighbors of current node: ");
        System.out.println(neighbors.peek());
        for(String j : neighborlist)
        	System.out.println(j);
    }
    
    
    
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			fileparser(configPath, identifier);
			inivclock(number_of_nodes);
			state = type;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		runServer(hostname,port);
		terminated=false;
		if(identifier == 0){
			inivfinish();
			snapshot s = new snapshot(neighborlist,snapshotDelay,all_nodes);
			Thread t1 = new Thread(s);
			t1.start();
			if(type == "active"){
				try {
					runActive();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else if(type == "passive"){
				try {
					runPassive();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		else if(identifier != 0){
		if(type == "active"){
			try {
				runActive();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if(type == "passive"){
			try {
				runPassive();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		}
	}

	private void runPassive() throws InterruptedException {
		if((state.equals("passive")) && (msgcnt >= maxNumber)){
			state = "finish";
			finish();
		}
		else if((state.equals("passive"))&&(msgcnt<maxNumber)){
			while(state == "passive"){
			Thread.sleep(10);
		}
		}
		runActive();
	}

	private void finish() {
		System.out.println(Node.hostname + " finished sending maxNumber of messages");
		while(true){
			
		}
	}

	private void runActive() throws InterruptedException{
		//state = "active";
		Thread.sleep(minSendDelay);
		if(msgcnt < maxNumber){
			Random rand = new Random();
		    int randomNum = rand.nextInt((maxPerActive - minPerActive) + 1) + minPerActive;
			//System.out.println("random no: " + randomNum);
			for(int i=0;i<randomNum;i++){
				if(msgcnt<=maxNumber){
				int[] currclock = Node.getClock();
				currclock[this.getIdentifier()]++;
		        int index = rand.nextInt(neighborlist.length);
		        String info[] = all_nodes[Integer.parseInt(neighborlist[index])].split(" ");
		        int idnode = Integer.parseInt(info[0]);
		        String hostnode = info[1];
		        int portnode = Integer.parseInt(info[2]);
		        setClock(currclock);
				clientThread c = new clientThread(idnode,hostnode,portnode);
				Message msg = new Message("appMsg",getClock(),idnode,identifier,getSnapcount());
				try {
					c.connect(hostnode, portnode, msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try{
					//if(minSendDelay>1000)
						Thread.sleep(minSendDelay);
					//else
						//Thread.sleep(1000);
				}
				catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				msgcnt = msgcnt+1;
				}
				else
					break;
			}
			state = "passive";
			runPassive();
			//msgcnt= msgcnt+randomNum;
		}
		else{
			state= "passive";
			runPassive();}
	}

	public static int getSnapcount() {
		return snapcount;
	}

	private void runServer(String hostname, int port) {
		try {
			server_socket = new ServerSocket(port);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		serverThread s = new serverThread(server_socket, this.getIdentifier(), this.all_nodes, this.neighborlist,this.configPath );
        Thread t = new Thread(s);
        t.start();
	}

	private void inivclock(int number_of_nodes) {
		vClock = new int[number_of_nodes];
		for(int i=0;i<number_of_nodes;i++)
			vClock[i]=0;
	}
	
	public static void inivfinish() {
		finish = new int[number_of_nodes];
		for(int i=0;i<number_of_nodes;i++)
			finish[i]=0;
	}
	
	public static void inivterminate() {
		terminate = new int[number_of_nodes][number_of_nodes];
		for(int i=0;i<number_of_nodes;i++){
			for(int j=0;j<number_of_nodes;j++){
				terminate[i][j]=0;
			}
		}

	}

}
