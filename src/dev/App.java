package dev;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by utsavdholakia on 3/11/17.
 */
public class App {
    public static Integer totalNodes;   //Total nodes in topology
    public static Node self;
    public static Map<Integer, Node> tempMap= new HashMap<Integer, Node>();
    public static Map<Integer, Node> nodeMap= new HashMap<Integer, Node>();		//Stores my neighbors
    
    public static Integer minPerActive = 0;
    public static Integer maxPerActive = 0;
    public static Integer minSendDelay = 0;
    public static Integer snapshotDelay = 0;
    public static Integer maxNumberMsgs = 0;
    //Store marker message is sent or not for a relevant snapshot ID
    public static volatile TreeMap<Integer, Boolean> markerMessageSent = new TreeMap<Integer, Boolean>();
    //Store which node has sent "it is passive now" message to node 0
    public static volatile Set<Integer> nodesPassive = new HashSet<Integer>();
    //Local vector clock of the node
    public static volatile Vector<Integer> vectorClock;
    //Store channelStates for different snapshots : for snapshot no. 0, store clock value of each node in the map(<Node ID, Vector Clock value>)
    public static volatile List<TreeMap<Integer, Integer>> channelStates = new ArrayList<TreeMap<Integer, Integer>>();
    public static volatile boolean isProcessActive = false;		//tells the current process state - active/passive
    public static volatile boolean mapProtocolTerminationFlag = false;	//keeps track of Map protocol termination condition
    public static volatile int sentMsgCount = 0;		//keeps track of all the sent messages over all active intervals
    public static volatile boolean stopMapProtocolsMessageSent = false;  //Used by node 0 to indicate it has sent stop map protocol message to everyone
    public static volatile Integer maxSnapshotID = 0;    //Used to check what is the max number of snapshot initiated by node 0, stop server after that
    public static volatile Integer snapshotNumber = 0;   //Used by node 0 to assign snapshotID to each snapshot

