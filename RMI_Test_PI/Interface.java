//package Main;

import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.DataOutputStream;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.net.InetAddress;
import java.net.Socket;
import java.net.MulticastSocket;
import java.net.DatagramPacket;

import Global.Globals;


public class Interface{
	
	boolean messageSent;
	ArrayList<InetAddress> DronesInets = new ArrayList<InetAddress>();
	InetAddress LeaderFromJava = null;



	void startInterface(String fileString){
		System.out.println("[Interface]: Starting interface with NetLogo");
		try{
			while(!messageSent){
				
				File inputFile = new File(fileString);
				BufferedReader br = new BufferedReader(new FileReader(inputFile));
				int choosenDrone = -1;
				String lectura = br.readLine();

				if(lectura!=null){
					String []result = lectura.split("\\s");
					choosenDrone = Integer.parseInt(result[2]);	
					System.out.println("[Interface]: Drone "+(choosenDrone+1)+" detected fire");
					sendMessage(choosenDrone,"-,fire,-,-");
					messageSent = true;
				}
				Thread.sleep(500);
			}
		}
		catch(FileNotFoundException fnfe){
			fnfe.printStackTrace();
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
		catch(InterruptedException ie){
			ie.printStackTrace();
		}

	}

	void sendMessage(int choosenDrone, String inputMsg){

		try{

			InetAddress address; 
			Socket s;
			address=DronesInets.get(choosenDrone);
			s= new Socket(address, Globals.ServerPort);
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			dos.writeUTF(inputMsg);
			System.out.println("[Interface]: Sending message -> "+inputMsg);
			s.close();

		}

		catch(IOException ioe){
			ioe.printStackTrace();
		}
		
	}

	public static void main(String[] args) {
		Interface interfaceObj = new Interface();
		Thread server = new DroneRegisterServer(interfaceObj);
		server.start();
		interfaceObj.messageSent = false;
		interfaceObj.startInterface(Globals.netLogoOutputFile);
	}
  	void addDroneInet(InetAddress newDroneInet){
    	if(!DronesInets.contains(newDroneInet)){
            System.out.println("[DroneServer]: Adding new DroneInet -> "+newDroneInet);
    		DronesInets.add(newDroneInet);
    	}
    }

    void writeFile() throws IOException{
    	BufferedWriter writer = new BufferedWriter(new FileWriter(Globals.netLogoInputFile,false));
    	writer.write(""+DronesInets.size());
    	writer.write("\n\""+LeaderFromJava+"\"");
    	for(InetAddress element : DronesInets){
    		writer.write("\n\""+element+"\"");
    	}
    	writer.close();
    }
}

class DroneRegisterServer extends Thread{
    final Interface interfaceRef;
    public DroneRegisterServer(Interface interfaceRef){
        this.interfaceRef = interfaceRef;
    }
    @Override
    public void run(){
        try{

            System.setProperty("java.net.preferIPv4Stack","true");//This line is used for specifying the prefered interface as IPv4
            String localInetAddress = InetAddress.getLocalHost().getHostAddress();
            System.out.println("[DroneRegisterServer]: Starting multicast server on -> "+localInetAddress);
            MulticastSocket ms = new MulticastSocket(Globals.MulticastServerInterface);//Aqui abre el socket para enviar un paquete
            InetAddress group = InetAddress.getByName(Globals.groupAddress);
            byte[] buffer = new byte[256];
            ms.joinGroup(group);
            while(true){
                DatagramPacket dp = new DatagramPacket(buffer, buffer.length);//Paquete a enviar
                ms.receive(dp);
                String inputMsg = new String(dp.getData(),0,dp.getLength());
                System.out.println("[DroneResgiterServer]: A new multicast message from " + dp.getAddress() + " was received -> "+inputMsg);
              
                switch(inputMsg){
                	case "Alta":
                		interfaceRef.addDroneInet(dp.getAddress());
                		break;
                	case "Leader":
                		interfaceRef.LeaderFromJava = dp.getAddress();
                		interfaceRef.writeFile();
                		break;
                }

                if("end".equals(inputMsg))
                    break;

            }
            ms.leaveGroup(group);
            ms.close();

        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
}
