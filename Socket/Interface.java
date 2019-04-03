//package Main;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.DataOutputStream;
import java.util.StringTokenizer;
import java.net.InetAddress;
import java.net.Socket;

public class Interface{
	
	boolean messageSent;

	void readFile(String fileString){
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
					System.out.println("[Interface]: Drone "+choosenDrone+1+" detected fire");
					
					sendMessage(choosenDrone,"[Interface]: fire");
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
			switch(choosenDrone){
				//Netlogo inicia en 0 a sus agentes y termina en 2 (para el caso de 3 drones)
				//Considerar que aquí 
				case 0:
					address = InetAddress.getByName("localhost");
					s = new Socket(address, 101);
					break;
				case 1:
					address = InetAddress.getByName("localhost");
					s = new Socket(address, 202);
					break;
				case 2:
					address = InetAddress.getByName("localhost");
					s = new Socket(address, 303);
					break;
				default:
					s = null;
			}

			System.out.println("[Interface]: Sending message -> "+inputMsg);
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			dos.writeUTF(inputMsg);
			messageSent = true;
			//s.close();

		}

		catch(IOException ioe){
			ioe.printStackTrace();
		}
		
	}

	public static void main(String[] args) {
		Interface interfaceObj = new Interface();
		interfaceObj.messageSent = false;
		interfaceObj.readFile("C:\\Users\\gamaa\\Documents\\Software Projects\\DistribuitedFireDetection\\NetLogo\\NetLogo-output");
	}

}