package eu.cifpfbmoll.netlib.node;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DummyTask {

    public static void main(String[] args) throws UnknownHostException {
        String ip = InetAddress.getLocalHost().getHostAddress();
        String[] splitIp = ip.split("\\.");
        String subnet = String.format("%s.%s.%s", splitIp[0], splitIp[1], splitIp[2]);
        System.out.println(ip + " " + subnet);
    }
}
