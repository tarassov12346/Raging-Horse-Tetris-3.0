package com.example.demoWebsocket.serviceImpl;

import com.example.demoWebsocket.model.Game;
import com.example.demoWebsocket.model.Tetramino;
import com.example.demoWebsocket.service.GameLogic;
import com.example.demoWebsocket.service.PlayGameService;
import com.example.demoWebsocket.service.StageService;
import com.example.demoWebsocket.service.StateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.IntStream;

@Service
public class PlayGame implements PlayGameService {

    @Autowired
    private Game game;

    @Autowired
    private StageService stage;

    @Autowired
    private StateService state;

    @Autowired
    private Tetramino tetramino;


    @Override
    public Game createGame(String playerName) {
        return game.buildGame(playerName, 0);
    }

    @Override
    public State initiateState(String playerName) {
        Stage emptyStage = stage.buildStage(makeEmptyMatrix(), getTetramino0(), 0, 0, 0);
        State initialState = state.buildState(emptyStage, false, createGame(playerName));
        return initialState.start().createStateWithNewTetramino().orElse(initialState);
    }

    @Override
    public State dropDownState(State state) {return state.dropDown().orElse(state);
    }

    @Override
    public State moveRightState(State state) {
        return state.moveRight().orElse(state);
    }

    @Override
    public State moveLeftState(State state) {
        return state.moveLeft().orElse(state);
    }

    @Override
    public State rotateState(State state) {
        return state.rotate().orElse(state);
    }

    @Override
    public Optional<State> moveDownState(State state) {return state.moveDown(state.getStepDown());}

    @Override
    public Optional<State> newTetraminoState(State state) {
        return state.createStateWithNewTetramino();
    }

    private char[][] makeEmptyMatrix() {
        final char[][] c = new char[GameLogic.HEIGHT][GameLogic.WIDTH];
        IntStream.range(0, GameLogic.HEIGHT).forEach(y -> IntStream.range(0, GameLogic.WIDTH).forEach(x -> c[y][x] = '0'));
        return c;
    }

    private Tetramino getTetramino0() {
        return tetramino.buildTetramino(new char[][]{{'0'}});
    }
}
