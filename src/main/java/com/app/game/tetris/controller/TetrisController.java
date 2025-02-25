package com.app.game.tetris.controller;

import com.app.game.tetris.daoservice.DaoGameService;
import com.app.game.tetris.daoserviceImpl.DaoGame;
import com.app.game.tetris.model.Game;
import com.app.game.tetris.service.PlayGameService;
import com.app.game.tetris.serviceImpl.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Controller
public class TetrisController {

    @Autowired
    private PlayGameService playGameService;

    @Autowired
    private DaoGameService daoGameService;

    private State state;

    @Autowired
    private SimpMessagingTemplate template;

    @MessageMapping("/hello")
    public void hello() {
        state = playGameService.initiateState("admin");
        daoGameService.retrieveScores();
        sendGameToBeDisplayed(state.getGame());
        sendDaoGameToBeDisplayed(playGameService.createGame(daoGameService.getBestPlayer(), daoGameService.getBestScore()));
    }

    @MessageMapping("/{moveId}")
    public void gamePlayDown(@DestinationVariable String moveId) {
        switch (moveId) {
            case "start" -> {

                ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
                service.scheduleAtFixedRate(() -> state = sendStateToBeDisplayed(state), 0, 1000, TimeUnit.MILLISECONDS);
            }
            case "1" -> {
                state = playGameService.rotateState(state);
                state = sendStateToBeDisplayed(state);
            }
            case "2" -> {
                state = playGameService.moveLeftState(state);
                state = sendStateToBeDisplayed(state);
            }
            case "3" -> {
                state = playGameService.moveRightState(state);
                state = sendStateToBeDisplayed(state);
            }
            case "4" -> {
                state = playGameService.dropDownState(state);
                state = sendStateToBeDisplayed(state);
            }
        }
    }

    private State sendStateToBeDisplayed(State state) {
        state = createStateAfterMoveDown(state);
        char[][] cellsToBeDisplayed = state.getStage().drawTetraminoOnCells();
        State stateToBeSent = state.buildState(state.getStage().buildStage(cellsToBeDisplayed), state.isRunning(), state.getGame());
        this.template.convertAndSend("/receive/stateObjects", stateToBeSent);
        return state;
    }

    private void sendDaoGameToBeDisplayed(Game game) {
        this.template.convertAndSend("/receive/daoGameObjects", game);
    }

    private void sendGameToBeDisplayed(Game game) {
        this.template.convertAndSend("/receive/gameObjects", game);
    }

    private State createStateAfterMoveDown(State state) {
        Optional<State> moveDownState = playGameService.moveDownState(state);
        if (moveDownState.isEmpty()) {
            Optional<State> newTetraminoState = playGameService.newTetraminoState(state);
            if (newTetraminoState.isEmpty()) {
                state = state.stop();
                return state;
            } else state = newTetraminoState.orElse(state);
        }
        state = moveDownState.orElse(state);
        return state;
    }
}
