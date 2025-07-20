package com.barrcon.patchy.repositories;

import com.barrcon.patchy.models.Tech;
import io.micrometer.observation.ObservationFilter;
import org.springframework.data.repository.CrudRepository;

public interface TechRepository extends CrudRepository<Tech, Long> {
    ObservationFilter getallTechnologies();

    Tech createTech(Tech tech);
}
