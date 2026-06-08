// Day3 Q4: StreamFlix LRU Memory Cache
// ---------------------------------------
// LRU stands for Least Recently Used. The idea: when the cache is full and
// a new item needs to come in, you kick out the item that hasn't been accessed
// in the longest time. The reasoning is "if you haven't used it recently,
// you probably won't need it soon."
//
// Implementing LRU from scratch requires a HashMap (O(1) lookup) combined with
// a Doubly-Linked List (O(1) eviction from any position). That's the classic
// LeetCode hard problem. But Java's LinkedHashMap already has BOTH baked in,
// plus a hook specifically designed for eviction logic. We just extend it.

import java.util.LinkedHashMap;
import java.util.Map;


// We extend LinkedHashMap<K, V> rather than composing it.
// Extending is appropriate here because we ARE an LRU cache — it's what we are,
// not just something we contain. And we need to override removeEldestEntry(),
// which is a protected method only accessible via inheritance.
//
// K is the cache key (e.g., movie ID string)
// V is the cached value (e.g., metadata object)
class VideoCache<K, V> extends LinkedHashMap<K, V> {

    private final int capacity;

    // The constructor is the most important line in this whole class.
    // Three parameters we're passing to super():
    //
    //   initialCapacity (capacity + 1):
    //     Pre-allocate slightly more than we'll ever hold. This avoids any
    //     internal resizing that could disrupt the ordering.
    //
    //   loadFactor (0.75f):
    //     Standard default. Controls when the internal hash table resizes.
    //     We don't want resizing to happen, so we size it generously.
    //
    //   accessOrder (true):
    //     THIS is the critical one. By default, LinkedHashMap maintains
    //     INSERTION order — items stay in the order they were first added.
    //     Setting this to TRUE switches to ACCESS order — every time you
    //     call get() or put() on an existing key, that entry gets moved to
    //     the TAIL of the internal doubly-linked list.
    //     The HEAD always holds the least recently used item. Perfect for LRU.
    public VideoCache(int capacity) {
        super(capacity + 1, 0.75f, true); // true = access order, not insertion order
        this.capacity = capacity;
    }


    // LinkedHashMap calls this automatically after every put().
    // It's asking: "should I evict the eldest (least recently accessed) entry?"
    // We return true when we've exceeded capacity, which tells LinkedHashMap
    // to remove the entry at the HEAD of its internal list — the LRU item.
    //
    // This is the eviction policy, and it's entirely declarative.
    // We're not manually tracking what to remove — we're just telling the
    // map "yes, evict when full." LinkedHashMap handles the rest.
    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        if (size() > capacity) {
            System.out.println("  [EVICT] Cache full — evicting LRU entry: \""
                    + eldest.getKey() + "\"");
            return true; // yes, remove it
        }
        return false; // no, we're still within capacity
    }


    // Wraps the normal get() with a log so we can see access order in action.
    // Under the hood, LinkedHashMap moves this entry to the tail on access
    // because we configured accessOrder = true.
    public V getWithLog(K key) {
        V value = get(key);
        if (value != null) {
            System.out.println("  [CACHE HIT] \"" + key + "\" accessed → moved to front of keep list");
        } else {
            System.out.println("  [CACHE MISS] \"" + key + "\" not in cache");
        }
        return value;
    }


    // Shows the current cache contents from LRU (head) to MRU (tail).
    // This order is maintained automatically by LinkedHashMap's internal list.
    public void printCache() {
        System.out.println("  Cache state (LRU → MRU): " + keySet());
    }
}


public class StreamFlixCache {
    public static void main(String[] args) {

        // Cache that holds exactly 5 movies
        VideoCache<String, String> cache = new VideoCache<>(5);

        System.out.println("===== STREAMFLIX VIDEO CACHE (capacity: 5) =====\n");

        // Fill the cache to capacity
        System.out.println("--- Loading 5 movies into cache ---");
        cache.put("Inception",        "Sci-fi thriller by Nolan");
        cache.put("The Matrix",        "Cyberpunk classic");
        cache.put("Interstellar",     "Space epic by Nolan");
        cache.put("The Dark Knight",  "Batman masterpiece");
        cache.put("Parasite",         "Korean social thriller");

        System.out.print("After loading: ");
        cache.printCache();
        // Order: Inception → Matrix → Interstellar → Dark Knight → Parasite
        //        (LRU on left, MRU on right — insertion order so far)

        // Now access "Inception" — the item that was added FIRST (LRU candidate).
        // Because accessOrder = true, this moves Inception to the tail (MRU end).
        // Now "The Matrix" becomes the least recently used item instead.
        System.out.println("\n--- Accessing 'Inception' ---");
        cache.getWithLog("Inception");

        System.out.print("After accessing Inception: ");
        cache.printCache();
        // Order: Matrix → Interstellar → Dark Knight → Parasite → Inception
        //        (Matrix is now the LRU — it hasn't been touched)

        // Add a 6th movie — this triggers the eviction policy.
        // removeEldestEntry returns true, LinkedHashMap evicts the HEAD (The Matrix).
        System.out.println("\n--- Adding 6th movie: 'Oppenheimer' ---");
        cache.put("Oppenheimer", "Historical drama by Nolan");

        System.out.print("After adding Oppenheimer: ");
        cache.printCache();
        // Matrix should be gone. Inception (recently accessed) is still here.

        System.out.println("\n--- Verifying eviction ---");
        System.out.println("  'The Matrix' in cache? " + cache.containsKey("The Matrix"));   // false
        System.out.println("  'Inception' in cache?  " + cache.containsKey("Inception"));    // true — was accessed
        System.out.println("  Cache size: " + cache.size()); // should be 5

        System.out.println("\n--- Adding 2 more movies ---");
        cache.put("Tenet", "Time-bending thriller");
        cache.put("Dunkirk", "War epic");
        // Each add should evict the current LRU item

        System.out.print("Final cache state: ");
        cache.printCache();
    }
}
