package entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class XrpcResponse {

    private int code;

    private Object body;

    private String message;
}
