package com.glasstowerstudios.garrulo.mock;

import android.provider.ContactsContract;

/**
 * Mock {@link android.database.Cursor} object for testing Contacts API, when actual on-phone
 * Contacts database should not be used.
 */
public class MockContactsCursor extends android.test.mock.MockCursor {
    private String mName;

    public MockContactsCursor(String aName) {
        mName = aName;
    }

    @Override
    public void close() {
        // No need to really do anything...
    }

    public int getCount() {
        if (mName != null) {
            return 1;
        }

        return 0;
    }

    @Override
    public String getString(int aColumn) {
        if (aColumn == 0) {
            return mName;
        }

        return null;
    }

    @Override
    public int getColumnIndex(String aName) {
        if (aName.equals(ContactsContract.PhoneLookup.DISPLAY_NAME)) {
            return 0;
        }

        return -1;
    }

    @Override
    public boolean isClosed() {
        return true;
    }

    @Override
    public boolean moveToFirst() {
        if (getCount() >= 1) {
            return true;
        }

        return false;
    }
}
