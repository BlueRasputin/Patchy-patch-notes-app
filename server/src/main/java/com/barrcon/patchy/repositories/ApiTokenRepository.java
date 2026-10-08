package com.barrcon.patchy.repositories;

import com.barrcon.patchy.models.ApiToken;
import com.barrcon.patchy.models.User;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface ApiTokenRepository extends CrudRepository<ApiToken, Long> {

    Optional<ApiToken> findByTokenHash(String tokenHash);

    List<ApiToken> findByUserOrderByCreatedAtDesc(User user);
}
