package collections.maps;

/** HashMap - backed by a hash table keyed on the key's hashCode(). O(1) average put/get/remove; iteration order is unspecified. */
public class HashMap {

    private record Point(int x, int y) {
    } // a record's generated equals()/hashCode() makes it safe to use as a map key

    public static void main(String[] args) {
        orderIsNotGuaranteed();
        keysNeedEqualsAndHashCode();
        nullKeyAndValueAreAllowed();
    }

    private static void orderIsNotGuaranteed() {
        java.util.HashMap<String, Integer> ages = new java.util.HashMap<>();
        ages.put("Zoe", 19);
        ages.put("Alice", 25);
        ages.put("Mike", 31);
        // Insertion order (Zoe, Alice, Mike) is NOT preserved here.
        System.out.println("iteration order (unspecified): " + ages);
    }

    private static void keysNeedEqualsAndHashCode() {
        java.util.HashMap<Point, String> labels = new java.util.HashMap<>();
        labels.put(new Point(1, 2), "origin-ish");
        // A DIFFERENT Point instance, but equals()/hashCode() say it's the same key - this overwrites, not adds.
        String previous = labels.put(new Point(1, 2), "still origin-ish");
        System.out.println("size (same key by equals()): " + labels.size() + ", overwrote: " + previous);
    }

    private static void nullKeyAndValueAreAllowed() {
        java.util.HashMap<String, String> map = new java.util.HashMap<>();
        map.put(null, "value for null key");
        map.put("key for null value", null);
        System.out.println("get(null): " + map.get(null));
        System.out.println("get(\"key for null value\"): " + map.get("key for null value"));
    }
}
