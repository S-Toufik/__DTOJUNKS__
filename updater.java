import com.tangosol.net.CacheFactory;
import com.tangosol.net.NamedCache;
import com.tangosol.util.InvocableMap;
import com.tangosol.util.ValueUpdater;
import com.tangosol.util.extractor.UniversalUpdater;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Dynamic field updates using EntryProcessor + UniversalUpdater
 * NO POF
 */
public class CoherenceDynamicUpdateDemo {

    /* ============================
       Domain Model (Plain Java)
       ============================ */

    public static class Person implements Serializable {
        private String name;
        private Address address;
        private Stats stats;

        public Person(String name, Address address, Stats stats) {
            this.name = name;
            this.address = address;
            this.stats = stats;
        }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public Address getAddress() { return address; }
        public void setAddress(Address address) { this.address = address; }

        public Stats getStats() { return stats; }
        public void setStats(Stats stats) { this.stats = stats; }

        @Override
        public String toString() {
            return "Person{name='" + name + "', address=" + address + ", stats=" + stats + '}';
        }
    }

    public static class Address implements Serializable {
        private String city;

        public Address(String city) { this.city = city; }

        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }

        @Override
        public String toString() {
            return "Address{city='" + city + "'}";
        }
    }

    public static class Stats implements Serializable {
        private int score;

        public Stats(int score) { this.score = score; }

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }

        @Override
        public String toString() {
            return "Stats{score=" + score + '}';
        }
    }

    /* ============================
       EntryProcessor
       ============================ */

    public static class DynamicUpdateProcessor<K, V>
            implements InvocableMap.EntryProcessor<K, V, Void>, Serializable {

        private final Map<String, Object> updates;

        public DynamicUpdateProcessor(Map<String, Object> updates) {
            this.updates = updates;
        }

        @Override
        public Void process(InvocableMap.Entry<K, V> entry) {

            if (!entry.isPresent()) {
                return null;
            }

            V value = entry.getValue(); // FULL deserialization (expected)

            for (Map.Entry<String, Object> update : updates.entrySet()) {
                ValueUpdater updater =
                        new UniversalUpdater(update.getKey());

                updater.update(value, update.getValue());
            }

            entry.setValue(value);
            return null;
        }
    }

    /* ============================
       Test
       ============================ */

    public static void main(String[] args) {

        CacheFactory.ensureCluster();
        NamedCache<String, Person> cache =
                CacheFactory.getCache("people");

        cache.put("p1", new Person(
                "John",
                new Address("London"),
                new Stats(10)));

        cache.put("p2", new Person(
                "Jane",
                new Address("Berlin"),
                new Stats(20)));

        System.out.println("Before:");
        cache.forEach((k, v) -> System.out.println(k + " -> " + v));

        Map<String, Object> updates = Map.of(
                "name", "Updated",
                "address.city", "Paris",
                "stats.score", 99
        );

        Set<String> keys = Set.of("p1", "p2");

        cache.invokeAll(keys, new DynamicUpdateProcessor<>(updates));

        System.out.println("\nAfter:");
        cache.forEach((k, v) -> System.out.println(k + " -> " + v));

        CacheFactory.shutdown();
    }
}
