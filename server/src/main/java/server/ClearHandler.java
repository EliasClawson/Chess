package server;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;
import service.ClearService;

// Not sure if I need this. It just goes through and clears everything
public class ClearHandler {
    private final ClearService clearService;
    private final Gson gson = new Gson();

    // Constructor for the clearerinator
    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }

    // Actually do the clearing
    public Object handleRequest(Request req, Response res) {
        try {
            clearService.clear();  // This will clear all DAOs
            res.status(200);
            return gson.toJson(new Object()); // Returns blank Json "{}"
        } catch (Exception e) {
            res.status(500);
            return gson.toJson(new ErrorResponse("Error: " + e.getMessage())); // Just supposed to catch clearing errors
        }
    }
}
