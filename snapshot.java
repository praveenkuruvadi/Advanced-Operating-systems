



public class snapshot implements Runnable{

	private String[] neighborlist;
	private int snapshotDelay;
    private String[] all_nodes;
	@Override
	public void run() {
		if(Node.getIdentifier() == 0){
		while(Node.terminated != true){
			try {
				Thread.sleep(snapshotDelay);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Node.inivfinish();
			Node.inivterminate();
			int[] currclk= Node.getClock();
			for(int i=0;i<Node.number_of_nodes;i++){
				Node.terminate[0][i]= currclk[i];
			}
			Node.setSnapcount();
			Node.finish[0]=1;
			for(int i=0;i<Node.number_neighbors;i++){
				Message msg = new Message("Marker",Node.getClock(),Integer.parseInt(neighborlist[i]),Node.getIdentifier(),Node.getSnapcount());
				if(i == 0){
					serverThread.writesnap(msg);
				}
				String[] info = all_nodes[Integer.parseInt(neighborlist[i])].split(" ");
				clientThread c = new clientThread(Integer.parseInt(info[0]) , info[1], Integer.parseInt(info[2]));
				try {
					c.connect(info[1], Integer.parseInt(info[2]), msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		}
		else if(Node.getIdentifier()  != 0){
			//Node.setSnapcount();
			for(int i=0;i<Node.number_neighbors;i++){
				Message msg = new Message("Marker",Node.getClock(),Integer.parseInt(neighborlist[i]),Node.getIdentifier(),Node.getSnapcount());
				String[] info = all_nodes[Integer.parseInt(neighborlist[i])].split(" ");
				clientThread c = new clientThread(Integer.parseInt(info[0]) , info[1], Integer.parseInt(info[2]));
				try {
					c.connect(info[1], Integer.parseInt(info[2]), msg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
			
		}
	
	public snapshot(String[] neighborlist,int snapshotDelay,String[] all_nodes){
		this.neighborlist = neighborlist;
		this.snapshotDelay = snapshotDelay;
		this.all_nodes = all_nodes;
	}

}
