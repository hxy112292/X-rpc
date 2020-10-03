package consumer.transport;

import com.alibaba.fastjson.JSON;
import entity.XrpcRequest;
import entity.XrpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.Selector;

public class JsonTransport implements Transport{

    private static final Logger logger = LoggerFactory.getLogger(JsonTransport.class);

    private final String host;
    private final int port;

    public JsonTransport(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public XrpcResponse send(XrpcRequest request) {

        Socket socket = null;
        XrpcResponse response = null;
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            socket = new Socket(host, port);

            outputStream = socket.getOutputStream();
            outputStream.write(JSON.toJSONBytes(request));
            outputStream.flush();

            inputStream = socket.getInputStream();
            byte[] buffer = new byte[1024];
            inputStream.read(buffer);

            response = JSON.parseObject(buffer, XrpcResponse.class);
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
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
        return response;
    }
}
