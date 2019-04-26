import java.rmi.Remote;
import java.rmi.RemoteException;
import java.net.UnknownHostException;

public interface RemoteElementInterface extends Remote{
	public void pushMessage(String msg) throws UnknownHostException,RemoteException;
}