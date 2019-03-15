package com.jrapps.notifyit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

public class SetupWizardContent extends Fragment {

    Button btnInstalled;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.content_setupwizard,container,false);

        btnInstalled = (Button)v.findViewById(R.id.btnInstalled);

        btnInstalled.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
                boolean first_run = preferences.getBoolean("first_run", true);

                if (first_run){

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("first_run", false);
                    editor.apply();

                    Intent i = new Intent(getActivity().getApplicationContext(), MainActivity.class);
                    startActivity(i);

                }

                getActivity().finish();

            }
        });

        return v;
    }
}