package com.hospital.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hospital.common.exception.BusinessException;
import com.hospital.common.result.PageResult;
import com.hospital.common.result.ResultCode;
import com.hospital.entity.Message;
import com.hospital.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 站内消息：列表、未读数、标记已读、删除。
 */
@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageMapper messageMapper;

    public PageResult<Message> list(Long userId, Integer isRead, long pageNum, long pageSize) {
        IPage<Message> page = messageMapper.selectPage(new Page<>(pageNum, pageSize),
                Wrappers.<Message>lambdaQuery()
                        .eq(Message::getUserId, userId)
                        .eq(isRead != null, Message::getIsRead, isRead)
                        .orderByDesc(Message::getId));
        return PageResult.of(page);
    }

    public long unreadCount(Long userId) {
        Long c = messageMapper.selectCount(Wrappers.<Message>lambdaQuery()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0));
        return c == null ? 0 : c;
    }

    public void markRead(Long userId, Long id) {
        Message m = getOwned(userId, id);
        if (m.getIsRead() != null && m.getIsRead() == 1) {
            return;
        }
        Message upd = new Message();
        upd.setId(id);
        upd.setIsRead(1);
        messageMapper.updateById(upd);
    }

    public void markAllRead(Long userId) {
        Message upd = new Message();
        upd.setIsRead(1);
        messageMapper.update(upd, Wrappers.<Message>lambdaUpdate()
                .eq(Message::getUserId, userId)
                .eq(Message::getIsRead, 0));
    }

    public void delete(Long userId, Long id) {
        getOwned(userId, id);
        messageMapper.deleteById(id);
    }

    private Message getOwned(Long userId, Long id) {
        Message m = messageMapper.selectById(id);
        if (m == null) {
            throw new BusinessException(ResultCode.RESOURCE_NOT_FOUND);
        }
        if (!m.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        return m;
    }
}
