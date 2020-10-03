package consumer.transport;

import entity.XrpcRequest;
import entity.XrpcResponse;

public interface Transport {

    /**
     * 远程调用，发送请求
     *
     * @param request XrpcRequest
     * @return XrpcResponse
     */
    XrpcResponse send(XrpcRequest request);
}
