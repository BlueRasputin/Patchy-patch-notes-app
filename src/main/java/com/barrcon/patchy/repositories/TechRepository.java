package com.barrcon.patchy.repositories;

import com.barrcon.patchy.models.Tech;
import org.springframework.data.repository.CrudRepository;

public interface TechRepository extends CrudRepository<Tech, Long> {
}
