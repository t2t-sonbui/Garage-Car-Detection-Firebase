package app.demo.garagecardetection;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    RecyclerView recyclerView;
    CoordinatorLayout rootLayout;
    CarStateAdapter mAdapter;
    ListCar listCar;
    private ChildEventListener mListenner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        listCar = new ListCar();
        mAdapter = new CarStateAdapter(this, listCar);
        recyclerView.setAdapter(mAdapter);
        //Load danh sach
        loadData();

    }

    @Override
    protected void onDestroy() {
        if (mListenner != null)
            FirebaseDatabase.getInstance().getReference().removeEventListener(mListenner);
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, Smartconfig.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private int getIndexForKey(String key) {
        int index = 0;
        for (CarItem snapshot : listCar.getItemlist()) {
            if (!TextUtils.isEmpty(snapshot.getKey()) && snapshot.getKey().equals(key)) {
                return index;
            } else {
                index++;
            }
        }
        throw new IllegalArgumentException("Key not found");
    }

    private void loadData() {
        mListenner = FirebaseDatabase.getInstance().getReference().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildKey) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                CarItem mApply = dataSnapshot.getValue(CarItem.class);
                if (mApply != null) {
                    mApply.setKey(dataSnapshot.getKey());
                    int index = 0;
                    if (previousChildKey != null) {
                        index = getIndexForKey(previousChildKey) + 1;
                    }

                    listCar.addItem(index, mApply);
                    mAdapter.notifyItemInserted(index);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
                CarItem mApply = dataSnapshot.getValue(CarItem.class);
                mApply.setKey(dataSnapshot.getKey());
                int index = getIndexForKey(dataSnapshot.getKey());
                listCar.setItem(index, mApply);
                mAdapter.notifyItemChanged(index);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                int index = getIndexForKey(dataSnapshot.getKey());
                listCar.removeItem(index);
                mAdapter.notifyItemRemoved(index);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildKey) {

                CarItem mApply = dataSnapshot.getValue(CarItem.class);
                mApply.setKey(dataSnapshot.getKey());
                int oldIndex = getIndexForKey(dataSnapshot.getKey());
                listCar.removeItem(oldIndex);
                int newIndex = previousChildKey == null ? 0 : (getIndexForKey(previousChildKey) + 1);
                listCar.addItem(newIndex, mApply);
                mAdapter.notifyItemMoved(oldIndex, newIndex);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
