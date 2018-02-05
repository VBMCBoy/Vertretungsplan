package com.KayKaprolat.Praktikum.Vertretungsplan2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {


  @Override
  public void onReceive(Context context, Intent intent) {
    // ViewerActivity.notification();

    Toast.makeText(context, "HAAALLOOOO", Toast.LENGTH_SHORT).show();
  }


}
