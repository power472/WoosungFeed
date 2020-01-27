package com.woosung.contacts;

import android.util.Log;

import org.json.JSONObject;

public class User {

    private final String mUserId;
    private final String mUserName;
    private final String mCellPhone;
    private final String mRank;
    private final String mDept;
    private final String mGroup;
    private final String mMail;
    private final String mAddr;
    private final boolean mDeleted;

    public String getUserId() { return mUserId; }

    public String getName() {
        return mUserName;
    }

    public String getRank() {
        return mRank;
    }

    public String getDept() {
        return mDept;
    }

    public String getGroup() {
        return mGroup;
    }

    public String getCellPhone() {
        return mCellPhone;
    }

    public String getEmail() { return mMail; }

    public String getAddr() { return mAddr; }

    public boolean isDeleted() {
        return mDeleted;
    }

    public User(String userId, String name, String cellPhone, String rank, String dept, String group, String mail, String addr,
                Boolean deleted) {

        mUserId = userId;
        mUserName = name;
        mCellPhone = cellPhone;
        mRank = rank;
        mDept = dept;
        mGroup = group;
        mMail = mail;
        mAddr = addr;
        mDeleted = deleted;

    }

    /**
     * Creates and returns an instance of the user from the provided JSON data.
     *
     * @param user The JSONObject containing user data
     * @return user The new instance of Voiper user created from the JSON data.
     */
    public static User valueOf(JSONObject user) {
        try {

            final String userId = user.getString("EMPLCODE");
            final String userName = user.getString("EMPLNAME");
            final String cellPhone = user.has("SELPHONE") ? user.getString("SELPHONE") : null;
            final String rank = user.has("RANKNM") ? user.getString("RANKNM") : null;
            final String dept = user.has("DEPTNM") ? user.getString("DEPTNM") : null;
            final String group = user.has("GROUPNM") ? user.getString("GROUPNM") : null;
            boolean deleted = user.has("INOFFICE") ? (user.getString("INOFFICE").equals("Y") ? false : true) : false;
//            deleted = true;
            final String mail = user.has("EMAIL") ? user.getString("EMAIL") : null;
            final String addr = user.has("ADDRESS") ? user.getString("ADDRESS") : null;

            return new User(userId, userName, cellPhone, rank, dept, group, mail, addr, deleted);

        } catch (final Exception ex) {
            Log.i("User", "Error parsing JSON user object" + ex.toString());

        }
        return null;

    }

    /**
     * Represents the User's status messages
     *
     */
    public static class Status {
        private final String mUserId;
        private final String mStatus;

        public String getUserId() {
            return mUserId;
        }

        public String getStatus() {
            return mStatus;
        }

        public Status(String userId, String status) {
            mUserId = userId;
            mStatus = status;
        }

        public static User.Status valueOf(JSONObject userStatus) {
            try {
                final String userId = userStatus.getString("i");
                final String status = userStatus.getString("s");
                return new User.Status(userId, status);
            } catch (final Exception ex) {
                Log.i("User.Status", "Error parsing JSON user object");
            }
            return null;
        }
    }

}
