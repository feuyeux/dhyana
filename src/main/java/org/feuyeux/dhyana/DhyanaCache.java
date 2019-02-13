package org.feuyeux.dhyana;

import org.feuyeux.dhyana.domain.DhyanaNode;
import com.google.common.collect.Sets;
import org.feuyeux.dhyana.domain.DhyanaConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author feuyeux@gmail.com
 * @date 2019/02/02
 */
public class DhyanaCache {
    private static final List<DhyanaNode> DEAD_MASTER_LIST = new ArrayList<>();
    private static final Map<DhyanaNode, Integer> DEAD_NODE_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> ELECT_MAP = new ConcurrentHashMap<>();

    public static void markDeadMaster(DhyanaNode dhyanaNode) {
        DEAD_MASTER_LIST.add(dhyanaNode);
    }

    public static void markDeadNode(DhyanaNode dhyanaNode) {
        final Integer count = getDeadNodeRetryTime(dhyanaNode);
        if (count == null) {
            DEAD_NODE_MAP.put(dhyanaNode, 1);
        } else {
            DEAD_NODE_MAP.put(dhyanaNode, count + 1);
        }
    }

    public static void cleanDeadNode(DhyanaNode dhyanaNode) {
        DEAD_NODE_MAP.remove(dhyanaNode);
    }

    public static Integer getDeadNodeRetryTime(DhyanaNode dhyanaNode) {
        return DEAD_NODE_MAP.get(dhyanaNode);
    }

    public static String getDeadMaster() {
        if (DEAD_MASTER_LIST.isEmpty()) {
            return null;
        }
        return DEAD_MASTER_LIST.get(0).getNodeName();
    }

    public static DhyanaNode cleanDeadMaster() {
        if (DEAD_MASTER_LIST.isEmpty()) {
            return null;
        }
        return DEAD_MASTER_LIST.remove(0);
    }

    public static void markElect(String payload, String host, int port) {
        String electItNode = host + ":" + port;
        for (Entry<String, Set<String>> e : ELECT_MAP.entrySet()) {
            e.getValue().remove(electItNode);
        }
        if (!DhyanaConstants.MASTER_NOT_AVAILABLE.equals(payload)) {
            Set<String> electItSet = ELECT_MAP.get(payload);
            if (electItSet == null) {
                electItSet = Sets.newConcurrentHashSet();
                ELECT_MAP.put(payload, electItSet);
            }
            electItSet.add(electItNode);
        }
    }

    public static String getElect(int quorum) {
        String elect = null;
        int max = 0;
        for (Entry<String, Set<String>> e : ELECT_MAP.entrySet()) {
            String deadMaster = getDeadMaster();
            if (deadMaster != null) {
                if (e.getKey().equals(deadMaster)) {
                    continue;
                }
            }
            int electCount = e.getValue().size();
            if (electCount >= quorum - 1) {
                if (max < electCount) {
                    max = electCount;
                    elect = e.getKey();
                }
            }
        }
        return elect;
    }
}
