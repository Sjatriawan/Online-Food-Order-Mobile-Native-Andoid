package com.lentera.silaq;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lentera.silaq.Common.Common;
import com.lentera.silaq.Database.CartDataSource;
import com.lentera.silaq.Database.CartDatabase;
import com.lentera.silaq.Database.LocalCartDataSource;
import com.lentera.silaq.EventBus.BestDealItemClik;
import com.lentera.silaq.EventBus.CategoryClick;
import com.lentera.silaq.EventBus.CounterCartEvent;
import com.lentera.silaq.EventBus.FoodItemClick;
import com.lentera.silaq.EventBus.HideFabCart;
import com.lentera.silaq.EventBus.MenuItemBack;
import com.lentera.silaq.EventBus.PlusClick;
import com.lentera.silaq.EventBus.PopularCategoryClick;
import com.lentera.silaq.Model.CategoryModel;
import com.lentera.silaq.Model.FoodModel;
import com.lentera.silaq.Model.PopularCategoryModel;
import com.lentera.silaq.Model.UserModel;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Place placeSelected;
    private AutocompleteSupportFragment places_fragment;
    private PlacesClient placesClient;
    private List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG);

    private NavController navController ;
    private AppBarConfiguration mAppBarConfiguration;

    private CartDataSource cartDataSource;
    private DrawerLayout drawer;

    android.app.AlertDialog dialog;

    int menuClickId=-1;

    @BindView(R.id.fab)
    CounterFab fab;

    @Override
    protected void onResume(){
        super.onResume();
        counterCartItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        initPlaceClient();

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        ButterKnife.bind(this);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navController.navigate(R.id.nav_cart);
            }
        });

         drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
