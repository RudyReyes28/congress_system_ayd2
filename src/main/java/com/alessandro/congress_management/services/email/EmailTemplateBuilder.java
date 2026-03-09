package com.alessandro.congress_management.services.email;


public class EmailTemplateBuilder {

    private static final String PRIMARY   = "#1a73e8";
    private static final String DARK_BG   = "#0f172a";
    private static final String CARD_BG   = "#1e293b";
    private static final String TEXT_MAIN = "#f1f5f9";
    private static final String TEXT_MUTED= "#94a3b8";
    private static final String ACCENT    = "#38bdf8";
    private static final String SUCCESS   = "#34d399";
    private static final String WARNING   = "#fbbf24";


    public String buildWelcomeEmail(String fullName, String username, String rawPassword, String reason) {
        String body = """
                <p style="color:%s; font-size:16px; line-height:1.7; margin:0 0 20px">
                    Hi <strong style="color:%s">%s</strong>, welcome to <strong>Congress Management</strong>!
                    An account has been created for you.
                </p>
                <p style="color:%s; font-size:14px; margin:0 0 24px">
                    <strong style="color:%s">Reason:</strong> %s
                </p>
                %s
                <p style="color:%s; font-size:13px; margin:24px 0 0; padding:16px; background:#0f172a; border-radius:8px; border-left:3px solid %s">
                    For security reasons, please change your password after your first login.
                </p>
                """.formatted(
                TEXT_MAIN, ACCENT, fullName,
                TEXT_MUTED, TEXT_MAIN, reason,
                credentialsBlock(username, rawPassword),
                TEXT_MUTED, WARNING
        );
        return buildLayout("Your account is ready", " Account Created", body);
    }

    public String buildCongressAdminEmail(String fullName, String congressName) {
        String body = """
                <p style="color:%s; font-size:16px; line-height:1.7; margin:0 0 20px">
                    Hi <strong style="color:%s">%s</strong>,
                </p>
                <p style="color:%s; font-size:16px; line-height:1.7; margin:0 0 24px">
                    You have been assigned as <strong style="color:%s">Congress Administrator</strong>
                    for the following event:
                </p>
                %s
                <p style="color:%s; font-size:14px; line-height:1.7; margin:24px 0 0">
                    As administrator you can manage rooms, activities, presenters, the scientific committee,
                    and attendee registration for this congress.
                </p>
                """.formatted(
                TEXT_MAIN, ACCENT, fullName,
                TEXT_MAIN, SUCCESS,
                highlightBlock("Congress", congressName, SUCCESS),
                TEXT_MUTED
        );
        return buildLayout("Congress Administrator Assignment", "You're a Congress Admin", body);
    }

    public String buildScientificCommitteeEmail(String fullName, String congressName) {
        String body = """
                <p style="color:%s; font-size:16px; line-height:1.7; margin:0 0 20px">
                    Hi <strong style="color:%s">%s</strong>,
                </p>
                <p style="color:%s; font-size:16px; line-height:1.7; margin:0 0 24px">
                    You have been selected as a member of the
                    <strong style="color:%s">Scientific Committee</strong> for:
                </p>
                %s
                <p style="color:%s; font-size:14px; line-height:1.7; margin:24px 0 0">
                    As a committee member you will review and evaluate submitted papers and workshop proposals.
                    Your expertise helps ensure the quality of the congress program.
                </p>
                """.formatted(
                TEXT_MAIN, ACCENT, fullName,
                TEXT_MAIN, ACCENT,
                highlightBlock("Congress", congressName, ACCENT),
                TEXT_MUTED
        );
        return buildLayout("Scientific Committee Invitation", "🔬 Scientific Committee", body);
    }

