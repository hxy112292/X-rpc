package provider.factory;

import com.alibaba.fastjson.JSON;
import constants.Constants;
import entity.XrpcRequest;
import entity.XrpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import provider.XrpcService;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author hxy
 */
public class NioPublisher extends AbstractPublisher{

    private static final Logger logger = LoggerFactory.getLogger(NioPublisher.class);

    private boolean shutdown = false;

    private final Map<String, XrpcService> services;

    private Selector selector;

    public NioPublisher() {
        this.services = new HashMap<>(10);
    }

    private void acceptHandler(SelectionKey key) throws IOException {
        ServerSocketChannel channel = (ServerSocketChannel) key.channel();
        SocketChannel client = channel.accept();
        client.configureBlocking(false);

        ByteBuffer buffer = ByteBuffer.allocate(Constants.NIO_BUFFER_SIZE);
        client.register(selector, SelectionKey.OP_READ, buffer);

        logger.debug("--------------------------------------");
        logger.debug("new client: " + client.getRemoteAddress());
        logger.debug("--------------------------------------");
    }

    private void readHandler(SelectionKey key) throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        // 开始读取
        int read = 0;
        while ((read = channel.read(buffer)) > 0) {
            buffer.flip();
            byte[] data = new byte[buffer.limit()];
            buffer.get(data);
            buffer.clear();

            XrpcRequest request = JSON.parseObject(data, XrpcRequest.class);
            logger.debug(request.toString());

            // 反射调用
            Class<?> clazz = Class.forName(request.getClassName());
            Method method = clazz.getMethod(request.getMethodName(), request.getTypes());

            // 通过名称查找已注册的服务方法
            XrpcService service = services.get(request.getClassName());
            Object body = method.invoke(service, request.getArgs());

            XrpcResponse response = new XrpcResponse();
            response.setBody(body);
            response.setCode(200);
            logger.debug(response.toString());

            buffer.put(JSON.toJSONBytes(response));
            buffer.flip();
            while (buffer.hasRemaining()) {
                channel.write(buffer);
            }
            buffer.clear();
        }
        if (read == -1) {
            logger.debug("client disconnected: " + channel.getRemoteAddress());
            channel.close();
        }
    }

    @Override
    public void publish(int port) {
        try {
            // 打开channel开始监听
            ServerSocketChannel channel = ServerSocketChannel.open();
            channel.bind(new InetSocketAddress("localhost", port));
            // 设置非阻塞
            channel.configureBlocking(false);

            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_ACCEPT);

            while (!shutdown) {
                while (selector.select(Constants.NIO_SEL_TIMEOUT) > 0) {
                    //查询存在的活跃的key
                    Set<SelectionKey> keys = selector.selectedKeys();
                    //迭代所有活跃的key，进行操作
                    Iterator<SelectionKey> iterator = keys.iterator();
                    while (iterator.hasNext()) {
                        //拿到某个key后,就将其从迭代器里除去
                        SelectionKey key = iterator.next();
                        iterator.remove();

                        if (key.isAcceptable()) {
                            acceptHandler(key);
                        } else if (key.isReadable()) {
                            readHandler(key);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void shutdown() {
        shutdown = true;
        try {
            selector.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void register(String serviceName, XrpcService service) {
        services.put(serviceName, service);
    }
}
