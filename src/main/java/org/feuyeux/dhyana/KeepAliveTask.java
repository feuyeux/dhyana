package org.feuyeux.dhyana;

import org.feuyeux.dhyana.domain.DhyanaNode;
import org.feuyeux.dhyana.transport.ConfigTransport;
import org.feuyeux.dhyana.transport.DhyanaTransport;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.feuyeux.dhyana.domain.DhyanaConstants;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author feuyeux@gmail.com
 * @date 2019/01/09
 */
@Slf4j
class KeepAliveTask implements Runnable {
    private final DhyanaEngine.KeepAliveCallback callback;
    private final ConfigTransport configTransport;
    private final DhyanaTransport dhyanaTransport;
    private final DhyanaNode currentNode;

    @Setter
    private boolean isRunning = true;

    KeepAliveTask(DhyanaNode currentNode, ConfigTransport configTransport, DhyanaTransport dhyanaTransport,
                  DhyanaEngine.KeepAliveCallback electMasterCallback) {
        this.currentNode = currentNode;
        this.configTransport = configTransport;
        this.dhyanaTransport = dhyanaTransport;
        this.callback = electMasterCallback;
    }

    @Override
    public void run() {
        while (isRunning) {
            if (Thread.currentThread().isInterrupted()) {
                log.error("Thread is interrupted.");
                return;
            }
            try {
                TimeUnit.SECONDS.sleep(DhyanaConstants.KEEP_ALIVE_INTERVAL_SECONDS);
            } catch (InterruptedException ignored) { }
            doRun();
        }
    }

    private void doRun() {
        if (!isRunning) {
            return;
        }
        List<DhyanaNode> nodes = configTransport.readNodes();
        nodes.parallelStream()
            .filter(n -> {
                boolean isOtherHost = !n.getIp().equals(currentNode.getIp());
                boolean isOtherPort = n.getPort() != currentNode.getPort();
                return isOtherHost || isOtherPort;
            })
            .forEach(n -> {
                try {
                    String health = dhyanaTransport.health(n);
                    if (health == null) {
                        callback.onFailure(n, new Exception("No health answer:" + n));
                    } else {
                        DhyanaCache.markElect(health, n.getIp(), n.getPort());
                        callback.onSuccess(n);
                    }
                } catch (Exception e) {
                    callback.onFailure(n, e);
                }
            });
    }
}
