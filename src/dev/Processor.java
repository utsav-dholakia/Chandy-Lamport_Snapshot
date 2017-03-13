package dev;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;

/**
 * Created by utsavdholakia on 3/12/17.
 */
public class Processor extends Thread{

    @Override
    public void run(){
        try {

            Message inMessage = Listener.messagesToBeProcessed.take();

            switch(inMessage.getMessageType()){
                case App:
                    //Find out clock value for a node in message pertaining to the nodeID mentioned
                    Integer clockValueInMessage = inMessage.getTimeStamp().get(inMessage.getSrcNodeID());
                    //Find out local clock value for a node
                    Integer clockValueLocal = App.vectorClock.get(inMessage.getSrcNodeID());
                    Integer maxValue = Math.max(clockValueInMessage, clockValueLocal);
                    //Set local clock value = Max of both above + 1
                    App.vectorClock.set(inMessage.getSrcNodeID(), maxValue + 1);
                    //If received an app message and total messages sent is less than max number of messages allowed then set process active
                    if(App.sentMsgCount < App.maxNumberMsgs)
                        App.isProcessActive = true;
                    break;

                case Marker:
                    //Find out local clock value for a node
                    clockValueLocal = App.vectorClock.get(inMessage.getSrcNodeID());
                    //Set channelState value for this snapshot to a value pertaining to the node mentioned in the message
                    TreeMap<Integer, Integer> channelState = App.channelStates.get(inMessage.getSnapshotID());
                    channelState.put(inMessage.getSrcNodeID(), clockValueLocal);
                    App.channelStates.set(inMessage.getSnapshotID(), channelState);

                    //If marker message is not sent to neighbors for this snapshot ID
                    sendMarkerMessages(inMessage);
                    break;

                case MapTermination:
                    App.mapProtocolTerminationFlag = true;
                    App.maxSnapshotID = inMessage.getSnapshotID();
                    break;

                case NodePassive:
                    if(App.self.getNodeId() == 0){
                        //Set node is passive flag true for the node which has sent the nodepassive message
                        App.nodesPassive.add(inMessage.getSrcNodeID());
                        //Find if all the nodes are passive or not
                        if(App.nodesPassive.size() == App.tempMap.keySet().size()){
                            App.stopMapProtocolsMessageSent = true;
                            //Indiate each node has to terminate their map protocol, and they have to mark max snapshot value
                            Message message = new Message(MessageType.MapTermination, 0, null, App.snapshotNumber);
                            sendMapTerminationMessages(message);
                        }
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendMarkerMessages(Message inMessage){
        if(!App.markerMessageSent.get(inMessage.getSnapshotID())) {
            Socket clientSocket;
            Iterator<Integer> it = App.nodeMap.keySet().iterator();
            //Change source node ID to my source node ID
            inMessage.setSrcNodeID(App.self.getNodeId());
            while (it.hasNext()) {
                try {
                    Node neighbor = App.nodeMap.get(it.next());
                    clientSocket = new Socket(neighbor.getNodeAddr(), neighbor.getPort());
                    ObjectOutputStream outMessage = new ObjectOutputStream(clientSocket.getOutputStream());
                    outMessage.writeObject(inMessage);
                    clientSocket.close();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
    }

    public static void sendMapTerminationMessages(Message inMessage){
        Socket clientSocket;
        Iterator<Integer> it = App.tempMap.keySet().iterator();
        while (it.hasNext()) {
            try {
                Node neighbor = App.tempMap.get(it.next());
                clientSocket = new Socket(neighbor.getNodeAddr(), neighbor.getPort());
                ObjectOutputStream outMessage = new ObjectOutputStream(clientSocket.getOutputStream());
                outMessage.writeObject(inMessage);
                clientSocket.close();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }
}