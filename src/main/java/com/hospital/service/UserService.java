package com.hospital.service;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hospital.common.exception.BusinessException;
import com.hospital.common.result.ResultCode;
import com.hospital.dto.auth.ChangePasswordDTO;
import com.hospital.dto.auth.UpdateProfileDTO;
import com.hospital.entity.User;
import com.hospital.mapper.UserMapper;
import com.hospital.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 用户资料相关：查询、更新、修改密码。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public User getByPhone(String phone) {
        return userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, phone));
    }

    public User getByUsername(String username) {
        return userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getUsername, username));
    }

    public User getByIdOrThrow(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return user;
    }

    public UserVO getProfile(Long userId) {
        return toVO(getByIdOrThrow(userId));
    }

    /** 更新个人资料，仅更新非空字段 */
    public UserVO updateProfile(Long userId, UpdateProfileDTO dto) {
        User user = getByIdOrThrow(userId);
        // 用户名变更需保证唯一
        if (StringUtils.hasText(dto.getUsername()) && !dto.getUsername().equals(user.getUsername())) {
            User exist = getByUsername(dto.getUsername());
            if (exist != null && !exist.getId().equals(userId)) {
                throw new BusinessException("该用户名已被占用");
            }
            user.setUsername(dto.getUsername());
        }
        if (dto.getRealName() != null) user.setRealName(dto.getRealName());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getGender() != null) user.setGender(dto.getGender());
        if (dto.getBirthday() != null) user.setBirthday(dto.getBirthday());
        if (StringUtils.hasText(dto.getAvatar())) user.setAvatar(dto.getAvatar());
        userMapper.updateById(user);
        return toVO(user);
    }

    /** 修改密码：校验原密码 */
    public void changePassword(Long userId, ChangePasswordDTO dto) {
        User user = getByIdOrThrow(userId);
        if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.OLD_PASSWORD_ERROR);
        }
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    public UserVO toVO(User user) {
        UserVO vo = new UserVO();
        BeanUtil.copyProperties(user, vo);
        return vo;
    }
}
