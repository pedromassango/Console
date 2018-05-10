package com.pedromassango.console.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Patterns;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pedromassango.console.R;
import com.pedromassango.console.ui.console.TerminalActivity;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.internal.markers.KMutableList;

public class MainActivity extends AppCompatActivity implements Function1<AttackOption, Unit> {

    private static ArrayList<AttackOption> attackOptions = new ArrayList<>();

    static {
        attackOptions.add(new AttackOption(AttackOptions.TERMINAL));
        attackOptions.add(new AttackOption(AttackOptions.DOS));
        attackOptions.add(new AttackOption(AttackOptions.DDOS));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recycler_attack_options);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // adapter para listar os tipos de ataques.
        AttackOptionsAdapter attackOptionsAdapter = new AttackOptionsAdapter(attackOptions, this);
        recyclerView.setAdapter(attackOptionsAdapter);
    }

    @Override
    public Unit invoke(AttackOption attackOption) {
        switch (attackOption.getType()) {
            case TERMINAL: // Caso o tipo de ataque seja o terminal, inicia a atividade de terminal.
                startActivity(new Intent(this, TerminalActivity.class));
                break;
        }
        return null;
    }
}
