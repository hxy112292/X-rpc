package provider.processor;

import entity.XrpcRequest;
import entity.XrpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import provider.XrpcService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Map;

/**
 * @author hxy
 */
public class ProcessorThread extends Thread{

    private static final Logger logger = LoggerFactory.getLogger(ProcessorThread.class);

    private final Socket socket;

    private final Map<String, XrpcService> services;

    public ProcessorThread(Socket socket, Map<String, XrpcService> services) {
        this.socket = socket;
        this.services = services;
    }

    @Override
    public void run() {

        ObjectInputStream inputStream = null;
        ObjectOutputStream outputStream = null;
        try {
            inputStream = new ObjectInputStream(socket.getInputStream());
            outputStream = new ObjectOutputStream(socket.getOutputStream());

            XrpcRequest request = (XrpcRequest) inputStream.readObject();
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

            outputStream.writeObject(response);
            outputStream.flush();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
                socket.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
}
