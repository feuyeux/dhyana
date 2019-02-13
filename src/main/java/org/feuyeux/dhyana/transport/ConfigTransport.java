package org.feuyeux.dhyana.transport;

import com.alibaba.fastjson.JSON;
import org.feuyeux.dhyana.config.DhyanaConfig;
import org.feuyeux.dhyana.domain.DhyanaCandidate;
import org.feuyeux.dhyana.domain.DhyanaNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.feuyeux.dhyana.domain.DhyanaConstants.DHYANA_CANDIDATE;
import static org.feuyeux.dhyana.domain.DhyanaConstants.DHYANA_NODES;

/**
 * @author feuyeux@gmail.com
 * @date 2019/01/09
 */
@Service
@Slf4j
public class ConfigTransport {
    private String dhyanaCandidateKey;
    private String dhyanaNodesKey;

    @Autowired
    private DhyanaConfig dhyanaConfig;

    @Autowired
    private RedisUtils redis;

    @PostConstruct
    private void init() {
        dhyanaCandidateKey = dhyanaConfig.getClusterName() + "_" + DHYANA_CANDIDATE;
        dhyanaNodesKey = dhyanaConfig.getClusterName() + "_" + DHYANA_NODES;
    }

    public long getCandidatesSize() {
        try {
            return redis.getSetSize(dhyanaCandidateKey);
        } catch (Exception e) {
            return -1;
        }
    }

    public List<DhyanaCandidate> readCandidates() {
        Set<String> candidates = redis.getFromSet(dhyanaCandidateKey);
        if (candidates == null || candidates.isEmpty()) {
            return new ArrayList<>();
        }
        return candidates.stream()
            .map(c -> JSON.parseObject(c, DhyanaCandidate.class))
            .collect(Collectors.toList());
    }

    public void cleanCandidates() {
        redis.deleteKey(dhyanaCandidateKey);
    }

    public void register(DhyanaCandidate candidate) {
        log.debug("register candidate[{}:{}]", candidate.getIp(), candidate.getPort());
        redis.addToSet(dhyanaCandidateKey, JSON.toJSONString(candidate));
    }

    public void unRegister(DhyanaCandidate candidate) {
        log.debug("unregister candidate[{}:{}]", candidate.getIp(), candidate.getPort());
        redis.deleteFromSet(dhyanaCandidateKey, JSON.toJSONString(candidate));
    }

    public List<DhyanaNode> readNodes() {
        try {
            Set<String> nodes = redis.getFromSet(dhyanaNodesKey);
            if (nodes == null || nodes.isEmpty()) {
                return new ArrayList<>();
            }
            return nodes.stream()
                .map(name -> JSON.parseObject(redis.readString(name), DhyanaNode.class))
                .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("", e);
            return new ArrayList<>();
        }
    }

    public void updateNode(DhyanaNode node) {
        log.debug("updateNode node {}[{}:{}]", node.getNodeName(), node.getIp(), node.getPort());
        String nodeName = node.getNodeName();
        if (!redis.getFromSet(dhyanaNodesKey).contains(nodeName)) {
            redis.addToSet(dhyanaNodesKey, nodeName);
        }
        redis.saveString(nodeName, JSON.toJSONString(node));
    }

    public void cleanNode(DhyanaNode node) {
        log.debug("cleanNode node {}[{}:{}]", node.getNodeName(), node.getIp(), node.getPort());
        String nodeName = node.getNodeName();
        redis.deleteFromSet(dhyanaNodesKey, nodeName);
        redis.deleteKey(nodeName);
    }

    public void cleanNodes() {
        Set<String> nodes = redis.getFromSet(dhyanaNodesKey);
        nodes.forEach(name -> redis.deleteKey(name));
        redis.deleteKey(dhyanaNodesKey);
    }
}
