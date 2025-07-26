package com.barrcon.patchy.repositories;

import com.barrcon.patchy.models.Feed;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

public interface FeedRepository extends CrudRepository<Feed, Long> {
    List<Feed> findByUserIdOrderByCreatedDateDesc(Long userId);
    List<Feed> findByUserIdAndIsReadFalse(Long userId);
    List<Feed> findByUserIdAndTechIdIn(Long userId, List<Long> techIds);
}