//
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_menu, R.id.nav_food_list, R.id.nav_cart, R.id.nav_sign_out,R.id.nav_view_orders)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        TextView txt_user = (TextView) headerView.findViewById(R.id.txt_user);
        Common.setSpanString("Hi, ",Common.currentUser.getName(),txt_user);


        counterCartItem();
    }

    private void initPlaceClient() {
        Places.initialize(this, getString(R.string.google_maps_key));
        placesClient = Places.createClient(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }


    private void signOut(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Keluar")
                .setMessage("Apakah anda ingin keluar?")
                .setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setPositiveButton("Iya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Common.selectedFood = null;
                Common.categorySelected = null;
                Common.currentUser = null;

                FirebaseAuth.getInstance().signOut();

                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onCategorySelected(CategoryClick event){
        if(event.isSuccess()){
            navController.navigate(R.id.nav_food_list);
//            Toast.makeText(this,"Klik ke "+event.getCategoryModel().getName(),Toast.LENGTH_SHORT).show();
        }
    }



    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onCategorySelected(FoodItemClick event){
        if(event.isSuccess()){
            navController.navigate(R.id.nav_food_detail);
//            Toast.makeText(this,"Klik ke "+event.getCategoryModel().getName(),Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onHidenFabEvent(HideFabCart event){
        if(event.isHidden()){
            fab.hide();
        }else
            fab.show();
    }


    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onCartCounter(CounterCartEvent event){
        if(event.isSuccess()){
            counterCartItem();
        }
    }
    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onBestDealItemClick(BestDealItemClik event){
        if(event.getBestDealModel() != null){
            dialog.show();
            FirebaseDatabase.getInstance()
                    .getReference("Category")
                    .child(event.getBestDealModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                Common.categorySelected = snapshot.getValue(CategoryModel.class);
                                FirebaseDatabase.getInstance()
                                        .getReference("Category")
                                        .child(event.getBestDealModel().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .equalTo(event.getBestDealModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.exists())
                                                {
                                                    for (DataSnapshot itemSnapshot: snapshot.getChildren()){
                                                        Common.selectedFood = itemSnapshot.getValue(FoodModel.class);
                                                        Common.selectedFood.setKey(itemSnapshot.getKey());
                                                    }
                                                    navController.navigate(R.id.nav_food_detail);
                                                }
                                                else {
                                                    Toast.makeText(HomeActivity.this, "item tidak tersedia", Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                dialog.dismiss();
                                                Toast.makeText(HomeActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });

                            }
                            else
                            {
                                dialog.dismiss();
                                Toast.makeText(HomeActivity.this, "item tidak tersedia", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            dialog.dismiss();
                            Toast.makeText(HomeActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onPopularItemClick(PopularCategoryClick event){
        if(event.getPopularCategoryModel() != null){
            dialog.show();
            FirebaseDatabase.getInstance()
                    .getReference("Category")
                    .child(event.getPopularCategoryModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                Common.categorySelected = snapshot.getValue(CategoryModel.class);
                                Common.categorySelected.setMenu_id(snapshot.getKey());
                                FirebaseDatabase.getInstance()
                                        .getReference("Category")
                                        .child(event.getPopularCategoryModel().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .equalTo(event.getPopularCategoryModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.exists())
                                                {
                                                    for (DataSnapshot itemSnapshot: snapshot.getChildren()){
                                                        Common.selectedFood = itemSnapshot.getValue(FoodModel.class);
                                                        Common.selectedFood.setKey(itemSnapshot.getKey());
                                                    }
                                                    navController.navigate(R.id.nav_food_detail);
                                                }
                                                else {
                                                    Toast.makeText(HomeActivity.this, "item tidak tersedia", Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                dialog.dismiss();
                                                Toast.makeText(HomeActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });

                            }
                            else
                            {
                                dialog.dismiss();
                                Toast.makeText(HomeActivity.this, "item tidak tersedia", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            dialog.dismiss();
                            Toast.makeText(HomeActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onPlusItemClick(PlusClick event){
        if(event.getPlusModel() != null){
            dialog.show();
            FirebaseDatabase.getInstance()
                    .getReference("Category")
                    .child(event.getPlusModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if(snapshot.exists()){
                                Common.categorySelected = snapshot.getValue(CategoryModel.class);
                                Common.categorySelected.setMenu_id(snapshot.getKey());
                                FirebaseDatabase.getInstance()
                                        .getReference("Category")
                                        .child(event.getPlusModel().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .equalTo(event.getPlusModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                if(snapshot.exists())
                                                {
                                                    for (DataSnapshot itemSnapshot: snapshot.getChildren()){
                                                        Common.selectedFood = itemSnapshot.getValue(FoodModel.class);
                                                        Common.selectedFood.setKey(itemSnapshot.getKey());
                                                    }
                                                    navController.navigate(R.id.nav_food_detail);
                                                }
                                                else {
                                                    Toast.makeText(HomeActivity.this, "item tidak tersedia", Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                dialog.dismiss();
                                                Toast.makeText(HomeActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });

                            }
                            else
                            {
                                dialog.dismiss();
                                Toast.makeText(HomeActivity.this, "item tidak tersedia", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            dialog.dismiss();
                            Toast.makeText(HomeActivity.this, ""+error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    private void counterCartItem() {
        cartDataSource.countItemCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {

                        fab.setCount(integer);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if(!e.getMessage().contains("empty")){
                            Toast.makeText(HomeActivity.this,"[COUNT KERANJANG]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }else
                            fab.setCount(0);
                    }
                });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)  {
        item.setChecked(true);
        drawer.closeDrawers();
        switch (item.getItemId())
        {
            case R.id.nav_home:
                if(item.getItemId() != menuClickId)
                    navController.navigate(R.id.nav_home);
                break;
            case R.id.nav_menu:
                if(item.getItemId() != menuClickId)
                    navController.navigate(R.id.nav_menu);
                break;
            case R.id.nav_cart:
                if(item.getItemId() != menuClickId)
                    navController.navigate(R.id.nav_cart);
                break;
            case R.id.nav_view_orders:
                if(item.getItemId() != menuClickId)
                    navController.navigate(R.id.nav_view_orders);
                break;
            case R.id.nav_sign_out:
                signOut();
                break;
            case R.id.nav_update_info:
                showUpdateInfoDialog();
                break;

        }
        menuClickId= item.getItemId();
        return true;
    }

    private void showUpdateInfoDialog() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setMessage("Update Info!");

        View itemView = LayoutInflater.from(this).inflate(R.layout.layout_register,null);

        EditText edt_name=(EditText) itemView.findViewById(R.id.edt_name);
        EditText edt_address=(EditText) itemView.findViewById(R.id.edt_address);
        EditText edt_phone=(EditText) itemView.findViewById(R.id.edt_phone);

//        places_fragment = (AutocompleteSupportFragment)getSupportFragmentManager()
//                .findFragmentById(R.id.places_autocomplete_fragment);
//        places_fragment.setPlaceFields(placeFields);
//        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
//            @Override
//            public void onPlaceSelected(@NonNull Place place) {
//                placeSelected = place;
//                txt_address_detail.setText(place.getAddress());
//            }
//
//            @Override
//            public void onError(@NonNull Status status) {
//                Toast.makeText(HomeActivity.this, ""+status.getStatusMessage(), Toast.LENGTH_SHORT).show();
//            }
//        });
        //
        edt_name.setText(Common.currentUser.getName());
        edt_address.setText(Common.currentUser.getAddress());
        edt_phone.setText(Common.currentUser.getPhone());

        builder.setView(itemView);
        builder.setNegativeButton("Batal", (dialog, which) -> {
            dialog.dismiss();
        });
        builder.setPositiveButton("Update", (dialog, which) -> {
                if (TextUtils.isEmpty(edt_name.getText().toString())){
                    Toast.makeText(HomeActivity.this, "Masukkan Nama", Toast.LENGTH_SHORT).show();
                    return;
                }
                Map<String, Object> update_data = new HashMap<>();
                update_data.put("name",edt_name.getText().toString());
                update_data.put("address", edt_address.getText().toString());
//

                FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCES)
                        .child(Common.currentUser.getUid())
                        .updateChildren(update_data)
                        .addOnFailureListener(e -> {
                            dialog.dismiss();
                            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }).addOnSuccessListener(aVoid -> {
                            dialog.dismiss();
                    Toast.makeText(this, "Update berhasil", Toast.LENGTH_SHORT).show();
                    Common.currentUser.setName(update_data.get("name").toString());
                    Common.currentUser.setAddress(update_data.get("address").toString());
                        });

        });

        builder.setView(itemView);
        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onMenItemBack(MenuItemBack event){
        menuClickId =-1;
        if(getSupportFragmentManager().getBackStackEntryCount() > 0)
            getSupportFragmentManager().popBackStack();
//        navController.popBackStack(R.id.nav_home,true);
    }
}
