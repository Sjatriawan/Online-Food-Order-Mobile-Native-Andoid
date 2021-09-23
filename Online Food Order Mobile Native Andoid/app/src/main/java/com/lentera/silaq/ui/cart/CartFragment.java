package com.lentera.silaq.ui.cart;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.Settings;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.lentera.silaq.Adapter.MyCartAdapter;
import com.lentera.silaq.Callback.ILoadTimeFromFirebaseListener;
import com.lentera.silaq.Common.Common;
import com.lentera.silaq.Common.MySwiperHelper;
import com.lentera.silaq.Database.CartDataSource;
import com.lentera.silaq.Database.CartDatabase;
import com.lentera.silaq.Database.CartItem;
import com.lentera.silaq.Database.LocalCartDataSource;
import com.lentera.silaq.EventBus.CounterCartEvent;
import com.lentera.silaq.EventBus.HideFabCart;
import com.lentera.silaq.EventBus.MenuItemBack;
import com.lentera.silaq.EventBus.UpdateItemCart;
import com.lentera.silaq.HomeActivity;
import com.lentera.silaq.Model.CategoryModel;
import com.lentera.silaq.Model.FCMResponse;
import com.lentera.silaq.Model.FCMSendData;
import com.lentera.silaq.Model.Order;
import com.lentera.silaq.R;
import com.lentera.silaq.Remote.IFCMService;
import com.lentera.silaq.Remote.RetrofitFCMClient;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class CartFragment extends Fragment implements ILoadTimeFromFirebaseListener {

    private Place placeSelected;
    private AutocompleteSupportFragment places_fragment;
    private PlacesClient placesClient;
    private List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG);

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private  CartViewModel cartViewModel;
    private CategoryModel categoryModel;
    private Parcelable recyclerViewState;
    private CartDataSource cartDataSource;

    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;


    IFCMService ifcmService;
    ILoadTimeFromFirebaseListener listener;

    @BindView(R.id.recycler_cart)
    RecyclerView recycler_cart;
    @BindView(R.id.txt_total_price)
    TextView txt_total_price;
    @BindView(R.id.txt_empty_cart)
    TextView txt_empty_cart;
    @BindView(R.id.group_place_holder)
    CardView group_place_holder;

    @OnClick(R.id.ongkir)
    void onClickOngkir(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_dialog, null);
        builder.setView(view);

        builder.setNegativeButton("Ya", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builder.create();
        dialog.show();

    }


    @OnClick(R.id.btn_place_order)
    void onPlaceOrderClick(){
            final LocationManager manager = (LocationManager)getContext().getSystemService    (Context.LOCATION_SERVICE );
            if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) )
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Konfirmasi Pesanan");
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_place_order, null);

        EditText edt_address = (EditText) view.findViewById(R.id.edt_address);
        EditText edt_comment = (EditText) view.findViewById(R.id.edt_comment);
        TextView txt_address = (TextView) view.findViewById(R.id.txt_address_detail);
        RadioButton rdi_home= (RadioButton)view.findViewById(R.id.rdi_home_address);
        RadioButton rdi_cod= (RadioButton)view.findViewById(R.id.rdi_cod);
        RadioButton rdi_other_address= (RadioButton)view.findViewById(R.id.rdi_other_address);
        RadioButton rdi_ship_to_this= (RadioButton)view.findViewById(R.id.rdi_ship_this_address);

        //Data
        edt_address.setText(Common.currentUser.getAddress());

        //Event
        rdi_home.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                edt_address.setEnabled(true);
                edt_address.setText(Common.currentUser.getAddress());
                txt_address.setVisibility(View.GONE);
                edt_comment.setVisibility(View.VISIBLE);
//                places_fragment.setHint(Common.currentUser.getAddress());
            }
        });
        rdi_other_address.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
              edt_address.setEnabled(true);
              edt_address.setText("");
              txt_address.setVisibility(View.GONE);
                edt_comment.setVisibility(View.VISIBLE);
            }
        });
        rdi_ship_to_this.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                edt_comment.setVisibility(View.GONE);
                fusedLocationProviderClient.getLastLocation()
                        .addOnFailureListener(e -> {
                            Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            txt_address.setVisibility(View.GONE);
                        })
                        .addOnCompleteListener(task -> {
                            String coocrdinates = new StringBuilder()
                                    .append(task.getResult().getLatitude())
                                    .append("/")
                                    .append(task.getResult().getLongitude()).toString();

                            Single<String> singleAddress = Single.just(getAddressFromLatLng(task.getResult().getLatitude(), task.getResult().getLongitude()));
                            Disposable disposable = singleAddress.subscribeWith(new DisposableSingleObserver<String>() {
                                @Override
                                public void onSuccess(String s) {
                                    edt_address.setText(coocrdinates);
                                    txt_address.setText(s);
                                    txt_address.setVisibility(View.VISIBLE);
//                                        places_fragment.setHint(s);
                                }

                                @Override
                                public void onError(Throwable e) {
                                    txt_address.setText(coocrdinates);
                                    txt_address.setText(e.getMessage());
                                    txt_address.setVisibility(View.VISIBLE);
                                }

                            });
                        });

                edt_address.setEnabled(false);
            }

        });

        builder.setView(view);
        builder.setNegativeButton("Tidak", (dialog, which) -> {
            dialog.dismiss();
        }).setPositiveButton("Ya", (dialog, which) -> {
            if(rdi_cod.isChecked())
                paymentCOD(edt_address.getText().toString(),edt_comment.getText().toString());
        });
        AlertDialog dialog = builder.create();
