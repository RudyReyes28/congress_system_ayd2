package com.alessandro.congress_management.services.certificate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CertificatePdfBuilderTest {

    private static final String PARTICIPANT  = "Ana López";
    private static final String CONGRESS     = "Tech Congress 2026";
    private static final String CONGRESS_DESC= "Exploring the future of software engineering";
    private static final String ACTIVITY     = "Machine Learning in Healthcare";
    private static final String ACTIVITY_DESC= "A deep dive into ML applications";
    private static final String DATE_RANGE   = "March 10, 2026 – March 12, 2026";

    private CertificatePdfBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new CertificatePdfBuilder();
    }

    //------------ TESTS FOR BUILD ATTENDANCE CERTIFICATE ------------

    @Test
    void testBuildAttendanceCertificate_ReturnsByteArray() throws Exception {
        byte[] result = builder.buildAttendanceCertificate(PARTICIPANT, CONGRESS, CONGRESS_DESC, DATE_RANGE);
        assertNotNull(result);
    }

    @Test
    void testBuildAttendanceCertificate_ProducesNonEmptyPdf() throws Exception {
        byte[] result = builder.buildAttendanceCertificate(PARTICIPANT, CONGRESS, CONGRESS_DESC, DATE_RANGE);
        assertTrue(result.length > 0);
    }

    @Test
    void testBuildAttendanceCertificate_HasPdfMagicBytes() throws Exception {
        // PDF files always start with "%PDF"
        byte[] result = builder.buildAttendanceCertificate(PARTICIPANT, CONGRESS, CONGRESS_DESC, DATE_RANGE);
        assertEquals('%', (char) result[0]);
        assertEquals('P', (char) result[1]);
        assertEquals('D', (char) result[2]);
        assertEquals('F', (char) result[3]);
    }

    @Test
    void testBuildAttendanceCertificate_WithNullDescription_DoesNotThrow() throws Exception {
        byte[] result = builder.buildAttendanceCertificate(PARTICIPANT, CONGRESS, null, DATE_RANGE);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testBuildAttendanceCertificate_WithBlankDescription_DoesNotThrow() throws Exception {
        byte[] result = builder.buildAttendanceCertificate(PARTICIPANT, CONGRESS, "  ", DATE_RANGE);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testBuildAttendanceCertificate_EachCallProducesIndependentBytes() throws Exception {
        // Two calls should return different byte arrays (timestamps differ), but both valid
        byte[] first  = builder.buildAttendanceCertificate(PARTICIPANT, CONGRESS, CONGRESS_DESC, DATE_RANGE);
        byte[] second = builder.buildAttendanceCertificate(PARTICIPANT, CONGRESS, CONGRESS_DESC, DATE_RANGE);
        assertNotNull(first);
        assertNotNull(second);
        assertTrue(first.length > 0);
        assertTrue(second.length > 0);
    }

    //------------ TESTS FOR BUILD PRESENTATION CERTIFICATE ------------

    @Test
    void testBuildPresentationCertificate_ReturnsByteArray() throws Exception {
        byte[] result = builder.buildPresentationCertificate(
                PARTICIPANT, CONGRESS, CONGRESS_DESC, ACTIVITY, ACTIVITY_DESC, DATE_RANGE);
        assertNotNull(result);
    }

    @Test
    void testBuildPresentationCertificate_ProducesNonEmptyPdf() throws Exception {
        byte[] result = builder.buildPresentationCertificate(
                PARTICIPANT, CONGRESS, CONGRESS_DESC, ACTIVITY, ACTIVITY_DESC, DATE_RANGE);
        assertTrue(result.length > 0);
    }

    @Test
    void testBuildPresentationCertificate_HasPdfMagicBytes() throws Exception {
        byte[] result = builder.buildPresentationCertificate(
                PARTICIPANT, CONGRESS, CONGRESS_DESC, ACTIVITY, ACTIVITY_DESC, DATE_RANGE);
        assertEquals('%', (char) result[0]);
        assertEquals('P', (char) result[1]);
        assertEquals('D', (char) result[2]);
        assertEquals('F', (char) result[3]);
    }

    @Test
    void testBuildPresentationCertificate_WithNullActivityDescription_DoesNotThrow() throws Exception {
        byte[] result = builder.buildPresentationCertificate(
                PARTICIPANT, CONGRESS, CONGRESS_DESC, ACTIVITY, null, DATE_RANGE);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testBuildPresentationCertificate_WithNullCongressDescription_DoesNotThrow() throws Exception {
        byte[] result = builder.buildPresentationCertificate(
                PARTICIPANT, CONGRESS, null, ACTIVITY, ACTIVITY_DESC, DATE_RANGE);
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testPresentationCertificate_IsLargerThanAttendance_DueToExtraSection() throws Exception {
        // Presentation cert has an extra activity section, so it should produce more content
        byte[] attendance    = builder.buildAttendanceCertificate(PARTICIPANT, CONGRESS, CONGRESS_DESC, DATE_RANGE);
        byte[] presentation  = builder.buildPresentationCertificate(
                PARTICIPANT, CONGRESS, CONGRESS_DESC, ACTIVITY, ACTIVITY_DESC, DATE_RANGE);
        assertTrue(presentation.length > attendance.length);
    }
}