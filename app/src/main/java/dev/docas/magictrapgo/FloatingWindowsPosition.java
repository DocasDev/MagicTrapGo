package dev.docas.magictrapgo;

import java.util.HashMap;

public class FloatingWindowsPosition {
    private static FloatingWindowsPosition _instance = null;

    private HashMap<String, WindowPosition> positions;

    private FloatingWindowsPosition()
    {
        positions = new HashMap<>();
    }

    public static FloatingWindowsPosition getInstance(){
        if(_instance == null)
            _instance = new FloatingWindowsPosition();
        return _instance;
    }

    public void registerWindowPosition(String windowsName, WindowPosition position){
        positions.put(windowsName,position);
    }

    public WindowPosition getWindowPosition(String windowsName){
        return positions.get(windowsName);
    }
}
