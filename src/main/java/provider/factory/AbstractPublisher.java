package provider.factory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import provider.XrpcService;

/**
 * @author hxy
 */
public abstract class AbstractPublisher {

    private static final Logger logger = LoggerFactory.getLogger(AbstractPublisher.class);

    /**
     * 发布服务
     *
     * @param port 端口号
     */
    public abstract void publish(int port);

    /**
     * 停止服务发布
     */
    public abstract void shutdown();

    /**
     * 发布的服务，注册
     *
     * @param serviceName 服务名称
     * @param service     服务实现
     */
    public abstract void register(String serviceName, XrpcService service);

    /**
     * 服务启动
     *
     * @param port 端口号
     */
    public final void start(int port) {
        logger.info("Server is listening... port: " + port);
        // shutdown hook，接到关闭信号或者程序执行完成时
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Server is shutting down...");
            shutdown();
        }));
        publish(port);
    }
}
