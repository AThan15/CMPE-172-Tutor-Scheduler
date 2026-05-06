package edu.sjsu.cmpe172.tutorscheduler.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import edu.sjsu.cmpe172.tutorscheduler.app.model.Appointment;
import edu.sjsu.cmpe172.tutorscheduler.app.model.AppointmentRequest;
import edu.sjsu.cmpe172.tutorscheduler.app.service.AppointmentService;

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
    public String appointments(Model model) {
        model.addAttribute("appointments", appointmentService.getAppointments());
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
}
