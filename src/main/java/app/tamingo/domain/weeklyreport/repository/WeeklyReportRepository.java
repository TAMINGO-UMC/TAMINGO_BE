package app.tamingo.domain.weeklyreport.repository;

import app.tamingo.domain.weeklyreport.entity.WeeklyReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long> {

    Optional<WeeklyReport> findByUserIdAndWeekStartDate(Long userId, LocalDate weekStartDate);


    // 1) dailySummaries만
    @EntityGraph(attributePaths = {"dailySummaries"})
    Optional<WeeklyReport> findWithDailySummariesByUserIdAndWeekStartDate(Long userId, LocalDate weekStartDate);

    // 2) insights만
    @EntityGraph(attributePaths = {"insights"})
    Optional<WeeklyReport> findWithInsightsByUserIdAndWeekStartDate(Long userId, LocalDate weekStartDate);


}
