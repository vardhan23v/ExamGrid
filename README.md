# ExamGrid

> Automated Exam Seating and Invigilation Allocation System using Spring Boot.

![Java](https://img.shields.io/badge/Java-17-007396?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?style=flat-square&logo=springsecurity&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=flat-square&logo=thymeleaf&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8-4479A1?style=flat-square&logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-C71A36?style=flat-square&logo=apachemaven&logoColor=white)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5-7952B3?style=flat-square&logo=bootstrap&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-yellow?style=flat-square)
![Build](https://img.shields.io/badge/Build-Passing-brightgreen?style=flat-square)

CS3603-1 Java and Spring Framework Laboratory Mini Project, V Semester CSE, NMAM Institute of Technology, Nitte.


## Tech stack

Java 17, Spring Boot 3.3, Spring MVC, Spring Data JPA (Hibernate), Spring Security, Bean Validation, Thymeleaf, Bootstrap 5, JavaScript Fetch API, MySQL 8, OpenPDF, Maven.

## Running the project

1. Install JDK 17 or newer, Maven 3.9 and MySQL 8.
2. Start MySQL. The default connection in `src/main/resources/application.properties` is `root` / `root` on `localhost:3306`. Change it if your MySQL password is different. The database `examgrid_db` is created automatically.
3. From the project folder run:

```
mvn spring-boot:run
```

4. Open http://localhost:8080

On first start the application creates the admin account and loads sample data (4 halls, 148 students, 10 faculty, 8 exams in 3 slots). Set `examgrid.seed-sample-data=false` to start with an empty database.

## Login accounts

| Role | Username | Password |
|---|---|---|
| Exam Cell (Admin) | admin | admin123 |
| Faculty | FAC101 to FAC110 | Welcome@123 |
| Student | USN, e.g. NNM24CS224 | Welcome@123 |

Seat lookup by USN at `/seat-lookup` does not need a login.

## Typical flow

1. Login as admin.
2. Check halls, students, faculty and exams (or add your own).
3. Open **Allocation**, click **1. Generate seating** for a slot.
4. Click **2. Assign invigilators** for the same slot.
5. Download the seating chart and duty roster PDFs.
6. Students search their USN on Seat Lookup; faculty log in to see their duties.

## Running tests

```
mvn test
```

Tests use an in-memory H2 database, so MySQL is not needed for them.

## Project structure

```
src/main/java/com/examgrid
  config/        DataSeeder (sample data)
  controller/api REST controllers (/api/**)
  controller/web Thymeleaf MVC controllers
  dto/           Response records
  entity/        JPA entities
  exception/     Custom exceptions and @ControllerAdvice handlers
  repository/    Spring Data JPA repositories
  security/      Spring Security configuration
  service/       Business logic, allocation engines, PDF export
src/main/resources
  templates/     Thymeleaf pages
  static/        CSS and JavaScript
docs/
  schema.sql     MySQL schema
  postman/       Postman collection
  latex/         Project reports (LaTeX sources and PDFs)
```
