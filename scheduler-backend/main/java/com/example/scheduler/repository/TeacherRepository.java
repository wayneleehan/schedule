package com.example.scheduler.repository; // 修正：拿掉前面的 main.java.

import com.example.scheduler.model.Teacher; // 修正：拿掉前面的 main.java.
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TeacherRepository extends JpaRepository<Teacher, Long> {
    // 這裡維持空著就好
    Teacher findByName(String name);
}