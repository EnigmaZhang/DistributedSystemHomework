import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 @author <a href = "https://github.com/EnigmaZhang/"> Zhang Xiaotian </a>
 */
public class MyRMIServer
{
    public static void main(String[] args)
    {
        String serverIP = "127.0.0.1";
        System.setProperty("java.rmi.server.hostname", serverIP);
        try
        {
            String name = "BookShelf";
            BookShelf engine = new BookShelfImpl();
            BookShelf skeleton = (BookShelf) UnicastRemoteObject.exportObject(engine, 10990);

            Registry registry = LocateRegistry.getRegistry(1099);
            System.out.println("Registering Shelf Object");
            registry.rebind(name, skeleton);
        }
        catch (Exception e)
        {
            System.err.println("Exception:" + e);
            e.printStackTrace();
        }
    }
}
