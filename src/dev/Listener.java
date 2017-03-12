package dev;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by utsavdholakia on 3/12/17.
 */
public class Listener extends Thread{
    Integer currPortNum;
    boolean serverOn = true;
    public static BlockingQueue<Message> messagesToBeProcessed = new LinkedBlockingQueue<Message>();

    Listener(Integer portNum){
        this.currPortNum = portNum;
    }

    @Override
    public void run() {
        ServerSocket serverSocket;
        try{
            //Initialize the receiver as a continuous listening server
            serverSocket = new ServerSocket();
            System.out.println("Listening on port : " + currPortNum);
            while (serverOn) {
                Socket sock = serverSocket.accept();
                System.out.println("Connected");
                //Enter a message that is received into the queue to be processed
                messagesToBeProcessed.put((Message) new ObjectInputStream(sock.getInputStream()).readObject());
                //Initiate thread of a class to process the messages one by one from queue
                Processor processor = new Processor();
                //Create a new thread only if no thread exists
                if(!processor.isAlive()){
                    new Thread(processor).start();
                }
            }
        } catch(Exception e){
            System.out.println("Could not create server on port number : " + currPortNum );
            e.printStackTrace();
        }
    }
}