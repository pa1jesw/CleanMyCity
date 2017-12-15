package com.pa1.cmc;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

import com.google.android.gms.location.LocationServices;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;
public class MainActivity extends AppCompatActivity
implements
GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,View.OnClickListener
{

    ImageView ivphoto;
    Button btnSend, btnCamera;
    TextView tvLoc;
    Bitmap photo;
    GoogleApiClient mLoct;
    Location mlastLoct;
    LocationManager locationManager;
    String loct;


    public void onBackPressed() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you really want to Exit.?");
        builder.setCancelable(false);

        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                }
        );
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.cancel();
            }
        });
        builder.setTitle("Exit Confirm");
        builder.show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        ivphoto = (ImageView) findViewById(R.id.ivPhoto);
        btnCamera = (Button) findViewById(R.id.btnCamera);
        tvLoc = (TextView) findViewById(R.id.tvLocation);
        btnSend = (Button) findViewById(R.id.btnSend);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent ci=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(ci, 100);

            }
        });
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!(loct.toString() =="")){
                    Drawable mdr = ivphoto.getDrawable();
                    Bitmap bitm= ((BitmapDrawable)mdr).getBitmap();
                    String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitm, " Address"+tvLoc.getText().toString(), null);
                    Uri uri = Uri.parse(path);
                    Intent share= new Intent(Intent.ACTION_SEND);
                    share.setType("image/jpeg");
                    share.putExtra(Intent.EXTRA_STREAM, uri);
                    share.putExtra("text",loct);
                    startActivity(Intent.createChooser(share, "Share Image"));

                }
            }
        });

        GoogleApiClient.Builder builderl = new GoogleApiClient.Builder(this);
        builderl.addApi(LocationServices.API);
        builderl.addConnectionCallbacks(this);
        builderl.addOnConnectionFailedListener(this);
        mLoct=builderl.build();
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        if(mLoct != null)
            mLoct.connect();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100 && resultCode == RESULT_OK)
        {
            photo = (Bitmap )data.getExtras().get("data");
            ivphoto.setImageBitmap(photo);

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mlastLoct = LocationServices.FusedLocationApi.getLastLocation(mLoct);
        if(mlastLoct != null) {
            double latitude = mlastLoct.getLatitude();
            double longitude = mlastLoct.getLongitude();

            Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);

            try {
                List<android.location.Address> fetchAddress = geocoder.getFromLocation(latitude, longitude, 1);
                if (fetchAddress != null) {
                    android.location.Address myAdd = fetchAddress.get(0);
                    loct = "" + myAdd.getLocality() + "," + myAdd.getSubLocality() + "-" + myAdd.getPostalCode() + "\n" +
                            myAdd.getAdminArea() + "," + myAdd.getCountryName();

                    tvLoc.setText( myAdd.getLocality() + "," + myAdd.getSubLocality() + "-" + myAdd.getPostalCode() + "\n" +
                            myAdd.getAdminArea() + "," + myAdd.getCountryName());
                } else
                    tvLoc.setText("No loaction Found");
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(getApplicationContext(),"Connection Suspended",Toast.LENGTH_LONG).show();

    }

    @Override
    public void onClick(View view) {
        try{
            File file =  new File(getExternalCacheDir(),""+loct+".png");
            FileOutputStream fout = new FileOutputStream(file);
            photo.compress(Bitmap.CompressFormat.JPEG,90,fout);
            fout.close();

            Intent si = new Intent(Intent.ACTION_SEND);
            String sh = tvLoc.getText().toString();
            si.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            si.putExtra(Intent.EXTRA_TEXT, sh);
            si.setType("image/png");
            startActivity(si);
        }
        catch (Exception e)
        {
            e.getStackTrace();
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(getApplicationContext(),"Connection Failed",Toast.LENGTH_LONG).show();
    }

}
