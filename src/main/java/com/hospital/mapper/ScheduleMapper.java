package com.hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.entity.Schedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ScheduleMapper extends BaseMapper<Schedule> {

    /**
     * 原子扣减号源（DB 层兜底，配合 Redis 预扣）：仅在剩余>0 时扣减，扣到 0 置为约满。
     * @return 影响行数，0 表示已无号源
     */
    @Update("UPDATE t_schedule SET remain_count = remain_count - 1, " +
            "status = CASE WHEN remain_count - 1 <= 0 THEN 0 ELSE status END " +
            "WHERE id = #{id} AND remain_count > 0")
    int deductStock(Long id);

    /** 回补号源（取消/超时释放），并恢复可预约状态 */
    @Update("UPDATE t_schedule SET remain_count = remain_count + 1, status = 1 " +
            "WHERE id = #{id} AND remain_count < total_count")
    int restoreStock(Long id);
}
