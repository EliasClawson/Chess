package testutil;

import spark.Response;

public class DummyResponse extends Response {
    private int status;
    private String body;

    @Override
    public void status(int statusCode) {
        this.status = statusCode;
    }

    @Override
    public int status() {
        return this.status;
    }

    public void setBody(String body) {
        this.body = body;
    }

    @Override
    public String body() {
        return this.body;
    }

    @Override public String type() { throw new UnsupportedOperationException(); }
}
