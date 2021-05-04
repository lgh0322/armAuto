package com.vaca.modifiId

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.InputFilter
import android.widget.EditText

class MainActivity : AppCompatActivity() {
    lateinit var x1:EditText
    lateinit var x2:EditText
    lateinit var x3:EditText
    lateinit var x4:EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        x1=findViewById(R.id.x1)
        x2=findViewById(R.id.x2)
        x3=findViewById(R.id.x3)
        x4=findViewById(R.id.x4)
        x1.filters= arrayOf(InputFilterMinMax("0","255"))
    }
}