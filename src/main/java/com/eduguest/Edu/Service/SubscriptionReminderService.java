package com.eduguest.Edu.Service;

import com.eduguest.Edu.Entity.AppNotification;
import com.eduguest.Edu.Entity.School;
import com.eduguest.Edu.Entity.SchoolMembership;
import com.eduguest.Edu.Entity.User;
import com.eduguest.Edu.Entity.UserRole;
import com.eduguest.Edu.Repository.AppNotificationRepository;
import com.eduguest.Edu.Repository.SchoolMembershipRepository;
import com.eduguest.Edu.Repository.SchoolRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Creates one founder reminder exactly seven days before a subscription ends. */
@Service
public class SubscriptionReminderService {
    private static final String REMINDER_TYPE = "subscription";

    private final SchoolRepository schoolRepository;
    private final SchoolMembershipRepository membershipRepository;
    private final AppNotificationRepository notificationRepository;

    public SubscriptionReminderService(SchoolRepository schoolRepository,
                                       SchoolMembershipRepository membershipRepository,
                                       AppNotificationRepository notificationRepository) {
        this.schoolRepository = schoolRepository;
        this.membershipRepository = membershipRepository;
        this.notificationRepository = notificationRepository;
    }

    @Scheduled(cron = "0 0 8 * * *", zone = "Africa/Douala")
    @Transactional
    public void sendSevenDayReminders() {
        LocalDate expiryDate = LocalDate.now().plusDays(7);
        schoolRepository.findAll().stream()
                .filter(School::isActive)
                .filter(school -> expiryDate.equals(school.getSubscriptionExpiresAt()))
                .forEach(school -> notifyFounder(school, expiryDate));
    }

    private void notifyFounder(School school, LocalDate expiryDate) {
        String dateText = expiryDate.toString();
        String message = "Votre abonnement EduGest expire le " + dateText
                + ". Renouvelez-le avant cette date pour éviter toute interruption.";

        membershipRepository.findBySchoolIdAndActiveTrueOrderByUser_FullName(school.getId()).stream()
                .filter(membership -> membership.getRole() == UserRole.FONDATEUR)
                .map(SchoolMembership::getUser)
                .filter(User::isActive)
                .forEach(founder -> saveIfMissing(school, founder, message, dateText));
    }

    private void saveIfMissing(School school, User founder, String message, String dateText) {
        if (notificationRepository.existsBySchool_IdAndRecipient_IdAndTypeAndMessageContaining(
                school.getId(), founder.getId(), REMINDER_TYPE, dateText)) {
            return;
        }
        AppNotification notification = new AppNotification();
        notification.setSchool(school);
        notification.setRecipient(founder);
        notification.setTitle("Abonnement bientôt expiré");
        notification.setMessage(message);
        notification.setTimestamp(LocalDateTime.now());
        notification.setRead(false);
        notification.setType(REMINDER_TYPE);
        notificationRepository.save(notification);
    }
}
