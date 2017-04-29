package dev;

import java.io.Serializable;
import java.util.TreeMap;
import java.util.Vector;

/**
 * Created by utsavdholakia on 3/11/17.
 */

enum MessageType {
    App,
    Marker,
    MapTermination,
    NodePassive,
    NodeAcive;
}

public class Message implements Serializable{
    MessageType messageType;
    Integer srcNodeID;
    //Only for App messages
    Vector<Integer> timeStamp;
    //Only for Marker messages
    Integer snapshotID;

    public Message(MessageType messageType, Integer srcNodeID, Vector<Integer> timeStamp, Integer snapshotID) {
        this.messageType = messageType;
        this.srcNodeID = srcNodeID;
        this.timeStamp = timeStamp;
        this.snapshotID = snapshotID;
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

}