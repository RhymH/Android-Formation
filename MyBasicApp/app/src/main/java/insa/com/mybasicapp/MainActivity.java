package insa.com.mybasicapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    public static final int REQUEST_CODE = 4567;
    public static final String MESSAGE_PARAMETER = "message";


    private Button m_addButton;
    private Button m_subButton;
    private TextView m_resultTv;
    private TextView m_resultFromActivity;
    private Button m_showButton;
    private Button m_openActivity;
    private Button m_openActivityForResult;
    private Button m_showSnackBar;
    private View m_openActivityLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_addButton = findViewById(R.id.addButton);
        m_subButton = findViewById(R.id.subButton);
        m_resultTv = findViewById(R.id.resultTextView);
        m_showButton = findViewById(R.id.showPopup);
        m_showSnackBar = findViewById(R.id.showSnackBar);
        m_resultFromActivity = findViewById(R.id.resultFromActivity);
        m_openActivityLayout = findViewById(R.id.openActivityLayout);

        m_openActivity = findViewById(R.id.openActivity);
        m_openActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SecondActivity.class);
                startActivity(intent);
            }
        });

        m_openActivityForResult = findViewById(R.id.openActivityForResult);
        m_openActivityForResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ThirdActivity.class);
                //Bundle bundle = new Bundle();
                //bundle.putString("message", "Are you ready?");
                //intent.putExtras(bundle);
                intent.putExtra(MESSAGE_PARAMETER, "Are you ready?");
                startActivityForResult(intent, REQUEST_CODE);
            }
        });
    }


    @Override
    public void onClick(View view) {
        Log.v("MainActivity", ">onClick");

        if( view == m_addButton) {
            Log.v("MainActivity", ">onClick : Add button clicked");
            String resultStrg = m_resultTv.getText().toString();
            int result = Integer.valueOf(resultStrg);
            result++;
            m_resultTv.setText(Integer.toString(result));
        } else if( view == m_subButton) {
            Log.v("MainActivity", ">onClick : Substract button clicked");
            String resultStrg = m_resultTv.getText().toString();
            int result = Integer.valueOf(resultStrg);
            result--;
            m_resultTv.setText(Integer.toString(result));
        } else if( view == m_showButton) {
            Log.v("MainActivity", ">onClick : ShowButton button clicked");
            Toast.makeText(this, "Voici la popup en question", Toast.LENGTH_LONG).show();
        } else if( view == m_showSnackBar) {
            Log.v("MainActivity", ">onClick : ShowSnackBar button clicked");
            Snackbar.make(view, "New Popup design", Snackbar.LENGTH_LONG)
                    .setAction("My Action", null).show();
        } else {
            Log.v("MainActivity", ">onClick : OTHER button clicked -- Not Managed yet");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE) {
            if(resultCode == Activity.RESULT_OK) {

                m_openActivityLayout.setBackgroundColor(ContextCompat.getColor( this, android.R.color.holo_green_light) );
                String result = data.getStringExtra("result");
                m_resultFromActivity.setText(result);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                m_openActivityLayout.setBackgroundColor( ContextCompat.getColor( this, android.R.color.holo_red_light) );
                String result = data.getStringExtra("result");
                m_resultFromActivity.setText(result);
            }
        }
    }
}
