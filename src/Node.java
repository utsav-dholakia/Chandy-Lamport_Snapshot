import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by utsavdholakia on 3/11/17.
 */
public class Node {
    public static String configFileName = "./configuration.txt";
    public static volatile ConcurrentHashMap<Integer, ArrayList<String>> neighbours =
            new ConcurrentHashMap<Integer, ArrayList<String>>();
    public static Integer nodeID;       //Current Node's nodeID
    public static String currHostName;  //Current Node's hostname
    public static Integer currPortNum;  //Current Node's port number
    public static Integer totalNodes;   //Total nodes in topology


    public static void main(String args[]) {
        BufferedReader br = null;
        FileReader fr = null;
        try {

            fr = new FileReader(configFileName);
            br = new BufferedReader(fr);

            String sCurrentLine;
            Integer validLines = 0;
            while ((sCurrentLine = br.readLine()) != null) {
                System.out.println(sCurrentLine);
                String delimiters = "\\s+[\t]*|\\s*[\t]+";
                String[] splitArray;
                sCurrentLine = sCurrentLine.trim();
                if (sCurrentLine.length() < 1 || sCurrentLine.charAt(0) == '#') {
                    //If line is empty or starts with a comment
                    continue;
                } else if (sCurrentLine.indexOf('#') != -1) {
                    //Take into account only lines without comments and/or portions before comment starts
                    sCurrentLine = sCurrentLine.substring(0, sCurrentLine.indexOf('#'));
                    splitArray = sCurrentLine.split(delimiters);
                } else {
                    splitArray = sCurrentLine.split(delimiters);
                }
                //Read the total Nodes value from the file
                if (splitArray.length == 1) {
                    Node.totalNodes = Integer.valueOf(splitArray[0]);
                    validLines = Node.totalNodes * 2;   //Total number of valid lines in config file
                    continue;
                }
                if (splitArray[1].equals(InetAddress.getLocalHost().getHostName())) {
                    //If the current node's hostname is matched with the line that contains it
                    Node.nodeID = Integer.valueOf(splitArray[0]);
                    Node.currHostName = splitArray[1];
                    Node.currPortNum = Integer.valueOf(splitArray[2]);
                    validLines--;
                } else if (!splitArray[1].startsWith("dc")) {
                    validLines--;
                    //Found current Node's neighbour list
                    String[] neighbourID = sCurrentLine.split(delimiters);
                    BufferedReader scanForAddress;
                    ArrayList<Integer> listForNode = new ArrayList<Integer>();
                    if (Node.nodeID == Integer.valueOf(neighbourID[0])) {
                        listForNode = new ArrayList<Integer>(2);
                        listForNode.add(0);
                        listForNode.add(0);
                    }
                    for (int i = 1; i < neighbourID.length; i++) {
                        if (Node.nodeID == Integer.valueOf(neighbourID[0])) {
                            listForNode = new ArrayList<Integer>(2);
                            listForNode.add(1);
                            listForNode.add(0);
                            //Add all current neighbours with 1-hop distance in the topologyKnowledge map
                        }
                        //Mark done as false from the immediate neighbour(Its branch has not been discovered completely)
                        //Node.doneMarkedByNeighbour.put(Integer.valueOf(neighbourID[i]), false);
                        scanForAddress = new BufferedReader(new FileReader(configFileName));
                        //Read hostname and port number for every neighbour ID using new reader from the start
                        String line, hostName;
                        Integer portNum;
                        while ((line = scanForAddress.readLine()) != null && Node.nodeID == Integer.valueOf(neighbourID[0])) {
                            String[] splitLine;
                            if (line.length() < 1 || line.charAt(0) == '#') {
                                //If line is empty or starts with a comment
                                continue;
                            } else if (line.indexOf('#') != -1) {
                                //Take into account only lines without comments and/or portions before comment starts
                                line = line.substring(0, sCurrentLine.indexOf('#'));
                                splitLine = line.split(delimiters);
                            } else {
                                splitLine = line.split(delimiters);
                            }
                            /*if(!splitLine[1].startsWith("dc")){   //TODO Remove comments and enable this block
                                continue;
                            }*/
                            if (splitLine.length > 1) {
                                if (Integer.valueOf(splitLine[0]) != Integer.valueOf(neighbourID[i])) {
                                    //If the node which is not a neighbour
                                    continue;
                                } else if (Integer.valueOf(splitLine[0]) == Integer.valueOf(neighbourID[i])) {
                                    //Read neighbour node's hostname and port number and store it
                                    if (splitLine.length >= 3) {
                                        hostName = splitLine[1];
                                        portNum = Integer.valueOf(splitLine[2]);
                                        if (!Node.neighbours.containsKey(Integer.valueOf(splitLine[0]))) {
                                            ArrayList<String> hostPort = new ArrayList<String>();
                                            hostPort.add(0, hostName);
                                            hostPort.add(1, portNum.toString());
                                            Node.neighbours.put(Integer.valueOf(splitLine[0]), hostPort);
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        scanForAddress.close();
                    }
                }
            }

        }
        catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        catch (IOException e1) {
            e1.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (br != null)
                    br.close();
                if (fr != null)
                    fr.close();
            }
            catch (IOException ex) {
                //ex.printStackTrace();
            }
        }
    }
}
