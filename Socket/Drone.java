import java.io.IOException;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.MulticastSocket;
import java.util.ArrayList;

public class Drone
{
    boolean Lider = false; 			//"Lider" es true/false si el drone es el liner o no
    boolean Mensajero = false;
    boolean Incendio = false;
    boolean SensorIncendio = false;

    ArrayList<InetAddress> neighbours = new ArrayList<InetAddress>();

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
        if(sendMulticast("Hello"))
            System.out.println("[Drone]: Discovery message sent successfully");
    }

    boolean sendMessage(InetAddress target, String inputMsg){
		try{

			InetAddress address = target; 
			Socket s = new Socket(address, 11000);
			DataOutputStream dos = new DataOutputStream(s.getOutputStream());
			dos.writeUTF(inputMsg);
			s.close();
			return true;
		}
		catch(IOException ioe){
			ioe.printStackTrace();
			return false;
		}
	}

    boolean sendMulticast(String msg){
        InetAddress group;
        try{
            DatagramSocket ds = new DatagramSocket();
            group = InetAddress.getByName("224.0.0.20");
            byte[] buffer = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer,buffer.length,group,10000);
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
    		neighbours.add(newNeighbour);
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

            ServerSocket ss = new ServerSocket(11000); 
            // running infinite loop for getting
            // client request
            Socket s = null;
            System.out.println("[DroneServer]: Starting connection server");
            s = ss.accept();
            System.out.println("[DroneServer]: A new client is connected: " + s.getInetAddress());
            
            // obtaining input and out streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            System.out.println("[DroneServer]: Assigning new thread for this communication");
            Thread t = new DroneClientHandler(s, dis, dos, droneRef);
            t.start();
        

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
					splitMsg[1] -> future purposes
					splitMsg[2] -> future purposes
					splitMsg[3] -> future purposes

                */
				System.out.println("[DroneServer]: Message received -> "+received);
                String splitMsg[] = received.split(",");

                if (splitMsg[0].equals("true")){
                	System.out.println("[DroneServer]: Adding new neighbour -> "+s.getInetAddress());
                	droneRef.addNeighbour(s.getInetAddress());
                	
                }
                else if(splitMsg[0].equals("false")){
                	System.out.println("[DroneServer]: Adding new neighbour -> "+s.getInetAddress());
                	droneRef.addNeighbour(s.getInetAddress());
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

            System.setProperty("java.net.preferIPv4Stack","true");
            String localInetAddress = InetAddress.getLocalHost().getHostAddress();
            System.out.println("[DroneMulticastServer]: InetAddress -> "+localInetAddress);
            MulticastSocket ms = new MulticastSocket(10000);
            InetAddress group = InetAddress.getByName("224.0.0.20");
            byte[] buffer = new byte[256];
            ms.joinGroup(group);
            while(true){
                DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
            	System.out.println("[DroneMulticastServer]: Multicast server started, listening...");
                ms.receive(dp);
	            if(!dp.getAddress().toString().equals("/"+localInetAddress)){
	                String inputMsg = new String(dp.getData(),0,dp.getLength());
	                System.out.println("[DroneMulticastServer]: A new multicast message from " + dp.getAddress() + " was received -> "+inputMsg);
	                
	                switch(inputMsg){
	                	case "Hello":
	                		droneRef.sendMessage(dp.getAddress(),droneRef.Lider+",-,-,-");
	                		//Quiere decir que es un nuevo dron en el escenario, y está buscando a alguien más
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
