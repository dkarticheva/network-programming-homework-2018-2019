package com.fmi.mpr.hw.chat; 

import java.io.IOException;
import java.net.SocketException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.io.FileOutputStream;
import java.io.File;

public class Reader implements Runnable {
	
    public static int BUFFER_SIZE = 8192;

    private Participant participant;
    private MulticastSocket socket;
    private int port;
    private InetAddress address;
	
    Reader(Participant participant, MulticastSocket ms, int p, InetAddress adr) {
        this.participant = participant;
        socket = ms;
        port = p;
        address = adr;
    }

    @Override
    public void run() {
        //boolean chatting = true; 
        while (participant.chatting) {

            byte[] buff = new byte[BUFFER_SIZE];
            DatagramPacket packet = new DatagramPacket(buff,buff.length);
            try {
                // message format TEXT : message
                socket.receive(packet);             
                String message = new String(packet.getData(), packet.getOffset(), packet.getLength());
                String[] mess = message.split(" ");
                if (mess[0].equals("TEXT")) {
                    System.out.println("The message you sent was: " + mess[2]);
                    System.out.println("You can enter another TEXT,IMAGE or VIDEO message: ");
                }
                else if (mess[0].equals("IMAGE") || mess[0].equals("VIDEO")) {
                    FileOutputStream os = new FileOutputStream(new File("testname.jpg"));
                    while (true) {
                        socket.receive(packet);
                        if (new String(packet.getData(), packet.getOffset(), packet.getLength())
                            .equals("END")) {
                            System.out.println("File " + mess[2] + " received!");
                            break;
                        }
                        os.write(packet.getData(), packet.getOffset(), packet.getLength());
                    }
                }
            } catch (SocketException e) {
                System.out.println("You have successfully logged out!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
} 

