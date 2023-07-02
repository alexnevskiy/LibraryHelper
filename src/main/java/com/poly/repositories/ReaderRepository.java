package com.poly.repositories;

import com.poly.domains.entities.ReaderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReaderRepository extends JpaRepository<ReaderEntity, Integer> {
}
