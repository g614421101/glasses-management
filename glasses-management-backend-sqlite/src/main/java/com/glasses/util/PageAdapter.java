package com.glasses.util;

import com.mybatisflex.core.paginate.Page;

import java.util.List;

public class PageAdapter<T> {

    private List<T> records;
    private long total;
    private long current;
    private long size;
    private long pages;

    public static <T> PageAdapter<T> of(Page<T> page) {
        PageAdapter<T> adapter = new PageAdapter<>();
        adapter.records = page.getRecords();
        adapter.total = page.getTotalRow();
        adapter.current = page.getPageNumber();
        adapter.size = page.getPageSize();
        adapter.pages = page.getTotalPage();
        return adapter;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getPages() {
        return pages;
    }

    public void setPages(long pages) {
        this.pages = pages;
    }
}
