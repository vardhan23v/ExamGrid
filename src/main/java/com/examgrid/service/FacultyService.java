package com.examgrid.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examgrid.entity.Faculty;
import com.examgrid.entity.Role;
import com.examgrid.exception.DuplicateResourceException;
import com.examgrid.exception.ResourceNotFoundException;
import com.examgrid.repository.FacultyRepository;
import com.examgrid.repository.InvigilationDutyRepository;

@Service
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final InvigilationDutyRepository invigilationDutyRepository;
    private final UserAccountService userAccountService;

    public FacultyService(FacultyRepository facultyRepository,
                          InvigilationDutyRepository invigilationDutyRepository,
                          UserAccountService userAccountService) {
        this.facultyRepository = facultyRepository;
        this.invigilationDutyRepository = invigilationDutyRepository;
        this.userAccountService = userAccountService;
    }

    public List<Faculty> findAll() {
        return facultyRepository.findAllByOrderByNameAsc();
    }

    public Faculty findById(Long id) {
        return facultyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Faculty", id));
    }

    public Faculty findByCode(String code) {
        return facultyRepository.findByFacultyCodeIgnoreCase(code)
                .orElseThrow(() -> new ResourceNotFoundException("Faculty", code));
    }

    public boolean codeExists(String code) {
        return facultyRepository.existsByFacultyCodeIgnoreCase(code);
    }

    public long count() {
        return facultyRepository.count();
    }

    @Transactional
    public Faculty create(Faculty faculty) {
        if (codeExists(faculty.getFacultyCode())) {
            throw new DuplicateResourceException("Faculty code " + faculty.getFacultyCode() + " already exists");
        }
        faculty.setId(null);
        faculty.setFacultyCode(faculty.getFacultyCode().toUpperCase());
        faculty.setDepartment(faculty.getDepartment().toUpperCase());
        Faculty saved = facultyRepository.save(faculty);
        userAccountService.createDefaultAccount(saved.getFacultyCode(), Role.FACULTY);
        return saved;
    }

    @Transactional
    public Faculty update(Long id, Faculty changes) {
        Faculty existing = findById(id);
        existing.setName(changes.getName());
        existing.setDepartment(changes.getDepartment().toUpperCase());
        existing.setEmail(changes.getEmail());
        existing.setMaxDuties(changes.getMaxDuties());
        return facultyRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Faculty faculty = findById(id);
        invigilationDutyRepository.deleteByFacultyId(id);
        userAccountService.deleteAccount(faculty.getFacultyCode());
        facultyRepository.delete(faculty);
    }
}
