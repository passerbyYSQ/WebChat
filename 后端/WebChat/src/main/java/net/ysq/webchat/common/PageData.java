package net.ysq.webchat.common;

import java.util.List;

/**
 * 分页数据返回的模型
 *
 * @author passerbyYSQ
 * @create 2021-02-02 23:45
 */
public class PageData<T> {
    private Integer page; // 返回给前端的，被纠正的当前页。可能因为越界而被纠正
    private Integer count; // 每一页显示的记录数。前端不传，会赋默认值
    private Long total; // 总记录数
    private List<T> list; // 当前页的列表数据

    public PageData() {
    }

    public PageData(Integer page, Integer count, Long total, List<T> list) {
        this.page = page;
        this.count = count;
        this.total = total;
        this.list = list;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Long getTotal() {
        return total;
    }

    public void setTotal(Long total) {
        this.total = total;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
