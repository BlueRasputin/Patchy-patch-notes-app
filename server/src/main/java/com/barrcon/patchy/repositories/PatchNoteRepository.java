package com.barrcon.patchy.repositories;

import com.barrcon.patchy.models.PatchNote;
import com.barrcon.patchy.models.Tech;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatchNoteRepository extends CrudRepository<PatchNote, Long> {

    // Find patch notes for a specific tech
    List<PatchNote> findByTech(Tech tech);

    List<PatchNote> findAllByOrderByCreatedAtDesc();

    Optional<PatchNote> findFirstByTechOrderByCreatedAtDesc(Tech tech);
}