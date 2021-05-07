package eu.cifpfbmoll.netlib.packet;

import eu.cifpfbmoll.netlib.annotation.PacketAttribute;
import eu.cifpfbmoll.netlib.annotation.PacketType;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parse {@link eu.cifpfbmoll.netlib.annotation.PacketType} classes and serialize/deserialize their PacketAttributes.
 */
public class PacketParser {
    /**
     * Serialize/Deserialize object's fields to a byte array using a ByteBuffer.
     */
    @FunctionalInterface
    private interface TypeHandler {
        void handle(Object object, Field field, ByteBuffer bb) throws IllegalAccessException;
    }

    /**
     * Store PacketParser's supported types' info.
     *
     * <p>This class is used to specify how to serialize and deserialize
     * a specific Object type using TypeHandler.</p>
     */
    private static final class TypeInfo {
        public final int size;
        public final TypeHandler serializer;
        public final TypeHandler deserializer;

        public TypeInfo(int size, TypeHandler serializer, TypeHandler deserializer) {
            this.size = size;
            this.serializer = serializer;
            this.deserializer = deserializer;
        }
    }

    private final Map<Class<?>, TypeInfo> types = new HashMap<>();
    private static final PacketParser instance = new PacketParser();

    /**
     * Get PacketParser's instance.
     *
     * @return PacketParser's instance.
     */
    public static PacketParser getInstance() {
        return instance;
    }

    public PacketParser() {
        this.types.put(Byte.TYPE, new TypeInfo(1,
                (object, field, bb) -> bb.put(field.getByte(object)),
                (object, field, bb) -> field.setByte(object, bb.get())));
        this.types.put(Byte.class, new TypeInfo(1,
                (object, field, bb) -> bb.put((byte) field.get(object)),
                (object, field, bb) -> field.set(object, bb.get())));
        this.types.put(Short.TYPE, new TypeInfo(2,
                (object, field, bb) -> bb.putShort(field.getShort(object)),
                (object, field, bb) -> field.set(object, bb.getShort())));
        this.types.put(Short.class, new TypeInfo(2,
                (object, field, bb) -> bb.putShort((short) field.get(object)),
                (object, field, bb) -> field.set(object, bb.getShort())));
        this.types.put(Integer.TYPE, new TypeInfo(4,
                (object, field, bb) -> bb.putInt(field.getInt(object)),
                (object, field, bb) -> field.set(object, bb.getInt())));
        this.types.put(Integer.class, new TypeInfo(4,
                (object, field, bb) -> bb.putInt((int) field.get(object)),
                (object, field, bb) -> field.set(object, bb.getInt())));
        this.types.put(Long.TYPE, new TypeInfo(8,
                (object, field, bb) -> bb.putLong(field.getLong(object)),
                (object, field, bb) -> field.set(object, bb.getLong())));
        this.types.put(Long.class, new TypeInfo(8,
                (object, field, bb) -> bb.putLong((long) field.get(object)),
                (object, field, bb) -> field.set(object, bb.getLong())));
        this.types.put(Float.TYPE, new TypeInfo(4,
                (object, field, bb) -> bb.putFloat(field.getFloat(object)),
                (object, field, bb) -> field.set(object, bb.getFloat())));
        this.types.put(Float.class, new TypeInfo(4,
                (object, field, bb) -> bb.putFloat((float) field.get(object)),
                (object, field, bb) -> field.set(object, bb.getFloat())));
        this.types.put(Double.TYPE, new TypeInfo(8,
                (object, field, bb) -> bb.putDouble(field.getDouble(object)),
                (object, field, bb) -> field.set(object, bb.getDouble())));
        this.types.put(Double.class, new TypeInfo(8,
                (object, field, bb) -> bb.putDouble((double) field.get(object)),
                (object, field, bb) -> field.set(object, bb.getDouble())));
        this.types.put(Character.TYPE, new TypeInfo(2,
                (object, field, bb) -> bb.putChar(field.getChar(object)),
                (object, field, bb) -> field.set(object, bb.getChar())));
        this.types.put(Character.class, new TypeInfo(2,
                (object, field, bb) -> bb.putChar((char) field.get(object)),
                (object, field, bb) -> field.set(object, bb.getChar())));
    }

    /**
     * Get TypeInfo for a specific type.
     *
     * @param type type to check for TypeInfo
     * @return TypeInfo that is assignable from the specified type, null if no TypeInfo was found
     * @throws IllegalArgumentException if specified type is not supported
     */
    public TypeInfo getTypeInfo(Class<?> type) {
        for (Class<?> key : types.keySet())
            if (key.isAssignableFrom(type)) return types.get(key);
        throw new IllegalArgumentException(String.format("'%s' type is not supported as a PacketAttribute.", type.getSimpleName()));
    }

    /**
     * Check for a supported type.
     *
     * @param type type to check
     * @return true if type is supported, false otherwise.
     */
    public boolean checkType(Class<?> type) {
        try {
            return getTypeInfo(type) != null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Get Class' packet type.
     *
     * @param clazz class to get packet type from
     * @return class' packet type or null if no PacketType annotation was found.
     */
    public String getPacketType(Class<?> clazz) {
        PacketType packetType = clazz.getAnnotation(PacketType.class);
        if (packetType == null) return null;
        return packetType.value();
    }

    /**
     * Get Object's packet type.
     *
     * @param object object to get packet type from
     * @return class' packet type or null if no PacketType annotation was found.
     */
    public String getPacketType(Object object) {
        return getPacketType(object.getClass());
    }

    /**
     * Serialize Object's annotated PacketAttribute fields to byte array.
     *
     * @param object object to serialize
     * @return serialized byte array
     * @throws IllegalAccessException if setting a field fails
     */
    public byte[] serialize(Object object) throws IllegalAccessException {
        if (object == null) return null;
        int size = 0;
        Class<?> clazz = object.getClass();
        List<Field> fields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(PacketAttribute.class)) {
                size += getTypeInfo(field.getType()).size;
                fields.add(field);
            }
        }
        ByteBuffer bb = ByteBuffer.allocate(size);
        for (Field field : fields)
            getTypeInfo(field.getType()).serializer.handle(object, field, bb);
        return bb.array();
    }

    /**
     * Deserialize Object's annotated PacketAttribute fields from byte array.
     *
     * @param object Object to deserialize
     * @param data   Object's serialized data
     * @throws IllegalAccessException if setting a field fails
     */
    public void deserialize(Object object, byte[] data) throws IllegalAccessException {
        if (object == null || data == null) return;
        Class<?> clazz = object.getClass();
        ByteBuffer bb = ByteBuffer.wrap(data);
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            if (field.isAnnotationPresent(PacketAttribute.class))
                getTypeInfo(field.getType()).deserializer.handle(object, field, bb);
        }
    }
}
