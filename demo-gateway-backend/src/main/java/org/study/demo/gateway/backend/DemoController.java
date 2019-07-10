package org.study.demo.gateway.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.study.common.api.enums.BizCodeEnum;
import org.study.common.api.enums.RespCodeEnum;
import org.study.common.api.vo.RequestVo;
import org.study.common.api.vo.ResponseVo;
import org.study.common.util.utils.AESUtil;
import org.study.common.util.utils.JsonUtil;
import org.study.common.util.utils.RandomUtil;
import org.study.demo.gateway.backend.vo.BatchVo;
import org.study.demo.gateway.backend.vo.DetailVo;
import org.study.demo.gateway.backend.vo.ProductVo;


@RestController
@RequestMapping("demo")
public class DemoController {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    @ResponseBody
    @RequestMapping(value = "/batch", method = RequestMethod.POST)
    public ResponseVo<BatchVo> country(@RequestBody RequestVo<BatchVo> requestVo) {
        log.info("处理前 RequestVo = {}", JsonUtil.toString(requestVo));

        if(requestVo == null){
            requestVo = new RequestVo();
        }

//        if(1==1){
//            throw new RuntimeException("测试服务异常");
//        }

        for(DetailVo detail : requestVo.getData().getDetails()){
            detail.setName(AESUtil.decryptECB(detail.getName(), requestVo.getSecKey()));
        }
        log.info("处理后 RequestVo = {}", JsonUtil.toString(requestVo));

        //重新加密返回给客户端，用以测试客户端解密是否正常
        String newSecKey = RandomUtil.get16LenStr();
        for(DetailVo detail : requestVo.getData().getDetails()){
            detail.setName(AESUtil.encryptECB(detail.getName() + "_return", newSecKey));
        }

        ResponseVo responseVo = ResponseVo.success(requestVo.getMchNo(), requestVo.getSignType(), requestVo.getData());
        responseVo.setSecKey(newSecKey);
        return responseVo;
    }

}
