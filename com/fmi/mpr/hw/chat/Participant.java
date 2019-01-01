package com.fmi.mpr.hw.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class Participant {
    public static int BUFFER_SIZE = 8192;
	
    private MulticastSocket socket;
    private int port;
    private InetAddress address;
    volatile boolean chatting = true;
    
    Participant(String hostname, int port) throws IOException {
        this.address = InetAddress.getByName(hostname);
        this.port = port;
        this.socket = new MulticastSocket(port);
    }
	
    public void start() {
        Thread reader = new Thread(new Reader(this, socket,port,address));
        reader.start();
        try {
            socket.joinGroup(address);
			
            while (true) {
                System.out.println("You can enter 3 types of messages: TEXT,IMAGE or VIDEO or enter EXIT to leave the chat. Which one do you choose? ");
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                String type = br.readLine();
                if (type.length() > 5) {
                    System.out.println("Please enter valid type of message");
                    continue;
                }
                if (type.equals("EXIT")) {
                    socket.leaveGroup(address);
                    socket.close();
                    chatting = false;                   
                    return;
                }
                System.out.println("You can now enter the message itself");
                String line = br.readLine();
                send(line, type);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }		
    }

    public static void main(String[] args) {
        try {
            Participant p = new Participant("239.0.0.0", 4444);
            p.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

