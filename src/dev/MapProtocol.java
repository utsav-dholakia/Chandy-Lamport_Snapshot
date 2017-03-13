package dev;

import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

public class MapProtocol extends Thread{

	public int computeMessagesSent(int minPerActive, int maxPerActive){
		int difference = maxPerActive - minPerActive;
		int messageCount=0;
		Random rnd = new Random();	
		int offset = rnd.nextInt(difference);	//generates a number between 0 and difference
		messageCount = offset + minPerActive;	//minPeractive messages + offset gives me the new pseudo-random number of messages to be sent
		if(App.sentMsgCount + messageCount > App.maxNumberMsgs){		// if I cant send the msgCount # of messages, I will send howmuch ever I can
			return App.maxNumberMsgs - App.sentMsgCount;
		}
		return messageCount;		//otherwise send the msgCount # of messages
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		int messageCount=0;

		while(App.mapProtocolTerminationFlag == false){
			if(App.sentMsgCount >= App.maxNumberMsgs){
				App.isProcessActive = false;
				Message alertToCoordinator = new Message(MessageType.NodePassive,App.self.getNodeId(),null,null);	//control msg to 0 saying I am permanently passive
				try{
					Socket socket = new Socket(App.tempMap.get(0).getNodeAddr(),App.tempMap.get(0).getPort());
					ObjectOutputStream outMessage = new ObjectOutputStream(socket.getOutputStream());
		            outMessage.writeObject(alertToCoordinator);
		            socket.close();
				}catch(Exception e){
					e.printStackTrace();
				}
				break;
			}
			if(App.isProcessActive == true){
				messageCount = computeMessagesSent(App.minPerActive, App.maxPerActive);	//get a random message count
				//Count number of neighbors I have
				int size = App.nodeMap.size();
				
				for(int i=0; i<messageCount; i++){
					//MessageType messageType, Integer srcNodeID, Vector<Integer> timeStamp, Integer snapshotID,boolean mapTermination,boolean nodePassive
						Message m = new Message(MessageType.App, App.self.getNodeId(), App.vectorClock, null);
						Random rnd = new Random();	
						int choice = rnd.nextInt(size);	// generate a random int between 0 and size-1
						
						//map random no generated to a specific neighbor uniquely
						Iterator<Integer> iter = App.nodeMap.keySet().iterator();
						int neighborSelected=-1;
						for(int j=0;j<=choice;j++){
							neighborSelected = iter.next();
						}
						try{
							//send the application message "m" to that randomly selected node from within my neighborList
							Socket socket = new Socket(App.nodeMap.get(neighborSelected).getNodeAddr(),App.nodeMap.get(neighborSelected).getPort());
							ObjectOutputStream outMessage = new ObjectOutputStream(socket.getOutputStream());
				            outMessage.writeObject(m);		
				            socket.close();
						}catch(Exception e){
							e.printStackTrace();
						}
					//increment vectorClock
						App.vectorClock.set(App.self.getNodeId(), App.vectorClock.get(App.self.getNodeId()+1));
					App.sentMsgCount++;	//increment the total number of messages	
				}
				//After all messages for this cycle have been sent, set process as inactive
				App.isProcessActive = false;
				
			}			
		}
		
	}

}
