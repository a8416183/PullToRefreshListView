
package com.dara;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class HelloAndroidActivity extends Activity {

    private static String TAG = "PullToRefresh";

    /**
     * Called when the activity is first created.
     * 
     * @param savedInstanceState If the activity is being re-initialized after
     *            previously being shut down then this Bundle contains the data
     *            it most recently supplied in onSaveInstanceState(Bundle).
     *            <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.main);

        final String[] dummyData = new String[] {
                "TEST 1", "TEST 2", "TEST 3"
        };
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),
                android.R.layout.simple_list_item_1, dummyData);
        final ListView listview = (ListView)findViewById(R.id.listview);
        listview.setAdapter(adapter);
    }

}
