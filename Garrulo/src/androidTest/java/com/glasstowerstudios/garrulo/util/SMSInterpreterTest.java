package com.glasstowerstudios.garrulo.util;

import android.content.ContentResolver;
import android.test.InstrumentationTestCase;

import com.glasstowerstudios.garrulo.mock.MockContactsContentResolver;

/**
 * Test case for {@link com.glasstowerstudios.garrulo.util.SMSInterpreter}.
 */
public class SMSInterpreterTest extends InstrumentationTestCase {

    private static final String VALID_PHONE_NUMBER = "+12345678910";
    private static final String VALID_INTL_PHONE = "+41446681800";

    public void testPhoneNumberResolution() {
        // First, test to make sure that a phone number that resolves to a name is correct.
        SMSInterpreter interpreter = new SMSInterpreter();
        ContentResolver cr = new MockContactsContentResolver();

        String name = interpreter.getContactName(VALID_PHONE_NUMBER, cr);
        assertEquals("Scott Johnson", name);

        String phoneNumber = interpreter.getSpeakableForm(VALID_PHONE_NUMBER);
        assertEquals("(234) 567-8910", phoneNumber);

        String intlPhoneNumber = interpreter.getSpeakableForm(VALID_INTL_PHONE);
        assertEquals("+41 44 668 18 00", intlPhoneNumber);
    }
}
