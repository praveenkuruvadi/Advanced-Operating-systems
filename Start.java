



public class Start implements Runnable{
	
	private int id;
	private String configPath;
	
	public Start(int id, String configPath){
		this.id = id;
		this.configPath = configPath;
	}

	@Override
	public void run() {
		if(id == 0)
			startNode("active");
		else
			startNode("passive");
		
	}

	private void startNode(String type) {
		Node n = new Node(type, id, configPath);
		Thread t = new Thread(n);
		t.start();
	}
	
	public static void main(String[] args){
		int no_of_node = Integer.parseInt(args[0]);
		//int no_of_node=2;
		String config_path = args[1];
		Start start = new Start(no_of_node, config_path);
		Thread t = new Thread(start);
		t.start();
	}

}

