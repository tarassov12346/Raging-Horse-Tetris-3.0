package com.app.game.tetris.controller;

import com.app.game.tetris.service.PlayGameService;
import com.app.game.tetris.serviceImpl.State;
import org.springframework.beans.factory.annotation.Autowired;
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

    private State state;

    @Autowired
    private SimpMessagingTemplate template;

    @MessageMapping("/start")
    public void gamePlayDown(){
        state=playGameService.initiateState("USERTEST");
        ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(() -> state=sendState(state), 0, 1000, TimeUnit.MILLISECONDS);
    }

    @MessageMapping({"/1"})
    public void gamePlayRotate(){
        state = playGameService.rotateState(state);
        state=sendState(state);
    }

    @MessageMapping({"/2"})
    public void gamePlayLeft(){
        state = playGameService.moveLeftState(state);
        state=sendState(state);
    }

    @MessageMapping({"/3"})
    public void gamePlayRight(){
        state = playGameService.moveRightState(state);
        state=sendState(state);
    }

    @MessageMapping({"/4"})
    public void gamePlayDropDown(){
        state = playGameService.dropDownState(state);
        state=sendState(state);
    }

    private State sendState(State state) {
        state = moveDownState(state);
        char[][] cells = state.getStage().drawTetraminoOnCells();
        State stateSend= state.buildState(state.getStage().setStage(cells), state.isRunning(), state.getGame());
        this.template.convertAndSend("/receive/stateObjects",stateSend);
        return  state;
    }

    private State moveDownState(State state){
        Optional<State> moveDownState = playGameService.moveDownState(state);
        if (moveDownState.isEmpty()) {
            Optional<State> newTetraminoState = playGameService.newTetraminoState(state);
            if (newTetraminoState.isEmpty()) {
                state=state.stop();
                return state;
            } else state = newTetraminoState.orElse(state);
        }
        state = moveDownState.orElse(state);
        return state;
    }
}
