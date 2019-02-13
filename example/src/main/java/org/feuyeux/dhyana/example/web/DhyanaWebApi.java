package org.feuyeux.dhyana.example.web;

import com.alibaba.fastjson.JSON;
import org.feuyeux.dhyana.domain.DhyanaNode;
import org.feuyeux.dhyana.transport.ConfigTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

/**
 * @author feuyeux@gmail.com
 * @date 2019/02/01
 */
@RestController
public class DhyanaWebApi {
    @Autowired
    private ConfigTransport configTransport;

    @GetMapping("/cluster")
    public String cluster() {
        Collection<DhyanaNode> dhyanaNodes = configTransport.readNodes();
        return JSON.toJSONString(dhyanaNodes);
    }
}
