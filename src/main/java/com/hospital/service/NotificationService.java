package com.hospital.service;

import com.hospital.entity.Message;
import com.hospital.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 站内消息推送（系统消息/订单通知）。写入 t_message，失败不影响主流程。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final MessageMapper messageMapper;

    public void push(Long userId, String title, String content) {
        try {
            Message m = new Message();
            m.setUserId(userId);
            m.setTitle(title);
            m.setContent(content);
            m.setIsRead(0);
            messageMapper.insert(m);
        } catch (Exception e) {
            log.warn("站内消息推送失败 userId={} title={}: {}", userId, title, e.getMessage());
        }
    }
}
