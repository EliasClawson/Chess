package server;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import service.ClearService;

public class ClearHandler {
    private final ClearService clearService;
    private final Gson gson = new Gson();

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }

    public Object handleRequest(Request req, Response res) {
        try {
            clearService.clear();  // This will clear all DAOs
            res.status(200);
            return gson.toJson(new Object()); // Returns "{}"
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage()));
        }
    }
}
