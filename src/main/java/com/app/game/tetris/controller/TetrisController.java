package com.app.game.tetris.controller;

import com.app.game.tetris.daoservice.DaoGameService;
import com.app.game.tetris.daoservice.DaoUserService;
import com.app.game.tetris.model.Game;
import com.app.game.tetris.model.Roles;
import com.app.game.tetris.model.User;
import com.app.game.tetris.service.PlayGameService;
import com.app.game.tetris.serviceImpl.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Controller
public class TetrisController {

    @Autowired
    private PlayGameService playGameService;

    @Autowired
    private DaoGameService daoGameService;

    private State state;

    private ScheduledExecutorService service;

    @Autowired
    private SimpMessagingTemplate template;


    @Autowired
    private DaoUserService daoUserService;


    @MessageMapping("/register")
    public void register(User user) {
        User newUser = new User();

        if (user.getPassword().equals(user.getPasswordConfirm())) {
            newUser.setUsername(user.getUsername());
            newUser.setPassword(user.getPassword());
            newUser.setPasswordConfirm(user.getPasswordConfirm());
        } else {
            this.template.convertAndSend("/receive/message", "The password is not confirmed!");
        }
        if (!daoUserService.saveUser(newUser)) {
            this.template.convertAndSend("/receive/message", "This user already exists!");
        } else {
            this.template.convertAndSend("/receive/message", "The user " + newUser.getUsername() + " has been successfully registered!");
        }
    }

    @MessageMapping("/hello")
    public void hello(Principal principal) {
        state = playGameService.initiateState(principal.getName());
        daoGameService.retrieveScores();
        sendGameToBeDisplayed(state.getGame());
        sendDaoGameToBeDisplayed(playGameService.createGame(daoGameService.getBestPlayer(), daoGameService.getBestScore()));
    }

    @MessageMapping("/admin")
    public void admin() {
        List<User> allUsersList = daoUserService.getAllUsers();
        allUsersList.forEach(user -> this.template.convertAndSend("/receive/users",
                new User(user.getId(), user.getUsername(), user.getPassword(),
                        String.join(";", user.getRoles().stream().map(Roles::getName).collect(Collectors.toList())),
                        user.getRoles())));
        getAllBestResults(daoGameService.getAllGames()).
                forEach(game -> this.template.convertAndSend("/receive/results", game));
    }


    @MessageMapping("/admin/{userId}")
    public void deleteUser(@DestinationVariable Long userId) {
        if (daoUserService.findUserById(userId).getUsername().equals(state.getGame().getPlayerName())) {
            this.template.convertAndSend("/receive/alert", "You cannot delete yourself!");
            return;
        }
        for (Roles role : daoUserService.findUserByUserName(state.getGame().getPlayerName()).getRoles()) {
            if (role.getName().equals("ROLE_ADMIN")) {
                daoGameService.deleteByName(daoUserService.findUserById(userId).getUsername());
                daoUserService.deleteUser(userId);
                admin();
                return;
            }
            this.template.convertAndSend("/receive/alert", "You are not admin!");
        }
    }

    @MessageMapping("/{moveId}")
    public void gamePlayDown(@DestinationVariable String moveId) {
        switch (moveId) {
            case "start" -> {
                sendDaoGameToBeDisplayed(playGameService.createGame(daoGameService.getBestPlayer(), daoGameService.getBestScore()));
                service = Executors.newScheduledThreadPool(1);
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
                daoGameService.recordScore(state.getGame());
                service.shutdown();
                return state;
            } else state = newTetraminoState.orElse(state);
        }
        state = moveDownState.orElse(state);
        return state;
    }

    private Set<Game> getAllBestResults(List<Game> playersList) {
        Set<Game> highestScoringPlayers = new HashSet<>();
        playersList.sort(Comparator.comparingInt(Game::getPlayerScore).reversed());
        highestScoringPlayers.addAll(playersList);
        return highestScoringPlayers;
    }
}
