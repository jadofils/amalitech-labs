package collections.maps;

/** The Map interface's core contract - unique keys, each mapped to one value. Backed here by HashMap. */
public class Map {

    public static void main(String[] args) {
        puttingAndGetting();
        checkingAndRemoving();
        viewingKeysValuesEntries();
        computeIfAbsentAndMerge();
    }

    private static void puttingAndGetting() {
        java.util.Map<String, Integer> ages = new java.util.HashMap<>();
        System.out.println("put(Alice, 25) [new]: " + ages.put("Alice", 25));   // null - no previous value
        System.out.println("put(Alice, 26) [overwrite]: " + ages.put("Alice", 26)); // 25 - the old value

        System.out.println("get(Alice): " + ages.get("Alice"));
        System.out.println("get(Dave) [missing]: " + ages.get("Dave"));                 // null - ambiguous if 0/false is valid
        System.out.println("getOrDefault(Dave, -1): " + ages.getOrDefault("Dave", -1)); // -1 - a real fallback instead
    }

    private static void checkingAndRemoving() {
        java.util.Map<String, Integer> ages = new java.util.HashMap<>(java.util.Map.of("Alice", 25, "Bob", 31));
        System.out.println("containsKey(Alice): " + ages.containsKey("Alice"));
        System.out.println("containsValue(31): " + ages.containsValue(31));
        System.out.println("remove(Alice): " + ages.remove("Alice"));
        System.out.println("after remove: " + ages);
    }

    private static void viewingKeysValuesEntries() {
        java.util.Map<String, Integer> ages = java.util.Map.of("Alice", 25, "Bob", 31);

        System.out.println("keySet(): " + ages.keySet());
        System.out.println("values(): " + ages.values());
        for (java.util.Map.Entry<String, Integer> entry : ages.entrySet()) {
            System.out.println("entry: " + entry.getKey() + " -> " + entry.getValue());
        }
        ages.forEach((name, age) -> System.out.println("forEach: " + name + " is " + age));
    }

    private static void computeIfAbsentAndMerge() {
        // computeIfAbsent: only computes/inserts a value when the key is missing - handy for lazy init (e.g. grouping).
        java.util.Map<String, java.util.List<String>> byFirstLetter = new java.util.HashMap<>();
        for (String name : java.util.List.of("Alice", "Amy", "Bob")) {
            byFirstLetter.computeIfAbsent(name.substring(0, 1), k -> new java.util.ArrayList<>()).add(name);
        }
        System.out.println("computeIfAbsent grouping: " + byFirstLetter);

        // merge: combines a new value with any existing one via a function - a clean way to build a word count.
        java.util.Map<String, Integer> wordCounts = new java.util.HashMap<>();
        for (String word : java.util.List.of("a", "b", "a", "a", "b")) {
            wordCounts.merge(word, 1, Integer::sum);
        }
        System.out.println("merge word count: " + wordCounts);
    }
}
