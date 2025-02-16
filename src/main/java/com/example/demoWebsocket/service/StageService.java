package com.example.demoWebsocket.service;

import com.example.demoWebsocket.model.Tetramino;
import com.example.demoWebsocket.serviceImpl.Stage;

public interface StageService extends GameLogic<Stage>{
    Stage buildStage(char[][] cells, Tetramino tetramino, int tetraminoX, int tetraminoY, int collapsedLayersCount);
    char[][] drawTetraminoOnCells();
    char[][] getCells();
    Tetramino getTetramino();
    int getTetraminoX();
    int getTetraminoY();
    int getCollapsedLayersCount();
    void setCells(char[][] cells);
}
