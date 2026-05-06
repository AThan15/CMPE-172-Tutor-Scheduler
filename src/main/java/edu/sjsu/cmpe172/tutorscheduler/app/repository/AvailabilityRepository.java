package edu.sjsu.cmpe172.tutorscheduler.app.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import edu.sjsu.cmpe172.tutorscheduler.app.model.AvailabilitySlot;

@Repository
public class AvailabilityRepository {

    private final JdbcTemplate jdbcTemplate;

    public AvailabilityRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<AvailabilitySlot> findAll() {
        String sql = """
                SELECT a.TutorID, t.TutorName, a.Date, a.StartTime, a.EndTime
                FROM Availability a
                JOIN Tutor t ON a.TutorID = t.TutorID
                LEFT JOIN TutorLesson l
                  ON l.TutorID = a.TutorID AND l.Date = a.Date AND l.StartTime = a.StartTime
                WHERE l.LessonID IS NULL
                ORDER BY a.Date, a.StartTime
                """;
        return jdbcTemplate.query(sql, new AvailabilityRowMapper());
    }

    public AvailabilitySlot lockSlotForUpdate(Long tutorId, LocalDate date, LocalTime startTime) {
        String sql = """
                SELECT a.TutorID, t.TutorName, a.Date, a.StartTime, a.EndTime
                FROM Availability a
                JOIN Tutor t ON a.TutorID = t.TutorID
                WHERE a.TutorID = ? AND a.Date = ? AND a.StartTime = ?
                FOR UPDATE
                """;
        List<AvailabilitySlot> results = jdbcTemplate.query(sql, new AvailabilityRowMapper(), tutorId, date, startTime);
        return results.isEmpty() ? null : results.get(0);
    }

    private static class AvailabilityRowMapper implements RowMapper<AvailabilitySlot> {
        @Override
        public AvailabilitySlot mapRow(ResultSet rs, int rowNum) throws SQLException {
            AvailabilitySlot slot = new AvailabilitySlot();
            slot.setTutorId(rs.getLong("TutorID"));
            slot.setTutorName(rs.getString("TutorName"));
            slot.setSubjectName("TBD");
            slot.setDate(rs.getDate("Date").toLocalDate());
            slot.setStartTime(rs.getTime("StartTime").toLocalTime());
            slot.setEndTime(rs.getTime("EndTime").toLocalTime());
            slot.setLocationName("TBD");
            return slot;
        }
    }
}
