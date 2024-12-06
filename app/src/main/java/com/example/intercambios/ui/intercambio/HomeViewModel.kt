package com.example.intercambios.ui.intercambio

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is home Fragment"
        value = "No tienes intercambios actualmente."
    }
    val text: LiveData<String> = _text
}