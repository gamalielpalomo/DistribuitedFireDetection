import java.io.IOException;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

import Global.Globals;

public class Drone
{
    boolean Lider = false; 			//"Lider" es true/false si el drone es el liner o no
    InetAddress whoIsLeader = null;
    boolean SensorIncendio = false;
    boolean Consenso = false; //If the drone is in a consensus, this variable is true, else false.
    //MsgArrived variable tells us if a new message was sent to us. This is used before
    // the consensus protocol
    boolean MsgArrived = false; 
    boolean Mensajero = false;
    boolean Incendio = false;

    ArrayList<InetAddress> neighbours = new ArrayList<InetAddress>();
    ArrayList<InetAddress> listCopy = new ArrayList<InetAddress>(neighbours);

    public static void main(String[] args) throws IOException{
        Drone droneObj = new Drone();
        Thread server = new DroneServer(droneObj);
        Thread mserver = new DroneMulticastServer(droneObj);
        server.start();
        mserver.start();
        droneObj.startDrone();

    }

    void startDrone(){
        discovery();
    }

    //Función que descubre cuántos drones hay en el escenario
    void discovery(){
    	try{
    		if(sendMulticast("Hello"))
            System.out.println("[Drone]: Discovery message sent successfully");
	        while(true){
	        	Thread.sleep(5000);
	        	if(!MsgArrived){
	        		System.out.println("[Drone]: Tolerance time finished");
	        		break;
	        	}
	        	MsgArrived = false;
	        }
	        
	        //A partir de aqui comienza el preconsenso
	        Consenso = true;
            System.out.println("\n--------- Starting pre-consensus ---------\n");
	        if(neighbours.size()==0){
	        	System.out.println("[Drone]: I'm alone, becoming leader");
	        	Lider = true;
	        }
	        else if(whoIsLeader==null)
	        	requestConsensus();
            ArrayList<InetAddress> listCopy = new ArrayList<InetAddress>(neighbours);
            //Despues de enviar solicitud de consenso a 
	        Consenso = false;
	    }
        catch(InterruptedException ie){
			ie.printStackTrace();
		}
	}
	

    void requestConsensus(){
   		for(InetAddress element : neighbours){
    		System.out.println("[Drone]: Sending consensus request to "+element);
			sendMessage(element,"-,-,consensus,-");
    	}
    }

    boolean sendMessage(InetAddress target, String inputMsg){
		try{
			InetAddress address = target; 
			Socket s = new Socket(address, Globals.ServerPort);
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			Random rnd = new Random();
			//Thread.sleep(rnd.nextInt(1000));
			dos.writeUTF(inputMsg);
			s.close();
			return true;
		}
		catch(IOException ioe){
			ioe.printStackTrace();
			return false;
		}
		/*catch(InterruptedException ie){
			ie.printStackTrace();
			return false;
		}*/
	}

    boolean sendMulticast(String msg){
        InetAddress group;
        try{
            DatagramSocket ds = new DatagramSocket();
            group = InetAddress.getByName(Globals.groupAddress);
            byte[] buffer = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer,buffer.length,group,Globals.MulticastServerPort);
            ds.send(packet);
            ds.close();
            return true;
        }
        catch(IOException ioe){
            ioe.printStackTrace();
            return false;
        }
    }

    void addNeighbour(InetAddress newNeighbour){
    	if(!neighbours.contains(newNeighbour))
            System.out.println("[DroneServer]: Adding new neighbour -> "+newNeighbour);
    		neighbours.add(newNeighbour);
            listCopy.add(newNeighbour);
    }

}

