package com.barrcon.patchy.repositories;

import com.barrcon.patchy.models.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {


    User findByEmail(String email);

    boolean existsByUsername(String username);

    User findByUsername(String username);

    Optional<User> findById(Long userId);
}