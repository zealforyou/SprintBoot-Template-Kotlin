package com.zz.mini.ss.db;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CacheDb {
    public static final CacheDb db = new CacheDb();
    public List<String> userCaches = new ArrayList<>();

    public Map<String, List<Map<String, Object>>> candidates = new HashMap<>();
    public Map<String, String> offerCache = new HashMap<>();
    public Map<String, String> answerCache = new HashMap<>();

    public void addCandidate(String user, Map<String, Object> candidate) {
        List<Map<String, Object>> list = candidates.computeIfAbsent(user, k -> new ArrayList<>());
        list.add(candidate);
    }
}
