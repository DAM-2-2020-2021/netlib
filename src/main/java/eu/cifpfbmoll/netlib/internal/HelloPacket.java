package eu.cifpfbmoll.netlib.internal;

import eu.cifpfbmoll.netlib.annotation.PacketAttribute;
import eu.cifpfbmoll.netlib.annotation.PacketType;

@PacketType("HELO")
public class HelloPacket {
    @PacketAttribute
    public String ip;
}
