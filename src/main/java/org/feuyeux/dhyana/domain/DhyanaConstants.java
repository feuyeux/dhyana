package org.feuyeux.dhyana.domain;

/**
 * @author feuyeux@gmail.com
 * @date 2019/02/10
 */
public interface DhyanaConstants {
    String MASTER_NOT_AVAILABLE = "N/A";
    String MASTER_AGREE = "T";
    String MASTER_DISAGREE = "F";
    String HEALTH = "H";
    int DEAD_NODE_MAX_RETRY = 10;
    int KEEP_ALIVE_MAX_RETRY = 3;
    int KEEP_ALIVE_INTERVAL_SECONDS = 3;
    int ELECT_MAX_RETRY = 10;
    int ELECT_INTERVAL_SECONDS = 5;
    int WAIT_CANDIDATE_CACHE_MAX_RETRY = 10;
    int WAIT_CANDIDATE_INTERVAL_MILLIS = 500;
    String DHYANA_CANDIDATE = "DHYANA_CANDIDATE";
    String DHYANA_NODES = "DHYANA_NODES";
}
