package org.study.facade.timer.service;

import org.study.common.statics.exceptions.BizException;
import org.study.common.statics.pojos.PageParam;
import org.study.common.statics.pojos.PageResult;
import org.study.facade.timer.entity.Instance;
import org.study.facade.timer.entity.Namespace;

import java.util.List;
import java.util.Map;

/**
 * Quartz实例管理接口
 */
public interface QuartzAdminService {

    /**
     * 暂停某个命名空间下的所有实例(异步)
     *
     * 注意：
     *     1、此方法会挂起当前命名空间下所有实例的所有任务，但应用并不会关闭，可调用 {@link #resumeAllInstanceAsync(String)} 恢复
     *     2、此方法内部是通过定时轮训数据库的形式来更新实例状态的，因此可能存在一定的延时性(默认5秒)，可调用 {@link #isAllInstancePausing(String)} 检测是否已全部暂停
     * @param namespace
     * @return  成功返回true，失败则返回false
     * @throws BizException
     */
    public boolean pauseAllInstanceAsync(String namespace) throws BizException;

    /**
     * 恢复某个命名空间下被暂停的所有实例(异步)
     *
     * 注意：
     *     1、此方法内部是通过定时轮训数据库的形式来更新实例状态的，因此可能存在一定的延时性(默认5秒)，可调用 {@link #isAllInstanceRunning(String)} 检测是否已全部恢复
     * @param namespace
     * @return  成功返回true，失败则返回false
     * @throws BizException
     */
    public boolean resumeAllInstanceAsync(String namespace) throws BizException;

    /**
     * 断命名空间下的所有实例是否都处于暂停状态
     * @param namespace
     * @return  已全部暂停则返回true，否则返回false
     * @throws BizException
     */
    public boolean isAllInstancePausing(String namespace) throws BizException;

    /**
     * 判断命名空间下的所有实例是否都处于运行状态
     * @param namespace
     * @return  已全部处于运行中则返回true，否则返回false
     * @throws BizException
     */
    public boolean isAllInstanceRunning(String namespace) throws BizException;

    /**
     * 分页查询实例列表
     * @param pageParam
     * @param paramMap
     * @return
     */
    public PageResult<List<Instance>> listInstancePage(Map<String, Object> paramMap, PageParam pageParam);

    /**
     * 取得所有命名空间
     * @return
     */
    public List<Namespace> listAllNamespace();
}
