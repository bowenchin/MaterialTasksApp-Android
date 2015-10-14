package com.bowenchin.android.materialtasks.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.UUID;

/**
 * Created by bowenchin on 21/7/2015.
 */
public class Task {
    private UUID mId;
    private String mTitle;
    private Date mDate;
    private boolean mChecked;
    private String mSubject;
    private Photo mPhoto;

    private static final String JSON_ID = "id";
    private static final String JSON_TITLE = "title";
    private static final String JSON_SUBJECT = "subject";
    private static final String JSON_SOLVED = "solved";
    private static final String JSON_DATE = "date";
    private static final String JSON_PHOTO = "photo";

    public Task(){
        //Generate unique identifier
        mId = UUID.randomUUID();
        mDate = new Date();
    }

    public Task(JSONObject json) throws JSONException{
        mId = UUID.fromString(json.getString(JSON_ID));
        mTitle = json.getString(JSON_TITLE);
        mChecked = json.getBoolean(JSON_SOLVED);
        mDate = new Date(json.getLong(JSON_DATE));
        mSubject = json.getString(JSON_SUBJECT);
        if(json.has(JSON_PHOTO))
            mPhoto = new Photo(json.getJSONObject(JSON_PHOTO));
    }

    public JSONObject toJSON() throws JSONException{
        JSONObject json = new JSONObject();
        json.put(JSON_ID,mId.toString());
        json.put(JSON_TITLE,mTitle);
        json.put(JSON_SUBJECT,mSubject);
        json.put(JSON_SOLVED,mChecked);
        json.put(JSON_DATE,mDate.getTime());
        if(mPhoto != null)
            json.put(JSON_PHOTO,mPhoto.toJSON());
        return json;
    }

    @Override
    public String toString(){
        return mTitle;
    }

    public UUID getId(){
        return mId;
    }

    public String getTitle(){
        return mTitle;
    }
    public void setTitle(String title){
        mTitle = title;
    }

    public String getSubject(){return mSubject;}
    public void setSubject(String subject){mSubject = subject;}

    public Date getDate(){return mDate;}
    public void setDate(Date date){
        mDate = date;
    }

    public boolean isChecked(){
       return mChecked;
    }
    public void setChecked(boolean checked){
        mChecked = checked;
    }

    public Photo getPhoto(){return mPhoto;}
    public void setPhoto(Photo p){mPhoto = p;}
}
