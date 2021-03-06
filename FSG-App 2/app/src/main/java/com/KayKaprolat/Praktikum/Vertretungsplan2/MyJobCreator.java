package com.KayKaprolat.Praktikum.Vertretungsplan2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

public class MyJobCreator implements JobCreator {

  @Override
  @Nullable
  public Job create(@NonNull String tag) {
    switch (tag) {
      case MySyncJob.TAG:
        return new MySyncJob();
      default:
        return null;
    }
  }

}
