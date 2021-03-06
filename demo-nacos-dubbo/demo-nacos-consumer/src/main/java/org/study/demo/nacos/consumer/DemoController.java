package org.study.demo.nacos.consumer;

import org.apache.dubbo.config.annotation.Reference;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.study.common.util.dto.EsQuery;
import org.study.demo.nacos.api.facade.DemoFacade;
import org.study.demo.nacos.api.vo.HelloVo;

import java.util.HashMap;
import java.util.Random;

@RestController
@RequestMapping("demo")
public class DemoController {
    @Reference
    DemoFacade demoFacade;

    @RequestMapping(value = "/sayHello", method = RequestMethod.GET)
    public String sayHello(String content) {
        String value = demoFacade.syaHello(content);
        return "{\"returnValue\":\"" + value + "\"}";
    }

    @RequestMapping(value = "/sayHello2", method = RequestMethod.GET)
    public HelloVo sayHello2(String content) {
        HelloVo helloVo = new HelloVo();
        helloVo.setContent(content);
        helloVo.setCount(new Random().nextInt(20));
        helloVo.setDescription("测试一下");
        return demoFacade.syaHello(helloVo);
    }

    @RequestMapping(value = "/esQuery", method = RequestMethod.GET)
    public EsQuery esQuery(String index){
        EsQuery esQuery = EsQuery.build(true).from(index);

        esQuery
//                    .eq("userNo", "888100000005252")
//                .neq("userNo", "888100000005252")
                .notIn("alterType", "1,2,3".split(","))
                .page(1, 20)
                .orderBy("userNo asc")
                .resultClass(HashMap.class);
        return demoFacade.hello(esQuery);
    }
}
