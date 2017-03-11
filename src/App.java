import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

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


    public static void main(String args[]) {
        String line = null;
        String hostName;
        boolean[] lines;
        try {
            hostName = InetAddress.getLocalHost().getHostName();
            //System.out.println("HOST"+hostName);
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            System.out.println("Error fetching host name!");
        }
        try {
            FileReader fileReader = new FileReader("configuration.txt");
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            int lineCount = 0, linesToRead = 0;

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
                        //ignore first line
                        lineCount++;
                        linesToRead = Integer.parseInt(line.trim());      //Remembering the number of lines to read,say, N
                        lines = new boolean[linesToRead];
                        continue;
                    } else if (lineCount > 0) {
                        if (lineCount <= linesToRead) {
                            //Store dcXX.utdallas and port number
                            String[] sysInfo = line.split("\\s+");
                            if (sysInfo.length == 3) {
                                Node node = new Node();
                                node.setNodeId(Integer.parseInt(sysInfo[0]));
                                node.setNodeAddr(sysInfo[1]);
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
                            tempTopology.put(Integer.parseInt(neighbors[0]), line);
                            if (self.getNodeId() == Integer.parseInt(neighbors[0])) {    //if 1st identifier is me ,then get my neighbors and add to nodeMap
                                for (int i = 1; i < neighbors.length; i++) {
                                    nodeMap.put(Integer.parseInt(neighbors[i]), tempMap.get(Integer.parseInt(neighbors[i])));
                                    discoveredTopology.put(Integer.parseInt(neighbors[i]), new TopologyPayload(1, false));    // already discovered nodes are my neighbors @ 1 hop dist
                                    //System.out.println(">>>>"+neighbors[i]+">>>"+tempMap.get(Integer.parseInt(neighbors[i])));
                                }
                            }
                            lineCount++;
                        }
                    }


                } else {
                    //line doesn't start with numeric value, ignored!
                    continue;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