    public String buildPresenterAcceptedEmail(String fullName, String congressName,
                                              String activityName, String activityType,
                                              boolean isInvited) {
        String roleLabel   = isInvited ? "Invited Speaker" : "Presenter";
        String roleColor   = isInvited ? WARNING : SUCCESS;
        String badgeIcon   = activityType.equalsIgnoreCase("TALLER") ? "🛠️" : "🎤";
        String headerTitle = isInvited
                ? "You've been invited as a speaker"
                : "Your submission was accepted";

        String invitedNote = isInvited ? """
                <p style="color:%s; font-size:13px; margin:0 0 20px; padding:12px 16px;
                           background:#0f172a; border-radius:8px; border-left:3px solid %s">
                    As an invited speaker you do not need to go through the submission process.
                    Your participation has been confirmed by the congress administration.
                </p>
                """.formatted(TEXT_MUTED, WARNING) : "";

        String body = """
                <p style="color:%s; font-size:16px; line-height:1.7; margin:0 0 20px">
                    Hi <strong style="color:%s">%s</strong>,
                </p>
                <p style="color:%s; font-size:16px; line-height:1.7; margin:0 0 24px">
                    Congratulations! You have been confirmed as
                    <strong style="color:%s">%s</strong> at the following congress:
                </p>
                %s
                %s
                %s
                """.formatted(
                TEXT_MAIN, ACCENT, fullName,
                TEXT_MAIN, roleColor, roleLabel,
                highlightBlock("🏛️ Congress", congressName, PRIMARY),
                highlightBlock(badgeIcon + " Activity (" + activityType + ")", activityName, roleColor),
                invitedNote
        );
        return buildLayout("Presenter Confirmation", headerTitle, body);
    }

    public String buildSubmissionCancelledEmail(String fullName,
                                                String congressName,
                                                String activityName,
                                                String activityType) {

        String headerTitle = "Your submission will not be scheduled";

        String body = """
            <p style="color:%s; font-size:16px; line-height:1.7; margin:0 0 20px">
                Hi <strong style="color:%s">%s</strong>,
            </p>

            <p style="color:%s; font-size:16px; line-height:1.7; margin:0 0 24px">
                We would like to inform you that although your submission was evaluated,
                it has not been scheduled as an activity in the congress program.
            </p>

            %s
            %s

            <div style="background:%s; border-radius:10px; padding:18px 22px;
                        border-left:4px solid %s; margin:16px 0">
                <p style="color:%s; font-size:11px; text-transform:uppercase;
                          letter-spacing:1.5px; margin:0 0 6px; font-weight:700">
                    STATUS
                </p>
                <p style="color:%s; font-size:17px; font-weight:700; margin:0">
                    Not scheduled in the congress program
                </p>
            </div>

            <p style="color:%s; font-size:14px; line-height:1.6; margin-top:24px">
                We appreciate your interest in participating in the congress and
                encourage you to submit again in future editions.
            </p>
            """.formatted(
                TEXT_MAIN, ACCENT, fullName,
                TEXT_MAIN,
                highlightBlock("🏛️ Congress", congressName, PRIMARY),
                highlightBlock("🎤 Activity (" + activityType + ")", activityName, WARNING),
                DARK_BG, WARNING,
                TEXT_MAIN,
                TEXT_MUTED,
                TEXT_MUTED
        );

        return buildLayout("Submission Update", headerTitle, body);
    }