    public static void main(String args[]) {
        String line = null;
        String hostName = null;
        boolean[] lines;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
            System.out.println("HOST "+hostName);
        } catch (IOException e1) {
            System.out.println("Error fetching host name!");
        }
        try {
            FileReader fileReader = new FileReader("configuration_chandy.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            int lineCount = 0, linesToRead = 0;
            int counter=0;

            //Read file correctly
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                //System.out.println(">>"+line);
                if (line.length() == 0) {              //if empty line
                    continue;
                } else if (line.startsWith("#")) {    //if comment
                    continue;
                } else if (line.indexOf("#") != -1) {        //if comment in between line
                    line = line.split("#")[0];
                }
                if (line.startsWith("0") ||
                        line.startsWith("1") ||
                        line.startsWith("2") ||
                        line.startsWith("3") ||
                        line.startsWith("4") ||
                        line.startsWith("5") ||
                        line.startsWith("6") ||
                        line.startsWith("7") ||
                        line.startsWith("8") ||
                        line.startsWith("9")) {              // Required scenario. Work here.
                    //System.out.println(""+line+" "+lineCount+" "+linesToRead);
                    if (lineCount == 0) {
                    	//get stuff for BS
                    	String[] info = line.split("\\s+");
                    	totalNodes= Integer.parseInt(info[0]);
                    	minPerActive= Integer.parseInt(info[1]);
                    	maxPerActive= Integer.parseInt(info[2]);
                    	minSendDelay= Integer.parseInt(info[3]);
                    	snapshotDelay= Integer.parseInt(info[4]);
                    	maxNumberMsgs= Integer.parseInt(info[5]);
                    	System.out.println("Read 1st line : " + totalNodes + " "+ minPerActive +" "+ maxPerActive +" "+ minSendDelay +" "+ snapshotDelay +" "+ maxNumberMsgs);
                        //ignore first line
                        lineCount++;
                        linesToRead = totalNodes;      //Remembering the number of lines to read,say, N
                        //lines = new boolean[linesToRead];
                        continue;
                    } else if (lineCount > 0) {
                        if (lineCount <= linesToRead) {
                            //Store dcXX.utdallas and port number
                            String[] sysInfo = line.split("\\s+");
                            System.out.println(">>>>>>>"+line);
                            if (sysInfo.length == 3) {
                                Node node = new Node();
                                node.setNodeId(Integer.parseInt(sysInfo[0]));
                                node.setNodeAddr(sysInfo[1]);		//for local system
                                // node.setNodeAddr(sysInfo[1]+".utdallas.edu");
                                node.setPort(Integer.parseInt(sysInfo[2]));
                                if (node.getNodeAddr().equals(hostName)) {      //identifying if the node is itself, then storing it in SELF.
                                    self = node;
                                }
                                tempMap.put(node.getNodeId(), node);      //temporarily storing all nodes as we are reading config file for buffering.
                                lineCount++;
                            }
                            continue;
                        } else if (lineCount > linesToRead) {  //Compute my neighbors
                            String[] neighbors = line.split("\\s+");
                            System.out.println("<><><><><><><><>"+line);

                            //Assuming node 0 to be initially active, setting state to active.
                            if(self.getNodeId() == 0){
                            	isProcessActive = true;
                            }

                            if (self.getNodeId() == counter) {    //if 1st identifier is me ,then get my neighbors and add to nodeMap
                            	for (int i = 0; i < neighbors.length; i++) {
                                    nodeMap.put(Integer.parseInt(neighbors[i]), tempMap.get(Integer.parseInt(neighbors[i])));
                                    System.out.println(">>>>"+neighbors[i]+">>>"+tempMap.get(Integer.parseInt(neighbors[i])).getNodeId());
                                }
                            }
                            /*else {
                                for (int i = 0; i < neighbors.length; i++) {
                                    if(self.getNodeId() == Integer.parseInt(neighbors[i])) {
                                        nodeMap.put(counter, tempMap.get(counter));
                                        System.out.println(">>>>" + neighbors[i] + ">>>" + tempMap.get(Integer.parseInt(neighbors[i])).getNodeId());
                                        break;
                                    }
                                }
                            }*/
                            counter++;		//placeholder for source node 0 123 and 1 04 and so on, since the source is missing from the lines.
                            lineCount++;
                        }
                    }


                } else {
                    //line doesn't start with numeric value, ignored!
                    continue;
                }
            }

            //Initialize vector clock with value 0 for each node, size = number of nodes
            vectorClock = new Vector<Integer>(Collections.nCopies(totalNodes, 0));

           //Listener(Server) class initiated
            Listener listener = new Listener(self.getPort());
            Thread listenerThread = new Thread(listener, "Listener Thread");
            listenerThread.start();

            Thread.sleep(5000);

            MapProtocol protocol = new MapProtocol();
            Thread protocolThread = new Thread(protocol, "Map Protocol Thread");
            protocolThread.start();
            Timer timer = new Timer();
            //If the node is co-ordinator node (node 0), then start sending snapshot initiating marker messages to neighbors
            if(self.getNodeId() == 0){
                TimerTask tasknew = new TimerTask() {
                    @Override
                    public void run() {
                        //Stop initiating snapshots after you sent map protocol termination messages to everyone
                        while(!App.stopMapProtocolsMessageSent){
                            if(App.markerMessageSent != null && !App.markerMessageSent.containsKey(snapshotNumber)) {
                                //Record local state (local vector clock value)
                                TreeMap<Integer, Integer> channelState = new TreeMap<Integer, Integer>();
                                for(int node = 0; node < App.totalNodes; node++) {
                                    channelState.put(node, App.vectorClock.get(node));
                                }
                                App.channelStates.add(snapshotNumber, channelState);
                                System.out.println("Sending marker message with snapshot ID : " + snapshotNumber);
                                Message outMessage = new Message(MessageType.Marker, 0, null, snapshotNumber);
                                //Mark that marker message has been sent for this snapshot ID
                                App.markerMessageSent.put(snapshotNumber, true);
                                Processor.sendMarkerMessages(outMessage);
                                snapshotNumber++;
                            }
                        }
                    }
                };
                if(!App.stopMapProtocolsMessageSent) {
                    tasknew.run();
                    timer.schedule(tasknew, snapshotDelay);
                }
            }
            //Wait for map protocol termination to be marked
            while(true){
                if(App.stopMapProtocolsMessageSent) {
                    timer.cancel();
                    timer.purge();
                }
                Boolean neighbourMarkerNotArrivedYet = false;
                //When it is marked, wait for marker messages with maxSnapshotID to arrive from all neighbors, then terminate server
                if(App.mapProtocolTerminationFlag && App.channelStates != null){
                    if(channelStates.contains(maxSnapshotID)) {
                        while (true) {
                            //System.out.println("starting while loop...3");
                            Set<Integer> neighborsWithMarkerMessage = channelStates.get(maxSnapshotID).keySet();
                            Iterator<Integer> neighborNodes = nodeMap.keySet().iterator();
                            neighbourMarkerNotArrivedYet = false;
                            while (neighborNodes.hasNext()) {
                                int node = neighborNodes.next();
                                if (!neighborsWithMarkerMessage.contains(node)) {
                                    neighbourMarkerNotArrivedYet = true;
                                    break;
                                }
                            }
                            if (!neighbourMarkerNotArrivedYet) {
                                break;
                            }
                        }
                    }
                    listener.stopListener();
                    break;
                }
                //Thread.sleep(1000);
            }
            //printOutput();

        } catch (Exception e) {

        }
        finally {
            printOutput();
        }
    }

    public static void printOutput(){
        try {
            FileWriter fileWriter = new FileWriter("configuration-" + self.getNodeId() + ".out");
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            for(int index = 0; index < App.channelStates.size() ; index++){
                for(int node = 0; node < App.totalNodes; node++) {
                    int value = App.channelStates.get(index).get(node);
                    fileWriter.write(value + "  ");
                }
                fileWriter.write("\n");
            }
            bufferedWriter.close();
            fileWriter.close();
        } catch(IOException e){

        }
    }
}



