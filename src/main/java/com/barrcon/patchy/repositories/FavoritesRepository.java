package com.barrcon.patchy.repositories;

import com.barrcon.patchy.models.Favorites;
import org.springframework.data.repository.CrudRepository;

public interface FavoritesRepository extends CrudRepository <Favorites, Long> {
}
