package net.ysq.webchat.dao;

import net.ysq.webchat.common.BaseMapper;
import net.ysq.webchat.po.ChatMsg;

import java.util.List;

public interface ChatMsgMapper extends BaseMapper<ChatMsg> {

    /**
     * 批量签收
     */
    int batchUpdateMsgSigned(List<String> msgIdList);

}
