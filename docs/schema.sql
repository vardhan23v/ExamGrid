CREATE DATABASE IF NOT EXISTS examgrid_db;
USE examgrid_db;

CREATE TABLE app_users (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    username  VARCHAR(30)  NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL,
    role      VARCHAR(10)  NOT NULL,
    enabled   BIT          NOT NULL DEFAULT 1
);

CREATE TABLE students (
    id        BIGINT AUTO_INCREMENT PRIMARY KEY,
    usn       VARCHAR(15)  NOT NULL UNIQUE,
    name      VARCHAR(80)  NOT NULL,
    branch    VARCHAR(10)  NOT NULL,
    semester  INT          NOT NULL,
    email     VARCHAR(100)
);

CREATE TABLE faculty (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    faculty_code  VARCHAR(15)  NOT NULL UNIQUE,
    name          VARCHAR(80)  NOT NULL,
    department    VARCHAR(10)  NOT NULL,
    email         VARCHAR(100),
    max_duties    INT          NOT NULL DEFAULT 6
);

CREATE TABLE halls (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    hall_name      VARCHAR(30) NOT NULL UNIQUE,
    block          VARCHAR(30) NOT NULL,
    total_rows     INT         NOT NULL,
    total_columns  INT         NOT NULL
);

CREATE TABLE exams (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    subject_code  VARCHAR(15)  NOT NULL,
    subject_name  VARCHAR(100) NOT NULL,
    branch        VARCHAR(10)  NOT NULL,
    semester      INT          NOT NULL,
    exam_date     DATE         NOT NULL,
    start_time    TIME         NOT NULL,
    end_time      TIME         NOT NULL
);

CREATE TABLE seat_allocations (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    exam_id      BIGINT      NOT NULL,
    student_id   BIGINT      NOT NULL,
    hall_id      BIGINT      NOT NULL,
    exam_date    DATE        NOT NULL,
    start_time   TIME        NOT NULL,
    seat_row     INT         NOT NULL,
    seat_column  INT         NOT NULL,
    seat_number  VARCHAR(20) NOT NULL,
    CONSTRAINT uk_exam_student UNIQUE (exam_id, student_id),
    CONSTRAINT uk_hall_seat UNIQUE (hall_id, exam_date, start_time, seat_row, seat_column),
    CONSTRAINT fk_seat_exam    FOREIGN KEY (exam_id)    REFERENCES exams (id),
    CONSTRAINT fk_seat_student FOREIGN KEY (student_id) REFERENCES students (id),
    CONSTRAINT fk_seat_hall    FOREIGN KEY (hall_id)    REFERENCES halls (id)
);

CREATE TABLE invigilation_duties (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    faculty_id  BIGINT NOT NULL,
    hall_id     BIGINT NOT NULL,
    exam_date   DATE   NOT NULL,
    start_time  TIME   NOT NULL,
    end_time    TIME   NOT NULL,
    CONSTRAINT fk_duty_faculty FOREIGN KEY (faculty_id) REFERENCES faculty (id),
    CONSTRAINT fk_duty_hall    FOREIGN KEY (hall_id)    REFERENCES halls (id)
);
