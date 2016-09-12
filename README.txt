CS6378- PROJECT 1

Project 1 involves the implementation of a distributed system of n nodes of a certain topology. All of this information is given in the config file.

The information is parsed from the config file and all the individual nodes are setup using the launcher script. 

Chandy and Lamports snapshot protocol is implemented on top of the application and records consistent global states. It also finds out the termination of the application and records the global state at the point of termination and stores it in the file terminated_globalState at the root node.

MY ASSUMPTIONS:
Root node is node 0.

Root always initiates the snapshot protocol and records the termination and creates the terminated_globalState file.

No node sends more than maxNumber of messages. 

Every node forms its own set of sendFile- storing all sent messages, receiveFile- storing all received messages and the output file as - <configfile>-<node_id>.txt which stores the local state at every snapshot.

HOW TO RUN:
Place all the files in a directory and run the launcher script using the following arguments:
bash launcher.sh <config_path> <netid: pxk150830>

Keep checking the terminal- Once you see the message application has terminated- run the clean up script, run the cleanup by:
bash cleanup.sh <config_path> <netid: pxk150830>

Observe at each node the output files-
1.sendFile-<nodeid> -records all sent messages
2.receiveFile-<nodeid> -records all received messages
3.<configfile>-<nodeid> -records consistent local state at every snapshot
4.terminated_globalState- exists only at root.Holds the entire global state i.e local state of all nodes at the point of termination.


