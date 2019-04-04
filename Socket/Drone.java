import java.io.IOException;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.util.List;

public class Drone
{
    boolean Lider = false; 
    boolean Mensajero = false;
    boolean Incendio = false;
    boolean SensorIncendio = false;

    public static void main(String[] args) throws IOException{
        
        Drone droneObj = new Drone();
        //Thread server = new DroneServer(droneObj);
        //server.start();
        droneObj.startDrone();

    }

    void startDrone(){
        discovery();
    }

    //Función que descubre cuántos drones hay en el escenario
    void discovery(){
        System.out.println(sendBroadcast("Hello"));
    }

    boolean sendBroadcast(String msg){
        InetAddress address;
        try{
            address = InetAddress.getByName("localhost");
            DatagramSocket ds = new DatagramSocket();
            ds.setBroadcast(true);
            byte[] buffer = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer,buffer.length,address,10000);
            ds.send(packet);
            ds.close();
            return true;
        }
        catch(IOException ioe){
            ioe.printStackTrace();
            return false;
        }
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

            ServerSocket ss = new ServerSocket(10000); 
            // running infinite loop for getting
            // client request
            Socket s = null;
            System.out.println("[Drone]: Starting connection server");
            s = ss.accept();
            System.out.println("[Drone]: A new client is connected: " + s.getInetAddress());
            
            // obtaining input and out streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            System.out.println("[Drone]: Assigning new thread for this communication");
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

        boolean Lider = false; 
        boolean Mensajero = false;
        boolean Incendio = false;
        boolean SensorIncendio = false;
        //while (true){
            try {
                
                // Ask user what he wants
                //dos.writeUTF("Que pasa?..\n"+
                 //       "Salir para terminar conexion");
                
                // receive the answer from client
                received = dis.readUTF();
                
                if(received.equals("Salir"))
                {
                    System.out.println("Cliente " + this.s + " quiere salir...");
                    System.out.println("Cerrando la conexion.");
                    this.s.close();
                    System.out.println("Conexion cerrada");
                    //break;
                }
                
                // creating Date object
                //Date date = new Date();
                
                // write on output stream based on the
                // answer from the client
                switch (received) {
                    
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
                }
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
