package server;

// I'm not sure if I need this, but it definitely helps with debugging
public class ErrorResponse {
    public final String message;

    public ErrorResponse(String message) {
        this.message = message;
    }
}