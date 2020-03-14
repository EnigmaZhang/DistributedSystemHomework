import java.io.*;
import java.net.*;

/**
 * @author <a href = "https://github.com/EnigmaZhang/"> Zhang Xiaotian </a>
 * The format is "6+7\n".
 */
@SuppressWarnings("InfiniteLoopStatement")
class UDPClient
{
    public static void main(String[] args) throws Exception
    {
        System.out.println("Please input the calculation, The format is \"6+7\\n.");
        BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));
        DatagramSocket socket = new DatagramSocket();
        InetAddress ip = InetAddress.getByName("localhost");
        int port = 9000;
        byte[] send;
        while (true)
        {
            byte[] receive = new byte[1024];
            String calculation = userIn.readLine();
            send = calculation.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(send, send.length, ip, port);
            socket.send(sendPacket);

            DatagramPacket receivePacket = new DatagramPacket(receive, receive.length);
            socket.receive(receivePacket);
            String result = new String(receivePacket.getData());
            System.out.println("Answer: " + result);
        }
    }
}