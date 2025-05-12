package com.landr.repository.dday;

import com.landr.domain.dday.DDay;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface DDayRepository extends JpaRepository<DDay, Long> {

    @Query("SELECT d FROM DDay d WHERE d.user.id = :userId")
    List<DDay> findByUserId(Long userId);
}
