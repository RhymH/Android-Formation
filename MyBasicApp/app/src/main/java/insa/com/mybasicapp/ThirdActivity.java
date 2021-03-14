package insa.com.mybasicapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import static insa.com.mybasicapp.MainActivity.MESSAGE_PARAMETER;

public class ThirdActivity extends AppCompatActivity {

    private TextView m_messageTv;
    private Button m_yesButton;
    private Button m_noButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_third);

        Bundle extras = getIntent().getExtras();
        String message = extras.getString(MESSAGE_PARAMETER);

        m_messageTv = findViewById(R.id.messageTv);
        m_messageTv.setText(message);

        m_yesButton = findViewById(R.id.yesButton);
        m_noButton = findViewById(R.id.noButton);
    }

    public void onClick(View view) {
        Log.v("ThirdActivity", ">onClick");
        if( view == m_yesButton) {
        } else if( view == m_noButton) {
        }
    }

    @Override
    public void onBackPressed() {
    }

    public void onClickYes(View view) {
        Log.v("ThirdActivity", ">onClick : Add button clicked");
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", "I am READY");
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    public void onClickNo(View view) {
        Log.v("ThirdActivity", ">onClick : Substract button clicked");
        Intent intent = new Intent();
        intent.putExtra("result", "I am NOT READY");
        setResult(Activity.RESULT_CANCELED, intent);
        finish();
    }
}
