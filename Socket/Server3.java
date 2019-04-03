/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//package server3;



import java.io.*; 
import java.text.*; 
import java.util.*; 
import java.net.*; 

// Server class
public class Server3
{
    public static void main(String[] args) throws IOException
    {
        
        ServerSocket ss = new ServerSocket(303);
       
        
        // running infinite loop for getting
        // client request
        while (true)
        {
            Socket s = null;
            
            try
            {
                // socket object to receive incoming client requests
                
                s = ss.accept();
                
                System.out.println("A new client is connected : " + s);
                
                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                
                System.out.println("Assigning new thread for this client");
                
                // create a new thread object
                Thread t = new ClientHandler(s, dis, dos);
                
                // Invoking the start() method
                t.start();
                
            }
            catch (Exception e){
                s.close();
                e.printStackTrace();
            }
        }
    }
}

// ClientHandler class
class ClientHandler extends Thread
{
    DateFormat fordate = new SimpleDateFormat("yyyy/MM/dd");
    DateFormat fortime = new SimpleDateFormat("hh:mm:ss");
    
    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    
    
    // Constructor
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos)
    {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
    }

    
    @Override
    public void run()
    {
        String received;

        boolean Lider = true; 
        boolean Mensajero = false;
        boolean Incendio = false;
        boolean SensorIncendio = false;
        while (true)
        {
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
                    break;
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
                        System.out.println("dir requested");
                        if(Incendio && Lider){
                            dos.writeUTF("incendio");
                        }else{
                            dos.writeUTF("coordenadas");
                            System.out.println("cord requested");
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
                        System.out.println("Fuego detectado!");
                        SensorIncendio = true;
                        break;
                    default:
                        dos.writeUTF("input no valido");
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        try
        {
            // closing resources
            this.dis.close();
            this.dos.close();
            
        }catch(IOException e){
            e.printStackTrace();
        }
    }
} 