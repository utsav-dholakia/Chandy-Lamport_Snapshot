package dev;

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
                    break;

                case Marker:

                    break;

                case Snapshot:
                    break;
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}