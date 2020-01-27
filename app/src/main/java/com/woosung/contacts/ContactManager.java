package com.woosung.contacts;

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Settings;
import android.util.Log;

import com.woosung.Constants;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Class for managing contacts sync related mOperations
 */
public class ContactManager {
    /**
     * Custom IM protocol used when storing status messages.
     */
    public static final String CUSTOM_IM_PROTOCOL = "SampleSyncAdapter";
    private static final String TAG = "ContactManager";

    /**
     * Synchronize raw contacts
     *
     * @param context The context of Authenticator Activity
     * @param users The list of users
     */
    public static synchronized void syncContacts(Context context, List<User> users) {
        String userId;
        long rawContactId = 0;
        final ContentResolver resolver = context.getContentResolver();
        final BatchOperation batchOperation =
                new BatchOperation(context, resolver);
        Log.d(TAG, "syncContacts");
        for (final User user : users) {
            userId = user.getUserId();
            // Check to see if the contact needs to be inserted or updated
            rawContactId = lookupRawContact(resolver, userId);
            if (rawContactId != 0) {
                if (!user.isDeleted()) {
                    // update contact
                    Log.d(TAG, "update "+user.getName());
                    updateContact(context, resolver, user,
                            rawContactId, batchOperation);
                } else {
                    // delete contact
                    Log.d(TAG, "delete "+user.getName());
                    deleteContact(context, rawContactId, batchOperation);
                }
            } else {
                // add new contact
                if (!user.isDeleted()) {
                    Log.d(TAG, "add "+user.getName());
                    addContact(context, user, batchOperation);
                }else{
                    Log.d(TAG, "already none "+user.getName());
                }
            }
            // A sync adapter should batch operations on multiple contacts,
            // because it will make a dramatic performance difference.
            if (batchOperation.size() >= 50) {
                batchOperation.execute();
            }
        }
        batchOperation.execute();
    }




    /**
     * Adds a single contact to the platform contacts provider.
     *
     * @param context the Authenticator Activity context
     * @param user the sample SyncAdapter User object
     */
    private static void addContact(Context context, User user, BatchOperation batchOperation) {
        // Put the data in the contacts provider

        final ContactOperations contactOp =
                ContactOperations.createNewContact(context, user.getUserId(),
                        batchOperation);
        contactOp.addName(user.getName(), user.getRank())
                .addEmail(user.getEmail(), Email.TYPE_WORK)
                .addPhone(user.getCellPhone(), Phone.TYPE_MOBILE)
                .addAddress(user.getAddr(), StructuredPostal.TYPE_WORK)
                .addOrganization(user.getRank(), user.getDept(), Organization.TYPE_WORK);
    }

    /**
     * Updates a single contact to the platform contacts provider.
     *
     * @param context the Authenticator Activity context
     * @param resolver the ContentResolver to use
     * @param user the sample SyncAdapter contact object.
     * @param rawContactId the unique Id for this rawContact in contacts
     *        provider
     */
    private static void updateContact(Context context,
                                      ContentResolver resolver,
                                      User user,
                                      long rawContactId,
                                      BatchOperation batchOperation) {

        Uri uri;
        String cellPhone = null;
        String email = null;
        String address;
        String dept;
        String rank;


        final Cursor c =
                resolver.query(Data.CONTENT_URI, DataQuery.PROJECTION,
                        DataQuery.SELECTION,
                        new String[] {String.valueOf(rawContactId)}, null);
        final ContactOperations contactOp =
                ContactOperations.updateExistingContact(context, rawContactId,
                        batchOperation);

        try {
            while (c.moveToNext()) {
                final long id = c.getLong(DataQuery.COLUMN_ID);
                final String mimeType = c.getString(DataQuery.COLUMN_MIMETYPE);
                uri = ContentUris.withAppendedId(Data.CONTENT_URI, id);

                if (mimeType.equals(StructuredName.CONTENT_ITEM_TYPE)) {
                    final String name =
                            c.getString(DataQuery.COLUMN_GIVEN_NAME);
                    contactOp.updateName(uri, name, user.getName(), user.getRank());


                }else if (mimeType.equals(Phone.CONTENT_ITEM_TYPE)) {
                    final int type = c.getInt(DataQuery.COLUMN_PHONE_TYPE);
                    if (type == Phone.TYPE_MOBILE) {
                        cellPhone = c.getString(DataQuery.COLUMN_PHONE_NUMBER);
                        contactOp.updatePhone(cellPhone, user.getCellPhone(), uri);
                    }


                }else if (Data.MIMETYPE.equals(Email.CONTENT_ITEM_TYPE)) {
                    final int type = c.getInt(DataQuery.COLUMN_EMAIL_TYPE);
                    if (type == Email.TYPE_WORK) {
                        email = c.getString(DataQuery.COLUMN_EMAIL_ADDRESS);
                        contactOp.updateEmail( email, user.getEmail(), uri);
                    }


                }else if (Data.MIMETYPE.equals(StructuredPostal.CONTENT_ITEM_TYPE)) {
                    final int type = c.getInt(DataQuery.COLUMN_ADDRESS_TYPE);
                    if (type == StructuredPostal.TYPE_WORK) {
                        address = c.getString(DataQuery.COLUMN_ADDRESS);
                        contactOp.updateAddress( address, user.getAddr(), uri);
                    }


                }else if (Data.MIMETYPE.equals(Organization.CONTENT_ITEM_TYPE)) {
                    final int type = c.getInt(DataQuery.COLUMN_ORGANIZATION_TYPE);
                    if (type == Organization.TYPE_WORK) {
                        dept = c.getString(DataQuery.COLUMN_DEPART);
                        rank = c.getString(DataQuery.COLUMN_RANK);
                        contactOp.updateOrganization( rank, user.getRank(), dept, user.getDept(), uri);
                    }




                }
            } // while
        } finally {
            c.close();
        }

        // Add the cell phone, if present and not updated above
        if (cellPhone == null) {
            contactOp.addPhone(user.getCellPhone(), Phone.TYPE_MOBILE);
        }

        // Add the email address, if present and not updated above
        if (email == null) {
            contactOp.addEmail(user.getEmail(), Email.TYPE_WORK);
        }

    }

