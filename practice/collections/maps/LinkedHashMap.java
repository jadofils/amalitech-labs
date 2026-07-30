package collections.maps;

/** LinkedHashMap - a HashMap plus a linked list threading the entries, so iteration follows insertion (or access) order. */
public class LinkedHashMap {

    public static void main(String[] args) {
        preservesInsertionOrder();
        accessOrderEnablesLru();
    }

    private static void preservesInsertionOrder() {
        java.util.LinkedHashMap<String, Integer> ages = new java.util.LinkedHashMap<>();
        ages.put("Zoe", 19);
        ages.put("Alice", 25);
        ages.put("Mike", 31);
        System.out.println("insertion order: " + ages);
    }

    private static void accessOrderEnablesLru() {
        // accessOrder=true reorders on every get()/put(), moving the touched entry to the end -
        // combined with overriding removeEldestEntry(), this is the textbook way to build an LRU cache.
        java.util.LinkedHashMap<String, Integer> lru = new java.util.LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(java.util.Map.Entry<String, Integer> eldest) {
                return size() > 2; // cap the cache at 2 entries
            }
        };
        lru.put("a", 1);
        lru.put("b", 2);
        lru.get("a");     // touching "a" moves it to the most-recently-used end
        lru.put("c", 3);  // over capacity - evicts "b", the least recently used

        System.out.println("LRU cache (max 2, \"b\" evicted): " + lru);
    }
}
