package dev;

import java.util.TreeMap;
import java.util.Vector;

/**
 * Created by utsavdholakia on 3/11/17.
 */

enum MessageType {
    App,
    Marker,
    MapTermination,
    Snapshot;
}

public class Message {
    MessageType messageType;
    Integer srcNodeID;
    Vector<Integer> timeStamp;
    Integer snapshotID;
    boolean mapTermination;

    public Message(MessageType messageType, Integer srcNodeID, Vector<Integer> timeStamp, Integer snapshotID,
                   boolean mapTermination) {
        this.messageType = messageType;
        this.srcNodeID = srcNodeID;
        this.timeStamp = timeStamp;
        this.snapshotID = snapshotID;
        this.mapTermination = mapTermination;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public Integer getSrcNodeID() {
        return srcNodeID;
    }

    public void setSrcNodeID(Integer srcNodeID) {
        this.srcNodeID = srcNodeID;
    }

    public Vector<Integer> getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Vector<Integer> timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Integer getSnapshotID() {
        return snapshotID;
    }

    public void setSnapshotID(Integer snapshotID) {
        this.snapshotID = snapshotID;
    }

    public boolean isMapTermination() {
        return mapTermination;
    }

    public void setMapTermination(boolean mapTermination) {
        this.mapTermination = mapTermination;
    }


}

/*
class Snapshot_Message extends Message{
    boolean nodeActive;
    TreeMap<Integer, Integer> channelStates;

    public Snapshot_Message(MessageType messageType, Integer srcNodeID, boolean nodeActive, TreeMap<Integer, Integer> channelStates) {
        super(messageType, srcNodeID);
        this.nodeActive = nodeActive;
        this.channelStates = channelStates;
    }

    public boolean isNodeActive() {
        return nodeActive;
    }

    public void setNodeActive(boolean nodeActive) {
        this.nodeActive = nodeActive;
    }

    public TreeMap<Integer, Integer> getChannelStates() {
        return channelStates;
    }

    public void setChannelStates(TreeMap<Integer, Integer> channelStates) {
        this.channelStates = channelStates;
    }
}
*/
