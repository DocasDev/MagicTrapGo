package dev.docas.magictrapgo;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.slider.Slider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    private final HashMap<String, Pokemon> pokemons = new HashMap<String, Pokemon>();
    private String[] pokemonNames;

    private AutoCompleteTextView pokemonInput;
    private EditText cpInput;
    private TextView result;
    private Button searchBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadDatabase();
        createDropDownList();
        searchListener();
    }

    private void searchListener(){
        pokemonInput = findViewById(R.id.pokemonList);
        cpInput = findViewById(R.id.pokemonCP);
        searchBtn = findViewById(R.id.searchBtn);
        result = findViewById(R.id.result);

        searchBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                pokemonInput.clearFocus();
                cpInput.clearFocus();
                result.setText("");

                if(pokemonInput.getText().length() == 0 ||
                        cpInput.getText().length() == 0)
                    return;

                Pokemon pokemon = pokemons.get(pokemonInput.getText().toString());
                if(pokemon == null)
                    return;

                closeKeyboard();

                IVCalculator ivCalculator = new IVCalculator(pokemon);
                ArrayList<Double> ivs = ivCalculator.discovery(Integer.parseInt(cpInput.getText().toString()), 1);
                DecimalFormat df = new DecimalFormat("#,##0.0'%'");
                if(ivs.size() == 0)
                    result.setText("IV not found");
                else if(ivs.size() == 1)
                    result.setText(df.format(ivs.get(0)));
                else
                    result.setText(df.format(ivs.get(0)) + " - " + df.format(ivs.get(1)));
            }
        });
    }

    private void closeKeyboard()
    {
        InputMethodManager manager
                = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void loadDatabase() {
        String jsonString;
        try {
            InputStream inputStream = getAssets().open("pokemon_stats.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();

            jsonString = new String(buffer, "UTF-8");
            JSONArray jsonArray = new JSONArray(jsonString);
            //Log.e("MainActivity", "The json is: " + jsonString);

            for(int i = 0; i < jsonArray.length();i++){
                JSONObject obj = jsonArray.getJSONObject(i);
                String name = obj.getString("pokemon_name");
                if(!obj.getString("form").equals("Normal"))
                    name += " (" + obj.getString("form") + ")";

                Pokemon pokemon = new Pokemon(
                        obj.getInt("pokemon_id"),
                        name,
                        obj.getInt("base_attack"),
                        obj.getInt("base_defense"),
                        obj.getInt("base_stamina")
                );
                pokemons.put(name, pokemon);
            }

            Toast.makeText(getApplicationContext(), pokemons.size() + " Pokemons loaded" , Toast.LENGTH_LONG).show();
            Log.e("MainActivity", "Amount pokemons: " + pokemons.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createDropDownList(){
        //Spinner spin = (Spinner) findViewById(R.id.pokemonList);
        //spin.setOnItemSelectedListener(this);

        pokemonNames = new String[pokemons.size()];

        int i = 0;
        for ( Pokemon p : pokemons.values() ) {
            pokemonNames[i] = p.getName();
            i++;
        }

        Arrays.sort(pokemonNames);

        Log.e("MainActivity", "Pokemon List: " + pokemonNames.length);

        //Creating the instance of ArrayAdapter containing list of fruit names
        ArrayAdapter<String> adapter = new ArrayAdapter<String>
                (this, android.R.layout.select_dialog_item, pokemonNames);
        //Getting the instance of AutoCompleteTextView
        AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.pokemonList);
        actv.setThreshold(1);//will start working from first character
        actv.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
    }
}