import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class RemoteElement implements RemoteElementInterface{
	Drone droneRef;

	public RemoteElement(Drone droneRef){
		super();
		this.droneRef = droneRef;
	}

	public void pushMessage(String msg) throws UnknownHostException,RemoteException{

		String splitMsg[] = msg.split(",");
		InetAddress sender = InetAddress.getByName(splitMsg[4]);

		System.out.println("[DroneRMIServer]: Message received from "+sender+"-> "+msg);
        

        if (splitMsg[0].equals("true")){
        	droneRef.whoIsLeader = sender;
        	droneRef.addNeighbour(sender);

        }
        else if(splitMsg[0].equals("false")){
        	droneRef.addNeighbour(sender);
        }
        else if(splitMsg[1].equals("fire")){
        	System.out.println("[DroneRMIServer]: Fuego detectado!");
            droneRef.SensorIncendio = true;
        }
        else if(splitMsg[1].equals("firestate")){
            System.out.println("[DroneRMIServer]: Someone detected fire, going to fire state, rounding fire zone");
            droneRef.Incendio = true;
        }
        else if(splitMsg[2].equals("consensus")){
            System.out.println("[DroneRMIServer]: Eliminando "+sender);
            droneRef.listCopy.remove(sender);
        }
        else if(splitMsg[2].equals("battery")){
            System.out.println("[DroneRMIServer]: Drone "+sender+" has "+splitMsg[3]+"%");
            droneRef.batteries.put(sender,Integer.parseInt(splitMsg[3]));
        }
        else if(splitMsg[1].equals("i detected fire")){   //Este msj solo le llega al lider
			droneRef.InstructionsForFire();
		}

	}
}