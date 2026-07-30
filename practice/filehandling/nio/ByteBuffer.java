package filehandling.nio;

/**
 * java.nio.ByteBuffer - a fixed-capacity byte array with a position/limit, used to read/write via
 * FileChannel (see FileChannel.java). flip() is the classic gotcha: after put()-ing data in, you
 * must flip() before reading it back out, or a read sees nothing (position is still at the end).
 */
public class ByteBuffer {

    public static void main(String[] args) {
        java.nio.ByteBuffer buffer = java.nio.ByteBuffer.allocate(8);
        System.out.println("fresh buffer - position: " + buffer.position() + ", limit: " + buffer.limit() + ", capacity: " + buffer.capacity());

        buffer.put((byte) 1).put((byte) 2).put((byte) 3);
        System.out.println("after 3 put()s - position: " + buffer.position() + " (limit unchanged: " + buffer.limit() + ")");

        buffer.flip(); // switches from "writing mode" to "reading mode": limit = old position, position resets to 0
        System.out.println("after flip() - position: " + buffer.position() + ", limit: " + buffer.limit());

        while (buffer.hasRemaining()) {
            System.out.println("get(): " + buffer.get());
        }

        buffer.clear(); // resets position to 0 and limit to capacity, ready to put() again - does NOT erase the old bytes
        System.out.println("after clear() - position: " + buffer.position() + ", limit: " + buffer.limit());
    }
}