    public String buildSubmissionEvaluationEmail(String fullName, String congressName, String activityName, String activityType, boolean isAccepted, String comments) {
            String statusLabel = isAccepted ? "Accepted" : "Rejected";
            String statusColor = isAccepted ? SUCCESS : WARNING;
            String headerTitle = isAccepted ? "Your submission was accepted" : "Your submission was rejected";

            String body = """
                    <p style="color:%s; font-size:16px; line-height:1.7; margin:0 0 20px">
                        Hi <strong style="color:%s">%s</strong>,
                    </p>
                    <p style="color:%s; font-size:16px; line-height:1.7; margin:0 0 24px">
                        We have completed the evaluation of your submission for the following congress:
                    </p>
                    %s
                    %s
                    <div style="background:%s; border-radius:10px; padding:18px 22px;
                                border-left:4px solid %s; margin:16px 0">
                        <p style="color:%s; font-size:11px; text-transform:uppercase;
                                letter-spacing:1.5px; margin:0 0 6px; font-weight:700">%s</p>
                        <p style="color:%s; font-size:17px; font-weight:700; margin:0">%s</p>
                    </div>
                    """.formatted(
                    TEXT_MAIN, ACCENT, fullName,
                    TEXT_MAIN,
                    highlightBlock("🏛️ Congress", congressName, PRIMARY),
                    highlightBlock("🎤 Activity (" + activityType + ")", activityName, statusColor),
                    DARK_BG, statusColor, TEXT_MAIN, statusLabel, TEXT_MUTED, comments != null ? comments : "No additional comments provided."
            );
            return buildLayout("Submission Evaluation Result", headerTitle, body);
    }


    private String credentialsBlock(String username, String password) {
        return """
                <table width="100%%" cellpadding="0" cellspacing="0" style="margin:0 0 8px">
                    <tr>
                        <td style="padding:8px 0">%s</td>
                    </tr>
                    <tr>
                        <td style="padding:8px 0">%s</td>
                    </tr>
                </table>
                """.formatted(
                credentialRow("Username", username, ACCENT),
                credentialRow("Password", password, WARNING)
        );
    }

    private String credentialRow(String label, String value, String valueColor) {
        return """
                <div style="display:flex; align-items:center; background:%s;
                            border-radius:8px; padding:14px 18px; margin:6px 0;">
                    <span style="color:%s; font-size:13px; min-width:110px; font-weight:600">%s</span>
                    <span style="color:%s; font-size:15px; font-family:monospace; font-weight:700;
                                 letter-spacing:0.5px; word-break:break-all">%s</span>
                </div>
                """.formatted(DARK_BG, TEXT_MUTED, label, valueColor, value);
    }

    private String highlightBlock(String label, String value, String accentColor) {
        return """
                <div style="background:%s; border-radius:10px; padding:18px 22px;
                            border-left:4px solid %s; margin:0 0 16px">
                    <p style="color:%s; font-size:11px; text-transform:uppercase;
                               letter-spacing:1.5px; margin:0 0 6px; font-weight:700">%s</p>
                    <p style="color:%s; font-size:17px; font-weight:700; margin:0">%s</p>
                </div>
                """.formatted(DARK_BG, accentColor, accentColor, label, TEXT_MAIN, value);
    }

    private String buildLayout(String previewText, String headerTitle, String bodyContent) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8"/>
                  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
                  <title>%s</title>
                </head>
                <body style="margin:0; padding:0; background-color:%s; font-family:'Segoe UI', Arial, sans-serif;">
                  <div style="display:none; max-height:0; overflow:hidden; color:%s">%s</div>

                  <!-- Wrapper -->
                  <table width="100%%" cellpadding="0" cellspacing="0"
                         style="background-color:%s; padding:40px 20px">
                    <tr>
                      <td align="center">
                        <table width="100%%" cellpadding="0" cellspacing="0"
                               style="max-width:580px; background-color:%s;
                                      border-radius:16px; overflow:hidden;
                                      box-shadow:0 20px 60px rgba(0,0,0,0.4)">

                          <!-- Header -->
                          <tr>
                            <td style="background:linear-gradient(135deg, %s 0%%, %s 100%%);
                                        padding:32px 40px">
                              <p style="margin:0 0 4px; color:rgba(255,255,255,0.7);
                                         font-size:12px; text-transform:uppercase;
                                         letter-spacing:2px; font-weight:600">
                                Congress Management
                              </p>
                              <h1 style="margin:0; color:#ffffff; font-size:22px;
                                          font-weight:700; line-height:1.3">
                                %s
                              </h1>
                            </td>
                          </tr>

