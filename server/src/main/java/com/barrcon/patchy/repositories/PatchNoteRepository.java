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

    // Find by tech and version to avoid duplicates
    Optional<PatchNote> findByTechAndVersion(Tech tech, String version);


    List<PatchNote> findAllByOrderByCreatedAtDesc();
}