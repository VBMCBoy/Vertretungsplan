package com.KayKaprolat.Praktikum.Vertretungsplan2;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SyncService extends Service {

  private static final Object syncAdapterLock = new Object();
  private static SyncAdapter syncAdapter;

  public SyncService() {
  }

  @Override
  public void onCreate() {
    super.onCreate();
    synchronized (SyncService.syncAdapterLock) {
      if (SyncService.syncAdapter == null) {
        SyncService.syncAdapter = new SyncAdapter(this.getApplicationContext(), true);
      }
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return SyncService.syncAdapter.getSyncAdapterBinder();
  }

}
