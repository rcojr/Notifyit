package com.jrapps.notifyit;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SetupWizardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setupwizard);

        SetupWizardContent fragment = new SetupWizardContent();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame,fragment);
        fragmentTransaction.commit();

    }

}
