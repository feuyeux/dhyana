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
public class DhyanaCandidate {
    private String id;
    private String ip;
    private int port;
}
