package dev;

import java.util.Random;

public class MapProtocol extends Thread{

	public int computeMessagesSent(int minPerActive, int maxPerActive){
		int difference = maxPerActive - minPerActive;
		int messageCount=0;
		Random rnd = new Random(difference);	//generates a number between 0 and difference
		int offset = rnd.nextInt();
		messageCount = offset + minPerActive;	//minPeractive messages + offset gives me the new pseudo-random number of messages to be sent
		if(App.sentMsgCount + messageCount > App.maxNumberMsgs){		// if I cant send the msgCount # of messages, I will send howmuch ever I can
			return App.maxNumberMsgs - App.sentMsgCount;
		}
		return messageCount;		//otherwise send the msgCount # of messages
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(App.mapProtocolTerminationFlag == false){
			if(App.isProcessActive == true){
				int messageCount = computeMessagesSent(App.minPerActive, App.maxPerActive);	//get a random message count
				for(int i=0; i<messageCount; i++){
						Message m = new Message(MessageType.App, App.self.getNodeId(), App.vectorClock, null, false);
						
				}
				
			}			
		}
		
	}

}
