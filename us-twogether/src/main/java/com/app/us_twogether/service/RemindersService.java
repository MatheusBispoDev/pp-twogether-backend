package com.app.us_twogether.service;

import com.app.us_twogether.domain.reminder.Reminder;
import com.app.us_twogether.domain.reminder.ReminderDTO;
import com.app.us_twogether.domain.space.Space;
import com.app.us_twogether.domain.user.User;
import com.app.us_twogether.repository.RemindersRepository;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class RemindersService {

    @Autowired
    private UserService userService;

    @Autowired
    private SpaceService spaceService;

    @Autowired
    private RemindersRepository remindersRepository;

    public ReminderDTO createReminders(String usernameCreation, Long spaceId, ReminderDTO reminderDTO) {
        Space space = spaceService.findSpaceById(spaceId);
        User user = userService.findByUsername(usernameCreation);

        Reminder newReminder = new Reminder();
        newReminder.setSpace(space);
        newReminder.setUserCreation(user);
        newReminder.setTitle(reminderDTO.title());
        newReminder.setDescription(reminderDTO.description());
        newReminder.setDateCreation(LocalDate.now());
        newReminder.setTimeCreation(LocalTime.now());
        newReminder.setDateCompletion(reminderDTO.dateCompletion());
        newReminder.setTimeCompletion(reminderDTO.timeCompletion());
        newReminder.setCompleted(false);

        remindersRepository.save(newReminder);

        return castRemindersToDTO(newReminder);
    }

    public ReminderDTO updateReminders(Long remindersId, ReminderDTO reminderDTO) {
        Reminder existingReminder = findRemindersById(remindersId);

        existingReminder.setTitle(reminderDTO.title());
        existingReminder.setDescription(reminderDTO.description());
        existingReminder.setDateCompletion(reminderDTO.dateCompletion());
        existingReminder.setTimeCompletion(reminderDTO.timeCompletion());
        existingReminder.setCompleted(reminderDTO.completed());

        remindersRepository.save(existingReminder);

        return castRemindersToDTO(existingReminder);
    }

    public void deletedReminders(Long remindersId) {
        Reminder existingReminder = findRemindersById(remindersId);

        remindersRepository.delete(existingReminder);
    }

    public ReminderDTO getReminders(Long remindersId) {
        Reminder reminder = findRemindersById(remindersId);

        return castRemindersToDTO(reminder);
    }

    public List<ReminderDTO> getAllRemindersFromSpace(Long spaceId, LocalDate dateCompletion) {
        Space space = spaceService.findSpaceById(spaceId);
        return remindersRepository.findBySpaceAndDate(space, dateCompletion);
    }

    public ReminderDTO completedReminders(Long remindersId) {
        //TODO Adicionar agendamento de completed e retirar requisição do controlller
        Reminder reminder = findRemindersById(remindersId);
        LocalDateTime completion = reminder.getDateCompletion().atTime(reminder.getTimeCompletion());

        if (completion.isBefore(LocalDateTime.now())) {
            reminder.setCompleted(true);
            remindersRepository.save(reminder);
        }
        return castRemindersToDTO(reminder);
    }

    private Reminder findRemindersById(Long remindersId) {
        return remindersRepository.findById(remindersId).orElseThrow(() -> new ResourceNotFoundException("Lembrete não encontrada"));
    }

    private ReminderDTO castRemindersToDTO(Reminder reminder) {
        return new ReminderDTO(reminder.getRemindersId(), reminder.getSpace().getSpaceId(), reminder.getUserCreation().getUsername(), reminder.getTitle(), reminder.getDescription(), reminder.getDateCreation(), reminder.getTimeCreation(), reminder.getDateCompletion(), reminder.getTimeCompletion(), reminder.isCompleted());
    }
}
