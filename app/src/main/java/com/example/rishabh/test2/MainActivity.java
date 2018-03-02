package com.example.rishabh.test2;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity  {

    private RecyclerView recyclerView;
    private RecyclerAdapter adapter;
    private List<ContactModel> contacts;
    private String data;

    private static final String TAG = "DEBUG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Ignore URI exposed Exception
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        recyclerView = findViewById(R.id.recycler_view);
        boolean result = Utility.checkPermissionReadContacts(MainActivity.this);
        if(result){
            showContacts();
        }

    }


    private void showContacts() {
        contacts = getContacts(this);
        adapter = new RecyclerAdapter(contacts,this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
        recyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
    }

    public List<ContactModel> getContacts(Context ctx) {
        List<ContactModel> list = new ArrayList<>();
        ContentResolver contentResolver = ctx.getContentResolver();
        Cursor cursor = contentResolver.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cursor!=null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String id = cursor.getString
                        (cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String phoneNumber = "";
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY));
                if (cursor.getInt(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor cursorInfo = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);

                    while (cursorInfo!=null && cursorInfo.moveToNext()) {
                        phoneNumber = cursorInfo.
                                getString(cursorInfo.getColumnIndex
                                        (ContactsContract.CommonDataKinds.Phone.NUMBER));
                    }
                    ContactModel contact = new ContactModel(id,name,phoneNumber);
                    list.add(contact);
                    // Log.d(TAG,contact.name + " " + contact.mobileNumber);
                    if(cursorInfo!=null){
                        cursorInfo.close();
                    }

                }
            }
            cursor.close();
        }
        return list;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.export_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.export){
            data = getData();
            boolean result = Utility.checkPermissionWriteExternalStorage(this);
            if(result){
                exportToCsv(data);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getData() {
        String str = "Id,Name,PhoneNumber\n";
        for(ContactModel contact : contacts){
            str += contact.id+",";
            str += contact.name+",";
            str += contact.mobileNumber+"\n";
        }
        return str;
    }

    private void exportToCsv(String data)  {
        io.reactivex.Observable.fromCallable( () -> {
            try{
                String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
                String fileName = "ContactDetails.csv";
                String zipfile = "ContactDetails.zip";
                String filePath = baseDir + File.separator + fileName;
                String zipPath = baseDir + File.separator + zipfile;
                File file = new File(filePath );
                FileOutputStream fOut = new FileOutputStream(file);
                OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
                myOutWriter.append(data);
                myOutWriter.close();
                fOut.flush();
                fOut.close();
                String[] files = {filePath};
                zip(files, zipPath);
                file.delete();
                Snackbar.make(findViewById(R.id.main_layout),
                        "Csv File exported succesfully with filename ContactDetails.zip",Snackbar.LENGTH_LONG).show();

            }catch (IOException e){
                Toast.makeText(this,"Something went wrong" ,Toast.LENGTH_SHORT).show();
            }
            return false;

        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();

    }

    public void zip(String[] _files, String zipFileName) throws IOException{
        final int BUFFER = 2048;
        BufferedInputStream origin = null;
        FileOutputStream dest = new FileOutputStream(zipFileName);
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                dest));
        byte data[] = new byte[BUFFER];

        for (int i = 0; i < _files.length; i++) {
            FileInputStream fi = new FileInputStream(_files[i]);
            origin = new BufferedInputStream(fi, BUFFER);

            ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
            out.putNextEntry(entry);
            int count;

            while ((count = origin.read(data, 0, BUFFER)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
        }

        out.close();
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_READ_CONTACTS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showContacts();
                } else {
                    //code for deny
                    Toast.makeText(this,"Read Contact permission not granted" , Toast.LENGTH_SHORT).show();
                }
                break;
            case Utility.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    exportToCsv(data);
                } else {
                    //code for deny
                    Toast.makeText(this,"Write External Strorage Permission not granted" , Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}

class Utility {
    public static final int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 123;
    public static final int MY_PERMISSIONS_READ_CONTACTS = 100;

    public static boolean checkPermissionWriteExternalStorage(final Context context)
    {

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            return false;
        } else {
            return true;
        }

    }

    public static boolean checkPermissionReadContacts(final Context context)
    {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED)

        {
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_CONTACTS}, MY_PERMISSIONS_READ_CONTACTS);
            return false;
        } else {
            return true;
        }
    }

}
