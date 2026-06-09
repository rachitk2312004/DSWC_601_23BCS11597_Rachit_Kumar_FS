// Day4 Q7: Multi-Tiered Audit Log Merger
// -----------------------------------------
// New concept: Collectors.toUnmodifiableMap() with a merge function.
//
// The naive approach — .collect(Collectors.toMap(key, value)) — throws an
// IllegalStateException the moment it encounters a duplicate key. For login logs
// where one user can have multiple entries (multiple IP addresses), this crashes.
//
// The fix is a merge function: a third argument that tells the collector what to do
// when two values compete for the same key. Here, we merge IP sets rather than
// discarding or crashing.

import java.util.*;
import java.util.stream.Collectors;


// Represents one login event — a user logged in from a specific IP at some point.
// The same user will appear multiple times in the list if they logged in from
// different IPs or at different times.
class LoginLog {
    private String userId;
    private String ipAddress;
    private String timestamp;

    public LoginLog(String userId, String ipAddress, String timestamp) {
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.timestamp = timestamp;
    }

    public String getUserId()    { return userId; }
    public String getIpAddress() { return ipAddress; }
    public String getTimestamp() { return timestamp; }
}


// ---------- The Audit Processor ----------

class AuditProcessor {

    public Map<String, Set<String>> buildUserIpMap(List<LoginLog> logs) {
        return logs.stream()

            // Collectors.toUnmodifiableMap() builds a Map and then wraps it in
            // an unmodifiable view — nobody can call .put() or .remove() on it afterward.
            // This is the correct choice for a security audit result that shouldn't
            // be mutated after it's produced.
            //
            // Three arguments:
            //   1. Key extractor   — what becomes the map key (userId)
            //   2. Value extractor — what becomes the map value (a HashSet with the IP)
            //   3. Merge function  — what to do when the same key appears twice
            .collect(Collectors.toUnmodifiableMap(

                // Key: the user ID — this is what we're grouping by
                LoginLog::getUserId,

                // Value: we need a MUTABLE Set so the merge function can add to it.
                // We start with a HashSet containing just this log's IP.
                // HashSet guarantees uniqueness — if the same IP appears twice, it
                // won't be duplicated in the set.
                log -> {
                    Set<String> ipSet = new HashSet<>();
                    ipSet.add(log.getIpAddress());
                    return ipSet;
                },

                // Merge function: called when a userId key already exists in the map.
                // 'existingSet' is what's already stored for this userId.
                // 'newSet' is the set we just created for the new duplicate log entry.
                // We add all IPs from the new set into the existing set and return it.
                //
                // Without this argument: IllegalStateException on the second "U-101" log.
                // With it: the IPs are merged cleanly.
                (existingSet, newSet) -> {
                    existingSet.addAll(newSet); // merge new IPs into existing set
                    return existingSet;
                }
            ));
    }
}


// ---------- Entry Point ----------

public class AuditLogMerger {
    public static void main(String[] args) {

        // Simulate raw login logs — same users appearing multiple times from different IPs.
        // This is exactly what a real security microservice would receive from its log ingestion.
        List<LoginLog> logs = Arrays.asList(
            new LoginLog("U-101", "192.168.1.1",   "08:01"),  // U-101 first login
            new LoginLog("U-102", "10.0.0.5",      "08:02"),  // U-102 first login
            new LoginLog("U-101", "192.168.1.1",   "08:15"),  // U-101 same IP again — deduped in Set
            new LoginLog("U-103", "172.16.0.1",    "08:17"),  // U-103 first login
            new LoginLog("U-101", "203.0.113.50",  "09:00"),  // U-101 new IP — merge needed
            new LoginLog("U-102", "10.0.0.99",     "09:30"),  // U-102 new IP — merge needed
            new LoginLog("U-101", "10.10.10.10",   "10:00"),  // U-101 third unique IP
            new LoginLog("U-103", "172.16.0.1",    "10:05"),  // U-103 same IP — deduped
            new LoginLog("U-104", "198.51.100.14", "10:15"),  // U-104 single login
            new LoginLog("U-102", "10.0.0.5",      "11:00")   // U-102 back to original IP — deduped
        );

        AuditProcessor processor = new AuditProcessor();
        Map<String, Set<String>> ipReport = processor.buildUserIpMap(logs);

        System.out.println("===== SECURITY AUDIT: USER IP MAPPING =====\n");
        System.out.println("Total log entries: " + logs.size());
        System.out.println("Unique users:      " + ipReport.size());
        System.out.println("\n--- User → Unique IP Addresses ---");

        // Sort by user ID for consistent output
        new TreeMap<>(ipReport).forEach((userId, ips) -> {
            List<String> sortedIps = new ArrayList<>(ips);
            Collections.sort(sortedIps);
            System.out.println(userId + " → " + sortedIps);
        });

        System.out.println("\n--- Security Alert: Users with multiple IPs ---");
        ipReport.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry ->
                System.out.println("  " + entry.getKey() + " logged in from "
                        + entry.getValue().size() + " different IPs — review recommended")
            );
    }
}
