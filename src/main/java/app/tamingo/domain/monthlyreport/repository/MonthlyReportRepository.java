package app.tamingo.domain.monthlyreport.repository;

import app.tamingo.domain.monthlyreport.entity.MonthlyReport;
import app.tamingo.domain.weeklyreport.entity.WeeklyReport;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface MonthlyReportRepository extends JpaRepository<MonthlyReport, Long> {

    Optional<MonthlyReport> findByUserIdAndMonthStartDate(Long userId, LocalDate monthStartDate);

    @EntityGraph(attributePaths = {"weekSummaries"})
    Optional<MonthlyReport> findWithWeekSummariesByUserIdAndMonthStartDate(Long userId, LocalDate monthStartDate);

    @EntityGraph(attributePaths = {"insights"})
    Optional<MonthlyReport> findWithInsightsByUserIdAndMonthStartDate(Long userId, LocalDate weekStartDate);

}
