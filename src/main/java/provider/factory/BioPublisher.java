package provider.factory;

import constants.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import provider.XrpcService;
import provider.processor.ProcessorThread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author hxy
 */
public class BioPublisher extends AbstractPublisher{


    private static final Logger logger = LoggerFactory.getLogger(BioPublisher.class);

    private final ExecutorService executor;

    private final Map<String, XrpcService> services;

    private boolean shutdown = false;

    protected BioPublisher() {
        services = new HashMap<>(Constants.MAX_SERVICE_SIZE);

        executor = new ThreadPoolExecutor(Constants.CORE_POOL_SIZE, Constants.MAX_POOL_SIZE, Constants.KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(), Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
    }

    @Override
    public void publish(int port) {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(port);

            while (!shutdown) {
                Socket socket = serverSocket.accept();
                // 将新的连接请求加入线程池
                executor.execute(new ProcessorThread(socket, services));
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public void shutdown() {
        shutdown = true;
        executor.shutdown();
    }

    @Override
    public void register(String serviceName, XrpcService service) {
        services.put(serviceName, service);
    }
}
