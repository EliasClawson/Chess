package testutil;

import service.UserService;

public class FakeUserService extends UserService {
    public FakeUserService() {
        // Pass null for DAOs since they won't be used in this fake.
        super(null, null);
    }

    @Override
    public String registerUser(String username, String password, String email) {
        if(username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                email == null || email.trim().isEmpty()){
            throw new IllegalArgumentException("Bad request: Missing fields.");
        }
        if(username.equals("duplicate")) {
            throw new IllegalArgumentException("Username already taken.");
        }
        return "dummyAuthToken";
    }

    @Override
    public String loginUser(String username, String password) {
        if("validUser".equals(username) && "correctPass".equals(password)) {
            return "dummyAuthToken";
        }
        throw new IllegalArgumentException("Invalid username or password.");
    }

    @Override
    public void logoutUser(String authToken) {
        if(!"dummyAuthToken".equals(authToken)) {
            throw new IllegalArgumentException("Invalid auth token.");
        }
    }
}
