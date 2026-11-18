package com.examgrid.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.examgrid.entity.Role;
import com.examgrid.entity.Student;
import com.examgrid.exception.DuplicateResourceException;
import com.examgrid.exception.ResourceNotFoundException;
import com.examgrid.repository.SeatAllocationRepository;
import com.examgrid.repository.StudentRepository;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final SeatAllocationRepository seatAllocationRepository;
    private final UserAccountService userAccountService;

    public StudentService(StudentRepository studentRepository,
                          SeatAllocationRepository seatAllocationRepository,
                          UserAccountService userAccountService) {
        this.studentRepository = studentRepository;
        this.seatAllocationRepository = seatAllocationRepository;
        this.userAccountService = userAccountService;
    }

    public List<Student> findAll() {
        return studentRepository.findAllByOrderByUsnAsc();
    }

    public Student findById(Long id) {
        return studentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Student", id));
    }

    public Student findByUsn(String usn) {
        return studentRepository.findByUsnIgnoreCase(usn).orElseThrow(() -> new ResourceNotFoundException("Student", usn));
    }

    public boolean usnExists(String usn) {
        return studentRepository.existsByUsnIgnoreCase(usn);
    }

    public long count() {
        return studentRepository.count();
    }

    @Transactional
    public Student create(Student student) {
        if (usnExists(student.getUsn())) {
            throw new DuplicateResourceException("A student with USN " + student.getUsn() + " already exists");
        }
        student.setId(null);
        student.setUsn(student.getUsn().toUpperCase());
        student.setBranch(student.getBranch().toUpperCase());
        Student saved = studentRepository.save(student);
        userAccountService.createDefaultAccount(saved.getUsn(), Role.STUDENT);
        return saved;
    }

    @Transactional
    public Student update(Long id, Student changes) {
        Student existing = findById(id);
        existing.setName(changes.getName());
        existing.setBranch(changes.getBranch().toUpperCase());
        existing.setSemester(changes.getSemester());
        existing.setEmail(changes.getEmail());
        return studentRepository.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Student student = findById(id);
        seatAllocationRepository.deleteByStudentId(id);
        userAccountService.deleteAccount(student.getUsn());
        studentRepository.delete(student);
    }
}
