package com.barrcon.patchy.repositories;

import com.barrcon.patchy.models.Tech;
import io.micrometer.observation.ObservationFilter;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface TechRepository extends CrudRepository<Tech, Long> {
    Optional<Tech> findByName(String name);
}
