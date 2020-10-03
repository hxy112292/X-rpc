package consumer.proxy;

import consumer.transport.Transport;

import java.lang.reflect.Proxy;

public class ConsumerProxyFactory {

    /**
     * 远程调用的代理工厂，生成代理类
     *
     * @param clazz     代理的接口类
     * @param transport 网络调用的运输机
     * @param <T>       类泛型
     * @return 返回代理类
     */
    @SuppressWarnings("unchecked")
    public static <T> T createProxy(final Class<T> clazz, Transport transport) {
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),
                new Class<?>[]{clazz}, new ConsumerInvocationHandler(transport));
    }
}
