package entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class XrpcRequest {

    private String className;

    private String methodName;

    private Object[] args;

    private Class<?>[] types;
}
