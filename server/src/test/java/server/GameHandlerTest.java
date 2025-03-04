package server;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import testutil.DummyRequest;
import testutil.DummyResponse;
import testutil.FakeGameService;

public class GameHandlerTest {

    private GameHandler gameHandler;
    private Gson gson;

    @BeforeEach
    public void setUp() {
        gson = new Gson();
        FakeGameService.reset();
        gameHandler = new GameHandler(new FakeGameService());
    }

    @Test
    @DisplayName("Handle Create Game - Positive")
    public void testHandleCreateGamePositive() {
        DummyRequest req = new DummyRequest("{\"gameName\":\"Test Game\"}");
        req.setHeader("Authorization", "dummyAuthToken");
        DummyResponse res = new DummyResponse();

        Object result = gameHandler.handleCreateGame(req, res);
        assertEquals(200, res.status(), "Expected HTTP status 200 for game creation");
    }

    @Test
    @DisplayName("Handle List Games - Positive")
    public void testHandleListGamesPositive() {
        DummyRequest req = new DummyRequest("");
        req.setHeader("Authorization", "dummyAuthToken");
        DummyResponse res = new DummyResponse();

        Object result = gameHandler.handleListGames(req, res);
        assertEquals(200, res.status(), "Expected HTTP status 200 for listing games");
    }

    @Test
    @DisplayName("Handle Join Game - Positive")
    public void testHandleJoinGamePositive() {
        String joinGameJson = "{\"gameID\":123,\"playerColor\":\"WHITE\"}";
        DummyRequest req = new DummyRequest(joinGameJson);
        req.setHeader("Authorization", "dummyAuthToken");
        DummyResponse res = new DummyResponse();

        Object result = gameHandler.handleJoinGame(req, res);
        assertEquals(200, res.status(), "Expected HTTP status 200 for joining game");
    }

    @Test
    @DisplayName("Handle Join Game - Negative (Slot Already Taken)")
    public void testHandleJoinGameSlotTaken() {
        String joinGameJson = "{\"gameID\":123,\"playerColor\":\"WHITE\"}";
        // First join.
        DummyRequest req1 = new DummyRequest(joinGameJson);
        req1.setHeader("Authorization", "dummyAuthToken");
        DummyResponse res1 = new DummyResponse();
        gameHandler.handleJoinGame(req1, res1);
        assertEquals(200, res1.status(), "First join should succeed");

        // Second join should fail.
        DummyRequest req2 = new DummyRequest(joinGameJson);
        req2.setHeader("Authorization", "dummyAuthToken");
        DummyResponse res2 = new DummyResponse();
        gameHandler.handleJoinGame(req2, res2);
        assertEquals(403, res2.status(), "Expected HTTP status 403 for duplicate join to same slot");
    }
}
