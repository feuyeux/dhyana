package org.feuyeux.dhyana;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.feuyeux.dhyana.config.DhyanaConfig;
import org.feuyeux.dhyana.domain.DhyanaCandidate;
import org.feuyeux.dhyana.domain.DhyanaConstants;
import org.feuyeux.dhyana.domain.DhyanaNode;
import org.feuyeux.dhyana.domain.DhyanaState;
import org.feuyeux.dhyana.transport.ConfigTransport;
import org.feuyeux.dhyana.transport.DhyanaTransport;
import org.feuyeux.dhyana.transport.NetworkUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author feuyeux@gmail.com
 * @date 2019/01/09
 */
@Service
@Slf4j
public class DhyanaEngine {
    private DhyanaNode currentNode;
    private DhyanaCandidate me;
    @Autowired
    private DhyanaTransport dhyanaTransport;
    @Autowired
    private ConfigTransport configTransport;
    @Autowired
    private DhyanaConfig dhyanaConfig;
    private KeepAliveTask keepAliveTask;
    private DhyanaElectTask electTask;
    private ThreadPoolExecutor executor = new ThreadPoolExecutor(10,
        30,
        5L,
        TimeUnit.MINUTES,
        new LinkedBlockingQueue<>(10),
        new ThreadFactoryBuilder().setNameFormat("dhyana-task-pool-%d").build(),
        new ThreadPoolExecutor.AbortPolicy());

    @PostConstruct
    public void init() {
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        log.debug("Dhyana Engine initializing... ");
        String dhyanaPort = System.getProperty("dhyana.port");
        if (dhyanaPort != null) {
            log.info("dhyana.dhyana.port={}", dhyanaPort);
            dhyanaConfig.setPort(Integer.valueOf(dhyanaPort));
        }

        String nodeName = System.getProperty("dhyana.nodeName");
        if (dhyanaPort != null) {
            log.info("dhyana.nodeName={}", nodeName);
            dhyanaConfig.setNodeName(nodeName);
        }

        String clean = System.getProperty("dhyana.cleanNode");
        if (clean != null && clean.equals(DhyanaConstants.MASTER_AGREE)) {
            configTransport.cleanCandidates();
            configTransport.cleanNodes();
        }

        nodeName = dhyanaConfig.getNodeName();
        if (nodeName == null) {
            nodeName = UUID.randomUUID().toString();
        }
        String ip = NetworkUtils.getNodeIp();
        int port = dhyanaConfig.getPort();

        this.currentNode = DhyanaNode.builder()
            .ip(ip)
            .port(port)
            .clusterName(dhyanaConfig.getClusterName())
            .nodeName(nodeName)
            .state(DhyanaState.INIT)
            .master(DhyanaConstants.MASTER_NOT_AVAILABLE)
            .build();

        me = DhyanaCandidate.builder()
            .id(nodeName)
            .ip(ip)
            .port(port)
            .build();

        dhyanaTransport.init(payload -> {
            String myMaster = getMaster();
            if (myMaster == null) {
                return false;
            }
            return myMaster.equals(payload);
        }, payload -> getMaster());

        configTransport.register(me);
        log.debug("Dhyana Engine start to execute elect... ");
        executeElect();

        log.debug("Dhyana Engine start to execute keep-alive... ");
        executeKeepalive();
    }

    private void shutdown() {
        if (keepAliveTask != null) {keepAliveTask.setRunning(false);}
        if (electTask != null) {electTask.setRunning(false);}
        configTransport.unRegister(me);
        currentNode.setState(DhyanaState.CLOSE);
        configTransport.updateNode(currentNode);
        System.exit(0);
    }

    private void executeKeepalive() {
        keepAliveTask = new KeepAliveTask(
            currentNode,
            configTransport,
            dhyanaTransport,
            new KeepAliveCallback());
        executor.submit(keepAliveTask);
    }

    private void executeElect() {
        currentNode.setState(DhyanaState.ELECTING);
        configTransport.updateNode(currentNode);
        electTask = new DhyanaElectTask(
            currentNode,
            configTransport,
            dhyanaTransport,
            dhyanaConfig,
            new ElectMasterCallback());
        executor.submit(electTask);
    }

    public boolean isMaster() {
        if (currentNode.getState() == DhyanaState.ELECTED) {
            String master = currentNode.getMaster();
            return currentNode.getNodeName().equals(master);
        } else {
            return false;
        }
    }

    public String getMaster() {
        return currentNode.getMaster();
    }

    class ElectMasterCallback {
        public void onSuccess(String master) {
            log.info("== Master Elected: {} ==", master);
            currentNode.setState(DhyanaState.ELECTED);
            configTransport.updateNode(currentNode);
            DhyanaNode dhyanaNode = DhyanaCache.cleanDeadMaster();
            if (dhyanaNode != null) {
                configTransport.cleanNode(dhyanaNode);
            }
        }

        public void onFailure(Exception e) {
            log.error("", e);
            currentNode.setState(DhyanaState.CLOSE);
            configTransport.updateNode(currentNode);
            shutdown();
        }
    }

    class KeepAliveCallback {
        final AtomicInteger retry = new AtomicInteger(0);

        public void onFailure(DhyanaNode dhyanaNode, Exception e) {
            DhyanaCache.markDeadNode(dhyanaNode);
            String errorMessage = e == null ? "" : e.getMessage();
            log.error("{}", errorMessage);
            String node = dhyanaNode.getNodeName();
            String master = currentNode.getMaster();
            if (node.equals(master)) {
                if (retry.incrementAndGet() >= DhyanaConstants.KEEP_ALIVE_MAX_RETRY - 1) {
                    DhyanaCache.markDeadMaster(dhyanaNode);
                    currentNode.setMaster(DhyanaConstants.MASTER_NOT_AVAILABLE);
                }
            } else {
                if (node.equals(DhyanaCache.getDeadMaster())) {
                    if (retry.incrementAndGet() >= DhyanaConstants.KEEP_ALIVE_MAX_RETRY) {
                        retry.set(0);
                        if (currentNode.getState() != DhyanaState.ELECTING) {
                            executeElect();
                        }
                    }
                } else {
                    final Integer retryTime = DhyanaCache.getDeadNodeRetryTime(dhyanaNode);
                    if (retryTime >= DhyanaConstants.DEAD_NODE_MAX_RETRY) {
                        configTransport.cleanNode(dhyanaNode);
                        DhyanaCache.cleanDeadNode(dhyanaNode);
                    }
                }
            }
        }

        public void onSuccess(DhyanaNode dhyanaNode) {
            DhyanaCache.cleanDeadNode(dhyanaNode);
            String othersMaster = DhyanaCache.getElect(dhyanaConfig.getMinimumNodes());
            String currentMaster = currentNode.getMaster();
            if (othersMaster != null && !DhyanaConstants.MASTER_NOT_AVAILABLE.equals(othersMaster)) {
                if (!othersMaster.equals(currentMaster)) {
                    log.debug("Current master[{}] is not equals with others[{}]", currentMaster, othersMaster);
                    if (currentNode.getState() != DhyanaState.ELECTING) {
                        executeElect();
                    }
                }
            }
        }
    }
}
