package com.iotteam3.ConnectedStudents.ui.bluetooth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.iotteam3.ConnectedStudents.R;

public class BluetoothFragment extends Fragment {

    private BluetoothViewModel bluetoothViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        bluetoothViewModel =
                ViewModelProviders.of(this).get(BluetoothViewModel.class);
        View root = inflater.inflate(R.layout.fragment_bluetooth, container, false);
        final TextView textView = root.findViewById(R.id.text_bluetooth);
        bluetoothViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}