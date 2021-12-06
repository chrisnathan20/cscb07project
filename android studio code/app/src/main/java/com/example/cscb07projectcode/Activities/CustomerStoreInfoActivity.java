package com.example.cscb07projectcode.Activities;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.cscb07projectcode.AdapterForCustomerStoreView;
import com.example.cscb07projectcode.Item;
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
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class CustomerStoreInfoActivity extends AppCompatActivity {

    RecyclerView recyclerView;

    AdapterForCustomerStoreView myAdapter;
    ArrayList<Item> list;
    ArrayList<Item> cartList;

    Button add;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_store_view);
        recyclerView = findViewById(R.id.product_list_from_selected_store);
        list = new ArrayList<>();
        //quan = findViewById(R.id.quantity); // quantity to be ordered

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myAdapter = new AdapterForCustomerStoreView(this, list);
        cartList = new ArrayList<>();
        add = findViewById(R.id.Add_to_Cart);
        String[] strItemsList = new String[0];

        Intent intent = getIntent();
        strItemsList = intent.getStringArrayExtra("strItemsList");
        Log.i("check", String.valueOf(strItemsList.length));
        // if there is cart data being passed from the cart page via back button
        if (strItemsList.length != 0) {
            cartList = PopulateList(strItemsList);
        }

        // This for when items in the recyler view are clicked

// RETRIEVING THE CUSTOMER USERNAME
        SharedPreferences pref = getSharedPreferences("credentialsCustomer", Context.MODE_PRIVATE);
        String username = pref.getString("username", "");
        // RETRIEVING THE CUSTOMER
        SharedPreferences pref2 = getSharedPreferences("credentials_store_name", Context.MODE_PRIVATE);
        String store_name = pref2.getString("store_name", "");


        // VISIT CART STUFF DOWN THERE
        Button cart_button = (Button) findViewById(R.id.visitCart); // what happens when you click the visit cart button
        cart_button.setOnClickListener(new View.OnClickListener() {
                                           @Override
                                           public void onClick(View v) {
                                               openCart();
                                           }
                                       }

        );


        // leverages store name to find the corresponding store's products
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("stores").child("list_of_stores");
        ValueEventListener listener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // loops through user (of type store owner) until it matches a username
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    Store store = child.getValue(Store.class);
                    if (store_name.equals(store.getName())) {
                        // create a database reference to access list of products
                        DatabaseReference newRef = ref.child(store.getName()).child("products");
                        ValueEventListener newListener = new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                // iterate through each product and append it into the arraylist
                                for (DataSnapshot newChild : snapshot.getChildren()) {
                                    Item item = newChild.getValue(Item.class);
                                    list.add(item);
                                }
                                recyclerView.setAdapter(myAdapter);
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
        myAdapter.setOnItemClickListener(new AdapterForCustomerStoreView.onItemClickListener() {
            @Override
            public void onItemClick(int position) {
                list.get(position); // GET THE ITEM AT THIS POSITION DONE

                Log.i("AAAAAA", list.get(position).getName());
            }

            @Override
            public void onAddtoCart(int position) {


                TextView message = findViewById(R.id.Messages);
                list.get(position); // GET THE ITEM AT THIS POSITION DONE
                int how_many_can_be_added = list.get(position).getQuantity() - howManyProductsLeft(list.get(position), 0); // calcuates  how many in inventory - how many are already in the cart
                // VALIDATION FOR QUANTITY ENTERED
                Log.i("how many more", " " + how_many_can_be_added + "  of " + list.get(position).getName());
                //  Log.i(" CAN I GET TO STRING",list.get(position).toString());
                // String quantity_edit_text_field_value;

                // quantity_edit_text_field_value = addQuantity.getText().toString();
                //Log.i("How many?",quantity_edit_text_field_value);
                // int quantity_to_be_added = Integer.parseInt(quantity_edit_text_field_value);


                if (how_many_can_be_added <= 0) {
                    findViewById(R.id.Add_to_Cart).setBackgroundColor(Color.DKGRAY);
                    message.setText("We cannot provide more as " + "we only have " + howManyProductsLeft(list.get(position), 0) + " " + list.get(position).getName() + "(s) in stock");
                    message.setTextColor(Color.RED);
                }
                // EditText quantity = findViewById(R.id.quantity);
                //  TextView MessageForQuantity = findViewById(R.id.MessageForQuantity);


                //String quantity_ordewred_for_item = quantity.getText().toString();
                //  int quant = Integer.parseInt(quantity_ordewred_for_item);
                // Log.i("IS THE QUANTITY READ", quantity_ordewred_for_item);


                //MessageForQuantity.setText(" ");
                //Intent intent = new Intent(getApplicationContext(),Cart_Order_Activity.class);
                //Log.i("DOES IT ADD STUFFBEFORE", cartList.size()  + "  Prev");
                // msg.setText("You can add" + ( ) + " more"));
                else {
                    cartList.add(list.get(position)); // if we have enough we will add them here
                }

                //list.get(position).setQuantity((list.get(position).getQuantity() - quant));


                //Log.i("How many left", " "+ list.get(position).getQuantity());
                //Log.i("How many ", " "+ cartList.get(position).getQuantity());
                // Log.i("DOES IT ADD STUFF ", cartList.size() + "  ");
                //intent.putExtra("extra", list.get(position).toString());
                //startActivity(intent);
            }


        });


    }

    // open cart button
    public void openCart() {
        for (Item i : cartList) {
            Log.i("TESt", i.getName() + "\n\n" + i.getDescription());
        }
        if (cartList.isEmpty()) {
            Intent intent = new Intent(getApplicationContext(), Cart_Order_Activity.class);
            intent.putExtra("strItemsList", new String[0]);
            startActivity(intent);
        }
        Intent intent = new Intent(this, Cart_Order_Activity.class);
        String[] arr_ = new String[cartList.size()];
        int i = 0;
        for (Item item_ : cartList) {
            arr_[i] = item_.toString();
            i++;
        }
        intent.putExtra("strItemsList", arr_);
        startActivity(intent);
    }


    public int howManyProductsLeft(Item i, int init) {
        int count = init;
        for (Item x : cartList) {
            if (x.equals(i)) {
                count++;
            }
        }
        return count;
    }

    public void remove_one_from_arrayList(ArrayList<Item> itemlist, Item item) {
        int location = itemlist.lastIndexOf(item); // finds the last occurence of this item
        itemlist.remove(location);
    }

    public ArrayList<Item> PopulateList(String[] str) {
        ArrayList<Item> x = new ArrayList<Item>();
        for (String s : str) {
            String[] arr = s.split(";");
            int i = 0;
            /**   for(String yyy:arr)
             {
             Log.i("MMM", yyy + i );
             i++;
             } **/
            Item to_Add = new Item(arr[0], arr[1], Double.parseDouble(arr[2]), Integer.parseInt(arr[3]), arr[4]);
            //to_Add.setQuantity(1);
            x.add(to_Add);
        }
        return x;
    }
}