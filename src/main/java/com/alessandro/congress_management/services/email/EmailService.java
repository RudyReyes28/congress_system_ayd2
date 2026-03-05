package com.alessandro.congress_management.services.email;

public interface EmailService {


    void sendWelcomeEmail(String to, String fullName, String username, String rawPassword, String reason);

    void sendCongressAdminAssignmentEmail(String to, String fullName, String congressName);

    void sendScientificCommitteeEmail(String to, String fullName, String congressName);

    void sendPresenterAcceptedEmail(String to, String fullName, String congressName,
                                    String activityName, String activityType, boolean isInvited);

    void sendSubmissionCancelledEmail(String to, String fullName,
                                      String congressName,
                                      String activityName,
                                      String activityType);

    void sendSubmissionEvaluationEmail(String to, String fullName, String congressName,
                                   String activityName, String activityType,
                                   boolean isAccepted, String comments);
}