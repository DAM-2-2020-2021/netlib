package eu.cifpfbmoll.netlib.node;

import java.net.UnknownHostException;

public class DummyTask {

    public DummyTask() {
        new NodeManager("192.168.1.104");
    }

    public static void main(String[] args) throws UnknownHostException {
        DummyTask dummyTask = new DummyTask();
    }
}
