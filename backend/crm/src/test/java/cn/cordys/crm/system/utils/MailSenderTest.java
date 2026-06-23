package cn.cordys.crm.system.utils;

import cn.cordys.common.exception.GenericException;
import cn.cordys.crm.system.dto.response.EmailDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class MailSenderTest {

    private final MailSender mailSender = new MailSender();

    @Test
    void shouldRejectMissingPort() {
        EmailDTO email = new EmailDTO();

        assertThrows(GenericException.class, () -> mailSender.buildMailSender(email));
    }

    @Test
    void shouldRejectNonNumericPort() {
        EmailDTO email = new EmailDTO();
        email.setPort("smtp");

        assertThrows(GenericException.class, () -> mailSender.buildMailSender(email));
    }
}
