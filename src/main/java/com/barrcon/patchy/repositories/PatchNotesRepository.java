package com.barrcon.patchy.repositories;

import com.barrcon.patchy.models.PatchNotes;
import org.springframework.data.repository.CrudRepository;

public interface PatchNotesRepository extends CrudRepository<PatchNotes, Long> {
}
