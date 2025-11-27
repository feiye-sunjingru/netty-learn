package com.feiye.advance.chatroom.server.session;

import io.netty.channel.Channel;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class GroupSessionMemoryImpl implements GroupSession {
    private final Map<String, Group> groupMap = new ConcurrentHashMap<>();

    @Override
    public Group createGroup(String name, Set<String> members) {
        //前提假设：这些成员都存在
        Group group = new Group(name, members);
        // key不存在或value是空，则插入并返回null; 否则就是key存在且value不为null,不处理且返回值
        Group res = groupMap.putIfAbsent(name, group);
        //创建成功
        if (res == null) {
            return group;
        } else {
            //key存在且value不为null：创建失败
            return null;
        }
    }

    @Override
    public Group joinMember(String name, String member) {
        // 当组存在且对应的值不为 null 时，才会使用给定的函数计算新值：这里set不为null
        return groupMap.computeIfPresent(name, (key, value) -> {
            value.getMembers().add(member);
            return value;
        });
    }

    @Override
    public Group removeMember(String name, String member) {
        return groupMap.computeIfPresent(name, (key, value) -> {
            value.getMembers().remove(member);
            return value;
        });
    }

    @Override
    public Group removeGroup(String name) {
        return groupMap.remove(name);
    }

    @Override
    public Set<String> getMembers(String name) {
        return groupMap.getOrDefault(name, Group.EMPTY_GROUP).getMembers();
    }

    @Override //获取组内每个成员的channel
    public List<Channel> getMembersChannel(String name) {
        return getMembers(name).stream()
                .map(member -> BaseSessionFactory.getSession().getChannel(member))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
