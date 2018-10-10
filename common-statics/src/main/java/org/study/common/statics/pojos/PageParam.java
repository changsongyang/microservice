package org.study.common.statics.pojos;

import java.io.Serializable;

/**
 * @Title：
 * @Description：分页查询的参数
 * @author： chenyf
 * @Version： V1.0
 * @Date： 2018/2/2
 */
public class PageParam implements Serializable {
    private static final long serialVersionUID = 6297178964005032338L;
    /**
     * 当前页数，默认为1，必须设置默认值，否则分页查询时容易报空指针异常
     */
    private int pageCurrent = 1;
    /**
     * 每页记录数，默认为2，必须设置默认值，否则分页查询时容易报空指针异常
     */
    private int pageSize = 20;
    /**
     * 是否需要总记录数，默认为false
     */
    private boolean isNeedTotalRecord = false;
    /**
     * 总记录数，根据查询条件查询出来的总记录数
     */
    private long totalRecord;
    /**
     * 排序字段及排序方向
     */
    private String sortColumns;

    public PageParam(){
    }

    public int getPageCurrent() {
        return pageCurrent;
    }

    public void setPageCurrent(int pageCurrent) {
        this.pageCurrent = pageCurrent;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public boolean isNeedTotalRecord() {
        return this.isNeedTotalRecord;
    }

    public void setIsNeedTotalRecord(boolean isNeedTotalRecord) {
        this.isNeedTotalRecord = isNeedTotalRecord;
    }

    public long getTotalRecord() {
        return totalRecord;
    }

    public void setTotalRecord(long totalRecord) {
        this.totalRecord = totalRecord;
    }

    public String getSortColumns() {
        return sortColumns;
    }

    public void setSortColumns(String sortColumns) {
        this.sortColumns = sortColumns;
    }

    public static PageParam newInstance(int pageCurrent, int pageSize){
        PageParam pageParam = new PageParam();
        pageParam.setPageCurrent(pageCurrent);
        pageParam.setPageSize(pageSize);
        return pageParam;
    }

    public static PageParam newInstance(int pageCurrent, int pageSize, String sortColumns){
        PageParam pageParam = new PageParam();
        pageParam.setPageCurrent(pageCurrent);
        pageParam.setPageSize(pageSize);
        pageParam.setSortColumns(sortColumns);
        return pageParam;
    }
}
