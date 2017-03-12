package dev;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import dev.Node;

/**
 * Created by utsavdholakia on 3/11/17.
 */
public class App {
    public static String configFileName = "./configuration.txt";
    public static volatile ConcurrentHashMap<Integer, ArrayList<String>> neighbours =
            new ConcurrentHashMap<Integer, ArrayList<String>>();
    public static Integer nodeID;       //Current Node's nodeID
    public static String currHostName;  //Current Node's hostname
    public static Integer currPortNum;  //Current Node's port number
    public static Integer totalNodes;   //Total nodes in topology
    public static Node self;
    public static Map<Integer, Node> tempMap= new HashMap<Integer, Node>();
    public static Map<Integer, Node> nodeMap= new HashMap<Integer, Node>();
    
    public static Integer minPerActive = 0;
    public static Integer maxPerActive = 0;
    public static Integer minSendDelay = 0;
    public static Integer maxSendDelay = 0;
    public static Integer maxNumberMsgs = 0;

    public static Vector<Integer> vectorClock = new Vector<Integer>();

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
            FileReader fileReader = new FileReader("configuration.txt");
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
                    	maxSendDelay= Integer.parseInt(info[4]);
                    	maxNumberMsgs= Integer.parseInt(info[5]);
                    	System.out.println("Read 1st line : " + totalNodes + " "+ minPerActive +" "+ maxPerActive +" "+ minSendDelay +" "+ maxSendDelay +" "+ maxNumberMsgs);
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
                        	//tempTopology.put(Integer.parseInt(neighbors[0]), line);

                            if (self.getNodeId() == counter) {    //if 1st identifier is me ,then get my neighbors and add to nodeMap
                            	for (int i = 0; i < neighbors.length; i++) {
                                    nodeMap.put(Integer.parseInt(neighbors[i]), tempMap.get(Integer.parseInt(neighbors[i])));
                                   // discoveredTopology.put(Integer.parseInt(neighbors[i]), new TopologyPayload(1, false));    // already discovered nodes are my neighbors @ 1 hop dist
                                    System.out.println(">>>>"+neighbors[i]+">>>"+tempMap.get(Integer.parseInt(neighbors[i])));
                                }
                            }
                            counter++;		//placeholder for source node 0 123 and 1 04 and so on, since the source is missing from the lines.
                            lineCount++;
                        }
                    }


                } else {
                    //line doesn't start with numeric value, ignored!
                    continue;
                }
            }


            //Listener(Server) class initiated
            Listener listener = new Listener(self.getPort());
            Thread serverThread = new Thread(listener);
            serverThread.start();

            Thread.sleep(5000);

            //Wait for server class be finished before finishing main thread
            serverThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



