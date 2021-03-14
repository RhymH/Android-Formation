package cnam.com.mytestchuck.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import cnam.com.mytestchuck.R;
import cnam.com.mytestchuck.rest.ChuckRequest;

public class MainActivity extends AppCompatActivity {

    private Spinner m_spinner;
    private ArrayAdapter m_categoriesAdapter;

    private List<String> m_categories = new ArrayList<>();
    private ChuckRequest m_chuckRequest;
    private MyHandler m_handler;
    private TextView m_jokeTv;

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            String joke = msg.getData().getString("joke");
            if ( joke != null ) {
                m_jokeTv.setText(joke);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_handler = new MyHandler();
        m_chuckRequest = new ChuckRequest(this, m_handler);

        m_categories.add("Random");

        m_jokeTv = findViewById(R.id.jokeTv);

        m_spinner = findViewById(R.id.categoriesSpinner);
        m_categoriesAdapter = new ArrayAdapter<>( this, android.R.layout.simple_spinner_dropdown_item, m_categories);
        m_spinner.setAdapter(m_categoriesAdapter);
    }

    public void chuckOnClick(View view) {
        m_chuckRequest.getRandom();
    }

}
