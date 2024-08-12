package com.noregret;

public enum MsgType {
    MSG_LOGIN, //登录
    MSG_REGISTER, //注册
    MSG_HOME, //个人主页
    MSG_LIST_FRIEND, //好友列表
    MSG_FRIEND_REQUEST, //好友申请
    MSG_NOTICE, //实时通知申请
    MSG_LIST_FRIEND_REQUEST, //好友申请列表
    MSG_FRIEND_RESPONSE, //处理好友申请
    MSG_DELETE_FRIEND, //删除好友
    MSG_OFFLINE, //离线
    MSG_PRIVATE_CHAT, //私聊
    MSG_SEND_MESSAGE1, //私聊消息
    MSG_CREATE_GROUP, //建群
    MSG_GROUP_REQUEST, //加入群组申请
    MSG_FIND, //找回密码
    MSG_LIST_GROUP, //群组列表
    MSG_MEMBER_ROLE, //获取群内身份
    MSG_GROUP_MEMBER, //群成员列表
    MSG_QUIT_GROUP, //退出群组
    MSG_LIST_GROUP_REQUEST, //加群申请列表
    MSG_GROUP_RESPONSE, //处理加群申请
    MSG_BREAK_GROUP, //解散群组
    MSG_REMOVE_MEMBER, //移除成员
    MSG_ADD_MANAGER, //添加管理员
    MSG_REMOVE_MANAGER, //移除管理员
    MSG_GROUP_CHAT, //群聊
    MSG_SEND_MESSAGE2, //群聊消息
    MSG_SEND_FILE, //发送文件
    MSG_BLOCK, //屏蔽
    MSG_UNBLOCK, //取消屏蔽
    MSG_FRIEND_MENU, //好友菜单
    MSG_BLOCK_MEMBER, //群禁言
    MSG_UNBLOCK_MEMBER //解除禁言
}
