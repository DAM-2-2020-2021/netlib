package eu.cifpfbmoll.netlib.internal;

import eu.cifpfbmoll.netlib.annotation.PacketType;

@PacketType(HelloPacket.type)
public class HelloPacket {
    public static final String type = "HELO";
}
