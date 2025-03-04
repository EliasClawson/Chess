package testutil;

import service.GameService;
import java.util.Collections;
import java.util.List;
import model.GameData;
import chess.ChessGame;

public class FakeGameService extends GameService {
    private static boolean whiteJoined = false;

    public FakeGameService() {
        super(null, null);
    }

    @Override
    public int createGame(String authToken, String gameName) {
        if(!"dummyAuthToken".equals(authToken)) {
            throw new IllegalArgumentException("Invalid auth token.");
        }
        return 123; // dummy game ID
    }

    @Override
    public List<GameData> listGames(String authToken) {
        if(!"dummyAuthToken".equals(authToken)) {
            throw new IllegalArgumentException("Invalid auth token.");
        }
        GameData dummyGame = new GameData(123, "dummyWhite", null, "Test Game", new ChessGame());
        return Collections.singletonList(dummyGame);
    }

    @Override
    public void joinGame(String authToken, int gameID, boolean joinAsWhite) {
        if(!"dummyAuthToken".equals(authToken)) {
            throw new IllegalArgumentException("Invalid auth token.");
        }
        if(gameID != 123) {
            throw new IllegalArgumentException("Game not found.");
        }
        if(joinAsWhite) {
            if(whiteJoined) {
                throw new IllegalArgumentException("White slot already taken.");
            }
            whiteJoined = true;
        }
    }

    public static void reset() {
        whiteJoined = false;
    }
}
