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
            if(inMessage.messageType != MessageType.Marker)
                System.out.println(inMessage.getSrcNodeID() + " : " + inMessage.getMessageType());
            else
                System.out.println(inMessage.getSrcNodeID() + " : " + inMessage.getMessageType() + " ,snapshotID : " + inMessage.getSnapshotID());
            Integer clockValueInMessage;
            Integer clockValueLocal;
            switch(inMessage.getMessageType()){
                case App:
                    for(int index = 0; index < App.totalNodes; index++) {
                        //Find out clock value for a node in message pertaining to the nodeID mentioned
                        clockValueInMessage = inMessage.getTimeStamp().get(index);
                        //Find out local clock value for a node
                        clockValueLocal = App.vectorClock.get(index);
                        Integer maxValue = Math.max(clockValueInMessage, clockValueLocal);
                        //Set local clock value = Max of both above + 1
                        if(App.self.getNodeId() == index) {
                            App.vectorClock.set(index, maxValue + 1);
                        }
                        else {
                            App.vectorClock.set(index, maxValue);
                        }
                    }
                    //If received an app message and total messages sent is less than max number of messages allowed then set process active
                    if(App.isProcessActive == false && (App.sentMsgCount < App.maxNumberMsgs)) {
                        App.isProcessActive = true;
                        sendNodeActiveMessage();
                    }
                    break;

                case Marker:
                    //Set channelState value for this snapshot to a value pertaining to the node mentioned in the message
                    TreeMap<Integer, Integer> channelState;
                    //If the node already has local snapshot for this snapshot ID
                    if(App.channelStates.contains(inMessage.getSnapshotID())) {
                        channelState = App.channelStates.get(inMessage.getSnapshotID());
                        channelState.put(inMessage.getSrcNodeID(), App.vectorClock.get(inMessage.getSrcNodeID()));
                        App.channelStates.set(inMessage.getSnapshotID(), channelState);
                    }
                    //If marker message is not sent to neighbors for this snapshot ID and we don't have local snapshot for this snapshot ID yet
                    else if(!App.markerMessageSent.containsKey(inMessage.getSnapshotID()) && !App.channelStates.contains(inMessage.getSnapshotID())) {
                        System.out.println("Sending marker message for snapshotID : " + inMessage.getSnapshotID());
                        channelState = new TreeMap<Integer, Integer>();
                        for(int node = 0; node < App.totalNodes; node++) {
                            channelState.put(node, App.vectorClock.get(node));
                        }
                        App.channelStates.add(inMessage.getSnapshotID(), channelState);
                        //Mark that marker message has been sent for this snapshot ID
                        App.markerMessageSent.put(inMessage.getSnapshotID(), true);
                        sendMarkerMessages(inMessage);
                    }
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
                            //Indicate each node has to terminate their map protocol, and they have to mark max snapshot value
                            Message message = new Message(MessageType.MapTermination, 0, null, App.snapshotNumber);
                            sendMapTerminationMessages(message);
                        }
                    }
                    break;

                case NodeAcive:
                    if(App.self.getNodeId() == 0){
                        //If node is active again then remove it from the nodepassive list
                        if(App.nodesPassive.contains(inMessage.getSrcNodeID())){
                            App.nodesPassive.remove(inMessage.getSrcNodeID());
                        }
                    }
                    break;
            }
        } catch (Exception e) {

        }
    }

    public static void sendMarkerMessages(Message inMessage){
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
                //System.out.println(e);
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
                //System.out.println(e);
            }
        }
    }

    public static void sendNodeActiveMessage() {
        Message alertToCoordinator = new Message(MessageType.NodeAcive,App.self.getNodeId(),null,null);	//control msg to 0 saying I am permanently passive
        try{
            Socket socket = new Socket(App.tempMap.get(0).getNodeAddr(),App.tempMap.get(0).getPort());
            ObjectOutputStream outMessage = new ObjectOutputStream(socket.getOutputStream());
            outMessage.writeObject(alertToCoordinator);
            socket.close();
        }catch(Exception e){

        }
    }
}