class DroneServer extends Thread{
    final Drone droneRef;
    public DroneServer(Drone droneRef){
        this.droneRef = droneRef;
    }
    @Override
    public void run(){
        try{

            ServerSocket ss = new ServerSocket(Globals.ServerPort); 
            // running infinite loop for getting
            // client request
            Socket s = null;
            System.out.println("[DroneServer]: Starting connection server");
            while(true){
            	s = ss.accept();
	            //System.out.println("[DroneServer]: A new client is connected: " + s.getInetAddress());
	            
	            // obtaining input and out streams
	            DataInputStream dis = new DataInputStream(s.getInputStream());
	            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
	            //System.out.println("[DroneServer]: Assigning new thread for this communication");
	            Thread t = new DroneClientHandler(s, dis, dos, droneRef);
	            t.start();
            }

        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
}

class DroneClientHandler extends Thread
{
    
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    final Drone droneRef;

    // Constructor
    public DroneClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, Drone droneRef)
    {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
        this.droneRef = droneRef;
    }
    
    public String bateria(){
        
        String Porcentaje = Integer.toString((int) (Math.random() * 100) + 1);
        
        return Porcentaje;
    }
    
    @Override
    public void run()
    {
        String received;
        //while (true){
            try {
                
                // Ask user what he wants
                //dos.writeUTF("Que pasa?..\n"+
                 //       "Salir para terminar conexion");
                
                // receive the answer from client
                received = dis.readUTF();
                
                /*
                if(received.equals("Salir"))
                {
                    System.out.println("Cliente " + this.s + " quiere salir...");
                    System.out.println("Cerrando la conexion.");
                    this.s.close();
                    System.out.println("Conexion cerrada");
                    //break;
                }
                */
                
                // creating Date object
                //Date date = new Date();
                
                // write on output stream based on the
                // answer from the client

                /* We split here the received message applying the defined format:

					splitMsg[0] -> The sender is leader? true/false and it is a reply for a "Hello" message
					splitMsg[1] -> This is the place in the message where the fire sensor sends true/false
					splitMsg[2] -> future purposes
					splitMsg[3] -> future purposes

                */
				System.out.println("[DroneServer]: Message received from "+s.getInetAddress()+"-> "+received);
                String splitMsg[] = received.split(",");

                if (splitMsg[0].equals("true")){
                	droneRef.whoIsLeader = s.getInetAddress();
                	droneRef.addNeighbour(s.getInetAddress());

                }
                else if(splitMsg[0].equals("false")){
                	droneRef.addNeighbour(s.getInetAddress());
                }
                else if(splitMsg[1].equals("true")){
                	System.out.println("[DroneServer]: Fuego detectado!");
                    this.droneRef.SensorIncendio = true;
                }
                else if(splitMsg[2].equals("consensus")){
                    System.out.println("[DroneServer]: Eliminando "+s.getInetAddress());
                    droneRef.listCopy.remove(s.getInetAddress());
                }
                /*switch (received) {
                    
                    // hay lider
                    case "Lider":
                        
                        if(Lider){
                            dos.writeUTF("true");
                        }else{
                        dos.writeUTF("false");
                        }    
                        break;
                        
                    case "dir" :
                        
                        if(Incendio && Lider){
                            dos.writeUTF("incendio");
                        }else{
                            dos.writeUTF("coordenadas");
                        }
                        break;
                        
                    case "soylider" :
                       Lider = true;
                       //quitar antiguo mando 
                       //ordenes de incendio 
                       break;
                       
                    case "nlider":
                        if(Lider){
                            Lider = false;
                        }
                        break;
                        
                    case "incendio":
                        Incendio = true;
                        break;

                    case "sensorincendio":
                        if(SensorIncendio == true)
                            dos.writeUTF("true"); 
                        else
                            dos.writeUTF("false");
                        break;
                        
                    case "soymensajero":
                        Mensajero = true;
                        break;
                    case "mensajero":
                        if(Mensajero){
                            dos.writeUTF("true");
                            break;
                        }
                        
                    case "bateria":
                        System.out.println("porcentaje de bateria");
                        int bateria = (int) (Math.random() * 100) + 1;
                        
                        System.out.println("porcentaje de bateria "+ bateria );
                        dos.writeUTF(Integer.toString(bateria));
                        break;

                    case "[Interface]: fire":
                        System.out.println("[Thread]: Fuego detectado!");
                        this.droneRef.SensorIncendio = true;
                        //SensorIncendio = true;
                        break;
                    default:
                        dos.writeUTF("input no valido");
                        break;
                }*/
            } catch (IOException e) {
                System.out.println("[Thread]: Exception");
                e.printStackTrace();
            }
        //}
        
        try
        {
            // closing resources
            this.dis.close();
            this.dos.close();
            this.s.close();
            
        }catch(IOException e){
            e.printStackTrace();
        }
    }
} 

class DroneMulticastServer extends Thread{
    final Drone droneRef;
    public DroneMulticastServer(Drone droneRef){
        this.droneRef = droneRef;
    }
    @Override
    public void run(){
        try{

            System.setProperty("java.net.preferIPv4Stack","true");//This line is used for specifying the prefered interface as IPv4
            String localInetAddress = InetAddress.getLocalHost().getHostAddress();
            System.out.println("[DroneMulticastServer]: Starting multicast server on -> "+localInetAddress);
            MulticastSocket ms = new MulticastSocket(Globals.MulticastServerPort);
            InetAddress group = InetAddress.getByName(Globals.groupAddress);
            byte[] buffer = new byte[256];
            ms.joinGroup(group);
            while(true){
                DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
                ms.receive(dp);
	            if(!dp.getAddress().toString().equals("/"+localInetAddress) && !droneRef.Consenso){
	                String inputMsg = new String(dp.getData(),0,dp.getLength());
	                System.out.println("[DroneMulticastServer]: A new multicast message from " + dp.getAddress() + " was received -> "+inputMsg);
	                
	                switch(inputMsg){
	                	case "Hello":
	                		droneRef.sendMessage(dp.getAddress(),droneRef.Lider+",-,-,-");
	                		//Quiere decir que es un nuevo dron en el escenario, y está buscando a alguien más
	                		droneRef.MsgArrived = true;
	                		droneRef.addNeighbour(dp.getAddress());
	                		break;
	                }

	                if("end".equals(inputMsg))
	                    break;
                }
            }
            ms.leaveGroup(group);
            ms.close();

        }
        catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
}
