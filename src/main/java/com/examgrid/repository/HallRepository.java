package com.examgrid.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.examgrid.entity.Hall;

public interface HallRepository extends JpaRepository<Hall, Long> {

    boolean existsByHallNameIgnoreCase(String hallName);

    List<Hall> findAllByOrderByHallNameAsc();
}
