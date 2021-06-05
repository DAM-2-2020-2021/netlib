package eu.cifpfbmoll.netlib.internal;

import eu.cifpfbmoll.netlib.annotation.PacketType;

@PacketType(ACKPacket.type)
public class ACKPacket {
    public static final String type = "ACK";
}
