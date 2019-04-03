/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//package Drones;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 *
 * @author johor
 */
public class Client3 {


    public static void movimiento(int patrulla){
        
        System.out.println("moviendome a la ubicacion: "+ patrulla);
        
    };
    
    public static boolean deteccionIncendio(){
    
        

        int incendio = (int) (Math.random() * 10) + 1;
        
        if(incendio >  3 && incendio < 6 ){
            System.out.println("Incendio");
            return true;
        }else{
        
        System.out.println("no hay incendio");
        }
        return false;
    }
    
    
    @SuppressWarnings("empty-statement")
    public static void main(String[] args) throws IOException
    {
       
        try
        {
           // Scanner scn = new Scanner(System.in);
            
            
            
            //ip de cada dron est
            InetAddress ip1 = InetAddress.getByName("localhost");
            InetAddress ip2 = InetAddress.getByName("localhost");
            InetAddress ip3 = InetAddress.getByName("localhost");
            
            // estableciendo la coneccion con cada socket
            
            Socket s1 = new Socket(ip1, 101);
            Socket s2 = new Socket(ip2, 202);
            Socket s3 = new Socket(ip3, 303);
            
            
            
            // para obtener los imputs y outputs de cada dron
            
            DataInputStream dis1 = new DataInputStream(s1.getInputStream());
            DataOutputStream dos1 = new DataOutputStream(s1.getOutputStream());
            DataInputStream dis2 = new DataInputStream(s2.getInputStream());
            DataOutputStream dos2 = new DataOutputStream(s2.getOutputStream());
            DataInputStream dis3 = new DataInputStream(s3.getInputStream());
            DataOutputStream dos3 = new DataOutputStream(s3.getOutputStream());
            
            
            // este loop perminte el hacer el cambio de info con todos los servidores
            int counter = 0;
            boolean drone1, drone2, drone3 = false;
            boolean incendio = false;
            while (true)
            {
                
                //podriamos indicar aqui quien tiene el rol de lider
                //System.out.println(dis1.readUTF());
                //String tosend = scn.nextLine();
                //dos1.writeUTF(tosend);
                System.out.println("Iniciando "+ counter);
                
                //pregunta a los demas drones quien es el lider
                dos1.writeUTF("Lider");
                dos2.writeUTF("Lider");
                dos3.writeUTF("Lider");
                String received1 = dis1.readUTF();
                String received2 = dis2.readUTF();
                String received3 = dis3.readUTF();
                
                //boolean received2 = dis1.readBoolean();
                System.out.println(received1);
                System.out.println(received2);
                System.out.println(received3);
                
                
                // aquí van todos los comandos necesarios por parte del cliente             
                if(received1.equalsIgnoreCase("true")){
                    System.out.println("El lider es el dron 1");
                    drone1 = true;
                    drone2 = drone3 = false;
               
                dos1.writeUTF("dir");
                received1 = dis1.readUTF();
               
                System.out.println(received1);
                switch(received1){
                    case "coordenadas":
                        movimiento(123);
                        break;
                    case "incendio":
                        
                        System.out.println("incendio cliente 3");
                        incendio = true;
                        dos3.writeUTF("mensajero");
                        received3 = dis3.readUTF();
                        System.out.println(received3);
                        if(received3.equalsIgnoreCase("true")){
                            System.out.println("soy el mensajero enviando coordenadas del incendio");
                        }
                        else{
                        
                            System.out.println("rodeando incendio");
                        
                        }
                        
                        break;
                                               
                };
                }
                //En caso que el dron 2 sea el Lider
                if(received2.equalsIgnoreCase("true")){
                    System.out.println("El lider es el dron 2");
                    drone2 = true;
                    drone1 = drone3 = false;
               
                dos2.writeUTF("dir");
                received2 = dis2.readUTF();
               
                System.out.println(received2);
                switch(received2){
                    case "coordenadas":
                        movimiento(123);
                        break;
                    case "incendio":
                        
                        incendio = true;
                        dos3.writeUTF("mensajero");
                        received3 = dis3.readUTF();
                        if(received3.equalsIgnoreCase("true")){
                            System.out.println("soy el mensajero enviando coordenadas del incendio");
                        }
                        else{
                        
                            System.out.println("rodeando incendio");
                        
                        }
                                               
                };
                }
                //para el dron3
                if(received3.equalsIgnoreCase("true")){
                    System.out.println("El lider es el dron 3");
                    drone3 = true;
                    drone2 = drone1 = false;
               
                dos3.writeUTF("dir");
                received3 = dis3.readUTF();
               
                System.out.println(received3);
                switch(received3){
                    case "coordenadas":
                        movimiento(123);
                        break;
                    case "incendio":
                        
                        incendio = true;
                        dos3.writeUTF("mensajero");
                        received3 = dis3.readUTF();
                        if(received3.equalsIgnoreCase("true")){
                            System.out.println("soy el mensajero enviando coordenadas del incendio");

                        }
                        else{
                        
                            System.out.println("rodeando incendio");
                        
                        }
                        break;
                                               
                };
                }
                               
                

                dos3.writeUTF("sensorincendio");
                received3 = dis3.readUTF();

                
                if(!incendio && received3.equalsIgnoreCase("true")){
                    //nuevo lider
                    
                    dos1.writeUTF("nlider");
                    dos2.writeUTF("nlider");
                    dos3.writeUTF("soylider");
                    
                    dos1.writeUTF("incendio");
                    dos2.writeUTF("incendio");
                    dos3.writeUTF("incendio");
                    
                    dos1.writeUTF("bateria");
                    dos2.writeUTF("bateria");
                    dos3.writeUTF("bateria");
                    
                    received1 = dis1.readUTF();
                    received2 = dis2.readUTF();
                    received3 = dis3.readUTF();
                    System.out.println("entradas bateria: "+received1+received2+received3);
                    
                    int bateria1 = Integer.parseInt(received1);
                    int bateria2 = Integer.parseInt(received2);
                    int bateria3 = Integer.parseInt(received3);
                    
                    
                    if(bateria1 > bateria2 && bateria1 > bateria3){
                        //dron1 tiene la bateria mayor
                        dos1.writeUTF("soymensajero");
                        dos2.writeUTF("soylider");
                        dos3.writeUTF("nlider");
                        dos1.writeUTF("nlider");
                    }else{
                        if(bateria2 > bateria1 && bateria2 > bateria3){
                            //dron2 tiene la bateria mayor
                            dos1.writeUTF("nlider");
                            dos2.writeUTF("nlider");
                            dos2.writeUTF("soymensajero");
                            dos3.writeUTF("soylider");
                        }else{
                            //dron3 tiene la bateria mayor
                            dos1.writeUTF("soylider");
                            dos2.writeUTF("nlider");
                            dos3.writeUTF("nlider");
                            dos3.writeUTF("soymensajero");                         
                        }
                    }
                    
                   
     
                    
                }
                
                if(counter > 6){
                    System.out.println("Cerrando esta conexion: " + s1);
                    dos1.writeUTF("Salir");
                    dos2.writeUTF("Salir");
                    dos3.writeUTF("Salir");
                    s1.close();
                    s2.close();
                    s3.close();
                    
                    System.out.println("Conexion cerrada");
                    break;
                }
                counter +=1;
                //esperar 5 segundos entre cada llamada
                 Thread.sleep(1*1000);
                 System.out.println("fin de la vuelta");
                 
            }
            System.out.println("saliendo pip pip ");
            // closing resources
            //scn.close();
            dis1.close();
            dos1.close();
            dis2.close();
            dos2.close();
            dis3.close();
            dos3.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
} 
