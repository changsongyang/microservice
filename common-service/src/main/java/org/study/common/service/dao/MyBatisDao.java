package org.study.common.service.dao;

import org.apache.ibatis.session.RowBounds;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.study.common.statics.exceptions.BizException;
import org.study.common.statics.pojos.BaseEntity;
import org.study.common.statics.pojos.PageParam;
import org.study.common.statics.pojos.PageResult;
import org.study.common.util.utils.ClassUtil;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于Mybatis的基础DAO，实现了常用的增删改查方法
 *
 * @author chenyf
 * @date 2018-02-02
 */
public class MyBatisDao<T extends Serializable, PK extends Serializable> extends SqlSessionDaoSupport {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 需要排序时排序字段在Map中的key名
     */
    public final static String SORT_COLUMNS = "sortColumns";
    /**
     * mybatis的命名空间与sqlId之间的分隔符
     */
    private final static String NAMESPACE_SEPARATOR = ".";
    /**
     * 在mybatis的mapper文件中的命名空间（等于当前Dao的实体类的class全名）
     */
    private String mapperNamespace;
    /**
     * 主键的列名称，默认使用id作为这列的列名称
     */
    private String pkColumnName = "id";

    /**----------------  在mybatis的mapper文件中一些常用的、并且跟本类中的方法相对应的sqlId  ------------------*/
    private final static String INSERT_SQL = "insert";
    private final static String INSERT_LIST_SQL = "batchInsert";
    private final static String UPDATE_SQL = "update";
    private final static String UPDATE_IF_NOT_NULL_SQL = "updateIfNotNull";
    private final static String DELETE_BY_SQL = "deleteBy";
    private final static String COUNT_BY_SQL = "countBy";
    private final static String LIST_BY_SQL = "listBy";
    private final static String GET_BY_PK_SQL = "getByPk";
    private final static String LIST_BY_PK_LIST_SQL = "listByPkList";
    private final static String DELETE_BY_PK_SQL = "deleteByPk";
    private final static String DELETE_BY_PK_LIST_SQL = "deleteByPkList";

    @Override
    @Autowired
    public void setSqlSessionTemplate(SqlSessionTemplate sqlSessionTemplate){
        super.setSqlSessionTemplate(sqlSessionTemplate);
    }

    //初始化bean属性
    @Override
    protected void initDao() throws Exception {
        Class cla = ClassUtil.getSuperClassGenericType(this.getClass(), 0);
        this.mapperNamespace = cla.getName();

        //设置primary key的列名称
        Field[] fields = cla.getDeclaredFields();
        for(Field field : fields){
            org.study.common.statics.annotations.PK pk = field.getAnnotation(org.study.common.statics.annotations.PK.class);
            if(pk != null){
                pkColumnName = field.getName();
                break;
            }
        }
        Assert.hasLength(this.mapperNamespace, "Property 'mapperNamespace' are required");
        logger.debug("using '{}' as the primary key column name for {}", pkColumnName, cla.getName());
    }

    /**
     * 插入数据
     *
     * @param  entity
     * @return int 插入的记录数
     */
    public int insert(T entity) {
        return this.getSqlSession().insert(fillSqlId(INSERT_SQL), entity);
    }

    /**
     * 批量插入数据
     * @param list
     * @return
     */
    public int insert(List<T> list) {
        return this.getSqlSession().insert(fillSqlId(INSERT_LIST_SQL), list);
    }

    /**
     * 根据自定义sql插入数据
     *
     * @param param
     * @return int 更新记录数
     */
    public int insert(String sqlId, Object param) {
        return this.getSqlSession().insert(fillSqlId(sqlId), param);
    }

    /**
     * 根据多个条件删除记录
     * @param paramMap
     */
    public int deleteBy(Map<String, Object> paramMap) {
        return this.deleteBy(DELETE_BY_SQL, paramMap);
    }

    /**
     * 根据自定义sql删除记录
     * @param param
     */
    public int deleteBy(String sqlId, Object param) {
        return this.getSqlSession().delete(this.fillSqlId(sqlId), param);
    }

    /**
     * 更新数据
     * @param entity
     * @return int 更新记录数
     */
    public int update(T entity) {
        int result = this.getSqlSession().update(fillSqlId(UPDATE_SQL), entity);
        if (result <= 0){
            throw new BizException(BizException.DB_UPDATE_RESULT_NOT_MATCH, "数据更新成功的记录数为0");
        }
        return result;
    }

