package com.example.demoWebsocket.service;

import com.example.demoWebsocket.model.Game;
import com.example.demoWebsocket.serviceImpl.Stage;
import com.example.demoWebsocket.serviceImpl.State;

import java.util.Optional;

public interface StateService extends GameLogic<Optional<State>>{
    State buildState(Stage stage, boolean isRunning, Game game);
    State start();
    State stop();
    Optional<State> createStateWithNewTetramino();
    Optional<State> restartWithNewTetramino();
    Optional<State> dropDown();
    Stage getStage();
    int getStepDown();
    void setStage(Stage stage);
}
