package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Disease;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DiseaseMapper extends BaseMapper<Disease> {

    /** 关注数原子增减 */
    @Update("UPDATE t_disease SET follow_count = follow_count + #{delta} WHERE id = #{id} AND follow_count + #{delta} >= 0")
    int updateFollowCount(Long id, int delta);
}