    /**
     * Deletes a contact from the platform contacts provider.
     *
     * @param context the Authenticator Activity context
     * @param rawContactId the unique Id for this rawContact in contacts
     *        provider
     */
    private static void deleteContact(Context context, long rawContactId,
                                      BatchOperation batchOperation) {
        batchOperation.add(ContactOperations.newDeleteCpo(
                ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId),
                true).build());
    }







    /**
     * writeDisplayPhoto
     *
     * @param context
     * @param userId the contact id
     * @param photo        the photo bytes
     *                     https://code.google.com/p/android/issues/detail?id=73499
     *                     https://forums.bitfire.at/topic/342/transactiontoolargeexception-when-syncing-contacts-with-high-res-images/5
     *                     http://developer.android.com/reference/android/provider/ContactsContract.RawContacts.DisplayPhoto.html
     */
    public static void writeDisplayPhoto(Context context, String userId, byte[] photo) throws IOException {
        long rawContactId = lookupRawContact(context.getContentResolver(), userId);
        if(rawContactId>0){
            Uri rawContactPhotoUri = Uri.withAppendedPath(
                    ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId),
                    RawContacts.DisplayPhoto.CONTENT_DIRECTORY);
            AssetFileDescriptor fd = context.getContentResolver().openAssetFileDescriptor(rawContactPhotoUri, "rw");
            OutputStream os = fd.createOutputStream();
            os.write(photo);
            os.close();
            fd.close();
        }
    }







    /**
     * When we first add a sync adapter to the system, the contacts from that
     * sync adapter will be hidden unless they're merged/grouped with an existing
     * contact.  But typically we want to actually show those contacts, so we
     * need to mess with the Settings table to get them to show up.
     *
     * @param context the Authenticator Activity context
     * @param account the Account who's visibility we're changing
     * @param visible true if we want the contacts visible, false for hidden
     */
    public static void setAccountContactsVisibility(Context context, Account account,
                                                    boolean visible) {
        ContentValues values = new ContentValues();
        values.put(RawContacts.ACCOUNT_NAME, account.name);
        values.put(RawContacts.ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        values.put(Settings.UNGROUPED_VISIBLE, visible ? 1 : 0);

        context.getContentResolver().insert(Settings.CONTENT_URI, values);
    }




    public static void deleteAllContacts(Context context) {
        long authorId = 0;
        ContentResolver resolver = context.getContentResolver();
        final Cursor c =
                resolver.query(RawContacts.CONTENT_URI, AllQuery.PROJECTION,
                        AllQuery.SELECTION, null, null);

        try {
            final BatchOperation batchOperation = new BatchOperation(context, resolver);
            while (c.moveToNext()) {
                authorId = c.getLong(AllQuery.COLUMN_ID);
                batchOperation.add(ContactOperations.newDeleteCpo(
                        ContentUris.withAppendedId(RawContacts.CONTENT_URI, authorId),
                        true).build());

                if (batchOperation.size() >= 50) {
                    batchOperation.execute();
                }
            }
            batchOperation.execute();

        } finally {
            if (c != null) {
                c.close();
            }
        }
    }




    /**
     * Returns the RawContact id for a sample SyncAdapter contact, or 0 if the
     * sample SyncAdapter user isn't found.
     *
     * @param resolver the Authenticator Activity context
     * @param userId the sample SyncAdapter user ID to lookup
     * @return the RawContact id, or 0 if not found
     */
    private static long lookupRawContact(ContentResolver resolver, String userId) {
        long authorId = 0;
        final Cursor c =
                resolver.query(RawContacts.CONTENT_URI, UserIdQuery.PROJECTION,
                        UserIdQuery.SELECTION, new String[] {userId},
                        null);
        try {
            if (c.moveToFirst()) {
                authorId = c.getLong(UserIdQuery.COLUMN_ID);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return authorId;
    }

    /**
     * Returns the Data id for a sample SyncAdapter contact's profile row, or 0
     * if the sample SyncAdapter user isn't found.
     *
     * @param resolver a content resolver
     * @param userId the sample SyncAdapter user ID to lookup
     * @return the profile Data row id, or 0 if not found
     */
    private static long lookupProfile(ContentResolver resolver, String userId) {
        long profileId = 0;
        final Cursor c =
                resolver.query(Data.CONTENT_URI, ProfileQuery.PROJECTION,
                        ProfileQuery.SELECTION, new String[] {userId},
                        null);
        try {
            if (c != null && c.moveToFirst()) {
                profileId = c.getLong(ProfileQuery.COLUMN_ID);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return profileId;
    }

    /**
     * Constants for a query to find a contact given a sample SyncAdapter user
     * ID.
     */
    private interface ProfileQuery {
        public final static String[] PROJECTION = new String[] {Data._ID};

        public final static int COLUMN_ID = 0;

        public static final String SELECTION =
                Data.MIMETYPE + "='" + ContactSyncAdapterColumns.MIME_PROFILE
                        + "' AND " + ContactSyncAdapterColumns.DATA_PID + "=?";
    }
    /**
     * Constants for a query to find a contact given a sample SyncAdapter user
     * ID.
     */
    private interface UserIdQuery {
        public final static String[] PROJECTION =
                new String[] {RawContacts._ID};

        public final static int COLUMN_ID = 0;

        public static final String SELECTION =
                RawContacts.ACCOUNT_TYPE + "='" + Constants.ACCOUNT_TYPE + "' AND "
                        + RawContacts.SOURCE_ID + "=?";
    }


    private interface AllQuery {
        public final static String[] PROJECTION =
                new String[] {RawContacts._ID};

        public final static int COLUMN_ID = 0;

        public static final String SELECTION =
                RawContacts.ACCOUNT_TYPE + "='" + Constants.ACCOUNT_TYPE + "'";
    }


    /**
     * Constants for a query to get contact data for a given rawContactId
     */
    private interface DataQuery {
        public static final String[] PROJECTION =
                new String[] {Data._ID, Data.MIMETYPE, Data.DATA1, Data.DATA2, Data.DATA3, Data.DATA4, Data.DATA5,};

        public static final int COLUMN_ID = 0;
        public static final int COLUMN_MIMETYPE = 1;
        public static final int COLUMN_DATA1 = 2;
        public static final int COLUMN_DATA2 = 3;
        public static final int COLUMN_DATA3 = 4;
        public static final int COLUMN_DATA4 = 5;
        public static final int COLUMN_DATA5 = 6;

        public static final int COLUMN_PHONE_NUMBER = COLUMN_DATA1;
        public static final int COLUMN_PHONE_TYPE = COLUMN_DATA2;

        public static final int COLUMN_EMAIL_ADDRESS = COLUMN_DATA1;
        public static final int COLUMN_EMAIL_TYPE = COLUMN_DATA2;

        public static final int COLUMN_GIVEN_NAME = COLUMN_DATA2;
        public static final int COLUMN_FAMILY_NAME = COLUMN_DATA3;

        public static final int COLUMN_ADDRESS = COLUMN_DATA1;
        public static final int COLUMN_ADDRESS_TYPE = COLUMN_DATA2;


        public static final int COLUMN_COMPANY = COLUMN_DATA1;
        public static final int COLUMN_RANK = COLUMN_DATA4;
        public static final int COLUMN_DEPART = COLUMN_DATA5;
        public static final int COLUMN_ORGANIZATION_TYPE = COLUMN_DATA2;




        public static final String SELECTION = Data.RAW_CONTACT_ID + "=?";
    }
}
