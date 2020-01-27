/*
 * Copyright (C) 2010 The Android Open Source Project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.woosung.contacts;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;

import com.woosung.Constants;


/**
 * Helper class for storing data in the platform content providers.
 */
public class ContactOperations {
    private static final String TAG="ContactOperations";
    private final ContentValues mValues;
    private ContentProviderOperation.Builder mBuilder;
    private final BatchOperation mBatchOperation;
    private final Context mContext;
    private boolean mYield;
    private long mRawContactId;
    private int mBackReference;
    private boolean mIsNewContact;

    /**
     * Returns an instance of ContactOperations instance for adding new contact
     * to the platform contacts provider.
     * 
     * @param context the Authenticator Activity context
     * @param userId the userId of the sample SyncAdapter user object
     * @return instance of ContactOperations
     */
    public static ContactOperations createNewContact(Context context,
        String userId, BatchOperation batchOperation) {

        return new ContactOperations(context, userId, batchOperation);
    }

    /**
     * Returns an instance of ContactOperations for updating existing contact in
     * the platform contacts provider.
     * 
     * @param context the Authenticator Activity context
     * @param rawContactId the unique Id of the existing rawContact
     * @return instance of ContactOperations
     */
    public static ContactOperations updateExistingContact(Context context,
        long rawContactId, BatchOperation batchOperation) {
        return new ContactOperations(context, rawContactId, batchOperation);
    }

    public ContactOperations(Context context, BatchOperation batchOperation) {
        mValues = new ContentValues();
        mYield = true;
        mContext = context;
        mBatchOperation = batchOperation;
    }

