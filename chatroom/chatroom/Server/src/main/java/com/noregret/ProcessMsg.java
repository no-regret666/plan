package com.noregret;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.noregret.Mapper.*;
import com.noregret.Pojo.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class ProcessMsg {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RequestMapper requestMapper;
    @Autowired
    private FriendMapper friendMapper;
    @Autowired
    private MessageMapper1 messageMapper1;
    @Autowired
    private GroupMapper groupMapper;
    @Autowired
    private MessageMapper2 messageMapper2;

    private static HashMap<String, ChannelHandlerContext> online1 = new HashMap<>(); //储存当前在线用户
    private static HashMap<ChannelHandlerContext, String> online2 = new HashMap<>();
    private static HashMap<String, String> privateChatting = new HashMap<>(); //记录私聊
    private static HashMap<String, String> groupChatting = new HashMap<>(); //记录群聊

    public static ChannelHandlerContext channel;

    public void init(ChannelHandlerContext ctx) {
        channel = ctx;
    }

    public void send(ObjectNode node, ChannelHandlerContext ctx) {
        byte[] bytes = node.toString().getBytes();
        int length = bytes.length;
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer();
        buf.writeInt(length);
        buf.writeBytes(bytes);
        ctx.writeAndFlush(buf);
    }

    public void sendResponse(String response) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode msg = mapper.readTree(response);
        String type = msg.get("type").asText();
        if (String.valueOf(MsgType.MSG_LOGIN).equals(type)) {
            String username = msg.get("username").asText();
            String password = msg.get("password").asText();
            User user = userMapper.getUser(username);
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_LOGIN));
            if (user == null) {
                node.put("code", 300); //用户不存在
            } else {
                if (password.equals(user.getPassword())) {
                    if (!isExist(username)) {
                        node.put("code", 100); //登录成功
                        online1.put(username, channel);
                        online2.put(channel, username);
                    } else {
                        node.put("code", 400); //不能重复登录
                    }
                } else {
                    node.put("code", 200); //密码错误
                }
            }
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_REGISTER).equals(type)) {
            String username = msg.get("username").asText();
            String password = msg.get("password").asText();
            User user = userMapper.getUser(username);
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_REGISTER));
            if (user == null) {
                userMapper.insertUser(username, password);
                node.put("code", 100); //注册成功
            } else {
                node.put("code", 200); //用户名已存在
            }
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_FIND).equals(type)) {
            String username = msg.get("username").asText();
            String password = msg.get("password").asText();
            userMapper.updatePassword(username, password);
        } else if (String.valueOf(MsgType.MSG_FRIEND_REQUEST).equals(type)) {
            String username = msg.get("username").asText();
            String friendName = msg.get("friendName").asText();
            User user = userMapper.getUser(friendName);
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_FRIEND_REQUEST));
            if (user == null) {
                node.put("code", 200); //不存在该用户
            } else {
                if (friendMapper.selectFriend2(username, friendName) == null) {
                    node.put("code", 100); //发送好友申请
                    requestMapper.insertRequest(username, friendName, 1);
                } else {
                    node.put("code", 300); //已添加该好友
                }
                if (isExist(friendName)) {
                    ChannelHandlerContext ctx = online1.get(friendName);
                    Request request = new Request(0, username, friendName, 1);
                    ctx.writeAndFlush(request);
                }
            }
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_HOME).equals(type)) {
            String username = msg.get("username").asText();
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_HOME));

            List<String> friends2 = messageMapper1.getFriends(username);
            String friends = mapper.writeValueAsString(friends2);
            node.put("friends", friends);

            List<String> fromUsers2 = requestMapper.selectRequest1(username);
            String fromUsers = mapper.writeValueAsString(fromUsers2);
            node.put("fromUsers", fromUsers);

            List<Request> requests2 = requestMapper.selectRequest2(username);
            String requests = mapper.writeValueAsString(requests2);
            node.put("requests", requests);

            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_LIST_FRIEND_REQUEST).equals(type)) {
            String username = msg.get("username").asText();
            List<String> fromUsers1 = requestMapper.selectRequest1(username);
            String fromUsers = mapper.writeValueAsString(fromUsers1);
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_LIST_FRIEND_REQUEST));
            node.put("fromUsers", fromUsers);
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_FRIEND_RESPONSE).equals(type)) {
            String fromUser = msg.get("fromUser").asText();
            String toUser = msg.get("toUser").asText();
            int code = msg.get("code").asInt();
            if (code == 0) {
                friendMapper.insertFriendship(fromUser, toUser);
                friendMapper.insertFriendship(toUser, fromUser);
            }
            requestMapper.deleteRequest(fromUser, toUser, 1);
        } else if (String.valueOf(MsgType.MSG_LIST_FRIEND).equals(type)) {
            String username = msg.get("username").asText();
            List<String> friends = friendMapper.selectFriend(username);
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_LIST_FRIEND));
            List<theFriend> online = new ArrayList<>();
            for (String friend : friends) {
                if (online1.containsKey(friend)) {
                    online.add(new theFriend(friend, 1));//在线
                } else {
                    online.add(new theFriend(friend, 0)); //不在线
                }
            }
            String online1 = mapper.writeValueAsString(online);
            node.put("online", online1);
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_DELETE_FRIEND).equals(type)) {
            String username = msg.get("username").asText();
            String friendName = msg.get("friendName").asText();
            friendMapper.deleteFriend(username, friendName);
            friendMapper.deleteFriend(friendName, username);
        } else if (String.valueOf(MsgType.MSG_OFFLINE).equals(type)) {
            String username = msg.get("username").asText();
            ChannelHandlerContext ctx = online1.get(username);
            online1.remove(username);
            online2.remove(ctx);
        } else if (String.valueOf(MsgType.MSG_PRIVATE_CHAT).equals(type)) {
            String username = msg.get("username").asText();
            String friendName = msg.get("friendName").asText();
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_PRIVATE_CHAT));
            if (friendMapper.selectFriend2(username, friendName) == null) {
                node.put("code", 100); //不是好友
            } else {
                node.put("code", 200);
                privateChatting.put(username, friendName);
                List<Message> messages2 = messageMapper1.privateChat(username, friendName);
                String messages = mapper.writeValueAsString(messages2);
                node.put("messages", messages);
                messageMapper1.update();
            }
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_SEND_MESSAGE1).equals(type)) {
            String fromUser = msg.get("fromUser").asText();
            String toUser = msg.get("toUser").asText();
            String content = msg.get("content").asText();
            String time = msg.get("time").asText();
            Timestamp time2 = Timestamp.valueOf(time);
            ObjectNode node = mapper.createObjectNode();
            node.put("content", content);
            node.put("time", time);
            node.put("type", String.valueOf(MsgType.MSG_SEND_MESSAGE1));
            node.put("fromUser", fromUser);
            if ("q".equals(content)) {
                privateChatting.remove(fromUser);
                return;
            }
            if (!privateChatting.containsKey(toUser)) {
                messageMapper1.insert(fromUser, toUser, content, time2, "unread");
            } else {
                if (privateChatting.get(toUser).equals(fromUser)) {
                    ChannelHandlerContext ctx = online1.get(toUser);
                    send(node, ctx);
                    messageMapper1.insert(fromUser, toUser, content, time2, "read");
                } else {
                    messageMapper1.insert(fromUser, toUser, content, time2, "unread");
                }
            }
        }else if(String.valueOf(MsgType.MSG_SEND_FILE).equals(type)){
            String from = msg.get("from").asText();
            String to = msg.get("to").asText();
            String file = msg.get("file").asText();
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_SEND_FILE));
            node.put("from",from);
            node.put("file",file);
            if(isExist(to)){
                ChannelHandlerContext ctx = online1.get(to);
                send(node, ctx);
            }
        }
        else if (String.valueOf(MsgType.MSG_CREATE_GROUP).equals(type)) {
            String groupName = msg.get("groupName").asText();
            String member = msg.get("member").asText();

            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_CREATE_GROUP));
            if (groupMapper.getGroup(groupName) != null) {
                node.put("code", 100); //名称已被占用
            } else {
                node.put("code", 200); //建群成功
                groupMapper.insert(groupName, member, 1);
            }
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_GROUP_REQUEST).equals(type)) {
            String to = msg.get("groupName").asText();
            String from = msg.get("member").asText();
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_GROUP_REQUEST));
            if (groupMapper.getGroup(to) == null) {
                node.put("code", 100); //群组不存在
            } else {
                requestMapper.insertRequest(from, to, 2);
                node.put("code", 200); //已发送加群申请
                List<String> managers = groupMapper.getManagers(to);
                for (String manager : managers) {
                    if (isExist(manager)) {
                        ChannelHandlerContext ctx = online1.get(manager);
                        Request request = new Request(0, from, to, 2);
                        ctx.writeAndFlush(request);
                    }
                }
            }
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_LIST_GROUP_REQUEST).equals(type)) {
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_LIST_GROUP_REQUEST));
            String username = msg.get("username").asText();
            List<Request> requests2 = requestMapper.selectRequest2(username);
            String requests = mapper.writeValueAsString(requests2);
            node.put("requests", requests);
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_GROUP_RESPONSE).equals(type)) {
            String groupName = msg.get("groupName").asText();
            String from = msg.get("fromUser").asText();
            int code = msg.get("code").asInt();
            if (code == 0) {
                groupMapper.insert(groupName, from, 3);
            }
            requestMapper.deleteRequest(from, groupName, 2);
        } else if (String.valueOf(MsgType.MSG_LIST_GROUP).equals(type)) {
            String username = msg.get("username").asText();
            List<String> groups2 = groupMapper.getGroups(username);
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_LIST_GROUP));
            String groups = mapper.writeValueAsString(groups2);
            node.put("groups", groups);
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_GROUP_MEMBER).equals(type)) {
            String groupName = msg.get("groupName").asText();
            List<Member> members = groupMapper.getMembers(groupName);
            ObjectNode node = mapper.createObjectNode();
            String member2 = mapper.writeValueAsString(members);
            node.put("members", member2);
            node.put("type", String.valueOf(MsgType.MSG_GROUP_MEMBER));
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_QUIT_GROUP).equals(type)) {
            String username = msg.get("username").asText();
            String groupName = msg.get("groupName").asText();
            groupMapper.deleteMember(groupName, username);
        } else if (String.valueOf(MsgType.MSG_BREAK_GROUP).equals(type)) {
            String groupName = msg.get("groupName").asText();
            groupMapper.deleteGroup(groupName);
            requestMapper.breakGroup(groupName);
            messageMapper2.breakGroup(groupName);
        } else if (String.valueOf(MsgType.MSG_REMOVE_MEMBER).equals(type)) {
            String groupName = msg.get("groupName").asText();
            String member = msg.get("member").asText();
            groupMapper.deleteMember(groupName, member);
        } else if (String.valueOf(MsgType.MSG_ADD_MANAGER).equals(type)) {
            String groupName = msg.get("groupName").asText();
            String member = msg.get("member").asText();
            groupMapper.modifyManager(groupName, member, 2);
        } else if (String.valueOf(MsgType.MSG_REMOVE_MANAGER).equals(type)) {
            String groupName = msg.get("groupName").asText();
            String member = msg.get("member").asText();
            groupMapper.modifyManager(groupName, member, 3);
        } else if (String.valueOf(MsgType.MSG_GROUP_CHAT).equals(type)) {
            String groupName = msg.get("groupName").asText();
            String member = msg.get("member").asText();
            groupChatting.put(member, groupName);
            ObjectNode node = mapper.createObjectNode();
            node.put("type", String.valueOf(MsgType.MSG_GROUP_CHAT));
            List<Message> messages2 = messageMapper2.groupChat(groupName);
            String messages = mapper.writeValueAsString(messages2);
            node.put("messages", messages);
            send(node, channel);
        } else if (String.valueOf(MsgType.MSG_SEND_MESSAGE2).equals(type)) {
            String from = msg.get("from").asText();
            String to = msg.get("to").asText();
            String content = msg.get("content").asText();
            String time2 = msg.get("time").asText();
            Timestamp time = Timestamp.valueOf(time2);
            ObjectNode node = mapper.createObjectNode();
            node.put("from", from);
            node.put("to", to);
            node.put("content", content);
            node.put("time", time.toString());
            node.put("type", String.valueOf(MsgType.MSG_SEND_MESSAGE2));
            if ("q".equals(content)) {
                groupChatting.remove(from);
                return;
            }

            messageMapper2.insert(from, to, content, time, "read");

            //遍历群友并转发
            List<String> members = groupMapper.getMemberNames(to);
            if (!members.isEmpty()) {
                for (String member : members) {
                    if(member.equals(from)){
                        break;
                    }
                    if (groupChatting.containsKey(member)) {
                        if (groupChatting.get(member).equals(to)) {
                            ChannelHandlerContext ctx = online1.get(member);
                            send(node, ctx);
                        }
                    }
                }
            }
        }
    }

    //判断该用户是否在线
    public static boolean isExist(ChannelHandlerContext ctx) {
        return online2.containsKey(ctx);
    }

    public static boolean isExist(String username) {
        return online1.containsKey(username);
    }

    public static void remove(ChannelHandlerContext ctx) {
        String username = online2.get(ctx);
        online1.remove(username);
        online2.remove(ctx);
        privateChatting.remove(username);
        groupChatting.remove(username);
    }

}
