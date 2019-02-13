package org.feuyeux.dhyana.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author feuyeux@gmail.com
 * @date 2019/01/09
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DhyanaNode {
    private String ip;
    private int port;
    private String clusterName;
    private String nodeName;
    private String master;
    private DhyanaState state;
}