    public ContactOperations(Context context, String userId,
                             BatchOperation batchOperation) {
        this(context, batchOperation);

        mBackReference = mBatchOperation.size();
        mIsNewContact = true;
        mValues.put(RawContacts.SOURCE_ID, userId);
        mValues.put(RawContacts.ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        mValues.put(RawContacts.ACCOUNT_NAME, Constants.ACCOUNT_NAME);
        mBuilder =
            newInsertCpo(RawContacts.CONTENT_URI, true).withValues(mValues);
        mBatchOperation.add(mBuilder.build());

    }












    public ContactOperations(Context context, long rawContactId,
                             BatchOperation batchOperation) {
        this(context, batchOperation);
        mIsNewContact = false;
        mRawContactId = rawContactId;
    }





    public ContactOperations addName(String name, String rank) {
        mValues.clear();
        if (!TextUtils.isEmpty(name)) {
            mValues.put(StructuredName.GIVEN_NAME, name+' '+rank+'님');
            mValues.put(StructuredName.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
        }
        if (mValues.size() > 0) addInsertOp();

        return this;
    }

    public ContactOperations updateName(Uri uri, String existingName, String name, String rank) {
        mValues.clear();
        if (!TextUtils.equals(existingName, name+' '+rank+'님')) {
            mValues.put(StructuredName.GIVEN_NAME, name+' '+rank+'님');
        }
        if (mValues.size() > 0) addUpdateOp(uri);

        return this;
    }





    public ContactOperations addEmail(String email, int emailType) {
        mValues.clear();
        if (!TextUtils.isEmpty(email)) {
            mValues.put(Email.DATA, email);
            mValues.put(Email.TYPE, emailType);
            mValues.put(Email.MIMETYPE, Email.CONTENT_ITEM_TYPE);
            addInsertOp();
        }
        return this;
    }

    public ContactOperations updateEmail(String existingEmail, String email, Uri uri) {
        if (!TextUtils.equals(existingEmail, email)) {
            mValues.clear();
            mValues.put(Email.DATA, email);
            addUpdateOp(uri);
        }
        return this;
    }






    public ContactOperations addPhone(String phone, int phoneType) {
        mValues.clear();
        if (!TextUtils.isEmpty(phone)) {
            mValues.put(Phone.NUMBER, phone);
            mValues.put(Phone.TYPE, phoneType);
            mValues.put(Phone.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
            addInsertOp();
        }
        return this;
    }

    public ContactOperations updatePhone(String existingNumber, String phone, Uri uri) {
        if (!TextUtils.equals(phone, existingNumber)) {
            mValues.clear();
            mValues.put(Phone.NUMBER, phone);
            addUpdateOp(uri);
        }
        return this;
    }



    public ContactOperations addAddress(String address, int addressType) {
        mValues.clear();
        if (!TextUtils.isEmpty(address)) {
            mValues.put(StructuredPostal.FORMATTED_ADDRESS, address);
            mValues.put(StructuredPostal.TYPE, addressType);
            mValues.put(StructuredPostal.MIMETYPE, StructuredPostal.CONTENT_ITEM_TYPE);
            addInsertOp();
        }
        return this;
    }

    public ContactOperations updateAddress(String existingAddress, String address, Uri uri) {
        if (!TextUtils.equals(existingAddress, address)) {
            mValues.clear();
            mValues.put(StructuredPostal.FORMATTED_ADDRESS, address);
            addUpdateOp(uri);
        }
        return this;
    }











    public ContactOperations addOrganization(String rank, String dept, int origanizationType) {
        mValues.clear();
        if (!TextUtils.isEmpty(rank)) {
            mValues.put(Organization.COMPANY, "(주)우성사료");
            mValues.put(Organization.DEPARTMENT, dept);
            mValues.put(Organization.TITLE, rank);
            mValues.put(Organization.TYPE, origanizationType);
            mValues.put(Organization.MIMETYPE, Organization.CONTENT_ITEM_TYPE);
            addInsertOp();
        }
        return this;
    }

    public ContactOperations updateOrganization(String existingRank, String rank, String existingDept, String dept, Uri uri) {
        if (!TextUtils.equals(existingRank, rank) || !TextUtils.equals(existingDept, dept) ) {
            mValues.clear();
            mValues.put(Organization.DEPARTMENT, dept);
            mValues.put(Organization.TITLE, rank);
            addUpdateOp(uri);
        }
        return this;
    }







    /**
     * Updates contact's profile action
     * 
     * @param userId sample SyncAdapter user id
     * @param uri Uri for the existing raw contact to be updated
     * @return instance of ContactOperations
     */
    public ContactOperations updateProfileAction(Integer userId, Uri uri) {
        mValues.clear();
        mValues.put(ContactSyncAdapterColumns.DATA_PID, userId);
        addUpdateOp(uri);
        return this;
    }

    /**
     * Adds an insert operation into the batch
     */
    private void addInsertOp() {
        if (!mIsNewContact) {
            mValues.put(Phone.RAW_CONTACT_ID, mRawContactId);
        }
        mBuilder =
            newInsertCpo(addCallerIsSyncAdapterParameter(Data.CONTENT_URI),
                mYield);
        mBuilder.withValues(mValues);
        if (mIsNewContact) {
            mBuilder
                .withValueBackReference(Data.RAW_CONTACT_ID, mBackReference);
        }
        mYield = false;
        mBatchOperation.add(mBuilder.build());
    }

    /**
     * Adds an update operation into the batch
     */
    private void addUpdateOp(Uri uri) {
        mBuilder = newUpdateCpo(uri, mYield).withValues(mValues);
        mYield = false;
        mBatchOperation.add(mBuilder.build());
    }

    public static ContentProviderOperation.Builder newInsertCpo(Uri uri,
        boolean yield) {
        return ContentProviderOperation.newInsert(
            addCallerIsSyncAdapterParameter(uri)).withYieldAllowed(yield);
    }

    public static ContentProviderOperation.Builder newUpdateCpo(Uri uri,
        boolean yield) {
        return ContentProviderOperation.newUpdate(
            addCallerIsSyncAdapterParameter(uri)).withYieldAllowed(yield);
    }

    public static ContentProviderOperation.Builder newDeleteCpo(Uri uri,
        boolean yield) {
        return ContentProviderOperation.newDelete(
            addCallerIsSyncAdapterParameter(uri)).withYieldAllowed(yield);

    }

    private static Uri addCallerIsSyncAdapterParameter(Uri uri) {
        return uri.buildUpon().appendQueryParameter(
            ContactsContract.CALLER_IS_SYNCADAPTER, "true").build();
    }

}
