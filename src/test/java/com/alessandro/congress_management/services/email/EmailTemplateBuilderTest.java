package com.alessandro.congress_management.services.email;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailTemplateBuilderTest {

    private EmailTemplateBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new EmailTemplateBuilder();
    }

    //-------------------- TESTS FOR BUILDING WELCOME EMAILS --------------------

    @Test
    void buildWelcomeEmail_containsFullName() {
        String html = builder.buildWelcomeEmail("Ana López", "ana.lopez", "secret123", "Congress Administrator");
        assertThat(html).contains("Ana López");
    }

    @Test
    void buildWelcomeEmail_containsUsername() {
        String html = builder.buildWelcomeEmail("Ana López", "ana.lopez", "secret123", "Congress Administrator");
        assertThat(html).contains("ana.lopez");
    }

    @Test
    void buildWelcomeEmail_containsPassword() {
        String html = builder.buildWelcomeEmail("Ana López", "ana.lopez", "secret123", "Congress Administrator");
        assertThat(html).contains("secret123");
    }

    @Test
    void buildWelcomeEmail_containsReason() {
        String html = builder.buildWelcomeEmail("Ana López", "ana.lopez", "secret123", "Invited Speaker");
        assertThat(html).contains("Invited Speaker");
    }

    @Test
    void buildWelcomeEmail_containsSecurityWarning() {
        String html = builder.buildWelcomeEmail("Ana López", "ana.lopez", "secret123", "Any reason");
        assertThat(html).containsIgnoringCase("change your password");
    }

    @Test
    void buildWelcomeEmail_isValidHtmlStructure() {
        String html = builder.buildWelcomeEmail("Ana López", "ana.lopez", "secret123", "Any");
        assertThat(html).contains("<!DOCTYPE html>")
                .contains("<body")
                .contains("</body>")
                .contains("</html>");
    }

    @Test
    void buildWelcomeEmail_containsCredentialLabels() {
        String html = builder.buildWelcomeEmail("Ana López", "ana.lopez", "secret123", "Any");
        assertThat(html).containsIgnoringCase("Username")
                .containsIgnoringCase("Password");
    }

    //--------------------- TESTS FOR BUILDING CONGRESS ADMIN EMAILS ---------------------

    @Test
    void buildCongressAdminEmail_containsFullName() {
        String html = builder.buildCongressAdminEmail("Carlos Ruiz", "Tech Congress 2026");
        assertThat(html).contains("Carlos Ruiz");
    }

    @Test
    void buildCongressAdminEmail_containsCongressName() {
        String html = builder.buildCongressAdminEmail("Carlos Ruiz", "Tech Congress 2026");
        assertThat(html).contains("Tech Congress 2026");
    }

    @Test
    void buildCongressAdminEmail_mentionsAdminRole() {
        String html = builder.buildCongressAdminEmail("Carlos Ruiz", "Tech Congress 2026");
        assertThat(html).containsIgnoringCase("Congress Administrator");
    }

    @Test
    void buildCongressAdminEmail_describesResponsibilities() {
        String html = builder.buildCongressAdminEmail("Carlos Ruiz", "Tech Congress 2026");
        assertThat(html).containsIgnoringCase("manage");
    }

    @Test
    void buildCongressAdminEmail_isValidHtmlStructure() {
        String html = builder.buildCongressAdminEmail("Carlos Ruiz", "Tech Congress 2026");
        assertThat(html).contains("<!DOCTYPE html>").contains("</html>");
    }

    //-------------TESTS FOR BUILDING SCIENTIFIC COMMITTEE EMAILS ---------------------

    @Test
    void buildScientificCommitteeEmail_containsFullName() {
        String html = builder.buildScientificCommitteeEmail("Dr. María García", "AI Summit 2026");
        assertThat(html).contains("Dr. María García");
    }

    @Test
    void buildScientificCommitteeEmail_containsCongressName() {
        String html = builder.buildScientificCommitteeEmail("Dr. María García", "AI Summit 2026");
        assertThat(html).contains("AI Summit 2026");
    }

    @Test
    void buildScientificCommitteeEmail_mentionsScientificCommittee() {
        String html = builder.buildScientificCommitteeEmail("Dr. María García", "AI Summit 2026");
        assertThat(html).containsIgnoringCase("Scientific Committee");
    }

    @Test
    void buildScientificCommitteeEmail_describesReviewRole() {
        String html = builder.buildScientificCommitteeEmail("Dr. María García", "AI Summit 2026");
        assertThat(html).containsIgnoringCase("review").containsIgnoringCase("evaluate");
    }

    @Test
    void buildScientificCommitteeEmail_isValidHtmlStructure() {
        String html = builder.buildScientificCommitteeEmail("Dr. María García", "AI Summit 2026");
        assertThat(html).contains("<!DOCTYPE html>").contains("</html>");
    }

   //--------------- TESTS FOR BUILDING PRESENTER ACCEPTED EMAILS ---------------------

    @Test
    void buildPresenterAcceptedEmail_containsCoreData() {
        String html = builder.buildPresenterAcceptedEmail(
                "Luis Torres", "Tech Congress 2026", "Machine Learning 101", "PONENCIA", false);
        assertThat(html)
                .contains("Luis Torres")
                .contains("Tech Congress 2026")
                .contains("Machine Learning 101");
    }

    @Test
    void buildPresenterAcceptedEmail_containsPonenciaType() {
        String html = builder.buildPresenterAcceptedEmail(
                "Luis Torres", "Congress", "My Talk", "PONENCIA", false);
        assertThat(html).contains("PONENCIA");
    }

    @Test
    void buildPresenterAcceptedEmail_containsTallerType() {
        String html = builder.buildPresenterAcceptedEmail(
                "Luis Torres", "Congress", "My Workshop", "TALLER", false);
        assertThat(html).contains("TALLER");
    }

    @Test
    void buildPresenterAcceptedEmail_showsInvitedSpeakerLabel() {
        String html = builder.buildPresenterAcceptedEmail(
                "Luis Torres", "Congress", "Keynote", "PONENCIA", true);
        assertThat(html).containsIgnoringCase("Invited Speaker");
    }

    @Test
    void buildPresenterAcceptedEmail_showsPresenterLabel() {
        String html = builder.buildPresenterAcceptedEmail(
                "Luis Torres", "Congress", "Talk", "PONENCIA", false);
        assertThat(html).containsIgnoringCase("Presenter");
    }

    @Test
    void buildPresenterAcceptedEmail_includesInvitedNote() {
        String html = builder.buildPresenterAcceptedEmail(
                "Luis Torres", "Congress", "Keynote", "PONENCIA", true);
        assertThat(html).containsIgnoringCase("submission process");
    }

    @Test
    void buildPresenterAcceptedEmail_doesNotIncludeInvitedNote() {
        String html = builder.buildPresenterAcceptedEmail(
                "Luis Torres", "Congress", "Talk", "PONENCIA", false);
        assertThat(html).doesNotContain("submission process");
    }

    @Test
    void buildPresenterAcceptedEmail_isValidHtmlStructure() {
        String html = builder.buildPresenterAcceptedEmail(
                "Luis Torres", "Congress", "Talk", "TALLER", false);
        assertThat(html).contains("<!DOCTYPE html>").contains("</html>");
    }
}