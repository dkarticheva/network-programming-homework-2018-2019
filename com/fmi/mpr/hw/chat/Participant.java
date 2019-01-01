package com.fmi.mpr.hw.chat;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
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
	
    private boolean validType(String type) {
    	return (type.equals("TEXT") || type.equals("IMAGE") || type.equals("VIDEO")
    			|| type.equals("EXIT") || type.equals("END"));
    }
    private void send(String message, String type) {
        StringBuilder msgBuild = new StringBuilder();
		
        if(type.equals("TEXT")) {
			
            msgBuild.append("TEXT");
            msgBuild.append(" : ");
            msgBuild.append(message);
            byte[] buf = msgBuild.toString().getBytes();
            DatagramPacket packet = new DatagramPacket(buf,buf.length,address,port);
            try {
                socket.send(packet);
            } catch (IOException e) {
                System.out.println("Issue while sending the text message: " + msgBuild.toString());
                e.printStackTrace();
            }
        }
        else if (type.equals("IMAGE") || type.equals("VIDEO")) {
            File file = new File(message);
            try {
                FileInputStream fis = new FileInputStream(file);
				
                if(type.equals("IMAGE")) {
                    msgBuild.append("IMAGE");
                }
                else {
                    msgBuild.append("VIDEO");
                }
                msgBuild.append(" : ");
                msgBuild.append(file.getName());
                byte[] buf = msgBuild.toString().getBytes();
                DatagramPacket packet = new DatagramPacket(buf,buf.length,address,port);
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    System.out.println("Issue while sending the media message: " + msgBuild.toString());
                    e.printStackTrace();
                }
                byte[] buffer = new byte[BUFFER_SIZE];
                int bytesRead;
                while ((bytesRead = fis.read(buffer, 0, BUFFER_SIZE)) > 0 ) {
                    packet = new DatagramPacket(buffer, bytesRead, address, port);
                    socket.send(packet);
                }
                fis.close();
                socket.send(new DatagramPacket(new String("END").getBytes(), 3, address, port));
                
            } catch (IOException e) {
                System.out.println("Problem while sending the message");
                e.printStackTrace();
            }
        }
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
                if (!validType(type)) {
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


