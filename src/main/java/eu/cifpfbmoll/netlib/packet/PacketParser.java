package eu.cifpfbmoll.netlib.packet;

import eu.cifpfbmoll.netlib.annotation.PacketAttribute;
import eu.cifpfbmoll.netlib.annotation.PacketType;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Parse {@link eu.cifpfbmoll.netlib.annotation.PacketType} classes and serialize/deserialize their PacketAttributes.
 */
public class PacketParser {
    private static final int SIZE = 1;
    private static final int BYTE_SIZE = 1;
    private static final int SHORT_SIZE = 2;
    private static final int INT_SIZE = 4;
    private static final int LONG_SIZE = 8;
    private static final int FLOAT_SIZE = 4;
    private static final int DOUBLE_SIZE = 8;
    private static final int CHAR_SIZE = 2;
    private static final int STRING_SIZE = 1;

    /**
     * Dynamically get object fields' size.
     */
    @FunctionalInterface
    private interface SizeHandler {
        int handle(Object object, Field field) throws IllegalAccessException;
    }

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
        private final SizeHandler size;
        private final TypeHandler serializer;
        private final TypeHandler deserializer;

        public TypeInfo(SizeHandler size, TypeHandler serializer, TypeHandler deserializer) {
            this.size = size;
            this.serializer = serializer;
            this.deserializer = deserializer;
        }

        /**
         * Get an Object's field size.
         *
         * @param object object to get size from
         * @param field object's field to calculate size
         * @return field's size
         */
        public int size(Object object, Field field) throws IllegalAccessException {
            return this.size.handle(object, field);
        }

        /**
         * Serialize an Object's field with its serializer function.
         *
         * @param object object to serialize
         * @param field field to serialize
         * @param bb ByteBuffer used to store serialized data
         */
        public void serialize(Object object, Field field, ByteBuffer bb) throws IllegalAccessException {
            if (this.serializer != null)
                this.serializer.handle(object, field, bb);
        }

