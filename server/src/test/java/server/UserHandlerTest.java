package server;

import com.google.gson.Gson;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import testutil.DummyRequest;
import testutil.DummyResponse;
import testutil.FakeUserService;

public class UserHandlerTest {

    private UserHandler userHandler;
    private Gson gson;
    private FakeUserService fakeUserService;

    @BeforeEach
    public void setUp() {
        gson = new Gson();
        fakeUserService = new FakeUserService();
        userHandler = new UserHandler(fakeUserService);
    }

    @Test
    @DisplayName("Handle Register - Positive")
    public void testHandleRegisterPositive() {
        String registrationJson = "{\"username\":\"newUser\",\"password\":\"newPass\",\"email\":\"new@mail.com\"}";
        DummyRequest req = new DummyRequest(registrationJson);
        DummyResponse res = new DummyResponse();

        Object result = userHandler.handleRegister(req, res);
        assertEquals(200, res.status(), "Expected HTTP status 200 on registration");
    }

    @Test
    @DisplayName("Handle Login - Positive")
    public void testHandleLoginPositive() {
        // FakeUserService expects username "validUser" with password "correctPass"
        String loginJson = "{\"username\":\"validUser\",\"password\":\"correctPass\"}";
        DummyRequest req = new DummyRequest(loginJson);
        DummyResponse res = new DummyResponse();

        Object result = userHandler.handleLogin(req, res);
        assertEquals(200, res.status(), "Expected HTTP status 200 on login");
    }

    @Test
    @DisplayName("Handle Logout - Positive")
    public void testHandleLogoutPositive() {
        DummyRequest req = new DummyRequest("");
        req.setHeader("Authorization", "dummyAuthToken");
        DummyResponse res = new DummyResponse();

        Object result = userHandler.handleLogout(req, res);
        assertEquals(200, res.status(), "Expected HTTP status 200 on logout");
    }
}
