package consumer.transport;

import entity.XrpcRequest;
import entity.XrpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author hxy
 */
public class SocketTransport implements Transport{

    private static final Logger logger = LoggerFactory.getLogger(SocketTransport.class);

    private final String host;

    private final int port;

    public SocketTransport(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public XrpcResponse send(XrpcRequest request) {

        Socket socket = null;
        ObjectOutputStream outputStream = null;
        ObjectInputStream inputStream = null;
        XrpcResponse response = null;
        try {
            socket = new Socket(host, port);
            outputStream = new ObjectOutputStream(socket.getOutputStream());
            outputStream.writeObject(request);
            outputStream.flush();
            inputStream = new ObjectInputStream(socket.getInputStream());
            response = (XrpcResponse) inputStream.readObject();
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
