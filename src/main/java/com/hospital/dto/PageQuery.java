package com.hospital.dto;

import lombok.Data;

/**
 * 分页查询基类。
 */
@Data
public class PageQuery {

    private Integer pageNum = 1;
    private Integer pageSize = 10;

    public long current() {
        return (pageNum == null || pageNum < 1) ? 1 : pageNum;
    }

    public long size() {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }
}
