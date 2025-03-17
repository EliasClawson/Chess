package server;

import model.GameInfo;

import java.util.List;

public record ListGamesResponse(List<GameInfo> games) { }
