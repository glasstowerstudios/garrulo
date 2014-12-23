package com.glasstowerstudios.garrulo.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.glasstowerstudios.garrulo.app.GarruloApplication;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;

/**
 * An {@link Interpreter} for {@link SmsMessage} objects.
 */
public class SMSInterpreter implements Interpreter<SmsMessage> {
    private static final String LOGTAG = SMSInterpreter.class.getSimpleName();

    @Override
    public String[] interpretMessage(SmsMessage aMessage) {
        String[] messages = new String[2];
        String phoneNumber = aMessage.getOriginatingAddress();
        String contactName = getContactNameOrNumber(phoneNumber);
        String messageFrom = "New message from " + contactName;
        String messageBody = aMessage.getMessageBody();
        messages[0] = messageFrom;
        messages[1] = messageBody;

        return messages;
    }

    /**
     * Retrieve the speakable form of a phone number.
     *
     * The speakable form of a phone number is one where the country code is removed, if the phone
     * number is in the same country as the device's locale, and split to make it more easily
     * parseable and spoken by a TextToSpeech convertor.
     *
     * @param aPhoneNumber A phone number to convert to speakable form.
     *
     * @return The speakable form of the given phone number, as a String.
     */
    public String getSpeakableForm(String aPhoneNumber) {
        String speakableForm = aPhoneNumber;
        PhoneNumberUtil util = PhoneNumberUtil.getInstance();
        TelephonyManager tMgr = (TelephonyManager)GarruloApplication.getInstance().getSystemService(Context.TELEPHONY_SERVICE);
        String myPhoneNumber = tMgr.getLine1Number();
        try {
            Phonenumber.PhoneNumber myNum = util.parse(myPhoneNumber, Locale.getDefault().getCountry());
            int myCc = myNum.getCountryCode();
            Phonenumber.PhoneNumber num = util.parse(aPhoneNumber, Locale.getDefault().getCountry());
            boolean withinCountry = myCc == num.getCountryCode();
            if (withinCountry) {
                speakableForm = util.format(num, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
            } else {
                speakableForm = util.format(num, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
            }
        } catch (NumberParseException e) {
            Log.e(LOGTAG, "Unable to parse phone number: " + aPhoneNumber, e);
        }

        return speakableForm;
    }

    /**
     * Retrieve a contact's name from the Contacts content provider, using a specific
     * {@link ContentResolver}.
     *
     * This can be used to specify which content resolver you wish to utilize for determining a
     * contact's name (e.g. if you wanted to test it using a mock).
     *
     * @param aPhoneNumber The phone number for which the name should be retrieved.
     * @param aResolver The {@link ContentResolver} to use to retrieve the data specified. If set
     *                  to null, then the default content resolver will be used.
     *
     * @return The name of the contact corresponding to the given number, if it exists; null,
     *         otherwise.
     */
    public String getContactName(String aPhoneNumber, ContentResolver aResolver) {
        if (aResolver == null) {
            aResolver = GarruloApplication.getInstance().getContentResolver();
        }

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(aPhoneNumber));
        Cursor cursor = aResolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return null;
        }

        String contactName = null;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

    /**
     * Retrieve the name associated with a phone number, or the phone number, in speakable form,
     * if no contact is associated with that number.
     *
     * @param aPhoneNumber A phone number, as a string, with or without country code.
     *
     * @return A string containing the contact's name, if a contact is associated with the phone
     *         number given; a formatted form of the phone number idealized for speaking, otherwise.
     */
    public String getContactNameOrNumber(String aPhoneNumber) {
        ContentResolver cr = GarruloApplication.getInstance().getContentResolver();
        String contactName = getContactName(aPhoneNumber, cr);

        if (contactName == null) {
            contactName = getSpeakableForm(aPhoneNumber);
        }

        return contactName;
    }
}
