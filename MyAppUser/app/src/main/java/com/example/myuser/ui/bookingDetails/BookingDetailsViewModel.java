package com.example.myuser.ui.bookingDetails;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BookingDetailsViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public BookingDetailsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is gallery fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}