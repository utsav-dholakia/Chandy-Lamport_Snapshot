package dev;

import java.util.TreeMap;
import java.util.Vector;

/**
 * Created by utsavdholakia on 3/11/17.
 */

enum MessageType {
    App,
    Marker,
    Snapshot;
}

public class Message {
    MessageType messageType;
    Integer srcNodeID;

    public Message(MessageType messageType, Integer srcNodeID) {
        this.messageType = messageType;
        this.srcNodeID = srcNodeID;
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
}

class App_Message extends Message{
    Vector<Integer> timeStamp;

    public App_Message(MessageType messageType, Integer srcNodeID, Vector<Integer> timeStamp) {
        super(messageType, srcNodeID);
        this.timeStamp = timeStamp;
    }

    public Vector<Integer> getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Vector<Integer> timeStamp) {
        this.timeStamp = timeStamp;
    }
}

class Marker_Message extends Message{
    Integer snapshot_ID;

    public Marker_Message(MessageType messageType, Integer srcNodeID, Integer snapshot_ID) {
        super(messageType, srcNodeID);
        this.snapshot_ID = snapshot_ID;
    }

    public Integer getSnapshot_ID() {
        return snapshot_ID;
    }

    public void setSnapshot_ID(Integer snapshot_ID) {
        this.snapshot_ID = snapshot_ID;
    }
}

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