    /**
     * 批量更新对象，如果需要保持数据的一致性，需要确保调用这个方法的地方有支持事务
     * @param entityList
     * @return int 更新记录数
     */
    public int update(List<T> entityList) {
        int result = 0;
        int expectCount = entityList.size();
        for(T entity : entityList){
            int updateCount = this.update(entity);
            result += updateCount;
        }
        if (result <= 0){
            throw new BizException(BizException.DB_UPDATE_RESULT_NOT_MATCH, "数据更新成功的记录数为0");
        }else if(result != entityList.size()){
            throw new BizException(BizException.DB_UPDATE_RESULT_NOT_MATCH, "数据更新成功的记录数("+result+")与预期数("+expectCount+")不匹配");
        }
        return result;
    }

    /**
     * 按值更新，字段值不为null的才更新
     * @param entity
     * @return int 更新记录数
     */
    public int updateIfNotNull(T entity) {
        int result = this.getSqlSession().update(fillSqlId(UPDATE_IF_NOT_NULL_SQL), entity);
        if (result <= 0){
            throw new BizException(BizException.DB_UPDATE_RESULT_NOT_MATCH, "数据更新成功的记录数为0");
        }
        return result;
    }

    /**
     * 按自定义sql的更新
     *
     * @param sqlId
     * @param param
     */
    public int update(String sqlId, Object param) {
        return this.getSqlSession().update(this.fillSqlId(sqlId), param);
    }

    /**
     * 取得只可能有一条记录的数据，如：根据 unique key 获取
     * 注意：请自行确保查询条件只会查到一条记录，否则会报错
     * @param param  需要使用Map类型的参数，因为listBy语句在Mapper文件中已指定是Map的参数，不然会报here is no getter for property named '*' in 'class java.lang.String'这样的异常
     * @return
     */
    public T getOne(Object param) {
        return this.getOne(LIST_BY_SQL, param);
    }

    /**
     * 根据自定义语句，获取符合条件的单个对象
     * @param sqlId
     * @param param
     * @return
     */
    public <E> E getOne(String sqlId, Object param) {
        return this.getSqlSession().selectOne(fillSqlId(sqlId), param);
    }

    /**
     * 获取所有记录并返回List
     * @return
     */
    public List<T> listAll() {
        return this.listAll(null);
    }

    /**
     * 获取所有记录并返回List，并指定排序字段
     * @param sortColumns 排序条件
     * @return
     */
    public List<T> listAll(String sortColumns) {
        Map<String, String> paramMap = null;
        if(isNotEmpty(sortColumns)){
            paramMap = new HashMap(1);
            paramMap.put(SORT_COLUMNS, this.filterSortColumns(sortColumns));
        }
        return this.getSqlSession().selectList(fillSqlId(LIST_BY_SQL), paramMap);
    }

    /**
     * 取得符合条件的总记录数
     * @param paramMap
     * @return
     */
    public long countBy(Map<String, Object> paramMap){
        return this.countBy(COUNT_BY_SQL, paramMap);
    }

    /**
     * 根据自定义语句，取得符合条件的总记录数
     * @param param
     * @return
     */
    public long countBy(String sqlId, Object param){
        Long counts = this.getSqlSession().selectOne(fillSqlId(sqlId), param);
        return counts==null ? 0 : counts.longValue();
    }

    /**
     * 多条件and查询并返回List(不分页、不排序)
     * @param paramMap
     * @return
     */
    public List<T> listBy(Map<String, Object> paramMap) {
        return this.listBy(paramMap, null);
    }

    /**
     * 多条件and查询并返回List(不分页、排序)
     * @param paramMap
     * @param sortColumns
     * @return
     */
    public List<T> listBy(Map<String, Object> paramMap, String sortColumns) {
        return this.listBy(LIST_BY_SQL, paramMap, sortColumns);
    }

    /**
     * 根据自定义语句，取得符合条件的记录并返回List(不分页)
     * @param sqlId
     * @param paramMap
     * @return
     */
    public <E> List<E> listBy(String sqlId, Map<String, Object> paramMap) {
        return this.listBy(sqlId, paramMap, null);
    }

    /**
     * 根据自定义语句，取得符合条件的记录并返回List(不分页、可排序)
     * @param paramMap
     * @param sortColumns
     * @return
     */
    public <E> List<E> listBy(String sqlId, Map<String, Object> paramMap, String sortColumns){
        if(isNotEmpty(sortColumns)){
            if(paramMap == null) {
                paramMap = new HashMap(1);
            }
            paramMap.put(SORT_COLUMNS, this.filterSortColumns(sortColumns));
        }
        return this.getSqlSession().selectList(fillSqlId(sqlId), paramMap);
    }

