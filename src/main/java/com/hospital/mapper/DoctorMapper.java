package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {

    /** 关注数原子增减 */
    @Update("UPDATE t_doctor SET follow_count = follow_count + #{delta} WHERE id = #{id} AND follow_count + #{delta} >= 0")
    int updateFollowCount(Long id, int delta);

    /** 接诊次数 +1 */
    @Update("UPDATE t_doctor SET consult_count = consult_count + 1 WHERE id = #{id}")
    int incrConsultCount(Long id);
}
