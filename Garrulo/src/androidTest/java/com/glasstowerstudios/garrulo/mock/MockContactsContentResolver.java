package com.glasstowerstudios.garrulo.mock;

import android.provider.ContactsContract;
import android.test.mock.MockContentResolver;

/**
 * Mock {@link android.content.ContentResolver} for Contacts API when on-phone Contacts should not
 * be used.
 */
public class MockContactsContentResolver extends MockContentResolver {
    public MockContactsContentResolver() {
        super();
        addProvider(ContactsContract.PhoneLookup.CONTENT_FILTER_URI.getAuthority(), new MockContactsContentProvider());
    }
}