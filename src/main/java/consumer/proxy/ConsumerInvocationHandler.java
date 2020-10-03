package consumer.proxy;

import consumer.transport.Transport;
import entity.XrpcRequest;
import entity.XrpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author hxy
 */
public class ConsumerInvocationHandler implements InvocationHandler {

    private static final Logger logger = LoggerFactory.getLogger(ConsumerInvocationHandler.class);

    private final Transport transport;

    public ConsumerInvocationHandler(Transport transport) {
        this.transport = transport;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 封装请求
        XrpcRequest request = new XrpcRequest();
        request.setMethodName(method.getName());
        request.setClassName(method.getDeclaringClass().getName());
        request.setArgs(args);
        request.setTypes(method.getParameterTypes());

        logger.debug(request.toString());
        // 远程代用
        XrpcResponse response = transport.send(request);
        logger.debug(response.toString());

        return response.getBody();
    }
}