    /**
     * 多条件and查询并返回List(可分页、可排序)
     * @param paramMap
     * @param pageParam
     * @return
     */
    public PageResult<List<T>> listPage(Map<String, Object> paramMap, PageParam pageParam){
        return this.listPage(LIST_BY_SQL, COUNT_BY_SQL, paramMap, pageParam);
    }

    /**
     * 多条件and查询并返回List(可分页、可排序)
     * @param sqlId     查询的sqlId
     * @param paramMap
     * @param pageParam
     * @return
     */
    public <E> PageResult<List<E>> listPage(String sqlId, Map<String, Object> paramMap, PageParam pageParam){
        return this.listPage(sqlId, COUNT_BY_SQL, paramMap, pageParam);
    }

    /**
     * 根据自定义语句，取得符合条件的记录并返回List(可分页、可排序)
     * @param sqlId         查询的sqlId
     * @param countSqlId    计算总记录数的sqlId，如果PageParam中的isNeedTotalRecord=false，则此值会被忽略
     * @param paramMap
     * @param pageParam
     * @return
     */
    public <E> PageResult<List<E>> listPage(String sqlId, String countSqlId, Map<String, Object> paramMap, PageParam pageParam) {
        Long totalRecord = 0L;
        List<E> dataList;
        if(pageParam.isNeedTotalRecord()){
            totalRecord = this.countBy(countSqlId, paramMap);
            if(totalRecord <= 0){
                //如果总记录数为0，就直接返回了
                dataList = new ArrayList<>();
                return PageResult.newInstance(dataList, pageParam, totalRecord);
            }
        }

        if(isNotEmpty(pageParam.getSortColumns())){
            if (paramMap == null) {
                paramMap = new HashMap(1);
            }
            paramMap.put(SORT_COLUMNS, this.filterSortColumns(pageParam.getSortColumns()));
        }
        dataList = this.getSqlSession().selectList(fillSqlId(sqlId), paramMap,
                new RowBounds(getOffset(pageParam), pageParam.getPageSize()));
        if(! pageParam.isNeedTotalRecord()){
            totalRecord = (long) dataList.size();
        }
        return PageResult.newInstance(dataList, pageParam, totalRecord);
    }

    /**
     * 多条件查询并返回MAP（不分页）
     * @param paramMap
     * @param property
     * @param <K>
     * @return
     */
    public <K> Map<K, T> mapBy(Map<String, Object> paramMap, String property){
        return this.mapBy(LIST_BY_SQL, paramMap, property);
    }

    /**
     * 自定义语句查询并返回MAP（不分页）
     * key:value = 某个值为字符串的字段:实体对象 的键值对，其中key是property参数指定的字段名的值
     * @param paramMap
     * @return
     */
    public <K, E> Map<K, E> mapBy(String sqlId, Map<String, Object> paramMap, String property){
        return this.getSqlSession().selectMap(fillSqlId(sqlId), paramMap, property);
    }

    /**
     * 多条件查询并返回MAP（分页）
     * @param paramMap
     * @param property
     * @param <K>
     * @return
     */
    public <K> PageResult<Map<K, T>> mapPage(Map<String, Object> paramMap, String property, PageParam pageParam){
        return this.mapPage(LIST_BY_SQL, COUNT_BY_SQL, paramMap, property, pageParam);
    }

    /**
     * 自定义语句查询并返回MAP（分页）
     * 注意：会在数据库进行排序，但返回到程序中的Map是无序的
     * @param sqlId         查询的sqlId
     * @param countSqlId    计算总记录数的sqlId，如果PageParam中的isNeedTotalRecord=false，则此值会被忽略
     * @param paramMap
     * @param property
     * @param pageParam
     * @param <K>
     * @param <E>
     * @return Map key:value = 某个值为字符串的字段:实体对象 的键值对，其中key是property参数指定的字段名的值
     */
    public <K, E> PageResult<Map<K, E>> mapPage(String sqlId, String countSqlId, Map<String, Object> paramMap, String property, PageParam pageParam){
        Long totalRecord = 0L;
        Map<K, E> dataMap;
        if(pageParam.isNeedTotalRecord()){
            totalRecord = this.countBy(countSqlId, paramMap);
            if(totalRecord <= 0){
                //如果总记录数为0，就直接返回了
                dataMap = new HashMap<>();
                return PageResult.newInstance(dataMap, pageParam, totalRecord);
            }
        }

        if(isNotEmpty(pageParam.getSortColumns())){
            if (paramMap == null) {
                paramMap = new HashMap(1);
            }
            paramMap.put(SORT_COLUMNS, this.filterSortColumns(pageParam.getSortColumns()));
        }
        dataMap = this.getSqlSession().selectMap(fillSqlId(sqlId), paramMap, property,
                new RowBounds(getOffset(pageParam), pageParam.getPageSize()));
        if(! pageParam.isNeedTotalRecord()){
            totalRecord = (long)dataMap.size();
        }
        return PageResult.newInstance(dataMap, pageParam, totalRecord);
    }


