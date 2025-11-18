package com.feiye.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static com.feiye.nio.channelBuffer.ByteBufferUtil.debugAll;

@Slf4j
public class SelectorServer {

    public static void main(String[] args) throws IOException {
        // 1.创建Selector去管理多个 channel
        Selector selector = Selector.open();

        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8080));

        // 2.将ServerSocketChannel与Selector建立联系(注册)：因为要用Selector监听ServerSocketChannel的连接事件
        // SelectionKey：将来发生某个事件后，可以通过registerKey知道 事件 和 发生事件的channel：0表示不关注任何事件
        SelectionKey sscKey = ssc.register(selector, 0, null);
        log.debug("registerKey: {}", sscKey);

        // 让注册key关注accept->ServerSocketChannel的连接请求事件：配置感兴趣的事件，对于ssc感兴趣的是accept事件
        sscKey.interestOps(SelectionKey.OP_ACCEPT);

        while (true) {
            // 3.select方法：当没有事件发生时会阻塞，一旦有任何事件触发，都会恢复运行；事件未处理时不阻塞
            selector.select();

            // 4.处理事件：拿到selector管理的所有事件 → 运行到这里时说明，上面的代码恢复运行，也就是说有事件触发
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();

            //在集合遍历时删除得采用迭代器删除
            while (iterator.hasNext()) {
                //事件来了：要么取消，要么处理。如果不处理会陷入循环
                SelectionKey key = iterator.next();
                iterator.remove();
                //key.cancel()认为不再处理这个事件，事件取消
                log.debug("key: {}", key);

                // 5.区分事件类型
                // ssc触发的事件
                if (key.isAcceptable()) {
                    SelectableChannel channel = key.channel();
                    if (channel instanceof ServerSocketChannel) {
                        SocketChannel sc = ((ServerSocketChannel) channel).accept();
                        log.debug("accept channel: {}", sc);
                        // 由于事件没有被删除，所以当调用连接事件获取新的连接时如果没有新的连接accept方法会返回null，所以不删除事件，会报空指针
                        sc.configureBlocking(false);
                        //作为一个sc的attachment关联到scKey
                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        SelectionKey scKey = sc.register(selector, 0, buffer);
                        scKey.interestOps(SelectionKey.OP_READ);
                    }
                } else if (key.isReadable()) {
                    //客户端关闭会引发read事件，所以这里要处理客户端关闭
                    SelectableChannel channel = key.channel();
                    if (channel instanceof SocketChannel) {
                        try {
                            //客户端发送的数据如果长度多于buffer会read多次读取
                            log.debug("read channel: {}", channel);
                            //取上次关联的附件
                            ByteBuffer buffer = (ByteBuffer) key.attachment();
                            int read = ((SocketChannel) channel).read(buffer);
                            // 如果客户端正常断开了，read的读取长度为-1，此时要让selector取消关注这个key
                            if (read == -1) {
                                // 正常断开，如果不取消那么会一直read空
                                key.cancel();
                            } else {
                                System.out.println(StandardCharsets.UTF_8.decode( buffer));
                                split(buffer);
                                // 说明buffer满了没有被压缩掉
                                if(buffer.position() == buffer.limit()){
                                    ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                                    buffer.flip();
                                    newBuffer.put(buffer);
                                    key.attach(newBuffer);
                                }
                            }
                        } catch (IOException e) {
                            // 如果客户端强制断开，如果不取消会导致一直read
                            key.cancel();
                            e.printStackTrace();
                        }
                    }
                }
                // 6处理完当前事件后要将事件从selectedKeys删除，因为selector不会主动去删除事件: 如果不处理会一直循环下去
//                iterator.remove();
            }
        }
    }

    private static void split(ByteBuffer source) {
        source.flip();

        for (int i = 0; i < source.limit(); i++) {
            //找到一条完整消息
            if (source.get(i) == '\n') {
                int length = i + 1 - source.position();
                //完整的消息存入新的byteBuffer
                ByteBuffer target = ByteBuffer.allocate(length);
                for (int j = 0; j < length; j++) {
                    //source读，target写
                    target.put(source.get());
                }
                debugAll(target);
            }
        }

        // 没读完的放在下次再继续读
        source.compact();
    }
}
