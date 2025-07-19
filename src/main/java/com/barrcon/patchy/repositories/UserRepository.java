package com.barrcon.patchy.repositories;

import com.barrcon.patchy.models.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User, Long> {
}