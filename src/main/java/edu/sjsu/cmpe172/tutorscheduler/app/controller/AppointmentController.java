package edu.sjsu.cmpe172.tutorscheduler.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.sjsu.cmpe172.tutorscheduler.app.model.Appointment;
import edu.sjsu.cmpe172.tutorscheduler.app.model.AppointmentRequest;
import edu.sjsu.cmpe172.tutorscheduler.app.model.TutorProfile;
import edu.sjsu.cmpe172.tutorscheduler.app.service.AppointmentService;

import java.util.List;

@Controller
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/slots")
    public String slots(Model model, @RequestParam(required = false) String conflict) {
        model.addAttribute("slots", appointmentService.getAvailableSlots());
        model.addAttribute("bookingConflict", conflict != null);
        return "slots";
    }

    @GetMapping("/appointments")
    public String appointments(Model model, @RequestParam(required = false) String canceled, @RequestParam(required = false) String cancelFailed) {
        model.addAttribute("appointments", appointmentService.getAppointments());
        model.addAttribute("canceled", canceled != null);
        model.addAttribute("cancelFailed", cancelFailed != null);
        return "appointments";
    }

    @GetMapping("/appointments/new")
    public String newAppointment(Model model, @RequestParam(required = false) Long tutorId, @RequestParam(required = false) String date, @RequestParam(required = false) String startTime) {
        AppointmentRequest request = new AppointmentRequest();
        request.setTutorId(tutorId);
        request.setDate(date);
        request.setStartTime(startTime);
        model.addAttribute("request", request);
        return "appointment-form";
    }

    @PostMapping("/appointments")
    public String createAppointment(@ModelAttribute("request") AppointmentRequest request, RedirectAttributes redirectAttributes) {
        try {
            Appointment appointment = appointmentService.bookAppointment(request);
            redirectAttributes.addAttribute("id", appointment.getLessonId());
            return "redirect:/appointments/confirmation";
        } catch (DataIntegrityViolationException ex) {
            redirectAttributes.addAttribute("conflict", "1");
            return "redirect:/slots";
        }
    }

    @GetMapping("/appointments/confirmation")
    public String confirmation(@RequestParam("id") Long id, Model model) {
        model.addAttribute("appointment", appointmentService.getAppointment(id));
        return "appointment-confirmation";
    }

    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        boolean canceled = appointmentService.cancelAppointment(id);
        if (canceled) {
            redirectAttributes.addAttribute("canceled", "1");
        } else {
            redirectAttributes.addAttribute("cancelFailed", "1");
        }
        return "redirect:/appointments";
    }

    @GetMapping("/tutor")
    public String tutorPortal(
            Model model,
            @RequestParam(required = false) Long tutorId,
            @RequestParam(required = false) String created,
            @RequestParam(required = false) String createFailed
    ) {
        List<TutorProfile> tutors = appointmentService.getTutors();
        Long selectedTutorId = tutorId;
        if (selectedTutorId == null && !tutors.isEmpty()) {
            selectedTutorId = tutors.get(0).getTutorId();
        }

        model.addAttribute("tutors", tutors);
        model.addAttribute("selectedTutorId", selectedTutorId);
        model.addAttribute("appointments", appointmentService.getTutorAppointments(selectedTutorId));
        model.addAttribute("created", created != null);
        model.addAttribute("createFailed", createFailed != null);
        return "tutor-portal";
    }

    @PostMapping("/tutor/availability")
    public String createTutorAvailability(
            @RequestParam Long tutorId,
            @RequestParam String date,
            @RequestParam String startTime,
            @RequestParam String endTime,
            RedirectAttributes redirectAttributes
    ) {
        try {
            boolean created = appointmentService.createTutorAvailability(tutorId, date, startTime, endTime);
            redirectAttributes.addAttribute("tutorId", tutorId);
            if (created) {
                redirectAttributes.addAttribute("created", "1");
            } else {
                redirectAttributes.addAttribute("createFailed", "1");
            }
            return "redirect:/tutor";
        } catch (RuntimeException ex) {
            redirectAttributes.addAttribute("tutorId", tutorId);
            redirectAttributes.addAttribute("createFailed", "1");
            return "redirect:/tutor";
        }
    }
}
