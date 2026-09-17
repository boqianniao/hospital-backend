package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    /** 医生平均评分 */
    @Select("SELECT ROUND(AVG(rating), 2) FROM t_review WHERE doctor_id = #{doctorId}")
    Double avgRatingByDoctor(Long doctorId);
}
