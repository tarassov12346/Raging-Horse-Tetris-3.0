package com.app.game.tetris.controller;

import com.app.game.tetris.daoservice.DaoGameService;
import com.app.game.tetris.daoservice.DaoMongoService;
import com.app.game.tetris.daoservice.DaoUserService;
import com.app.game.tetris.model.Game;
import com.app.game.tetris.model.Roles;
import com.app.game.tetris.model.SavedGame;
import com.app.game.tetris.model.User;
import com.app.game.tetris.service.PlayGameService;
import com.app.game.tetris.serviceImpl.State;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.OutputStream;
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

    @Autowired
    private DaoMongoService daoMongoService;


    @MessageMapping("/register")
    public void register(User user) {
        if (daoUserService.isRolesDBEmpty()) {
            daoUserService.prepareRolesDB();
            daoUserService.prepareUserDB();
        }
        User newUser = new User();
        if (!user.getUsername().matches(".*[a-zA-Z]+.*")) {
            this.template.convertAndSend("/receive/message", "The user name should contain at least one letter!");
            return;
        }
        if (!user.getPassword().matches(".*[a-zA-Z]+.*") || !user.getPassword().matches("(.)*(\\d)(.)*")) {
            this.template.convertAndSend("/receive/message", "The password should contain at least one letter and one digit!");
            return;
        }
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
        //  daoMongoService.runMongoServer();

        if (!daoMongoService.isImageFilePresentInMongoDB(state.getGame().getPlayerName())) {
            daoMongoService.prepareMongoDBForNewPLayer(state.getGame().getPlayerName());
        }

        sendGameToBeDisplayed(state.getGame());
        sendDaoGameToBeDisplayed(playGameService.createGame(daoGameService.getBestPlayer(), daoGameService.getBestScore()));
    }

    @MessageMapping("/profile")
    public void profile() {
        daoGameService.retrievePlayerScores(state.getGame());



        this.template.convertAndSend("/receive/playerStat",
                playGameService.createGame(state.getGame().getPlayerName(), daoGameService.getPlayerBestScore()));
        this.template.convertAndSend("/receive/playerAttemptsNumber",
                playGameService.createGame("DataTransferObject", daoGameService.getPlayerAttemptsNumber()));
    }

    @MessageMapping("/upload")
    public void test(String imageBase64Stringsep) {
        daoMongoService.cleanImageMongodb(state.getGame().getPlayerName(), "");
        daoMongoService.loadMugShotIntoMongodb(state.getGame().getPlayerName(), Base64.getDecoder().decode(imageBase64Stringsep));
    }

    @GetMapping({"/getPhoto"})
    public void getPhoto(HttpServletRequest request,
                         HttpServletResponse response) {
        byte[] imagenEnBytes = daoMongoService.loadByteArrayFromMongodb(state.getGame().getPlayerName(), "mugShot");
        response.setHeader("Accept-ranges", "bytes");
        response.setContentType("image/jpeg");
        response.setContentLength(imagenEnBytes.length);
        response.setHeader("Expires", "0");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Content-Description", "File Transfer");
        response.setHeader("Content-Transfer-Encoding:", "binary");
        try {
            OutputStream out = response.getOutputStream();
            out.write(imagenEnBytes);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping({"/getSnapShot"})
    public void getSnapShot(HttpServletRequest request,
                            HttpServletResponse response) {
        byte[] imagenEnBytes = daoMongoService.loadByteArrayFromMongodb(state.getGame().getPlayerName(), "deskTopSnapShot");
        response.setHeader("Accept-ranges", "bytes");
        response.setContentType("image/jpeg");
        response.setContentLength(imagenEnBytes.length);
        response.setHeader("Expires", "0");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Content-Description", "File Transfer");
        response.setHeader("Content-Transfer-Encoding:", "binary");
        try {
            OutputStream out = response.getOutputStream();
            out.write(imagenEnBytes);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping({"/getSnapShotBest"})
    public void getSnapShotBest(HttpServletRequest request,
                                HttpServletResponse response) {
        byte[] imagenEnBytes = daoMongoService.loadByteArrayFromMongodb(state.getGame().getPlayerName(), "deskTopSnapShotBest");
        response.setHeader("Accept-ranges", "bytes");
        response.setContentType("image/jpeg");
        response.setContentLength(imagenEnBytes.length);
        response.setHeader("Expires", "0");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Content-Description", "File Transfer");
        response.setHeader("Content-Transfer-Encoding:", "binary");
        try {
            OutputStream out = response.getOutputStream();
            out.write(imagenEnBytes);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                daoMongoService.cleanSavedGameMongodb(daoUserService.findUserById(userId).getUsername());
                daoMongoService.cleanImageMongodb(daoUserService.findUserById(userId).getUsername(), "");
                daoMongoService.cleanImageMongodb(daoUserService.findUserById(userId).getUsername(), "deskTopSnapShot");
                daoMongoService.cleanImageMongodb(daoUserService.findUserById(userId).getUsername(), "deskTopSnapShotBest");
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

    @MessageMapping("/save")
    public void gameSave() {
        service.shutdown();
        SavedGame savedGame = playGameService.saveGame(state.getGame(), state);
        if (daoMongoService.isSavedGamePresentInMongoDB(state.getGame().getPlayerName() + "SavedGame")) {
            daoMongoService.cleanSavedGameMongodb(state.getGame().getPlayerName());
        }
        daoMongoService.loadSavedGameIntoMongodb(savedGame, state.getGame());
        sendSavedStateToBeDisplayed(state);
    }

    @MessageMapping("/restart")
    public void gameRestart() {
        if (daoMongoService.isSavedGamePresentInMongoDB(state.getGame().getPlayerName() + "SavedGame")) {
            state = playGameService.recreateStateFromSavedGame(daoMongoService.loadSavedGameFromMongodb(state.getGame()));
            state = sendStateToBeDisplayed(state);
        }
    }

    @MessageMapping("/snapShot")
    public void makeSnapShot() {
        daoMongoService.makeDesktopSnapshot("deskTopSnapShot", state, daoGameService.getBestPlayer(), daoGameService.getBestScore());
        daoMongoService.cleanImageMongodb(state.getGame().getPlayerName(), "deskTopSnapShot");
        daoMongoService.loadSnapShotIntoMongodb(state.getGame().getPlayerName(), "deskTopSnapShot");
        if (state.getGame().getPlayerScore() >= daoGameService.getPlayerBestScore()) {
            daoMongoService.makeDesktopSnapshot("deskTopSnapShotBest", state, daoGameService.getBestPlayer(), daoGameService.getBestScore());
            daoMongoService.cleanImageMongodb(state.getGame().getPlayerName(), "deskTopSnapShotBest");
            daoMongoService.loadSnapShotIntoMongodb(state.getGame().getPlayerName(), "deskTopSnapShotBest");
        }
        sendFinalStateToBeDisplayed(state);
    }

    private State sendStateToBeDisplayed(State state) {
        state = createStateAfterMoveDown(state);
        char[][] cellsToBeDisplayed = state.getStage().drawTetraminoOnCells();
        State stateToBeSent = state.buildState(state.getStage().buildStage(cellsToBeDisplayed), state.isRunning(), state.getGame());
        this.template.convertAndSend("/receive/stateObjects", stateToBeSent);
        return state;
    }

    private State sendFinalStateToBeDisplayed(State state) {
        state = createStateAfterMoveDown(state);
        char[][] cellsToBeDisplayed = state.getStage().drawTetraminoOnCells();
        State stateToBeSent = state.buildState(state.getStage().buildStage(cellsToBeDisplayed), state.isRunning(), state.getGame());
        this.template.convertAndSend("/receive/stateFinal", stateToBeSent);
        return state;
    }

    private State sendSavedStateToBeDisplayed(State state) {
        state = createStateAfterMoveDown(state);
        char[][] cellsToBeDisplayed = state.getStage().drawTetraminoOnCells();
        State stateToBeSent = state.buildState(state.getStage().buildStage(cellsToBeDisplayed), state.isRunning(), state.getGame());
        this.template.convertAndSend("/receive/stateSaved", stateToBeSent);
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
                daoGameService.retrieveScores();
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
