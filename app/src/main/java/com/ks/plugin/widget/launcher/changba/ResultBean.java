package com.ks.plugin.widget.launcher.changba;

import java.util.List;

/**
 * Created by Administrator on 2018/3/5.
 */
public class ResultBean {
    private String currdressid;
    private List<TitlePhoto> items;

    public String getCurrdressid() {
        return currdressid;
    }

    public void setCurrdressid(String currdressid) {
        this.currdressid = currdressid;
    }

    public List<TitlePhoto> getItems() {
        return items;
    }

    public void setItems(List<TitlePhoto> items) {
        this.items = items;
    }
}