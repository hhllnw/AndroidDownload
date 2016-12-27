package com.example.hhllnw.download.entities;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by hhl on 2016/12/20.
 * 下载实体
 */
@DatabaseTable(tableName = "downloadentity")
public class DownloadEntity implements Serializable {
    @DatabaseField(id = true)
    private String id;
    @DatabaseField
    private String name;
    @DatabaseField
    private String url;
    @DatabaseField
    private int curLength;
    @DatabaseField
    private int totalLength;
    @DatabaseField
    private Status status = Status.idle;
    @DatabaseField
    private boolean isSupportRange = false;
    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private HashMap<Integer, Integer> ranges;
    @DatabaseField
    private int percent;

    public DownloadEntity() {
    }

    public DownloadEntity(String url) {
        this.url = url;
    }

    public DownloadEntity(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public enum Status {idle, connecting, err, waiting, downloading, pause, resume, cancel, completed}


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getCurLength() {
        return curLength;
    }

    public void setCurLength(int curLength) {
        this.curLength = curLength;
    }

    public int getTotalLength() {
        return totalLength;
    }

    public void setTotalLength(int totalLength) {
        this.totalLength = totalLength;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isSupportRange() {
        return isSupportRange;
    }

    public void setSupportRange(boolean supportRange) {
        isSupportRange = supportRange;
    }

    public HashMap<Integer, Integer> getRanges() {
        return ranges;
    }

    public void setRanges(HashMap<Integer, Integer> ranges) {
        this.ranges = ranges;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj != null)
            return obj.hashCode() == this.hashCode();
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