//        dialog.setOnDismissListener(dialog1 -> {
//        if (places_fragment != null)
//            getActivity().getSupportFragmentManager().beginTransaction().remove(places_fragment).commit();
//        });
        dialog.show();
    }

    public static void displayPromptForEnablingGPS(final Activity activity)
    {

        final AlertDialog.Builder builder =  new AlertDialog.Builder(activity);
        final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
        final String message = "Do you want open GPS setting?";

        builder.setMessage(message)
                .setPositiveButton("OK",
                        (d, id) -> {
                            activity.startActivity(new Intent(action));
                            d.dismiss();
                        })
                .setNegativeButton("Cancel",
                        (d, id) -> d.cancel());
        builder.create().show();
    }
    //new edit
    @OnClick(R.id.btn_place_order)
    void onWaiting(){
        final AlertDialog.Builder builderw = new AlertDialog.Builder(getContext());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.waiting, null);
        builderw.setView(view);

        builderw.setNegativeButton("Ya", (dialog, which) -> {
            dialog.dismiss();
        });
        AlertDialog dialog = builderw.create();
        dialog.show();
    }

    private void paymentCOD(String address, String comment) {
        compositeDisposable.add(cartDataSource.getAllCart(Common.currentUser.getUid())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(cartItems -> {
            cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Double>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }
                        @Override
                        public void onSuccess(Double total) {
                            double finalPrice = total;
                            Order order = new Order();
                            order.setUserId(Common.currentUser.getUid());
                            order.setUserName(Common.currentUser.getName());
                            order.setUserPhone(Common.currentUser.getPhone());
                            order.setShippingAddress(address);
                            order.setComment(comment);
                            if(currentLocation != null){
                                order.setLat(currentLocation.getLatitude());
                                order.setLng(currentLocation.getLongitude());

                            }
                            else {
                                order.setLat(-0.1f);
                                order.setLng(-0.1f);
                            }
                            order.setCartItemList(cartItems);
                            order.setTotalPayment(total);
                            order.setDiscount(0);
                            order.setFinalPayment(finalPrice);
                            order.setCod(true);
                            order.setMarket(Common.categorySelected.getName());
                            order.setTransactionId("Bayar di tempat");

                            syncLoacalTimeWithGlobalTime(order);

                            //
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (!e.getMessage().contains("Query returned empty result set"))
                                Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }, throwable -> {
            Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
        }));
    }

    private void syncLoacalTimeWithGlobalTime(Order order) {
        final DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long offset = snapshot.getValue(Long.class);
                long estimateServerTimeMS = System.currentTimeMillis()+offset;
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
                Date resultDate = new Date(estimateServerTimeMS);
                Log.d("TEST_DATE",""+sdf.format(resultDate));

                listener.onLoadSuccess(order, estimateServerTimeMS);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onLoadFailed(error.getMessage());
            }
        });
    }

    private void writeOrderToFirebase(Order order) {
        FirebaseDatabase.getInstance()
                .getReference(Common.ORDER_REF)
                .child(Common.createOrderNumber())
                .setValue(order)
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }).addOnCompleteListener(task -> {
                    cartDataSource.cleanCart(Common.currentUser.getUid())
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(new SingleObserver<Integer>() {
                                @Override
                                public void onSubscribe(Disposable d) {

                                }

                                @Override
                                public void onSuccess(Integer integer) {
                                    Map<String, String> notiData = new HashMap<>();
                                    notiData.put(Common.NOTI_TITLE,"New Order");
                                    notiData.put(Common.NOTI_CONTENT,"Pesanan baru dari"+Common.currentUser.getPhone());

                                    FCMSendData sendData = new FCMSendData(Common.createTopicOrder(),notiData);

                                    compositeDisposable.add(ifcmService.sendNotification(sendData)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(fcmResponse -> {
                                                Toast.makeText(getContext(), "Pesanan berhasil dikirim", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                            }, throwable -> {
                                                Toast.makeText(getContext(), "Pesanan berhasil tetapi gagal mengirim pemberitahuan", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));

                                            }));
                                }

                                @Override
                                public void onError(Throwable e) {
                                    Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                });
    }

    private String getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        String result= "";
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude,longitude,1);
            if(addressList != null && addressList.size()>0){
                Address address = addressList.get(0);
                StringBuilder sb= new StringBuilder(address.getAddressLine(0));
                result = sb.toString();
            }else
                result  = "Alamat tidak ditemukan";
        } catch (IOException e) {
            e.printStackTrace();
            result= e.getMessage();
        }
        return result;
    }


    private MyCartAdapter adapter;

    private Unbinder unbinder;

    public View onCreateView(@NonNull LayoutInflater inflater,
                         ViewGroup container, Bundle savedInstanceState){
        cartViewModel=
                ViewModelProviders.of(this).get(CartViewModel.class);
        View root = inflater.inflate(R.layout.fragment_cart, container, false);

        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);
        listener = this;
        cartViewModel.initCartDataSource(getContext());
        cartViewModel.getMutableLiveDataCartItems().observe(this, new Observer<List<CartItem>>() {
            @Override
            public void onChanged(List<CartItem> cartItems) {
                if(cartItems == null || cartItems.isEmpty()){
                    recycler_cart.setVisibility(View.GONE);
                    group_place_holder.setVisibility(View.GONE);
                    txt_empty_cart.setVisibility(View.VISIBLE);

                }else
                {
                    recycler_cart.setVisibility(View.VISIBLE);
                    group_place_holder.setVisibility(View.VISIBLE);
                    txt_empty_cart.setVisibility(View.GONE);

                    adapter = new MyCartAdapter(getContext(),cartItems);
                    recycler_cart.setAdapter(adapter);
                }
            }
        });
        unbinder = ButterKnife.bind(this, root);
        initViews();
        initLocation();
        return root;
    }

    private void initLocation() {
        buildLocationrequest();
        buildLocationCallback();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.getMainLooper());
    }

    private void buildLocationCallback() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
            }
        };

    }

    private void buildLocationrequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }

    private void initViews() {

        initPlacesClient();
        setHasOptionsMenu(true);

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        EventBus.getDefault().postSticky(new HideFabCart(true));
        recycler_cart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recycler_cart.setLayoutManager(layoutManager);
        recycler_cart.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recycler_cart, 200) {
              @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Hapus", 30,0, Color.parseColor("#FF3C30"),
                        pod -> {
                                CartItem cartItem = adapter.getItemAtPosition(pod);
                                cartDataSource.deleteCartItem(cartItem)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(new SingleObserver<Integer>() {
                                            @Override
                                            public void onSubscribe(Disposable d) {

                                            }

                                            @Override
                                            public void onSuccess(Integer integer) {
                                                adapter.notifyItemRemoved(pod);
                                                sumAllItemCart();
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                                Toast.makeText(getContext(),"berhasil menghapus item", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onError(Throwable e) {
                                                Toast.makeText(getContext(),""+e.getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        });
                             }));
            }
        };

        sumAllItemCart();
    }

    private void initPlacesClient() {
        Places.initialize(getContext(),getString(R.string.google_maps_key));
        placesClient = Places.createClient(getContext());
    }

    private void sumAllItemCart() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double aDouble) {
                        txt_total_price.setText(new StringBuilder("Total: Rp. ").append(aDouble));
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!e.getMessage().contains("Query returned empty result set"))
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.action_settings).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.cart_menu,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_clear_cart){
            cartDataSource.cleanCart(Common.currentUser.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            Toast.makeText(getContext(), "Berhasil menghapus semua item", Toast.LENGTH_SHORT).show();
                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }
    @Override
    public void onStop() {
        EventBus.getDefault().postSticky(new HideFabCart(false));
        cartViewModel.onStop();
        if(EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        if(fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,Looper.getMainLooper());
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateItemCartEvent(UpdateItemCart event){
        if(event.getCartItem() != null){
            recyclerViewState = recycler_cart.getLayoutManager().onSaveInstanceState();
            cartDataSource.updateCartItem(event.getCartItem())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            calculateTotalPrice();
                            recycler_cart.getLayoutManager().onRestoreInstanceState(recyclerViewState);
                        }

                        @Override
                        public void onError(Throwable e) {
//                            Toast.makeText(getContext(), "[UPDATE KERANJANG]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    private void calculateTotalPrice() {
        cartDataSource.sumPriceInCart(Common.currentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double price) {
                        txt_total_price.setText(new StringBuilder("Total: Rp. ")
                        .append(Common.formatPrice(price)));
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!e.getMessage().contains("Query returned empty result set:"))
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onLoadSuccess(Order order, long estimateTimeInMs) {
        order.setCreateDate(estimateTimeInMs);
        order.setOrderStatus(0);
        writeOrderToFirebase(order);
    }

    @Override
    public void onLoadFailed(String message) {
        Toast.makeText(getContext(), ""+message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}