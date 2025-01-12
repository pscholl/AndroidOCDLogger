package unifr.sensorrecorder;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;

import unifr.sensorrecorder.DataContainer.StaticDataProvider;
import unifr.sensorrecorder.Networking.NetworkManager;

//import static android.Manifest.permission.RECORD_AUDIO;
//import static android.content.pm.PackageManager.PERMISSION_DENIED;

public class ConfActivity extends WearableActivity {

    private static double FACTOR = 0.146467f; // c = a * sqrt(2)
    private SharedPreferences configs;
    private EditText serverNameInput;
    private EditText userIdentifierInput;
    private CheckBox useZipsCheckbox;
    private CheckBox useMKVCheckbox;
    private CheckBox useMicCheckbox;
    private CheckBox autoUpdateTFCheckbox;
    private CheckBox updateTFCheckbox;
    private Switch multipleMicSwitch;
    private Button downloadTFModelButton;
    private Button deleteTokenButton;
    private NetworkManager networkManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conf);

        adjustInset();

        configs = this.getSharedPreferences(
                getString(R.string.configs), Context.MODE_PRIVATE);

        networkManager = StaticDataProvider.getNetworkManager();

        serverNameInput = (EditText)findViewById(R.id.editTextServerName);
        userIdentifierInput = (EditText)findViewById(R.id.editTextUserIdentifier);
        userIdentifierInput.setText(android.os.Build.MODEL);

        useZipsCheckbox = (CheckBox) findViewById(R.id.useZipCheckbox);
        useMKVCheckbox = (CheckBox) findViewById(R.id.useMKVCheckbox);
        //useMicCheckbox = (CheckBox) findViewById(R.id.useMicCheckbox);
        autoUpdateTFCheckbox = (CheckBox) findViewById(R.id.autoUpdateTFCheckbox);
        updateTFCheckbox = (CheckBox) findViewById(R.id.updateTFCheckbox);
        //multipleMicSwitch = (Switch) findViewById(R.id.multipleMicSwitch);

        downloadTFModelButton = (Button)findViewById(R.id.buttonGetTFModel);
        deleteTokenButton = (Button)findViewById(R.id.buttonDeleteToken);

        if (configs.contains(getString(R.string.conf_serverName))) {
            String serverName = configs.getString(getString(R.string.conf_serverName), getString(R.string.predefined_serverName));
            serverNameInput.setText(serverName);
            if(serverName.equals(""))
                downloadTFModelButton.setEnabled(false);
        } else
            downloadTFModelButton.setEnabled(false);
        if (configs.contains(getString(R.string.conf_userIdentifier)))
            userIdentifierInput.setText(configs.getString(getString(R.string.conf_userIdentifier), ""));
        if(configs.contains(getString(R.string.conf_serverToken)))
            deleteTokenButton.setVisibility(View.VISIBLE);

        if(configs.contains(getString(R.string.conf_useZip)))
            useZipsCheckbox.setChecked(configs.getBoolean(getString(R.string.conf_useZip), true));
        if(configs.contains(getString(R.string.conf_useMKV)))
            useMKVCheckbox.setChecked(configs.getBoolean(getString(R.string.conf_useMKV), false));
        if(configs.contains(getString(R.string.conf_auto_update_tf))) {
            autoUpdateTFCheckbox.setChecked(configs.getBoolean(getString(R.string.conf_auto_update_tf), false));
            if(configs.getBoolean(getString(R.string.conf_auto_update_tf), false))
                updateTFCheckbox.setEnabled(false);
        }
        if(configs.contains(getString(R.string.conf_check_for_tf_update)))
            updateTFCheckbox.setChecked(configs.getBoolean(getString(R.string.conf_check_for_tf_update), true));

        /*
        if(ContextCompat.checkSelfPermission(this, RECORD_AUDIO) == PERMISSION_DENIED){
            useMicCheckbox.setChecked(false);
            SharedPreferences.Editor configEditor = configs.edit();
            configEditor.putBoolean(getString(R.string.conf_useMic), false);
            configEditor.apply();
        }

        if(configs.contains(getString(R.string.conf_useMic))) {
            boolean useMic = configs.getBoolean(getString(R.string.conf_useMic), true);
            useMicCheckbox.setChecked(useMic);
            if (!useMic)
                multipleMicSwitch.setEnabled(false);
        }
        if(configs.contains(getString(R.string.conf_multipleMic)))
            multipleMicSwitch.setChecked(configs.getBoolean(getString(R.string.conf_multipleMic), true));


        useMicCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if(isChecked){
                    if(ContextCompat.checkSelfPermission(confActivity, RECORD_AUDIO) == PERMISSION_DENIED){
                        useMicCheckbox.setChecked(false);
                        ActivityCompat.requestPermissions(confActivity,
                                new String[]{RECORD_AUDIO},
                                1);
                    } else {
                        multipleMicSwitch.setEnabled(true);
                    }
                } else {
                    multipleMicSwitch.setEnabled(false);
                }
            }
        });
        */

        autoUpdateTFCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {
                if(isChecked){
                    updateTFCheckbox.setEnabled(false);
                } else {
                    updateTFCheckbox.setEnabled(true);
                }
            }
        });

        Button applyButton = (Button)findViewById(R.id.buttonApply);
        applyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor configEditor = configs.edit();
                String serverName = serverNameInput.getText().toString();
                String userIdentifier = userIdentifierInput.getText().toString();
                if (!serverName.equals("")) {
                    if (serverName.length() < 4 || !serverName.substring(0, 4).equals("http"))
                        serverName = "https://" + serverName;
                    configEditor.putString(getString(R.string.conf_serverName), serverName);
                } else{
                    configEditor.putString(getString(R.string.conf_serverName), "");
                }
                Log.i("sensorrecorder", "Set servername to " + serverNameInput.getText().toString());
                if (!userIdentifier.equals(""))
                    configEditor.putString(getString(R.string.conf_userIdentifier), userIdentifier);
                Log.i("sensorrecorder", "Set user to " + userIdentifierInput.getText().toString());

                configEditor.putBoolean(getString(R.string.conf_useZip), useZipsCheckbox.isChecked());
                configEditor.putBoolean(getString(R.string.conf_useMKV), useMKVCheckbox.isChecked());
                //configEditor.putBoolean(getString(R.string.conf_useMic), useMicCheckbox.isChecked());
                //configEditor.putBoolean(getString(R.string.conf_multipleMic), multipleMicSwitch.isChecked());
                configEditor.putBoolean(getString(R.string.conf_auto_update_tf), autoUpdateTFCheckbox.isChecked());
                configEditor.putBoolean(getString(R.string.conf_check_for_tf_update), updateTFCheckbox.isChecked());

                configEditor.apply();

                if (configs.getString(getString(R.string.conf_serverToken), "").equals("") &&
                        !configs.getString(getString(R.string.conf_serverName), "").equals("")){
                    networkManager.requestServerToken();
                }

                finish();
            }
        });

        downloadTFModelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                networkManager.downloadTFModel();
            }
        });

        deleteTokenButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor configEditor = configs.edit();
                configEditor.remove(getString(R.string.conf_serverToken));
                configEditor.apply();
                deleteTokenButton.setVisibility(View.INVISIBLE);
            }
        });

        adjustInset();

    }

    private void adjustInset() {
        if (getResources().getConfiguration().isScreenRound()) {
            int inset = (int)(FACTOR * getResources().getDisplayMetrics().widthPixels);
            View layout = (View) findViewById(R.id.confMainview);
            Log.d("conf", "set inset to " + inset);
            // inset*=2;
            layout.setPadding(inset, inset, inset, inset);
        }
    }

    // @Override
    // public void onRequestPermissionsResult(int requestCode,
    //                                        String permissions[], int[] grantResults) {
    //     Log.d("Sensorrecorder", "rc: " + requestCode +  "length: "+permissions.length + " gr: " + grantResults.length);
    //     switch (requestCode) {
    //         case 1: {
    //             // If request is cancelled, the result arrays are empty.
    //             if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
    //                 Log.d("Sensorrecorder", "permission: " + permissions[0]);
    //                 useMicCheckbox.setChecked(true);
    //             } else {
    //                 useMicCheckbox.setChecked(false);
    //             }
    //             return;
    //         }
    //     }
    // }
}
