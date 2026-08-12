package com.xiaxiaoyu.xingbangmenu.common;

import java.util.List;

public class PageResult<T> {

    private List<T> list;
    private long total;
    private int page;
    private int pageSize;
    private int totalPages;

    public PageResult(List<T> list, long total, int page, int pageSize) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
    }

    public List<T> getList() { return list; }
    public long getTotal() { return total; }
    public int getPage() { return page; }
    public int getPageSize() { return pageSize; }
    public int getTotalPages() { return totalPages; }
}
