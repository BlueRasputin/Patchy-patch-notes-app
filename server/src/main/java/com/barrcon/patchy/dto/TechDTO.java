package com.barrcon.patchy.dto;

//public class TechDTO {
//
//    private Long userId;
//    private Long techId;
//
//
//    public TechDTO() {
//    }
//
//    public TechDTO(Long userId, Long techId) {
//       this.userId = userId;
//        this.techId = techId;
//    }
//
//    public Long getUserId() {
//        return userId;
//    }
//
//    public void setUserId(Long userId) {
//        this.userId = userId;
//    }
//
//    public Long getTechId() {
//        return techId;
//    }
//
//    public void setTechId(Long techId) {
//        this.techId = techId;
//    }
//}

import java.util.List;
import java.util.Set;

public class TechDTO {
    private Set<Long> techIds;

    public TechDTO() {
    }

    public Set<Long> getTechIds() {
        return techIds;
    }

    public void setTechIds(Set<Long> techIds) {
        this.techIds = techIds;
    }
}


//@PostMapping("/{userId}/favorites")
//public ResponseEntity<User> addFavorite(@PathVariable Long userId, @RequestBody Long techId) {
//    return userRepository.findById(userId)
//            .flatMap(user -> techRepository.findById(techId)
//                    .map(tech -> {
//                        user.getFavoriteTechs().add(tech);
//                        return userRepository.save(user);
//                    }))
//            .map(ResponseEntity::ok)
//            .orElseGet(() -> ResponseEntity.notFound().build());
//}