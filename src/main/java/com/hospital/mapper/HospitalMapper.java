package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Hospital;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface HospitalMapper extends BaseMapper<Hospital> {

    /** 关注数原子增减 */
    @Update("UPDATE t_hospital SET follow_count = follow_count + #{delta} WHERE id = #{id} AND follow_count + #{delta} >= 0")
    int updateFollowCount(Long id, int delta);
}
