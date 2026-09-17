package com.hospital.vo;

import com.hospital.common.result.PageResult;
import com.hospital.entity.Article;
import com.hospital.entity.Disease;
import com.hospital.entity.Doctor;
import com.hospital.entity.Hospital;
import lombok.AllArgsConstructor;
import lombok.Data;

/** 全站搜索聚合结果。 */
@Data
@AllArgsConstructor
public class SearchOverviewVO {

    private PageResult<Hospital> hospitals;
    private PageResult<Doctor> doctors;
    private PageResult<Disease> diseases;
    private PageResult<Article> articles;
}
