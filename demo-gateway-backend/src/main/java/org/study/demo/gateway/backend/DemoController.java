package org.study.demo.gateway.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.study.common.util.utils.JsonUtil;
import org.study.demo.gateway.backend.vo.CountryVo;
import org.study.demo.gateway.backend.vo.RequestVo;
import org.study.demo.gateway.backend.vo.ResponseVo;


@RestController
@RequestMapping("demo")
public class DemoController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(value = "/country", method = RequestMethod.POST)
    public ResponseVo<CountryVo> country(@RequestBody RequestVo<CountryVo> requestVo) {
        log.info("RequestVo = {}", JsonUtil.toString(requestVo));

        if(requestVo == null){
            requestVo = new RequestVo();
        }
        requestVo.getData().setNum(86);
        requestVo.getData().setCnName("中国");
        requestVo.getData().setEnName("China");

        ResponseVo responseVo = new ResponseVo();
        responseVo.setData(requestVo.getData());
        responseVo.setMchNo("22222222222222");
        return responseVo;
    }

}
