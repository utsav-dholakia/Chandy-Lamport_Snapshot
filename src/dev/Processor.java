package dev;

import java.util.HashMap;
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
                    App_Message appMessage = (App_Message) inMessage;
                    //Find out clock value for a node in message pertaining to the nodeID mentioned
                    Integer clockValueInMessage = appMessage.getTimeStamp().get(appMessage.getSrcNodeID());
                    //Find out local clock value for a node
                    Integer clockValueLocal = App.vectorClock.get(appMessage.getSrcNodeID());
                    Integer maxValue = Math.max(clockValueInMessage, clockValueLocal);
                    //Set local clock value = Max of both above + 1
                    App.vectorClock.set(appMessage.getSrcNodeID(), maxValue + 1);
                    break;

                case Marker:
                    Marker_Message markerMessage = (Marker_Message)inMessage;
                    //Find out local clock value for a node
                    clockValueLocal = App.vectorClock.get(markerMessage.getSrcNodeID());
                    //Set channelState value for this snapshot to a value pertaining to the node mentioned in the message
                    TreeMap<Integer, Integer> channelState = App.channelStates.get(markerMessage.getSnapshot_ID());
                    channelState.put(markerMessage.getSrcNodeID(), clockValueLocal);
                    App.channelStates.set(markerMessage.getSnapshot_ID(), channelState);
                    break;

                case MapTermination:
                    break;
                case Snapshot:
                    break;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}