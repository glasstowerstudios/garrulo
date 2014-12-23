package com.glasstowerstudios.garrulo.mock;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.test.InstrumentationTestCase;

/**
 * Test to verify that our mock objects are set up correctly.
 */
public class TestMockSetup extends InstrumentationTestCase {
    private static final String PHONE_NUMBER = "+12345678910";
    private static final String PHONE_NUMBER2 = "+19999999999";

    public void testMockCursor() {
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(PHONE_NUMBER));
        ContentResolver cr = new MockContactsContentResolver();
        Cursor cursor = cr.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        assertNotNull(cursor);

        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        assertEquals("Scott Johnson", name);

        Uri uri2 = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(PHONE_NUMBER2));
        Cursor cursor2 = cr.query(uri2, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        assertNull(cursor2.getString(cursor2.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME)));
    }
}
