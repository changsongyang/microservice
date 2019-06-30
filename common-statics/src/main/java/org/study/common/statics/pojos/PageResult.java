package org.study.common.statics.pojos;

/**
 * @Description:分页查询结果返回的bean
 * @author: chenyf
 * @Date: 2018/2/3
 */
public class PageResult<T> extends BaseResult<T> {
    private static final long serialVersionUID = -189498483727889L;

    /**
     * 当前页
     */
    private Integer pageCurrent;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Long totalRecord;

    /**
     * elasticsearch遍历扫描时的scrollId
     */
    private String scrollId;

    public Integer getPageCurrent() {
        return pageCurrent;
    }

    public void setPageCurrent(Integer pageCurrent) {
        this.pageCurrent = pageCurrent;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Long getTotalRecord() {
        return totalRecord;
    }

    public void setTotalRecord(Long totalRecord) {
        this.totalRecord = totalRecord;
    }

    public String getScrollId() {
        return scrollId;
    }

    public void setScrollId(String scrollId) {
        this.scrollId = scrollId;
    }

    public static <T> PageResult<T> newInstance(PageParam pageParam, T data){
        return PageResult.newInstance(data, pageParam, (long)BaseResult.calcDataLength(data));
    }

    public static <T> PageResult<T> newInstance(T data, PageParam pageParam, Long totalRecord){
        PageResult<T> pageResult = new PageResult<T>();
        pageResult.setPageCurrent(pageParam.getPageCurrent());
        pageResult.setPageSize(pageParam.getPageSize());
        pageResult.setData(data);
        pageResult.setTotalRecord(totalRecord);
        return pageResult;
    }

    public static <T> PageResult<T> newInstance(T data, Integer pageCurrent, Integer pageSize){
        return PageResult.newInstance(data, pageCurrent, pageSize, (long)BaseResult.calcDataLength(data));
    }

    public static <T> PageResult<T> newInstance(T data, Integer pageCurrent, Integer pageSize, Long totalRecord){
        PageResult<T> pageResult = new PageResult<T>();
        pageResult.setPageCurrent(pageCurrent);
        pageResult.setPageSize(pageSize);
        pageResult.setData(data);
        pageResult.setTotalRecord(totalRecord);
        return pageResult;
    }
}
