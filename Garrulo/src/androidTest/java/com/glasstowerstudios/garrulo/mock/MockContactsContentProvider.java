package com.glasstowerstudios.garrulo.mock;

import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.test.mock.MockContentProvider;

import java.util.HashMap;

/**
 * Mock {@link android.content.ContentProvider} for Contacts API when actual on-phone Contacts
 * database should not be used.
 */
public class MockContactsContentProvider extends MockContentProvider {
    private HashMap<String, String> mNames;

    public MockContactsContentProvider() {
        mNames = new HashMap<>();
        mNames.put("+12345678910", "Scott Johnson");
    }

    @Override
    public Cursor query(Uri aUri,
                        String[] aProjection,
                        String aSelection,
                        String[] aSelectionArgs,
                        String aSortOrder) {

        String number = Uri.decode(aUri.getLastPathSegment());
        if (aProjection.length == 1
            && aProjection[0].equals(ContactsContract.PhoneLookup.DISPLAY_NAME)) {
            return new MockContactsCursor(mNames.get(number));
        }

        return new MockContactsCursor(null);
    }
}
