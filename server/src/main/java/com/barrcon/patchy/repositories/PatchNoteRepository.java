package com.barrcon.patchy.repositories;

import com.barrcon.patchy.models.PatchNote;
import com.barrcon.patchy.models.Tech;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatchNoteRepository extends CrudRepository<PatchNote, Long> {

    List<PatchNote> findByTechOrderByCreatedAtDesc(Tech tech);

    Optional<PatchNote> findFirstByTechOrderByCreatedAtDesc(Tech tech);

    // Newest note per tech, for feeds (notes are append-only history)
    @Query("""
            select p from PatchNote p
            where p.createdAt = (select max(p2.createdAt) from PatchNote p2 where p2.tech = p.tech)
            order by p.createdAt desc
            """)
    List<PatchNote> findLatestPerTech();

    List<PatchNote> findTop10ByPendingSummaryTrueOrderByCreatedAtAsc();
}
