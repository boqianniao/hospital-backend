package com.hospital.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 搜索联想词，type 用于前端直接切换到对应分类。 */
@Data
@AllArgsConstructor
public class SearchSuggestionVO {

    private String keyword;
    private String type;
    private String typeName;
}