                          <!-- Body -->
                          <tr>
                            <td style="padding:36px 40px">
                              %s
                            </td>
                          </tr>

                          <!-- Footer -->
                          <tr>
                            <td style="padding:20px 40px 32px; border-top:1px solid #1e293b">
                              <p style="margin:0; color:%s; font-size:12px; line-height:1.6">
                                This is an automated message. Please do not reply directly to this email.<br/>
                                If you have questions, contact your congress administrator.
                              </p>
                            </td>
                          </tr>

                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(
                previewText,
                DARK_BG, DARK_BG, previewText,   // preview text hidden span
                DARK_BG, CARD_BG,
                PRIMARY, "#0d47a1",               // header gradient
                headerTitle,
                bodyContent,
                TEXT_MUTED
        );
    }

    public String buildInvitationEmail(String fullName, String activationLink, long expiryHours) {
        return """
            <!DOCTYPE html>
            <html lang="es">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
              <title>Activación de cuenta</title>
              <style>
                body { margin:0; padding:0; background:#0f1117; font-family:'Segoe UI',Arial,sans-serif; color:#e2e8f0; }
                .wrapper { max-width:600px; margin:40px auto; background:#1a1d27; border-radius:12px; overflow:hidden; border:1px solid #2d3148; }
                .header  { background:linear-gradient(135deg,#1e3a5f,#0d2137); padding:36px 40px; text-align:center; }
                .header h1 { margin:0; font-size:22px; color:#f8fafc; letter-spacing:0.5px; }
                .header p  { margin:6px 0 0; font-size:13px; color:#94a3b8; }
                .body    { padding:36px 40px; }
                .greeting { font-size:16px; color:#f1f5f9; margin-bottom:16px; }
                .message  { font-size:14px; color:#94a3b8; line-height:1.7; margin-bottom:28px; }
                .btn-wrap { text-align:center; margin-bottom:28px; }
                .btn { display:inline-block; padding:14px 36px; background:linear-gradient(135deg,#2563eb,#1d4ed8);
                       color:#fff; text-decoration:none; border-radius:8px; font-size:15px; font-weight:600;
                       letter-spacing:0.3px; }
                .expiry { background:#1e2235; border:1px solid #2d3148; border-radius:8px;
                          padding:12px 16px; font-size:13px; color:#94a3b8; text-align:center; margin-bottom:24px; }
                .expiry span { color:#f59e0b; font-weight:600; }
                .fallback { font-size:12px; color:#64748b; word-break:break-all; text-align:center; }
                .footer  { background:#13151f; padding:20px 40px; text-align:center; font-size:12px; color:#475569; }
              </style>
            </head>
            <body>
              <div class="wrapper">
                <div class="header">
                  <h1>Sistema de Gestión de Congresos</h1>
                  <p>Bienvenido/a a la plataforma</p>
                </div>
                <div class="body">
                  <p class="greeting">Hola, <strong>%s</strong></p>
                  <p class="message">
                    Has sido registrado/a en el Sistema de Gestión de Congresos.<br/>
                    Para completar tu registro y activar tu cuenta, haz clic en el siguiente botón
                    y elige tu contraseña.
                  </p>
                  <div class="btn-wrap">
                    <a class="btn" href="%s">Activar mi cuenta</a>
                  </div>
                  <div class="expiry">
                    Este enlace expirará en <span>%d horas</span>. Si no lo usas a tiempo,
                    solicita uno nuevo al administrador.
                  </div>
                  <p class="fallback">
                    Si el botón no funciona, copia y pega este enlace en tu navegador:<br/>%s
                  </p>
                </div>
                <div class="footer">
                  Si no esperabas este correo, puedes ignorarlo con seguridad.
                </div>
              </div>
            </body>
            </html>
            """.formatted(fullName, activationLink, expiryHours, activationLink);
    }
}