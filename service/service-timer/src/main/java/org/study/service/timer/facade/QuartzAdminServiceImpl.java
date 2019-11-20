package org.study.service.timer.facade;

import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.study.common.statics.pojos.PageParam;
import org.study.common.statics.pojos.PageResult;
import org.study.facade.timer.entity.Instance;
import org.study.facade.timer.entity.Namespace;
import org.study.facade.timer.service.QuartzAdminService;
import org.study.service.timer.biz.InstanceBiz;
import org.study.service.timer.biz.NamespaceBiz;

import java.util.List;
import java.util.Map;

@Service
public class QuartzAdminServiceImpl implements QuartzAdminService {
    @Autowired
    NamespaceBiz namespaceBiz;
    @Autowired
    InstanceBiz instanceStageBiz;

    /**
     * 暂停某个命名空间下的所有实例
     * @return
     */
    @Override
    public boolean pauseAllInstanceAsync(String namespace){
        return namespaceBiz.pauseNamespaceAsync(namespace);
    }

    /**
     * 恢复某个命名空间下被暂停的所有实例
     * @return
     */
    @Override
    public boolean resumeAllInstanceAsync(String namespace){
        return namespaceBiz.resumeNamespaceAsync(namespace);
    }

    /**
     * 取得所有命名空间
     * @return
     */
    @Override
    public List<Namespace> listAllNamespace(){
        return namespaceBiz.listAllNamespace();
    }

    /**
     * 判断命名空间下的所有实例是否都处于暂停状态
     */
    @Override
    public boolean isAllInstancePausing(String namespace){
        return instanceStageBiz.isAllInstancePausing(namespace);
    }

    /**
     * 判断命名空间下的所有实例是否都处于运行状态
     */
    @Override
    public boolean isAllInstanceRunning(String namespace){
        return instanceStageBiz.isAllInstanceRunning(namespace);
    }

    /**
     * 分页查询实例列表
     * @param pageParam
     * @param paramMap
     * @return
     */
    @Override
    public PageResult<List<Instance>> listInstancePage(Map<String, Object> paramMap, PageParam pageParam){
        return instanceStageBiz.listInstancePage(paramMap, pageParam);
    }
}
