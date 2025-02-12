package server;

import java.util.List;

public record ListGamesResponse(List<GameInfo> games) { }
