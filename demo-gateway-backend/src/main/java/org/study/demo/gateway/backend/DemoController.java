package org.study.demo.gateway.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.study.common.util.utils.JsonUtil;
import org.study.demo.gateway.backend.vo.ProductVo;
import org.study.demo.gateway.backend.vo.RequestVo;
import org.study.demo.gateway.backend.vo.ResponseVo;


@RestController
@RequestMapping("demo")
public class DemoController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(value = "/country", method = RequestMethod.POST)
    public ResponseVo<ProductVo> country(@RequestBody RequestVo<ProductVo> requestVo) throws Exception{
        log.info("RequestVo = {}", JsonUtil.toString(requestVo));

        if(requestVo == null){
            requestVo = new RequestVo();
        }

//        if(1==1){
//            throw new Exception("测试服务一异常");
//        }

        ResponseVo responseVo = new ResponseVo<ProductVo>();
        responseVo.setRespCode("A0001");
        responseVo.setRespMsg("受理成功");
        responseVo.setMchNo(requestVo.getMchNo());
        responseVo.setSignType(requestVo.getSignType());
        responseVo.setData(requestVo.getData());
        return responseVo;
    }

}
