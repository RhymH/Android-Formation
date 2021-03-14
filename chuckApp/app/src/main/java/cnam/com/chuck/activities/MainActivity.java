package cnam.com.chuck.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import cnam.com.chuck.R;
import cnam.com.chuck.rest.ChuckRequest;
import cnam.com.chuck.rest.BasicListener;

public class MainActivity extends AppCompatActivity {

    private Button m_chuckButton;
    private TextView m_jokeTv;
    private Spinner m_spinner;
    private ImageView m_jokeIv;

    private List<String> m_categories = new ArrayList<>();

    private static MyHandler m_handler;
    private ChuckRequest m_chuckRequest;
    private ArrayAdapter<String> m_categoriesAdapter;

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            String joke = msg.getData().getString("joke");
            if( joke != null) {
                m_jokeTv.setText(joke);
            }
            ArrayList<String> categories = msg.getData().getStringArrayList("categories");
            if( categories != null) {
                m_categoriesAdapter.addAll(categories);
                m_categoriesAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m_handler = new MyHandler();
        m_chuckRequest = new ChuckRequest(this, m_handler);

        m_chuckButton = findViewById(R.id.chuckButton);
        m_jokeTv = findViewById(R.id.jokeTv);
        m_jokeIv = findViewById(R.id.jokeIcon);

        m_categories.add("Random");

        m_chuckRequest.getCategories(null);

        m_spinner = findViewById(R.id.categoriesSpinner);
        // Create an ArrayAdapter using the string array and a default spinner layout
        m_categoriesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, m_categories);
        // Apply the adapter to the spinner
        m_spinner.setAdapter(m_categoriesAdapter);
    }

    public void chuckOnClick(View view) {
        String category = (String)m_spinner.getSelectedItem();

        m_chuckRequest.getCategory(category, new BasicListener() {
            @Override
            public void onIconReceived(Bitmap image) {
                // callback
                m_jokeIv.setImageBitmap(image);
            }

            @Override
            public void onSuccess() {
            }

            @Override
            public void onError(int code) {
            }
        });
    }

}
