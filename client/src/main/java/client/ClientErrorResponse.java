package client;

// I'm not sure if I need this, but it definitely helps with debugging
public class ClientErrorResponse {
    public final String message;

    public ClientErrorResponse(String message) {
        this.message = message;
    }
}