    /**------------------------------------------------- 根据PK主键作相关操作的便捷方法 START ---------------------------------------------------*/
    /**
     * 根据主键删除记录
     * 当单一主键时传主键对象即可,当为组合组件时传MAP
     *
     * @param pk
     */
    public int deleteByPk(PK pk) {
        return this.getSqlSession().delete(fillSqlId(DELETE_BY_PK_SQL), pk);
    }

    /**
     * 根据多个主键删除记录
     * @param pkList
     * @return
     */
    public int deleteByPkList(List<PK> pkList) {
        return this.getSqlSession().delete(fillSqlId(DELETE_BY_PK_LIST_SQL), pkList);
    }

    /**
     * 根据主键获取记录
     * @param pk
     * @return
     */
    public T getByPk(PK pk) {
        return this.getSqlSession().selectOne(fillSqlId(GET_BY_PK_SQL), pk);
    }

    /**
     * 根据多个主键获取记录
     * @param pkList List<Long>
     * @return
     */
    public List<T> listByPkList(List<PK> pkList){
        return this.getSqlSession().selectList(fillSqlId(LIST_BY_PK_LIST_SQL), pkList);
    }

    /**
     * 多条件and查询并返回以id为key的MAP（不分页）
     * key:value = 主键:实体对象 的键值对，其中key默认是字段名为id的值
     * @param paramMap
     * @return
     */
    public Map<PK, T> mapByPk(Map<String, Object> paramMap){
        return this.mapBy(LIST_BY_SQL, paramMap, pkColumnName);
    }

    /**
     * 多条件查询并返回以id为key的MAP（分页）
     *
     * key:value = 主键:实体对象 的键值对，其中key默认是字段名为id的值
     * @param paramMap
     * @return
     */
    public PageResult<Map<PK, T>> mapByPkPage(Map<String, Object> paramMap, PageParam pageParam){
        return this.mapPage(LIST_BY_SQL, COUNT_BY_SQL, paramMap, pkColumnName, pageParam);
    }
    /**------------------------------------------------- 根据PK主键作相关操作的便捷方法 END ---------------------------------------------------*/


    /**
     * 先判断当前sqlId是否已经包含了命名空间，如果有，直接返回，如果没有，则为其补充在Mapper中完整的查询ID，即为这个sqlId加上了命名空间
     * @param sqlId
     * @return
     */
    protected final String fillSqlId(String sqlId){
        if(isNotEmpty(sqlId)){
            //已经指定了命名空间，则直接返回
            if(sqlId.contains(getMapperNamespace())){
                return sqlId;
            }
        }
        return getMapperNamespace() + NAMESPACE_SEPARATOR + sqlId;
    }

    /**
     * 校验sortColumn里面是否包含了：""、'' 这两种符号，避免sql注入
     * @param sortColumns
     * @return
     */
    protected final String filterSortColumns(String sortColumns){
        if(isNotEmpty(sortColumns)){
            if(sortColumns.contains("\"") || sortColumns.contains("'")){
                throw new IllegalArgumentException("Illegal sortColumns value");
            }
            return sortColumns;
        }
        return sortColumns;
    }

    /**
     * 取得Mapper中的命名空间
     * @return
     */
    private String getMapperNamespace() {
        return this.mapperNamespace;
    }

    /**
     * 计算分页查询时的起始位置
     * @param pageParam
     * @return
     */
    private int getOffset(PageParam pageParam){
        int calPageCurrent = pageParam.getPageCurrent() - 1;
        calPageCurrent = (calPageCurrent < 0) ? 0 : calPageCurrent;
        return calPageCurrent * pageParam.getPageSize();
    }

    /**
     * 判断字符串是否不为空
     * @param str
     * @return
     */
    private boolean isNotEmpty(String str){
        if(str != null && str.trim().length() > 0){
            return true;
        }
        return false;
    }
}
