package com.example.cscb07projectcode.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cscb07projectcode.Item;
import com.example.cscb07projectcode.ProductRecyclerAdapter;
import com.example.cscb07projectcode.R;
import com.example.cscb07projectcode.Store;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class CustomerOrderActivity extends AppCompatActivity {

    private ArrayList<Item> itemsList;
    private RecyclerView recyclerView;
    private ProductRecyclerAdapter.RecyclerViewClickListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_order);

        recyclerView = findViewById(R.id.recyclerViewOrdersId);

        itemsList = new ArrayList<>();

        setStoreInfo();
        // set a short delay to read from database
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.i("mytag", "caught exception");
        }
    }

    public void setStoreInfo() {
        Intent intent = getIntent();
        String value= getIntent().getStringExtra("getData");
        TextView textView = (TextView) findViewById(R.id.textView13);
        textView.setText(value);

        // leverages store name to find the corresponding store's products
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("stores").child("list_of_stores");
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // loops through user (of type store owner) until it matches a username
                for(DataSnapshot child:dataSnapshot.getChildren()) {
                    Store store = child.getValue(Store.class);
                    if(value.equals(store.getName())){
                        // create a database reference to access list of products
                        DatabaseReference newRef = ref.child(store.getName()).child("products");
                        ValueEventListener newListener = new ValueEventListener(){
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                // iterate through each product and append it into the arraylist
                                for(DataSnapshot newChild:snapshot.getChildren()) {
                                    Item item = newChild.getValue(Item.class);
                                    itemsList.add(item);
                                }
                                setAdapter();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        };
                        newRef.addValueEventListener(newListener);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("warning", "loadPost:onCancelled", databaseError.toException());
            }
        };
        ref.addValueEventListener(listener);
    }


    public void setAdapter(){
        setOnClickListener();
        ProductRecyclerAdapter adapter = new ProductRecyclerAdapter(itemsList, listener, "CustomerOrderActivity");
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setClickable(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
    }

    // called when clicking on any product under current products
    private void setOnClickListener() {
        listener = new ProductRecyclerAdapter.RecyclerViewClickListener() {
            @Override
            public void onClick(View v, int position) {
                Intent intent = new Intent(getApplicationContext(), CustomerOrderActivity.class);

                // use intent as a vehicle to transfer product details into next activity
                intent.putExtra("productName", itemsList.get(position).getName());
                intent.putExtra("productDesc", itemsList.get(position).getDescription());
                intent.putExtra("productPrice", String.valueOf(itemsList.get(position).getPrice()));
                intent.putExtra("productQty", String.valueOf(itemsList.get(position).getQuantity()));
                intent.putExtra("productUnit", itemsList.get(position).getUnit());

                startActivity(intent);
            }
        };
    }

}