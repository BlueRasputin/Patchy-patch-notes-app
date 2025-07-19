package com.barrcon.patchy.repositories;

import com.barrcon.patchy.models.Feed;
import org.springframework.data.repository.CrudRepository;

public interface FeedRepository extends CrudRepository<Feed, Long> {
}
