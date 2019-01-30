package org.study.demo.nacos;

import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.study.demo.nacos.config.EnvConfig;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("demo")
public class DemoController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    EnvConfig envConfig;

    @ResponseBody
    @RequestMapping(value = "/configChange", method = RequestMethod.GET)
    public Map<String, Object> configChange() {
        Map<String, Object> map = new HashMap<>();
        map.put("useLocalCache", envConfig.getUseLocalCache());
        map.put("localKey", envConfig.getLocalKey());
        map.put("localValue", envConfig.getLocalValue());

        log.info("Map = {}", JSON.toJSONString(map));
        return map;
    }

}