        /**
         * Deserialize an Object's field with its deserializer function.
         *
         * @param object object to deserialize
         * @param field field to deserialize
         * @param bb ByteBuffer used where the serialized data is stored
         */
        public void deserialize(Object object, Field field, ByteBuffer bb) throws IllegalAccessException {
            if (this.deserializer != null)
                this.deserializer.handle(object, field, bb);
        }
    }

    private static final PacketParser instance = new PacketParser();
    private final Map<Class<?>, TypeInfo> types = new HashMap<>();

    /**
     * Get PacketParser's instance.
     *
     * @return PacketParser's instance.
     */
    public static PacketParser getInstance() {
        return instance;
    }

    public PacketParser() {
        this.types.put(Byte.TYPE, new TypeInfo(
                (object, field) -> BYTE_SIZE,
                (object, field, bb) -> bb.put(field.getByte(object)),
                (object, field, bb) -> field.setByte(object, bb.get())));
        this.types.put(Byte.class, new TypeInfo(
                (object, field) -> BYTE_SIZE,
                (object, field, bb) -> bb.put((byte) field.get(object)),
                (object, field, bb) -> field.set(object, bb.get())));
        this.types.put(byte[].class, new TypeInfo(
                (object, field) -> {
                    byte[] arr = (byte[]) field.get(object);
                    return arr.length * BYTE_SIZE + SIZE;
                },
                (object, field, bb) -> {
                    byte[] arr = (byte[]) field.get(object);
                    bb.put((byte) arr.length);
                    for (byte b : arr) bb.put(b);
                },
                (object, field, bb) -> {
                    int size = bb.get() & 0xff;
                    byte[] arr = new byte[size];
                    for (int i = 0; i < size; i++)
                        arr[i] = bb.get();
                    field.set(object, arr);
                }));

        this.types.put(Short.TYPE, new TypeInfo(
                (object, field) -> SHORT_SIZE,
                (object, field, bb) -> bb.putShort(field.getShort(object)),
                (object, field, bb) -> field.set(object, bb.getShort())));
        this.types.put(Short.class, new TypeInfo(
                (object, field) -> SHORT_SIZE,
                (object, field, bb) -> bb.putShort((short) field.get(object)),
                (object, field, bb) -> field.set(object, bb.getShort())));
        this.types.put(short[].class, new TypeInfo(
                (object, field) -> {
                    short[] arr = (short[]) field.get(object);
                    return arr.length * SHORT_SIZE + SIZE;
                },
                (object, field, bb) -> {
                    short[] arr = (short[]) field.get(object);
                    bb.put((byte) arr.length);
                    for (short b : arr) bb.putShort(b);
                },
                (object, field, bb) -> {
                    int size = bb.get() & 0xff;
                    short[] arr = new short[size];
                    for (int i = 0; i < size; i++)
                        arr[i] = bb.getShort();
                    field.set(object, arr);
                }));

        this.types.put(Integer.TYPE, new TypeInfo(
                (object, field) -> INT_SIZE,
                (object, field, bb) -> bb.putInt(field.getInt(object)),
                (object, field, bb) -> field.set(object, bb.getInt())));
        this.types.put(Integer.class, new TypeInfo(
                (object, field) -> INT_SIZE,
                (object, field, bb) -> bb.putInt((int) field.get(object)),
                (object, field, bb) -> field.set(object, bb.getInt())));
        this.types.put(int[].class, new TypeInfo(
                (object, field) -> {
                    int[] arr = (int[]) field.get(object);
                    return arr.length * INT_SIZE + SIZE;
                },
                (object, field, bb) -> {
                    int[] arr = (int[]) field.get(object);
                    bb.put((byte) arr.length);
                    for (int b : arr) bb.putInt(b);
                },
                (object, field, bb) -> {
                    int size = bb.get() & 0xff;
                    int[] arr = new int[size];
                    for (int i = 0; i < size; i++)
                        arr[i] = bb.getInt();
                    field.set(object, arr);
                }));

        this.types.put(Long.TYPE, new TypeInfo(
                (object, field) -> LONG_SIZE,
                (object, field, bb) -> bb.putLong(field.getLong(object)),
                (object, field, bb) -> field.set(object, bb.getLong())));
        this.types.put(Long.class, new TypeInfo(
                (object, field) -> LONG_SIZE,
                (object, field, bb) -> bb.putLong((long) field.get(object)),
                (object, field, bb) -> field.set(object, bb.getLong())));
        this.types.put(long[].class, new TypeInfo(
                (object, field) -> {
                    long[] arr = (long[]) field.get(object);
                    return arr.length * LONG_SIZE + SIZE;
                },
                (object, field, bb) -> {
                    long[] arr = (long[]) field.get(object);
                    bb.put((byte) arr.length);
                    for (long b : arr) bb.putLong(b);
                },
                (object, field, bb) -> {
                    int size = bb.get() & 0xff;
                    long[] arr = new long[size];
                    for (int i = 0; i < size; i++)
                        arr[i] = bb.getLong();
                    field.set(object, arr);
                }));

        this.types.put(Float.TYPE, new TypeInfo(
                (object, field) -> FLOAT_SIZE,
                (object, field, bb) -> bb.putFloat(field.getFloat(object)),
                (object, field, bb) -> field.set(object, bb.getFloat())));
        this.types.put(Float.class, new TypeInfo(
                (object, field) -> FLOAT_SIZE,
                (object, field, bb) -> bb.putFloat((float) field.get(object)),
                (object, field, bb) -> field.set(object, bb.getFloat())));
        this.types.put(float[].class, new TypeInfo(
                (object, field) -> {
                    float[] arr = (float[]) field.get(object);
                    return arr.length * FLOAT_SIZE + SIZE;
                },
                (object, field, bb) -> {
                    float[] arr = (float[]) field.get(object);
                    bb.put((byte) arr.length);
                    for (float b : arr) bb.putFloat(b);
                },
                (object, field, bb) -> {
                    int size = bb.get() & 0xff;
                    float[] arr = new float[size];
                    for (int i = 0; i < size; i++)
                        arr[i] = bb.getFloat();
                    field.set(object, arr);
                }));

        this.types.put(Double.TYPE, new TypeInfo(
                (object, field) -> DOUBLE_SIZE,
                (object, field, bb) -> bb.putDouble(field.getDouble(object)),
                (object, field, bb) -> field.set(object, bb.getDouble())));
        this.types.put(Double.class, new TypeInfo(
                (object, field) -> DOUBLE_SIZE,
                (object, field, bb) -> bb.putDouble((double) field.get(object)),
                (object, field, bb) -> field.set(object, bb.getDouble())));
        this.types.put(double[].class, new TypeInfo(
                (object, field) -> {
                    double[] arr = (double[]) field.get(object);
                    return arr.length * DOUBLE_SIZE + SIZE;
                },
                (object, field, bb) -> {
                    double[] arr = (double[]) field.get(object);
                    bb.put((byte) arr.length);
                    for (double b : arr) bb.putDouble(b);
                },
                (object, field, bb) -> {
                    int size = bb.get() & 0xff;
                    double[] arr = new double[size];
                    for (int i = 0; i < size; i++)
                        arr[i] = bb.getDouble();
                    field.set(object, arr);
                }));

        this.types.put(Character.TYPE, new TypeInfo(
                (object, field) -> CHAR_SIZE,
                (object, field, bb) -> bb.putChar(field.getChar(object)),
                (object, field, bb) -> field.set(object, bb.getChar())));
        this.types.put(Character.class, new TypeInfo(
                (object, field) -> CHAR_SIZE,
                (object, field, bb) -> bb.putChar((char) field.get(object)),
                (object, field, bb) -> field.set(object, bb.getChar())));
        this.types.put(char[].class, new TypeInfo(
                (object, field) -> {
                    char[] arr = (char[]) field.get(object);
                    return arr.length * CHAR_SIZE + SIZE;
                },
                (object, field, bb) -> {
                    char[] arr = (char[]) field.get(object);
                    bb.put((byte) arr.length);
                    for (char b : arr) bb.putChar(b);
                },
                (object, field, bb) -> {
                    int size = bb.get() & 0xff;
                    char[] arr = new char[size];
                    for (int i = 0; i < size; i++)
                        arr[i] = bb.getChar();
                    field.set(object, arr);
                }));

        this.types.put(String.class, new TypeInfo(
                (object, field) -> {
                    String str = (String) field.get(object);
                    return str.length() * STRING_SIZE + SIZE;
                },
                (object, field, bb) -> {
                    String str = (String) field.get(object);
                    byte[] bytes = str.getBytes(Packet.CHARSET_ENCODING);
                    bb.put((byte) bytes.length);
                    for (byte b : bytes)
                        bb.put(b);
                },
                (object, field, bb) -> {
                    int size = bb.get() & 0xff;
                    byte[] bytes = new byte[size];
                    for (int i = 0; i < size; i++)
                        bytes[i] = bb.get();
                    field.set(object, new String(bytes, Packet.CHARSET_ENCODING));
                }));
    }

    /**
     * Get TypeInfo for a specific type.
     *
     * @param type type to check for TypeInfo
     * @return TypeInfo that is assignable from the specified type, null if no TypeInfo was found
     * @throws IllegalArgumentException if specified type is not supported
     */
    private TypeInfo getTypeInfo(Class<?> type) {
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
                TypeInfo typeInfo = getTypeInfo(field.getType());
                size += typeInfo.size.handle(object, field);
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
