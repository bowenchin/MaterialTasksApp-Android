package com.bowenchin.android.materialtasks.model;

/**
 * Created by bowenchin on 25/7/2015.
 */

        import org.json.JSONException;
        import org.json.JSONObject;

public class Photo {
    private static final String JSON_FILENAME = "filename";
    private static final String JSON_ORIENTATION = "orientation";

    private final String mFilename;
    private final int mOrientation;

    /**
     * Create a Photo representing existing file on disk
     */
    public Photo(String filename, int orientation) {
        mFilename = filename;
        mOrientation = orientation;
    }

    public Photo(JSONObject json) throws JSONException {
        mFilename = json.getString(JSON_FILENAME);
        mOrientation = json.getInt(JSON_ORIENTATION);
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(JSON_FILENAME, mFilename);
        json.put(JSON_ORIENTATION, mOrientation);

        return json;
    }

    public String getFilename() {
        return mFilename;
    }

    public int getOrientation() {
        return mOrientation;
    }

}