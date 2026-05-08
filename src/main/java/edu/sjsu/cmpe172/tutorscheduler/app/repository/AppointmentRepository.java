package edu.sjsu.cmpe172.tutorscheduler.app.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import edu.sjsu.cmpe172.tutorscheduler.app.model.Appointment;

@Repository
public class AppointmentRepository {

    private final JdbcTemplate jdbcTemplate;

    public AppointmentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Appointment> findAll() {
        String sql = """
                SELECT l.LessonID, l.Status, l.Date, l.StartTime,
                       s.StudentName, s.SEmail, t.TutorName,
                       subj.SubjectName, loc.LocationName
                FROM TutorLesson l
                JOIN Student s ON l.StudentID = s.StudentID
                JOIN Tutor t ON l.TutorID = t.TutorID
                JOIN Subject subj ON l.SubjectID = subj.SubjectID
                JOIN Location loc ON l.LocationID = loc.LocationID
                ORDER BY l.Date, l.StartTime
                """;
        return jdbcTemplate.query(sql, new AppointmentRowMapper());
    }

    public Appointment findById(Long id) {
        String sql = """
                SELECT l.LessonID, l.Status, l.Date, l.StartTime,
                       s.StudentName, s.SEmail, t.TutorName,
                       subj.SubjectName, loc.LocationName
                FROM TutorLesson l
                JOIN Student s ON l.StudentID = s.StudentID
                JOIN Tutor t ON l.TutorID = t.TutorID
                JOIN Subject subj ON l.SubjectID = subj.SubjectID
                JOIN Location loc ON l.LocationID = loc.LocationID
                WHERE l.LessonID = ?
                """;
        List<Appointment> results = jdbcTemplate.query(sql, new AppointmentRowMapper(), id);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<Appointment> findByTutorId(Long tutorId) {
        String sql = """
                SELECT l.LessonID, l.Status, l.Date, l.StartTime,
                       s.StudentName, s.SEmail, t.TutorName,
                       subj.SubjectName, loc.LocationName
                FROM TutorLesson l
                JOIN Student s ON l.StudentID = s.StudentID
                JOIN Tutor t ON l.TutorID = t.TutorID
                JOIN Subject subj ON l.SubjectID = subj.SubjectID
                JOIN Location loc ON l.LocationID = loc.LocationID
                WHERE l.TutorID = ?
                ORDER BY l.Date, l.StartTime
                """;
        return jdbcTemplate.query(sql, new AppointmentRowMapper(), tutorId);
    }

    public boolean cancelById(Long lessonId) {
        String sql = "UPDATE TutorLesson SET Status = 'CANCELED' WHERE LessonID = ? AND Status <> 'CANCELED'";
        return jdbcTemplate.update(sql, lessonId) > 0;
    }

    public boolean existsByTutorAndTime(Long tutorId, LocalDate date, LocalTime startTime) {
        String sql = "SELECT 1 FROM TutorLesson WHERE TutorID = ? AND Date = ? AND StartTime = ? AND Status = 'BOOKED' LIMIT 1";
        List<Integer> results = jdbcTemplate.queryForList(sql, Integer.class, tutorId, date, startTime);
        return !results.isEmpty();
    }

    public Long findOrCreateStudent(String name, String email) {
        String findSql = "SELECT StudentID FROM Student WHERE SEmail = ? LIMIT 1";
        List<Long> existing = jdbcTemplate.queryForList(findSql, Long.class, email);
        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        String insertSql = "INSERT INTO Student (StudentName, SEmail, SPhoneNumber) VALUES (?, ?, NULL)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(insertSql, new String[] { "StudentID" });
            ps.setString(1, name);
            ps.setString(2, email);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public Long findOrCreateSubject(String subjectName) {
        String resolvedSubjectName = (subjectName == null || subjectName.isBlank()) ? "General" : subjectName;
        String findSql = "SELECT SubjectID FROM Subject WHERE SubjectName = ? LIMIT 1";
        List<Long> existing = jdbcTemplate.queryForList(findSql, Long.class, resolvedSubjectName);
        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        String insertSql = "INSERT INTO Subject (SubjectName) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(insertSql, new String[] { "SubjectID" });
            ps.setString(1, resolvedSubjectName);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public Long findOrCreateLocation(String locationName, String address) {
        String resolvedLocationName = (locationName == null || locationName.isBlank()) ? "TBD" : locationName;
        String findSql = "SELECT LocationID FROM Location WHERE LocationName = ? LIMIT 1";
        List<Long> existing = jdbcTemplate.queryForList(findSql, Long.class, resolvedLocationName);
        if (!existing.isEmpty()) {
            return existing.get(0);
        }

        String insertSql = "INSERT INTO Location (LocationName, Address) VALUES (?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(insertSql, new String[] { "LocationID" });
            ps.setString(1, resolvedLocationName);
            if (address == null) {
                ps.setNull(2, Types.VARCHAR);
            } else {
                ps.setString(2, address);
            }
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        return key != null ? key.longValue() : null;
    }

    public Appointment insertLesson(Long tutorId, LocalDate date, LocalTime startTime, LocalTime lessonEnd, Long studentId, Long locationId, Long subjectId) {
        String insertSql = """
                INSERT INTO TutorLesson
                (Status, LessonEnd, TutorID, Date, StartTime, StudentID, LocationID, SubjectID)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(con -> {
            var ps = con.prepareStatement(insertSql, new String[] { "LessonID" });
            ps.setString(1, "BOOKED");
            ps.setTime(2, java.sql.Time.valueOf(lessonEnd));
            ps.setLong(3, tutorId);
            ps.setDate(4, java.sql.Date.valueOf(date));
            ps.setTime(5, java.sql.Time.valueOf(startTime));
            ps.setLong(6, studentId);
            ps.setLong(7, locationId);
            ps.setLong(8, subjectId);
            return ps;
        }, keyHolder);

        Number lessonId = keyHolder.getKey();
        if (lessonId == null) {
            return null;
        }

        return findById(lessonId.longValue());
    }

    private static class AppointmentRowMapper implements RowMapper<Appointment> {
        @Override
        public Appointment mapRow(ResultSet rs, int rowNum) throws SQLException {
            Appointment appointment = new Appointment();
            appointment.setLessonId(rs.getLong("LessonID"));
            appointment.setStatus(rs.getString("Status"));
            appointment.setDate(rs.getDate("Date").toLocalDate());
            appointment.setStartTime(rs.getTime("StartTime").toLocalTime());
            appointment.setStudentName(rs.getString("StudentName"));
            appointment.setStudentEmail(rs.getString("SEmail"));
            appointment.setTutorName(rs.getString("TutorName"));
            appointment.setSubjectName(rs.getString("SubjectName"));
            appointment.setLocationName(rs.getString("LocationName"));
            return appointment;
        }
    }
}
