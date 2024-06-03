package dev.docas.magictrapgo;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.util.stream.IntStream;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class FloatingWindowIVCalculator extends Service {
    private final HashMap<String, Pokemon> pokemons = new HashMap<String, Pokemon>();
    private String[] pokemonNames;

    private AutoCompleteTextView pokemonInput;
    private EditText cpInput;
    private TextView result;
    private ImageView searchBtn;
    private CustomImageView toggleBtn;
    private ImageView closeBtn;
    private RadioGroup captureTypes;

    // The reference variables for the
    // ViewGroup, WindowManager.LayoutParams,
    // WindowManager, Button, EditText classes are created
    private ViewGroup floatView;
    private int LAYOUT_TYPE;
    private WindowManager.LayoutParams floatWindowLayoutParam;
    private WindowManager windowManager;
    private boolean keyboardOpened = false;

    // As FloatingWindowGFG inherits Service class,
    // it actually overrides the onBind method
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // The screen height and width are calculated, cause
        // the height and width of the floating window is set depending on this
        DisplayMetrics metrics = getApplicationContext().getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        // To obtain a WindowManager of a different Display,
        // we need a Context for that display, so WINDOW_SERVICE is used
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // A LayoutInflater instance is created to retrieve the
        // LayoutInflater for the floating_layout xml
        LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);

        // inflate a new view hierarchy from the floating_layout xml
        floatView = (ViewGroup) inflater.inflate(R.layout.floating_layout, null);

        pokemonInput = floatView.findViewById(R.id.pokemonList);
        cpInput = floatView.findViewById(R.id.pokemonCP);
        searchBtn = floatView.findViewById(R.id.searchBtn);
        toggleBtn = floatView.findViewById(R.id.toggleBtn);
        closeBtn = floatView.findViewById(R.id.closeBtn);
        result = floatView.findViewById(R.id.result);
        captureTypes = floatView.findViewById(R.id.capture_types);

        // WindowManager.LayoutParams takes a lot of parameters to set the
        // the parameters of the layout. One of them is Layout_type.
        LAYOUT_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;

        // translucency by PixelFormat.TRANSLUCENT
        floatWindowLayoutParam = new WindowManager.LayoutParams(
                //(int) (width * (0.55f)),
                (int) (width * (0.7f)),
                (int) (height * (0.58f)),
                LAYOUT_TYPE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );

        // The Gravity of the Floating Window is set.
        // The Window will appear in the center of the screen
        floatWindowLayoutParam.gravity = Gravity.CENTER;

        // X and Y value of the window is set
        floatWindowLayoutParam.x = 0;
        floatWindowLayoutParam.y = 0;

        // The ViewGroup that inflates the floating_layout.xml is
        // added to the WindowManager with all the parameters
        windowManager.addView(floatView, floatWindowLayoutParam);

        loadDatabase();
        createDropDownList();
        searchListener();
        closeListener();
        toggleListener();
        touchListener();
    }

    private void searchListener(){
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

                hideKeyboard();

                CatchParameters catchParameters = getCatchParameters(pokemon.getType());
                IVCalculator ivCalculator = new IVCalculator(pokemon);
                ArrayList<Double> ivs = ivCalculator.discovery(
                        Integer.parseInt(cpInput.getText().toString()),
                        catchParameters
                );
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

    private CatchParameters getCatchParameters(String type){
        int selectedId = captureTypes.getCheckedRadioButtonId();
        if(selectedId == R.id.wild_type)
            return new CatchParameters(1, 15, IntStream.range(1, 36).toArray());
            //return new int[] {1, 15, 1, 35};
        else if(selectedId == R.id.research_type)
            return new CatchParameters(10, 15, IntStream.range(1, 36).toArray());
            //return new int[] {10, 15};
        else if(selectedId == R.id.raid_type && type.equals("S"))
            return new CatchParameters(6, 15, new int[] { 20, 25 });
            //return new int[] {10, 20};
        else if(selectedId == R.id.raid_type)
            return new CatchParameters(10, 15, new int[] { 20, 25 });


        return new CatchParameters(1, 15, IntStream.range(1, 36).toArray());
    }

    private void hideKeyboard() {
        InputMethodManager manager
                = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        manager.hideSoftInputFromWindow(floatView.getApplicationWindowToken(), 0);
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
                        obj.getInt("base_stamina"),
                        obj.getString("form")
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
        AutoCompleteTextView actv = (AutoCompleteTextView) floatView.findViewById(R.id.pokemonList);
        actv.setThreshold(1);//will start working from first character
        actv.setAdapter(adapter);//setting the adapter data into the AutoCompleteTextView
    }

    private void closeListener(){
        // The button that helps to maximize the app
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // stopSelf() method is used to stop the service if
                // it was previously started
                stopSelf();

                // The window is removed from the screen
                windowManager.removeView(floatView);
            }
        });
    }

    private void toggleListener(){
        // The button that helps to maximize the app
        toggleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // stopSelf() method is used to stop the service if
                // it was previously started
                stopSelf();

                // The window is removed from the screen
                windowManager.removeView(floatView);

                startService(new Intent(FloatingWindowIVCalculator.this, FloatingWindowButton.class));
            }
        });
    }

    private void touchListener(){
        // Another feature of the floating window is, the window is movable.
        // The window can be moved at any position on the screen.
        floatView.setOnTouchListener(new View.OnTouchListener() {
            final WindowManager.LayoutParams floatWindowLayoutUpdateParam = floatWindowLayoutParam;
            double x;
            double y;
            double px;
            double py;

            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()) {
                    // When the window will be touched,
                    // the x and y position of that position
                    // will be retrieved
                    case MotionEvent.ACTION_DOWN:
                        x = floatWindowLayoutUpdateParam.x;
                        y = floatWindowLayoutUpdateParam.y;

                        // returns the original raw X
                        // coordinate of this event
                        px = event.getRawX();

                        // returns the original raw Y
                        // coordinate of this event
                        py = event.getRawY();
                        break;
                    // When the window will be dragged around,
                    // it will update the x, y of the Window Layout Parameter
                    case MotionEvent.ACTION_MOVE:
                        floatWindowLayoutUpdateParam.x = (int) ((x + event.getRawX()) - px);
                        floatWindowLayoutUpdateParam.y = (int) ((y + event.getRawY()) - py);

                        // updated parameter is applied to the WindowManager
                        windowManager.updateViewLayout(floatView, floatWindowLayoutUpdateParam);
                        break;
                }
                return false;
            }
        });
    }

    // It is called when stopService()
    // method is called in MainActivity
    @Override
    public void onDestroy() {
        stopSelf();
        // Window is removed from the screen
        windowManager.removeView(floatView);
        super.onDestroy();
    }
}