package com.askylol.bookaseat.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.askylol.bookaseat.R;
import com.askylol.bookaseat.utils.Data;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Created by Sharon on 03-Jul-17.
 */
public class AdminManagementActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_management);

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.nav_admin_management);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView adminsListView = (ListView) findViewById(R.id.admins_list);
        AdminsAdapter adminsAdapter = new AdminsAdapter(this, R.layout.view_admin);
        adminsListView.setAdapter(adminsAdapter);

        updateAdapter(Data.INSTANCE.library.getAdmins().values());

        if (Data.INSTANCE.library != null) {
            Data.INSTANCE.library.getLibraryRef()
                    .getDatabase()
                    .getReference(String.format("libraries/%s/admins", Data.INSTANCE.library.getLibraryRef().getKey()))
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                updateAdapter(((Map<String, String>) dataSnapshot.getValue()).values());
                            } else {
                                updateAdapter(new ArrayList<String>());
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
        }

        ((Button) findViewById(R.id.add_new_admin_button)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Data.INSTANCE.library.addAdmin(((EditText) findViewById(R.id.new_admin_mail_edit_text)).getText().toString());
            }
        });
    }

    private void updateAdapter(Collection<String> admins) {
        AdminsAdapter adminsAdapter = (AdminsAdapter) ((ListView) findViewById(R.id.admins_list)).getAdapter();

        if (adminsAdapter != null) {
            adminsAdapter.clear();

            for (String s : admins) {
                adminsAdapter.add(s);
            }
        }
    }

    private class AdminsAdapter extends ArrayAdapter<String> {
        public AdminsAdapter(@NonNull Context context, @LayoutRes int resource) {
            super(context, resource);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            View v = convertView;

            if (v == null) {
                LayoutInflater inflater = LayoutInflater.from(getContext());
                v = inflater.inflate(R.layout.view_admin, null);
            }

            String admin = getItem(position);

            if (admin == null) {
                return v;
            }

            ((TextView) v.findViewById(R.id.admin_mail_text_view)).setText(admin);

            return v;
        }
    }
}
