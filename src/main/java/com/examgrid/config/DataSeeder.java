package com.examgrid.config;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.examgrid.entity.AppUser;
import com.examgrid.entity.Exam;
import com.examgrid.entity.Faculty;
import com.examgrid.entity.Hall;
import com.examgrid.entity.Role;
import com.examgrid.entity.Student;
import com.examgrid.repository.AppUserRepository;
import com.examgrid.repository.ExamRepository;
import com.examgrid.repository.FacultyRepository;
import com.examgrid.repository.HallRepository;
import com.examgrid.repository.StudentRepository;
import com.examgrid.service.UserAccountService;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final String[] FIRST_NAMES = {
            "Aarav", "Ananya", "Rahul", "Sneha", "Karthik", "Divya", "Rohan", "Pooja", "Akash", "Meghana",
            "Varun", "Shreya", "Nikhil", "Kavya", "Manish", "Bhavana", "Suhas", "Ankitha", "Vishal", "Deeksha",
            "Pranav", "Sahana", "Tejas", "Navya", "Adithya", "Spoorthi", "Gagan", "Rashmi", "Harsha", "Chaitra"
    };
    private static final String[] LAST_NAMES = {
            "Shetty", "Rao", "Bhat", "Kamath", "Nayak", "Hegde", "Pai", "Acharya", "Poojary", "Shenoy",
            "Kini", "Prabhu", "Salian", "Kotian", "Amin"
    };

    private final AppUserRepository appUserRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final HallRepository hallRepository;
    private final ExamRepository examRepository;
    private final PasswordEncoder passwordEncoder;
    private final boolean seedSampleData;

    public DataSeeder(AppUserRepository appUserRepository, StudentRepository studentRepository,
                      FacultyRepository facultyRepository, HallRepository hallRepository,
                      ExamRepository examRepository, PasswordEncoder passwordEncoder,
                      @Value("${examgrid.seed-sample-data:true}") boolean seedSampleData) {
        this.appUserRepository = appUserRepository;
        this.studentRepository = studentRepository;
        this.facultyRepository = facultyRepository;
        this.hallRepository = hallRepository;
        this.examRepository = examRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedSampleData = seedSampleData;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!appUserRepository.existsByUsernameIgnoreCase("ADMIN")) {
            appUserRepository.save(new AppUser("ADMIN", passwordEncoder.encode("admin123"), Role.ADMIN));
        }
        if (!seedSampleData || studentRepository.count() > 0) {
            return;
        }
        String defaultHash = passwordEncoder.encode(UserAccountService.DEFAULT_PASSWORD);

        hallRepository.saveAll(List.of(
                new Hall("LH-101", "Academic Block A", 6, 6),
                new Hall("LH-102", "Academic Block A", 6, 6),
                new Hall("LH-201", "Academic Block B", 7, 8),
                new Hall("SH-01", "Seminar Hall Block C", 8, 8)));

        List<Student> students = new ArrayList<>();
        students.add(new Student("NNM25CS522", "Prajwal Jogi", "CSE", 5, "nnm25cs522@nmamit.in"));
        students.add(new Student("NNM25CS521", "Prajwal Acharya", "CSE", 5, "nnm25cs521@nmamit.in"));
        students.add(new Student("NNM24CS224", "Sathwik P Shetty", "CSE", 5, "nnm24cs224@nmamit.in"));
        students.add(new Student("NNM24CS225", "Satvik Hegde", "CSE", 5, "nnm24cs225@nmamit.in"));
        addBatch(students, "NNM24CS", 226, 44, "CSE", 5, 0);
        addBatch(students, "NNM24EC", 101, 36, "ECE", 5, 7);
        addBatch(students, "NNM24ME", 101, 24, "ME", 5, 13);
        addBatch(students, "NNM25CS", 101, 40, "CSE", 3, 19);
        studentRepository.saveAll(students);

        List<Faculty> faculty = List.of(
                new Faculty("FAC101", "Dr. Ramesh Kini", "CSE", "ramesh.kini@nmamit.in", 6),
                new Faculty("FAC102", "Prof. Sunitha Rao", "CSE", "sunitha.rao@nmamit.in", 6),
                new Faculty("FAC103", "Prof. Ganesh Bhat", "CSE", "ganesh.bhat@nmamit.in", 6),
                new Faculty("FAC104", "Dr. Lakshmi Shenoy", "ECE", "lakshmi.shenoy@nmamit.in", 6),
                new Faculty("FAC105", "Prof. Arjun Pai", "ECE", "arjun.pai@nmamit.in", 6),
                new Faculty("FAC106", "Dr. Vinay Hegde", "ME", "vinay.hegde@nmamit.in", 6),
                new Faculty("FAC107", "Prof. Rekha Nayak", "ME", "rekha.nayak@nmamit.in", 6),
                new Faculty("FAC108", "Dr. Prakash Acharya", "MAT", "prakash.acharya@nmamit.in", 6),
                new Faculty("FAC109", "Prof. Deepa Kamath", "CSE", "deepa.kamath@nmamit.in", 6),
                new Faculty("FAC110", "Prof. Mahesh Shetty", "ISE", "mahesh.shetty@nmamit.in", 6));
        facultyRepository.saveAll(faculty);

        LocalDate day1 = LocalDate.of(2026, 12, 7);
        LocalDate day2 = LocalDate.of(2026, 12, 8);
        LocalTime morning = LocalTime.of(9, 30);
        LocalTime morningEnd = LocalTime.of(12, 30);
        LocalTime afternoon = LocalTime.of(14, 0);
        LocalTime afternoonEnd = LocalTime.of(17, 0);
        examRepository.saveAll(List.of(
                new Exam("CS3603", "Java and Spring Framework", "CSE", 5, day1, morning, morningEnd),
                new Exam("EC3601", "Digital Signal Processing", "ECE", 5, day1, morning, morningEnd),
                new Exam("ME3601", "Heat Transfer", "ME", 5, day1, morning, morningEnd),
                new Exam("CS2301", "Data Structures and Applications", "CSE", 3, day1, morning, morningEnd),
                new Exam("CS3601", "Computer Networks", "CSE", 5, day2, morning, morningEnd),
                new Exam("EC3602", "VLSI Design", "ECE", 5, day2, morning, morningEnd),
                new Exam("ME3602", "Design of Machine Elements", "ME", 5, day2, morning, morningEnd),
                new Exam("CS2302", "Discrete Mathematical Structures", "CSE", 3, day2, afternoon, afternoonEnd)));

        List<AppUser> accounts = new ArrayList<>();
        students.forEach(s -> accounts.add(new AppUser(s.getUsn(), defaultHash, Role.STUDENT)));
        faculty.forEach(f -> accounts.add(new AppUser(f.getFacultyCode(), defaultHash, Role.FACULTY)));
        appUserRepository.saveAll(accounts);
    }

    private void addBatch(List<Student> students, String prefix, int start, int count,
                          String branch, int semester, int nameOffset) {
        for (int i = 0; i < count; i++) {
            String usn = prefix + String.format("%03d", start + i);
            String name = FIRST_NAMES[(i + nameOffset) % FIRST_NAMES.length] + " "
                    + LAST_NAMES[(i * 7 + nameOffset) % LAST_NAMES.length];
            students.add(new Student(usn, name, branch, semester, usn.toLowerCase() + "@nmamit.in"));
        }
    }
}
