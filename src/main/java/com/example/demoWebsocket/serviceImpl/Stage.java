package com.example.demoWebsocket.serviceImpl;

import com.example.demoWebsocket.model.Tetramino;
import com.example.demoWebsocket.service.StageService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

@Service
public class Stage implements StageService {
    private static final StringBuilder pause = new StringBuilder("go!");
    private char[][] cells;
    private Tetramino tetramino;
    private int tetraminoX;
    private int tetraminoY;
    private int collapsedLayersCount;

    private Stage(char[][] cells, Tetramino tetramino, int tetraminoX, int tetraminoY, int collapsedLayersCount) {
        this.cells = cells;
        this.tetramino = tetramino;
        this.tetraminoX = tetraminoX;
        this.tetraminoY = tetraminoY;
        this.collapsedLayersCount = collapsedLayersCount;
    }

    private Stage() {
    }

    public Stage buildStage(char[][] cells, Tetramino tetramino, int tetraminoX, int tetraminoY, int collapsedLayersCount) {
        return new Stage(cells, tetramino, tetraminoX, tetraminoY, collapsedLayersCount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stage stage = (Stage) o;
        return tetraminoX == stage.tetraminoX && tetraminoY == stage.tetraminoY && collapsedLayersCount == stage.collapsedLayersCount && Arrays.deepEquals(cells, stage.cells) && tetramino.equals(stage.tetramino);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(tetramino, tetraminoX, tetraminoY, collapsedLayersCount);
        result = 31 * result + Arrays.hashCode(cells);
        return result;
    }

    @Override
    public Stage moveLeft() {
        return new Stage(cells, tetramino, tetraminoX - 1, tetraminoY, collapsedLayersCount);
    }

    @Override
    public Stage moveRight() {
        return new Stage(cells, tetramino, tetraminoX + 1, tetraminoY, collapsedLayersCount);
    }

    @Override
    public Stage moveDown(int step) {
        if (pause.toString().equals("go!"))
            return new Stage(cells, tetramino, tetraminoX, tetraminoY + step, collapsedLayersCount);
        else return new Stage(cells, tetramino, tetraminoX, tetraminoY, collapsedLayersCount);
    }

    @Override
    public Stage rotate() {
        return new Stage(cells, tetramino.buildTetramino(rotateMatrix(tetramino.getShape())), tetraminoX, tetraminoY, collapsedLayersCount);
    }

    @Override
    public void setPause() {
        if (pause.toString().equals("go!")) pause.setCharAt(2, '?');
        else pause.setCharAt(2, '!');
    }

    @Override
    public void unsetPause() {
        if (pause.toString().equals("go?")) pause.setCharAt(2, '!');
        else pause.setCharAt(2, '!');
    }

    @Override
    public Stage setTetramino(Tetramino tetramino, int x, int y) {
        return new Stage(cells, tetramino, x, y, collapsedLayersCount);
    }

    @Override
    public Stage addTetramino() {
        return new Stage(drawTetraminoOnCells(), tetramino, tetraminoX, tetraminoY, collapsedLayersCount);
    }

    @Override
    public Stage collapseFilledLayers() {
        final char[][] c = Arrays.stream(cells).map(char[]::clone).toArray(char[][]::new); // copy
        final int[] ny2 = {0, HEIGHT - 1};

        IntStream.rangeClosed(0, HEIGHT - 1).forEach(y1 -> {
            if (!isFull(cells[HEIGHT - 1 - y1])) {
                System.arraycopy(c, HEIGHT - 1 - y1, c, ny2[1]--, 1);
            } else {
                ny2[0]++;
            }
        });
        return new Stage(c, tetramino, tetraminoX, tetraminoY, collapsedLayersCount + ny2[0]);
    }

    @Override
    public boolean checkCollision(int dx, int dy, boolean rotate) {
        final char[][] m = rotate ? rotateMatrix(tetramino.getShape()) : tetramino.getShape();
        final int h = m.length;
        final int w = m[0].length;
        return IntStream.range(0, h).anyMatch(y -> IntStream.range(0, w).anyMatch(x -> (
                m[y][x] != '0' && ((tetraminoY + y + dy >= HEIGHT)
                        || ((tetraminoX + x + dx) < 0)
                        || ((tetraminoX + x + dx) >= WIDTH)
                        || (cells[tetraminoY + y + dy][tetraminoX + x + dx] != '0'))
        )));
    }

    public char[][] drawTetraminoOnCells() {
        //  final char[][] c = Arrays.stream(cells).map(char[]::clone).toArray(char[][]::new); // copy
        final char[][] c = Arrays.stream(cells).map(char[]::clone).toArray(char[][]::new); // copy
        IntStream.range(0, tetramino.getShape().length).forEach(y ->
                IntStream.range(0, tetramino.getShape()[0].length).forEach(x -> {
                    if (tetramino.getShape()[y][x] != '0')
                        c[tetraminoY + y][tetraminoX + x] = tetramino.getShape()[y][x];
                }));
        return c;
    }

    public char[][] getCells() {
        return cells;
    }

    public Tetramino getTetramino() {
        return tetramino;
    }

    public int getTetraminoX() {
        return tetraminoX;
    }

    public int getTetraminoY() {
        return tetraminoY;
    }

    public int getCollapsedLayersCount() {
        return collapsedLayersCount;
    }

    private char[][] rotateMatrix(char[][] m) {
        final int h = m.length;
        final int w = m[0].length;
        final char[][] t = new char[h][w];
        IntStream.range(0, h).forEach(y -> IntStream.range(0, w).forEach(x -> t[w - x - 1][y] = m[y][x]));
        return t;
    }

    private boolean isFull(char[] row) {
        return IntStream.range(0, row.length).noneMatch(i -> row[i] == '0');
    }

    public void setCells(char[][] cells) {
        this.cells = cells;
    }

    public Stage setStage(char[][] cells){
        return buildStage(cells,tetramino,tetraminoX,tetraminoY,collapsedLayersCount);
    }
}
