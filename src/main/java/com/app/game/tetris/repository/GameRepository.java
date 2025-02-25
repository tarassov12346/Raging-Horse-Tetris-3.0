package com.app.game.tetris.repository;

import com.app.game.tetris.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<Game,Long> {
    Long deleteByPlayerName(String name);
}
