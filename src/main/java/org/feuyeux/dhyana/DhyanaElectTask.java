package org.feuyeux.dhyana;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.feuyeux.dhyana.DhyanaEngine.ElectMasterCallback;
import org.feuyeux.dhyana.config.DhyanaConfig;
import org.feuyeux.dhyana.domain.DhyanaCandidate;
import org.feuyeux.dhyana.domain.DhyanaNode;
import org.feuyeux.dhyana.transport.ConfigTransport;
import org.feuyeux.dhyana.transport.DhyanaTransport;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.feuyeux.dhyana.domain.DhyanaConstants;

/**
 * @author feuyeux@gmail.com
 * @date 2019/01/10
 */
@Slf4j
class DhyanaElectTask implements Runnable {
    private final ElectMasterCallback callback;
    private final ConfigTransport configTransport;
    private final DhyanaTransport dhyanaTransport;
    private final DhyanaNode currentNode;
    private final DhyanaConfig dhyanaConfig;
    @Setter
    private boolean isRunning = true;

    DhyanaElectTask(DhyanaNode currentNode, ConfigTransport configTransport, DhyanaTransport dhyanaTransport, DhyanaConfig dhyanaConfig,
                    ElectMasterCallback electMasterCallback) {
        this.currentNode = currentNode;
        this.configTransport = configTransport;
        this.dhyanaTransport = dhyanaTransport;
        this.dhyanaConfig = dhyanaConfig;
        this.callback = electMasterCallback;
    }

    @Override
    public void run() {
        try {
            doRun(1);
        } catch (Exception e) {
            callback.onFailure(new Exception("fail to elect master", e));
        }
    }

    private void doRun(int num) {
        if (!isRunning || Thread.currentThread().isInterrupted()) {
            log.error("Thread is interrupted.");
            return;
        }
        log.debug("====== Electing leader[{}] ======", num);
        try {
            int quorum = dhyanaConfig.getMinimumNodes();
            List<DhyanaCandidate> dhyanaCandidates = null;
            for (int i = DhyanaConstants.WAIT_CANDIDATE_CACHE_MAX_RETRY; i > 0; i--) {
                long candidatesSize = configTransport.getCandidatesSize();
                int j = DhyanaConstants.WAIT_CANDIDATE_CACHE_MAX_RETRY - i;
                log.debug("Electing leader[{}] [{}] candidatesSize = {}", num, j, candidatesSize);
                if (candidatesSize < quorum) {
                    TimeUnit.MILLISECONDS.sleep(DhyanaConstants.WAIT_CANDIDATE_INTERVAL_MILLIS * j);
                } else {
                    dhyanaCandidates = configTransport.readCandidates();
                    break;
                }
            }

            int agreeCount = 0;
            String masterCandidate = null;
            if (dhyanaCandidates != null) {
                Optional<String> first = dhyanaCandidates.parallelStream().map(DhyanaCandidate::getId).sorted().findFirst();
                if (first.isPresent()) {
                    masterCandidate = first.get();
                    currentNode.setMaster(masterCandidate);
                    log.debug("Master decision is: {}", masterCandidate);
                    for (DhyanaCandidate c : dhyanaCandidates) {
                        if (!c.getIp().equals(currentNode.getIp()) || c.getPort() != currentNode.getPort()) {
                            boolean r = false;
                            try {
                                String answer = dhyanaTransport.ask(c, masterCandidate);
                                r = DhyanaConstants.MASTER_AGREE.equals(answer);
                                log.debug("[{}:{}] answer {}", c.getIp(), c.getPort(), answer);
                            } catch (Exception e) {
                                log.error("", e);
                            }
                            if (r) {
                                agreeCount++;
                                if (agreeCount >= quorum - 1) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            boolean success = agreeCount >= quorum - 1;
            if (success) {
                callback.onSuccess(masterCandidate);
            } else {
                masterCandidate = DhyanaCache.getElect(quorum);
                log.debug("Watch others' elected, masterCandidate={}", masterCandidate);
                if (masterCandidate != null && !DhyanaConstants.MASTER_NOT_AVAILABLE.equals(masterCandidate)) {
                    callback.onSuccess(masterCandidate);
                } else if (num < DhyanaConstants.ELECT_MAX_RETRY) {
                    TimeUnit.SECONDS.sleep(DhyanaConstants.ELECT_INTERVAL_SECONDS);
                    doRun(++num);
                }
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }
}
