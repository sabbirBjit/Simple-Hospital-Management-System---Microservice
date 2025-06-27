package com.hms.appointment.service;

import com.hms.appointment.model.Appointment;
import com.hms.appointment.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class AppointmentReminderService {
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void sendAppointmentReminders() {
        try {
            LocalDate tomorrow = LocalDate.now().plusDays(1);
            LocalTime reminderStartTime = LocalTime.of(8, 0);
            LocalTime reminderEndTime = LocalTime.of(18, 0);
            
            List<Appointment> upcomingAppointments = appointmentRepository
                .findUpcomingAppointmentsForDate(tomorrow, reminderStartTime, reminderEndTime);
            
            for (Appointment appointment : upcomingAppointments) {
                String reminderEventData = String.format(
                    "{\"appointmentId\":%d,\"patientUserId\":%d,\"doctorUserId\":%d,\"appointmentDate\":\"%s\",\"appointmentTime\":\"%s\",\"reminderType\":\"24_HOUR\"}", 
                    appointment.getId(),
                    appointment.getPatientUserId(),
                    appointment.getDoctorUserId(),
                    appointment.getAppointmentDate().toString(),
                    appointment.getAppointmentTime().toString()
                );
                
                kafkaTemplate.send("appointment.reminder", reminderEventData);
            }
        } catch (Exception e) {
            // Log error but don't fail the scheduled task
            System.err.println("Error sending appointment reminders: " + e.getMessage());
        }
    }
}
