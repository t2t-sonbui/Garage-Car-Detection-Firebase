package app.demo.garagecardetection;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import app.demo.garagecardetection.esptouch.EsptouchTask;
import app.demo.garagecardetection.esptouch.IEsptouchListener;
import app.demo.garagecardetection.esptouch.IEsptouchResult;
import app.demo.garagecardetection.esptouch.IEsptouchTask;
import app.demo.garagecardetection.esptouch.task.__IEsptouchTask;
import app.demo.garagecardetection.utils.InterfaceUtils;
import app.demo.garagecardetection.utils.NetworkUtils;

/**
 * Created by Son Bui on 28/06/2016.
 */
public class Smartconfig extends AppCompatActivity implements View.OnClickListener {
    private TextView mTvApSsid;
    private EditText mEdtApPassword;
    private Button mBtnConfirm;
    private CheckBox mSwitchIsSsidHidden;
    private EspWifiAdminSimple mWifiAdmin;
    private Spinner mSpinnerTaskCount;
    private LinearLayout mqttVerifyGroup;
    private TextView mqttVerifyMessage;
    private ImageView mqttVerifyCheck;
    private ProgressBar mqttVerifyProgress;
    private static final String ERROR_COLOR = "#550000";
    private static final String SUCCESS_COLOR = "#005500";

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // display the connected ap's ssid
        String apSsid = mWifiAdmin.getWifiConnectedSsid();
        if (apSsid != null) {
            mTvApSsid.setText(apSsid);
        } else {
            mTvApSsid.setText("");
        }
        // check whether the wifi is connected
        boolean isApSsidEmpty = TextUtils.isEmpty(apSsid);
        mBtnConfirm.setEnabled(!isApSsidEmpty);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_smartconfig);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("ESP smartconfig");

        // Get reference API verify group.
        mqttVerifyGroup = (LinearLayout) findViewById(R.id.sitewhere_mqtt_verify_grp);

        // Get reference 'verify message' text view.
        mqttVerifyMessage = (TextView) findViewById(R.id.sitewhere_mqtt_verify);

        // Get reference to check indicator for MQTT verify.
        mqttVerifyCheck = (ImageView) findViewById(R.id.sitewhere_mqtt_verify_check);

        // Get reference to progress indicator for MQTT verify.
        mqttVerifyProgress = (ProgressBar) findViewById(
                R.id.sitewhere_mqtt_verify_progress);

        mWifiAdmin = new EspWifiAdminSimple(this);
        mTvApSsid = (TextView) findViewById(R.id.tvApSssidConnected);
        mEdtApPassword = (EditText) findViewById(R.id.edtApPassword);
        mBtnConfirm = (Button) findViewById(R.id.btnConfirm);
        mSwitchIsSsidHidden = (CheckBox) findViewById(R.id.switchIsSsidHidden);
        mBtnConfirm.setOnClickListener(this);
        mSpinnerTaskCount = (Spinner) findViewById(R.id.spinnerTaskResultCount);
        initSpinner();
    }

    private void initSpinner() {
        int[] spinnerItemsInt = getResources().getIntArray(R.array.taskResultCount);
        int length = spinnerItemsInt.length;
        Integer[] spinnerItemsInteger = new Integer[length];
        for (int i = 0; i < length; i++) {
            spinnerItemsInteger[i] = spinnerItemsInt[i];
        }
        ArrayAdapter<Integer> adapter = new ArrayAdapter<Integer>(this,
                android.R.layout.simple_list_item_1, spinnerItemsInteger);
        mSpinnerTaskCount.setAdapter(adapter);
        mSpinnerTaskCount.setSelection(1);
    }

    @Override
    public void onClick(View v) {
        if (v == mBtnConfirm) {

            if (!NetworkUtils.isOnline(this)) {
                InterfaceUtils.showAlert(this, R.string.no_wifi_message,
                        R.string.no_wifi_title);
                return;
            }


            String apSsid = mTvApSsid.getText().toString();
            String apPassword = mEdtApPassword.getText().toString();
            String apBssid = mWifiAdmin.getWifiConnectedBssid();
            Boolean isSsidHidden = mSwitchIsSsidHidden.isChecked();
            String isSsidHiddenStr = "NO";
            String taskResultCountStr = Integer.toString(mSpinnerTaskCount
                    .getSelectedItemPosition());
            if (isSsidHidden) {
                isSsidHiddenStr = "YES";
            }

            Log.d("Smartconfig", "mBtnConfirm is clicked, mEdtApSsid = " + apSsid
                    + ", " + " mEdtApPassword = " + apPassword);

            new EsptouchAsyncTask3().execute(apSsid, apBssid, apPassword,
                    isSsidHiddenStr, taskResultCountStr);
        }
    }

    private void onEsptoucResultAddedPerform(final IEsptouchResult result) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                String text = result.getBssid() + " is connected to the wifi";
                Toast.makeText(getApplicationContext(), text,
                        Toast.LENGTH_LONG).show();
                mqttVerifyMessage.setText(text);
            }

        });
    }

    private IEsptouchListener myListener = new IEsptouchListener() {

        @Override
        public void onEsptouchResultAdded(final IEsptouchResult result) {
            onEsptoucResultAddedPerform(result);
        }
    };

    private class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {
        private ProgressDialog mProgressDialog;

        private IEsptouchTask mEsptouchTask;
        // without the lock, if the user tap confirm and cancel quickly enough,
        // the bug will arise. the reason is follows:
        // 0. task is starting created, but not finished
        // 1. the task is cancel for the task hasn't been created, it do nothing
        // 2. task is created
        // 3. Oops, the task should be cancelled, but it is running
        private final Object mLock = new Object();

        @Override
        protected void onPreExecute() {
            // Disable button until processing is complete.
            mBtnConfirm.setEnabled(false);
            mqttVerifyMessage.setTextColor(Color.parseColor(SUCCESS_COLOR));
            mqttVerifyMessage.setText("Configuring...");
            // Make the group visible.
            mqttVerifyGroup.setVisibility(View.VISIBLE);
            mqttVerifyProgress.setVisibility(View.VISIBLE);

            mProgressDialog = new ProgressDialog(Smartconfig.this);
            mProgressDialog
                    .setMessage("Esptouch is configuring, please wait for a moment...");
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    synchronized (mLock) {
                        if (__IEsptouchTask.DEBUG) {
                            Log.i("Smartconfig", "progress dialog is canceled");
                        }
                        if (mEsptouchTask != null) {
                            mEsptouchTask.interrupt();
                        }
                    }
                    mBtnConfirm.setEnabled(true);
                    mqttVerifyProgress.setVisibility(View.GONE);
                    mqttVerifyMessage.setTextColor(Color.parseColor(ERROR_COLOR));
                    mqttVerifyMessage.setText("Esptouch cancel");
                }
            });
            mProgressDialog.show();

        }

        @Override
        protected List<IEsptouchResult> doInBackground(String... params) {
            int taskResultCount = -1;
            synchronized (mLock) {
                String apSsid = params[0];
                String apBssid = params[1];
                String apPassword = params[2];
                String isSsidHiddenStr = params[3];
                String taskResultCountStr = params[4];
                boolean isSsidHidden = false;
                if (isSsidHiddenStr.equals("YES")) {
                    isSsidHidden = true;
                }
                taskResultCount = Integer.parseInt(taskResultCountStr);
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword,
                        isSsidHidden, Smartconfig.this);
                mEsptouchTask.setEsptouchListener(myListener);
            }
            List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
            return resultList;
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {
            mBtnConfirm.setEnabled(true);
            IEsptouchResult firstResult = result.get(0);
            // check whether the task is cancelled and no results received
            if (!firstResult.isCancelled()) {
                int count = 0;
                // max results to be displayed, if it is more than maxDisplayCount,
                // just show the count of redundant ones
                final int maxDisplayCount = 5;
                // the task received some results including cancelled while
                // executing before receiving enough results
                if (firstResult.isSuc()) {
                    StringBuilder sb = new StringBuilder();
                    for (IEsptouchResult resultInList : result) {
                        sb.append("Esptouch success, bssid = "
                                + resultInList.getBssid()
                                + ",InetAddress = "
                                + resultInList.getInetAddress()
                                .getHostAddress() + "\n");
                        count++;
                        if (count >= maxDisplayCount) {
                            break;
                        }
                    }
                    if (count < result.size()) {
                        sb.append("\nthere's " + (result.size() - count)
                                + " more result(s) without showing\n");
                    }
                    mProgressDialog.setMessage(sb.toString());
                    mqttVerifyProgress.setVisibility(View.GONE);
                    mqttVerifyCheck.setVisibility(View.VISIBLE);
                    mqttVerifyMessage.setTextColor(Color.parseColor(SUCCESS_COLOR));
                    mqttVerifyMessage.setText(sb.toString());

                } else {
                    mProgressDialog.setMessage("Esptouch fail");
                    mqttVerifyProgress.setVisibility(View.GONE);
                    mqttVerifyMessage.setTextColor(Color.parseColor(ERROR_COLOR));
                    mqttVerifyMessage.setText("Esptouch fail");
                }
                if (mProgressDialog.isShowing()) mProgressDialog.dismiss();
            }
        }
    }
}
