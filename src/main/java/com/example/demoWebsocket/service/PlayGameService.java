package com.example.demoWebsocket.service;

import com.example.demoWebsocket.model.Game;
import com.example.demoWebsocket.serviceImpl.State;

import java.util.Optional;

public interface PlayGameService {
    Game createGame(String playerName);
    State initiateState(String playerName);
    State dropDownState(State state);
    State moveRightState(State state);
    State moveLeftState(State state);
    State rotateState(State state);
    Optional<State> moveDownState(State state);
    Optional<State> newTetraminoState(State state);
}
