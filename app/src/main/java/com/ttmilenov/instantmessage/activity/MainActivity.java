package com.ttmilenov.instantmessage.activity;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ttmilenov.instantmessage.Common;
import com.ttmilenov.instantmessage.storage.DataProvider;
import com.ttmilenov.instantmessage.util.PhotoCache;
import com.ttmilenov.instantmessage.R;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends ActionBarActivity implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

    private AlertDialog disclaimer;
    ListView listView;
    private ActionBar actionBar;
    private ContactCursorAdapter contactCursorAdapter;
    public static PhotoCache photoCache;
    public static String[] email_arr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.contactslist);
        listView.setOnItemClickListener(this);
        contactCursorAdapter = new ContactCursorAdapter(this, null);
        listView.setAdapter(contactCursorAdapter);
        actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.show();
        photoCache = new PhotoCache(this);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME, ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setTitle(Common.APPLICATION_TITLE);
        setSubtitle();
//		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
//		
//		ArrayAdapter<CharSequence> dropdownAdapter = ArrayAdapter.createFromResource(this, R.array.dropdown_arr, android.R.layout.simple_list_item_1);
//		actionBar.setListNavigationCallbacks(dropdownAdapter, new ActionBar.OnNavigationListener() {
//			
//			@Override
//			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
//				getLoaderManager().restartLoader(0, getArgs(itemPosition), MainActivity.this);
//				return true;
//			}
//		});

        getSupportLoaderManager().initLoader(0, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                AddContactDialog newFragment = AddContactDialog.newInstance();
                newFragment.show(getSupportFragmentManager(), "AddContactDialog");
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View view, int arg2, long arg3) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(Common.PROFILE_ID, String.valueOf(arg3));
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        if (disclaimer != null)
            disclaimer.dismiss();
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = new CursorLoader(this,
                DataProvider.CONTENT_URI_PROFILE,
                new String[]{DataProvider.COL_ID, DataProvider.COL_NAME, DataProvider.COL_EMAIL, DataProvider.COL_COUNT},
                null,
                null,
                DataProvider.COL_ID + " DESC");
        return loader;
    }

    @Override
    public void onLoadFinished(android.support.v4.content.Loader<Cursor> arg0, Cursor arg1) {
        contactCursorAdapter.swapCursor(arg1);
    }

    @Override
    public void onLoaderReset(android.support.v4.content.Loader<Cursor> arg0) {
        contactCursorAdapter.swapCursor(null);
    }

    public class ContactCursorAdapter extends CursorAdapter {

        private LayoutInflater mInflater;

        public ContactCursorAdapter(Context context, Cursor c) {
            super(context, c, 0);
            this.mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return getCursor() == null ? 0 : super.getCount();
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View itemLayout = mInflater.inflate(R.layout.main_list_item, parent, false);
            ViewHolder holder = new ViewHolder();
            itemLayout.setTag(holder);
            holder.text1 = (TextView) itemLayout.findViewById(R.id.text1);
            holder.text2 = (TextView) itemLayout.findViewById(R.id.text2);
            holder.textEmail = (TextView) itemLayout.findViewById(R.id.textEmail);
            holder.avatar = (ImageView) itemLayout.findViewById(R.id.avatar);
            return itemLayout;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.text1.setText(cursor.getString(cursor.getColumnIndex(DataProvider.COL_NAME)));
            holder.textEmail.setText(cursor.getString(cursor.getColumnIndex(DataProvider.COL_EMAIL)));
            int count = cursor.getInt(cursor.getColumnIndex(DataProvider.COL_COUNT));
            if (count > 0) {
                holder.text2.setVisibility(View.VISIBLE);
                holder.text2.setText(String.format("%d new message%s", count, count == 1 ? "" : "s"));
            } else
                holder.text2.setVisibility(View.GONE);
            photoCache.DisplayBitmap(requestPhoto(cursor.getString(cursor.getColumnIndex(DataProvider.COL_EMAIL))), holder.avatar);
        }
    }

    private void setSubtitle() {
        if (getAccountsPermission()) {
            setSubtitle(getEmailList());
        }
    }

    private void setSubtitle(List<String> emailList) {
        email_arr = emailList.toArray(new String[emailList.size()]);
        actionBar.setSubtitle(Common.getPreferredEmail(email_arr));
    }

    private boolean getAccountsPermission() {
        return Common.requestPermission(this, Manifest.permission.GET_ACCOUNTS, Common.PERMISSIONS_REQUEST_GET_ACCOUNTS);
    }

    private List<String> getEmailList() {
        List<String> lst = new ArrayList<>();
        Account[] accounts = AccountManager.get(this).getAccounts();
        for (Account account : accounts) {
            if (Patterns.EMAIL_ADDRESS.matcher(account.name).matches()) {
                lst.add(account.name);
            }
        }
        return lst;
    }

    private static class ViewHolder {
        TextView text1;
        TextView text2;
        TextView textEmail;
        ImageView avatar;
    }

    @SuppressLint("InlinedApi")
    private Uri requestPhoto(String email) {
        Cursor emailCur = null;
        Uri uri = null;
        try {
            int SDK_INT = android.os.Build.VERSION.SDK_INT;
            if (SDK_INT >= 11) {
                uri = getProfileImageUriHigherVersion(emailCur, email);
            } else if (SDK_INT < 11) {
                uri = getProfileImageUriLowerVersion(emailCur, email);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (emailCur != null)
                    emailCur.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return uri;
    }

    private Uri getProfileImageUriHigherVersion(Cursor emailCur, String email) {
        Uri uri = null;
        String[] projection = {ContactsContract.CommonDataKinds.Email.PHOTO_URI};
        ContentResolver cr = getContentResolver();

        emailCur = cr.query(
                ContactsContract.CommonDataKinds.Email.CONTENT_URI, projection,
                ContactsContract.CommonDataKinds.Email.ADDRESS + " = ?",
                new String[]{email}, null);
        if (emailCur != null && emailCur.getCount() > 0) {
            if (emailCur.moveToNext()) {
                String photoUri = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.PHOTO_URI));
                if (photoUri != null)
                    uri = Uri.parse(photoUri);
            }
        }
        return uri;
    }

    private Uri getProfileImageUriLowerVersion(Cursor emailCur, String email) {
        Uri uri = null;
        String[] projection = {ContactsContract.CommonDataKinds.Photo.CONTACT_ID};
        ContentResolver cr = getContentResolver();
        emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                projection,
                ContactsContract.CommonDataKinds.Email.ADDRESS + " = ?",
                new String[]{email}, null);
        if (emailCur.moveToNext()) {
            int columnIndex = emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Photo.CONTACT_ID);
            long contactId = emailCur.getLong(columnIndex);
            uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId);
            uri = Uri.withAppendedPath(uri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY);
        }
        return uri;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Common.PERMISSIONS_REQUEST_GET_ACCOUNTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    List<String> emailList = getEmailList();
                    email_arr = emailList.toArray(new String[emailList.size()]);
                    actionBar.setSubtitle(Common.getPreferredEmail(email_arr));
                } else {

                }
                return;
            }
            case Common.PERMISSIONS_REQUEST_INTERNET: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                return;
            }
            case Common.PERMISSIONS_REQUEST_READ_CONTACTS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                return;
            }
            case Common.PERMISSIONS_REQUEST_RECEIVE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                return;
            }
            case Common.PERMISSIONS_REQUEST_WAKE_LOCK: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {

                }
                return;
            }
        }
    }

}
