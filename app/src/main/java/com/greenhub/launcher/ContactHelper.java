
package com.greenhub.launcher;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

public class ContactHelper {

    public static String getContactNameByNumber(Context context, String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return null;
        }

        ContentResolver cr = context.getContentResolver();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] projection = new String[]{
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        };
        
        String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?";
        String[] selectionArgs = new String[]{phoneNumber.replaceAll("\\D", "")};

        try (Cursor cursor = cr.query(uri, projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                return cursor.getString(nameIdx);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Try partial match
        try (Cursor cursor = cr.query(uri, projection, null, null, null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    int nameIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                    int phoneIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    String number = cursor.getString(phoneIdx);
                    if (number != null) {
                        String normalized = number.replaceAll("\\D", "");
                        String queryNormalized = phoneNumber.replaceAll("\\D", "");
                        if (normalized.equals(queryNormalized) || 
                            normalized.endsWith(queryNormalized) ||
                            queryNormalized.endsWith(normalized)) {
                            return cursor.getString(nameIdx);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}
